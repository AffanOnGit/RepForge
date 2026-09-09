package com.repforge.core.data.mapper

import com.repforge.core.data.database.entity.UserProfileEntity
import com.repforge.core.domain.model.BiologicalSex
import com.repforge.core.domain.model.TrainingExperience
import com.repforge.core.domain.model.UnitSystem
import com.repforge.core.domain.model.UserProfile

fun UserProfileEntity.toDomain(): UserProfile {
    return UserProfile(
        id = id,
        email = email,
        weightKg = weightKg,
        heightCm = heightCm,
        age = age,
        biologicalSex = biologicalSex?.let {
            try {
                BiologicalSex.valueOf(it)
            } catch (_: Exception) {
                null
            }
        },
        bodyFatPercentage = bodyFatPercentage,
        trainingExperience = try {
            TrainingExperience.valueOf(trainingExperience)
        } catch (_: Exception) {
            TrainingExperience.INTERMEDIATE
        },
        unitSystem = UnitSystem.fromString(unitSystem),
        createdAtMillis = createdAtMillis
    )
}

fun UserProfile.toEntity(): UserProfileEntity {
    return UserProfileEntity(
        id = id,
        email = email,
        weightKg = weightKg,
        heightCm = heightCm,
        age = age,
        biologicalSex = biologicalSex?.name,
        bodyFatPercentage = bodyFatPercentage,
        trainingExperience = trainingExperience.name,
        unitSystem = unitSystem.toString(),
        createdAtMillis = createdAtMillis
    )
}
