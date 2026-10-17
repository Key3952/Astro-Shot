package com.example.astro.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AstroDao {

    @Query("SELECT * FROM target_sessions ORDER BY id DESC")
    fun getAllSessions(): Flow<List<TargetSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: TargetSessionEntity): Long

    @Update
    suspend fun updateSession(session: TargetSessionEntity)

    @Delete
    suspend fun deleteSession(session: TargetSessionEntity)

    @Query("SELECT * FROM checklist_items ORDER BY id ASC")
    fun getAllChecklistItems(): Flow<List<ChecklistItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChecklistItems(items: List<ChecklistItemEntity>)

    @Update
    suspend fun updateChecklistItem(item: ChecklistItemEntity)

    @Query("UPDATE checklist_items SET isChecked = :isChecked")
    suspend fun setAllChecklistChecked(isChecked: Boolean)
}
