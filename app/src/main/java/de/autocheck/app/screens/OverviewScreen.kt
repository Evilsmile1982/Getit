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
fun OverviewScreen(
    store: VehicleStore,
    vehicles: List<Vehicle>,
    activeVehicle: String,
    onActiveVehicle: (String) -> Unit,
    onVisited: () -> Unit
) {

    LaunchedEffect(Unit) {
        onVisited()
    }

    val vehicle =
        vehicles.firstOrNull {
            it.name ==
                activeVehicle
        }

    val repairs =
        store.loadRepairs()
            .count {
                it.vehicle ==
                    activeVehicle
            }

    val maintenance =
        store.loadMaintenance()
            .count {
                it.vehicle ==
                    activeVehicle
            }

    val tires =
        store.loadTires()
            .count {
                it.vehicle ==
                    activeVehicle
            }

    val pickerl =
        store.loadPickerl()
            .firstOrNull {
                it.vehicle ==
                    activeVehicle
            }

    Column(
        Modifier.fillMaxSize()
    ) {

        VehicleSelector(
            vehicles,
            activeVehicle,
            onActiveVehicle
        )

        Spacer(
            Modifier.height(
                10.dp
            )
        )

        if (
            vehicle == null
        ) {

            EmptyCard(
                "Bitte zuerst ein Fahrzeug anlegen."
            )

            return@Column
        }

        LazyColumn(

            modifier =
                Modifier.fillMaxSize(),

            verticalArrangement =
                Arrangement.spacedBy(
                    10.dp
                )
        ) {

            item {

                CardForm {

                    Text(

                        vehicle.name,

                        color =
                            Color.White,

                        fontSize =
                            24.sp,

                        fontWeight =
                            FontWeight.Bold
                    )

                    Text(

                        "${vehicle.make} ${vehicle.model} ${vehicle.year}"
                            .trim(),

                        color =
                            Muted
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
                                Color.White
                        )
                    }

                    Spacer(
                        Modifier.height(
                            8.dp
                        )
                    )

                    Text(
                        "Reparaturen: $repairs",
                        color =
                            Color.White
                    )

                    Text(
                        "Wartungen: $maintenance",
                        color =
                            Color.White
                    )

                    Text(
                        "Reifensätze: $tires",
                        color =
                            Color.White
                    )

                    Text(

                        "Nächster Pickerl-Termin: ${
                            pickerl
                                ?.nextDate
                                ?.ifBlank {
                                    "nicht eingetragen"
                                }
                                ?: "nicht eingetragen"
                        }",

                        color =
                            Color.White
                    )
                }
            }

            item {

                vehicle.imageUri
                    .takeIf {
                        it.isNotBlank()
                    }
                    ?.let { uri ->

                        VehicleImage(

                            uri =
                                uri,

                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .height(
                                        180.dp
                                    )
                        )
                    }
            }
        }
    }
}
