
package de.autocheck.app

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val EV_PREFS = "carvita_electric_history"
private const val EV_ENTRIES_KEY = "charging_entries"
private val EvGold = Color(0xFFD4AF37)

private data class ElectricChargingEntry(
    val date: String,
    val odometerKm: Int,
    val energyKwh: Double,
    val startSoc: Int,
    val endSoc: Int,
    val chargingType: String,
    val chargingSpeed: String,
    val totalCost: Double,
    val pricePerKwh: Double,
    val soh: Double?
)

@Composable
fun ElectricConsumptionScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val preferences = remember(context) {
        context.getSharedPreferences(EV_PREFS, Context.MODE_PRIVATE)
    }

    val entries = remember(preferences) {
        mutableStateListOf<ElectricChargingEntry>().apply {
            try {
                val array = JSONArray(
                    preferences.getString(EV_ENTRIES_KEY, "[]") ?: "[]"
                )
                for (i in 0 until array.length()) {
                    val item = array.optJSONObject(i) ?: continue
                    add(
                        ElectricChargingEntry(
                            date = item.optString("date"),
                            odometerKm = item.optInt("odometerKm"),
                            energyKwh = item.optDouble("energyKwh"),
                            startSoc = item.optInt("startSoc"),
                            endSoc = item.optInt("endSoc"),
                            chargingType = item.optString("chargingType"),
                            chargingSpeed = item.optString("chargingSpeed"),
                            totalCost = item.optDouble("totalCost"),
                            pricePerKwh = item.optDouble("pricePerKwh"),
                            soh = if (item.isNull("soh")) {
                                null
                            } else {
                                item.optDouble("soh")
                            }
                        )
                    )
                }
            } catch (_: Exception) {
                // Ungültige gespeicherte Daten nicht übernehmen.
            }
        }
    }

    var date by remember {
        mutableStateOf(
            SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY).format(Date())
        )
    }
    var kmText by remember { mutableStateOf("") }
    var energyText by remember { mutableStateOf("") }
    var startText by remember { mutableStateOf("") }
    var endText by remember { mutableStateOf("") }
    var typeText by remember { mutableStateOf("Zuhause (AC)") }
    var speedText by remember { mutableStateOf("Normal (bis 11 kW)") }
    var totalCostText by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }
    var sohText by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var editingIndex by remember { mutableStateOf<Int?>(null) }

    fun persist() {
        val array = JSONArray()
        entries.forEach { entry ->
            array.put(
                JSONObject().apply {
                    put("date", entry.date)
                    put("odometerKm", entry.odometerKm)
                    put("energyKwh", entry.energyKwh)
                    put("startSoc", entry.startSoc)
                    put("endSoc", entry.endSoc)
                    put("chargingType", entry.chargingType)
                    put("chargingSpeed", entry.chargingSpeed)
                    put("totalCost", entry.totalCost)
                    put("pricePerKwh", entry.pricePerKwh)
                    if (entry.soh == null) {
                        put("soh", JSONObject.NULL)
                    } else {
                        put("soh", entry.soh)
                    }
                }
            )
        }
        preferences.edit()
            .putString(EV_ENTRIES_KEY, array.toString())
            .apply()
    }

    fun resetForm() {
        date = SimpleDateFormat(
            "dd.MM.yyyy",
            Locale.GERMANY
        ).format(Date())
        kmText = ""
        energyText = ""
        startText = ""
        endText = ""
        typeText = "Zuhause (AC)"
        speedText = "Normal (bis 11 kW)"
        totalCostText = ""
        priceText = ""
        sohText = ""
        editingIndex = null
        error = null
    }

    val chronological = entries.sortedBy { parseEvDate(it.date) }
    val totalEnergy = entries.sumOf { it.energyKwh }
    val totalCost = entries.sumOf { it.totalCost }

    val distance = if (chronological.size >= 2) {
        (
            chronological.last().odometerKm -
                chronological.first().odometerKm
        ).coerceAtLeast(0)
    } else {
        0
    }

    val estimatedConsumption =
        if (distance > 0) totalEnergy / distance * 100.0 else null

    DetailScaffold(
        title = "Elektro und Verbrauch",
        onBack = onBack
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                "LADEHISTORIE FÜR ELEKTROFAHRZEUGE",
                color = EvGold
            )

            Text(
                "Trage jede Ladung ein. Der Verbrauch wird als " +
                    "Schätzwert aus den gespeicherten Lademengen und " +
                    "Kilometerständen ermittelt.",
                color = Color.LightGray
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF202020)
                )
            ) {
                Column(
                    Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Text("GESAMTÜBERSICHT", color = EvGold)

                    Text(
                        "Geladene Energie: ${evFormat(totalEnergy)} kWh",
                        color = Color.White
                    )

                    Text(
                        "Gesamtkosten: ${evFormat(totalCost)} €",
                        color = Color.White
                    )

                    Text(
                        if (estimatedConsumption == null) {
                            "Verbrauch: mindestens zwei Kilometerstände erforderlich"
                        } else {
                            "Geschätzter Verbrauch: " +
                                "${evFormat(estimatedConsumption)} kWh/100 km"
                        },
                        color = Color.White
                    )

                    if (distance > 0) {
                        Text(
                            "Gefahrene Kilometer im Zeitraum: $distance km",
                            color = Color.LightGray
                        )
                    }
                }
            }

            Text(
                if (editingIndex == null) {
                    "Neuen Ladevorgang eintragen"
                } else {
                    "Ladevorgang bearbeiten"
                },
                color = EvGold
            )

            OutlinedTextField(
                value = date,
                onValueChange = { date = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Datum (TT.MM.JJJJ) *") },
                singleLine = true
            )

            OutlinedTextField(
                value = kmText,
                onValueChange = {
                    kmText = it.filter(Char::isDigit)
                    error = null
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Kilometerstand (km) *") },
                singleLine = true
            )

            OutlinedTextField(
                value = energyText,
                onValueChange = {
                    energyText = it
                    error = null
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Geladene Energie (kWh) *") },
                singleLine = true
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = startText,
                    onValueChange = {
                        startText = it.filter(Char::isDigit)
                        error = null
                    },
                    modifier = Modifier.weight(1f),
                    label = { Text("Start-Akku (%) *") },
                    singleLine = true
                )

                OutlinedTextField(
                    value = endText,
                    onValueChange = {
                        endText = it.filter(Char::isDigit)
                        error = null
                    },
                    modifier = Modifier.weight(1f),
                    label = { Text("Ende-Akku (%) *") },
                    singleLine = true
                )
            }

            OutlinedTextField(
                value = typeText,
                onValueChange = { typeText = it },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text("Ladeart: Zuhause AC, öffentlich AC, DC, HPC *")
                },
                singleLine = true
            )

            OutlinedTextField(
                value = speedText,
                onValueChange = { speedText = it },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text("Ladegeschwindigkeit, z. B. 11 kW oder 150 kW *")
                },
                singleLine = true
            )

            Text(
                "Kosten: Gesamtpreis oder Preis pro kWh eingeben",
                color = Color.LightGray
            )

            OutlinedTextField(
                value = totalCostText,
                onValueChange = {
                    totalCostText = it
                    error = null
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Gesamtpreis (€)") },
                singleLine = true
            )

            OutlinedTextField(
                value = priceText,
                onValueChange = {
                    priceText = it
                    error = null
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Oder Preis pro kWh (€)") },
                singleLine = true
            )

            OutlinedTextField(
                value = sohText,
                onValueChange = { sohText = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Akkuzustand SoH (%) – optional") },
                singleLine = true
            )

            error?.let {
                Text(it, color = Color(0xFFFF8A80))
            }

            Button(
                onClick = {
                    val km = kmText.toIntOrNull()
                    val energy = evParse(energyText)
                    val start = startText.toIntOrNull()
                    val end = endText.toIntOrNull()
                    val totalInput = evParse(totalCostText)
                    val priceInput = evParse(priceText)
                    val soh = if (sohText.isBlank()) {
                        null
                    } else {
                        evParse(sohText)
                    }

                    error = when {
                        date.isBlank() ->
                            "Bitte gib ein Datum ein."

                        km == null || km < 0 ->
                            "Bitte gib einen gültigen Kilometerstand ein."

                        energy == null || energy <= 0 ->
                            "Die geladene Energie muss größer als 0 sein."

                        start == null || start !in 0..100 ->
                            "Der Start-Ladestand muss zwischen 0 und 100 % liegen."

                        end == null || end !in 0..100 || end <= start ->
                            "Der End-Ladestand muss über dem Startwert und höchstens 100 % sein."

                        typeText.isBlank() ->
                            "Bitte gib die Ladeart ein."

                        speedText.isBlank() ->
                            "Bitte gib die Ladegeschwindigkeit ein."

                        totalCostText.isBlank() && priceText.isBlank() ->
                            "Bitte Gesamtpreis oder Preis pro kWh eingeben."

                        totalCostText.isNotBlank() &&
                            (totalInput == null || totalInput < 0) ->
                            "Bitte gib einen gültigen Gesamtpreis ein."

                        priceText.isNotBlank() &&
                            (priceInput == null || priceInput < 0) ->
                            "Bitte gib einen gültigen Preis pro kWh ein."

                        sohText.isNotBlank() &&
                            (soh == null || soh !in 0.0..100.0) ->
                            "Der SoH muss zwischen 0 und 100 % liegen."

                        else -> null
                    }

                    if (error == null) {
                        val cost = if (totalCostText.isNotBlank()) {
                            totalInput!!
                        } else {
                            energy!! * priceInput!!
                        }

                        val unitPrice = if (priceText.isNotBlank()) {
                            priceInput!!
                        } else {
                            cost / energy!!
                        }

                        val newEntry = ElectricChargingEntry(
                            date = date.trim(),
                            odometerKm = km!!,
                            energyKwh = energy!!,
                            startSoc = start!!,
                            endSoc = end!!,
                            chargingType = typeText.trim(),
                            chargingSpeed = speedText.trim(),
                            totalCost = cost,
                            pricePerKwh = unitPrice,
                            soh = soh
                        )

                        val index = editingIndex
                        if (index == null) {
                            entries.add(newEntry)
                        } else {
                            entries[index] = newEntry
                        }

                        val sorted = entries.sortedBy {
                            parseEvDate(it.date)
                        }
                        entries.clear()
                        entries.addAll(sorted)
                        persist()
                        resetForm()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    if (editingIndex == null) {
                        "Ladevorgang speichern"
                    } else {
                        "Änderungen speichern"
                    }
                )
            }

            if (editingIndex != null) {
                TextButton(
                    onClick = { resetForm() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Bearbeitung abbrechen")
                }
            }

            Text(
                "GESPEICHERTE LADEVORGÄNGE (${entries.size})",
                color = EvGold
            )

            if (entries.isEmpty()) {
                Text(
                    "Noch keine Ladevorgänge gespeichert.",
                    color = Color.LightGray
                )
            } else {
                entries.forEachIndexed { index, entry ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF202020)
                        )
                    ) {
                        Column(
                            Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Text(entry.date, color = EvGold)

                            Text(
                                "Kilometerstand: ${entry.odometerKm} km",
                                color = Color.White
                            )

                            Text(
                                "Geladen: ${evFormat(entry.energyKwh)} kWh",
                                color = Color.White
                            )

                            Text(
                                "Akku: ${entry.startSoc}% → ${entry.endSoc}%",
                                color = Color.White
                            )

                            Text(
                                "${entry.chargingType} · ${entry.chargingSpeed}",
                                color = Color.LightGray
                            )

                            Text(
                                "Kosten: ${evFormat(entry.totalCost)} € · " +
                                    "${evFormat(entry.pricePerKwh)} €/kWh",
                                color = Color.White
                            )

                            entry.soh?.let {
                                Text(
                                    "SoH: ${evFormat(it)}%",
                                    color = Color.LightGray
                                )
                            }

                            val previous = chronological.lastOrNull {
                                parseEvDate(it.date) < parseEvDate(entry.date)
                            }

                            if (
                                previous != null &&
                                entry.odometerKm > previous.odometerKm
                            ) {
                                val driven =
                                    entry.odometerKm - previous.odometerKm

                                Text(
                                    "Schätzwert: " +
                                        "${evFormat(entry.energyKwh / driven * 100.0)} " +
                                        "kWh/100 km seit dem vorherigen Eintrag",
                                    color = EvGold
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(
                                    onClick = {
                                        editingIndex = index
                                        date = entry.date
                                        kmText = entry.odometerKm.toString()
                                        energyText = entry.energyKwh.toString()
                                        startText = entry.startSoc.toString()
                                        endText = entry.endSoc.toString()
                                        typeText = entry.chargingType
                                        speedText = entry.chargingSpeed
                                        totalCostText = entry.totalCost.toString()
                                        priceText = entry.pricePerKwh.toString()
                                        sohText = entry.soh?.toString() ?: ""
                                        error = null
                                    }
                                ) {
                                    Text("Bearbeiten")
                                }

                                TextButton(
                                    onClick = {
                                        entries.removeAt(index)
                                        if (editingIndex == index) {
                                            resetForm()
                                        }
                                        persist()
                                    }
                                ) {
                                    Text("Löschen")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun evParse(value: String): Double? =
    value.trim()
        .replace(" ", "")
        .replace(",", ".")
        .toDoubleOrNull()

private fun evFormat(value: Double): String =
    String.format(Locale.GERMANY, "%.2f", value)

private fun parseEvDate(value: String): Long =
    try {
        SimpleDateFormat(
            "dd.MM.yyyy",
            Locale.GERMANY
        ).parse(value)?.time ?: 0L
    } catch (_: Exception) {
        0L
    }
