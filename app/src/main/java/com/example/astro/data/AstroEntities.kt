package com.example.astro.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "target_sessions")
data class TargetSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val targetType: String, // "Млечный Путь", "Галактика", "Туманность", "Сияние", "Метеоры", "Луна"
    val locationName: String,
    val targetDate: String,
    val notes: String,
    val focalLength: Float,
    val aperture: Float,
    val iso: Int,
    val subExposureSeconds: Float,
    val subCount: Int,
    val isCompleted: Boolean = false
)

@Entity(tableName = "checklist_items")
data class ChecklistItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val category: String, // "Оптика", "Питание", "Монтировка", "Комфорт"
    val isChecked: Boolean = false
)
