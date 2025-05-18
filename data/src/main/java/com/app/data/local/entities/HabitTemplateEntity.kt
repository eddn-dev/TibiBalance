package com.app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.app.domain.enums.HabitCategory
@Entity(tableName = "habit_templates")
data class HabitTemplateEntity(
    @PrimaryKey val id: String,
    val name        : String,
    val icon        : String,
    val category    : HabitCategory,
    val formJson    : String
)