package de.autocheck.app

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale

/*
 * ============================================================
 * WARTUNGSDATEN
 * ============================================================
 *
 * Jeder gespeicherte Wartungseintrag ist unabhängig.
 *
 * Dadurch sind beispielsweise möglich:
 *
 * Ölwechsel 06/2024
 * Ölwechsel 08/2025
 * Ölwechsel 09/2026
 *
 * ohne dass ein vorhandener Eintrag überschrieben wird.
 */

private const val MAINTENANCE_ENTRY_V3 =
    "[WARTUNG_ENTRY_V3]"

private const val OLD_MAINTENANCE_V2 =
    "[WARTUNGSPLAN_V2]"

private const val OLD_MAINTENANCE =
    "[WARTUNGEN]"

private const val OLD_SEPARATOR =
    "||"

/*
 * Feste Wartungspunkte.
 */
private val standardMaintenanceItems =
    listOf(
        "ZAHNRIEMEN MIT WASSERPUMPE",
        "ÖL",
        "ÖLFILTER",
        "LUFTFILTER",
        "KRAFTSTOFFFILTER",
        "INNENRAUMFILTER",
        "BATTERIE",
        "BREMSFLÜSSIGKEIT",
        "KÜHLFLÜSSIGKEIT"
    )

private val maintenanceMonths =
    (1..12).map {
        it.toString()
    }

/*
 * ============================================================
 * ENTWURF EINES WARTUNGSEINTRAGS
 * ============================================================
 */

private data class MaintenanceDraftItem(
    val name: String,
    val selected: Boolean = false,
    val month: String = "",
    val year: String = "",
    val mileage: String = "",
    val used: String = "",
    val partNumber: String = "",
    val materialCost: String = "",
    val laborCost: String = "",
    val imageUri: String = "",
    val custom: Boolean = false
)

/*
 * ============================================================
 * GESPEICHERTER WARTUNGSEINTRAG FÜR DIE ANZEIGE
 * ============================================================
 */

private data class MaintenanceHistoryItem(
    val id: Long,
    val vehicleId: String,
    val vehicleName: String,
    val name: String,
    val month: String,
    val year: String,
    val mileage: String,
    val used: String,
    val partNumber: String,
    val materialCost: Double,
    val laborCost: Double,
    val totalCost: Double,
    val imageUri: String,
    val workshop: String,
    val notes: String
)

/*
 * ============================================================
 * ZAHLEN
 * ============================================================
 */

private fun parseMoney(
    value: String
): Double {

    return value
        .trim()
        .replace("€", "")
        .replace(" ", "")
        .replace(".", "")
        .replace(",", ".")
        .toDoubleOrNull()
        ?: 0.0
}

private fun money(
    value: Double
): String {

    return String.format(
        Locale.GERMANY,
        "%.2f €",
        value
    )
}

/*
 * ============================================================
 * SPEICHERN EINES EINZELNEN WARTUNGSEINTRAGS
 * ============================================================
 */

private fun encodeMaintenanceEntry(
    vehicleId: String,
    item: MaintenanceDraftItem,
    notes: String
): String {

    val json =
        JSONObject()

    json.put("vehicleId", vehicleId)
    json.put("name", item.name.trim())
    json.put("month", item.month.trim())
    json.put("year", item.year.trim())
    json.put("mileage", item.mileage.trim())
    json.put("used", item.used.trim())
    json.put("partNumber", item.partNumber.trim())
    json.put("materialCost", item.materialCost.trim())
    json.put("laborCost", item.laborCost.trim())
    json.put("imageUri", item.imageUri.trim())
    json.put("custom", item.custom)
    json.put("notes", notes.trim())

    return MAINTENANCE_ENTRY_V3 +
        json.toString()
}

/*
 * ============================================================
 * AUS EINEM ALTEN EINTRAG EIN ITEM ERZEUGEN
 * ============================================================
 */

private fun decodeMaintenanceEntry(
    entry: Maintenance,
    fallbackVehicleId: String
): List<MaintenanceHistoryItem> {

    val notes =
        entry.notes.trim()

    /*
     * ------------------------------------------------------------
     * NEUES FORMAT
     * ------------------------------------------------------------
     */

    if (
        notes.startsWith(
            MAINTENANCE_ENTRY_V3
        )
    ) {

        return try {

            val json =
                JSONObject(
                    notes.removePrefix(
                        MAINTENANCE_ENTRY_V3
                    )
                )

            val vehicleId =
                json.optString(
                    "vehicleId",
                    fallbackVehicleId
                )

            val name =
                json.optString(
                    "name",
                    "Wartung"
                )

            val month =
                json.optString("month", "")

            val year =
                json.optString("year", "")

            val mileage =
                json.optString(
                    "mileage",
                    entry.mileage
                )

            val used =
                json.optString("used", "")

            val partNumber =
                json.optString("partNumber", "")

            val material =
                parseMoney(
                    json.optString(
                        "materialCost",
                        entry.cost
                    )
                )

            val labor =
                parseMoney(
                    json.optString(
                        "laborCost",
                        "0"
                    )
                )

            val imageUri =
                json.optString("imageUri", "")

            val freeNotes =
                json.optString("notes", "")

            listOf(
                MaintenanceHistoryItem(
                    id = entry.id,
                    vehicleId = vehicleId,
                    vehicleName = entry.vehicle,
                    name = name,
                    month = month,
                    year = year,
                    mileage = mileage,
                    used = used,
                    partNumber = partNumber,
                    materialCost = material,
                    laborCost = labor,
                    totalCost = material + labor,
                    imageUri = imageUri,
                    workshop = entry.workshop,
                    notes = freeNotes
                )
            )

        } catch (
            _: Exception
        ) {
            emptyList()
        }
    }

    /*
     * ------------------------------------------------------------
     * ALTES WARTUNGSPLAN-V2-FORMAT
     * ------------------------------------------------------------
     */

    if (
        notes.startsWith(
            OLD_MAINTENANCE_V2
        )
    ) {

        return try {

            val rest =
                notes.removePrefix(
                    OLD_MAINTENANCE_V2
                )

            val parts =
                rest.split("\n", limit = 2)

            val jsonText =
                parts.firstOrNull()?.trim() ?: ""

            val freeNotes =
                if (parts.size > 1) {
                    parts[1].trim()
                } else {
                    ""
                }

            val array =
                JSONArray(jsonText)

            val names =
                buildList {

                    for (
                        index in 0 until array.length()
                    ) {

                        val json =
                            array.getJSONObject(index)

                        val name =
                            json.optString("name", "")

                        if (name.isNotBlank()) {
                            add(name)
                        }
                    }
                }

            val first =
                if (array.length() > 0) {
                    array.getJSONObject(0)
                } else {
                    null
                }

            val month =
                first?.optString("month", "") ?: ""

            val year =
                first?.optString("year", "") ?: ""

            val mileage =
                first?.optString(
                    "mileage",
                    entry.mileage
                ) ?: entry.mileage

            val total =
                parseMoney(entry.cost)

            listOf(
                MaintenanceHistoryItem(
                    id = entry.id,
                    vehicleId = fallbackVehicleId,
                    vehicleName = entry.vehicle,
                    name = names.joinToString(" + ")
                        .ifBlank { "Wartung" },
                    month = month,
                    year = year,
                    mileage = mileage,
                    used = "",
                    partNumber = "",
                    materialCost = total,
                    laborCost = 0.0,
                    totalCost = total,
                    imageUri = "",
                    workshop = entry.workshop,
                    notes = freeNotes
                )
            )

        } catch (
            _: Exception
        ) {
            emptyList()
        }
    }

    /*
     * ------------------------------------------------------------
     * SEHR ALTES FORMAT
     * ------------------------------------------------------------
     */

    if (
        notes.startsWith(
            OLD_MAINTENANCE
        )
    ) {

        val rest =
            notes.removePrefix(
                OLD_MAINTENANCE
            )

        val parts =
            rest.split("\n", limit = 2)

        val itemText =
            parts.firstOrNull()?.trim() ?: ""

        val freeNotes =
            if (parts.size > 1) {
                parts[1].trim()
            } else {
                ""
            }

        val names =
            itemText
                .split(OLD_SEPARATOR)
                .map { it.trim() }
                .filter { it.isNotBlank() }

        return listOf(
            MaintenanceHistoryItem(
                id = entry.id,
                vehicleId = fallbackVehicleId,
                vehicleName = entry.vehicle,
                name = names.joinToString(" + ")
                    .ifBlank { "Wartung" },
                month = legacyMonth(entry.date),
                year = legacyYear(entry.date),
                mileage = entry.mileage,
                used = "",
                partNumber = "",
                materialCost = parseMoney(entry.cost),
                laborCost = 0.0,
                totalCost = parseMoney(entry.cost),
                imageUri = "",
                workshop = entry.workshop,
                notes = freeNotes
            )
        )
    }

    /*
     * ------------------------------------------------------------
     * ALTES FREIES NOTIZFELD
     * ------------------------------------------------------------
     */

    return listOf(
        MaintenanceHistoryItem(
            id = entry.id,
            vehicleId = fallbackVehicleId,
            vehicleName = entry.vehicle,
            name = "Wartung",
            month = legacyMonth(entry.date),
            year = legacyYear(entry.date),
            mileage = entry.mileage,
            used = "",
            partNumber = "",
            materialCost = parseMoney(entry.cost),
            laborCost = 0.0,
            totalCost = parseMoney(entry.cost),
            imageUri = "",
            workshop = entry.workshop,
            notes = notes
        )
    )
}

/*
 * ============================================================
 * ALTE DATUMSFUNKTIONEN
 * ============================================================
 */

private fun legacyMonth(
    date: String
): String {

    val value =
        date.trim()

    if (value.isBlank()) {
        return ""
    }

    val parts =
        when {
            value.contains(".") -> value.split(".")
            value.contains("-") -> value.split("-")
            else -> emptyList()
        }

    if (parts.size != 3) {
        return ""
    }

    return when {
        parts[0].length == 4 ->
            parts[1].toIntOrNull()?.toString() ?: ""

        parts[2].length == 4 ->
            parts[1].toIntOrNull()?.toString() ?: ""

        else -> ""
    }
}

private fun legacyYear(
    date: String
): String {

    val value =
        date.trim()

    if (value.isBlank()) {
        return ""
    }

    val parts =
        when {
            value.contains(".") -> value.split(".")
            value.contains("-") -> value.split("-")
            else -> emptyList()
        }

    if (parts.size != 3) {
        return ""
    }

    return when {
        parts[0].length == 4 -> parts[0]
        parts[2].length == 4 -> parts[2]
        else -> ""
    }
}

/*
 * ============================================================
 * BILD LADEN
 * ============================================================
 */

private fun loadBitmap(
    context: Context,
    uriString: String
): Bitmap? {

    if (uriString.isBlank()) {
        return null
    }

    return try {

        val uri =
            Uri.parse(uriString)

        context.contentResolver
            .openInputStream(uri)
            ?.use {
                BitmapFactory.decodeStream(it)
            }

    } catch (
        _: Exception
    ) {
        null
    }
}

/*
 * ============================================================
 * HAUPTSCREEN
 * ============================================================
 */

@Composable
fun MaintenanceScreen(
    store: VehicleStore,
    vehicles: List<Vehicle>,
    activeVehicle: String,
    onActiveVehicle: (String) -> Unit,
    onVisited: () -> Unit
) {

    val context =
        LocalContext.current

    LaunchedEffect(Unit) {
        onVisited()
    }

    /*
     * Aktives Fahrzeug.
     */

    val currentVehicle =
        vehicles.firstOrNull()

    val activeVehicleId =
        currentVehicle?.id ?: ""

    /*
     * ============================================================
     * FORMULAR
     * ============================================================
     */

    var showForm by remember {
        mutableStateOf(false)
    }

    var editingId by remember {
        mutableStateOf<Long?>(null)
    }

    var draftItems by remember {
        mutableStateOf(
            standardMaintenanceItems.map {
                MaintenanceDraftItem(name = it)
            }
        )
    }

    var customItemName by remember {
        mutableStateOf("")
    }

    var workshop by remember {
        mutableStateOf("")
    }

    var notes by remember {
        mutableStateOf("")
    }

    var entries by remember {
        mutableStateOf(store.loadMaintenance())
    }

    /*
     * ============================================================
     * BILD-AUSWAHL
     * ============================================================
     */

    var imageTargetIndex by remember {
        mutableStateOf<Int?>(null)
    }

    val imageLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.OpenDocument()
        ) { uri ->

            val index =
                imageTargetIndex

            if (
                uri != null &&
                index != null
            ) {

                try {

                    context.contentResolver
                        .takePersistableUriPermission(
                            uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                        )

                } catch (
                    _: SecurityException
                ) {
                    // Provider unterstützt eventuell
                    // keine persistente Berechtigung.
                }

                if (index in draftItems.indices) {

                    val updated =
                        draftItems.toMutableList()

                    updated[index] =
                        updated[index].copy(
                            imageUri = uri.toString()
                        )

                    draftItems = updated
                }
            }

            imageTargetIndex = null
        }

    /*
     * ============================================================
     * HISTORIE
     * ============================================================
     */

    val history =
        buildList {

            entries.forEach { entry ->

                val decoded =
                    decodeMaintenanceEntry(
                        entry = entry,
                        fallbackVehicleId = activeVehicleId
                    )

                addAll(
                    decoded.filter { item ->

                        if (item.vehicleId.isNotBlank()) {
                            item.vehicleId == activeVehicleId
                        } else {
                            entry.vehicle == activeVehicle
                        }
                    }
                )
            }
        }
            .sortedWith(
                compareByDescending<MaintenanceHistoryItem> {
                    it.year.toIntOrNull() ?: 0
                }.thenByDescending {
                    it.month.toIntOrNull() ?: 0
                }
            )

    val totalInvestment =
        history.sumOf {
            it.totalCost
        }

    /*
     * ============================================================
     * FORM RESET
     * ============================================================
     */

    fun resetForm() {

        editingId = null

        draftItems =
            standardMaintenanceItems.map {
                MaintenanceDraftItem(name = it)
            }

        customItemName = ""
        workshop = ""
        notes = ""
        showForm = false
    }

    /*
     * ============================================================
     * NEUER EINTRAG
     * ============================================================
     */

    fun startNewEntry() {

        editingId = null

        draftItems =
            standardMaintenanceItems.map {
                MaintenanceDraftItem(name = it)
            }

        customItemName = ""
        workshop = ""
        notes = ""
        showForm = true
    }

    /*
     * ============================================================
     * BEARBEITEN
     * ============================================================
     */

    fun editEntry(
        historyItem: MaintenanceHistoryItem
    ) {

        val item =
            MaintenanceDraftItem(
                name = historyItem.name,
                selected = true,
                month = historyItem.month,
                year = historyItem.year,
                mileage = historyItem.mileage,
                used = historyItem.used,
                partNumber = historyItem.partNumber,
                materialCost =
                    if (historyItem.materialCost > 0) {
                        historyItem.materialCost.toString()
                    } else {
                        ""
                    },
                laborCost =
                    if (historyItem.laborCost > 0) {
                        historyItem.laborCost.toString()
                    } else {
                        ""
                    },
                imageUri = historyItem.imageUri,
                custom = !standardMaintenanceItems.contains(
                    historyItem.name
                )
            )

        draftItems =
            if (item.custom) {

                standardMaintenanceItems.map {
                    MaintenanceDraftItem(name = it)
                } + item

            } else {

                standardMaintenanceItems.map {
                    if (it == item.name) {
                        item
                    } else {
                        MaintenanceDraftItem(name = it)
                    }
                }
            }

        workshop = historyItem.workshop
        notes = historyItem.notes
        editingId = historyItem.id
        showForm = true
    }

    /*
     * ============================================================
     * LÖSCHEN
     * ============================================================
     */

    fun deleteEntry(
        historyItem: MaintenanceHistoryItem
    ) {

        entries =
            entries.filterNot {
                it.id == historyItem.id
            }

        store.saveMaintenance(entries)
    }

    /*
     * ============================================================
     * SPEICHERN
     * ============================================================
     */

    fun saveForm() {

        val selected =
            draftItems.filter {
                it.selected && it.name.isNotBlank()
            }

        if (selected.isEmpty()) {
            return
        }

        /*
         * --------------------------------------------------------
         * BEARBEITEN
         * --------------------------------------------------------
         */

        if (editingId != null) {

            val item =
                selected.first()

            val updatedEntry =
                Maintenance(
                    id = editingId!!,
                    vehicle = activeVehicle,
                    date =
                        if (
                            item.month.isNotBlank() &&
                            item.year.isNotBlank()
                        ) {
                            "%02d/%s".format(
                                Locale.GERMANY,
                                item.month.toIntOrNull() ?: 0,
                                item.year
                            )
                        } else {
                            ""
                        },
                    mileage = item.mileage.trim(),
                    cost = (
                        parseMoney(item.materialCost) +
                            parseMoney(item.laborCost)
                        ).toString(),
                    workshop = workshop.trim(),
                    notes = encodeMaintenanceEntry(
                        vehicleId = activeVehicleId,
                        item = item,
                        notes = notes
                    )
                )

            entries =
                entries.map {
                    if (it.id == editingId) {
                        updatedEntry
                    } else {
                        it
                    }
                }

            store.saveMaintenance(entries)
            resetForm()
            return
        }

        /*
         * --------------------------------------------------------
         * NEUE EINTRÄGE
         * --------------------------------------------------------
         *
         * Jeder ausgewählte Wartungspunkt wird zu einem
         * eigenen Datensatz.
         */

        val now =
            System.currentTimeMillis()

        val newEntries =
            selected.mapIndexed { index, item ->

                Maintenance(
                    id = now + index,
                    vehicle = activeVehicle,
                    date =
                        if (
                            item.month.isNotBlank() &&
                            item.year.isNotBlank()
                        ) {
                            "%02d/%s".format(
                                Locale.GERMANY,
                                item.month.toIntOrNull() ?: 0,
                                item.year
                            )
                        } else {
                            ""
                        },
                    mileage = item.mileage.trim(),
                    cost = (
                        parseMoney(item.materialCost) +
                            parseMoney(item.laborCost)
                        ).toString(),
                    workshop = workshop.trim(),
                    notes = encodeMaintenanceEntry(
                        vehicleId = activeVehicleId,
                        item = item,
                        notes = notes
                    )
                )
            }

        entries = entries + newEntries
        store.saveMaintenance(entries)
        resetForm()
    }

    /*
     * ============================================================
     * UI
     * ============================================================
     */

    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        VehicleSelector(
            vehicles = vehicles,
            activeVehicle = activeVehicle,
            onSelected = onActiveVehicle
        )

        Spacer(
            modifier = Modifier.height(10.dp)
        )

        if (vehicles.isEmpty()) {

            Text(
                "Kein Fahrzeug vorhanden.",
                color = Color.White,
                modifier = Modifier.padding(16.dp)
            )

            return@Column
        }

        /*
         * ========================================================
         * HISTORIE
         * ========================================================
         */

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            /*
             * ----------------------------------------------------
             * GESAMTBILD
             * ----------------------------------------------------
             */

            item {

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF11141A)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Column(
                        Modifier.padding(16.dp)
                    ) {

                        Text(
                            "WARTUNGSHISTORIE",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(
                            Modifier.height(6.dp)
                        )

                        Text(
                            "Gesamte bisherige Investition",
                            color = Color(0xFFB8BEC8)
                        )

                        Spacer(
                            Modifier.height(4.dp)
                        )

                        Text(
                            money(totalInvestment),
                            color = Color(0xFFD4AF37),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            /*
             * ----------------------------------------------------
             * NEUE WARTUNG
             * ----------------------------------------------------
             */

            item {

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = ::startNewEntry
                ) {
                    Text("Wartung hinzufügen")
                }
            }

            /*
             * ----------------------------------------------------
             * FORMULAR
             * ----------------------------------------------------
             */

            if (showForm) {

                item {

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF11141A)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {

                        Column(
                            Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {

                            Text(
                                if (editingId != null) {
                                    "Wartung bearbeiten"
                                } else {
                                    "Neue Wartung"
                                },
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )

                            /*
                             * FESTE WARTUNGEN
                             */

                            draftItems.forEachIndexed { index, item ->

                                MaintenanceDraftCard(
                                    item = item,
                                    onItemChanged = { updated ->

                                        val list =
                                            draftItems.toMutableList()

                                        list[index] = updated
                                        draftItems = list
                                    },
                                    onChooseImage = {

                                        imageTargetIndex = index

                                        imageLauncher.launch(
                                            arrayOf("image/*")
                                        )
                                    },
                                    onRemoveImage = {

                                        val list =
                                            draftItems.toMutableList()

                                        list[index] =
                                            list[index].copy(
                                                imageUri = ""
                                            )

                                        draftItems = list
                                    }
                                )
                            }

                            /*
                             * EIGENE WARTUNG
                             */

                            OutlinedTextField(
                                value = customItemName,
                                onValueChange = {
                                    customItemName = it
                                },
                                label = {
                                    Text("Eigene Wartung")
                                },
                                modifier = Modifier.fillMaxWidth()
                            )

                            Button(
                                onClick = {

                                    val customName =
                                        customItemName.trim()

                                    if (customName.isNotBlank()) {

                                        val exists =
                                            draftItems.any {
                                                it.name.equals(
                                                    customName,
                                                    ignoreCase = true
                                                )
                                            }

                                        if (!exists) {

                                            draftItems =
                                                draftItems +
                                                    MaintenanceDraftItem(
                                                        name = customName,
                                                        selected = true,
                                                        custom = true
                                                    )
                                        }

                                        customItemName = ""
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Eigene Wartung hinzufügen")
                            }

                            /*
                             * WERKSTATT
                             */

                            OutlinedTextField(
                                value = workshop,
                                onValueChange = {
                                    workshop = it
                                },
                                label = {
                                    Text("Werkstatt")
                                },
                                modifier = Modifier.fillMaxWidth()
                            )

                            /*
                             * NOTIZ
                             */

                            OutlinedTextField(
                                value = notes,
                                onValueChange = {
                                    notes = it
                                },
                                label = {
                                    Text("Notiz")
                                },
                                modifier = Modifier.fillMaxWidth()
                            )

                            /*
                             * AKTIONEN
                             */

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {

                                Button(
                                    modifier = Modifier.weight(1f),
                                    onClick = ::saveForm
                                ) {
                                    Text("Alles speichern")
                                }

                                OutlinedButton(
                                    modifier = Modifier.weight(1f),
                                    onClick = ::resetForm
                                ) {
                                    Text("Abbrechen")
                                }
                            }
                        }
                    }
                }
            }

            /*
             * LEER
             */

            if (history.isEmpty() && !showForm) {

                item {

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF11141A)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {

                        Text(
                            "Noch keine Wartungen gespeichert.",
                            color = Color(0xFFB8BEC8),
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }

            /*
             * HISTORIE
             */

            items(
                history,
                key = {
                    "${it.id}_${it.name}"
                }
            ) { item ->

                MaintenanceHistoryCard(
                    item = item,
                    onEdit = {
                        editEntry(item)
                    },
                    onDelete = {
                        deleteEntry(item)
                    }
                )
            }
        }
    }
}

/*
 * ============================================================
 * FORMULAR-KARTE
 * ============================================================
 */

@Composable
private fun MaintenanceDraftCard(
    item: MaintenanceDraftItem,
    onItemChanged: (MaintenanceDraftItem) -> Unit,
    onChooseImage: () -> Unit,
    onRemoveImage: () -> Unit
) {

    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A1E26)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {

        Column(
            Modifier.padding(12.dp)
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                Checkbox(
                    checked = item.selected,
                    onCheckedChange = { checked ->

                        onItemChanged(
                            item.copy(
                                selected = checked
                            )
                        )
                    }
                )

                Text(
                    item.name,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            if (item.selected) {

                /*
                 * Monat
                 */

                var monthExpanded by remember(item.name) {
                    mutableStateOf(false)
                }

                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        monthExpanded = true
                    }
                ) {

                    Text(
                        if (item.month.isBlank()) {
                            "Monat auswählen"
                        } else {
                            "Monat: ${item.month}"
                        }
                    )
                }

                DropdownMenu(
                    expanded = monthExpanded,
                    onDismissRequest = {
                        monthExpanded = false
                    }
                ) {

                    maintenanceMonths.forEach { month ->

                        DropdownMenuItem(
                            text = {
                                Text(month)
                            },
                            onClick = {

                                onItemChanged(
                                    item.copy(
                                        month = month
                                    )
                                )

                                monthExpanded = false
                            }
                        )
                    }
                }

                /*
                 * Jahr
                 */

                OutlinedTextField(
                    value = item.year,
                    onValueChange = {
                        onItemChanged(
                            item.copy(
                                year = it.filter { character ->
                                    character.isDigit()
                                }
                            )
                        )
                    },
                    label = {
                        Text("Jahr")
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                /*
                 * Kilometerstand
                 */

                OutlinedTextField(
                    value = item.mileage,
                    onValueChange = {
                        onItemChanged(
                            item.copy(
                                mileage = it.filter { character ->
                                    character.isDigit()
                                }
                            )
                        )
                    },
                    label = {
                        Text("Kilometerstand optional")
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                /*
                 * Verwendet
                 */

                OutlinedTextField(
                    value = item.used,
                    onValueChange = {
                        onItemChanged(
                            item.copy(used = it)
                        )
                    },
                    label = {
                        Text("Verwendet / Material")
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                /*
                 * Teilenummer
                 */

                OutlinedTextField(
                    value = item.partNumber,
                    onValueChange = {
                        onItemChanged(
                            item.copy(partNumber = it)
                        )
                    },
                    label = {
                        Text("Teilenummer optional")
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                /*
                 * Materialkosten
                 */

                OutlinedTextField(
                    value = item.materialCost,
                    onValueChange = {
                        onItemChanged(
                            item.copy(materialCost = it)
                        )
                    },
                    label = {
                        Text("Materialkosten €")
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                /*
                 * Arbeitskosten
                 */

                OutlinedTextField(
                    value = item.laborCost,
                    onValueChange = {
                        onItemChanged(
                            item.copy(laborCost = it)
                        )
                    },
                    label = {
                        Text("Arbeitskosten €")
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                /*
                 * Bild
                 */

                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onChooseImage
                ) {

                    Text(
                        if (item.imageUri.isBlank()) {
                            "Bild hinzufügen"
                        } else {
                            "Bild ändern"
                        }
                    )
                }

                if (item.imageUri.isNotBlank()) {

                    MaintenanceImage(
                        uri = item.imageUri,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                    )

                    OutlinedButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = onRemoveImage
                    ) {
                        Text("Bild entfernen")
                    }
                }
            }
        }
    }
}

/*
 * ============================================================
 * HISTORIEN-KARTE
 * ============================================================
 */

@Composable
private fun MaintenanceHistoryCard(
    item: MaintenanceHistoryItem,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {

    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF11141A)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {

        Column(
            Modifier.padding(16.dp)
        ) {

            Text(
                item.name,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(4.dp))

            if (item.month.isNotBlank() && item.year.isNotBlank()) {

                Text(
                    "%02d/%s".format(
                        Locale.GERMANY,
                        item.month.toIntOrNull() ?: 0,
                        item.year
                    ),
                    color = Color(0xFFD4AF37)
                )
            }

            if (item.mileage.isNotBlank()) {
                Text(
                    "${item.mileage} km",
                    color = Color(0xFFB8BEC8)
                )
            }

            if (item.used.isNotBlank()) {
                Text(
                    "Verwendet: ${item.used}",
                    color = Color.White
                )
            }

            if (item.partNumber.isNotBlank()) {
                Text(
                    "Teilenummer: ${item.partNumber}",
                    color = Color(0xFFB8BEC8)
                )
            }

            if (item.materialCost > 0) {
                Text(
                    "Material: ${money(item.materialCost)}",
                    color = Color.White
                )
            }

            if (item.laborCost > 0) {
                Text(
                    "Arbeitskosten: ${money(item.laborCost)}",
                    color = Color.White
                )
            }

            Text(
                "Gesamtkosten: ${money(item.totalCost)}",
                color = Color(0xFFD4AF37),
                fontWeight = FontWeight.Bold
            )

            if (item.workshop.isNotBlank()) {
                Text(
                    "Werkstatt: ${item.workshop}",
                    color = Color(0xFFB8BEC8)
                )
            }

            if (item.notes.isNotBlank()) {
                Text(
                    "Notiz: ${item.notes}",
                    color = Color(0xFFB8BEC8)
                )
            }

            if (item.imageUri.isNotBlank()) {

                Spacer(Modifier.height(8.dp))

                MaintenanceImage(
                    uri = item.imageUri,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                )
            }

            Spacer(Modifier.height(10.dp))

            var menuExpanded by remember(item.id) {
                mutableStateOf(false)
            }

            androidx.compose.foundation.layout.Box(
                modifier = Modifier.fillMaxWidth()
            ) {

                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        menuExpanded = true
                    }
                ) {

                    Text(
                        "☰  Bearbeiten / Löschen",
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = {
                        menuExpanded = false
                    }
                ) {

                    DropdownMenuItem(
                        text = {
                            Text("Bearbeiten")
                        },
                        onClick = {
                            menuExpanded = false
                            onEdit()
                        }
                    )

                    DropdownMenuItem(
                        text = {
                            Text("Löschen")
                        },
                        onClick = {
                            menuExpanded = false
                            onDelete()
                        }
                    )
                }
            }
        }
    }
}

/*
 * ============================================================
 * BILD-ANZEIGE
 * ============================================================
 */

@Composable
private fun MaintenanceImage(
    uri: String,
    modifier: Modifier
) {

    val context =
        LocalContext.current

    val bitmap =
        remember(uri) {
            loadBitmap(
                context,
                uri
            )
        }

    if (bitmap != null) {

        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "Wartungsbild",
            modifier = modifier
        )
    }
}
