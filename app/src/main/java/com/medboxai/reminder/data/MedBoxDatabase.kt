package com.medboxai.reminder.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [MedicationEntity::class], version = 1, exportSchema = false)
abstract class MedBoxDatabase : RoomDatabase() {
    abstract fun medicationDao(): MedicationDao

    companion object {
        @Volatile
        private var INSTANCE: MedBoxDatabase? = null

        fun getInstance(context: Context): MedBoxDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    MedBoxDatabase::class.java,
                    "medbox-db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
