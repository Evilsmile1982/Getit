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
        mutableStateOf(
            false
        )
    }

    var date by remember {
        mutableStateOf(
            ""
        )
    }

    var mileage by remember {
        mutableStateOf(
            ""
        )
    }

    var description by remember {
        mutableStateOf(
            ""
        )
    }

    var cost by remember {
        mutableStateOf(
            ""
        )
    }

    var workshop by remember {
        mutableStateOf(
            ""
        )
    }

    var repairs by remember {
        mutableStateOf(
            store.loadRepairs()
        )
    }

    val list =
        repairs.filter {
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
            vehicles.isEmpty()
        ) return@Column

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

            if (
                list.isEmpty()
            ) {

                item {

                    EmptyCard(
                        "Noch keine Reparatur eingetragen."
                    )
                }
            }

            items(
                list
            ) { repair ->

                RecordCard(

                    title =
                        repair.description
                            .ifBlank {
                                "Reparatur"
                            },

                    lines =
                        listOf(

                            "Datum: ${repair.date}",

                            "Kilometerstand: ${repair.mileage}",

                            "Kosten: ${repair.cost}",

                            "Werkstatt: ${repair.workshop}"
                        ),

                    onDelete = {

                        repairs =
                            repairs.filterNot {
                                it.id ==
                                    repair.id
                            }

                        store.saveRepairs(
                            repairs
                        )
                    }
                )
            }

            if (
                showForm
            ) {

                item {

                    CardForm {

                        FormField(
                            "Datum (YYYY-MM-DD)",
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
                                                date.trim(),

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

                                date =
                                    ""

                                mileage =
                                    ""

                                description =
                                    ""

                                cost =
                                    ""

                                workshop =
                                    ""

                                showForm =
                                    false
                            },

                            onCancel = {
                                showForm =
                                    false
                            }
                        )
                    }
                }
            }
        }

        if (
            !showForm
        ) {

            Button(

                modifier =
                    Modifier.fillMaxWidth(),

                onClick = {
                    showForm =
                        true
                }

            ) {

                Text(
                    "Reparatur hinzufügen"
                )
            }
        }
    }
}
