package de.autocheck.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

private fun repairDisplayDate(value: String): String {

    val trimmed =
        value.trim()

    if (trimmed.isBlank()) {
        return ""
    }

    if (
        trimmed.matches(
            Regex("""\d{4}-\d{2}-\d{2}""")
        )
    ) {

        val parts =
            trimmed.split("-")

        if (parts.size == 3) {

            return "${parts[2]}.${parts[1]}.${parts[0]}"
        }
    }

    return trimmed
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

    Column(
        modifier =
            androidx.compose.ui.Modifier
                .fillMaxSize()
    ) {

        VehicleSelector(
            vehicles,
            activeVehicle,
            onActiveVehicle
        )

        Spacer(
            modifier =
                androidx.compose.ui.Modifier
                    .height(10.dp)
        )

        if (vehicles.isEmpty()) {
            return@Column
        }

        LazyColumn(

            modifier =
                androidx.compose.ui.Modifier
                    .weight(1f),

            verticalArrangement =
                Arrangement.spacedBy(10.dp)

        ) {

            if (list.isEmpty()) {

                item {

                    EmptyCard(
                        "Noch keine Reparatur eingetragen."
                    )
                }
            }

            items(list) { repair ->

                RecordCard(

                    title =
                        repair.description
                            .ifBlank {
                                "Reparatur"
                            },

                    lines =
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
                        ),

                    onDelete = {

                        repairs =
                            repairs.filterNot {
                                it.id == repair.id
                            }

                        store.saveRepairs(
                            repairs
                        )
                    }
                )
            }

            if (showForm) {

                item {

                    CardForm {

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

                        FormButtons(

                            onSave = {

                                val updated =
                                    repairs +
                                        Repair(

                                            id =
                                                System
                                                    .currentTimeMillis(),

                                            vehicle =
                                                activeVehicle,

                                            date =
                                                repairDisplayDate(
                                                    date
                                                ),

                                            mileage =
                                                mileage.trim(),

                                            description =
                                                description.trim(),

                                            cost =
                                                cost.trim(),

                                            workshop =
                                                workshop.trim()
                                        )

                                repairs =
                                    updated

                                store.saveRepairs(
                                    updated
                                )

                                date = ""
                                mileage = ""
                                description = ""
                                cost = ""
                                workshop = ""

                                showForm = false
                            },

                            onCancel = {

                                showForm = false
                            }
                        )
                    }
                }
            }
        }

        if (!showForm) {

            Button(

                modifier =
                    androidx.compose.ui.Modifier
                        .fillMaxWidth(),

                onClick = {
                    showForm = true
                }

            ) {

                androidx.compose.material3.Text(
                    "Reparatur hinzufügen"
                )
            }
        }
    }
}
