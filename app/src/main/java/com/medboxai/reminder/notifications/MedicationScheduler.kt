package com.medboxai.reminder.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.AlarmManagerCompat
import com.medboxai.reminder.data.Medication
import java.time.Clock

class MedicationScheduler(private val context: Context) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun schedule(medication: Medication, clock: Clock = Clock.systemDefaultZone()) {
        if (medication.id == Medication.UNSAVED_ID || !medication.isEnabled) return
        val triggerAtMillis = medication.nextTriggerMillis(clock)
        val pendingIntent = createPendingIntent(medication, PendingIntent.FLAG_UPDATE_CURRENT)
        AlarmManagerCompat.setExactAndAllowWhileIdle(
            alarmManager,
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            pendingIntent
        )
    }

    fun cancel(id: Long) {
        if (id == Medication.UNSAVED_ID) return
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = reminderAction(id)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            id.toInt(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        pendingIntent?.let { alarmManager.cancel(it) }
    }

    private fun createPendingIntent(medication: Medication, flag: Int): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = reminderAction(medication.id)
            putExtra(ReminderReceiver.EXTRA_MEDICATION_ID, medication.id)
            putExtra(ReminderReceiver.EXTRA_MEDICATION_NAME, medication.name)
            putExtra(ReminderReceiver.EXTRA_MEDICATION_DOSAGE, medication.dosage)
            putExtra(ReminderReceiver.EXTRA_MEDICATION_INSTRUCTIONS, medication.instructions)
            putExtra(ReminderReceiver.EXTRA_MEDICATION_HOUR, medication.time.hour)
            putExtra(ReminderReceiver.EXTRA_MEDICATION_MINUTE, medication.time.minute)
        }
        val flags = flag or PendingIntent.FLAG_IMMUTABLE
        return PendingIntent.getBroadcast(context, medication.id.toInt(), intent, flags)
    }

    private fun reminderAction(id: Long): String = "${ReminderReceiver.ACTION_REMINDER}:$id"
}
