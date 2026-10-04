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
     * Die ID ist stabil und unabhängig vom Fahrzeugnamen.
     */
    var activeVehicleId by remember {

        mutableStateOf(
            store.activeVehicleId()
        )
    }

    /*
     * Das aktuell aktive Fahrzeug wird immer über seine ID
     * ermittelt.
     */
    val activeVehicle =
        vehicles.firstOrNull {
            it.id == activeVehicleId
        }

    /*
     * Die bestehenden Untermenüs arbeiten momentan noch mit
     * dem Fahrzeugnamen. Der Name wird deshalb ausschließlich
     * aus dem zentral aktiven Fahrzeug abgeleitet.
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
     * START / AKTIVES FAHRZEUG SICHERSTELLEN
     * ============================================================
     */
    LaunchedEffect(Unit) {

        kotlinx.coroutines
            .delay(
                300
            )

        homeReady = true

        /*
         * Falls kein gültiges aktives Fahrzeug vorhanden ist,
         * wird automatisch das erste Fahrzeug aktiviert.
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
                         * Vorübergehend wird hier weiterhin der
                         * Name übergeben. Im nächsten Schritt
                         * stellen wir VehicleScreen selbst auf ID
                         * um.
                         */
                        activeVehicle =
                            activeVehicleName,

                        onActiveVehicle = {
                            selectedName ->

                            /*
                             * Den Namen niemals direkt als
                             * dauerhafte aktive Kennung verwenden.
                             *
                             * Wir suchen das Fahrzeug und speichern
                             * anschließend ausschließlich seine ID.
                             */
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

                        onSave = {
                                vehicle,
                                oldName ->

                            /*
                             * Primär über die stabile ID suchen.
                             *
                             * Fallback über den alten Namen ist
                             * wichtig für bereits vorhandene Daten.
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

                                vehicles[index] =
                                    vehicle

                                /*
                                 * Bestehende Reparatur-/Wartungs-/
                                 * Pickerl-/Reifendaten werden bei
                                 * einer Namensänderung weiterhin
                                 * übernommen.
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
                                 * Neues Fahrzeug.
                                 */
                                vehicles.add(
                                    vehicle
                                )
                            }

                            /*
                             * Fahrzeugliste dauerhaft speichern.
                             */
                            store.save(
                                vehicles
                            )

                            /*
                             * Das gerade gespeicherte Fahrzeug
                             * wird zum EINZIGEN aktiven Fahrzeug.
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

                            /*
                             * Fahrzeug aus der Liste entfernen.
                             */
                            vehicles.remove(
                                vehicle
                            )

                            /*
                             * Zugehörige Daten löschen.
                             */
                            store.deleteVehicleData(
                                vehicle.name
                            )

                            /*
                             * Pickerl-Erinnerung löschen.
                             */
                            cancelPickerlReminder(
                                context,
                                vehicle.name
                            )

                            /*
                             * Geänderte Fahrzeugliste dauerhaft
                             * speichern.
                             */
                            store.save(
                                vehicles
                            )

                            /*
                             * Wenn das aktive Fahrzeug gelöscht
                             * wurde, wird genau ein neues aktives
                             * Fahrzeug bestimmt.
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
