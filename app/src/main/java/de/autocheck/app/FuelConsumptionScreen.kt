
package de.autocheck.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val FuelGold = Color(0xFFD4AF37)
private val FuelPanel = Color(0xFF1C1C1C)
private val FuelMuted = Color(0xFFB8BEC8)

private data class FuelEntry(
    val id: String,
    val date: String,
    val odometer: Int,
    val liters: Double,
    val totalCost: Double
)

@Composable
fun FuelConsumptionScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current

    val vehicleStore = remember {
        VehicleStore(context)
    }

    val vehicles = remember {
        vehicleStore.load()
    }

    var selectedVehicleId by remember {
        mutableStateOf(vehicleStore.activeVehicleId())
    }

    val selectedVehicle = vehicles.firstOrNull {
        it.id == selectedVehicleId
    }

    val preferences = remember {
        context.getSharedPreferences(
            "carvita_fuel_data",
            android.content.Context.MODE_PRIVATE
        )
    }

    val entries = remember(selectedVehicleId) {
        mutableStateListOf<FuelEntry>().apply {
            if (selectedVehicleId.isNotBlank()) {
                addAll(
                    loadFuelEntries(
                        preferences.getString(
                            "fuel_$selectedVehicleId",
                            "[]"
                        ) ?: "[]"
                    )
                )
            }
        }
    }

    var vehicleMenuExpanded by remember {
        mutableStateOf(false)
    }

    var date by remember {
        mutableStateOf(
            SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.GERMANY
            ).format(Date())
        )
    }

    var odometerText by remember { mutableStateOf("") }
    var litersText by remember { mutableStateOf("") }
    var totalCostText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val sortedEntries = entries.sortedBy { it.odometer }
    val totalFuelCost = entries.sumOf { it.totalCost }
    val totalLiters = entries.sumOf { it.liters }

    val averageConsumption = if (sortedEntries.size < 2) {
        null
    } else {
        val distance =
            sortedEntries.last().odometer - sortedEntries.first().odometer

        if (distance <= 0) {
            null
        } else {
            sortedEntries.drop(1).sumOf { it.liters } * 100.0 / distance
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 28.dp)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 18.dp,
                end = 18.dp,
                top = 12.dp,
                bottom = 20.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp, bottom = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Tank und Kilometer – Verbrauch",
                        color = FuelGold,
                        fontSize = 23.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Deine Tankvorgänge und Kosten im Überblick.",
                        color = FuelMuted,
                        fontSize = 14.sp
                    )
                }
            }

            item {
                if (vehicles.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = FuelPanel
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                "Noch kein Fahrzeug angelegt",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                "Lege zuerst unter „Mein Auto“ ein Fahrzeug an. Danach kannst du Tankvorgänge erfassen.",
                                color = FuelMuted
                            )

                            OutlinedButton(
                                onClick = onBack,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Zurück")
                            }
                        }
                    }
                } else {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "Fahrzeug auswählen",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )

                        OutlinedButton(
                            onClick = { vehicleMenuExpanded = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                selectedVehicle?.let {
                                    "${it.name} · ${it.make} ${it.model}"
                                } ?: "Fahrzeug auswählen",
                                color = Color.White
                            )
                        }

                        DropdownMenu(
                            expanded = vehicleMenuExpanded,
                            onDismissRequest = {
                                vehicleMenuExpanded = false
                            }
                        ) {
                            vehicles.forEach { vehicle ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            "${vehicle.name} · ${vehicle.make} ${vehicle.model}"
                                        )
                                    },
                                    onClick = {
                                        selectedVehicleId = vehicle.id
                                        vehicleMenuExpanded = false
                                        vehicleStore.setActiveVehicleId(vehicle.id)
                                    }
                                )
                            }
                        }
                    }
                }
            }

            if (selectedVehicle != null) {
                item {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            "Deine Übersicht",
                            color = Color.White,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            FuelSummaryCard(
                                title = "Tankkosten",
                                value = fuelMoney(totalFuelCost),
                                modifier = Modifier.weight(1f)
                            )

                            FuelSummaryCard(
                                title = "Getankt",
                                value = String.format(
                                    Locale.GERMANY,
                                    "%.1f l",
                                    totalLiters
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        FuelSummaryCard(
                            title = "Durchschnittsverbrauch",
                            value = averageConsumption?.let {
                                String.format(
                                    Locale.GERMANY,
                                    "%.2f l/100 km",
                                    it
                                )
                            } ?: "Noch nicht verfügbar",
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text(
                            text = "Für einen Verbrauchswert werden mindestens zwei Tankvorgänge mit unterschiedlichen Kilometerständen benötigt.",
                            color = FuelMuted,
                            fontSize = 12.sp
                        )
                    }
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = FuelPanel
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                "Tankvorgang erfassen",
                                color = FuelGold,
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold
                            )

                            OutlinedTextField(
                                value = date,
                                onValueChange = {
                                    date = it
                                    errorMessage = null
                                },
                                label = { Text("Datum (JJJJ-MM-TT)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = odometerText,
                                onValueChange = {
                                    odometerText = it.filter { c -> c.isDigit() }
                                    errorMessage = null
                                },
                                label = { Text("Kilometerstand") },
                                suffix = { Text("km") },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number
                                ),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = litersText,
                                onValueChange = {
                                    litersText = it
                                    errorMessage = null
                                },
                                label = { Text("Getankte Liter") },
                                suffix = { Text("l") },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Decimal
                                ),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = totalCostText,
                                onValueChange = {
                                    totalCostText = it
                                    errorMessage = null
                                },
                                label = { Text("Gesamtpreis") },
                                suffix = { Text("€") },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Decimal
                                ),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            val litersValue = parseFuelNumber(litersText)
                            val costValue = parseFuelNumber(totalCostText)

                            if (
                                litersValue != null &&
                                litersValue > 0.0 &&
                                costValue != null &&
                                costValue >= 0.0
                            ) {
                                Text(
                                    text = "Preis pro Liter: ${
                                        fuelMoney(costValue / litersValue)
                                    } / l",
                                    color = FuelGold,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            errorMessage?.let {
                                Text(
                                    text = it,
                                    color = Color(0xFFFF7777),
                                    fontSize = 13.sp
                                )
                            }

                            Button(
                                onClick = {
                                    val km = odometerText.toIntOrNull()
                                    val liters = parseFuelNumber(litersText)
                                    val cost = parseFuelNumber(totalCostText)

                                    when {
                                        date.isBlank() -> {
                                            errorMessage = "Bitte gib ein Datum ein."
                                        }

                                        km == null || km < 0 -> {
                                            errorMessage = "Bitte gib einen gültigen Kilometerstand ein."
                                        }

                                        liters == null || liters <= 0.0 -> {
                                            errorMessage = "Bitte gib eine Literzahl größer als null ein."
                                        }

                                        cost == null || cost < 0.0 -> {
                                            errorMessage = "Bitte gib einen gültigen Gesamtpreis ein."
                                        }

                                        entries.any { it.odometer == km } -> {
                                            errorMessage = "Für diesen Kilometerstand gibt es bereits einen Tankvorgang."
                                        }

                                        entries.any { it.odometer > km } -> {
                                            errorMessage = "Der Kilometerstand muss größer als die bisherigen Kilometerstände sein."
                                        }

                                        else -> {
                                            entries.add(
                                                FuelEntry(
                                                    id = java.util.UUID.randomUUID().toString(),
                                                    date = date.trim(),
                                                    odometer = km,
                                                    liters = liters,
                                                    totalCost = cost
                                                )
                                            )

                                            preferences.edit()
                                                .putString(
                                                    "fuel_${selectedVehicle.id}",
                                                    fuelEntriesToJson(entries)
                                                )
                                                .apply()

                                            odometerText = ""
                                            litersText = ""
                                            totalCostText = ""
                                            errorMessage = null
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Tankvorgang speichern")
                            }
                        }
                    }
                }

                item {
                    Text(
                        "Tankverlauf",
                        color = Color.White,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (entries.isEmpty()) {
                    item {
                        Text(
                            "Noch keine Tankvorgänge gespeichert.",
                            color = FuelMuted
                        )
                    }
                } else {
                    items(
                        items = entries.sortedByDescending { it.odometer },
                        key = { it.id }
                    ) { entry ->
                        val previousEntry = sortedEntries.lastOrNull {
                            it.odometer < entry.odometer
                        }

                        val consumption = previousEntry?.let {
                            val distance = entry.odometer - it.odometer
                            if (distance > 0) {
                                entry.liters * 100.0 / distance
                            } else {
                                null
                            }
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = FuelPanel
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    entry.date,
                                    color = FuelGold,
                                    fontWeight = FontWeight.Bold
                                )

                                Text(
                                    "${entry.odometer} km · ${
                                        String.format(
                                            Locale.GERMANY,
                                            "%.2f l",
                                            entry.liters
                                        )
                                    }",
                                    color = Color.White
                                )

                                Text(
                                    "Gesamt: ${fuelMoney(entry.totalCost)} · ${
                                        fuelMoney(entry.totalCost / entry.liters)
                                    } / l",
                                    color = FuelMuted
                                )

                                Text(
                                    consumption?.let {
                                        "Verbrauch seit vorherigem Eintrag: ${
                                            String.format(
                                                Locale.GERMANY,
                                                "%.2f l/100 km",
                                                it
                                            )
                                        }"
                                    } ?: "Für diesen Eintrag ist noch kein Verbrauch berechenbar.",
                                    color = FuelMuted,
                                    fontSize = 12.sp
                                )

                                OutlinedButton(
                                    onClick = {
                                        entries.remove(entry)

                                        preferences.edit()
                                            .putString(
                                                "fuel_${selectedVehicle.id}",
                                                fuelEntriesToJson(entries)
                                            )
                                            .apply()
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Tankvorgang löschen")
                                }
                            }
                        }
                    }
                }
            }
        }

        OutlinedButton(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 18.dp,
                    end = 18.dp,
                    top = 6.dp,
                    bottom = 12.dp
                )
        ) {
            Text("Zurück zur zweiten Startseite")
        }
    }
}

@Composable
private fun FuelSummaryCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = FuelPanel
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                title,
                color = FuelMuted,
                fontSize = 12.sp
            )

            Text(
                value,
                color = FuelGold,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private fun parseFuelNumber(value: String): Double? {
    val normalized = value
        .trim()
        .replace(" ", "")
        .replace(",", ".")

    if (normalized.isBlank()) return null

    return normalized.toDoubleOrNull()
}

private fun fuelMoney(value: Double): String {
    return String.format(
        Locale.GERMANY,
        "%.2f €",
        value
    )
}

private fun loadFuelEntries(json: String): List<FuelEntry> {
    val array = JSONArray(json)

    return buildList {
        for (i in 0 until array.length()) {
            val obj = array.optJSONObject(i) ?: continue

            add(
                FuelEntry(
                    id = obj.optString("id"),
                    date = obj.optString("date"),
                    odometer = obj.optInt("odometer"),
                    liters = obj.optDouble("liters"),
                    totalCost = obj.optDouble("totalCost")
                )
            )
        }
    }
}

private fun fuelEntriesToJson(entries: List<FuelEntry>): String {
    val array = JSONArray()

    entries.forEach { entry ->
        array.put(
            JSONObject().apply {
                put("id", entry.id)
                put("date", entry.date)
                put("odometer", entry.odometer)
                put("liters", entry.liters)
                put("totalCost", entry.totalCost)
            }
        )
    }

    return array.toString()
}
