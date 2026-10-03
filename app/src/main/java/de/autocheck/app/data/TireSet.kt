package de.autocheck.app

data class TireSet(
    val id: Long,
    val vehicle: String,
    val season: String,
    val dimension: String,
    val brand: String,
    val dot: String,
    val tread: String,
    val condition: String,
    val storage: String,

    // Neue Angaben für den Reifenbereich
    val boltPattern: String = "",
    val offset: String = "",
    val purchaseMonth: String = "",
    val purchaseYear: String = "",
    val price: String = ""
)
