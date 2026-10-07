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
import kotlin.math.abs

private const val SERVICE_REMINDER_CHANNEL =
    "service_reminders"

private const val SERVICE_REMINDER_CHANNEL_NAME =
    "Service Erinnerungen"


/*
 * ============================================================
 * SERVICE REMINDER RECEIVER
 * ============================================================
 *
 * Dieser Receiver wird vom Android AlarmManager aufgerufen,
 * wenn eine zeitliche Service-Erinnerung fällig ist.
 */
class ServiceReminderReceiver :
    BroadcastReceiver() {

    override fun onReceive(
        context: Context,
        intent: Intent?
    ) {

        val vehicle =
            intent?.getStringExtra(
                "vehicle"
            ) ?: "Fahrzeug"

        val nextService =
            intent?.getStringExtra(
                "nextService"
            ) ?: ""

        val reminderMonths =
            intent?.getIntExtra(
                "reminderMonths",
                0
            ) ?: 0

        val message =
            if (
                reminderMonths > 0
            ) {

                "$vehicle: Service steht in $reminderMonths Monaten an."

            } else {

                "$vehicle: Nächstes Service steht an."
            }

        showServiceReminderNotification(
            context = context,
            vehicle = vehicle,
            nextService = nextService,
            message = message
        )
    }
}


/*
 * ============================================================
 * BENACHRICHTIGUNG
 * ============================================================
 */
private fun showServiceReminderNotification(
    context: Context,
    vehicle: String,
    nextService: String,
    message: String
) {

    val manager =
        context.getSystemService(
            Context.NOTIFICATION_SERVICE
        ) as NotificationManager


    /*
     * Android 8+
     */
    if (
        Build.VERSION.SDK_INT >=
        Build.VERSION_CODES.O
    ) {

        val channel =
            NotificationChannel(
                SERVICE_REMINDER_CHANNEL,
                SERVICE_REMINDER_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {

                description =
                    "Erinnerungen an Service und Wartungsintervalle"
            }

        manager.createNotificationChannel(
            channel
        )
    }


    /*
     * Android 8+
     */
    if (
        Build.VERSION.SDK_INT >=
        Build.VERSION_CODES.O
    ) {

        val notification =
            android.app.Notification
                .Builder(
                    context,
                    SERVICE_REMINDER_CHANNEL
                )
                .setSmallIcon(
                    android.R.drawable.ic_dialog_info
                )
                .setContentTitle(
                    "Service Erinnerung"
                )
                .setContentText(
                    message
                )
                .setStyle(
                    android.app.Notification
                        .BigTextStyle()
                        .bigText(
                            "$message\nNächstes Service: $nextService"
                        )
                )
                .setAutoCancel(
                    true
                )
                .build()

        manager.notify(
            serviceReminderNotificationId(
                vehicle
            ),
            notification
        )

    } else {

        /*
         * Ältere Android-Versionen
         */
        @Suppress("DEPRECATION")
        val notification =
            android.app.Notification
                .Builder(
                    context
                )
                .setSmallIcon(
                    android.R.drawable.ic_dialog_info
                )
                .setContentTitle(
                    "Service Erinnerung"
                )
                .setContentText(
                    message
                )
                .setAutoCancel(
                    true
                )
                .build()

        @Suppress("DEPRECATION")
        manager.notify(
            serviceReminderNotificationId(
                vehicle
            ),
            notification
        )
    }
}


/*
 * ============================================================
 * ZEITLICHE SERVICE-ERINNERUNG EINPLANEN
 * ============================================================
 *
 * Beispiel:
 *
 * Nächstes Service:
 * März 2027
 *
 * Erinnerung:
 * 2 Monate vorher
 *
 * Ergebnis:
 * Alarm für Jänner 2027 um 09:00 Uhr.
 */
fun scheduleServiceReminder(
    context: Context,
    vehicle: String,
    nextServiceMonth: String,
    nextServiceYear: String,
    reminderMonths: Int
) {

    /*
     * Vorherige Erinnerung für dieses Fahrzeug löschen.
     */
    cancelServiceReminder(
        context,
        vehicle
    )


    /*
     * Keine Erinnerung gewünscht.
     */
    if (
        reminderMonths <= 0
    ) {
        return
    }


    /*
     * Fehlende Service-Daten.
     */
    if (
        nextServiceMonth.isBlank() ||
        nextServiceYear.isBlank()
    ) {
        return
    }


    /*
     * Monatsname in Monatsnummer umwandeln.
     */
    val month =
        serviceReminderMonthNumber(
            nextServiceMonth
        )


    /*
     * Jahr aus dem Text lesen.
     */
    val year =
        nextServiceYear
            .trim()
            .toIntOrNull()
            ?: return


    /*
     * Ungültiger Monat.
     */
    if (
        month <= 0
    ) {
        return
    }


    /*
     * Service-Datum erstellen.
     *
     * Wir verwenden den ersten Tag des
     * ausgewählten Monats.
     */
    val serviceDate =
        try {

            LocalDate.of(
                year,
                month,
                1
            )

        } catch (
            _: Exception
        ) {

            return
        }


    /*
     * Erinnerung um die gewünschte Anzahl
     * Monate vorziehen.
     */
    val reminderDate =
        serviceDate.minusMonths(
            reminderMonths.toLong()
        )


    /*
     * Erinnerung um 09:00 Uhr.
     */
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


    /*
     * Intent für den BroadcastReceiver.
     */
    val intent =
        Intent(
            context,
            ServiceReminderReceiver::class.java
        ).apply {

            putExtra(
                "vehicle",
                vehicle
            )

            putExtra(
                "nextService",
                "$nextServiceMonth $nextServiceYear"
            )

            putExtra(
                "reminderMonths",
                reminderMonths
            )
        }


    /*
     * Eindeutiger PendingIntent pro Fahrzeug.
     */
    val pending =
        PendingIntent.getBroadcast(
            context,
            serviceReminderRequestCode(
                vehicle
            ),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                PendingIntent.FLAG_IMMUTABLE
        )


    /*
     * Android AlarmManager.
     */
    val alarmManager =
        context.getSystemService(
            Context.ALARM_SERVICE
        ) as AlarmManager


    /*
     * Falls das berechnete Datum bereits vorbei ist,
     * wird für den Test bzw. zur Vermeidung eines
     * ungültigen Alarms ein kleiner Abstand verwendet.
     */
    val safeTrigger =
        if (
            trigger <=
            System.currentTimeMillis()
        ) {

            System.currentTimeMillis() +
                5_000L

        } else {

            trigger
        }


    /*
     * Alarm setzen.
     */
    alarmManager.setAndAllowWhileIdle(
        AlarmManager.RTC_WAKEUP,
        safeTrigger,
        pending
    )
}


/*
 * ============================================================
 * SERVICE-ERINNERUNG LÖSCHEN
 * ============================================================
 */
fun cancelServiceReminder(
    context: Context,
    vehicle: String
) {

    val intent =
        Intent(
            context,
            ServiceReminderReceiver::class.java
        )


    val pending =
        PendingIntent.getBroadcast(
            context,
            serviceReminderRequestCode(
                vehicle
            ),
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


/*
 * ============================================================
 * KILOMETER-ERINNERUNG
 * ============================================================
 *
 * Diese Funktion prüft:
 *
 * Nächstes Service bei:
 * 100.000 km
 *
 * Erinnerung:
 * 1.000 km vorher
 *
 * Erinnerung ab:
 * 99.000 km
 *
 * WICHTIG:
 * Die Funktion ist vorbereitet.
 * Das aktuelle Fahrzeugmodell besitzt noch kein
 * dauerhaft gespeichertes "aktuelles km"-Feld.
 *
 * Deshalb wird diese Funktion erst vollständig
 * genutzt, sobald wir den aktuellen Kilometerstand
 * in der App ergänzen.
 */
fun checkServiceKmReminder(
    context: Context,
    vehicle: String,
    nextServiceKm: String,
    currentKm: String,
    reminderKmBefore: Int
) {

    /*
     * Keine Kilometer-Erinnerung.
     */
    if (
        reminderKmBefore <= 0
    ) {
        return
    }


    /*
     * Nächsten Service-Kilometerstand lesen.
     */
    val nextKm =
        nextServiceKm
            .replace(
                ".",
                ""
            )
            .replace(
                ",",
                ""
            )
            .trim()
            .toIntOrNull()
            ?: return


    /*
     * Aktuellen Kilometerstand lesen.
     */
    val current =
        currentKm
            .replace(
                ".",
                ""
            )
            .replace(
                ",",
                ""
            )
            .trim()
            .toIntOrNull()
            ?: return


    /*
     * Kilometerstand berechnen,
     * ab dem die Erinnerung ausgelöst wird.
     */
    val reminderAt =
        nextKm -
            reminderKmBefore


    /*
     * Erinnerung auslösen.
     */
    if (
        current >= reminderAt
    ) {

        showServiceReminderNotification(
            context =
                context,

            vehicle =
                vehicle,

            nextService =
                "$nextKm km",

            message =
                "$vehicle: Service-Erinnerung. Nächstes Service bei $nextKm km."
        )
    }
}


/*
 * ============================================================
 * MONAT -> ZAHL
 * ============================================================
 */
private fun serviceReminderMonthNumber(
    month: String
): Int {

    return when (
        month
            .trim()
            .uppercase()
    ) {

        "JANUAR",
        "JANUARY" ->
            1

        "FEBRUAR",
        "FEBRUARY" ->
            2

        "MÄRZ",
        "MAERZ",
        "MARCH" ->
            3

        "APRIL" ->
            4

        "MAI",
        "MAY" ->
            5

        "JUNI",
        "JUNE" ->
            6

        "JULI",
        "JULY" ->
            7

        "AUGUST" ->
            8

        "SEPTEMBER" ->
            9

        "OKTOBER",
        "OCTOBER" ->
            10

        "NOVEMBER" ->
            11

        "DEZEMBER",
        "DECEMBER" ->
            12

        else ->
            0
    }
}


/*
 * ============================================================
 * REQUEST CODE
 * ============================================================
 *
 * Dadurch bekommt jedes Fahrzeug einen eigenen Alarm.
 */
private fun serviceReminderRequestCode(
    vehicle: String
): Int {

    return (
        vehicle.hashCode() xor
            0x53A7
        )
        .let {

            if (
                it == Int.MIN_VALUE
            ) {

                1

            } else {

                abs(
                    it
                )
            }
        }
}


/*
 * ============================================================
 * NOTIFICATION ID
 * ============================================================
 *
 * Dadurch bekommt jedes Fahrzeug eine eigene
 * Benachrichtigungs-ID.
 */
private fun serviceReminderNotificationId(
    vehicle: String
): Int {

    return (
        vehicle.hashCode() xor
            0x71B9
        )
        .let {

            if (
                it == Int.MIN_VALUE
            ) {

                2

            } else {

                abs(
                    it
                )
            }
        }
}
