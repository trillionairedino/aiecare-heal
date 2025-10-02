package com.medboxai.reminder.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalTime

@Entity(tableName = "medications")
data class MedicationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val dosage: String,
    val instructions: String,
    @ColumnInfo(name = "hour") val hour: Int,
    @ColumnInfo(name = "minute") val minute: Int,
    @ColumnInfo(name = "is_enabled") val isEnabled: Boolean
) {
    fun toDomain(): Medication = Medication(
        id = id,
        name = name,
        dosage = dosage,
        instructions = instructions,
        time = LocalTime.of(hour, minute),
        isEnabled = isEnabled
    )

    companion object {
        fun fromDomain(medication: Medication): MedicationEntity = MedicationEntity(
            id = medication.id,
            name = medication.name,
            dosage = medication.dosage,
            instructions = medication.instructions,
            hour = medication.time.hour,
            minute = medication.time.minute,
            isEnabled = medication.isEnabled
        )
    }
}
