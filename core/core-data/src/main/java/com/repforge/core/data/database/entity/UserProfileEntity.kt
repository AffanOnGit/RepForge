package com.repforge.core.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey
    val id: String,

    val email: String = "",

    @ColumnInfo(name = "weight_kg")
    val weightKg: Double? = null,

    @ColumnInfo(name = "height_cm")
    val heightCm: Double? = null,

    val age: Int? = null,

    @ColumnInfo(name = "biological_sex")
    val biologicalSex: String? = null, // MALE, FEMALE

    @ColumnInfo(name = "body_fat_percentage")
    val bodyFatPercentage: Double? = null,

    @ColumnInfo(name = "training_experience")
    val trainingExperience: String = "INTERMEDIATE", // BEGINNER, INTERMEDIATE, ADVANCED

    @ColumnInfo(name = "unit_system")
    val unitSystem: String = "metric", // metric, imperial

    @ColumnInfo(name = "created_at")
    val createdAtMillis: Long = System.currentTimeMillis()
)
