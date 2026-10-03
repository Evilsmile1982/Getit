package de.autocheck.app

data class Pickerl(
    val vehicle: String,
    val lastDate: String,
    val nextDate: String,
    val notes: String,
    val reminder: Boolean,
    val photoUri: String = "",
    val reminderMonths: Int = 3
)
