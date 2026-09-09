package com.repforge.core.data.mapper

import com.repforge.core.data.database.entity.ExerciseEntity
import com.repforge.core.domain.model.Equipment
import com.repforge.core.domain.model.Exercise
import com.repforge.core.domain.model.SubMuscle

fun ExerciseEntity.toDomain(): Exercise {
    return Exercise(
        id = id,
        name = name,
        equipment = try {
            Equipment.valueOf(equipment)
        } catch (_: Exception) {
            Equipment.BARBELL
        },
        primarySubMuscle = try {
            SubMuscle.valueOf(primarySubMuscle)
        } catch (_: Exception) {
            SubMuscle.MID_CHEST
        },
        secondarySubMuscles = if (secondarySubMuscles.isBlank()) {
            emptyList()
        } else {
            secondarySubMuscles.split(",")
                .mapNotNull {
                    try {
                        SubMuscle.valueOf(it.trim())
                    } catch (_: Exception) {
                        null
                    }
                }
        },
        creatorTags = if (creatorTags.isBlank()) {
            emptyList()
        } else {
            creatorTags.split(",").map { it.trim() }
        },
        isCustom = isCustom
    )
}

fun Exercise.toEntity(): ExerciseEntity {
    return ExerciseEntity(
        id = id,
        name = name,
        equipment = equipment.name,
        primarySubMuscle = primarySubMuscle.name,
        secondarySubMuscles = secondarySubMuscles.joinToString(",") { it.name },
        creatorTags = creatorTags.joinToString(","),
        isCustom = isCustom
    )
}
