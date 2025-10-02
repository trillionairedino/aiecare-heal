package com.medboxai.reminder.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.medboxai.reminder.R
import com.medboxai.reminder.data.Medication
import java.time.LocalTime

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getLongExtra(EXTRA_MEDICATION_ID, Medication.UNSAVED_ID)
        val name = intent.getStringExtra(EXTRA_MEDICATION_NAME) ?: return
        val dosage = intent.getStringExtra(EXTRA_MEDICATION_DOSAGE) ?: ""
        val instructions = intent.getStringExtra(EXTRA_MEDICATION_INSTRUCTIONS) ?: ""
        val hour = intent.getIntExtra(EXTRA_MEDICATION_HOUR, 0)
        val minute = intent.getIntExtra(EXTRA_MEDICATION_MINUTE, 0)

        val message = buildString {
            append(dosage)
            if (instructions.isNotBlank()) {
                if (isNotEmpty()) append(" • ")
                append(instructions)
            }
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_pill)
            .setContentTitle(name)
            .setContentText(message.ifBlank { context.getString(R.string.notification_default_body) })
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(id.toInt(), notification)

        val medication = Medication(
            id = id,
            name = name,
            dosage = dosage,
            instructions = instructions,
            time = LocalTime.of(hour, minute),
            isEnabled = true
        )
        MedicationScheduler(context).schedule(medication)
    }

    companion object {
        const val CHANNEL_ID = "medication_reminders"
        const val ACTION_REMINDER = "com.medboxai.reminder.ACTION_REMINDER"

        const val EXTRA_MEDICATION_ID = "extra_medication_id"
        const val EXTRA_MEDICATION_NAME = "extra_medication_name"
        const val EXTRA_MEDICATION_DOSAGE = "extra_medication_dosage"
        const val EXTRA_MEDICATION_INSTRUCTIONS = "extra_medication_instructions"
        const val EXTRA_MEDICATION_HOUR = "extra_medication_hour"
        const val EXTRA_MEDICATION_MINUTE = "extra_medication_minute"
    }
}
