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

    var activeVehicle by remember {

        mutableStateOf(
            store.activeVehicle()
        )
    }

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

    LaunchedEffect(Unit) {

        kotlinx.coroutines
            .delay(
                300
            )

        homeReady = true

        if (
            activeVehicle.isBlank() &&
            vehicles.isNotEmpty()
        ) {

            activeVehicle =
                vehicles.first().name

            store.setActiveVehicle(
                activeVehicle
            )
        }
    }

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
                screen = it
            }
        )

    } else {

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

                Screen.AUTO ->

                    VehicleScreen(

                        vehicles =
                            vehicles,

                        activeVehicle =
                            activeVehicle,

                        onActiveVehicle = {
                            activeVehicle =
                                it

                            store.setActiveVehicle(
                                it
                            )
                        },

                        onSave = {
                                vehicle,
                                oldName ->

                            val index =
                                vehicles.indexOfFirst {
                                    it.name ==
                                        oldName
                                }

                            if (
                                index >= 0
                            ) {

                                vehicles[index] =
                                    vehicle

                                if (
                                    oldName !=
                                    vehicle.name
                                ) {

                                    migrateVehicleName(
                                        store,
                                        oldName,
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

                            activeVehicle =
                                vehicle.name

                            store.setActiveVehicle(
                                activeVehicle
                            )
                        },

                        onDelete = {
                                vehicle ->

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

                            if (
                                activeVehicle ==
                                vehicle.name
                            ) {

                                activeVehicle =
                                    vehicles
                                        .firstOrNull()
                                        ?.name
                                        ?: ""

                                store.setActiveVehicle(
                                    activeVehicle
                                )
                            }
                        }
                    )

                Screen.REPARATUREN ->

                    RepairScreen(

                        store =
                            store,

                        vehicles =
                            vehicles,

                        activeVehicle =
                            activeVehicle,

                        onActiveVehicle = {

                            activeVehicle =
                                it

                            store.setActiveVehicle(
                                it
                            )
                        },

                        onVisited = {

                            visitedStore.markVisited(
                                Screen.REPARATUREN
                            )
                        }
                    )

                Screen.PICKERL ->

                    PickerlScreen(

                        store =
                            store,

                        vehicles =
                            vehicles,

                        activeVehicle =
                            activeVehicle,

                        onActiveVehicle = {

                            activeVehicle =
                                it

                            store.setActiveVehicle(
                                it
                            )
                        },

                        onVisited = {

                            visitedStore.markVisited(
                                Screen.PICKERL
                            )
                        }
                    )

                Screen.WARTUNGEN ->

                    MaintenanceScreen(

                        store =
                            store,

                        vehicles =
                            vehicles,

                        activeVehicle =
                            activeVehicle,

                        onActiveVehicle = {

                            activeVehicle =
                                it

                            store.setActiveVehicle(
                                it
                            )
                        },

                        onVisited = {

                            visitedStore.markVisited(
                                Screen.WARTUNGEN
                            )
                        }
                    )

                Screen.GESAMTBLICK ->

                    OverviewScreen(

                        store =
                            store,

                        vehicles =
                            vehicles,

                        activeVehicle =
                            activeVehicle,

                        onActiveVehicle = {

                            activeVehicle =
                                it

                            store.setActiveVehicle(
                                it
                            )
                        },

                        onVisited = {

                            visitedStore.markVisited(
                                Screen.GESAMTBLICK
                            )
                        }
                    )

                Screen.REIFEN ->

                    TireScreen(

                        store =
                            store,

                        vehicles =
                            vehicles,

                        activeVehicle =
                            activeVehicle,

                        onActiveVehicle = {

                            activeVehicle =
                                it

                            store.setActiveVehicle(
                                it
                            )
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
