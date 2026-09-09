package com.repforge.core.domain.model

/**
 * Unit system preference for the user.
 * All internal storage is in metric (kg, cm). Conversion happens at the display layer only.
 */
sealed class UnitSystem {
    data object Metric : UnitSystem()
    data object Imperial : UnitSystem()

    companion object {
        fun fromString(value: String): UnitSystem = when (value.lowercase()) {
            "imperial" -> Imperial
            else -> Metric
        }
    }

    override fun toString(): String = when (this) {
        is Metric -> "metric"
        is Imperial -> "imperial"
    }
}

/**
 * Biological sex for basal metabolic rate and caloric calculations.
 */
enum class BiologicalSex {
    MALE,
    FEMALE;
}

/**
 * Training experience level. Calibrates EPOC and work capacity estimations.
 */
enum class TrainingExperience(val displayName: String) {
    BEGINNER("Beginner"),
    INTERMEDIATE("Intermediate"),
    ADVANCED("Advanced");
}

/**
 * User profile with biometrics and preferences.
 * Pure domain model — storage is always in metric units (kg, cm).
 */
data class UserProfile(
    val id: String,
    val email: String = "",
    val weightKg: Double? = null,
    val heightCm: Double? = null,
    val age: Int? = null,
    val biologicalSex: BiologicalSex? = null,
    val bodyFatPercentage: Double? = null,
    val trainingExperience: TrainingExperience = TrainingExperience.INTERMEDIATE,
    val unitSystem: UnitSystem = UnitSystem.Metric,
    val createdAtMillis: Long = System.currentTimeMillis()
)

/**
 * Unit conversion utilities.
 * Internal storage is always metric. These convert for display purposes only.
 */
object UnitConverter {
    // Weight conversions
    const val KG_TO_LB = 2.20462
    const val LB_TO_KG = 0.453592

    fun kgToLb(kg: Double): Double = kg * KG_TO_LB
    fun lbToKg(lb: Double): Double = lb * LB_TO_KG

    // Height conversions
    const val CM_TO_INCH = 0.393701
    const val INCH_TO_CM = 2.54

    fun cmToFeetInches(cm: Double): Pair<Int, Int> {
        val totalInches = cm * CM_TO_INCH
        val feet = (totalInches / 12).toInt()
        val inches = (totalInches % 12).toInt()
        return feet to inches
    }

    fun feetInchesToCm(feet: Int, inches: Int): Double {
        val totalInches = (feet * 12) + inches
        return totalInches * INCH_TO_CM
    }

    /**
     * Format weight for display based on unit system.
     */
    fun formatWeight(kg: Double, unitSystem: UnitSystem): String = when (unitSystem) {
        is UnitSystem.Metric -> "%.1f kg".format(kg)
        is UnitSystem.Imperial -> "%.1f lb".format(kgToLb(kg))
    }

    /**
     * Format height for display based on unit system.
     */
    fun formatHeight(cm: Double, unitSystem: UnitSystem): String = when (unitSystem) {
        is UnitSystem.Metric -> "%.0f cm".format(cm)
        is UnitSystem.Imperial -> {
            val (feet, inches) = cmToFeetInches(cm)
            "$feet'$inches\""
        }
    }

    /**
     * Convert display weight input to internal metric (kg) for storage.
     */
    fun displayWeightToKg(value: Double, unitSystem: UnitSystem): Double = when (unitSystem) {
        is UnitSystem.Metric -> value
        is UnitSystem.Imperial -> lbToKg(value)
    }

    /**
     * Convert internal metric (kg) to display weight.
     */
    fun kgToDisplayWeight(kg: Double, unitSystem: UnitSystem): Double = when (unitSystem) {
        is UnitSystem.Metric -> kg
        is UnitSystem.Imperial -> kgToLb(kg)
    }
}

/**
 * Available barbell plates for plate math calculation, per unit system.
 */
object PlateMath {
    /** Standard metric Olympic plates in kg. */
    val metricPlates = listOf(25.0, 20.0, 15.0, 10.0, 5.0, 2.5, 1.25)

    /** Standard imperial plates in lb. */
    val imperialPlates = listOf(45.0, 35.0, 25.0, 10.0, 5.0, 2.5)

    /** Standard bar weights in kg. */
    val standardBarWeights = mapOf(
        "Olympic Barbell (20kg)" to 20.0,
        "Women's Barbell (15kg)" to 15.0,
        "EZ Curl Bar (10kg)" to 10.0,
        "Trap Bar (25kg)" to 25.0
    )

    /**
     * Calculate plates needed per side for a given total load.
     * Uses a greedy algorithm: largest plates first.
     *
     * @param totalWeightKg Total weight including the bar, in kg.
     * @param barWeightKg Weight of the bar in kg.
     * @param unitSystem Determines which plate set to use.
     * @return List of plate weights per side, in the display unit.
     */
    fun calculatePlatesPerSide(
        totalWeightKg: Double,
        barWeightKg: Double = 20.0,
        unitSystem: UnitSystem = UnitSystem.Metric
    ): List<Double> {
        val weightPerSide = when (unitSystem) {
            is UnitSystem.Metric -> (totalWeightKg - barWeightKg) / 2.0
            is UnitSystem.Imperial -> (UnitConverter.kgToLb(totalWeightKg) - UnitConverter.kgToLb(barWeightKg)) / 2.0
        }

        if (weightPerSide <= 0) return emptyList()

        val plates = when (unitSystem) {
            is UnitSystem.Metric -> metricPlates
            is UnitSystem.Imperial -> imperialPlates
        }

        val result = mutableListOf<Double>()
        var remaining = weightPerSide

        for (plate in plates) {
            while (remaining >= plate - 0.001) { // Floating point tolerance
                result.add(plate)
                remaining -= plate
            }
        }

        return result
    }
}
