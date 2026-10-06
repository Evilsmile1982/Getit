package de.autocheck.app

import java.util.UUID

data class Vehicle(
    val id: String = UUID.randomUUID().toString(),

    // Fahrzeugdaten
    val name: String,

    // Bezeichnung:
    // PKW, MOTORRAD, WOHNMOBIL, QUAD, MOPED,
    // NUTZFAHRZEUG, LKW oder BAUMASCHINE
    val vehicleType: String = "PKW",

    val make: String,
    val model: String,

    // Motorisierung
    val motorization: String = "",
    val power: String = "",

    // Baujahr
    val year: String,

    // Weitere Fahrzeugdaten
    val plate: String = "",
    val vin: String = "",

    // Persönliches Fahrzeugbild
    val imageUri: String = "",

    // Kaufpreis
    val purchasePrice: String = ""
)
