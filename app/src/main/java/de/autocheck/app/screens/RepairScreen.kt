package de.autocheck.app

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private fun repairDisplayDate(value: String): String {

    val trimmed = value.trim()

    if (trimmed.isBlank()) {
        return ""
    }

    // ISO: yyyy-MM-dd -> dd.MM.yyyy
    if (
        trimmed.matches(
            Regex("""\d{4}-\d{2}-\d{2}""")
        )
    ) {

        val parts = trimmed.split("-")

        if (parts.size == 3) {

            return "${parts[2]}.${parts[1]}.${parts[0]}"
        }
    }

    // Amerikanisch: MM/dd/yyyy -> dd.MM.yyyy
    if (
        trimmed.matches(
            Regex("""\d{1,2}/\d{1,2}/\d{4}""")
        )
    ) {

        val parts = trimmed.split("/")

        if (parts.size == 3) {

            val month =
                parts[0].padStart(2, '0')

            val day =
                parts[1].padStart(2, '0')

            val year =
                parts[2]

            return "$day.$month.$year"
        }
    }

    return trimmed
}

private fun repairInputDate(value: String): String {

    return repairDisplayDate(value)
}

@Composable
fun RepairScreen(
    store: VehicleStore,
    vehicles: List<Vehicle>,
    activeVehicle: String,
    onActiveVehicle: (String) -> Unit,
    onVisited: () -> Unit
) {

    LaunchedEffect(Unit) {

        onVisited()
    }

    var showForm by remember {

        mutableStateOf(false)
    }

    var editingId by remember {

        mutableStateOf<Long?>(null)
    }

    var date by remember {

        mutableStateOf("")
    }

    var mileage by remember {

        mutableStateOf("")
    }

    var description by remember {

        mutableStateOf("")
    }

    var cost by remember {

        mutableStateOf("")
    }

    var workshop by remember {

        mutableStateOf("")
    }

    var repairs by remember {

        mutableStateOf(
            store.loadRepairs()
        )
    }

    val list =
        repairs.filter {

            it.vehicle == activeVehicle
        }

    fun resetForm() {

        editingId = null

        date = ""

        mileage = ""

        description = ""

        cost = ""

        workshop = ""

        showForm = false
    }

    fun startNewRepair() {

        editingId = null

        date = ""

        mileage = ""

        description = ""

        cost = ""

        workshop = ""

        showForm = true
    }

    fun startEdit(
        repair: Repair
    ) {

        editingId =
            repair.id

        date =
            repairInputDate(
                repair.date
            )

        mileage =
            repair.mileage

        description =
            repair.description

        cost =
            repair.cost

        workshop =
            repair.workshop

        showForm = true
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
    ) {

        VehicleSelector(
            vehicles,
            activeVehicle,
            onActiveVehicle
        )

        Spacer(
            modifier =
                Modifier
                    .height(10.dp)
        )

        if (vehicles.isEmpty()) {

            return@Column
        }

        LazyColumn(

            modifier =
                Modifier
                    .weight(1f),

            verticalArrangement =
                Arrangement.spacedBy(
                    10.dp
                )
        ) {

            if (list.isEmpty()) {

                item {

                    EmptyCard(
                        "Noch keine Reparatur eingetragen."
                    )
                }
            }

            items(
                items = list,
                key = {
                    it.id
                }
            ) { repair ->

                RepairRecordCard(

                    repair = repair,

                    onEdit = {

                        startEdit(
                            repair
                        )
                    },

                    onDelete = {

                        repairs =
                            repairs.filterNot {

                                it.id ==
                                    repair.id
                            }

                        store.saveRepairs(
                            repairs
                        )

                        if (
                            editingId ==
                            repair.id
                        ) {

                            resetForm()
                        }
                    }
                )
            }

            if (showForm) {

                item {

                    CardForm {

                        Text(

                            text =
                                if (
                                    editingId !=
                                    null
                                ) {

                                    "Reparatur bearbeiten"

                                } else {

                                    "Neue Reparatur"
                                },

                            color =
                                Color.White,

                            fontSize =
                                20.sp,

                            fontWeight =
                                FontWeight.Bold
                        )

                        Spacer(
                            modifier =
                                Modifier
                                    .height(10.dp)
                        )

                        FormField(

                            "Datum (TT.MM.JJJJ)",

                            date

                        ) {

                            date =
                                it
                        }

                        FormField(

                            "Kilometerstand",

                            mileage

                        ) {

                            mileage =
                                it
                        }

                        FormField(

                            "Beschreibung",

                            description

                        ) {

                            description =
                                it
                        }

                        FormField(

                            "Kosten",

                            cost

                        ) {

                            cost =
                                it
                        }

                        FormField(

                            "Werkstatt",

                            workshop

                        ) {

                            workshop =
                                it
                        }

                        Spacer(
                            modifier =
                                Modifier
                                    .height(10.dp)
                        )

                        Row(

                            modifier =
                                Modifier
                                    .fillMaxWidth(),

                            horizontalArrangement =
                                Arrangement.spacedBy(
                                    10.dp
                                )
                        ) {

                            Button(

                                modifier =
                                    Modifier
                                        .weight(1f),

                                onClick = {

                                    val normalizedDate =
                                        repairDisplayDate(
                                            date
                                        )

                                    val updatedRepairs =

                                        if (
                                            editingId !=
                                            null
                                        ) {

                                            /*
                                             * BEARBEITEN:
                                             * Der vorhandene Eintrag
                                             * wird anhand seiner ID
                                             * ersetzt.
                                             */
                                            repairs.map {

                                                existing ->

                                                if (
                                                    existing.id ==
                                                    editingId
                                                ) {

                                                    existing.copy(

                                                        vehicle =
                                                            activeVehicle,

                                                        date =
                                                            normalizedDate,

                                                        mileage =
                                                            mileage.trim(),

                                                        description =
                                                            description.trim(),

                                                        cost =
                                                            cost.trim(),

                                                        workshop =
                                                            workshop.trim()
                                                    )

                                                } else {

                                                    existing
                                                }
                                            }

                                        } else {

                                            /*
                                             * NEUER EINTRAG:
                                             * Nur wenn keine ID zum
                                             * Bearbeiten vorhanden ist,
                                             * wird ein neuer Eintrag
                                             * erzeugt.
                                             */
                                            repairs +
                                                Repair(

                                                    id =
                                                        System
                                                            .currentTimeMillis(),

                                                    vehicle =
                                                        activeVehicle,

                                                    date =
                                                        normalizedDate,

                                                    mileage =
                                                        mileage.trim(),

                                                    description =
                                                        description.trim(),

                                                    cost =
                                                        cost.trim(),

                                                    workshop =
                                                        workshop.trim()
                                                )
                                        }

                                    repairs =
                                        updatedRepairs

                                    store.saveRepairs(
                                        updatedRepairs
                                    )

                                    resetForm()
                                }

                            ) {

                                Text(

                                    if (
                                        editingId !=
                                        null
                                    ) {

                                        "Änderung speichern"

                                    } else {

                                        "Speichern"
                                    }
                                )
                            }

                            OutlinedButton(

                                modifier =
                                    Modifier
                                        .weight(1f),

                                onClick = {

                                    resetForm()
                                }

                            ) {

                                Text(
                                    "Abbrechen"
                                )
                            }
                        }
                    }
                }
            }
        }

        if (!showForm) {

            Button(

                modifier =
                    Modifier
                        .fillMaxWidth(),

                onClick = {

                    startNewRepair()
                }

            ) {

                Text(
                    "Reparatur hinzufügen"
                )
            }
        }
    }
}

@Composable
private fun RepairRecordCard(

    repair: Repair,

    onEdit: () -> Unit,

    onDelete: () -> Unit

) {

    Card(

        colors =
            CardDefaults.cardColors(

                containerColor =
                    Color(
                        0xFF11141A
                    )
            ),

        modifier =
            Modifier
                .fillMaxWidth()
    ) {

        Column(

            modifier =
                Modifier
                    .padding(16.dp)
        ) {

            Text(

                text =
                    repair.description
                        .ifBlank {

                            "Reparatur"
                        },

                color =
                    Color.White,

                fontSize =
                    18.sp,

                fontWeight =
                    FontWeight.Bold
            )

            Spacer(
                modifier =
                    Modifier
                        .height(6.dp)
            )

            val lines =
                listOf(

                    "Datum: ${
                        repairDisplayDate(
                            repair.date
                        )
                    }",

                    "Kilometerstand: ${
                        repair.mileage
                    }",

                    "Kosten: ${
                        repair.cost
                    }",

                    "Werkstatt: ${
                        repair.workshop
                    }"
                )

            lines
                .filter {

                    it.substringAfter(
                        ":"
                    ).isNotBlank()
                }
                .forEach {

                    Text(

                        text = it,

                        color =
                            Color(
                                0xFFB8BEC8
                            )
                    )
                }

            Spacer(
                modifier =
                    Modifier
                        .height(10.dp)
            )

            Row(

                modifier =
                    Modifier
                        .fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.spacedBy(
                        10.dp
                    )
            ) {

                OutlinedButton(

                    modifier =
                        Modifier
                            .weight(1f),

                    onClick =
                        onEdit

                ) {

                    Text(
                        "Bearbeiten"
                    )
                }

                OutlinedButton(

                    modifier =
                        Modifier
                            .weight(1f),

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
