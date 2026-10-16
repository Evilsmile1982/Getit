
package de.autocheck.app.utils

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import java.time.LocalDate
import java.time.ZoneId

private const val CHANNEL_ID = "winter_tire_reminders"
private const val PREFS_NAME = "autocheck_reminders"
private const val ENABLED_KEY = "winter_tire_reminders_enabled"

private const val PLAN_REMINDER = 8101
private const val CHANGE_REMINDER = 8102
private const val END_REMINDER = 8103

private val reminderTypes = listOf(
    PLAN_REMINDER,
    CHANGE_REMINDER,
    END_REMINDER
)

class WinterTireReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val preferences = context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )

        // Ausgeschaltete Erinnerungen dürfen keine Benachrichtigung auslösen.
        if (!preferences.getBoolean(ENABLED_KEY, false)) {
            return
        }

        val type = intent?.getIntExtra("reminderType", PLAN_REMINDER)
            ?: PLAN_REMINDER

        val (title, message) = when (type) {
            PLAN_REMINDER -> Pair(
                "Winterreifen planen",
                "Plane jetzt den saisonalen Reifenwechsel und kontrolliere deine Winterreifen."
            )

            CHANGE_REMINDER -> Pair(
                "Zeit für Winterreifen",
                "Prüfe, ob deine Winterreifen montiert sind. In Österreich gilt die Winterreifenpflicht von 1. November bis 15. April bei winterlichen Fahrbahnverhältnissen."
            )

            else -> Pair(
                "Reifenwechsel im Frühjahr",
                "Prüfe, ob ein Wechsel auf Sommerreifen sinnvoll ist. Berücksichtige dabei Wetter und Fahrbahnverhältnisse."
            )
        }

        val manager = context.getSystemService(
            Context.NOTIFICATION_SERVICE
        ) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    "Reifen-Erinnerungen",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description =
                        "Jährliche Erinnerungen zum saisonalen Reifenwechsel"
                }
            )
        }

        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            android.app.Notification.Builder(context, CHANNEL_ID)
        } else {
            @Suppress("DEPRECATION")
            android.app.Notification.Builder(context)
        }

        val notification = builder
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(
                android.app.Notification.BigTextStyle().bigText(message)
            )
            .setAutoCancel(true)
            .build()

        manager.notify(type, notification)

        // Den nächsten jährlichen Termin nur planen, solange die Funktion aktiv ist.
        if (preferences.getBoolean(ENABLED_KEY, false)) {
            scheduleReminder(
                context = context,
                type = type,
                year = LocalDate.now().year + 1
            )
        }
    }
}

fun scheduleWinterTireReminders(context: Context) {
    context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    ).edit()
        .putBoolean(ENABLED_KEY, true)
        .apply()

    val today = LocalDate.now()

    reminderTypes.forEach { type ->
        val (month, day) = reminderDate(type)
        var year = today.year

        if (!LocalDate.of(year, month, day).isAfter(today)) {
            year++
        }

        scheduleReminder(
            context = context,
            type = type,
            year = year
        )
    }
}

fun cancelWinterTireReminders(context: Context) {
    // Zuerst deaktivieren, damit ein Receiver keine neuen Termine plant.
    context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    ).edit()
        .putBoolean(ENABLED_KEY, false)
        .apply()

    val alarmManager = context.getSystemService(
        Context.ALARM_SERVICE
    ) as AlarmManager

    reminderTypes.forEach { type ->
        val intent = Intent(
            context,
            WinterTireReminderReceiver::class.java
        )

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            type,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }
}

fun areWinterTireRemindersEnabled(context: Context): Boolean {
    return context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    ).getBoolean(ENABLED_KEY, false)
}

private fun reminderDate(type: Int): Pair<Int, Int> {
    return when (type) {
        PLAN_REMINDER -> Pair(10, 1)
        CHANGE_REMINDER -> Pair(10, 15)
        else -> Pair(4, 15)
    }
}

private fun scheduleReminder(
    context: Context,
    type: Int,
    year: Int
) {
    val (month, day) = reminderDate(type)

    val triggerAt = LocalDate.of(year, month, day)
        .atTime(9, 0)
        .atZone(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli()

    val intent = Intent(
        context,
        WinterTireReminderReceiver::class.java
    ).apply {
        putExtra("reminderType", type)
    }

    val pendingIntent = PendingIntent.getBroadcast(
        context,
        type,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or
            PendingIntent.FLAG_IMMUTABLE
    )

    val alarmManager = context.getSystemService(
        Context.ALARM_SERVICE
    ) as AlarmManager

    alarmManager.setAndAllowWhileIdle(
        AlarmManager.RTC_WAKEUP,
        triggerAt,
        pendingIntent
    )
}
