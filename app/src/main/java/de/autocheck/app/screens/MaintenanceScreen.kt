package de.autocheck.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.json.JSONArray
import org.json.JSONObject

private const val MAINTENANCE_V2_PREFIX =
    "[WARTUNGSPLAN_V2]"

private const val OLD_MAINTENANCE_PREFIX =
    "[WARTUNGEN]"

private const val OLD_MAINTENANCE_SEPARATOR =
    "||"

private val standardMaintenanceItems =
    listOf(
        "ZAHNRIEMEN MIT WASSERPUMPE",
        "ÖL",
        "ÖLFILTER",
        "LUFTFILTER",
        "KRAFTSTOFFFILTER",
        "INNENRAUMFILTER",
        "BATTERIE"
    )

private val maintenanceMonths =
    (1..12).map {
        it.toString()
    }

private data class MaintenanceDraftItem(
    val name: String,
    val selected: Boolean = false,
    val month: String = "",
    val year: String = "",
    val mileage: String = "",
    val custom: Boolean = false
)

private data class DecodedMaintenance(
    val items: List<MaintenanceDraftItem>,
    val notes: String
)

private fun encodeMaintenanceNotes(
    items: List<MaintenanceDraftItem>,
    notes: String
): String {

    val array =
        JSONArray()

    items
        .filter {
            it.selected &&
                it.name.isNotBlank()
        }
        .forEach { item ->

            val json =
                JSONObject()

            json.put(
                "name",
                item.name.trim()
            )

            json.put(
                "month",
                item.month.trim()
            )

            json.put(
                "year",
                item.year.trim()
            )

            json.put(
                "mileage",
                item.mileage.trim()
            )

            json.put(
                "custom",
                item.custom
            )

            array.put(
                json
            )
        }

    return buildString {

        append(
            MAINTENANCE_V2_PREFIX
        )

        append(
            array.toString()
        )

        append(
            "\n"
        )

        append(
            notes.trim()
        )
    }
}

private fun decodeMaintenanceNotes(
    storedNotes: String,
    legacyDate: String,
    legacyMileage: String
): DecodedMaintenance {

    if (
        storedNotes.startsWith(
            MAINTENANCE_V2_PREFIX
        )
    ) {

        val rest =
            storedNotes.removePrefix(
                MAINTENANCE_V2_PREFIX
            )

        val parts =
            rest.split(
                "\n",
                limit = 2
            )

        val jsonText =
            parts
                .firstOrNull()
                ?.trim()
                ?: ""

        val notes =
            if (
                parts.size > 1
            ) {
                parts[1].trim()
            } else {
                ""
            }

        val result =
            mutableListOf<MaintenanceDraftItem>()

        try {

            val array =
                JSONArray(
                    jsonText
                )

            for (
                index in
                0 until array.length()
            ) {

                val json =
                    array.getJSONObject(
                        index
                    )

                val name =
                    json.optString(
                        "name",
                        ""
                    )

                if (
                    name.isBlank()
                ) {
                    continue
                }

                result.add(
                    MaintenanceDraftItem(
                        name =
                            name,
                        selected =
                            true,
                        month =
                            json.optString(
                                "month",
                                ""
                            ),
                        year =
                            json.optString(
                                "year",
                                ""
                            ),
                        mileage =
                            json.optString(
                                "mileage",
                                ""
                            ),
                        custom =
                            json.optBoolean(
                                "custom",
                                false
                            )
                    )
                )
            }

        } catch (
            _: Exception
        ) {

            return DecodedMaintenance(
                items =
                    emptyList(),
                notes =
                    notes
            )
        }

        return DecodedMaintenance(
            items =
                result,
            notes =
                notes
        )
    }

    if (
        storedNotes.startsWith(
            OLD_MAINTENANCE_PREFIX
        )
    ) {

        val rest =
            storedNotes.removePrefix(
                OLD_MAINTENANCE_PREFIX
            )

        val parts =
            rest.split(
                "\n",
                limit = 2
            )

        val itemPart =
            parts
                .firstOrNull()
                ?.trim()
                ?: ""

        val notes =
            if (
                parts.size > 1
            ) {
                parts[1].trim()
            } else {
                ""
            }

        val oldItems =
            if (
                itemPart.isBlank()
            ) {
                emptyList()
            } else {
                itemPart
                    .split(
                        OLD_MAINTENANCE_SEPARATOR
                    )
                    .map {
                        it.trim()
                    }
                    .filter {
                        it.isNotBlank()
                    }
            }

        val result =
            oldItems.map { itemName ->

                MaintenanceDraftItem(
                    name =
                        itemName,
                    selected =
                        true,
                    month =
                        legacyMonth(
                            legacyDate
                        ),
                    year =
                        legacyYear(
                            legacyDate
                        ),
                    mileage =
                        legacyMileage,
                    custom =
                        !standardMaintenanceItems.contains(
                            itemName
                        )
                )
            }

        return DecodedMaintenance(
            items =
                result,
            notes =
                notes
        )
    }

    if (
        storedNotes.isNotBlank()
    ) {

        return DecodedMaintenance(
            items =
                listOf(
                    MaintenanceDraftItem(
                        name =
                            "Wartung",
                        selected =
                            true,
                        month =
                            legacyMonth(
                                legacyDate
                            ),
                        year =
                            legacyYear(
                                legacyDate
                            ),
                        mileage =
                            legacyMileage,
                        custom =
                            true
                    )
                ),
            notes =
                storedNotes.trim()
        )
    }

    if (
        legacyDate.isNotBlank() ||
        legacyMileage.isNotBlank()
    ) {

        return DecodedMaintenance(
            items =
                listOf(
                    MaintenanceDraftItem(
                        name =
                            "Wartung",
                        selected =
                            true,
                        month =
                            legacyMonth(
                                legacyDate
                            ),
                        year =
                            legacyYear(
                                legacyDate
                            ),
                        mileage =
                            legacyMileage,
                        custom =
                            true
                    )
                ),
            notes =
                ""
        )
    }

    return DecodedMaintenance(
        items =
            emptyList(),
        notes =
            ""
    )
}

private fun legacyMonth(
    date: String
): String {

    val value =
        date.trim()

    if (
        value.isBlank()
    ) {
        return ""
    }

    val parts =
        when {
            value.contains(".") ->
                value.split(".")

            value.contains("-") ->
                value.split("-")

            else ->
                emptyList()
        }

    if (
        parts.size != 3
    ) {
        return ""
    }

    return when {

        parts[0].length == 4 ->
            parts[1]
                .toIntOrNull()
                ?.toString()
                ?: ""

        parts[2].length == 4 ->
            parts[1]
                .toIntOrNull()
                ?.toString()
                ?: ""

        else ->
            ""
    }
}

private fun legacyYear(
    date: String
): String {

    val value =
        date.trim()

    if (
        value.isBlank()
    ) {
        return ""
    }

    val parts =
        when {
            value.contains(".") ->
                value.split(".")

            value.contains("-") ->
                value.split("-")

            else ->
                emptyList()
        }

    if (
        parts.size != 3
    ) {
        return ""
    }

    return when {

        parts[0].length == 4 ->
            parts[0]

        parts[2].length == 4 ->
            parts[2]

        else ->
            ""
    }
}

@Composable
fun MaintenanceScreen(
    store: VehicleStore,
    vehicles: List<Vehicle>,
    activeVehicle: String,
    onActiveVehicle: (String) -> Unit,
    onVisited: () -> Unit
) {

    LaunchedEffect(
        Unit
    ) {
        onVisited()
    }

    var showForm by remember {
        mutableStateOf(
            false
        )
    }

    var editingId by remember {
        mutableStateOf<Long?>(
            null
        )
    }

    var draftItems by remember {
        mutableStateOf(
            standardMaintenanceItems.map {
                MaintenanceDraftItem(
                    name =
                        it,
                    selected =
                        false,
                    custom =
                        false
                )
            }
        )
    }

    var customItemName by remember {
        mutableStateOf(
            ""
        )
    }

    var notes by remember {
        mutableStateOf(
            ""
        )
    }

    var cost by remember {
        mutableStateOf(
            ""
        )
    }

    var workshop by remember {
        mutableStateOf(
            ""
        )
    }

    var entries by remember {
        mutableStateOf(
            store.loadMaintenance()
        )
    }

    val vehicleEntries =
        entries.filter {
            it.vehicle ==
                activeVehicle
        }

    fun resetForm() {

        editingId =
            null

        draftItems =
            standardMaintenanceItems.map {
                MaintenanceDraftItem(
                    name =
                        it,
                    selected =
                        false,
                    custom =
                        false
                )
            }

        customItemName =
            ""

        notes =
            ""

        cost =
            ""

        workshop =
            ""

        showForm =
            false
    }

    fun startNewEntry() {

        editingId =
            null

        draftItems =
            standardMaintenanceItems.map {
                MaintenanceDraftItem(
                    name =
                        it,
                    selected =
                        false,
                    custom =
                        false
                )
            }

        customItemName =
            ""

        notes =
            ""

        cost =
            ""

        workshop =
            ""

        showForm =
            true
    }

    fun editEntry(
        entry: Maintenance
    ) {

        val decoded =
            decodeMaintenanceNotes(
                storedNotes =
                    entry.notes,
                legacyDate =
                    entry.date,
                legacyMileage =
                    entry.mileage
            )

        val savedStandard =
            decoded.items.filter {
                standardMaintenanceItems.contains(
                    it.name
                )
            }

        val customItems =
            decoded.items.filter {
                !standardMaintenanceItems.contains(
                    it.name
                )
            }

        val rebuiltStandard =
            standardMaintenanceItems.map {
                standardName ->

                savedStandard.firstOrNull {
                    it.name ==
                        standardName
                }
                    ?: MaintenanceDraftItem(
                        name =
                            standardName,
                        selected =
                            false,
                        custom =
                            false
                    )
            }

        draftItems =
            rebuiltStandard +
                customItems

        customItemName =
            ""

        notes =
            decoded.notes

        cost =
            entry.cost

        workshop =
            entry.workshop

        editingId =
            entry.id

        showForm =
            true
    }

    Column(
        modifier =
            Modifier.fillMaxSize()
    ) {

        VehicleSelector(
            vehicles =
                vehicles,
            activeVehicle =
                activeVehicle,
            onSelected =
                onActiveVehicle
        )

        Spacer(
            modifier =
                Modifier.height(
                    10.dp
                )
        )

        if (
            vehicles.isEmpty()
        ) {

            return@Column
        }

        LazyColumn(
            modifier =
                Modifier.weight(
                    1f
                ),

            verticalArrangement =
                Arrangement.spacedBy(
                    10.dp
                )
        ) {

            if (
                vehicleEntries.isEmpty() &&
                !showForm
            ) {

                item {

                    EmptyMaintenanceCard()
                }
            }

            if (
                showForm
            ) {

                item {

                    MaintenanceFormCard(
                        draftItems =
                            draftItems,

                        onDraftItemsChanged = {
                            draftItems =
                                it
                        },

                        customItemName =
                            customItemName,

                        onCustomItemNameChanged = {
                            customItemName =
                                it
                        },

                        onAddCustomItem = {

                            val name =
                                customItemName.trim()

                            if (
                                name.isNotBlank()
                            ) {

                                val alreadyExists =
                                    draftItems.any {
                                        it.name.equals(
                                            name,
                                            ignoreCase =
                                                true
                                        )
                                    }

                                if (
                                    !alreadyExists
                                ) {

                                    draftItems =
                                        draftItems +
                                            MaintenanceDraftItem(
                                                name =
                                                    name,
                                                selected =
                                                    true,
                                                custom =
                                                    true
                                            )
                                }

                                customItemName =
                                    ""
                            }
                        },

                        cost =
                            cost,

                        onCostChanged = {
                            cost =
                                it
                        },

                        workshop =
                            workshop,

                        onWorkshopChanged = {
                            workshop =
                                it
                        },

                        notes =
                            notes,

                        onNotesChanged = {
                            notes =
                                it
                        },

                        isEditing =
                            editingId != null,

                        onSave = {

                            val selected =
                                draftItems.filter {
                                    it.selected &&
                                        it.name.isNotBlank()
                                }

                            if (
                                selected.isNotEmpty()
                            ) {

                                val newEntry =
                                    Maintenance(
                                        id =
                                            editingId
                                                ?: System
                                                    .currentTimeMillis(),

                                        vehicle =
                                            activeVehicle,

                                        date =
                                            "",

                                        mileage =
                                            "",

                                        cost =
                                            cost.trim(),

                                        workshop =
                                            workshop.trim(),

                                        notes =
                                            encodeMaintenanceNotes(
                                                items =
                                                    selected,
                                                notes =
                                                    notes
                                            )
                                    )

                                val updatedEntries =
                                    if (
                                        editingId != null
                                    ) {

                                        entries.map {
                                            current ->

                                            if (
                                                current.id ==
                                                    editingId
                                            ) {
                                                newEntry
                                            } else {
                                                current
                                            }
                                        }

                                    } else {

                                        entries +
                                            newEntry
                                    }

                                entries =
                                    updatedEntries

                                store.saveMaintenance(
                                    updatedEntries
                                )

                                resetForm()
                            }
                        },

                        onCancel = {
                            resetForm()
                        },

                        onRemoveCustomItem = {
                            itemToRemove ->

                            draftItems =
                                draftItems.filterNot {
                                    it.name ==
                                        itemToRemove.name &&
                                        it.custom
                                }
                        }
                    )
                }

            } else {

                items(
                    vehicleEntries
                ) { entry ->

                    MaintenanceSummaryCard(
                        entry =
                            entry,

                        onEdit = {
                            editEntry(
                                entry
                            )
                        },

                        onDelete = {

                            val updatedEntries =
                                entries.filterNot {
                                    it.id ==
                                        entry.id
                                }

                            entries =
                                updatedEntries

                            store.saveMaintenance(
                                updatedEntries
                            )
                        }
                    )
                }
            }
        }

        if (
            !showForm
        ) {

            Button(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(
                            top = 8.dp
                        ),

                onClick = {
                    startNewEntry()
                }
            ) {

                Text(
                    "Wartung hinzufügen"
                )
            }
        }
    }
}

@Composable
private fun MaintenanceFormCard(
    draftItems:
        List<MaintenanceDraftItem>,

    onDraftItemsChanged:
        (List<MaintenanceDraftItem>) -> Unit,

    customItemName:
        String,

    onCustomItemNameChanged:
        (String) -> Unit,

    onAddCustomItem:
        () -> Unit,

    cost:
        String,

    onCostChanged:
        (String) -> Unit,

    workshop:
        String,

    onWorkshopChanged:
        (String) -> Unit,

    notes:
        String,

    onNotesChanged:
        (String) -> Unit,

    isEditing:
        Boolean,

    onSave:
        () -> Unit,

    onCancel:
        () -> Unit,

    onRemoveCustomItem:
        (MaintenanceDraftItem) -> Unit
) {

    val selectedCount =
        draftItems.count {
            it.selected
        }

    Card(
        modifier =
            Modifier.fillMaxWidth(),

        colors =
            CardDefaults.cardColors(
                containerColor =
                    Color(
                        0xFF11141A
                    )
            )
    ) {

        Column(
            modifier =
                Modifier.padding(
                    16.dp
                ),

            verticalArrangement =
                Arrangement.spacedBy(
                    10.dp
                )
        ) {

            Text(
                if (
                    isEditing
                ) {
                    "Wartung ändern"
                } else {
                    "Neue Wartung"
                }
            )

            Text(
                "Wartungsarbeiten"
            )

            Text(
                "Jede Wartung bekommt ihren eigenen Monat, ihr eigenes Jahr und optional den Kilometerstand."
            )

            draftItems.forEachIndexed {
                index,
                item ->

                MaintenanceItemEditor(
                    item =
                        item,

                    onItemChanged = {
                        changedItem ->

                        val updated =
                            draftItems.toMutableList()

                        updated[index] =
                            changedItem

                        onDraftItemsChanged(
                            updated
                        )
                    },

                    onRemove =
                        if (
                            item.custom
                        ) {
                            {
                                onRemoveCustomItem(
                                    item
                                )
                            }
                        } else {
                            null
                        }
                )
            }

            Card(
                modifier =
                    Modifier.fillMaxWidth(),

                colors =
                    CardDefaults.cardColors(
                        containerColor =
                            Color(
                                0xFF191D24
                            )
                    )
            ) {

                Column(
                    modifier =
                        Modifier.padding(
                            12.dp
                        ),

                    verticalArrangement =
                        Arrangement.spacedBy(
                            8.dp
                        )
                ) {

                    Text(
                        "Eigene Wartung"
                    )

                    OutlinedTextField(
                        value =
                            customItemName,

                        onValueChange =
                            onCustomItemNameChanged,

                        modifier =
                            Modifier.fillMaxWidth(),

                        label = {
                            Text(
                                "Bezeichnung"
                            )
                        },

                        placeholder = {
                            Text(
                                "z. B. Bremsflüssigkeit"
                            )
                        },

                        singleLine =
                            true
                    )

                    Button(
                        modifier =
                            Modifier.fillMaxWidth(),

                        onClick =
                            onAddCustomItem
                    ) {

                        Text(
                            "Eigene Wartung hinzufügen"
                        )
                    }
                }
            }

            Text(
                "Allgemeine Angaben"
            )

            OutlinedTextField(
                value =
                    cost,

                onValueChange =
                    onCostChanged,

                modifier =
                    Modifier.fillMaxWidth(),

                label = {
                    Text(
                        "Kosten"
                    )
                },

                singleLine =
                    true
            )

            OutlinedTextField(
                value =
                    workshop,

                onValueChange =
                    onWorkshopChanged,

                modifier =
                    Modifier.fillMaxWidth(),

                label = {
                    Text(
                        "Werkstatt"
                    )
                },

                singleLine =
                    true
            )

            OutlinedTextField(
                value =
                    notes,

                onValueChange =
                    onNotesChanged,

                modifier =
                    Modifier.fillMaxWidth(),

                label = {
                    Text(
                        "Notizen"
                    )
                }
            )

            Text(
                "Ausgewählte Wartungen: $selectedCount"
            )

            Row(
                modifier =
                    Modifier.fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.spacedBy(
                        8.dp
                    )
            ) {

                OutlinedButton(
                    modifier =
                        Modifier.weight(
                            1f
                        ),

                    onClick =
                        onCancel
                ) {

                    Text(
                        "Abbrechen"
                    )
                }

                Button(
                    modifier =
                        Modifier.weight(
                            1f
                        ),

                    enabled =
                        selectedCount > 0,

                    onClick =
                        onSave
                ) {

                    Text(
                        "Alles speichern"
                    )
                }
            }
        }
    }
}

@Composable
private fun MaintenanceItemEditor(
    item:
        MaintenanceDraftItem,

    onItemChanged:
        (MaintenanceDraftItem) -> Unit,

    onRemove:
        (() -> Unit)?
) {

    Card(
        modifier =
            Modifier.fillMaxWidth(),

        colors =
            CardDefaults.cardColors(
                containerColor =
                    if (
                        item.selected
                    ) {
                        Color(
                            0xFF191D24
                        )
                    } else {
                        Color(
                            0xFF11141A
                        )
                    }
            )
    ) {

        Column(
            modifier =
                Modifier.padding(
                    10.dp
                ),

            verticalArrangement =
                Arrangement.spacedBy(
                    8.dp
                )
        ) {

            Row(
                modifier =
                    Modifier.fillMaxWidth(),

                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Checkbox(
                    checked =
                        item.selected,

                    onCheckedChange = {
                        checked ->

                        onItemChanged(
                            item.copy(
                                selected =
                                    checked
                            )
                        )
                    }
                )

                Text(
                    modifier =
                        Modifier.weight(
                            1f
                        ),

                    text =
                        item.name
                )

                if (
                    onRemove != null
                ) {

                    OutlinedButton(
                        onClick =
                            onRemove
                    ) {

                        Text(
                            "Entfernen"
                        )
                    }
                }
            }

            if (
                item.selected
            ) {

                Text(
                    "Monat, Jahr und Kilometerstand"
                )

                Row(
                    modifier =
                        Modifier.fillMaxWidth(),

                    horizontalArrangement =
                        Arrangement.spacedBy(
                            8.dp
                        ),

                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    MonthDropdown(
                        modifier =
                            Modifier.weight(
                                1f
                            ),

                        month =
                            item.month,

                        onMonthChanged = {
                            newMonth ->

                            onItemChanged(
                                item.copy(
                                    month =
                                        newMonth
                                )
                            )
                        }
                    )

                    OutlinedTextField(
                        value =
                            item.year,

                        onValueChange = {
                            value ->

                            val filtered =
                                value
                                    .filter {
                                        it.isDigit()
                                    }
                                    .take(
                                        4
                                    )

                            onItemChanged(
                                item.copy(
                                    year =
                                        filtered
                                )
                            )
                        },

                        modifier =
                            Modifier.weight(
                                1f
                            ),

                        label = {
                            Text(
                                "Jahr"
                            )
                        },

                        placeholder = {
                            Text(
                                "2025"
                            )
                        },

                        singleLine =
                            true
                    )
                }

                OutlinedTextField(
                    value =
                        item.mileage,

                    onValueChange = {
                        value ->

                        val filtered =
                            value.filter {
                                it.isDigit()
                            }

                        onItemChanged(
                            item.copy(
                                mileage =
                                    filtered
                            )
                        )
                    },

                    modifier =
                        Modifier.fillMaxWidth(),

                    label = {
                        Text(
                            "Kilometerstand (optional)"
                        )
                    },

                    placeholder = {
                        Text(
                            "225000"
                        )
                    },

                    singleLine =
                        true
                )
            }
        }
    }
}

@Composable
private fun MonthDropdown(
    modifier:
        Modifier,

    month:
        String,

    onMonthChanged:
        (String) -> Unit
) {

    var expanded by remember {
        mutableStateOf(
            false
        )
    }

    Box(
        modifier =
            modifier
    ) {

        OutlinedButton(
            modifier =
                Modifier.fillMaxWidth(),

            onClick = {
                expanded =
                    true
            }
        ) {

            Text(
                if (
                    month.isBlank()
                ) {
                    "Monat"
                } else {
                    month
                }
            )
        }

        DropdownMenu(
            expanded =
                expanded,

            onDismissRequest = {
                expanded =
                    false
            }
        ) {

            maintenanceMonths.forEach {
                option ->

                DropdownMenuItem(
                    text = {
                        Text(
                            option
                        )
                    },

                    onClick = {

                        onMonthChanged(
                            option
                        )

                        expanded =
                            false
                    }
                )
            }
        }
    }
}

@Composable
private fun MaintenanceSummaryCard(
    entry:
        Maintenance,

    onEdit:
        () -> Unit,

    onDelete:
        () -> Unit
) {

    val decoded =
        decodeMaintenanceNotes(
            storedNotes =
                entry.notes,

            legacyDate =
                entry.date,

            legacyMileage =
                entry.mileage
        )

    Card(
        modifier =
            Modifier.fillMaxWidth(),

        colors =
            CardDefaults.cardColors(
                containerColor =
                    Color(
                        0xFF11141A
                    )
            )
    ) {

        Column(
            modifier =
                Modifier.padding(
                    16.dp
                ),

            verticalArrangement =
                Arrangement.spacedBy(
                    8.dp
                )
        ) {

            Text(
                "Gesamtbild"
            )

            Text(
                "Fahrzeug: ${entry.vehicle}"
            )

            if (
                decoded.items.isNotEmpty()
            ) {

                decoded.items
                    .filter {
                        it.selected
                    }
                    .forEach { item ->

                        MaintenanceSummaryRow(
                            item =
                                item
                        )
                    }

            } else {

                Text(
                    "Keine einzelnen Wartungspunkte eingetragen."
                )
            }

            if (
                entry.cost.isNotBlank()
            ) {

                Text(
                    "Kosten: ${entry.cost}"
                )
            }

            if (
                entry.workshop.isNotBlank()
            ) {

                Text(
                    "Werkstatt: ${entry.workshop}"
                )
            }

            if (
                decoded.notes.isNotBlank()
            ) {

                Text(
                    "Notizen: ${decoded.notes}"
                )
            }

            Row(
                modifier =
                    Modifier.fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.spacedBy(
                        8.dp
                    )
            ) {

                OutlinedButton(
                    modifier =
                        Modifier.weight(
                            1f
                        ),

                    onClick =
                        onEdit
                ) {

                    Text(
                        "Ändern"
                    )
                }

                OutlinedButton(
                    modifier =
                        Modifier.weight(
                            1f
                        ),

                    onClick =
                        onDelete
                ) {

                    Text(
                        "Löschen"
                    )
                }
            }
        }
    }
}

@Composable
private fun MaintenanceSummaryRow(
    item:
        MaintenanceDraftItem
) {

    val dateText =
        when {

            item.month.isNotBlank() &&
                item.year.isNotBlank() ->
                "${item.month}/${item.year}"

            item.year.isNotBlank() ->
                item.year

            item.month.isNotBlank() ->
                "Monat ${item.month}"

            else ->
                "Datum nicht eingetragen"
        }

    val mileageText =
        if (
            item.mileage.isNotBlank()
        ) {
            " · ${item.mileage} km"
        } else {
            ""
        }

    Text(
        "• ${item.name}: $dateText$mileageText"
    )
}

@Composable
private fun EmptyMaintenanceCard() {

    Card(
        modifier =
            Modifier.fillMaxWidth(),

        colors =
            CardDefaults.cardColors(
                containerColor =
                    Color(
                        0xFF11141A
                    )
            )
    ) {

        Column(
            modifier =
                Modifier.padding(
                    20.dp
                )
        ) {

            Text(
                "Noch keine Wartung gespeichert."
            )

            Spacer(
                modifier =
                    Modifier.height(
                        6.dp
                    )
            )

            Text(
                "Über „Wartung hinzufügen“ kannst du die erste Wartung erfassen."
            )
        }
    }
}
