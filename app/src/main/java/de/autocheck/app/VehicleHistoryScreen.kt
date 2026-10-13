
package de.autocheck.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

private val HistoryGold = Color(0xFFD4AF37)
private val HistoryPanel = Color(0xFF1C1C1C)
private val HistoryMuted = Color(0xFFB8BEC8)

private data class VehicleHistoryEntry(
    val date: String,
    val category: String,
    val title: String,
    val details: String,
    val mileage: Int?,
    val cost: Double?
)

@Composable
fun VehicleHistoryScreen(
    store: VehicleStore,
    vehicles: List<Vehicle>,
    activeVehicleId: String,
    onBack: () -> Unit
) {
    var selectedVehicleId by remember(activeVehicleId) {
        mutableStateOf(activeVehicleId)
    }

    var vehicleMenuExpanded by remember {
        mutableStateOf(false)
    }

    val selectedVehicle = vehicles.firstOrNull {
        it.id == selectedVehicleId
    }

    val history = remember(selectedVehicleId, vehicles) {
        val vehicle = vehicles.firstOrNull {
            it.id == selectedVehicleId
        }

        if (vehicle == null) {
            emptyList()
        } else {
            val vehicleName = vehicle.name

            buildList {
                store.loadRepairs()
                    .filter { it.vehicle == vehicleName }
                    .forEach { repair ->
                        add(
                            VehicleHistoryEntry(
                                date = repair.date,
                                category = "Reparatur",
                                title = repair.description.ifBlank {
                                    repair.repairDescription.ifBlank {
                                        "Reparatur"
                                    }
                                },
                                details = buildString {
                                    if (repair.workshop.isNotBlank()) {
                                        append("Werkstatt: ${repair.workshop}")
                                    }
                                    if (repair.mileage.isNotBlank()) {
                                        if (isNotEmpty()) append(" · ")
                                        append("${repair.mileage} km")
                                    }
                                },
                                mileage = repair.mileage.toIntOrNull(),
                                cost = historyMoney(repair.cost)
                                    .takeIf { it > 0.0 }
                            )
                        )
                    }

                store.loadMaintenance()
                    .filter { it.vehicle == vehicleName }
                    .forEach { maintenance ->
                        add(
                            VehicleHistoryEntry(
                                date = maintenance.date,
                                category = "Wartung",
                                title = maintenance.notes.ifBlank {
                                    "Wartung"
                                },
                                details = buildString {
                                    if (maintenance.workshop.isNotBlank()) {
                                        append("Werkstatt: ${maintenance.workshop}")
                                    }
                                    if (maintenance.mileage.isNotBlank()) {
                                        if (isNotEmpty()) append(" · ")
                                        append("${maintenance.mileage} km")
                                    }
                                },
                                mileage = maintenance.mileage.toIntOrNull(),
                                cost = historyMoney(maintenance.cost)
                                    .takeIf { it > 0.0 }
                            )
                        )
                    }

                store.loadPickerl()
                    .filter { it.vehicle == vehicleName }
                    .forEach { pickerl ->
                        add(
                            VehicleHistoryEntry(
                                date = pickerl.lastDate,
                                category = "Pickerl / TÜV",
                                title = "Letzte Überprüfung",
                                details = buildString {
                                    if (pickerl.lastDate.isNotBlank()) {
                                        append("Letzte Prüfung: ${pickerl.lastDate}")
                                    }
                                    if (pickerl.nextDate.isNotBlank()) {
                                        if (isNotEmpty()) append(" · ")
                                        append("Nächster Termin: ${pickerl.nextDate}")
                                    }
                                    if (pickerl.notes.isNotBlank()) {
                                        if (isNotEmpty()) append("\n")
                                        append(pickerl.notes)
                                    }
                                },
                                mileage = null,
                                cost = null
                            )
                        )
                    }

                store.loadTires()
                    .filter { it.vehicle == vehicleName }
                    .forEach { tires ->
                        add(
                            VehicleHistoryEntry(
                                date = listOf(
                                    tires.purchaseYear,
                                    tires.purchaseMonth
                                )
                                    .filter { it.isNotBlank() }
                                    .joinToString("-"),
                                category = "Reifen",
                                title = "${tires.season} – ${tires.brand}",
                                details = buildString {
                                    if (tires.dimension.isNotBlank()) {
                                        append(tires.dimension)
                                    }
                                    if (tires.rimType.isNotBlank()) {
                                        if (isNotEmpty()) append(" · ")
                                        append(tires.rimType)
                                    }
                                    if (tires.tread.isNotBlank()) {
                                        if (isNotEmpty()) append(" · ")
                                        append("Profil: ${tires.tread}")
                                    }
                                    if (tires.storage.isNotBlank()) {
                                        if (isNotEmpty()) append("\n")
                                        append("Lagerung: ${tires.storage}")
                                    }
                                },
                                mileage = null,
                                cost = historyMoney(tires.price)
                                    .takeIf { it > 0.0 }
                            )
                        )
                    }
            }.sortedWith(
                compareByDescending<VehicleHistoryEntry> {
                    historySortDate(it.date)
                }.thenBy {
                    it.category
                }
            )
        }
    }

    val totalCosts = history.sumOf {
        it.cost ?: 0.0
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
            contentPadding = PaddingValues(
                start = 18.dp,
                end = 18.dp,
                top = 16.dp,
                bottom = 20.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp, bottom = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Fahrzeughistorie",
                        color = HistoryGold,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Alle vorhandenen Fahrzeugereignisse auf einen Blick.",
                        color = HistoryMuted,
                        fontSize = 14.sp
                    )
                }
            }

            item {
                if (vehicles.isEmpty()) {
                    HistoryCard {
                        Text(
                            "Noch kein Fahrzeug angelegt. Lege zuerst unter „Mein Auto“ ein Fahrzeug an.",
                            color = Color.White
                        )
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
                            onClick = {
                                vehicleMenuExpanded = true
                            },
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
                                    }
                                )
                            }
                        }
                    }
                }
            }

            if (selectedVehicle != null) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        HistoryCard(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                "Ereignisse",
                                color = HistoryMuted,
                                fontSize = 12.sp
                            )
                            Text(
                                history.size.toString(),
                                color = HistoryGold,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        HistoryCard(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                "Erfasste Kosten",
                                color = HistoryMuted,
                                fontSize = 12.sp
                            )
                            Text(
                                String.format(
                                    Locale.GERMANY,
                                    "%.2f €",
                                    totalCosts
                                ),
                                color = HistoryGold,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                item {
                    Text(
                        "Chronologischer Verlauf",
                        color = Color.White,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (history.isEmpty()) {
                    item {
                        HistoryCard {
                            Text(
                                "Für dieses Fahrzeug sind noch keine Reparaturen, Wartungen, Pickerl- oder Reifeneinträge vorhanden.",
                                color = HistoryMuted
                            )
                        }
                    }
                } else {
                    items(
                        items = history,
                        key = {
                            "${it.category}_${it.date}_${it.title}_${it.details}"
                        }
                    ) { entry ->
                        HistoryCard {
                            Text(
                                text = entry.category.uppercase(Locale.GERMANY),
                                color = HistoryGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = entry.title,
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )

                            if (entry.date.isNotBlank()) {
                                Text(
                                    text = "Datum: ${entry.date}",
                                    color = HistoryMuted,
                                    fontSize = 12.sp
                                )
                            }

                            if (entry.details.isNotBlank()) {
                                Text(
                                    text = entry.details,
                                    color = HistoryMuted,
                                    fontSize = 13.sp
                                )
                            }

                            entry.cost?.let { cost ->
                                Text(
                                    text = String.format(
                                        Locale.GERMANY,
                                        "Kosten: %.2f €",
                                        cost
                                    ),
                                    color = HistoryGold,
                                    fontWeight = FontWeight.SemiBold
                                )
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
private fun HistoryCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = HistoryPanel
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            content()
        }
    }
}

private fun historyMoney(value: String): Double {
    val cleaned = value
        .replace("€", "")
        .replace(" ", "")
        .trim()

    if (cleaned.isBlank()) return 0.0

    return try {
        if (cleaned.contains(",") && cleaned.contains(".")) {
            cleaned.replace(".", "").replace(",", ".").toDouble()
        } else {
            cleaned.replace(",", ".").toDouble()
        }
    } catch (_: NumberFormatException) {
        0.0
    }
}

private fun historySortDate(value: String): String {
    val trimmed = value.trim()

    if (trimmed.matches(Regex("""\d{4}-\d{2}-\d{2}"""))) {
        return trimmed
    }

    if (trimmed.matches(Regex("""\d{4}-\d{1,2}"""))) {
        val parts = trimmed.split("-")
        return "${parts[0]}-${parts[1].padStart(2, '0')}-01"
    }

    if (trimmed.matches(Regex("""\d{1,2}\.\d{1,2}\.\d{4}"""))) {
        val parts = trimmed.split(".")
        return "${parts[2]}-${parts[1].padStart(2, '0')}-${parts[0].padStart(2, '0')}"
    }

    return trimmed
}
