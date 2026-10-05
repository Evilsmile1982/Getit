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
     *
     * Ab jetzt wird ausschließlich die stabile Vehicle-ID
     * verwendet.
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
     * Die bestehenden Untermenüs arbeiten weiterhin mit dem
     * Fahrzeugnamen.
     *
     * Der Name wird ausschließlich aus dem zentral aktiven
     * Fahrzeug abgeleitet.
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

        homeReady = true

        /*
         * Falls keine gültige aktive ID gespeichert ist,
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

                Screen.REIFEN ->
                    "Reifen"

                Screen.HOME ->
                    "AutoCheck"
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

                        /*
                         * WICHTIG:
                         *
                         * VehicleScreen arbeitet jetzt direkt
                         * mit der stabilen Fahrzeug-ID.
                         */
                        activeVehicle =
                            activeVehicleId,

                        /*
                         * VehicleScreen liefert ebenfalls
                         * ausschließlich die Fahrzeug-ID zurück.
                         */
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

                                /*
                                 * Zentrale aktive ID ändern.
                                 */
                                activeVehicleId =
                                    selectedVehicle.id

                                /*
                                 * Dauerhaft speichern.
                                 */
                                store.setActiveVehicleId(
                                    selectedVehicle.id
                                )
                            }
                        },

                        /*
                         * ==================================================
                         * FAHRZEUG SPEICHERN
                         * ==================================================
                         */
                        onSave = {
                                vehicle,
                                oldName ->

                            /*
                             * Primär über die stabile ID suchen.
                             *
                             * Fallback über den Namen bleibt für
                             * bestehende ältere Fahrzeuge erhalten.
                             */
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

                                /*
                                 * Fahrzeug aktualisieren.
                                 */
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

                                /*
                                 * Neues Fahrzeug hinzufügen.
                                 */
                                vehicles.add(
                                    vehicle
                                )
                            }

                            /*
                             * Fahrzeugliste speichern.
                             */
                            store.save(
                                vehicles
                            )

                            /*
                             * Das gespeicherte Fahrzeug ist
                             * eindeutig das aktive Fahrzeug.
                             */
                            activeVehicleId =
                                vehicle.id

                            store.setActiveVehicleId(
                                vehicle.id
                            )
                        },

                        /*
                         * ==================================================
                         * FAHRZEUG LÖSCHEN
                         * ==================================================
                         */
                        onDelete = {
                                vehicle ->

                            val wasActive =
                                vehicle.id ==
                                    activeVehicleId

                            /*
                             * Fahrzeug entfernen.
                             */
                            vehicles.remove(
                                vehicle
                            )

                            /*
                             * Zugehörige Daten entfernen.
                             */
                            store.deleteVehicleData(
                                vehicle.name
                            )

                            /*
                             * Pickerl-Erinnerung entfernen.
                             */
                            cancelPickerlReminder(
                                context,
                                vehicle.name
                            )

                            /*
                             * Fahrzeugliste speichern.
                             */
                            store.save(
                                vehicles
                            )

                            /*
                             * Wurde das aktive Fahrzeug gelöscht,
                             * wird genau ein neues Fahrzeug aktiv.
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
                 *
                 * Diese Screens arbeiten momentan noch mit
                 * dem Fahrzeugnamen.
                 *
                 * Sie bekommen aber ausschließlich den Namen
                 * des zentral aktiven Fahrzeugs.
                 */
                Screen.REPARATUREN ->

                    RepairScreen(

                        store =
                            store,

                        vehicles =
                            vehicles,

                        activeVehicle =
                            activeVehicleName,

                        onActiveVehicle = {
                            selectedName ->

                            val selectedVehicle =
                                vehicles.firstOrNull {
                                    it.name ==
                                        selectedName
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
                            vehicles,

                        activeVehicle =
                            activeVehicleName,

                        onActiveVehicle = {
                            selectedName ->

                            val selectedVehicle =
                                vehicles.firstOrNull {
                                    it.name ==
                                        selectedName
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
                            vehicles,

                        activeVehicle =
                            activeVehicleName,

                        onActiveVehicle = {
                            selectedName ->

                            val selectedVehicle =
                                vehicles.firstOrNull {
                                    it.name ==
                                        selectedName
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
                            vehicles,

                        activeVehicle =
                            activeVehicleName,

                        onActiveVehicle = {
                            selectedName ->

                            val selectedVehicle =
                                vehicles.firstOrNull {
                                    it.name ==
                                        selectedName
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

                        onVisited = {

                            visitedStore.markVisited(
                                Screen.GESAMTBLICK
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
                            vehicles,

                        activeVehicle =
                            activeVehicleName,

                        onActiveVehicle = {
                            selectedName ->

                            val selectedVehicle =
                                vehicles.firstOrNull {
                                    it.name ==
                                        selectedName
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

                        onVisited = {

                            visitedStore.markVisited(
                                Screen.REIFEN
                            )
                        }
                    )

                else -> Unit
            }
        }
    }
}
