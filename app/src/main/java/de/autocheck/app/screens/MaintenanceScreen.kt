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
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

private val maintenanceDisplayFormatter =
    DateTimeFormatter.ofPattern(
        "dd.MM.yyyy"
    )

private val maintenanceIsoFormatter =
    DateTimeFormatter.ofPattern(
        "yyyy-MM-dd"
    )

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

private const val MAINTENANCE_ITEMS_PREFIX =
    "[WARTUNGEN]"

private const val MAINTENANCE_ITEMS_SEPARATOR =
    "||"

private fun normalizeMaintenanceDate(
    value: String
): String {

    val trimmed =
        value.trim()

    if (
        trimmed.isBlank()
    ) {
        return ""
    }

    return try {

        LocalDate
            .parse(
                trimmed,
                maintenanceDisplayFormatter
            )
            .format(
                maintenanceDisplayFormatter
            )

    } catch (
        _: DateTimeParseException
    ) {

        try {

            LocalDate
                .parse(
                    trimmed,
                    maintenanceIsoFormatter
                )
                .format(
                    maintenanceDisplayFormatter
                )

        } catch (
            _: DateTimeParseException
        ) {

            trimmed
        }
    }
}

private fun encodeMaintenanceNotes(
    selectedItems: List<String>,
    customItem: String,
    notes: String
): String {

    val allItems =
        buildList {

            addAll(
                selectedItems
            )

            if (
                customItem.isNotBlank()
            ) {

                add(
                    customItem.trim()
                )
            }
        }

    val itemText =
        allItems.joinToString(
            MAINTENANCE_ITEMS_SEPARATOR
        )

    return buildString {

        append(
            MAINTENANCE_ITEMS_PREFIX
        )

        append(
            itemText
        )

        append(
            "\n"
        )

        append(
            notes.trim()
        )
    }
}

private fun decodeMaintenanceItems(
    storedNotes: String
): Pair<List<String>, String> {

    if (
        !storedNotes.startsWith(
            MAINTENANCE_ITEMS_PREFIX
        )
    ) {

        return Pair(
            emptyList(),
            storedNotes
        )
    }

    val rest =
        storedNotes
            .removePrefix(
                MAINTENANCE_ITEMS_PREFIX
            )

    val lines =
        rest.split(
            "\n",
            limit = 2
        )

    val itemPart =
        lines
            .firstOrNull()
            ?.trim()
            ?: ""

    val notes =
        if (
            lines.size > 1
        ) {

            lines[1]
                .trim()

        } else {

            ""
        }

    val items =
        if (
            itemPart.isBlank()
        ) {

            emptyList()

        } else {

            itemPart
                .split(
                    MAINTENANCE_ITEMS_SEPARATOR
                )
                .map {
                    it.trim()
                }
                .filter {
                    it.isNotBlank()
                }
        }

    return Pair(
        items,
        notes
    )
}

private fun isCustomMaintenanceItem(
    item: String
): Boolean {

    return !standardMaintenanceItems.contains(
        item
    )
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

    var date by remember {

        mutableStateOf(
            ""
        )
    }

    var mileage by remember {

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

    var notes by remember {

        mutableStateOf(
            ""
        )
    }

    var selectedItems by remember {

        mutableStateOf(
            emptyList<String>()
        )
    }

    var customItem by remember {

        mutableStateOf(
            ""
        )
    }

    var entries by remember {

        mutableStateOf(
            store.loadMaintenance()
        )
    }

    val list =
        entries.filter {

            it.vehicle ==
                activeVehicle
        }

    fun resetForm() {

        editingId =
            null

        date =
            ""

        mileage =
            ""

        cost =
            ""

        workshop =
            ""

        notes =
            ""

        selectedItems =
            emptyList()

        customItem =
            ""

        showForm =
            false
    }

    fun editEntry(
        entry: Maintenance
    ) {

        val decoded =
            decodeMaintenanceItems(
                entry.notes
            )

        selectedItems =
            decoded.first.filter {
                standardMaintenanceItems.contains(
                    it
                )
            }

        val customItems =
            decoded.first.filter {
                isCustomMaintenanceItem(
                    it
                )
            }

        customItem =
            customItems.joinToString(
                ", "
            )

        date =
            normalizeMaintenanceDate(
                entry.date
            )

        mileage =
            entry.mileage

        cost =
            entry.cost

        workshop =
            entry.workshop

        notes =
            decoded.second

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
                Modifier
                    .weight(
                        1f
                    ),

            verticalArrangement =
                Arrangement.spacedBy(
                    10.dp
                )
        ) {

            if (
                list.isEmpty() &&
                !showForm
            ) {

                item {

                    EmptyCard(
                        "Noch keine Wartung gespeichert."
                    )
                }
            }

            if (
                showForm
            ) {

                item {

                    MaintenanceFormCard(

                        selectedItems =
                            selectedItems,

                        onSelectedItemsChanged = {
                            selectedItems =
                                it
                        },

                        customItem =
                            customItem,

                        onCustomItemChanged = {
                            customItem =
                                it
                        },

                        date =
                            date,

                        onDateChanged = {
                            date =
                                it
                        },

                        mileage =
                            mileage,

                        onMileageChanged = {
                            mileage =
                                it
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

                            val normalizedDate =
                                normalizeMaintenanceDate(
                                    date
                                )

                            val encodedNotes =
                                encodeMaintenanceNotes(
                                    selectedItems =
                                        selectedItems,
                                    customItem =
                                        customItem,
                                    notes =
                                        notes
                                )

                            val newEntry =
                                Maintenance(

                                    id =
                                        editingId
                                            ?: System
                                                .currentTimeMillis(),

                                    vehicle =
                                        activeVehicle,

                                    date =
                                        normalizedDate,

                                    mileage =
                                        mileage.trim(),

                                    cost =
                                        cost.trim(),

                                    workshop =
                                        workshop.trim(),

                                    notes =
                                        encodedNotes
                                )

                            val updated =
                                if (
                                    editingId != null
                                ) {

                                    entries.map {

                                        if (
                                            it.id ==
                                                editingId
                                        ) {

                                            newEntry

                                        } else {

                                            it
                                        }
                                    }

                                } else {

                                    entries +
                                        newEntry
                                }

                            entries =
                                updated

                            store.saveMaintenance(
                                updated
                            )

                            resetForm()
                        },

                        onCancel = {

                            resetForm()
                        }
                    )
                }
            }

            if (
                !showForm
            ) {

                items(
                    list
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

                            val updated =
                                entries.filterNot {

                                    it.id ==
                                        entry.id
                                }

                            entries =
                                updated

                            store.saveMaintenance(
                                updated
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
                            top =
                                8.dp
                        ),

                onClick = {

                    editingId =
                        null

                    date =
                        ""

                    mileage =
                        ""

                    cost =
                        ""

                    workshop =
                        ""

                    notes =
                        ""

                    selectedItems =
                        emptyList()

                    customItem =
                        ""

                    showForm =
                        true
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
    selectedItems: List<String>,
    onSelectedItemsChanged:
        (List<String>) -> Unit,
    customItem: String,
    onCustomItemChanged:
        (String) -> Unit,
    date: String,
    onDateChanged:
        (String) -> Unit,
    mileage: String,
    onMileageChanged:
        (String) -> Unit,
    cost: String,
    onCostChanged:
        (String) -> Unit,
    workshop: String,
    onWorkshopChanged:
        (String) -> Unit,
    notes: String,
    onNotesChanged:
        (String) -> Unit,
    isEditing: Boolean,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {

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
                text =
                    if (
                        isEditing
                    ) {

                        "Wartung ändern"

                    } else {

                        "Neue Wartung"
                    }
            )

            Text(
                text =
                    "Wartungsarbeiten"
            )

            standardMaintenanceItems.forEach {
                item ->

                MaintenanceCheckRow(

                    label =
                        item,

                    checked =
                        selectedItems.contains(
                            item
                        ),

                    onCheckedChange = {

                        val updated =
                            if (
                                it
                            ) {

                                selectedItems +
                                    item

                            } else {

                                selectedItems.filterNot {

                                    selected ->
                                    selected ==
                                        item
                                }
                            }

                        onSelectedItemsChanged(
                            updated
                        )
                    }
                )
            }

            OutlinedTextField(

                value =
                    customItem,

                onValueChange =
                    onCustomItemChanged,

                modifier =
                    Modifier.fillMaxWidth(),

                label = {
                    Text(
                        "Eigene Wartung"
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

            OutlinedTextField(

                value =
                    date,

                onValueChange =
                    onDateChanged,

                modifier =
                    Modifier.fillMaxWidth(),

                label = {
                    Text(
                        "Datum"
                    )
                },

                placeholder = {
                    Text(
                        "TT.MM.JJJJ"
                    )
                },

                singleLine =
                    true
            )

            OutlinedTextField(

                value =
                    mileage,

                onValueChange =
                    onMileageChanged,

                modifier =
                    Modifier.fillMaxWidth(),

                label = {
                    Text(
                        "Kilometerstand"
                    )
                },

                singleLine =
                    true
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

            Row(

                modifier =
                    Modifier.fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.spacedBy(
                        10.dp
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

                    onClick =
                        onSave

                ) {

                    Text(
                        if (
                            isEditing
                        ) {

                            "Änderung speichern"

                        } else {

                            "Wartung speichern"
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun MaintenanceCheckRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {

    Row(

        modifier =
            Modifier.fillMaxWidth(),

        verticalAlignment =
            Alignment.CenterVertically
    ) {

        Checkbox(

            checked =
                checked,

            onCheckedChange =
                onCheckedChange
        )

        Text(
            text =
                label
        )
    }
}

@Composable
private fun MaintenanceSummaryCard(
    entry: Maintenance,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {

    val decoded =
        decodeMaintenanceItems(
            entry.notes
        )

    val selectedMaintenanceItems =
        decoded.first

    val storedNotes =
        decoded.second

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
                text =
                    "Gespeichert"
            )

            Text(
                text =
                    "Fahrzeug: ${entry.vehicle}"
            )

            if (
                selectedMaintenanceItems.isNotEmpty()
            ) {

                Text(
                    text =
                        "Wartungsarbeiten:"
                )

                selectedMaintenanceItems.forEach {
                    item ->

                    Text(
                        text =
                            "✓ $item"
                    )
                }
            }

            if (
                entry.date.isNotBlank()
            ) {

                Text(
                    text =
                        "Datum: ${normalizeMaintenanceDate(entry.date)}"
                )
            }

            if (
                entry.mileage.isNotBlank()
            ) {

                Text(
                    text =
                        "Kilometerstand: ${entry.mileage}"
                )
            }

            if (
                entry.cost.isNotBlank()
            ) {

                Text(
                    text =
                        "Kosten: ${entry.cost}"
                )
            }

            if (
                entry.workshop.isNotBlank()
            ) {

                Text(
                    text =
                        "Werkstatt: ${entry.workshop}"
                )
            }

            if (
                storedNotes.isNotBlank()
            ) {

                Text(
                    text =
                        "Notizen: $storedNotes"
                )
            }

            Spacer(
                modifier =
                    Modifier.height(
                        4.dp
                    )
            )

            Row(

                modifier =
                    Modifier.fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.spacedBy(
                        10.dp
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
