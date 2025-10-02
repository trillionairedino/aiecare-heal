package com.medboxai.reminder.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.medboxai.reminder.data.MedBoxDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DeviceBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != Intent.ACTION_LOCKED_BOOT_COMPLETED
        ) {
            return
        }

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            val database = MedBoxDatabase.getInstance(context)
            val enabledMedications = database.medicationDao().findAllEnabled()
            val scheduler = MedicationScheduler(context)
            enabledMedications
                .map { it.toDomain() }
                .forEach { scheduler.schedule(it) }
            pendingResult.finish()
        }
    }
}
