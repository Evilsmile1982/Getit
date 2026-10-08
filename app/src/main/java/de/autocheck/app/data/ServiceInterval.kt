package de.autocheck.app.data

data class ServiceInterval(
    val id: Long = 0L,

    val vehicle: String = "",

    // Durchgeführtes Service
    val serviceMonth: String = "",
    val serviceYear: String = "",
    val serviceKm: String = "",

    // Nächstes geplantes Service
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
