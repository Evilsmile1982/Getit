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

private val Background = Color(0xFF050608)
private val Surface = Color(0xFF11151C)
private val Muted = Color(0xFF9AA3B2)

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
    val vehicle: String,
    val date: String,
    val mileage: String,
    val description: String,
    val cost: String,
    val workshop: String
)

data class Maintenance(
    val id: Long,
    val vehicle: String,
    val date: String,
    val mileage: String,
    val cost: String,
    val workshop: String,
    val notes: String
)

data class Pickerl(
    val vehicle: String,
    val lastDate: String,
    val nextDate: String,
    val notes: String,
    val reminder: Boolean
)

data class TireSet(
    val id: Long,
    val vehicle: String,
    val season: String,
    val dimension: String,
    val brand: String,
    val dot: String,
    val tread: String,
    val condition: String,
    val storage: String
)

class VehicleStore(context: Context) {

    private val prefs =
        context.getSharedPreferences("autocheck", Context.MODE_PRIVATE)

    fun load(): List<Vehicle> {
        val array =
            JSONArray(
                prefs.getString("vehicles", "[]") ?: "[]"
            )

        return buildList {
            for (i in 0 until array.length()) {
                val o = array.getJSONObject(i)

                add(
                    Vehicle(
                        name = o.optString("name"),
                        make = o.optString("make"),
                        model = o.optString("model"),
                        year = o.optString("year"),
                        plate = o.optString("plate"),
                        vin = o.optString("vin"),
                        imageUri = o.optString("imageUri")
                    )
                )
            }
        }
    }

    fun save(list: List<Vehicle>) {

        val array = JSONArray()

        list.forEach { vehicle ->

            array.put(
                JSONObject().apply {
                    put("name", vehicle.name)
                    put("make", vehicle.make)
                    put("model", vehicle.model)
                    put("year", vehicle.year)
                    put("plate", vehicle.plate)
                    put("vin", vehicle.vin)
                    put("imageUri", vehicle.imageUri)
                }
            )
        }

        prefs.edit()
            .putString("vehicles", array.toString())
            .apply()
    }

    fun activeVehicle(): String =
        prefs.getString(
            "activeVehicle",
            ""
        ) ?: ""

    fun setActiveVehicle(
        name: String
    ) {
        prefs.edit()
            .putString(
                "activeVehicle",
                name
            )
            .apply()
    }

    fun loadRepairs(): List<Repair> {

        val array =
            JSONArray(
                prefs.getString(
                    "repairs",
                    "[]"
                ) ?: "[]"
            )

        return buildList {

            for (i in 0 until array.length()) {

                val o =
                    array.getJSONObject(i)

                add(
                    Repair(
                        id = o.optLong("id"),
                        vehicle = o.optString("vehicle"),
                        date = o.optString("date"),
                        mileage = o.optString("mileage"),
                        description = o.optString("description"),
                        cost = o.optString("cost"),
                        workshop = o.optString("workshop")
                    )
                )
            }
        }
    }

    fun saveRepairs(
        list: List<Repair>
    ) {

        val array = JSONArray()

        list.forEach {

            array.put(
                JSONObject().apply {
                    put("id", it.id)
                    put("vehicle", it.vehicle)
                    put("date", it.date)
                    put("mileage", it.mileage)
                    put("description", it.description)
                    put("cost", it.cost)
                    put("workshop", it.workshop)
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

    fun loadMaintenance(): List<Maintenance> {

        val array =
            JSONArray(
                prefs.getString(
                    "maintenance",
                    "[]"
                ) ?: "[]"
            )

        return buildList {

            for (i in 0 until array.length()) {

                val o =
                    array.getJSONObject(i)

                add(
                    Maintenance(
                        id = o.optLong("id"),
                        vehicle = o.optString("vehicle"),
                        date = o.optString("date"),
                        mileage = o.optString("mileage"),
                        cost = o.optString("cost"),
                        workshop = o.optString("workshop"),
                        notes = o.optString("notes")
                    )
                )
            }
        }
    }

    fun saveMaintenance(
        list: List<Maintenance>
    ) {

        val array = JSONArray()

        list.forEach {

            array.put(
                JSONObject().apply {
                    put("id", it.id)
                    put("vehicle", it.vehicle)
                    put("date", it.date)
                    put("mileage", it.mileage)
                    put("cost", it.cost)
                    put("workshop", it.workshop)
                    put("notes", it.notes)
                }
            )
        }

        prefs.edit()
            .putString(
                "maintenance",
                array.toString()
            )
            .apply()
    }

    fun loadPickerl(): List<Pickerl> {

        val array =
            JSONArray(
                prefs.getString(
                    "pickerl",
                    "[]"
                ) ?: "[]"
            )

        return buildList {

            for (i in 0 until array.length()) {

                val o =
                    array.getJSONObject(i)

                add(
                    Pickerl(
                        vehicle =
                            o.optString(
                                "vehicle"
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
                                "reminder"
                            )
                    )
                )
            }
        }
    }

    fun savePickerl(
        list: List<Pickerl>
    ) {

        val array = JSONArray()

        list.forEach {

            array.put(
                JSONObject().apply {
                    put("vehicle", it.vehicle)
                    put("lastDate", it.lastDate)
                    put("nextDate", it.nextDate)
                    put("notes", it.notes)
                    put("reminder", it.reminder)
                }
            )
        }

        prefs.edit()
            .putString(
                "pickerl",
                array.toString()
            )
            .apply()
    }

    fun loadTires(): List<TireSet> {

        val array =
            JSONArray(
                prefs.getString(
                    "tires",
                    "[]"
                ) ?: "[]"
            )

        return buildList {

            for (i in 0 until array.length()) {

                val o =
                    array.getJSONObject(i)

                add(
                    TireSet(
                        id =
                            o.optLong(
                                "id"
                            ),
                        vehicle =
                            o.optString(
                                "vehicle"
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
                        tread =
                            o.optString(
                                "tread"
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

        val array = JSONArray()

        list.forEach {

            array.put(
                JSONObject().apply {
                    put("id", it.id)
                    put("vehicle", it.vehicle)
                    put("season", it.season)
                    put("dimension", it.dimension)
                    put("brand", it.brand)
                    put("dot", it.dot)
                    put("tread", it.tread)
                    put("condition", it.condition)
                    put("storage", it.storage)
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

    fun deleteVehicleData(
        name: String
    ) {

        saveRepairs(
            loadRepairs()
                .filterNot {
                    it.vehicle == name
                }
        )

        saveMaintenance(
            loadMaintenance()
                .filterNot {
                    it.vehicle == name
                }
        )

        savePickerl(
            loadPickerl()
                .filterNot {
                    it.vehicle == name
                }
        )

        saveTires(
            loadTires()
                .filterNot {
                    it.vehicle == name
                }
        )
    }
}

private class VisitedStore(
    context: Context
) {

    private val prefs =
        context.getSharedPreferences(
            "autocheck_visited",
            Context.MODE_PRIVATE
        )

    fun isVisited(
        screen: Screen
    ): Boolean =
        prefs.getBoolean(
            screen.name,
            false
        )

    fun markVisited(
        screen: Screen
    ) {

        prefs.edit()
            .putBoolean(
                screen.name,
                true
            )
            .apply()
    }
}

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
    val icon:
        androidx.compose.ui.graphics.vector.ImageVector,
    val accent: Color,
    val screen: Screen
)

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
            Build.VERSION.SDK_INT >= 33
        ) {

            requestPermissions(
                arrayOf(
                    "android.permission.POST_NOTIFICATIONS"
                ),
                5001
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
                    "pickerl_reminders",
                    "Pickerl Erinnerungen",
                    NotificationManager
                        .IMPORTANCE_DEFAULT
                )

            getSystemService(
                NotificationManager::class.java
            )
                .createNotificationChannel(
                    channel
                )
        }
    }
}

class PickerlReceiver :
    BroadcastReceiver() {

    override fun onReceive(
        context: Context,
        intent: Intent?
    ) {

        val vehicle =
            intent?.getStringExtra(
                "vehicle"
            ) ?: "Fahrzeug"

        val nextDate =
            intent?.getStringExtra(
                "nextDate"
            ) ?: ""

        val manager =
            context.getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager

        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.O
        ) {

            val notification =
                android.app.Notification
                    .Builder(
                        context,
                        "pickerl_reminders"
                    )
                    .setSmallIcon(
                        android.R.drawable
                            .ic_dialog_info
                    )
                    .setContentTitle(
                        "Pickerl / TÜV Erinnerung"
                    )
                    .setContentText(
                        "$vehicle: Prüfung am $nextDate"
                    )
                    .setAutoCancel(
                        true
                    )
                    .build()

            manager.notify(
                vehicle.hashCode(),
                notification
            )

        } else {

            @Suppress("DEPRECATION")
            val notification =
                android.app.Notification
                    .Builder(
                        context
                    )
                    .setSmallIcon(
                        android.R.drawable
                            .ic_dialog_info
                    )
                    .setContentTitle(
                        "Pickerl / TÜV Erinnerung"
                    )
                    .setContentText(
                        "$vehicle: Prüfung am $nextDate"
                    )
                    .setAutoCancel(
                        true
                    )
                    .build()

            @Suppress("DEPRECATION")
            manager.notify(
                vehicle.hashCode(),
                notification
            )
        }
    }
}

private fun schedulePickerlReminder(
    context: Context,
    vehicle: String,
    nextDate: String
) {

    if (
        nextDate.isBlank()
    ) return

    val expiry =
        try {
            LocalDate.parse(
                nextDate
            )
        } catch (
            _: Exception
        ) {
            return
        }

    val reminderDate =
        expiry.minusMonths(
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
                vehicle
            )

            putExtra(
                "nextDate",
                nextDate
            )
        }

    val pending =
        PendingIntent.getBroadcast(
            context,
            vehicle.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                PendingIntent.FLAG_IMMUTABLE
        )

    val alarmManager =
        context.getSystemService(
            Context.ALARM_SERVICE
        ) as AlarmManager

    if (
        trigger <=
        System.currentTimeMillis()
    ) {

        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis() +
                5_000L,
            pending
        )

    } else {

        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            trigger,
            pending
        )
    }
}

private fun cancelPickerlReminder(
    context: Context,
    vehicle: String
) {

    val intent =
        Intent(
            context,
            PickerlReceiver::class.java
        )

    val pending =
        PendingIntent.getBroadcast(
            context,
            vehicle.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                PendingIntent.FLAG_IMMUTABLE
        )

    val alarmManager =
        context.getSystemService(
            Context.ALARM_SERVICE
        ) as AlarmManager

    alarmManager.cancel(
        pending
    )
}

@Composable
private fun AutoCheckApp() {

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

private fun migrateVehicleName(
    store: VehicleStore,
    oldName: String,
    newName: String
) {

    store.saveRepairs(
        store.loadRepairs().map {

            if (
                it.vehicle ==
                oldName
            ) {
                it.copy(
                    vehicle =
                        newName
                )
            } else {
                it
            }
        }
    )

    store.saveMaintenance(
        store.loadMaintenance().map {

            if (
                it.vehicle ==
                oldName
            ) {
                it.copy(
                    vehicle =
                        newName
                )
            } else {
                it
            }
        }
    )

    store.savePickerl(
        store.loadPickerl().map {

            if (
                it.vehicle ==
                oldName
            ) {
                it.copy(
                    vehicle =
                        newName
                )
            } else {
                it
            }
        }
    )

    store.saveTires(
        store.loadTires().map {

            if (
                it.vehicle ==
                oldName
            ) {
                it.copy(
                    vehicle =
                        newName
                )
            } else {
                it
            }
        }
    )
}

@Composable
private fun HomeScreen(
    visible: Boolean,
    visitedStore: VisitedStore,
    onSelect: (Screen) -> Unit
) {

    val items =
        listOf(

            MenuItemData(
                "Mein Auto",
                "Fahrzeug & Details",
                Icons.Filled.DirectionsCar,
                Color(0xFFE51B2A),
                Screen.AUTO
            ),

            MenuItemData(
                "Reparaturen",
                "Reparaturen verwalten",
                Icons.Filled.CarRepair,
                Color(0xFF1688E8),
                Screen.REPARATUREN
            ),

            MenuItemData(
                "Pickerl/TÜV",
                "Termine & Fristen",
                Icons.Filled.Event,
                Color(0xFF20C75A),
                Screen.PICKERL
            ),

            MenuItemData(
                "Wartungen",
                "Verschiedenes",
                Icons.Filled.Build,
                Color(0xFFE59A18),
                Screen.WARTUNGEN
            ),

            MenuItemData(
                "Gesamtblick",
                "Die wichtigsten Infos",
                Icons.Filled.Visibility,
                Color(0xFF9A4DFF),
                Screen.GESAMTBLICK
            ),

            MenuItemData(
                "Reifen",
                "Größen, Dimensionen und Alter",
                Icons.Filled.TireRepair,
                Color(0xFF17C8BD),
                Screen.REIFEN
            )
        )

    LazyColumn(

        modifier =
            Modifier
                .fillMaxSize()
                .background(
                    Background
                ),

        horizontalAlignment =
            Alignment.CenterHorizontally,

        contentPadding =
            androidx.compose.foundation
                .layout
                .PaddingValues(
                    start = 14.dp,
                    end = 14.dp,
                    top = 22.dp,
                    bottom = 24.dp
                ),

        verticalArrangement =
            Arrangement.spacedBy(
                14.dp
            )
    ) {

        item {

            Image(

                painter =
                    painterResource(
                        R.drawable.bild_4
                    ),

                contentDescription =
                    "AutoCheck",

                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(
                            72.dp
                        ),

                contentScale =
                    ContentScale.Fit
            )
        }

        item {

            Card(

                modifier =
                    Modifier
                        .fillMaxWidth(),

                shape =
                    RoundedCornerShape(
                        22.dp
                    ),

                colors =
                    CardDefaults
                        .cardColors(
                            containerColor =
                                Color.Black
                        )
            ) {

                Image(

                    painter =
                        painterResource(
                            R.drawable.bild_3
                        ),

                    contentDescription =
                        "AutoCheck Fahrzeug",

                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(
                                245.dp
                            ),

                    contentScale =
                        ContentScale.Crop
                )
            }
        }

        items(
            items.chunked(
                2
            )
        ) { row ->

            Row(

                modifier =
                    Modifier
                        .fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.spacedBy(
                        10.dp
                    )
            ) {

                row.forEach { item ->

                    AnimatedVisibility(

                        visible =
                            visible,

                        modifier =
                            Modifier
                                .weight(
                                    1f
                                ),

                        enter =
                            slideInHorizontally(

                                animationSpec =
                                    tween(
                                        2500
                                    ),

                                initialOffsetX = {
                                    fullWidth ->

                                    if (
                                        row.indexOf(
                                            item
                                        ) == 0
                                    ) {
                                        -fullWidth
                                    } else {
                                        fullWidth
                                    }
                                }
                            )
                    ) {

                        MenuCard(

                            item =
                                item,

                            visited =
                                visitedStore
                                    .isVisited(
                                        item.screen
                                    ),

                            onClick = {
                                onSelect(
                                    item.screen
                                )
                            }
                        )
                    }
                }

                if (
                    row.size == 1
                ) {

                    Spacer(
                        Modifier
                            .weight(
                                1f
                            )
                    )
                }
            }
        }

        item {

            Text(

                "AutoCheck • Verliere nicht die Übersicht",

                color =
                    Muted,

                fontSize =
                    12.sp,

                modifier =
                    Modifier.padding(
                        top = 2.dp
                    )
            )
        }
    }
}

@Composable
private fun MenuCard(
    item: MenuItemData,
    visited: Boolean,
    onClick: () -> Unit
) {

    Card(

        modifier =
            Modifier
                .fillMaxWidth()
                .height(
                    104.dp
                )
                .clickable(
                    onClick =
                        onClick
                ),

        shape =
            RoundedCornerShape(
                17.dp
            ),

        colors =
            CardDefaults
                .cardColors(
                    containerColor =
                        Surface
                ),

        elevation =
            CardDefaults
                .cardElevation(
                    defaultElevation =
                        5.dp
                )
    ) {

        Row(

            modifier =
                Modifier
                    .fillMaxSize()
                    .background(

                        Brush.horizontalGradient(

                            listOf(

                                item.accent.copy(
                                    alpha =
                                        0.20f
                                ),

                                Color.Transparent
                            )
                        )
                    )
                    .padding(
                        11.dp
                    ),

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Box(

                modifier =
                    Modifier
                        .size(
                            52.dp
                        )
                        .clip(
                            RoundedCornerShape(
                                14.dp
                            )
                        )
                        .background(
                            item.accent.copy(
                                alpha =
                                    0.90f
                            )
                        ),

                contentAlignment =
                    Alignment.Center
            ) {

                Icon(

                    item.icon,

                    item.title,

                    tint =
                        Color.White,

                    modifier =
                        Modifier.size(
                            29.dp
                        )
                )
            }

            Spacer(
                Modifier.width(
                    10.dp
                )
            )

            Column(

                modifier =
                    Modifier.weight(
                        1f
                    )
            ) {

                Text(

                    item.title,

                    color =
                        Color.White,

                    fontSize =
                        15.sp,

                    fontWeight =
                        FontWeight.Bold
                )

                Text(

                    item.subtitle,

                    color =
                        Muted,

                    fontSize =
                        10.sp,

                    lineHeight =
                        12.sp,

                    maxLines =
                        2
                )

                if (
                    visited
                ) {

                    Row(

                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {

                        Icon(

                            Icons.Filled
                                .CheckCircle,

                            contentDescription =
                                null,

                            tint =
                                item.accent,

                            modifier =
                                Modifier.size(
                                    13.dp
                                )
                        )

                        Spacer(
                            Modifier.width(
                                3.dp
                            )
                        )

                        Text(

                            "Ich war hier",

                            color =
                                item.accent,

                            fontSize =
                                9.sp
                        )
                    }
                }
            }

            Icon(

                Icons.Filled
                    .KeyboardArrowRight,

                contentDescription =
                    "Öffnen",

                tint =
                    item.accent,

                modifier =
                    Modifier.size(
                        24.dp
                    )
            )
        }
    }
}

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
                    Text(
                        title
                    )
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
                                Color(
                                    0xFF050608
                                ),
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

@Composable
private fun VehicleScreen(
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
                            12.dp
                        )
                    )
                    .background(
                        Color(
                            0xFF20242D
                        )
                    ),

            contentAlignment =
                Alignment.Center
        ) {

            Icon(

                Icons.Filled
                    .DirectionsCar,

                contentDescription =
                    null,

                tint =
                    Color.White,

                modifier =
                    Modifier.size(
                        36.dp
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
        >(
            null
        )
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
                        12.dp
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
                            12.dp
                        )
                    )
                    .background(
                        Color(
                            0xFF20242D
                        )
                    ),

            contentAlignment =
                Alignment.Center
        ) {

            Icon(

                Icons.Filled
                    .DirectionsCar,

                contentDescription =
                    null,

                tint =
                    Color.White,

                modifier =
                    Modifier.size(
                        36.dp
                    )
            )
        }
    }
}

@Composable
private fun VehicleSelector(
    vehicles: List<Vehicle>,
    activeVehicle: String,
    onSelected: (String) -> Unit
) {

    var expanded by remember {
        mutableStateOf(
            false
        )
    }

    if (
        vehicles.isEmpty()
    ) {

        Text(

            "Bitte zuerst unter „Mein Auto“ ein Fahrzeug anlegen.",

            color =
                Color(
                    0xFFB8BEC8
                )
        )

        return
    }

    val selected =
        vehicles
            .firstOrNull {
                it.name ==
                    activeVehicle
            }
            ?.name
            ?: vehicles
                .first()
                .name

    Box {

        OutlinedTextField(

            value =
                selected,

            onValueChange = {},

            readOnly =
                true,

            label = {
                Text(
                    "Aktives Fahrzeug"
                )
            },

            modifier =
                Modifier
                    .fillMaxWidth()
                    .clickable {
                        expanded =
                            true
                    }
        )

        DropdownMenu(

            expanded =
                expanded,

            onDismissRequest = {
                expanded =
                    false
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

                        expanded =
                            false

                        onSelected(
                            vehicle.name
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun RepairScreen(
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

@Composable
private fun PickerlScreen(
    store: VehicleStore,
    vehicles: List<Vehicle>,
    activeVehicle: String,
    onActiveVehicle: (String) -> Unit,
    onVisited: () -> Unit
) {

    LaunchedEffect(Unit) {
        onVisited()
    }

    var lastDate by remember {
        mutableStateOf(
            ""
        )
    }

    var nextDate by remember {
        mutableStateOf(
            ""
        )
    }

    var notes by remember {
        mutableStateOf(
            ""
        )
    }

    var reminder by remember {
        mutableStateOf(
            true
        )
    }

    var saved by remember {
        mutableStateOf(
            false
        )
    }

    val context =
        LocalContext.current

    val all =
        remember {

            mutableStateOf(
                store.loadPickerl()
            )
        }

    LaunchedEffect(
        activeVehicle
    ) {

        val entry =
            all.value.firstOrNull {
                it.vehicle ==
                    activeVehicle
            }

        lastDate =
            entry?.lastDate
                ?: ""

        nextDate =
            entry?.nextDate
                ?: ""

        notes =
            entry?.notes
                ?: ""

        reminder =
            entry?.reminder
                ?: true

        saved =
            entry != null
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

            item {

                CardForm {

                    Text(

                        "Pickerl / TÜV",

                        color =
                            Color.White,

                        fontWeight =
                            FontWeight.Bold,

                        fontSize =
                            20.sp
                    )

                    Spacer(
                        Modifier.height(
                            4.dp
                        )
                    )

                    FormField(

                        "Letzte Prüfung (YYYY-MM-DD)",

                        lastDate

                    ) {
                        lastDate =
                            it
                    }

                    FormField(

                        "Nächste Prüfung (YYYY-MM-DD)",

                        nextDate

                    ) {
                        nextDate =
                            it
                    }

                    FormField(

                        "Notizen",

                        notes

                    ) {
                        notes =
                            it
                    }

                    Row(

                        modifier =
                            Modifier.fillMaxWidth(),

                        verticalAlignment =
                            Alignment.CenterVertically,

                        horizontalArrangement =
                            Arrangement.SpaceBetween
                    ) {

                        Text(

                            "Erinnerung 3 Monate vorher",

                            color =
                                Color.White
                        )

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

                        onClick = {

                            val entry =
                                Pickerl(

                                    vehicle =
                                        activeVehicle,

                                    lastDate =
                                        lastDate.trim(),

                                    nextDate =
                                        nextDate.trim(),

                                    notes =
                                        notes.trim(),

                                    reminder =
                                        reminder
                                )

                            val updated =
                                all.value
                                    .filterNot {
                                        it.vehicle ==
                                            activeVehicle
                                    } +
                                    entry

                            all.value =
                                updated

                            store.savePickerl(
                                updated
                            )

                            if (
                                reminder &&
                                nextDate.isNotBlank()
                            ) {

                                schedulePickerlReminder(

                                    context,

                                    activeVehicle,

                                    nextDate.trim()
                                )

                            } else {

                                cancelPickerlReminder(

                                    context,

                                    activeVehicle
                                )
                            }

                            saved =
                                true
                        }

                    ) {

                        Text(

                            if (
                                saved
                            ) {
                                "Pickerl gespeichert"
                            } else {
                                "Pickerl speichern"
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MaintenanceScreen(
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

    var notes by remember {
        mutableStateOf(
            ""
        )
    }

    var entries by remember {
        mutableStateOf(
            store.loadMaintenance()
        )
    }

    val list =
        entries.filter {
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
                        "Noch keine Wartung eingetragen."
                    )
                }
            }

            items(
                list
            ) { entry ->

                RecordCard(

                    title =
                        "Wartung",

                    lines =
                        listOf(

                            "Datum: ${entry.date}",

                            "Kilometerstand: ${entry.mileage}",

                            "Kosten: ${entry.cost}",

                            "Werkstatt: ${entry.workshop}",

                            "Notizen: ${entry.notes}"
                        ),

                    onDelete = {

                        entries =
                            entries.filterNot {
                                it.id ==
                                    entry.id
                            }

                        store.saveMaintenance(
                            entries
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

                        FormField(
                            "Notizen",
                            notes
                        ) {
                            notes =
                                it
                        }

                        FormButtons(

                            onSave = {

                                val updated =
                                    entries +
                                        Maintenance(

                                            id =
                                                System
                                                    .currentTimeMillis(),

                                            vehicle =
                                                activeVehicle,

                                            date =
                                                date.trim(),

                                            mileage =
                                                mileage.trim(),

                                            cost =
                                                cost.trim(),

                                            workshop =
                                                workshop.trim(),

                                            notes =
                                                notes.trim()
                                        )

                                entries =
                                    updated

                                store.saveMaintenance(
                                    updated
                                )

                                date =
                                    ""

                                mileage =
                                    ""

                                cost =
                                    ""

                                workshop =
                                    ""

                                notes =
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
                    "Wartung hinzufügen"
                )
            }
        }
    }
}

@Composable
private fun TireScreen(
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

    var season by remember {
        mutableStateOf(
            "Sommer"
        )
    }

    var dimension by remember {
        mutableStateOf(
            ""
        )
    }

    var brand by remember {
        mutableStateOf(
            ""
        )
    }

    var dot by remember {
        mutableStateOf(
            ""
        )
    }

    var tread by remember {
        mutableStateOf(
            ""
        )
    }

    var condition by remember {
        mutableStateOf(
            ""
        )
    }

    var storage by remember {
        mutableStateOf(
            ""
        )
    }

    var tires by remember {
        mutableStateOf(
            store.loadTires()
        )
    }

    val list =
        tires.filter {
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
                        "Noch kein Reifensatz eingetragen."
                    )
                }
            }

            items(
                list
            ) { tire ->

                RecordCard(

                    title =
                        "${tire.season} – ${tire.dimension}",

                    lines =
                        listOf(

                            "Marke: ${tire.brand}",

                            "DOT: ${tire.dot}",

                            "Profiltiefe: ${tire.tread}",

                            "Zustand: ${tire.condition}",

                            "Lagerung: ${tire.storage}"
                        ),

                    onDelete = {

                        tires =
                            tires.filterNot {
                                it.id ==
                                    tire.id
                            }

                        store.saveTires(
                            tires
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
                            "Saison (Sommer/Winter)",
                            season
                        ) {
                            season =
                                it
                        }

                        FormField(
                            "Dimension",
                            dimension
                        ) {
                            dimension =
                                it
                        }

                        FormField(
                            "Marke",
                            brand
                        ) {
                            brand =
                                it
                        }

                        FormField(
                            "DOT",
                            dot
                        ) {
                            dot =
                                it
                        }

                        FormField(
                            "Profiltiefe",
                            tread
                        ) {
                            tread =
                                it
                        }

                        FormField(
                            "Zustand",
                            condition
                        ) {
                            condition =
                                it
                        }

                        FormField(
                            "Lagerung",
                            storage
                        ) {
                            storage =
                                it
                        }

                        FormButtons(

                            onSave = {

                                val updated =
                                    tires +
                                        TireSet(

                                            id =
                                                System
                                                    .currentTimeMillis(),

                                            vehicle =
                                                activeVehicle,

                                            season =
                                                season.trim(),

                                            dimension =
                                                dimension.trim(),

                                            brand =
                                                brand.trim(),

                                            dot =
                                                dot.trim(),

                                            tread =
                                                tread.trim(),

                                            condition =
                                                condition.trim(),

                                            storage =
                                                storage.trim()
                                        )

                                tires =
                                    updated

                                store.saveTires(
                                    updated
                                )

                                dimension =
                                    ""

                                brand =
                                    ""

                                dot =
                                    ""

                                tread =
                                    ""

                                condition =
                                    ""

                                storage =
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
                    "Reifensatz hinzufügen"
                )
            }
        }
    }
}

@Composable
private fun OverviewScreen(
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

@Composable
private fun CardForm(
    content:
        @Composable
        ColumnScope.() -> Unit
) {

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
                ),

            content =
                content
        )
    }
}

@Composable
private fun FormField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {

    OutlinedTextField(

        value =
            value,

        onValueChange =
            onValueChange,

        label = {
            Text(
                label
            )
        },

        modifier =
            Modifier.fillMaxWidth()
    )
}

@Composable
private fun FormButtons(
    onSave: () -> Unit,
    onCancel: () -> Unit
) {

    Row(

        horizontalArrangement =
            Arrangement.spacedBy(
                8.dp
            )
    ) {

        Button(
            onClick =
                onSave
        ) {

            Text(
                "Speichern"
            )
        }

        OutlinedButton(
            onClick =
                onCancel
        ) {

            Text(
                "Abbrechen"
            )
        }
    }
}

@Composable
private fun EmptyCard(
    text: String
) {

    CardForm {

        Text(

            text,

            color =
                Color.White,

            fontWeight =
                FontWeight.Bold
        )
    }
}

@Composable
private fun RecordCard(
    title: String,
    lines: List<String>,
    onDelete: () -> Unit
) {

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

            Text(

                title,

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

            lines
                .filter {
                    it.substringAfter(
                        ":"
                    ).isNotBlank()
                }
                .forEach {

                    Text(

                        it,

                        color =
                            Color(
                                0xFFB8BEC8
                            )
                    )
                }

            Spacer(
                Modifier.height(
                    8.dp
                )
            )

            OutlinedButton(
                onClick =
                    onDelete
            ) {

                Text(
                    "Eintrag löschen"
                )
            }
        }
    }
}
