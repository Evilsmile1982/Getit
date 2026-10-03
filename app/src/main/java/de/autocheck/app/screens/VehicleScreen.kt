package de.autocheck.app

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CarRepair
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.TireRepair
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.autocheck.app.ui.AutoCheckTheme
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.ZoneId

@Composable
fun VehicleScreen(
    vehicles: MutableList<Vehicle>,
    activeVehicle: String,
    onActiveVehicle: (String) -> Unit,
    onSave: (Vehicle, String) -> Unit,
    onDelete: (Vehicle) -> Unit
) {

    var adding by remember {
        mutableStateOf(
            false
        )
    }

    var editingName by remember {
        mutableStateOf(
            ""
        )
    }

    var name by remember {
        mutableStateOf(
            ""
        )
    }

    var make by remember {
        mutableStateOf(
            ""
        )
    }

    var model by remember {
        mutableStateOf(
            ""
        )
    }

    var year by remember {
        mutableStateOf(
            ""
        )
    }

    var plate by remember {
        mutableStateOf(
            ""
        )
    }

    var vin by remember {
        mutableStateOf(
            ""
        )
    }

    var imageUri by remember {
        mutableStateOf(
            ""
        )
    }

    val context =
        LocalContext.current

    val launcher =
        androidx.activity.compose
            .rememberLauncherForActivityResult(

                ActivityResultContracts
                    .OpenDocument()

            ) { uri ->

                if (
                    uri != null
                ) {

                    try {

                        context
                            .contentResolver
                            .takePersistableUriPermission(

                                uri,

                                Intent
                                    .FLAG_GRANT_READ_URI_PERMISSION
                            )

                    } catch (
                        _: SecurityException
                    ) {
                    }

                    imageUri =
                        uri.toString()
                }
            }

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

        if (
            vehicles.isEmpty()
        ) {

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
                vehicles
            ) { vehicle ->

                Card(

                    colors =
                        CardDefaults
                            .cardColors(
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

                                    "${vehicle.make} ${vehicle.model} ${vehicle.year}"
                                        .trim(),

                                    color =
                                        Color(
                                            0xFFB8BEC8
                                        )
                                )

                                if (
                                    vehicle.plate
                                        .isNotBlank()
                                ) {

                                    Text(

                                        "Kennzeichen: ${vehicle.plate}",

                                        color =
                                            Color.White
                                    )
                                }

                                if (
                                    vehicle.vin
                                        .isNotBlank()
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

                        Row(

                            horizontalArrangement =
                                Arrangement.spacedBy(
                                    8.dp
                                )
                        ) {

                            Button(

                                onClick = {

                                    onActiveVehicle(
                                        vehicle.name
                                    )
                                }

                            ) {

                                Text(

                                    if (
                                        activeVehicle ==
                                        vehicle.name
                                    ) {
                                        "Aktiv"
                                    } else {
                                        "Aktiv setzen"
                                    }
                                )
                            }

                            OutlinedButton(

                                onClick = {

                                    editingName =
                                        vehicle.name

                                    name =
                                        vehicle.name

                                    make =
                                        vehicle.make

                                    model =
                                        vehicle.model

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

            if (
                adding
            ) {

                item {

                    Card(

                        colors =
                            CardDefaults
                                .cardColors(
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
                                    editingName
                                        .isBlank()
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

                            OutlinedTextField(

                                name,

                                {
                                    name =
                                        it
                                },

                                label = {
                                    Text(
                                        "Bezeichnung"
                                    )
                                },

                                modifier =
                                    Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(

                                make,

                                {
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

                            OutlinedTextField(

                                model,

                                {
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

                            OutlinedTextField(

                                year,

                                {
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

                            OutlinedTextField(

                                plate,

                                {
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

                            OutlinedTextField(

                                vin,

                                {
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
                                imageUri
                                    .isNotBlank()
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

                            Row(

                                horizontalArrangement =
                                    Arrangement.spacedBy(
                                        8.dp
                                    )
                            ) {

                                Button(

                                    enabled =
                                        name
                                            .isNotBlank(),

                                    onClick = {

                                        onSave(

                                            Vehicle(

                                                name =
                                                    name.trim(),

                                                make =
                                                    make.trim(),

                                                model =
                                                    model.trim(),

                                                year =
                                                    year.trim(),

                                                plate =
                                                    plate.trim(),

                                                vin =
                                                    vin.trim(),

                                                imageUri =
                                                    imageUri
                                            ),

                                            editingName
                                        )

                                        name =
                                            ""

                                        make =
                                            ""

                                        model =
                                            ""

                                        year =
                                            ""

                                        plate =
                                            ""

                                        vin =
                                            ""

                                        imageUri =
                                            ""

                                        editingName =
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

                                        editingName =
                                            ""
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

        if (
            !adding &&
            vehicles.size < 5
        ) {

            Button(

                modifier =
                    Modifier.fillMaxWidth(),

                onClick = {

                    editingName =
                        ""

                    name =
                        ""

                    make =
                        ""

                    model =
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
