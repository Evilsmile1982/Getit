
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

private const val PLAN_REMINDER = 8101
private const val CHANGE_REMINDER = 8102
private const val END_REMINDER = 8103

class WinterTireReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val type = intent?.getIntExtra("reminderType", PLAN_REMINDER)
            ?: PLAN_REMINDER

        val (title, message) = when (type) {
            PLAN_REMINDER -> Pair(
                "Winterreifen planen",
                "In vier Wochen beginnt der gesetzliche Winterreifenzeitraum. Plane jetzt den Reifenwechsel und kontrolliere deine Winterreifen."
            )

            CHANGE_REMINDER -> Pair(
                "Zeit für Winterreifen",
                "Ab 1. November gilt bei winterlichen Fahrbahnverhältnissen die Winterreifenpflicht. Prüfe, ob deine Winterreifen montiert sind."
            )

            else -> Pair(
                "Winterreifenzeitraum endet heute",
                "Ab 15. April endet der gesetzliche Zeitraum. Wechsle auf Sommerreifen nur, wenn Wetter und Fahrbahnverhältnisse es zulassen."
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
                    description = "Jährliche Erinnerungen zum saisonalen Reifenwechsel"
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

        // Nach der Erinnerung gleich den Termin für das nächste Jahr planen.
        scheduleReminder(context, type, LocalDate.now().year + 1)
    }
}

fun scheduleWinterTireReminders(context: Context) {
    val today = LocalDate.now()

    val reminderTypes = listOf(
        PLAN_REMINDER,
        CHANGE_REMINDER,
        END_REMINDER
    )

    reminderTypes.forEach { type ->
        val (month, day) = reminderDate(type)

        var year = today.year
        val date = LocalDate.of(year, month, day)

        if (!date.isAfter(today)) {
            year++
        }

        scheduleReminder(context, type, year)
    }
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
