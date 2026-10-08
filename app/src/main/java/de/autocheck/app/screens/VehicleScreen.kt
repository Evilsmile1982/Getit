package de.autocheck.app

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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

    var purchasePrice by remember {
        mutableStateOf("")
    }

    var purchasePriceVisible by remember {
        mutableStateOf(false)
    }

    var revealedPurchasePriceIds by remember {
        mutableStateOf(setOf<String>())
    }

    var vehicleToDelete by remember {
        mutableStateOf<Vehicle?>(null)
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

                                /*
                                 * ==================================================
                                 * KAUFPREIS
                                 * ==================================================
                                 */

                                val priceVisible =
                                    revealedPurchasePriceIds
                                        .contains(vehicle.id)

                                Row(
                                    verticalAlignment =
                                        Alignment.CenterVertically
                                ) {

                                    Text(
                                        "Kaufpreis: ",
                                        color =
                                            Color(
                                                0xFFB8BEC8
                                            )
                                    )

                                    Text(
                                        if (
                                            priceVisible &&
                                            vehicle.purchasePrice.isNotBlank()
                                        ) {
                                            "${vehicle.purchasePrice} €"
                                        } else {
                                            "******** €"
                                        },

                                        color =
                                            Color.White,

                                        fontWeight =
                                            FontWeight.Medium,

                                        modifier =
                                            Modifier.clickable {

                                                revealedPurchasePriceIds =
                                                    if (
                                                        priceVisible
                                                    ) {

                                                        revealedPurchasePriceIds -
                                                            vehicle.id

                                                    } else {

                                                        revealedPurchasePriceIds +
                                                            vehicle.id
                                                    }
                                            }
                                    )

                                    Spacer(
                                        Modifier.width(
                                            10.dp
                                        )
                                    )

                                    EyeIcon(
                                        visible =
                                            priceVisible,

                                        onClick = {

                                            revealedPurchasePriceIds =
                                                if (
                                                    priceVisible
                                                ) {

                                                    revealedPurchasePriceIds -
                                                        vehicle.id

                                                } else {

                                                    revealedPurchasePriceIds +
                                                        vehicle.id
                                                }
                                        }
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

                            /*
                             * ==================================================
                             * AKTIV-BUTTON
                             * ==================================================
                             *
                             * AKTIVES FAHRZEUG:
                             * GRÜN + "AKTIV"
                             *
                             * NICHT AKTIVES FAHRZEUG:
                             * GOLD + "Aktiv setzen"
                             * ==================================================
                             */

                            Button(

                                onClick = {

                                    onActiveVehicle(
                                        vehicle.id
                                    )
                                },

                                colors =
                                    ButtonDefaults.buttonColors(

                                        containerColor =
                                            if (
                                                activeVehicle ==
                                                vehicle.id
                                            ) {

                                                Color(
                                                    0xFF4CAF50
                                                )

                                            } else {

                                                Color(
                                                    0xFFFFD700
                                                )
                                            },

                                        contentColor =
                                            if (
                                                activeVehicle ==
                                                vehicle.id
                                            ) {

                                                Color.White

                                            } else {

                                                Color.Black
                                            }
                                    )

                            ) {

                                Text(

                                    if (
                                        activeVehicle ==
                                        vehicle.id
                                    ) {

                                        "AKTIV"

                                    } else {

                                        "Aktiv setzen"
                                    },

                                    fontWeight =
                                        FontWeight.Bold
                                )
                            }

                            /*
                             * ==================================================
                             * BEARBEITEN
                             * ==================================================
                             */

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

                                    purchasePrice =
                                        vehicle.purchasePrice

                                    purchasePriceVisible =
                                        false

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

                                vehicleToDelete =
                                    vehicle
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
                             * FAHRZEUGTYP
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
                             * KAUFPREIS
                             * ==================================================
                             */

                            OutlinedTextField(

                                value =
                                    purchasePrice,

                                onValueChange = { value ->

                                    purchasePrice =
                                        value.filter {
                                            it.isDigit() ||
                                                it == ',' ||
                                                it == '.'
                                        }
                                },

                                label = {
                                    Text(
                                        "Kaufpreis in €"
                                    )
                                },

                                placeholder = {
                                    Text(
                                        "z. B. 12500"
                                    )
                                },

                                singleLine =
                                    true,

                                keyboardOptions =
                                    KeyboardOptions(
                                        keyboardType =
                                            KeyboardType.Decimal
                                    ),

                                visualTransformation =
                                    if (
                                        purchasePriceVisible
                                    ) {

                                        VisualTransformation.None

                                    } else {

                                        AsteriskVisualTransformation
                                    },

                                trailingIcon = {

                                    EyeIcon(
                                        visible =
                                            purchasePriceVisible,

                                        onClick = {

                                            purchasePriceVisible =
                                                !purchasePriceVisible
                                        }
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

                                        val vehicleToSave =

                                            if (
                                                editingVehicleId.isNotBlank()
                                            ) {

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
                                                        imageUri,

                                                    purchasePrice =
                                                        purchasePrice.trim()
                                                )

                                            } else {

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
                                                        imageUri,

                                                    purchasePrice =
                                                        purchasePrice.trim()
                                                )
                                            }

                                        onSave(

                                            vehicleToSave,

                                            editingName
                                        )

                                        /*
                                         * Formular zurücksetzen
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

                                        purchasePrice =
                                            ""

                                        purchasePriceVisible =
                                            false

                                        vehicleTypeExpanded =
                                            false

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

                                        purchasePrice =
                                            ""

                                        purchasePriceVisible =
                                            false

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

                    purchasePrice =
                        ""

                    purchasePriceVisible =
                        false

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

    /*
     * ============================================================
     * LÖSCH-BESTÄTIGUNG
     * ============================================================
     */

    if (
        vehicleToDelete != null
    ) {

        val vehicle =
            vehicleToDelete!!

        AlertDialog(

            onDismissRequest = {

                vehicleToDelete =
                    null
            },

            title = {

                Text(
                    "Fahrzeug löschen?"
                )
            },

            text = {

                Text(
                    "Möchtest du das Fahrzeug „${vehicle.name}“ wirklich löschen? Alle gespeicherten Daten dieses Fahrzeugs werden entfernt."
                )
            },

            confirmButton = {

                TextButton(

                    onClick = {

                        onDelete(
                            vehicle
                        )

                        revealedPurchasePriceIds =
                            revealedPurchasePriceIds -
                                vehicle.id

                        vehicleToDelete =
                            null
                    }

                ) {

                    Text(
                        "Löschen"
                    )
                }
            },

            dismissButton = {

                TextButton(

                    onClick = {

                        vehicleToDelete =
                            null
                    }

                ) {

                    Text(
                        "Abbrechen"
                    )
                }
            }
        )
    }
}

/*
 * ================================================================
 * EIGENES AUGEN-SYMBOL
 * ================================================================
 */

@Composable
private fun EyeIcon(
    visible: Boolean,
    onClick: () -> Unit
) {

    Canvas(

        modifier =
            Modifier
                .size(34.dp)
                .clickable {
                    onClick()
                }

    ) {

        val eyeColor =
            Color(0xFFB8BEC8)

        val strokeWidth =
            2.dp.toPx()

        drawOval(

            color =
                eyeColor,

            style =
                Stroke(
                    width =
                        strokeWidth
                )
        )

        drawCircle(

            color =
                eyeColor,

            radius =
                4.dp.toPx()
        )

        if (visible) {

            drawLine(

                color =
                    eyeColor,

                start =
                    androidx.compose.ui.geometry.Offset(
                        x = 4.dp.toPx(),
                        y = 4.dp.toPx()
                    ),

                end =
                    androidx.compose.ui.geometry.Offset(
                        x =
                            size.width -
                                4.dp.toPx(),

                        y =
                            size.height -
                                4.dp.toPx()
                    ),

                strokeWidth =
                    strokeWidth,

                cap =
                    StrokeCap.Round
            )
        }
    }
}

/*
 * ================================================================
 * STERNCHEN-MASKIERUNG
 * ================================================================
 */

private object AsteriskVisualTransformation :
    VisualTransformation {

    override fun filter(
        text: AnnotatedString
    ): TransformedText {

        val maskedText =
            AnnotatedString(
                "*".repeat(
                    text.text.length
                )
            )

        return TransformedText(
            maskedText,
            OffsetMapping.Identity
        )
    }
}
