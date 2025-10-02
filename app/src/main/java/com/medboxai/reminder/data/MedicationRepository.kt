package com.medboxai.reminder.data

import android.content.Context
import com.medboxai.reminder.notifications.MedicationScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.LocalTime

class MedicationRepository private constructor(
    private val dao: MedicationDao,
    private val scheduler: MedicationScheduler
) {
    fun observeMedications(): Flow<List<Medication>> =
        dao.observeAll().map { list -> list.map(MedicationEntity::toDomain) }

    suspend fun upsertMedication(
        id: Long,
        name: String,
        dosage: String,
        instructions: String,
        time: LocalTime,
        isEnabled: Boolean
    ): Long = withContext(Dispatchers.IO) {
        val medication = Medication(
            id = id,
            name = name,
            dosage = dosage,
            instructions = instructions,
            time = time,
            isEnabled = isEnabled
        )
        val entity = MedicationEntity.fromDomain(medication)
        val storedId = dao.upsert(entity)
        val resolvedId = if (id == Medication.UNSAVED_ID) storedId else id
        val resolvedMedication = medication.copy(id = resolvedId)
        if (resolvedMedication.isEnabled) {
            scheduler.schedule(resolvedMedication)
        } else {
            scheduler.cancel(resolvedMedication.id)
        }
        resolvedId
    }

    suspend fun toggleMedication(id: Long, enabled: Boolean) = withContext(Dispatchers.IO) {
        val current = dao.findById(id) ?: return@withContext
        val updated = current.copy(isEnabled = enabled)
        dao.upsert(updated)
        if (enabled) {
            scheduler.schedule(updated.toDomain())
        } else {
            scheduler.cancel(id)
        }
    }

    suspend fun deleteMedication(id: Long) = withContext(Dispatchers.IO) {
        val entity = dao.findById(id) ?: return@withContext
        dao.delete(entity)
        scheduler.cancel(id)
    }

    companion object {
        fun create(context: Context): MedicationRepository {
            val database = MedBoxDatabase.getInstance(context)
            val scheduler = MedicationScheduler(context)
            return MedicationRepository(database.medicationDao(), scheduler)
        }
    }
}
