package com.medboxai.reminder.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicationDao {
    @Query("SELECT * FROM medications ORDER BY hour, minute")
    fun observeAll(): Flow<List<MedicationEntity>>

    @Query("SELECT * FROM medications WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): MedicationEntity?

    @Query("SELECT * FROM medications WHERE is_enabled = 1")
    suspend fun findAllEnabled(): List<MedicationEntity>

    @Upsert
    suspend fun upsert(medication: MedicationEntity): Long

    @Delete
    suspend fun delete(medication: MedicationEntity)
}
