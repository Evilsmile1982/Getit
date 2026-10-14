package de.autocheck.app

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun AutoCheckApp() {

    val context = LocalContext.current

    val store = remember {
        VehicleStore(context)
    }

    val visitedStore = remember {
        VisitedStore(context)
    }

    val vehicles = remember {
        mutableStateListOf<Vehicle>().apply {
            addAll(store.load())
        }
    }

    /*
     * EINZIGE QUELLE FÜR DAS AKTIVE FAHRZEUG
     */
    var activeVehicleId by remember {
        mutableStateOf(store.activeVehicleId())
    }

    val activeVehicle = vehicles.firstOrNull {
        it.id == activeVehicleId
    }

    val activeVehicleName = activeVehicle?.name ?: ""

    var screen by remember {
        mutableStateOf(Screen.HOME)
    }

    var showPickerlDocuments by remember {
        mutableStateOf(false)
    }

    var homeReady by remember {
        mutableStateOf(false)
    }

    /*
     * START
     */
    LaunchedEffect(Unit) {

        kotlinx.coroutines.delay(300)

        homeReady = true

        if (
            vehicles.isNotEmpty() &&
            vehicles.none {
                it.id == activeVehicleId
            }
        ) {
            val firstVehicle = vehicles.first()

            activeVehicleId = firstVehicle.id

            store.setActiveVehicleId(firstVehicle.id)
        }
    }

    /*
     * HOME
     */
    if (screen == Screen.HOME) {

        HomePagerScreen(
            visible = homeReady,
            visitedStore = visitedStore,
            onSelect = {
                showPickerlDocuments = false
                screen = it
            }
        )

    } else {

        /*
         * TITEL
         */
        val title = when (screen) {

            Screen.AUTO -> "Mein Auto"

            Screen.REPARATUREN -> "Reparaturen"

            Screen.PICKERL ->
                if (showPickerlDocuments) {
                    "Pickerl-Dokumentation"
                } else {
                    "Pickerl / TÜV"
                }

            Screen.WARTUNGEN -> "Wartungen"

            Screen.GESAMTBLICK -> "Gesamtblick"

            Screen.SERVICE -> "Service und Intervalle"

            Screen.REIFEN -> "Reifen"

            Screen.HOME -> "CARVITA"
        }

        DetailScaffold(
            title = title,
            onBack = {
                if (
                    screen == Screen.PICKERL &&
                    showPickerlDocuments
                ) {
                    showPickerlDocuments = false
                } else {
                    screen = Screen.HOME
                }
            }
        ) {

            when (screen) {

                /*
                 * MEIN AUTO
                 */
                Screen.AUTO ->

                    VehicleScreen(
                        vehicles = vehicles,
                        activeVehicle = activeVehicleId,

                        onActiveVehicle = { selectedVehicleId ->

                            val selectedVehicle =
                                vehicles.firstOrNull {
                                    it.id == selectedVehicleId
                                }

                            if (selectedVehicle != null) {

                                activeVehicleId = selectedVehicle.id

                                store.setActiveVehicleId(
                                    selectedVehicle.id
                                )
                            }
                        },

                        onSave = { vehicle, oldName ->

                            val index =
                                vehicles.indexOfFirst {
                                    it.id == vehicle.id
                                }.takeIf {
                                    it >= 0
                                } ?: vehicles.indexOfFirst {
                                    it.name == oldName
                                }

                            if (index >= 0) {

                                val oldVehicle = vehicles[index]

                                vehicles[index] = vehicle

                                if (
                                    oldVehicle.name != vehicle.name
                                ) {
                                    migrateVehicleName(
                                        store,
                                        oldVehicle.name,
                                        vehicle.name
                                    )
                                }

                            } else if (vehicles.size < 5) {

                                vehicles.add(vehicle)
                            }

                            store.save(vehicles)

                            activeVehicleId = vehicle.id

                            store.setActiveVehicleId(vehicle.id)
                        },

                        onDelete = { vehicle ->

                            val wasActive =
                                vehicle.id == activeVehicleId

                            vehicles.remove(vehicle)

                            store.deleteVehicleData(vehicle.name)

                            cancelPickerlReminder(
                                context,
                                vehicle.name
                            )

                            store.save(vehicles)

                            if (wasActive) {

                                val nextVehicle =
                                    vehicles.firstOrNull()

                                if (nextVehicle != null) {

                                    activeVehicleId = nextVehicle.id

                                    store.setActiveVehicleId(
                                        nextVehicle.id
                                    )

                                } else {

                                    activeVehicleId = ""

                                    store.setActiveVehicleId("")
                                }
                            }
                        }
                    )

                /*
                 * REPARATUREN
                 */
                Screen.REPARATUREN ->

                    RepairScreen(
                        store = store,

                        vehicles = if (activeVehicle != null) {
                            listOf(activeVehicle)
                        } else {
                            emptyList()
                        },

                        activeVehicle = activeVehicleName,

                        onActiveVehicle = {
                            // Fahrzeugwechsel nur unter Mein Auto.
                        },

                        onVisited = {
                            visitedStore.markVisited(
                                Screen.REPARATUREN
                            )
                        }
                    )

                /*
                 * PICKERL UND DOKUMENTATION
                 */
                Screen.PICKERL -> {

                    if (showPickerlDocuments) {

                        PickerlDocumentsScreen(
                            vehicle = activeVehicleName,
                            onBack = {
                                showPickerlDocuments = false
                            }
                        )

                    } else {

                        Column {

                            PickerlScreen(
                                store = store,

                                vehicles = if (activeVehicle != null) {
                                    listOf(activeVehicle)
                                } else {
                                    emptyList()
                                },

                                activeVehicle = activeVehicleName,

                                onActiveVehicle = {
                                    // Fahrzeugwechsel nur unter Mein Auto.
                                },

                                onVisited = {
                                    visitedStore.markVisited(
                                        Screen.PICKERL
                                    )
                                }
                            )

                            Button(
                                onClick = {
                                    showPickerlDocuments = true
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text("Prüfberichte und Rechnungen")
                            }
                        }
                    }
                }

                /*
                 * WARTUNGEN
                 */
                Screen.WARTUNGEN ->

                    MaintenanceScreen(
                        store = store,

                        vehicles = if (activeVehicle != null) {
                            listOf(activeVehicle)
                        } else {
                            emptyList()
                        },

                        activeVehicle = activeVehicleName,

                        onActiveVehicle = {
                            // Fahrzeugwechsel nur unter Mein Auto.
                        },

                        onVisited = {
                            visitedStore.markVisited(
                                Screen.WARTUNGEN
                            )
                        }
                    )

                /*
                 * GESAMTBLICK
                 */
                Screen.GESAMTBLICK ->

                    OverviewScreen(
                        store = store,

                        vehicles = if (activeVehicle != null) {
                            listOf(activeVehicle)
                        } else {
                            emptyList()
                        },

                        activeVehicle = activeVehicleName,

                        onActiveVehicle = {
                            // Fahrzeugwechsel nur unter Mein Auto.
                        },

                        onVisited = {
                            visitedStore.markVisited(
                                Screen.GESAMTBLICK
                            )
                        }
                    )

                /*
                 * SERVICE UND INTERVALLE
                 */
                Screen.SERVICE ->

                    ServiceMenuScreen(
                        store = store,

                        vehicles = if (activeVehicle != null) {
                            listOf(activeVehicle)
                        } else {
                            emptyList()
                        },

                        activeVehicle = activeVehicleName,

                        activeVehicleData = activeVehicle,

                        onActiveVehicle = {
                            // Fahrzeugwechsel nur unter Mein Auto.
                        },

                        onVisited = {
                            visitedStore.markVisited(
                                Screen.SERVICE
                            )
                        }
                    )

                /*
                 * REIFEN
                 */
                Screen.REIFEN ->

                    TireScreen(
                        store = store,

                        vehicles = if (activeVehicle != null) {
                            listOf(activeVehicle)
                        } else {
                            emptyList()
                        },

                        activeVehicle = activeVehicleName,

                        onActiveVehicle = {
                            // Fahrzeugwechsel nur unter Mein Auto.
                        },

                        onVisited = {
                            visitedStore.markVisited(
                                Screen.REIFEN
                            )
                        }
                    )

                Screen.HOME -> Unit
            }
        }
    }
}
