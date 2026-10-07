package de.autocheck.app.data

data class ServiceInterval(
    val id: Long = 0L,

    val vehicle: String = "",

    // Letztes Service
    val lastServiceMonth: String = "",
    val lastServiceYear: String = "",
    val lastServiceKm: String = "",

    // Nächstes Service
    val nextServiceMonth: String = "",
    val nextServiceYear: String = "",
    val nextServiceKm: String = "",

    // Weitere Informationen
    val documentation: String = "",
    val cost: String = "",

    // Zeitbasierte Erinnerung
    val reminderEnabled: Boolean = false,
    val reminderMonthsBefore: Int = 0
)
