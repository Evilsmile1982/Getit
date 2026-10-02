package de.autocheck.app

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CarRepair
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
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

// ============================================================
// DATENMODELLE
// ============================================================

data class Vehicle(
    val name: String,
    val make: String,
    val model: String,
    val year: String,
    val plate: String = "",
    val vin: String = "",
    val imageUri: String = ""
)

data class Repair(
    val id: Long,
    val vehicleName: String,
    val date: String,
    val mileage: String,
    val description: String,
    val cost: String,
    val workshop: String
)

data class Maintenance(
    val id: Long,
    val vehicleName: String,
    val date: String,
    val mileage: String,
    val cost: String,
    val workshop: String,
    val notes: String
)

data class Pickerl(
    val vehicleName: String,
    val lastDate: String,
    val nextDate: String,
    val notes: String,
    val reminder: Boolean
)

data class TireSet(
    val id: Long,
    val vehicleName: String,
    val season: String,
    val dimension: String,
    val brand: String,
    val dot: String,
    val treadDepth: String,
    val condition: String,
    val storage: String
)

private enum class Screen {
    HOME,
    AUTO,
    REPARATUREN,
    PICKERL,
    WARTUNGEN,
    GESAMTBLICK,
    REIFEN
}

private data class MenuItemData(
    val title: String,
    val subtitle: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val screen: Screen
)

// ============================================================
// SPEICHER
// ============================================================

class VehicleStore(
    context: Context
) {

    private val prefs =
        context.getSharedPreferences(
            "autocheck",
            Context.MODE_PRIVATE
        )

    fun loadVehicles(): List<Vehicle> {

        val raw =
            prefs.getString(
                "vehicles",
                "[]"
            ) ?: "[]"

        val array =
            try {
                JSONArray(raw)
            } catch (_: Exception) {
                JSONArray()
            }

        return buildList {

            for (i in 0 until array.length()) {

                val o =
                    array.optJSONObject(i)
                        ?: continue

                add(
                    Vehicle(
                        name =
                            o.optString("name"),
                        make =
                            o.optString("make"),
                        model =
                            o.optString("model"),
                        year =
                            o.optString("year"),
                        plate =
                            o.optString("plate"),
                        vin =
                            o.optString("vin"),
                        imageUri =
                            o.optString("imageUri")
                    )
                )
            }
        }
    }

    fun saveVehicles(
        list: List<Vehicle>
    ) {

        val array =
            JSONArray()

        list.forEach { vehicle ->

            array.put(
                JSONObject().apply {

                    put(
                        "name",
                        vehicle.name
                    )

                    put(
                        "make",
                        vehicle.make
                    )

                    put(
                        "model",
                        vehicle.model
                    )

                    put(
                        "year",
                        vehicle.year
                    )

                    put(
                        "plate",
                        vehicle.plate
                    )

                    put(
                        "vin",
                        vehicle.vin
                    )

                    put(
                        "imageUri",
                        vehicle.imageUri
                    )
                }
            )
        }

        prefs.edit()
            .putString(
                "vehicles",
                array.toString()
            )
            .apply()
    }

    fun loadRepairs(): List<Repair> {

        val raw =
            prefs.getString(
                "repairs",
                "[]"
            ) ?: "[]"

        val array =
            try {
                JSONArray(raw)
            } catch (_: Exception) {
                JSONArray()
            }

        return buildList {

            for (i in 0 until array.length()) {

                val o =
                    array.optJSONObject(i)
                        ?: continue

                add(
                    Repair(
                        id =
                            o.optLong(
                                "id"
                            ),
                        vehicleName =
                            o.optString(
                                "vehicleName"
                            ),
                        date =
                            o.optString(
                                "date"
                            ),
                        mileage =
                            o.optString(
                                "mileage"
                            ),
                        description =
                            o.optString(
                                "description"
                            ),
                        cost =
                            o.optString(
                                "cost"
                            ),
                        workshop =
                            o.optString(
                                "workshop"
                            )
                    )
                )
            }
        }
    }

    fun saveRepairs(
        list: List<Repair>
    ) {

        val array =
            JSONArray()

        list.forEach { repair ->

            array.put(
                JSONObject().apply {

                    put(
                        "id",
                        repair.id
                    )

                    put(
                        "vehicleName",
                        repair.vehicleName
                    )

                    put(
                        "date",
                        repair.date
                    )

                    put(
                        "mileage",
                        repair.mileage
                    )

                    put(
                        "description",
                        repair.description
                    )

                    put(
                        "cost",
                        repair.cost
                    )

                    put(
                        "workshop",
                        repair.workshop
                    )
                }
            )
        }

        prefs.edit()
            .putString(
                "repairs",
                array.toString()
            )
            .apply()
    }

    fun loadMaintenances(): List<Maintenance> {

        val raw =
            prefs.getString(
                "maintenances",
                "[]"
            ) ?: "[]"

        val array =
            try {
                JSONArray(raw)
            } catch (_: Exception) {
                JSONArray()
            }

        return buildList {

            for (i in 0 until array.length()) {

                val o =
                    array.optJSONObject(i)
                        ?: continue

                add(
                    Maintenance(
                        id =
                            o.optLong(
                                "id"
                            ),
                        vehicleName =
                            o.optString(
                                "vehicleName"
                            ),
                        date =
                            o.optString(
                                "date"
                            ),
                        mileage =
                            o.optString(
                                "mileage"
                            ),
                        cost =
                            o.optString(
                                "cost"
                            ),
                        workshop =
                            o.optString(
                                "workshop"
                            ),
                        notes =
                            o.optString(
                                "notes"
                            )
                    )
                )
            }
        }
    }

    fun saveMaintenances(
        list: List<Maintenance>
    ) {

        val array =
            JSONArray()

        list.forEach { item ->

            array.put(
                JSONObject().apply {

                    put(
                        "id",
                        item.id
                    )

                    put(
                        "vehicleName",
                        item.vehicleName
                    )

                    put(
                        "date",
                        item.date
                    )

                    put(
                        "mileage",
                        item.mileage
                    )

                    put(
                        "cost",
                        item.cost
                    )

                    put(
                        "workshop",
                        item.workshop
                    )

                    put(
                        "notes",
                        item.notes
                    )
                }
            )
        }

        prefs.edit()
            .putString(
                "maintenances",
                array.toString()
            )
            .apply()
    }

    fun loadPickerls(): List<Pickerl> {

        val raw =
            prefs.getString(
                "pickerls",
                "[]"
            ) ?: "[]"

        val array =
            try {
                JSONArray(raw)
            } catch (_: Exception) {
                JSONArray()
            }

        return buildList {

            for (i in 0 until array.length()) {

                val o =
                    array.optJSONObject(i)
                        ?: continue

                add(
                    Pickerl(
                        vehicleName =
                            o.optString(
                                "vehicleName"
                            ),
                        lastDate =
                            o.optString(
                                "lastDate"
                            ),
                        nextDate =
                            o.optString(
                                "nextDate"
                            ),
                        notes =
                            o.optString(
                                "notes"
                            ),
                        reminder =
                            o.optBoolean(
                                "reminder",
                                true
                            )
                    )
                )
            }
        }
    }

    fun savePickerls(
        list: List<Pickerl>
    ) {

        val array =
            JSONArray()

        list.forEach { item ->

            array.put(
                JSONObject().apply {

                    put(
                        "vehicleName",
                        item.vehicleName
                    )

                    put(
                        "lastDate",
                        item.lastDate
                    )

                    put(
                        "nextDate",
                        item.nextDate
                    )

                    put(
                        "notes",
                        item.notes
                    )

                    put(
                        "reminder",
                        item.reminder
                    )
                }
            )
        }

        prefs.edit()
            .putString(
                "pickerls",
                array.toString()
            )
            .apply()
    }

    fun loadTires(): List<TireSet> {

        val raw =
            prefs.getString(
                "tires",
                "[]"
            ) ?: "[]"

        val array =
            try {
                JSONArray(raw)
            } catch (_: Exception) {
                JSONArray()
            }

        return buildList {

            for (i in 0 until array.length()) {

                val o =
                    array.optJSONObject(i)
                        ?: continue

                add(
                    TireSet(
                        id =
                            o.optLong(
                                "id"
                            ),
                        vehicleName =
                            o.optString(
                                "vehicleName"
                            ),
                        season =
                            o.optString(
                                "season"
                            ),
                        dimension =
                            o.optString(
                                "dimension"
                            ),
                        brand =
                            o.optString(
                                "brand"
                            ),
                        dot =
                            o.optString(
                                "dot"
                            ),
                        treadDepth =
                            o.optString(
                                "treadDepth"
                            ),
                        condition =
                            o.optString(
                                "condition"
                            ),
                        storage =
                            o.optString(
                                "storage"
                            )
                    )
                )
            }
        }
    }

    fun saveTires(
        list: List<TireSet>
    ) {

        val array =
            JSONArray()

        list.forEach { item ->

            array.put(
                JSONObject().apply {

                    put(
                        "id",
                        item.id
                    )

                    put(
                        "vehicleName",
                        item.vehicleName
                    )

                    put(
                        "season",
                        item.season
                    )

                    put(
                        "dimension",
                        item.dimension
                    )

                    put(
                        "brand",
                        item.brand
                    )

                    put(
                        "dot",
                        item.dot
                    )

                    put(
                        "treadDepth",
                        item.treadDepth
                    )

                    put(
                        "condition",
                        item.condition
                    )

                    put(
                        "storage",
                        item.storage
                    )
                }
            )
        }

        prefs.edit()
            .putString(
                "tires",
                array.toString()
            )
            .apply()
    }

    fun loadActiveVehicle(): String {

        return prefs.getString(
            "activeVehicle",
            ""
        ) ?: ""
    }

    fun saveActiveVehicle(
        name: String
    ) {

        prefs.edit()
            .putString(
                "activeVehicle",
                name
            )
            .apply()
    }
}

// ============================================================
// ACTIVITY
// ============================================================

class MainActivity :
    ComponentActivity() {

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(
            savedInstanceState
        )

        createNotificationChannel()

        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.TIRAMISU &&
            checkSelfPermission(
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            requestPermissions(
                arrayOf(
                    Manifest.permission
                        .POST_NOTIFICATIONS
                ),
                1001
            )
        }

        setContent {

            AutoCheckTheme {

                AutoCheckApp()
            }
        }
    }

    private fun createNotificationChannel() {

        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.O
        ) {

            val channel =
                NotificationChannel(
                    "pickerl",
                    "Pickerl Erinnerungen",
                    NotificationManager
                        .IMPORTANCE_DEFAULT
                )

            val manager =
                getSystemService(
                    NotificationManager::class.java
                )

            manager.createNotificationChannel(
                channel
            )
        }
    }
}

// ============================================================
// PICKERL ERINNERUNG
// ============================================================

class PickerlReceiver :
    BroadcastReceiver() {

    override fun onReceive(
        context: Context,
        intent: Intent?
    ) {

        val vehicle =
            intent?.getStringExtra(
                "vehicle"
            )
                ?: "Dein Fahrzeug"

        val manager =
            context.getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager

        val notification =
            if (
                Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.O
            ) {

                android.app.Notification
                    .Builder(
                        context,
                        "pickerl"
                    )
                    .setSmallIcon(
                        android.R.drawable
                            .ic_dialog_info
                    )
                    .setContentTitle(
                        "Pickerl-Erinnerung"
                    )
                    .setContentText(
                        "Das Pickerl für $vehicle ist in 3 Monaten fällig."
                    )
                    .setAutoCancel(true)
                    .build()

            } else {

                @Suppress("DEPRECATION")
                android.app.Notification
                    .Builder(context)
                    .setSmallIcon(
                        android.R.drawable
                            .ic_dialog_info
                    )
                    .setContentTitle(
                        "Pickerl-Erinnerung"
                    )
                    .setContentText(
                        "Das Pickerl für $vehicle ist in 3 Monaten fällig."
                    )
                    .setAutoCancel(true)
                    .build()
            }

        manager.notify(
            vehicle.hashCode(),
            notification
        )
    }
}

// ============================================================
// HAUPTAPP
// ============================================================

@Composable
private fun AutoCheckApp() {

    val context =
        LocalContext.current

    val store =
        remember {
            VehicleStore(context)
        }

    val vehicles =
        remember {
            mutableStateListOf<Vehicle>()
                .apply {
                    addAll(
                        store.loadVehicles()
                    )
                }
        }

    val repairs =
        remember {
            mutableStateListOf<Repair>()
                .apply {
                    addAll(
                        store.loadRepairs()
                    )
                }
        }

    val maintenances =
        remember {
            mutableStateListOf<Maintenance>()
                .apply {
                    addAll(
                        store.loadMaintenances()
                    )
                }
        }

    val pickerls =
        remember {
            mutableStateListOf<Pickerl>()
                .apply {
                    addAll(
                        store.loadPickerls()
                    )
                }
        }

    val tires =
        remember {
            mutableStateListOf<TireSet>()
                .apply {
                    addAll(
                        store.loadTires()
                    )
                }
        }

    var activeVehicle by remember {

        mutableStateOf(
            store.loadActiveVehicle()
        )
    }

    var screen by remember {
        mutableStateOf(
            Screen.HOME
        )
    }

    var homeReady by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(Unit) {

        kotlinx.coroutines.delay(
            2500
        )

        homeReady = true
    }

    if (
        activeVehicle.isBlank() &&
        vehicles.isNotEmpty()
    ) {

        activeVehicle =
            vehicles.first().name

        store.saveActiveVehicle(
            activeVehicle
        )
    }

    if (
        screen == Screen.HOME
    ) {

        HomeScreen(
            visible =
                homeReady,
            onSelect = {
                screen = it
            }
        )

    } else {

        DetailScaffold(
            title =
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

                    else ->
                        "AutoCheck"
                },

            onBack = {
                screen =
                    Screen.HOME
            }
        ) {

            when (screen) {

                Screen.AUTO -> {

                    VehicleScreen(
                        vehicles =
                            vehicles,
                        activeVehicle =
                            activeVehicle,
                        onActiveVehicle = {
                            activeVehicle =
                                it

                            store.saveActiveVehicle(
                                it
                            )
                        },
                        onAdd = {
                            vehicle ->

                            if (
                                vehicles.size < 5 &&
                                vehicles.none {
                                    it.name.equals(
                                        vehicle.name,
                                        true
                                    )
                                }
                            ) {

                                vehicles.add(
                                    vehicle
                                )

                                store.saveVehicles(
                                    vehicles
                                )

                                if (
                                    activeVehicle
                                        .isBlank()
                                ) {

                                    activeVehicle =
                                        vehicle.name

                                    store.saveActiveVehicle(
                                        activeVehicle
                                    )
                                }
                            }
                        },
                        onUpdate = {
                            index,
                            vehicle ->

                            if (
                                index in
                                vehicles.indices
                            ) {

                                val oldName =
                                    vehicles[
                                        index
                                    ].name

                                vehicles[
                                    index
                                ] =
                                    vehicle

                                if (
                                    activeVehicle ==
                                    oldName
                                ) {

                                    activeVehicle =
                                        vehicle.name

                                    store.saveActiveVehicle(
                                        activeVehicle
                                    )
                                }

                                for (
                                    i in repairs.indices
                                ) {

                                    if (
                                        repairs[i]
                                            .vehicleName ==
                                        oldName
                                    ) {

                                        repairs[i] =
                                            repairs[i]
                                                .copy(
                                                    vehicleName =
                                                        vehicle.name
                                                )
                                    }
                                }

                                for (
                                    i in
                                    maintenances.indices
                                ) {

                                    if (
                                        maintenances[i]
                                            .vehicleName ==
                                        oldName
                                    ) {

                                        maintenances[i] =
                                            maintenances[i]
                                                .copy(
                                                    vehicleName =
                                                        vehicle.name
                                                )
                                    }
                                }

                                for (
                                    i in
                                    pickerls.indices
                                ) {

                                    if (
                                        pickerls[i]
                                            .vehicleName ==
                                        oldName
                                    ) {

                                        pickerls[i] =
                                            pickerls[i]
                                                .copy(
                                                    vehicleName =
                                                        vehicle.name
                                                )
                                    }
                                }

                                for (
                                    i in tires.indices
                                ) {

                                    if (
                                        tires[i]
                                            .vehicleName ==
                                        oldName
                                    ) {

                                        tires[i] =
                                            tires[i]
                                                .copy(
                                                    vehicleName =
                                                        vehicle.name
                                                )
                                    }
                                }

                                store.saveVehicles(
                                    vehicles
                                )

                                store.saveRepairs(
                                    repairs
                                )

                                store.saveMaintenances(
                                    maintenances
                                )

                                store.savePickerls(
                                    pickerls
                                )

                                store.saveTires(
                                    tires
                                )
                            }
                        },
                        onDelete = {
                            index ->

                            if (
                                index in
                                vehicles.indices
                            ) {

                                val name =
                                    vehicles[
                                        index
                                    ].name

                                vehicles.removeAt(
                                    index
                                )

                                repairs.removeAll {
                                    it.vehicleName ==
                                        name
                                }

                                maintenances.removeAll {
                                    it.vehicleName ==
                                        name
                                }

                                pickerls.removeAll {
                                    it.vehicleName ==
                                        name
                                }

                                tires.removeAll {
                                    it.vehicleName ==
                                        name
                                }

                                if (
                                    activeVehicle ==
                                    name
                                ) {

                                    activeVehicle =
                                        vehicles
                                            .firstOrNull()
                                            ?.name
                                            ?: ""

                                    store.saveActiveVehicle(
                                        activeVehicle
                                    )
                                }

                                store.saveVehicles(
                                    vehicles
                                )

                                store.saveRepairs(
                                    repairs
                                )

                                store.saveMaintenances(
                                    maintenances
                                )

                                store.savePickerls(
                                    pickerls
                                )

                                store.saveTires(
                                    tires
                                )
                            }
                        }
                    )
                }

                Screen.REPARATUREN -> {

                    RepairsScreen(
                        vehicles =
                            vehicles,
                        repairs =
                            repairs,
                        selectedVehicle =
                            activeVehicle,
                        onVehicleSelected = {
                            activeVehicle =
                                it

                            store.saveActiveVehicle(
                                it
                            )
                        },
                        onSave = {
                            item ->

                            repairs.add(
                                item
                            )

                            store.saveRepairs(
                                repairs
                            )
                        },
                        onUpdate = {
                            item ->

                            val index =
                                repairs.indexOfFirst {
                                    it.id ==
                                        item.id
                                }

                            if (
                                index >= 0
                            ) {

                                repairs[
                                    index
                                ] =
                                    item

                                store.saveRepairs(
                                    repairs
                                )
                            }
                        },
                        onDelete = {
                            item ->

                            repairs.removeAll {
                                it.id ==
                                    item.id
                            }

                            store.saveRepairs(
                                repairs
                            )
                        }
                    )
                }

                Screen.PICKERL -> {

                    PickerlScreen(
                        vehicles =
                            vehicles,
                        pickerls =
                            pickerls,
                        selectedVehicle =
                            activeVehicle,
                        onVehicleSelected = {
                            activeVehicle =
                                it

                            store.saveActiveVehicle(
                                it
                            )
                        },
                        onSave = {
                            item ->

                            pickerls.removeAll {
                                it.vehicleName ==
                                    item.vehicleName
                            }

                            pickerls.add(
                                item
                            )

                            store.savePickerls(
                                pickerls
                            )

                            if (
                                item.reminder &&
                                item.nextDate
                                    .isNotBlank()
                            ) {

                                schedulePickerlReminder(
                                    context =
                                        context,
                                    vehicleName =
                                        item.vehicleName,
                                    nextDate =
                                        item.nextDate
                                )
                            }
                        }
                    )
                }

                Screen.WARTUNGEN -> {

                    MaintenanceScreen(
                        vehicles =
                            vehicles,
                        maintenances =
                            maintenances,
                        selectedVehicle =
                            activeVehicle,
                        onVehicleSelected = {
                            activeVehicle =
                                it

                            store.saveActiveVehicle(
                                it
                            )
                        },
                        onSave = {
                            item ->

                            maintenances.add(
                                item
                            )

                            store.saveMaintenances(
                                maintenances
                            )
                        },
                        onUpdate = {
                            item ->

                            val index =
                                maintenances
                                    .indexOfFirst {
                                        it.id ==
                                            item.id
                                    }

                            if (
                                index >= 0
                            ) {

                                maintenances[
                                    index
                                ] =
                                    item

                                store.saveMaintenances(
                                    maintenances
                                )
                            }
                        },
                        onDelete = {
                            item ->

                            maintenances.removeAll {
                                it.id ==
                                    item.id
                            }

                            store.saveMaintenances(
                                maintenances
                            )
                        }
                    )
                }

                Screen.GESAMTBLICK -> {

                    OverviewScreen(
                        vehicles =
                            vehicles,
                        repairs =
                            repairs,
                        maintenances =
                            maintenances,
                        pickerls =
                            pickerls,
                        tires =
                            tires,
                        selectedVehicle =
                            activeVehicle,
                        onVehicleSelected = {
                            activeVehicle =
                                it

                            store.saveActiveVehicle(
                                it
                            )
                        }
                    )
                }

                Screen.REIFEN -> {

                    TiresScreen(
                        vehicles =
                            vehicles,
                        tires =
                            tires,
                        selectedVehicle =
                            activeVehicle,
                        onVehicleSelected = {
                            activeVehicle =
                                it

                            store.saveActiveVehicle(
                                it
                            )
                        },
                        onSave = {
                            item ->

                            tires.add(
                                item
                            )

                            store.saveTires(
                                tires
                            )
                        },
                        onUpdate = {
                            item ->

                            val index =
                                tires.indexOfFirst {
                                    it.id ==
                                        item.id
                                }

                            if (
                                index >= 0
                            ) {

                                tires[
                                    index
                                ] =
                                    item

                                store.saveTires(
                                    tires
                                )
                            }
                        },
                        onDelete = {
                            item ->

                            tires.removeAll {
                                it.id ==
                                    item.id
                            }

                            store.saveTires(
                                tires
                            )
                        }
                    )
                }

                else -> Unit
            }
        }
    }
}

// ============================================================
// ORIGINAL CUT1 HOME SCREEN
// ============================================================

@Composable
private fun HomeScreen(
    visible: Boolean,
    onSelect: (Screen) -> Unit
) {

    val items =
        listOf(

            MenuItemData(
                "Mein Auto",
                "Fahrzeug & Details",
                Icons.Filled.DirectionsCar,
                Screen.AUTO
            ),

            MenuItemData(
                "Reparaturen",
                "Reparaturen verwalten",
                Icons.Filled.CarRepair,
                Screen.REPARATUREN
            ),

            MenuItemData(
                "Pickerl/TÜV",
                "Termine & Fristen",
                Icons.Filled.Event,
                Screen.PICKERL
            ),

            MenuItemData(
                "Wartungen",
                "Verschiedenes",
                Icons.Filled.Build,
                Screen.WARTUNGEN
            ),

            MenuItemData(
                "Gesamtblick",
                "Die wichtigsten Infos",
                Icons.Filled.Visibility,
                Screen.GESAMTBLICK
            ),

            MenuItemData(
                "Reifen",
                "Größen, Dimensionen und Alter",
                Icons.Filled.TireRepair,
                Screen.REIFEN
            )
        )

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(
                    Color(0xFF050608)
                )
                .padding(
                    horizontal = 14.dp
                )
    ) {

        Column(
            modifier =
                Modifier.fillMaxSize(),
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            Spacer(
                Modifier.height(
                    28.dp
                )
            )

            Text(
                "AutoCheck",
                fontSize = 30.sp,
                fontWeight =
                    FontWeight.Bold,
                color =
                    Color.White
            )

            Spacer(
                Modifier.height(
                    8.dp
                )
            )

            Image(
                painter =
                    painterResource(
                        R.drawable.bild_3
                    ),
                contentDescription =
                    "AutoCheck Hauptbild",
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .weight(1f),
                contentScale =
                    ContentScale.Fit
            )

            Spacer(
                Modifier.height(
                    10.dp
                )
            )

            Column(
                modifier =
                    Modifier.fillMaxWidth(),
                verticalArrangement =
                    Arrangement.spacedBy(
                        10.dp
                    )
            ) {

                for (
                    row in 0..2
                ) {

                    Row(
                        modifier =
                            Modifier.fillMaxWidth(),
                        horizontalArrangement =
                            Arrangement.spacedBy(
                                10.dp
                            )
                    ) {

                        for (
                            col in 0..1
                        ) {

                            val item =
                                items[
                                    row * 2 +
                                        col
                                ]

                            AnimatedVisibility(
                                visible =
                                    visible,
                                enter =
                                    slideInHorizontally(
                                        animationSpec =
                                            tween(
                                                2500
                                            ),
                                        initialOffsetX = {
                                            if (
                                                col == 0
                                            ) {
                                                -it
                                            } else {
                                                it
                                            }
                                        }
                                    )
                            ) {

                                MenuCard(
                                    item =
                                        item,
                                    modifier =
                                        Modifier.weight(
                                            1f
                                        ),
                                    onClick = {
                                        onSelect(
                                            item.screen
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(
                Modifier.height(
                    20.dp
                )
            )
        }
    }
}

@Composable
private fun MenuCard(
    item: MenuItemData,
    modifier: Modifier,
    onClick: () -> Unit
) {

    Card(
        modifier =
            modifier.clickable(
                onClick =
                    onClick
            ),
        shape =
            RoundedCornerShape(
                16.dp
            ),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    Color(0xFF11141A)
            )
    ) {

        Row(
            modifier =
                Modifier.padding(
                    13.dp
                ),
            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Box(
                modifier =
                    Modifier
                        .size(
                            46.dp
                        )
                        .clip(
                            RoundedCornerShape(
                                12.dp
                            )
                        )
                        .background(
                            Color(0xFFE21D32)
                        ),
                contentAlignment =
                    Alignment.Center
            ) {

                Icon(
                    item.icon,
                    item.title,
                    tint =
                        Color.White
                )
            }

            Spacer(
                Modifier.width(
                    10.dp
                )
            )

            Column {

                Text(
                    item.title,
                    fontWeight =
                        FontWeight.Bold,
                    color =
                        Color.White
                )

                Text(
                    item.subtitle,
                    fontSize =
                        11.sp,
                    color =
                        Color(0xFFB8BEC8),
                    maxLines = 2
                )
            }
        }
    }
}

// ============================================================
// DETAIL SCAFFOLD
// ============================================================

@OptIn(
    ExperimentalMaterial3Api::class
)
@Composable
private fun DetailScaffold(
    title: String,
    onBack: () -> Unit,
    content: @Composable () -> Unit
) {

    Scaffold(

        containerColor =
            Color(0xFF050608),

        topBar = {

            TopAppBar(

                title = {
                    Text(title)
                },

                navigationIcon = {

                    IconButton(
                        onClick =
                            onBack
                    ) {

                        Icon(
                            Icons.Filled.Menu,
                            "Zurück"
                        )
                    }
                },

                colors =
                    TopAppBarDefaults
                        .topAppBarColors(
                            containerColor =
                                Color(0xFF050608),
                            titleContentColor =
                                Color.White,
                            navigationIconContentColor =
                                Color.White
                        )
            )
        }

    ) { padding ->

        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(
                        padding
                    )
                    .padding(
                        16.dp
                    )
        ) {

            content()
        }
    }
}

// ============================================================
// FAHRZEUGAUSWAHL
// ============================================================

@Composable
private fun VehicleSelector(
    vehicles: List<Vehicle>,
    selectedVehicle: String,
    onSelected: (String) -> Unit
) {

    var expanded by remember {
        mutableStateOf(
            false
        )
    }

    Box(
        modifier =
            Modifier.fillMaxWidth()
    ) {

        OutlinedButton(
            modifier =
                Modifier.fillMaxWidth(),

            onClick = {
                expanded = true
            }
        ) {

            Text(
                if (
                    selectedVehicle
                        .isBlank()
                ) {
                    "Fahrzeug auswählen"
                } else {
                    selectedVehicle
                }
            )
        }

        DropdownMenu(
            expanded =
                expanded,
            onDismissRequest = {
                expanded = false
            }
        ) {

            vehicles.forEach {
                vehicle ->

                DropdownMenuItem(

                    text = {
                        Text(
                            vehicle.name
                        )
                    },

                    onClick = {

                        onSelected(
                            vehicle.name
                        )

                        expanded =
                            false
                    }
                )
            }
        }
    }
}

// ============================================================
// MEIN AUTO
// ============================================================

@Composable
private fun VehicleScreen(
    vehicles: List<Vehicle>,
    activeVehicle: String,
    onActiveVehicle: (String) -> Unit,
    onAdd: (Vehicle) -> Unit,
    onUpdate: (Int, Vehicle) -> Unit,
    onDelete: (Int) -> Unit
) {

    var editingIndex by remember {
        mutableStateOf<Int?>(null)
    }

    var adding by remember {
        mutableStateOf(false)
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

                val index =
                    vehicles.indexOf(
                        vehicle
                    )

                VehicleCard(
                    vehicle =
                        vehicle,
                    active =
                        vehicle.name ==
                            activeVehicle,
                    onSelect = {
                        onActiveVehicle(
                            vehicle.name
                        )
                    },
                    onEdit = {
                        editingIndex =
                            index
                        adding =
                            false
                    },
                    onDelete = {
                        onDelete(
                            index
                        )
                    }
                )
            }

            if (
                adding ||
                editingIndex != null
            ) {

                item {

                    VehicleForm(
                        vehicle =
                            editingIndex
                                ?.let {
                                    vehicles[it]
                                },
                        onCancel = {

                            adding =
                                false

                            editingIndex =
                                null
                        },
                        onSave = {
                            vehicle ->

                            val index =
                                editingIndex

                            if (
                                index == null
                            ) {

                                onAdd(
                                    vehicle
                                )

                            } else {

                                onUpdate(
                                    index,
                                    vehicle
                                )
                            }

                            adding =
                                false

                            editingIndex =
                                null
                        }
                    )
                }
            }
        }

        if (
            !adding &&
            editingIndex == null &&
            vehicles.size < 5
        ) {

            Button(
                modifier =
                    Modifier.fillMaxWidth(),

                onClick = {
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

@Composable
private fun VehicleCard(
    vehicle: Vehicle,
    active: Boolean,
    onSelect: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {

    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(
                    onClick =
                        onSelect
                ),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    if (
                        active
                    ) {
                        Color(
                            0xFF252A32
                        )
                    } else {
                        Color(
                            0xFF11141A
                        )
                    }
            )
    ) {

        Column(
            modifier =
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
                            86.dp
                        )
                )

                Spacer(
                    Modifier.width(
                        14.dp
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
                            20.sp
                    )

                    Text(
                        "${vehicle.make} ${vehicle.model}",
                        color =
                            Color(0xFFB8BEC8)
                    )

                    if (
                        vehicle.year
                            .isNotBlank()
                    ) {

                        Text(
                            "Baujahr: ${vehicle.year}",
                            color =
                                Color(0xFFB8BEC8)
                        )
                    }

                    if (
                        vehicle.plate
                            .isNotBlank()
                    ) {

                        Text(
                            "Kennzeichen: ${vehicle.plate}",
                            color =
                                Color(0xFFB8BEC8)
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

                OutlinedButton(
                    onClick =
                        onEdit
                ) {

                    Icon(
                        Icons.Filled.Edit,
                        null
                    )

                    Spacer(
                        Modifier.width(
                            4.dp
                        )
                    )

                    Text(
                        "Bearbeiten"
                    )
                }

                OutlinedButton(
                    onClick =
                        onDelete
                ) {

                    Icon(
                        Icons.Filled.Delete,
                        null
                    )

                    Spacer(
                        Modifier.width(
                            4.dp
                        )
                    )

                    Text(
                        "Löschen"
                    )
                }
            }
        }
    }
}

@Composable
private fun VehicleForm(
    vehicle: Vehicle?,
    onCancel: () -> Unit,
    onSave: (Vehicle) -> Unit
) {

    var name by remember {
        mutableStateOf(
            vehicle?.name ?: ""
        )
    }

    var make by remember {
        mutableStateOf(
            vehicle?.make ?: ""
        )
    }

    var model by remember {
        mutableStateOf(
            vehicle?.model ?: ""
        )
    }

    var year by remember {
        mutableStateOf(
            vehicle?.year ?: ""
        )
    }

    var plate by remember {
        mutableStateOf(
            vehicle?.plate ?: ""
        )
    }

    var vin by remember {
        mutableStateOf(
            vehicle?.vin ?: ""
        )
    }

    var imageUri by remember {
        mutableStateOf(
            vehicle?.imageUri ?: ""
        )
    }

    val picker =
        rememberLauncherForActivityResult(
            ActivityResultContracts
                .OpenDocument()
        ) { uri ->

            if (
                uri != null
            ) {

                imageUri =
                    uri.toString()
            }
        }

    Card(
        modifier =
            Modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    Color(0xFF11141A)
            )
    ) {

        Column(
            modifier =
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
                    vehicle == null
                ) {
                    "Fahrzeug hinzufügen"
                } else {
                    "Fahrzeug bearbeiten"
                },
                color =
                    Color.White,
                fontSize =
                    20.sp,
                fontWeight =
                    FontWeight.Bold
            )

            VehicleImage(
                uri =
                    imageUri,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(
                            180.dp
                        )
            )

            OutlinedButton(
                modifier =
                    Modifier.fillMaxWidth(),

                onClick = {

                    picker.launch(
                        arrayOf(
                            "image/*"
                        )
                    )
                }
            ) {

                Text(
                    "Fahrzeugbild auswählen"
                )
            }

            AppField(
                value =
                    name,
                label =
                    "Bezeichnung",
                onValueChange = {
                    name =
                        it
                }
            )

            AppField(
                value =
                    make,
                label =
                    "Marke",
                onValueChange = {
                    make =
                        it
                }
            )

            AppField(
                value =
                    model,
                label =
                    "Modell",
                onValueChange = {
                    model =
                        it
                }
            )

            AppField(
                value =
                    year,
                label =
                    "Baujahr",
                onValueChange = {
                    year =
                        it
                }
            )

            AppField(
                value =
                    plate,
                label =
                    "Kennzeichen",
                onValueChange = {
                    plate =
                        it
                }
            )

            AppField(
                value =
                    vin,
                label =
                    "VIN / FIN",
                onValueChange = {
                    vin =
                        it
                }
            )

            Row(
                horizontalArrangement =
                    Arrangement.spacedBy(
                        8.dp
                    )
            ) {

                OutlinedButton(
                    modifier =
                        Modifier.weight(
                            1f
                        ),
                    onClick =
                        onCancel
                ) {

                    Text(
                        "Abbrechen"
                    )
                }

                Button(
                    modifier =
                        Modifier.weight(
                            1f
                        ),

                    enabled =
                        name.isNotBlank(),

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
                            )
                        )
                    }
                ) {

                    Text(
                        "Speichern"
                    )
                }
            }
        }
    }
}

@Composable
private fun VehicleImage(
    uri: String,
    modifier: Modifier
) {

    if (
        uri.isBlank()
    ) {

        Box(
            modifier =
                modifier
                    .clip(
                        RoundedCornerShape(
                            14.dp
                        )
                    )
                    .background(
                        Color(0xFF242830)
                    ),
            contentAlignment =
                Alignment.Center
        ) {

            Icon(
                Icons.Filled.DirectionsCar,
                null,
                tint =
                    Color(0xFF8D949E),
                modifier =
                    Modifier.size(
                        42.dp
                    )
            )
        }

        return
    }

    val context =
        LocalContext.current

    var bitmap by remember(
        uri
    ) {
        mutableStateOf<
            androidx.compose.ui.graphics
                .ImageBitmap?
        >(null)
    }

    LaunchedEffect(
        uri
    ) {

        bitmap =
            kotlinx.coroutines
                .withContext(
                    kotlinx.coroutines
                        .Dispatchers.IO
                ) {

                    try {

                        context
                            .contentResolver
                            .openInputStream(
                                Uri.parse(
                                    uri
                                )
                            )
                            ?.use {
                                BitmapFactory
                                    .decodeStream(
                                        it
                                    )
                                    ?.asImageBitmap()
                            }

                    } catch (
                        _: Exception
                    ) {

                        null
                    }
                }
    }

    if (
        bitmap != null
    ) {

        Image(
            bitmap =
                bitmap!!,
            contentDescription =
                "Fahrzeugbild",
            modifier =
                modifier.clip(
                    RoundedCornerShape(
                        14.dp
                    )
                ),
            contentScale =
                ContentScale.Crop
        )

    } else {

        Box(
            modifier =
                modifier
                    .clip(
                        RoundedCornerShape(
                            14.dp
                        )
                    )
                    .background(
                        Color(0xFF242830)
                    ),
            contentAlignment =
                Alignment.Center
        ) {

            Text(
                "Bild wird geladen...",
                color =
                    Color.White
            )
        }
    }
}

// ============================================================
// REPARATUREN
// ============================================================

@Composable
private fun RepairsScreen(
    vehicles: List<Vehicle>,
    repairs: List<Repair>,
    selectedVehicle: String,
    onVehicleSelected: (String) -> Unit,
    onSave: (Repair) -> Unit,
    onUpdate: (Repair) -> Unit,
    onDelete: (Repair) -> Unit
) {

    var editing by remember {
        mutableStateOf<Repair?>(
            null
        )
    }

    var adding by remember {
        mutableStateOf(false)
    }

    Column(
        modifier =
            Modifier.fillMaxSize()
    ) {

        VehicleSelector(
            vehicles =
                vehicles,
            selectedVehicle =
                selectedVehicle,
            onSelected =
                onVehicleSelected
        )

        Spacer(
            Modifier.height(
                12.dp
            )
        )

        if (
            !adding &&
            editing == null
        ) {

            Button(
                modifier =
                    Modifier.fillMaxWidth(),

                onClick = {
                    adding =
                        true
                }
            ) {

                Text(
                    "Reparatur hinzufügen"
                )
            }
        }

        Spacer(
            Modifier.height(
                12.dp
            )
        )

        if (
            adding ||
            editing != null
        ) {

            RepairForm(
                vehicleName =
                    selectedVehicle,
                repair =
                    editing,
                onCancel = {
                    adding =
                        false

                    editing =
                        null
                },
                onSave = {
                    item ->

                    if (
                        editing == null
                    ) {

                        onSave(
                            item
                        )

                    } else {

                        onUpdate(
                            item
                        )
                    }

                    adding =
                        false

                    editing =
                        null
                }
            )

        } else {

            LazyColumn(
                verticalArrangement =
                    Arrangement.spacedBy(
                        10.dp
                    )
            ) {

                items(
                    repairs.filter {
                        it.vehicleName ==
                            selectedVehicle
                    }
                ) { repair ->

                    RepairCard(
                        repair =
                            repair,
                        onEdit = {
                            editing =
                                repair
                        },
                        onDelete = {
                            onDelete(
                                repair
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun RepairForm(
    vehicleName: String,
    repair: Repair?,
    onCancel: () -> Unit,
    onSave: (Repair) -> Unit
) {

    var date by remember {
        mutableStateOf(
            repair?.date
                ?: LocalDate.now()
                    .toString()
        )
    }

    var mileage by remember {
        mutableStateOf(
            repair?.mileage ?: ""
        )
    }

    var description by remember {
        mutableStateOf(
            repair?.description ?: ""
        )
    }

    var cost by remember {
        mutableStateOf(
            repair?.cost ?: ""
        )
    }

    var workshop by remember {
        mutableStateOf(
            repair?.workshop ?: ""
        )
    }

    Card(
        colors =
            CardDefaults.cardColors(
                containerColor =
                    Color(0xFF11141A)
            )
    ) {

        Column(
            modifier =
                Modifier.padding(
                    16.dp
                ),
            verticalArrangement =
                Arrangement.spacedBy(
                    8.dp
                )
        ) {

            Text(
                "Reparatur",
                color =
                    Color.White,
                fontSize =
                    20.sp,
                fontWeight =
                    FontWeight.Bold
            )

            AppField(
                date,
                "Datum",
                { date = it }
            )

            AppField(
                mileage,
                "Kilometerstand",
                { mileage = it }
            )

            AppField(
                description,
                "Beschreibung",
                { description = it },
                false
            )

            AppField(
                cost,
                "Kosten in €",
                { cost = it }
            )

            AppField(
                workshop,
                "Werkstatt",
                { workshop = it }
            )

            Row(
                horizontalArrangement =
                    Arrangement.spacedBy(
                        8.dp
                    )
            ) {

                OutlinedButton(
                    modifier =
                        Modifier.weight(
                            1f
                        ),
                    onClick =
                        onCancel
                ) {

                    Text(
                        "Abbrechen"
                    )
                }

                Button(
                    modifier =
                        Modifier.weight(
                            1f
                        ),

                    enabled =
                        description
                            .isNotBlank(),

                    onClick = {

                        onSave(
                            Repair(
                                id =
                                    repair?.id
                                        ?: System
                                            .currentTimeMillis(),
                                vehicleName =
                                    vehicleName,
                                date =
                                    date,
                                mileage =
                                    mileage,
                                description =
                                    description,
                                cost =
                                    cost,
                                workshop =
                                    workshop
                            )
                        )
                    }
                ) {

                    Text(
                        "Speichern"
                    )
                }
            }
        }
    }
}

@Composable
private fun RepairCard(
    repair: Repair,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {

    Card(
        colors =
            CardDefaults.cardColors(
                containerColor =
                    Color(0xFF11141A)
            )
    ) {

        Column(
            modifier =
                Modifier.padding(
                    16.dp
                )
        ) {

            Text(
                repair.description,
                color =
                    Color.White,
                fontSize =
                    18.sp,
                fontWeight =
                    FontWeight.Bold
            )

            Text(
                "Datum: ${repair.date}",
                color =
                    Color(0xFFB8BEC8)
            )

            Text(
                "Kilometerstand: ${repair.mileage}",
                color =
                    Color(0xFFB8BEC8)
            )

            Text(
                "Kosten: ${repair.cost} €",
                color =
                    Color(0xFFB8BEC8)
            )

            if (
                repair.workshop
                    .isNotBlank()
            ) {

                Text(
                    "Werkstatt: ${repair.workshop}",
                    color =
                        Color(0xFFB8BEC8)
                )
            }

            Spacer(
                Modifier.height(
                    8.dp
                )
            )

            Row(
                horizontalArrangement =
                    Arrangement.spacedBy(
                        8.dp
                    )
            ) {

                OutlinedButton(
                    onClick =
                        onEdit
                ) {

                    Text(
                        "Bearbeiten"
                    )
                }

                OutlinedButton(
                    onClick =
                        onDelete
                ) {

                    Text(
                        "Löschen"
                    )
                }
            }
        }
    }
}

// ============================================================
// PICKERL / TÜV
// ============================================================

@Composable
private fun PickerlScreen(
    vehicles: List<Vehicle>,
    pickerls: List<Pickerl>,
    selectedVehicle: String,
    onVehicleSelected: (String) -> Unit,
    onSave: (Pickerl) -> Unit
) {

    val context =
        LocalContext.current

    val existing =
        pickerls.firstOrNull {
            it.vehicleName ==
                selectedVehicle
        }

    var lastDate by remember(
        selectedVehicle
    ) {
        mutableStateOf(
            existing?.lastDate
                ?: ""
        )
    }

    var nextDate by remember(
        selectedVehicle
    ) {
        mutableStateOf(
            existing?.nextDate
                ?: ""
        )
    }

    var notes by remember(
        selectedVehicle
    ) {
        mutableStateOf(
            existing?.notes
                ?: ""
        )
    }

    var reminder by remember(
        selectedVehicle
    ) {
        mutableStateOf(
            existing?.reminder
                ?: true
        )
    }

    Column(
        modifier =
            Modifier.fillMaxSize()
    ) {

        VehicleSelector(
            vehicles =
                vehicles,
            selectedVehicle =
                selectedVehicle,
            onSelected =
                onVehicleSelected
        )

        Spacer(
            Modifier.height(
                12.dp
            )
        )

        Card(
            colors =
                CardDefaults.cardColors(
                    containerColor =
                        Color(0xFF11141A)
                )
        ) {

            Column(
                modifier =
                    Modifier.padding(
                        16.dp
                    ),
                verticalArrangement =
                    Arrangement.spacedBy(
                        8.dp
                    )
            ) {

                Text(
                    "Pickerl / TÜV",
                    color =
                        Color.White,
                    fontSize =
                        20.sp,
                    fontWeight =
                        FontWeight.Bold
                )

                AppField(
                    lastDate,
                    "Letztes Pickerl (YYYY-MM-DD)",
                    { lastDate = it }
                )

                AppField(
                    nextDate,
                    "Nächstes Pickerl (YYYY-MM-DD)",
                    { nextDate = it }
                )

                AppField(
                    notes,
                    "Notizen",
                    { notes = it },
                    false
                )

                Row(
                    modifier =
                        Modifier.fillMaxWidth(),
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    Column(
                        modifier =
                            Modifier.weight(
                                1f
                            )
                    ) {

                        Text(
                            "Erinnerung",
                            color =
                                Color.White
                        )

                        Text(
                            "3 Monate vor dem Pickerl",
                            color =
                                Color(0xFFB8BEC8),
                            fontSize =
                                12.sp
                        )
                    }

                    Switch(
                        checked =
                            reminder,
                        onCheckedChange = {
                            reminder =
                                it
                        }
                    )
                }

                Button(
                    modifier =
                        Modifier.fillMaxWidth(),

                    enabled =
                        selectedVehicle
                            .isNotBlank(),

                    onClick = {

                        val item =
                            Pickerl(
                                vehicleName =
                                    selectedVehicle,
                                lastDate =
                                    lastDate,
                                nextDate =
                                    nextDate,
                                notes =
                                    notes,
                                reminder =
                                    reminder
                            )

                        onSave(
                            item
                        )

                        if (
                            reminder &&
                            nextDate
                                .isNotBlank()
                        ) {

                            schedulePickerlReminder(
                                context,
                                selectedVehicle,
                                nextDate
                            )
                        }
                    }
                ) {

                    Text(
                        "Pickerl speichern"
                    )
                }
            }
        }
    }
}

// ============================================================
// PICKERL ALARM
// ============================================================

private fun schedulePickerlReminder(
    context: Context,
    vehicleName: String,
    nextDate: String
) {

    try {

        val date =
            LocalDate.parse(
                nextDate
            )

        val reminderDate =
            date.minusMonths(
                3
            )

        val trigger =
            reminderDate
                .atTime(
                    9,
                    0
                )
                .atZone(
                    ZoneId.systemDefault()
                )
                .toInstant()
                .toEpochMilli()

        val intent =
            Intent(
                context,
                PickerlReceiver::class.java
            ).apply {

                putExtra(
                    "vehicle",
                    vehicleName
                )
            }

        val pending =
            PendingIntent.getBroadcast(
                context,
                vehicleName.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE
            )

        val alarm =
            context.getSystemService(
                Context.ALARM_SERVICE
            ) as AlarmManager

        alarm.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            trigger,
            pending
        )

    } catch (_: Exception) {
        // Ungültiges Datum wird ignoriert.
    }
}

// ============================================================
// WARTUNGEN
// ============================================================

@Composable
private fun MaintenanceScreen(
    vehicles: List<Vehicle>,
    maintenances: List<Maintenance>,
    selectedVehicle: String,
    onVehicleSelected: (String) -> Unit,
    onSave: (Maintenance) -> Unit,
    onUpdate: (Maintenance) -> Unit,
    onDelete: (Maintenance) -> Unit
) {

    var adding by remember {
        mutableStateOf(false)
    }

    var editing by remember {
        mutableStateOf<Maintenance?>(
            null
        )
    }

    Column(
        modifier =
            Modifier.fillMaxSize()
    ) {

        VehicleSelector(
            vehicles =
                vehicles,
            selectedVehicle =
                selectedVehicle,
            onSelected =
                onVehicleSelected
        )

        Spacer(
            Modifier.height(
                12.dp
            )
        )

        if (
            !adding &&
            editing == null
        ) {

            Button(
                modifier =
                    Modifier.fillMaxWidth(),

                onClick = {
                    adding =
                        true
                }
            ) {

                Text(
                    "Wartung hinzufügen"
                )
            }
        }

        Spacer(
            Modifier.height(
                12.dp
            )
        )

        if (
            adding ||
            editing != null
        ) {

            MaintenanceForm(
                vehicleName =
                    selectedVehicle,
                maintenance =
                    editing,
                onCancel = {
                    adding =
                        false
                    editing =
                        null
                },
                onSave = {
                    item ->

                    if (
                        editing == null
                    ) {
                        onSave(
                            item
                        )
                    } else {
                        onUpdate(
                            item
                        )
                    }

                    adding =
                        false
                    editing =
                        null
                }
            )

        } else {

            LazyColumn(
                verticalArrangement =
                    Arrangement.spacedBy(
                        10.dp
                    )
            ) {

                items(
                    maintenances.filter {
                        it.vehicleName ==
                            selectedVehicle
                    }
                ) { item ->

                    MaintenanceCard(
                        maintenance =
                            item,
                        onEdit = {
                            editing =
                                item
                        },
                        onDelete = {
                            onDelete(
                                item
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun MaintenanceForm(
    vehicleName: String,
    maintenance: Maintenance?,
    onCancel: () -> Unit,
    onSave: (Maintenance) -> Unit
) {

    var date by remember {
        mutableStateOf(
            maintenance?.date
                ?: LocalDate.now()
                    .toString()
        )
    }

    var mileage by remember {
        mutableStateOf(
            maintenance?.mileage
                ?: ""
        )
    }

    var cost by remember {
        mutableStateOf(
            maintenance?.cost
                ?: ""
        )
    }

    var workshop by remember {
        mutableStateOf(
            maintenance?.workshop
                ?: ""
        )
    }

    var notes by remember {
        mutableStateOf(
            maintenance?.notes
                ?: ""
        )
    }

    Card(
        colors =
            CardDefaults.cardColors(
                containerColor =
                    Color(0xFF11141A)
            )
    ) {

        Column(
            modifier =
                Modifier.padding(
                    16.dp
                ),
            verticalArrangement =
                Arrangement.spacedBy(
                    8.dp
                )
        ) {

            Text(
                "Wartung",
                color =
                    Color.White,
                fontSize =
                    20.sp,
                fontWeight =
                    FontWeight.Bold
            )

            AppField(
                date,
                "Datum",
                { date = it }
            )

            AppField(
                mileage,
                "Kilometerstand",
                { mileage = it }
            )

            AppField(
                cost,
                "Kosten in €",
                { cost = it }
            )

            AppField(
                workshop,
                "Werkstatt",
                { workshop = it }
            )

            AppField(
                notes,
                "Notizen",
                { notes = it },
                false
            )

            Row(
                horizontalArrangement =
                    Arrangement.spacedBy(
                        8.dp
                    )
            ) {

                OutlinedButton(
                    modifier =
                        Modifier.weight(
                            1f
                        ),
                    onClick =
                        onCancel
                ) {

                    Text(
                        "Abbrechen"
                    )
                }

                Button(
                    modifier =
                        Modifier.weight(
                            1f
                        ),
                    onClick = {

                        onSave(
                            Maintenance(
                                id =
                                    maintenance?.id
                                        ?: System
                                            .currentTimeMillis(),
                                vehicleName =
                                    vehicleName,
                                date =
                                    date,
                                mileage =
                                    mileage,
                                cost =
                                    cost,
                                workshop =
                                    workshop,
                                notes =
                                    notes
                            )
                        )
                    }
                ) {

                    Text(
                        "Speichern"
                    )
                }
            }
        }
    }
}

@Composable
private fun MaintenanceCard(
    maintenance: Maintenance,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {

    Card(
        colors =
            CardDefaults.cardColors(
                containerColor =
                    Color(0xFF11141A)
            )
    ) {

        Column(
            modifier =
                Modifier.padding(
                    16.dp
                )
        ) {

            Text(
                "Wartung",
                color =
                    Color.White,
                fontSize =
                    18.sp,
                fontWeight =
                    FontWeight.Bold
            )

            Text(
                "Datum: ${maintenance.date}",
                color =
                    Color(0xFFB8BEC8)
            )

            Text(
                "Kilometerstand: ${maintenance.mileage}",
                color =
                    Color(0xFFB8BEC8)
            )

            Text(
                "Kosten: ${maintenance.cost} €",
                color =
                    Color(0xFFB8BEC8)
            )

            if (
                maintenance.workshop
                    .isNotBlank()
            ) {

                Text(
                    "Werkstatt: ${maintenance.workshop}",
                    color =
                        Color(0xFFB8BEC8)
                )
            }

            if (
                maintenance.notes
                    .isNotBlank()
            ) {

                Text(
                    "Notizen: ${maintenance.notes}",
                    color =
                        Color(0xFFB8BEC8)
                )
            }

            Spacer(
                Modifier.height(
                    8.dp
                )
            )

            Row(
                horizontalArrangement =
                    Arrangement.spacedBy(
                        8.dp
                    )
            ) {

                OutlinedButton(
                    onClick =
                        onEdit
                ) {

                    Text(
                        "Bearbeiten"
                    )
                }

                OutlinedButton(
                    onClick =
                        onDelete
                ) {

                    Text(
                        "Löschen"
                    )
                }
            }
        }
    }
}

// ============================================================
// REIFEN
// ============================================================

@Composable
private fun TiresScreen(
    vehicles: List<Vehicle>,
    tires: List<TireSet>,
    selectedVehicle: String,
    onVehicleSelected: (String) -> Unit,
    onSave: (TireSet) -> Unit,
    onUpdate: (TireSet) -> Unit,
    onDelete: (TireSet) -> Unit
) {

    var adding by remember {
        mutableStateOf(false)
    }

    var editing by remember {
        mutableStateOf<TireSet?>(
            null
        )
    }

    Column(
        modifier =
            Modifier.fillMaxSize()
    ) {

        VehicleSelector(
            vehicles =
                vehicles,
            selectedVehicle =
                selectedVehicle,
            onSelected =
                onVehicleSelected
        )

        Spacer(
            Modifier.height(
                12.dp
            )
        )

        if (
            !adding &&
            editing == null
        ) {

            Button(
                modifier =
                    Modifier.fillMaxWidth(),

                onClick = {
                    adding =
                        true
                }
            ) {

                Text(
                    "Reifensatz hinzufügen"
                )
            }
        }

        Spacer(
            Modifier.height(
                12.dp
            )
        )

        if (
            adding ||
            editing != null
        ) {

            TireForm(
                vehicleName =
                    selectedVehicle,
                tire =
                    editing,
                onCancel = {
                    adding =
                        false
                    editing =
                        null
                },
                onSave = {
                    item ->

                    if (
                        editing == null
                    ) {
                        onSave(
                            item
                        )
                    } else {
                        onUpdate(
                            item
                        )
                    }

                    adding =
                        false
                    editing =
                        null
                }
            )

        } else {

            LazyColumn(
                verticalArrangement =
                    Arrangement.spacedBy(
                        10.dp
                    )
            ) {

                items(
                    tires.filter {
                        it.vehicleName ==
                            selectedVehicle
                    }
                ) { tire ->

                    TireCard(
                        tire =
                            tire,
                        onEdit = {
                            editing =
                                tire
                        },
                        onDelete = {
                            onDelete(
                                tire
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun TireForm(
    vehicleName: String,
    tire: TireSet?,
    onCancel: () -> Unit,
    onSave: (TireSet) -> Unit
) {

    var season by remember {
        mutableStateOf(
            tire?.season
                ?: "Sommer"
        )
    }

    var dimension by remember {
        mutableStateOf(
            tire?.dimension
                ?: ""
        )
    }

    var brand by remember {
        mutableStateOf(
            tire?.brand
                ?: ""
        )
    }

    var dot by remember {
        mutableStateOf(
            tire?.dot
                ?: ""
        )
    }

    var tread by remember {
        mutableStateOf(
            tire?.treadDepth
                ?: ""
        )
    }

    var condition by remember {
        mutableStateOf(
            tire?.condition
                ?: ""
        )
    }

    var storage by remember {
        mutableStateOf(
            tire?.storage
                ?: ""
        )
    }

    var expanded by remember {
        mutableStateOf(
            false
        )
    }

    Card(
        colors =
            CardDefaults.cardColors(
                containerColor =
                    Color(0xFF11141A)
            )
    ) {

        Column(
            modifier =
                Modifier.padding(
                    16.dp
                ),
            verticalArrangement =
                Arrangement.spacedBy(
                    8.dp
                )
        ) {

            Text(
                "Reifensatz",
                color =
                    Color.White,
                fontSize =
                    20.sp,
                fontWeight =
                    FontWeight.Bold
            )

            Box {

                OutlinedButton(
                    modifier =
                        Modifier.fillMaxWidth(),

                    onClick = {
                        expanded =
                            true
                    }
                ) {

                    Text(
                        "Saison: $season"
                    )
                }

                DropdownMenu(
                    expanded =
                        expanded,
                    onDismissRequest = {
                        expanded =
                            false
                    }
                ) {

                    DropdownMenuItem(
                        text = {
                            Text(
                                "Sommer"
                            )
                        },
                        onClick = {
                            season =
                                "Sommer"
                            expanded =
                                false
                        }
                    )

                    DropdownMenuItem(
                        text = {
                            Text(
                                "Winter"
                            )
                        },
                        onClick = {
                            season =
                                "Winter"
                            expanded =
                                false
                        }
                    )
                }
            }

            AppField(
                dimension,
                "Dimension",
                { dimension = it }
            )

            AppField(
                brand,
                "Marke",
                { brand = it }
            )

            AppField(
                dot,
                "DOT",
                { dot = it }
            )

            AppField(
                tread,
                "Profiltiefe",
                { tread = it }
            )

            AppField(
                condition,
                "Zustand",
                { condition = it }
            )

            AppField(
                storage,
                "Lagerung",
                { storage = it }
            )

            Row(
                horizontalArrangement =
                    Arrangement.spacedBy(
                        8.dp
                    )
            ) {

                OutlinedButton(
                    modifier =
                        Modifier.weight(
                            1f
                        ),
                    onClick =
                        onCancel
                ) {

                    Text(
                        "Abbrechen"
                    )
                }

                Button(
                    modifier =
                        Modifier.weight(
                            1f
                        ),
                    onClick = {

                        onSave(
                            TireSet(
                                id =
                                    tire?.id
                                        ?: System
                                            .currentTimeMillis(),
                                vehicleName =
                                    vehicleName,
                                season =
                                    season,
                                dimension =
                                    dimension,
                                brand =
                                    brand,
                                dot =
                                    dot,
                                treadDepth =
                                    tread,
                                condition =
                                    condition,
                                storage =
                                    storage
                            )
                        )
                    }
                ) {

                    Text(
                        "Speichern"
                    )
                }
            }
        }
    }
}

@Composable
private fun TireCard(
    tire: TireSet,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {

    Card(
        colors =
            CardDefaults.cardColors(
                containerColor =
                    Color(0xFF11141A)
            )
    ) {

        Column(
            modifier =
                Modifier.padding(
                    16.dp
                )
        ) {

            Text(
                tire.season,
                color =
                    Color.White,
                fontSize =
                    18.sp,
                fontWeight =
                    FontWeight.Bold
            )

            Text(
                "Dimension: ${tire.dimension}",
                color =
                    Color(0xFFB8BEC8)
            )

            Text(
                "Marke: ${tire.brand}",
                color =
                    Color(0xFFB8BEC8)
            )

            Text(
                "DOT: ${tire.dot}",
                color =
                    Color(0xFFB8BEC8)
            )

            Text(
                "Profiltiefe: ${tire.treadDepth}",
                color =
                    Color(0xFFB8BEC8)
            )

            Text(
                "Zustand: ${tire.condition}",
                color =
                    Color(0xFFB8BEC8)
            )

            Text(
                "Lagerung: ${tire.storage}",
                color =
                    Color(0xFFB8BEC8)
            )

            Spacer(
                Modifier.height(
                    8.dp
                )
            )

            Row(
                horizontalArrangement =
                    Arrangement.spacedBy(
                        8.dp
                    )
            ) {

                OutlinedButton(
                    onClick =
                        onEdit
                ) {

                    Text(
                        "Bearbeiten"
                    )
                }

                OutlinedButton(
                    onClick =
                        onDelete
                ) {

                    Text(
                        "Löschen"
                    )
                }
            }
        }
    }
}

// ============================================================
// GESAMTBLICK
// ============================================================

@Composable
private fun OverviewScreen(
    vehicles: List<Vehicle>,
    repairs: List<Repair>,
    maintenances: List<Maintenance>,
    pickerls: List<Pickerl>,
    tires: List<TireSet>,
    selectedVehicle: String,
    onVehicleSelected: (String) -> Unit
) {

    Column(
        modifier =
            Modifier.fillMaxSize()
    ) {

        VehicleSelector(
            vehicles =
                vehicles,
            selectedVehicle =
                selectedVehicle,
            onSelected =
                onVehicleSelected
        )

        Spacer(
            Modifier.height(
                12.dp
            )
        )

        val vehicle =
            vehicles.firstOrNull {
                it.name ==
                    selectedVehicle
            }

        if (
            vehicle == null
        ) {

            Text(
                "Noch kein Fahrzeug vorhanden.",
                color =
                    Color.White
            )

        } else {

            LazyColumn(
                verticalArrangement =
                    Arrangement.spacedBy(
                        10.dp
                    )
            ) {

                item {

                    VehicleOverviewCard(
                        vehicle =
                            vehicle
                    )
                }

                item {

                    OverviewCard(
                        title =
                            "Reparaturen",
                        value =
                            repairs.count {
                                it.vehicleName ==
                                    selectedVehicle
                            }.toString()
                    )
                }

                item {

                    OverviewCard(
                        title =
                            "Wartungen",
                        value =
                            maintenances.count {
                                it.vehicleName ==
                                    selectedVehicle
                            }.toString()
                    )
                }

                item {

                    OverviewCard(
                        title =
                            "Reifensätze",
                        value =
                            tires.count {
                                it.vehicleName ==
                                    selectedVehicle
                            }.toString()
                    )
                }

                item {

                    val pickerl =
                        pickerls.firstOrNull {
                            it.vehicleName ==
                                selectedVehicle
                        }

                    Card(
                        colors =
                            CardDefaults
                                .cardColors(
                                    containerColor =
                                        Color(
                                            0xFF11141A
                                        )
                                )
                    ) {

                        Column(
                            modifier =
                                Modifier.padding(
                                    16.dp
                                )
                        ) {

                            Text(
                                "Pickerl / TÜV",
                                color =
                                    Color.White,
                                fontSize =
                                    18.sp,
                                fontWeight =
                                    FontWeight.Bold
                            )

                            Spacer(
                                Modifier.height(
                                    6.dp
                                )
                            )

                            if (
                                pickerl ==
                                    null
                            ) {

                                Text(
                                    "Noch keine Pickerl-Daten gespeichert.",
                                    color =
                                        Color(
                                            0xFFB8BEC8
                                        )
                                )

                            } else {

                                Text(
                                    "Nächstes Pickerl: ${pickerl.nextDate}",
                                    color =
                                        Color(
                                            0xFFB8BEC8
                                        )
                                )

                                Text(
                                    if (
                                        pickerl
                                            .reminder
                                    ) {
                                        "Erinnerung: aktiv"
                                    } else {
                                        "Erinnerung: deaktiviert"
                                    },
                                    color =
                                        Color(
                                            0xFFB8BEC8
                                        )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VehicleOverviewCard(
    vehicle: Vehicle
) {

    Card(
        colors =
            CardDefaults.cardColors(
                containerColor =
                    Color(0xFF11141A)
            )
    ) {

        Column(
            modifier =
                Modifier.padding(
                    16.dp
                )
        ) {

            VehicleImage(
                uri =
                    vehicle.imageUri,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(
                            190.dp
                        )
            )

            Spacer(
                Modifier.height(
                    10.dp
                )
            )

            Text(
                vehicle.name,
                color =
                    Color.White,
                fontSize =
                    22.sp,
                fontWeight =
                    FontWeight.Bold
            )

            Text(
                "${vehicle.make} ${vehicle.model}",
                color =
                    Color(0xFFB8BEC8)
            )

            if (
                vehicle.year
                    .isNotBlank()
            ) {

                Text(
                    "Baujahr: ${vehicle.year}",
                    color =
                        Color(0xFFB8BEC8)
                )
            }

            if (
                vehicle.plate
                    .isNotBlank()
            ) {

                Text(
                    "Kennzeichen: ${vehicle.plate}",
                    color =
                        Color(0xFFB8BEC8)
                )
            }

            if (
                vehicle.vin
                    .isNotBlank()
            ) {

                Text(
                    "VIN / FIN: ${vehicle.vin}",
                    color =
                        Color(0xFFB8BEC8)
                )
            }
        }
    }
}

@Composable
private fun OverviewCard(
    title: String,
    value: String
) {

    Card(
        colors =
            CardDefaults.cardColors(
                containerColor =
                    Color(0xFF11141A)
            )
    ) {

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        16.dp
                    ),
            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Text(
                title,
                color =
                    Color.White,
                fontSize =
                    18.sp,
                modifier =
                    Modifier.weight(
                        1f
                    )
            )

            Text(
                value,
                color =
                    Color.White,
                fontSize =
                    22.sp,
                fontWeight =
                    FontWeight.Bold
            )
        }
    }
}

// ============================================================
// STANDARD-TEXTFELD
// ============================================================

@Composable
private fun AppField(
    value: String,
    label: String,
    onValueChange: (String) -> Unit,
    singleLine: Boolean = true
) {

    OutlinedTextField(
        value =
            value,
        onValueChange =
            onValueChange,
        modifier =
            Modifier.fillMaxWidth(),
        label = {
            Text(
                label
            )
        },
        singleLine =
            singleLine
    )
}
