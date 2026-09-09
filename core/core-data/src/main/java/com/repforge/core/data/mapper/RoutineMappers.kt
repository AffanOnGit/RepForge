package com.repforge.core.data.mapper

import com.repforge.core.data.database.entity.ExerciseGroupEntity
import com.repforge.core.data.database.entity.RoutineEntity
import com.repforge.core.data.database.entity.RoutineExerciseEntity
import com.repforge.core.domain.model.ExerciseGroup
import com.repforge.core.domain.model.ExerciseGroupType
import com.repforge.core.domain.model.Routine
import com.repforge.core.domain.model.RoutineExercise

fun RoutineEntity.toDomain(groups: List<ExerciseGroup> = emptyList()): Routine {
    return Routine(
        id = id,
        name = name,
        description = description,
        estimatedDurationMinutes = estimatedDurationMinutes,
        creatorName = creatorName,
        sourceVideoId = sourceVideoId,
        exerciseGroups = groups,
        createdAtMillis = createdAtMillis,
        updatedAtMillis = updatedAtMillis
    )
}

fun Routine.toEntity(): RoutineEntity {
    return RoutineEntity(
        id = id,
        name = name,
        description = description,
        estimatedDurationMinutes = estimatedDurationMinutes,
        creatorName = creatorName,
        sourceVideoId = sourceVideoId,
        createdAtMillis = createdAtMillis,
        updatedAtMillis = updatedAtMillis
    )
}

fun ExerciseGroupEntity.toDomain(): ExerciseGroup {
    return ExerciseGroup(
        id = id,
        groupType = try {
            ExerciseGroupType.valueOf(groupType)
        } catch (_: Exception) {
            ExerciseGroupType.SINGLE
        },
        orderInRoutine = orderInRoutine,
        restAfterGroupSeconds = restAfterGroupSeconds,
        timeCapSeconds = timeCapSeconds
    )
}

fun ExerciseGroup.toEntity(routineId: String): ExerciseGroupEntity {
    return ExerciseGroupEntity(
        id = id,
        routineId = routineId,
        groupType = groupType.name,
        orderInRoutine = orderInRoutine,
        restAfterGroupSeconds = restAfterGroupSeconds,
        timeCapSeconds = timeCapSeconds
    )
}

fun RoutineExerciseEntity.toDomain(): RoutineExercise {
    return RoutineExercise(
        id = id,
        exerciseGroupId = exerciseGroupId,
        exerciseId = exerciseId,
        orderInGroup = orderInGroup,
        prescribedSets = prescribedSets,
        prescribedRepsMin = prescribedRepsMin,
        prescribedRepsMax = prescribedRepsMax,
        restSeconds = restSeconds,
        executionNotes = executionNotes
    )
}

fun RoutineExercise.toEntity(): RoutineExerciseEntity {
    return RoutineExerciseEntity(
        id = id,
        exerciseGroupId = exerciseGroupId,
        exerciseId = exerciseId,
        orderInGroup = orderInGroup,
        prescribedSets = prescribedSets,
        prescribedRepsMin = prescribedRepsMin,
        prescribedRepsMax = prescribedRepsMax,
        restSeconds = restSeconds,
        executionNotes = executionNotes
    )
}
