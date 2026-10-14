package de.autocheck.app.data

data class Pickerl(
val vehicle: String,
val lastDate: String,
val nextDate: String,
val notes: String,
val reminder: Boolean,
val photoUri: String = "",
val reminderMonths: Int = 3
)

data class PickerlDocument(
val id: String,
val vehicle: String,
val date: String,
val type: String,
val name: String,
val uri: String,
val mimeType: String = "application/pdf",
val includeInPdf: Boolean = true
)
