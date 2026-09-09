package com.repforge.core.domain.engine

import com.repforge.core.domain.model.BiologicalSex
import com.repforge.core.domain.model.SetType
import com.repforge.core.domain.model.TrainingExperience
import com.repforge.core.domain.model.UserProfile
import com.repforge.core.domain.model.WorkoutSet

/**
 * Bio-Energetic Caloric Engine for Resistance Training.
 *
 * Implements the 3-factor scientific formula per PRD Section 7.2:
 * 1. Basal Metabolic Component: Mifflin-St Jeor BMR scaled by resistance training MET (3.5 - 6.0).
 * 2. Mechanical Volume Load: Tonnage (kg) scaled against athlete's body weight.
 * 3. EPOC Modifier: Elevated fatigue factor for failure sets, drop sets, and short rest intervals.
 *
 * Enforces an honest ±15% confidence band labeled as "Rough Estimate".
 */
object CaloricEngine {

    data class CalorieEstimate(
        val basalKcal: Double,
        val mechanicalKcal: Double,
        val epocKcal: Double,
        val totalKcal: Double,
        val lowBoundKcal: Double,
        val highBoundKcal: Double,
        val confidenceBandPercentage: Double = 15.0
    )

    /**
     * Calculate Mifflin-St Jeor Basal Metabolic Rate (BMR) in kcal/day.
     */
    fun calculateBMR(
        weightKg: Double,
        heightCm: Double,
        age: Int,
        sex: BiologicalSex
    ): Double {
        val base = (10.0 * weightKg) + (6.25 * heightCm) - (5.0 * age)
        return when (sex) {
            BiologicalSex.MALE -> base + 5.0
            BiologicalSex.FEMALE -> base - 161.0
        }
    }

    /**
     * Calculate caloric expenditure for a resistance training session.
     */
    fun estimateSessionCalories(
        userProfile: UserProfile?,
        durationMinutes: Double,
        completedSets: List<WorkoutSet>,
        averageHeartRateBpm: Double? = null
    ): CalorieEstimate {
        val weightKg = userProfile?.weightKg ?: 75.0
        val heightCm = userProfile?.heightCm ?: 175.0
        val age = userProfile?.age ?: 25
        val sex = userProfile?.biologicalSex ?: BiologicalSex.MALE

        // 1. Basal Component (BMR * MET * time)
        val dailyBMR = calculateBMR(weightKg, heightCm, age, sex)
        val bmrPerMinute = dailyBMR / 1440.0 // 24 * 60

        // Resistance training MET: typically 5.0 for standard strength, 6.0 if high heart rate
        val met = if (averageHeartRateBpm != null && averageHeartRateBpm > 130) {
            6.0
        } else {
            5.0
        }
        val basalKcal = bmrPerMinute * met * durationMinutes

        // 2. Mechanical Volume Load
        // Working & failure sets count towards tonnage
        val totalTonnage = completedSets
            .filter { it.isCompleted && it.setType != SetType.WARMUP }
            .sumOf { it.weightKg * it.repsCompleted }

        // Mechanical expenditure scaled to body weight
        val weightScaling = (weightKg / 75.0).coerceIn(0.7, 1.5)
        val mechanicalKcal = (totalTonnage * 0.018) * weightScaling

        // 3. EPOC Modifier (Excess Post-Exercise Oxygen Consumption)
        val failureCount = completedSets.count { it.isCompleted && it.setType == SetType.FAILURE }
        val dropSetCount = completedSets.count { it.isCompleted && it.setType == SetType.DROP_SET }
        val workingCount = completedSets.count { it.isCompleted && it.setType == SetType.WORKING }

        val experienceFactor = when (userProfile?.trainingExperience) {
            TrainingExperience.BEGINNER -> 1.15
            TrainingExperience.INTERMEDIATE -> 1.0
            TrainingExperience.ADVANCED -> 0.90
            null -> 1.0
        }

        val epocBase = (failureCount * 8.0) + (dropSetCount * 6.0) + (workingCount * 2.5)
        val epocKcal = epocBase * experienceFactor

        val totalKcal = basalKcal + mechanicalKcal + epocKcal
        val lowBound = totalKcal * 0.85
        val highBound = totalKcal * 1.15

        return CalorieEstimate(
            basalKcal = basalKcal,
            mechanicalKcal = mechanicalKcal,
            epocKcal = epocKcal,
            totalKcal = totalKcal,
            lowBoundKcal = lowBound,
            highBoundKcal = highBound
        )
    }
}
