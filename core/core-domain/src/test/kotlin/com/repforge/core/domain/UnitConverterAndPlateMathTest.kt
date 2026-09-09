package com.repforge.core.domain

import com.repforge.core.domain.engine.CaloricEngine
import com.repforge.core.domain.model.BiologicalSex
import com.repforge.core.domain.model.PlateMath
import com.repforge.core.domain.model.SetType
import com.repforge.core.domain.model.TrainingExperience
import com.repforge.core.domain.model.UnitConverter
import com.repforge.core.domain.model.UnitSystem
import com.repforge.core.domain.model.UserProfile
import com.repforge.core.domain.model.WorkoutSet
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class UnitConverterAndPlateMathTest {

    @Test
    fun `test weight conversion kg to lb and back`() {
        val kg = 100.0
        val lb = UnitConverter.kgToLb(kg)
        assertEquals(220.462, lb, 0.01)

        val backToKg = UnitConverter.lbToKg(lb)
        assertEquals(100.0, backToKg, 0.01)
    }

    @Test
    fun `test height conversion cm to feet inches`() {
        val cm = 180.0
        val (feet, inches) = UnitConverter.cmToFeetInches(cm)
        assertEquals(5, feet)
        assertEquals(10, inches) // 180 cm ~ 70.86 inches -> 5 ft 10 in

        val backToCm = UnitConverter.feetInchesToCm(feet, inches)
        assertEquals(177.8, backToCm, 0.1)
    }

    @Test
    fun `test greedy plate math for 100kg total with 20kg bar`() {
        // (100 - 20) / 2 = 40 kg per side -> 25kg + 15kg = 40kg
        val plates = PlateMath.calculatePlatesPerSide(
            totalWeightKg = 100.0,
            barWeightKg = 20.0,
            unitSystem = UnitSystem.Metric
        )
        assertEquals(listOf(25.0, 15.0), plates)
    }

    @Test
    fun `test greedy plate math when weight equals bar weight returns empty list`() {
        val plates = PlateMath.calculatePlatesPerSide(
            totalWeightKg = 20.0,
            barWeightKg = 20.0,
            unitSystem = UnitSystem.Metric
        )
        assertTrue(plates.isEmpty())
    }

    @Test
    fun `test BMR calculation Mifflin-St Jeor`() {
        // 80kg, 180cm, 28yo, Male: 10*80 + 6.25*180 - 5*28 + 5 = 800 + 1125 - 140 + 5 = 1790
        val bmrMale = CaloricEngine.calculateBMR(80.0, 180.0, 28, BiologicalSex.MALE)
        assertEquals(1790.0, bmrMale, 0.01)

        // Female offset is -161: 800 + 1125 - 140 - 161 = 1624
        val bmrFemale = CaloricEngine.calculateBMR(80.0, 180.0, 28, BiologicalSex.FEMALE)
        assertEquals(1624.0, bmrFemale, 0.01)
    }

    @Test
    fun `test caloric engine calculates honest plus minus 15 percent range`() {
        val profile = UserProfile(
            id = "test-user",
            weightKg = 80.0,
            heightCm = 180.0,
            age = 28,
            biologicalSex = BiologicalSex.MALE,
            trainingExperience = TrainingExperience.INTERMEDIATE
        )

        val completedSets = listOf(
            WorkoutSet(
                id = "set-1",
                sessionId = "session-1",
                exerciseId = "bench-press",
                setNumber = 1,
                setType = SetType.WORKING,
                weightKg = 100.0,
                repsCompleted = 10,
                isCompleted = true
            ),
            WorkoutSet(
                id = "set-2",
                sessionId = "session-1",
                exerciseId = "bench-press",
                setNumber = 2,
                setType = SetType.FAILURE,
                weightKg = 100.0,
                repsCompleted = 8,
                isCompleted = true
            )
        )

        val estimate = CaloricEngine.estimateSessionCalories(
            userProfile = profile,
            durationMinutes = 60.0,
            completedSets = completedSets
        )

        assertTrue(estimate.totalKcal > 0)
        assertTrue(estimate.lowBoundKcal < estimate.totalKcal)
        assertTrue(estimate.highBoundKcal > estimate.totalKcal)
        assertEquals(15.0, estimate.confidenceBandPercentage)

        // Low is 85% of total, High is 115% of total
        assertEquals(estimate.totalKcal * 0.85, estimate.lowBoundKcal, 0.1)
        assertEquals(estimate.totalKcal * 1.15, estimate.highBoundKcal, 0.1)
    }
}
