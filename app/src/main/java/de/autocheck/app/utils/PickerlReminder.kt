package de.autocheck.app

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
import java.time.format.DateTimeFormatter

private val pickerReminderIsoFormatter =
    DateTimeFormatter.ofPattern(
        "yyyy-MM-dd"
    )

private val pickerReminderDisplayFormatter =
    DateTimeFormatter.ofPattern(
        "dd.MM.yyyy"
    )

class PickerlReceiver : BroadcastReceiver() {

    override fun onReceive(
        context: Context,
        intent: Intent?
    ) {

        val vehicle =
            intent?.getStringExtra(
                "vehicle"
            ) ?: "Fahrzeug"

        val nextDate =
            intent?.getStringExtra(
                "nextDate"
            ) ?: ""

        val displayDate =
            try {

                LocalDate
                    .parse(
                        nextDate,
                        pickerReminderIsoFormatter
                    )
                    .format(
                        pickerReminderDisplayFormatter
                    )

            } catch (_: Exception) {

                nextDate
            }

        val manager =
            context.getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager

        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.O
        ) {

            val channel =
                NotificationChannel(
                    "pickerl_reminders",
                    "Pickerl / TÜV Erinnerungen",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {

                    description =
                        "Erinnerungen an Pickerl- und TÜV-Termine"
                }

            manager.createNotificationChannel(
                channel
            )

            val notification =
                android.app.Notification
                    .Builder(
                        context,
                        "pickerl_reminders"
                    )
                    .setSmallIcon(
                        android.R.drawable
                            .ic_dialog_info
                    )
                    .setContentTitle(
                        "Pickerl / TÜV Erinnerung"
                    )
                    .setContentText(
                        "$vehicle: Prüfung am $displayDate"
                    )
                    .setAutoCancel(
                        true
                    )
                    .build()

            manager.notify(
                vehicle.hashCode(),
                notification
            )

        } else {

            @Suppress("DEPRECATION")
            val notification =
                android.app.Notification
                    .Builder(
                        context
                    )
                    .setSmallIcon(
                        android.R.drawable
                            .ic_dialog_info
                    )
                    .setContentTitle(
                        "Pickerl / TÜV Erinnerung"
                    )
                    .setContentText(
                        "$vehicle: Prüfung am $displayDate"
                    )
                    .setAutoCancel(
                        true
                    )
                    .build()

            @Suppress("DEPRECATION")
            manager.notify(
                vehicle.hashCode(),
                notification
            )
        }
    }
}

fun schedulePickerlReminder(
    context: Context,
    vehicle: String,
    nextDate: String,
    reminderMonths: Int = 3
) {

    if (
        nextDate.isBlank()
    ) {
        return
    }

    val expiry =
        try {

            LocalDate.parse(
                nextDate,
                pickerReminderIsoFormatter
            )

        } catch (_: Exception) {

            try {

                LocalDate.parse(
                    nextDate
                )

            } catch (_: Exception) {

                return
            }
        }

    val months =
        reminderMonths.coerceIn(
            1,
            5
        )

    val reminderDate =
        expiry.minusMonths(
            months.toLong()
        )

    val trigger =
        reminderDate
            .atTime(
                9,
                0
            )
            .atZone(
                ZoneId.systemDefault()
            )
            .toInstant()
            .toEpochMilli()

    val intent =
        Intent(
            context,
            PickerlReceiver::class.java
        ).apply {

            putExtra(
                "vehicle",
                vehicle
            )

            putExtra(
                "nextDate",
                nextDate
            )

            putExtra(
                "reminderMonths",
                months
            )
        }

    val pending =
        PendingIntent.getBroadcast(
            context,
            vehicle.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                PendingIntent.FLAG_IMMUTABLE
        )

    val alarmManager =
        context.getSystemService(
            Context.ALARM_SERVICE
        ) as AlarmManager

    if (
        trigger <=
        System.currentTimeMillis()
    ) {

        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis() +
                5_000L,
            pending
        )

    } else {

        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            trigger,
            pending
        )
    }
}

fun cancelPickerlReminder(
    context: Context,
    vehicle: String
) {

    val intent =
        Intent(
            context,
            PickerlReceiver::class.java
        )

    val pending =
        PendingIntent.getBroadcast(
            context,
            vehicle.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                PendingIntent.FLAG_IMMUTABLE
        )

    val alarmManager =
        context.getSystemService(
            Context.ALARM_SERVICE
        ) as AlarmManager

    alarmManager.cancel(
        pending
    )

    pending.cancel()
}
