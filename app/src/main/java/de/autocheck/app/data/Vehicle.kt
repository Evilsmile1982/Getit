package de.autocheck.app

import java.util.UUID

data class Vehicle(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val make: String,
    val model: String,
    val year: String,
    val plate: String = "",
    val vin: String = "",
    val imageUri: String = ""
)
