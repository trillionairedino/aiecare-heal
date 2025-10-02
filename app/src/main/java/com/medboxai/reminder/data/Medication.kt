package com.medboxai.reminder.data

import java.time.Clock
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Domain model representing a medication reminder scheduled to trigger once per day.
 */
data class Medication(
    val id: Long = UNSAVED_ID,
    val name: String,
    val dosage: String,
    val instructions: String,
    val time: LocalTime,
    val isEnabled: Boolean
) {
    fun nextDoseDateTime(now: LocalDateTime = LocalDateTime.now()): LocalDateTime {
        val candidate = now.toLocalDate().atTime(time)
        return if (candidate.isAfter(now)) candidate else candidate.plusDays(1)
    }

    fun nextTriggerMillis(clock: Clock = Clock.systemDefaultZone()): Long {
        val now = LocalDateTime.now(clock)
        val candidate = now.toLocalDate().atTime(time)
        val next = if (candidate.isAfter(now)) candidate else candidate.plusDays(1)
        return next.atZone(clock.zone).toInstant().toEpochMilli()
    }

    companion object {
        const val UNSAVED_ID = 0L
    }
}
