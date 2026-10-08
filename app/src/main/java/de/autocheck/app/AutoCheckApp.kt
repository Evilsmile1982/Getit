package de.autocheck.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

@Composable
fun AutoCheckApp() {

    val context =
        LocalContext.current

    val store =
        remember {
            VehicleStore(
                context
            )
        }

    val visitedStore =
        remember {
            VisitedStore(
                context
            )
        }

    val vehicles =
        remember {

            mutableStateListOf<Vehicle>()
                .apply {
                    addAll(
                        store.load()
                    )
                }
        }

    /*
     * ============================================================
     * EINZIGE QUELLE FÜR DAS AKTIVE FAHRZEUG
     * ============================================================
     */
    var activeVehicleId by remember {

        mutableStateOf(
            store.activeVehicleId()
        )
    }

    /*
     * Aktives Fahrzeug anhand der stabilen ID bestimmen.
     */
    val activeVehicle =
        vehicles.firstOrNull {
            it.id == activeVehicleId
        }

    /*
     * Bestehende Untermenüs arbeiten weiterhin mit dem
     * Fahrzeugnamen.
     */
    val activeVehicleName =
        activeVehicle?.name ?: ""

    var screen by remember {
        mutableStateOf(
            Screen.HOME
        )
    }

    var homeReady by remember {
        mutableStateOf(
            false
        )
    }

    /*
     * ============================================================
     * START
     * ============================================================
     */
    LaunchedEffect(Unit) {

        kotlinx.coroutines
            .delay(
                300
            )

        homeReady =
            true

        /*
         * Falls keine gültige aktive ID vorhanden ist,
         * wird automatisch das erste Fahrzeug aktiv.
         */
        if (
            vehicles.isNotEmpty() &&
            vehicles.none {
                it.id == activeVehicleId
            }
        ) {

            val firstVehicle =
                vehicles.first()

            activeVehicleId =
                firstVehicle.id

            store.setActiveVehicleId(
                firstVehicle.id
            )
        }
    }

    /*
     * ============================================================
     * HOME
     * ============================================================
     */
    if (
        screen ==
        Screen.HOME
    ) {

        HomeScreen(

            visible =
                homeReady,

            visitedStore =
                visitedStore,

            onSelect = {
                screen =
                    it
            }
        )

    } else {

        /*
         * ========================================================
         * TITEL
         * ========================================================
         */
        val title =
            when (screen) {

                Screen.AUTO ->
                    "Mein Auto"

                Screen.REPARATUREN ->
                    "Reparaturen"

                Screen.PICKERL ->
                    "Pickerl / TÜV"

                Screen.WARTUNGEN ->
                    "Wartungen"

                Screen.GESAMTBLICK ->
                    "Gesamtblick"

                Screen.SERVICE ->
                    "Service und Intervalle"

                Screen.REIFEN ->
                    "Reifen"

                Screen.HOME ->
                    "CARVITA"
            }

        DetailScaffold(

            title =
                title,

            onBack = {
                screen =
                    Screen.HOME
            }

        ) {

            when (screen) {

                /*
                 * ==================================================
                 * MEIN AUTO
                 * ==================================================
                 */
                Screen.AUTO ->

                    VehicleScreen(

                        vehicles =
                            vehicles,

                        activeVehicle =
                            activeVehicleId,

                        onActiveVehicle = {
                            selectedVehicleId ->

                            val selectedVehicle =
                                vehicles.firstOrNull {
                                    it.id ==
                                        selectedVehicleId
                                }

                            if (
                                selectedVehicle != null
                            ) {

                                activeVehicleId =
                                    selectedVehicle.id

                                store.setActiveVehicleId(
                                    selectedVehicle.id
                                )
                            }
                        },

                        onSave = {
                                vehicle,
                                oldName ->

                            val index =
                                vehicles.indexOfFirst {
                                    it.id ==
                                        vehicle.id
                                }
                                    .takeIf {
                                        it >= 0
                                    }
                                    ?: vehicles.indexOfFirst {
                                        it.name ==
                                            oldName
                                    }

                            if (
                                index >= 0
                            ) {

                                val oldVehicle =
                                    vehicles[index]

                                vehicles[index] =
                                    vehicle

                                /*
                                 * Bestehende Daten bei einer
                                 * Namensänderung übernehmen.
                                 */
                                if (
                                    oldVehicle.name !=
                                    vehicle.name
                                ) {

                                    migrateVehicleName(
                                        store,
                                        oldVehicle.name,
                                        vehicle.name
                                    )
                                }

                            } else if (
                                vehicles.size < 5
                            ) {

                                vehicles.add(
                                    vehicle
                                )
                            }

                            store.save(
                                vehicles
                            )

                            /*
                             * Gespeichertes Fahrzeug wird
                             * automatisch aktives Fahrzeug.
                             */
                            activeVehicleId =
                                vehicle.id

                            store.setActiveVehicleId(
                                vehicle.id
                            )
                        },

                        onDelete = {
                                vehicle ->

                            val wasActive =
                                vehicle.id ==
                                    activeVehicleId

                            vehicles.remove(
                                vehicle
                            )

                            store.deleteVehicleData(
                                vehicle.name
                            )

                            cancelPickerlReminder(
                                context,
                                vehicle.name
                            )

                            store.save(
                                vehicles
                            )

                            /*
                             * Wenn das aktive Fahrzeug gelöscht
                             * wurde, wird genau ein anderes Fahrzeug
                             * aktiv.
                             */
                            if (
                                wasActive
                            ) {

                                val nextVehicle =
                                    vehicles.firstOrNull()

                                if (
                                    nextVehicle != null
                                ) {

                                    activeVehicleId =
                                        nextVehicle.id

                                    store.setActiveVehicleId(
                                        nextVehicle.id
                                    )

                                } else {

                                    activeVehicleId =
                                        ""

                                    store.setActiveVehicleId(
                                        ""
                                    )
                                }
                            }
                        }
                    )

                /*
                 * ==================================================
                 * REPARATUREN
                 * ==================================================
                 */
                Screen.REPARATUREN ->

                    RepairScreen(

                        store =
                            store,

                        vehicles =
                            if (
                                activeVehicle != null
                            ) {
                                listOf(
                                    activeVehicle
                                )
                            } else {
                                emptyList()
                            },

                        activeVehicle =
                            activeVehicleName,

                        onActiveVehicle = {
                            // Aktives Fahrzeug wird ausschließlich
                            // in Mein Auto geändert.
                        },

                        onVisited = {

                            visitedStore.markVisited(
                                Screen.REPARATUREN
                            )
                        }
                    )

                /*
                 * ==================================================
                 * PICKERL
                 * ==================================================
                 */
                Screen.PICKERL ->

                    PickerlScreen(

                        store =
                            store,

                        vehicles =
                            if (
                                activeVehicle != null
                            ) {
                                listOf(
                                    activeVehicle
                                )
                            } else {
                                emptyList()
                            },

                        activeVehicle =
                            activeVehicleName,

                        onActiveVehicle = {
                            // Aktives Fahrzeug wird ausschließlich
                            // in "Mein Auto" geändert.
                        },

                        onVisited = {

                            visitedStore.markVisited(
                                Screen.PICKERL
                            )
                        }
                    )

                /*
                 * ==================================================
                 * WARTUNGEN
                 * ==================================================
                 */
                Screen.WARTUNGEN ->

                    MaintenanceScreen(

                        store =
                            store,

                        vehicles =
                            if (
                                activeVehicle != null
                            ) {
                                listOf(
                                    activeVehicle
                                )
                            } else {
                                emptyList()
                            },

                        activeVehicle =
                            activeVehicleName,

                        onActiveVehicle = {
                            // Aktives Fahrzeug wird ausschließlich
                            // in Mein Auto geändert.
                        },

                        onVisited = {

                            visitedStore.markVisited(
                                Screen.WARTUNGEN
                            )
                        }
                    )

                /*
                 * ==================================================
                 * GESAMTBLICK
                 * ==================================================
                 */
                Screen.GESAMTBLICK ->

                    OverviewScreen(

                        store =
                            store,

                        vehicles =
                            if (
                                activeVehicle != null
                            ) {
                                listOf(
                                    activeVehicle
                                )
                            } else {
                                emptyList()
                            },

                        activeVehicle =
                            activeVehicleName,

                        onActiveVehicle = {
                            // Aktives Fahrzeug wird ausschließlich
                            // in Mein Auto geändert.
                        },

                        onVisited = {

                            visitedStore.markVisited(
                                Screen.GESAMTBLICK
                            )
                        }
                    )

                /*
                 * ==================================================
                 * SERVICE UND INTERVALLE
                 * ==================================================
                 *
                 * NEU:
                 *
                 * Statt direkt die bisherige ServiceScreen-Seite
                 * zu öffnen, wird jetzt zuerst das neue Service-Menü
                 * angezeigt.
                 */
                Screen.SERVICE ->

                    ServiceMenuScreen(

                        store =
                            store,

                        vehicles =
                            if (
                                activeVehicle != null
                            ) {
                                listOf(
                                    activeVehicle
                                )
                            } else {
                                emptyList()
                            },

                        activeVehicle =
                            activeVehicleName,

                        activeVehicleData =
                            activeVehicle,

                        onActiveVehicle = {
                            // Das aktive Fahrzeug wird weiterhin
                            // ausschließlich in "Mein Auto"
                            // geändert.
                        },

                        onVisited = {

                            visitedStore.markVisited(
                                Screen.SERVICE
                            )
                        }
                    )

                /*
                 * ==================================================
                 * REIFEN
                 * ==================================================
                 */
                Screen.REIFEN ->

                    TireScreen(

                        store =
                            store,

                        vehicles =
                            if (
                                activeVehicle != null
                            ) {
                                listOf(
                                    activeVehicle
                                )
                            } else {
                                emptyList()
                            },

                        activeVehicle =
                            activeVehicleName,

                        onActiveVehicle = {
                            // Aktives Fahrzeug wird ausschließlich
                            // in Mein Auto geändert.
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
