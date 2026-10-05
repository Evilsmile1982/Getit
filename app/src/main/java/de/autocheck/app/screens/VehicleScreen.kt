package de.autocheck.app

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight

@Composable
fun VehicleScreen(
    vehicles: MutableList<Vehicle>,
    activeVehicle: String,
    onActiveVehicle: (String) -> Unit,
    onSave: (Vehicle, String) -> Unit,
    onDelete: (Vehicle) -> Unit
) {

    var adding by remember {
        mutableStateOf(false)
    }

    var editingVehicleId by remember {
        mutableStateOf("")
    }

    var editingName by remember {
        mutableStateOf("")
    }

    var name by remember {
        mutableStateOf("")
    }

    var vehicleType by remember {
        mutableStateOf("PKW")
    }

    var make by remember {
        mutableStateOf("")
    }

    var model by remember {
        mutableStateOf("")
    }

    var motorization by remember {
        mutableStateOf("")
    }

    var power by remember {
        mutableStateOf("")
    }

    var year by remember {
        mutableStateOf("")
    }

    var plate by remember {
        mutableStateOf("")
    }

    var vin by remember {
        mutableStateOf("")
    }

    var imageUri by remember {
        mutableStateOf("")
    }

    var vehicleTypeExpanded by remember {
        mutableStateOf(false)
    }

    val vehicleTypes =
        listOf(
            "PKW",
            "WOHNMOBIL",
            "MOTORRAD",
            "QUAD",
            "MOPED",
            "NUTZFAHRZEUG",
            "LKW",
            "BAUMASCHINE"
        )

    val context =
        LocalContext.current

    /*
     * ============================================================
     * BILD AUS GALERIE
     * ============================================================
     */

    val launcher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.OpenDocument()
        ) { uri ->

            if (uri != null) {

                try {

                    context.contentResolver
                        .takePersistableUriPermission(
                            uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                        )

                } catch (
                    _: SecurityException
                ) {
                    // Nicht jeder Provider erlaubt persistente Rechte.
                }

                imageUri =
                    uri.toString()
            }
        }

    /*
     * ============================================================
     * HAUPTBEREICH
     * ============================================================
     */

    Column(
        modifier =
            Modifier.fillMaxSize()
    ) {

        Text(
            "${vehicles.size}/5 Fahrzeuge",
            color =
                Color(0xFFB8BEC8),
            modifier =
                Modifier.padding(
                    bottom = 12.dp
                )
        )

        /*
         * ========================================================
         * KEINE FAHRZEUGE
         * ========================================================
         */

        if (vehicles.isEmpty()) {

            Text(
                "Noch kein Fahrzeug angelegt.",
                color =
                    Color.White,
                fontSize =
                    20.sp,
                fontWeight =
                    FontWeight.Bold
            )

            Spacer(
                Modifier.height(
                    8.dp
                )
            )
        }

        /*
         * ========================================================
         * FAHRZEUGLISTE
         * ========================================================
         */

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

            items(
                vehicles,
                key = {
                    it.id
                }
            ) { vehicle ->

                Card(

                    colors =
                        CardDefaults.cardColors(
                            containerColor =
                                Color(
                                    0xFF11141A
                                )
                        ),

                    modifier =
                        Modifier.fillMaxWidth()
                ) {

                    Column(
                        Modifier.padding(
                            16.dp
                        )
                    ) {

                        /*
                         * ==================================================
                         * FAHRZEUGKOPF
                         * ==================================================
                         */

                        Row(

                            verticalAlignment =
                                Alignment.CenterVertically

                        ) {

                            VehicleImage(

                                uri =
                                    vehicle.imageUri,

                                modifier =
                                    Modifier.size(
                                        82.dp
                                    )
                            )

                            Spacer(
                                Modifier.width(
                                    12.dp
                                )
                            )

                            Column(

                                modifier =
                                    Modifier.weight(
                                        1f
                                    )

                            ) {

                                Text(

                                    vehicle.name,

                                    color =
                                        Color.White,

                                    fontWeight =
                                        FontWeight.Bold,

                                    fontSize =
                                        18.sp
                                )

                                Text(

                                    "${vehicle.vehicleType} • ${vehicle.make} ${vehicle.model}"
                                        .trim(),

                                    color =
                                        Color(
                                            0xFFB8BEC8
                                        )
                                )

                                if (
                                    vehicle.motorization.isNotBlank()
                                ) {

                                    Text(

                                        "Motorisierung: ${vehicle.motorization}",

                                        color =
                                            Color(
                                                0xFFB8BEC8
                                            )
                                    )
                                }

                                if (
                                    vehicle.power.isNotBlank()
                                ) {

                                    Text(

                                        "PS/KW: ${vehicle.power}",

                                        color =
                                            Color(
                                                0xFFB8BEC8
                                            )
                                    )
                                }

                                if (
                                    vehicle.year.isNotBlank()
                                ) {

                                    Text(

                                        "Baujahr: ${vehicle.year}",

                                        color =
                                            Color(
                                                0xFFB8BEC8
                                            )
                                    )
                                }

                                if (
                                    vehicle.plate.isNotBlank()
                                ) {

                                    Text(

                                        "Kennzeichen: ${vehicle.plate}",

                                        color =
                                            Color.White
                                    )
                                }

                                if (
                                    vehicle.vin.isNotBlank()
                                ) {

                                    Text(

                                        "FIN/VIN: ${vehicle.vin}",

                                        color =
                                            Color(
                                                0xFFB8BEC8
                                            ),

                                        maxLines =
                                            1
                                    )
                                }
                            }
                        }

                        Spacer(
                            Modifier.height(
                                10.dp
                            )
                        )

                        /*
                         * ==================================================
                         * AKTIV / BEARBEITEN
                         * ==================================================
                         */

                        Row(

                            horizontalArrangement =
                                Arrangement.spacedBy(
                                    8.dp
                                )
                        ) {

                            Button(

                                onClick = {

                                    onActiveVehicle(
                                        vehicle.id
                                    )
                                }

                            ) {

                                Text(

                                    if (
                                        activeVehicle ==
                                        vehicle.id
                                    ) {
                                        "Aktiv"
                                    } else {
                                        "Aktiv setzen"
                                    }
                                )
                            }

                            OutlinedButton(

                                onClick = {

                                    editingVehicleId =
                                        vehicle.id

                                    editingName =
                                        vehicle.name

                                    name =
                                        vehicle.name

                                    vehicleType =
                                        vehicle.vehicleType.ifBlank {
                                            "PKW"
                                        }

                                    make =
                                        vehicle.make

                                    model =
                                        vehicle.model

                                    motorization =
                                        vehicle.motorization

                                    power =
                                        vehicle.power

                                    year =
                                        vehicle.year

                                    plate =
                                        vehicle.plate

                                    vin =
                                        vehicle.vin

                                    imageUri =
                                        vehicle.imageUri

                                    adding =
                                        true
                                }

                            ) {

                                Text(
                                    "Bearbeiten"
                                )
                            }
                        }

                        Spacer(
                            Modifier.height(
                                4.dp
                            )
                        )

                        /*
                         * ==================================================
                         * FAHRZEUG LÖSCHEN
                         * ==================================================
                         */

                        OutlinedButton(

                            onClick = {

                                onDelete(
                                    vehicle
                                )
                            }

                        ) {

                            Text(
                                "Fahrzeug entfernen"
                            )
                        }
                    }
                }
            }

            /*
             * ========================================================
             * FORMULAR
             * ========================================================
             */

            if (adding) {

                item {

                    Card(

                        colors =
                            CardDefaults.cardColors(
                                containerColor =
                                    Color(
                                        0xFF11141A
                                    )
                            ),

                        modifier =
                            Modifier.fillMaxWidth()

                    ) {

                        Column(

                            Modifier.padding(
                                16.dp
                            ),

                            verticalArrangement =
                                Arrangement.spacedBy(
                                    8.dp
                                )
                        ) {

                            Text(

                                if (
                                    editingVehicleId.isBlank()
                                ) {
                                    "Fahrzeug hinzufügen"
                                } else {
                                    "Fahrzeug bearbeiten"
                                },

                                color =
                                    Color.White,

                                fontWeight =
                                    FontWeight.Bold
                            )

                            /*
                             * ==================================================
                             * BEZEICHNUNG
                             * ==================================================
                             */

                            OutlinedButton(

                                onClick = {

                                    vehicleTypeExpanded =
                                        true
                                },

                                modifier =
                                    Modifier.fillMaxWidth()
                            ) {

                                Text(
                                    "Bezeichnung: $vehicleType"
                                )
                            }

                            DropdownMenu(

                                expanded =
                                    vehicleTypeExpanded,

                                onDismissRequest = {

                                    vehicleTypeExpanded =
                                        false
                                }

                            ) {

                                vehicleTypes.forEach { type ->

                                    DropdownMenuItem(

                                        text = {
                                            Text(
                                                type
                                            )
                                        },

                                        onClick = {

                                            vehicleType =
                                                type

                                            vehicleTypeExpanded =
                                                false
                                        }
                                    )
                                }
                            }

                            /*
                             * ==================================================
                             * MARKE
                             * ==================================================
                             */

                            OutlinedTextField(

                                value =
                                    make,

                                onValueChange = {
                                    make =
                                        it
                                },

                                label = {
                                    Text(
                                        "Marke"
                                    )
                                },

                                modifier =
                                    Modifier.fillMaxWidth()
                            )

                            /*
                             * ==================================================
                             * MODELL
                             * ==================================================
                             */

                            OutlinedTextField(

                                value =
                                    model,

                                onValueChange = {
                                    model =
                                        it
                                },

                                label = {
                                    Text(
                                        "Modell"
                                    )
                                },

                                modifier =
                                    Modifier.fillMaxWidth()
                            )

                            /*
                             * ==================================================
                             * MOTORISIERUNG
                             * ==================================================
                             */

                            OutlinedTextField(

                                value =
                                    motorization,

                                onValueChange = {
                                    motorization =
                                        it
                                },

                                label = {
                                    Text(
                                        "Motorisierung"
                                    )
                                },

                                modifier =
                                    Modifier.fillMaxWidth()
                            )

                            /*
                             * ==================================================
                             * PS / KW
                             * ==================================================
                             */

                            OutlinedTextField(

                                value =
                                    power,

                                onValueChange = {
                                    power =
                                        it
                                },

                                label = {
                                    Text(
                                        "PS/KW"
                                    )
                                },

                                modifier =
                                    Modifier.fillMaxWidth()
                            )

                            /*
                             * ==================================================
                             * BAUJAHR
                             * ==================================================
                             */

                            OutlinedTextField(

                                value =
                                    year,

                                onValueChange = {
                                    year =
                                        it
                                },

                                label = {
                                    Text(
                                        "Baujahr"
                                    )
                                },

                                modifier =
                                    Modifier.fillMaxWidth()
                            )

                            /*
                             * ==================================================
                             * KENNZEICHEN
                             * ==================================================
                             */

                            OutlinedTextField(

                                value =
                                    plate,

                                onValueChange = {
                                    plate =
                                        it
                                },

                                label = {
                                    Text(
                                        "Kennzeichen"
                                    )
                                },

                                modifier =
                                    Modifier.fillMaxWidth()
                            )

                            /*
                             * ==================================================
                             * FIN / VIN
                             * ==================================================
                             */

                            OutlinedTextField(

                                value =
                                    vin,

                                onValueChange = {
                                    vin =
                                        it
                                },

                                label = {
                                    Text(
                                        "FIN / VIN"
                                    )
                                },

                                modifier =
                                    Modifier.fillMaxWidth()
                            )

                            /*
                             * ==================================================
                             * FAHRZEUGBILD
                             * ==================================================
                             */

                            OutlinedButton(

                                onClick = {

                                    launcher.launch(
                                        arrayOf(
                                            "image/*"
                                        )
                                    )
                                },

                                modifier =
                                    Modifier.fillMaxWidth()
                            ) {

                                Text(
                                    "Fahrzeugbild aus Galerie wählen"
                                )
                            }

                            if (
                                imageUri.isNotBlank()
                            ) {

                                VehicleImage(

                                    uri =
                                        imageUri,

                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .height(
                                                160.dp
                                            )
                                )
                            }

                            /*
                             * ==================================================
                             * SPEICHERN / ABBRECHEN
                             * ==================================================
                             */

                            Row(

                                horizontalArrangement =
                                    Arrangement.spacedBy(
                                        8.dp
                                    )
                            ) {

                                Button(

                                    enabled =
                                        vehicleType.isNotBlank() &&
                                        make.isNotBlank() &&
                                        model.isNotBlank(),

                                    onClick = {

                                        /*
                                         * ==================================================
                                         * AUTOMATISCHER FAHRZEUGNAME
                                         * ==================================================
                                         *
                                         * PS/KW wird absichtlich NICHT
                                         * in den Fahrzeugnamen aufgenommen.
                                         *
                                         * Dadurch lautet die Überschrift
                                         * beispielsweise:
                                         *
                                         * PKW Volkswagen Golf 2001
                                         *
                                         * Die PS/KW bleiben separat
                                         * im Fahrzeug gespeichert.
                                         */

                                        val generatedName =
                                            listOf(
                                                vehicleType,
                                                make.trim(),
                                                model.trim(),
                                                year.trim()
                                            )
                                                .filter {
                                                    it.isNotBlank()
                                                }
                                                .joinToString(
                                                    " "
                                                )

                                        /*
                                         * ==================================================
                                         * FAHRZEUG SPEICHERN
                                         * ==================================================
                                         */

                                        val vehicleToSave =

                                            if (
                                                editingVehicleId.isNotBlank()
                                            ) {

                                                /*
                                                 * BEARBEITEN
                                                 *
                                                 * Die bestehende ID bleibt
                                                 * vollständig erhalten.
                                                 */

                                                Vehicle(

                                                    id =
                                                        editingVehicleId,

                                                    name =
                                                        generatedName,

                                                    vehicleType =
                                                        vehicleType,

                                                    make =
                                                        make.trim(),

                                                    model =
                                                        model.trim(),

                                                    motorization =
                                                        motorization.trim(),

                                                    power =
                                                        power.trim(),

                                                    year =
                                                        year.trim(),

                                                    plate =
                                                        plate.trim(),

                                                    vin =
                                                        vin.trim(),

                                                    imageUri =
                                                        imageUri
                                                )

                                            } else {

                                                /*
                                                 * NEUES FAHRZEUG
                                                 *
                                                 * Vehicle erzeugt automatisch
                                                 * eine neue stabile UUID.
                                                 */

                                                Vehicle(

                                                    name =
                                                        generatedName,

                                                    vehicleType =
                                                        vehicleType,

                                                    make =
                                                        make.trim(),

                                                    model =
                                                        model.trim(),

                                                    motorization =
                                                        motorization.trim(),

                                                    power =
                                                        power.trim(),

                                                    year =
                                                        year.trim(),

                                                    plate =
                                                        plate.trim(),

                                                    vin =
                                                        vin.trim(),

                                                    imageUri =
                                                        imageUri
                                                )
                                            }

                                        onSave(

                                            vehicleToSave,

                                            editingName
                                        )

                                        /*
                                         * ==================================================
                                         * FORMULAR ZURÜCKSETZEN
                                         * ==================================================
                                         */

                                        editingVehicleId =
                                            ""

                                        editingName =
                                            ""

                                        name =
                                            ""

                                        vehicleType =
                                            "PKW"

                                        make =
                                            ""

                                        model =
                                            ""

                                        motorization =
                                            ""

                                        power =
                                            ""

                                        year =
                                            ""

                                        plate =
                                            ""

                                        vin =
                                            ""

                                        imageUri =
                                            ""

                                        adding =
                                            false
                                    }

                                ) {

                                    Text(
                                        "Speichern"
                                    )
                                }

                                OutlinedButton(

                                    onClick = {

                                        adding =
                                            false

                                        editingVehicleId =
                                            ""

                                        editingName =
                                            ""

                                        name =
                                            ""

                                        vehicleType =
                                            "PKW"

                                        make =
                                            ""

                                        model =
                                            ""

                                        motorization =
                                            ""

                                        power =
                                            ""

                                        year =
                                            ""

                                        plate =
                                            ""

                                        vin =
                                            ""

                                        imageUri =
                                            ""

                                        vehicleTypeExpanded =
                                            false
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
        }

        /*
         * ========================================================
         * NEUES FAHRZEUG
         * ========================================================
         */

        if (
            !adding &&
            vehicles.size < 5
        ) {

            Button(

                modifier =
                    Modifier.fillMaxWidth(),

                onClick = {

                    editingVehicleId =
                        ""

                    editingName =
                        ""

                    name =
                        ""

                    vehicleType =
                        "PKW"

                    make =
                        ""

                    model =
                        ""

                    motorization =
                        ""

                    power =
                        ""

                    year =
                        ""

                    plate =
                        ""

                    vin =
                        ""

                    imageUri =
                        ""

                    vehicleTypeExpanded =
                        false

                    adding =
                        true
                }

            ) {

                Text(
                    "Fahrzeug hinzufügen"
                )
            }
        }
    }
}
