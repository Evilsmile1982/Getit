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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CarRepair
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
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
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import de.autocheck.app.ui.AutoCheckTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.ZoneId
import java.util.UUID

private val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

/* ---------------------------------------------------------
   DATENMODELLE
   --------------------------------------------------------- */

data class Vehicle(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val brand: String = "",
    val model: String = "",
    val year: String = "",
    val plate: String = "",
    val vin: String = "",
    val imageUri: String = "",
    val wasHere: Boolean = false
)

data class Repair(
    val id: String = UUID.randomUUID().toString(),
    val vehicleId: String = "",
    val date: String = "",
    val mileage: String = "",
    val description: String = "",
    val cost: String = "",
    val workshop: String = ""
)

data class Maintenance(
    val id: String = UUID.randomUUID().toString(),
    val vehicleId: String = "",
    val date: String = "",
    val mileage: String = "",
    val description: String = "",
    val cost: String = "",
    val workshop: String = "",
    val notes: String = ""
)

data class PickerlData(
    val vehicleId: String = "",
    val lastDate: String = "",
    val nextDate: String = "",
    val notes: String = "",
    val reminder: Boolean = true
)

data class TireSet(
    val id: String = UUID.randomUUID().toString(),
    val vehicleId: String = "",
    val season: String = "",
    val dimension: String = "",
    val brand: String = "",
    val dot: String = "",
    val tread: String = "",
    val condition: String = "",
    val storage: String = ""
)

/* ---------------------------------------------------------
   SPEICHER
   --------------------------------------------------------- */

class Store(context: Context) {

    private val prefs =
        context.getSharedPreferences("autocheck_data", Context.MODE_PRIVATE)

    val vehicles = mutableStateListOf<Vehicle>()
    val repairs = mutableStateListOf<Repair>()
    val maintenances = mutableStateListOf<Maintenance>()
    val pickerls = mutableStateListOf<PickerlData>()
    val tires = mutableStateListOf<TireSet>()

    var activeVehicleId by mutableStateOf<String?>(null)

    init {
        loadAll()
    }

    private fun loadAll() {
        loadVehicles()
        loadRepairs()
        loadMaintenances()
        loadPickerls()
        loadTires()

        activeVehicleId =
            prefs.getString("activeVehicleId", null)
                ?: vehicles.firstOrNull()?.id
    }

    fun setActiveVehicle(id: String) {
        activeVehicleId = id
        prefs.edit()
            .putString("activeVehicleId", id)
            .apply()
    }

    fun saveVehicles(list: List<Vehicle>) {
        vehicles.clear()
        vehicles.addAll(list)

        val array = JSONArray()

        list.forEach { vehicle ->
            array.put(
                JSONObject().apply {
                    put("id", vehicle.id)
                    put("name", vehicle.name)
                    put("brand", vehicle.brand)
                    put("model", vehicle.model)
                    put("year", vehicle.year)
                    put("plate", vehicle.plate)
                    put("vin", vehicle.vin)
                    put("imageUri", vehicle.imageUri)
                    put("wasHere", vehicle.wasHere)
                }
            )
        }

        prefs.edit()
            .putString("vehicles", array.toString())
            .apply()
    }

    private fun loadVehicles() {
        vehicles.clear()

        val raw =
            prefs.getString("vehicles", "[]") ?: "[]"

        val array = JSONArray(raw)

        for (i in 0 until array.length()) {
            val o = array.getJSONObject(i)

            vehicles.add(
                Vehicle(
                    id = o.optString("id"),
                    name = o.optString("name"),
                    brand = o.optString("brand"),
                    model = o.optString("model"),
                    year = o.optString("year"),
                    plate = o.optString("plate"),
                    vin = o.optString("vin"),
                    imageUri = o.optString("imageUri"),
                    wasHere = o.optBoolean("wasHere")
                )
            )
        }
    }

    fun saveRepairs(list: List<Repair>) {
        repairs.clear()
        repairs.addAll(list)

        val array = JSONArray()

        list.forEach {
            array.put(
                JSONObject().apply {
                    put("id", it.id)
                    put("vehicleId", it.vehicleId)
                    put("date", it.date)
                    put("mileage", it.mileage)
                    put("description", it.description)
                    put("cost", it.cost)
                    put("workshop", it.workshop)
                }
            )
        }

        prefs.edit()
            .putString("repairs", array.toString())
            .apply()
    }

    private fun loadRepairs() {
        repairs.clear()

        val array =
            JSONArray(
                prefs.getString("repairs", "[]") ?: "[]"
            )

        for (i in 0 until array.length()) {
            val o = array.getJSONObject(i)

            repairs.add(
                Repair(
                    id = o.optString("id"),
                    vehicleId = o.optString("vehicleId"),
                    date = o.optString("date"),
                    mileage = o.optString("mileage"),
                    description = o.optString("description"),
                    cost = o.optString("cost"),
                    workshop = o.optString("workshop")
                )
            )
        }
    }

    fun saveMaintenances(list: List<Maintenance>) {
        maintenances.clear()
        maintenances.addAll(list)

        val array = JSONArray()

        list.forEach {
            array.put(
                JSONObject().apply {
                    put("id", it.id)
                    put("vehicleId", it.vehicleId)
                    put("date", it.date)
                    put("mileage", it.mileage)
                    put("description", it.description)
                    put("cost", it.cost)
                    put("workshop", it.workshop)
                    put("notes", it.notes)
                }
            )
        }

        prefs.edit()
            .putString("maintenances", array.toString())
            .apply()
    }

    private fun loadMaintenances() {
        maintenances.clear()

        val array =
            JSONArray(
                prefs.getString("maintenances", "[]") ?: "[]"
            )

        for (i in 0 until array.length()) {
            val o = array.getJSONObject(i)

            maintenances.add(
                Maintenance(
                    id = o.optString("id"),
                    vehicleId = o.optString("vehicleId"),
                    date = o.optString("date"),
                    mileage = o.optString("mileage"),
                    description = o.optString("description"),
                    cost = o.optString("cost"),
                    workshop = o.optString("workshop"),
                    notes = o.optString("notes")
                )
            )
        }
    }

    fun savePickerl(data: PickerlData) {
        val updated =
            pickerls
                .filter { it.vehicleId != data.vehicleId }
                .toMutableList()

        updated.add(data)

        pickerls.clear()
        pickerls.addAll(updated)

        val array = JSONArray()

        pickerls.forEach {
            array.put(
                JSONObject().apply {
                    put("vehicleId", it.vehicleId)
                    put("lastDate", it.lastDate)
                    put("nextDate", it.nextDate)
                    put("notes", it.notes)
                    put("reminder", it.reminder)
                }
            )
        }

        prefs.edit()
            .putString("pickerls", array.toString())
            .apply()
    }

    private fun loadPickerls() {
        pickerls.clear()

        val array =
            JSONArray(
                prefs.getString("pickerls", "[]") ?: "[]"
            )

        for (i in 0 until array.length()) {
            val o = array.getJSONObject(i)

            pickerls.add(
                PickerlData(
                    vehicleId = o.optString("vehicleId"),
                    lastDate = o.optString("lastDate"),
                    nextDate = o.optString("nextDate"),
                    notes = o.optString("notes"),
                    reminder = o.optBoolean("reminder", true)
                )
            )
        }
    }

    fun saveTires(list: List<TireSet>) {
        tires.clear()
        tires.addAll(list)

        val array = JSONArray()

        list.forEach {
            array.put(
                JSONObject().apply {
                    put("id", it.id)
                    put("vehicleId", it.vehicleId)
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
            .putString("tires", array.toString())
            .apply()
    }

    private fun loadTires() {
        tires.clear()

        val array =
            JSONArray(
                prefs.getString("tires", "[]") ?: "[]"
            )

        for (i in 0 until array.length()) {
            val o = array.getJSONObject(i)

            tires.add(
                TireSet(
                    id = o.optString("id"),
                    vehicleId = o.optString("vehicleId"),
                    season = o.optString("season"),
                    dimension = o.optString("dimension"),
                    brand = o.optString("brand"),
                    dot = o.optString("dot"),
                    tread = o.optString("tread"),
                    condition = o.optString("condition"),
                    storage = o.optString("storage")
                )
            )
        }
    }
}

/* ---------------------------------------------------------
   NAVIGATION
   --------------------------------------------------------- */

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
    val icon: ImageVector,
    val screen: Screen
)

/* ---------------------------------------------------------
   MAIN
   --------------------------------------------------------- */

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createNotificationChannel(this)

        setContent {
            AutoCheckTheme {
                AutoCheckApp()
            }
        }
    }
}

/* ---------------------------------------------------------
   APP
   --------------------------------------------------------- */

@Composable
private fun AutoCheckApp() {

    val context = LocalContext.current

    val store =
        remember {
            Store(context)
        }

    var screen by remember {
        mutableStateOf(Screen.HOME)
    }

    var homeReady by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(2500)
        homeReady = true
    }

    val activeVehicle =
        store.vehicles.firstOrNull {
            it.id == store.activeVehicleId
        } ?: store.vehicles.firstOrNull()

    if (screen == Screen.HOME) {

        HomeScreen(
            visible = homeReady,
            onSelect = {
                screen = it
            }
        )

    } else {

        DetailScaffold(
            title =
                when (screen) {
                    Screen.AUTO -> "Mein Auto"
                    Screen.REPARATUREN -> "Reparaturen"
                    Screen.PICKERL -> "Pickerl / TÜV"
                    Screen.WARTUNGEN -> "Wartungen"
                    Screen.GESAMTBLICK -> "Gesamtblick"
                    Screen.REIFEN -> "Reifen"
                    else -> "AutoCheck"
                },
            onBack = {
                screen = Screen.HOME
            }
        ) {

            when (screen) {

                Screen.AUTO -> {
                    VehicleScreen(store)
                }

                Screen.REPARATUREN -> {
                    RepairsScreen(
                        store = store,
                        activeVehicle = activeVehicle
                    )
                }

                Screen.PICKERL -> {
                    PickerlScreen(
                        store = store,
                        activeVehicle = activeVehicle
                    )
                }

                Screen.WARTUNGEN -> {
                    MaintenanceScreen(
                        store = store,
                        activeVehicle = activeVehicle
                    )
                }

                Screen.GESAMTBLICK -> {
                    OverviewScreen(
                        store = store,
                        activeVehicle = activeVehicle
                    )
                }

                Screen.REIFEN -> {
                    TiresScreen(
                        store = store,
                        activeVehicle = activeVehicle
                    )
                }

                else -> Unit
            }
        }
    }
}

/* ---------------------------------------------------------
   CUT1 HOME – DIESE STRUKTUR BLEIBT
   --------------------------------------------------------- */

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
                .background(Color(0xFF050608))
                .padding(horizontal = 14.dp)
    ) {

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(
                Modifier.height(28.dp)
            )

            Text(
                "AutoCheck",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(
                Modifier.height(8.dp)
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
                Modifier.height(10.dp)
            )

            Column(
                modifier =
                    Modifier.fillMaxWidth(),
                verticalArrangement =
                    Arrangement.spacedBy(10.dp)
            ) {

                for (row in 0..2) {

                    Row(
                        modifier =
                            Modifier.fillMaxWidth(),
                        horizontalArrangement =
                            Arrangement.spacedBy(10.dp)
                    ) {

                        for (col in 0..1) {

                            val item =
                                items[row * 2 + col]

                            AnimatedVisibility(
                                visible = visible,
                                enter =
                                    slideInHorizontally(
                                        animationSpec =
                                            tween(2500),
                                        initialOffsetX = {
                                            if (col == 0) {
                                                -it
                                            } else {
                                                it
                                            }
                                        }
                                    )
                            ) {

                                MenuCard(
                                    item = item,
                                    modifier =
                                        Modifier.weight(1f),
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
                Modifier.height(20.dp)
            )
        }
    }
}

/* ---------------------------------------------------------
   CUT1 MENÜKARTE
   --------------------------------------------------------- */

@Composable
private fun MenuCard(
    item: MenuItemData,
    modifier: Modifier,
    onClick: () -> Unit
) {

    Card(
        modifier = modifier,
        onClick = onClick,
        shape =
            RoundedCornerShape(16.dp),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    Color(0xFF11141A)
            )
    ) {

        Row(
            modifier =
                Modifier.padding(13.dp),
            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Box(
                modifier =
                    Modifier
                        .size(46.dp)
                        .clip(
                            RoundedCornerShape(12.dp)
                        )
                        .background(
                            Color(0xFFE21D32)
                        ),
                contentAlignment =
                    Alignment.Center
            ) {

                Icon(
                    imageVector = item.icon,
                    contentDescription =
                        item.title,
                    tint = Color.White
                )
            }

            Spacer(
                Modifier.width(10.dp)
            )

            Column {

                Text(
                    item.title,
                    fontWeight =
                        FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    item.subtitle,
                    fontSize = 11.sp,
                    color =
                        Color(0xFFB8BEC8),
                    maxLines = 2
                )
            }
        }
    }
}

/* ---------------------------------------------------------
   DETAIL LAYOUT
   --------------------------------------------------------- */

@OptIn(ExperimentalMaterial3Api::class)
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
                        onClick = onBack
                    ) {

                        Icon(
                            Icons.Filled.Menu,
                            contentDescription =
                                "Zurück"
                        )
                    }
                },

                colors =
                    TopAppBarDefaults.topAppBarColors(
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
                    .padding(padding)
        ) {
            content()
        }
    }
}

/* ---------------------------------------------------------
   FAHRZEUG
   --------------------------------------------------------- */

@Composable
private fun VehicleScreen(
    store: Store
) {

    var editingVehicle by remember {
        mutableStateOf<Vehicle?>(null)
    }

    var adding by remember {
        mutableStateOf(false)
    }

    if (adding || editingVehicle != null) {

        VehicleForm(
            existing = editingVehicle,
            onCancel = {
                adding = false
                editingVehicle = null
            },
            onSave = { vehicle ->

                if (editingVehicle == null) {

                    store.saveVehicles(
                        store.vehicles + vehicle
                    )

                } else {

                    store.saveVehicles(
                        store.vehicles.map {
                            if (it.id == vehicle.id) {
                                vehicle
                            } else {
                                it
                            }
                        }
                    )
                }

                if (store.activeVehicleId == null) {
                    store.setActiveVehicle(vehicle.id)
                }

                adding = false
                editingVehicle = null
            }
        )

        return
    }

    LazyColumn(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(16.dp),
        verticalArrangement =
            Arrangement.spacedBy(12.dp)
    ) {

        item {

            Text(
                "${store.vehicles.size}/5 Fahrzeuge",
                color =
                    Color(0xFFB8BEC8),
                modifier =
                    Modifier.padding(
                        bottom = 4.dp
                    )
            )
        }

        if (store.vehicles.isEmpty()) {

            item {

                EmptyVehicleMessage()
            }
        }

        items(
            store.vehicles,
            key = {
                it.id
            }
        ) { vehicle ->

            VehicleCard(
                vehicle = vehicle,
                active =
                    vehicle.id ==
                        store.activeVehicleId,
                onSelect = {
                    store.setActiveVehicle(
                        vehicle.id
                    )
                },
                onEdit = {
                    editingVehicle = vehicle
                },
                onDelete = {

                    val remaining =
                        store.vehicles.filter {
                            it.id != vehicle.id
                        }

                    store.saveVehicles(
                        remaining
                    )

                    if (
                        store.activeVehicleId ==
                        vehicle.id
                    ) {

                        remaining
                            .firstOrNull()
                            ?.id
                            ?.let {
                                store.setActiveVehicle(
                                    it
                                )
                            }
                    }
                },
                onWasHere = {

                    store.saveVehicles(
                        store.vehicles.map {
                            if (it.id ==
                                vehicle.id
                            ) {
                                it.copy(
                                    wasHere = true
                                )
                            } else {
                                it
                            }
                        }
                    )
                }
            )
        }

        if (store.vehicles.size < 5) {

            item {

                Button(
                    onClick = {
                        adding = true
                    },
                    modifier =
                        Modifier.fillMaxWidth()
                ) {

                    Icon(
                        Icons.Filled.Add,
                        contentDescription = null
                    )

                    Spacer(
                        Modifier.width(8.dp)
                    )

                    Text(
                        "Fahrzeug hinzufügen"
                    )
                }
            }
        }
    }
}

/* ---------------------------------------------------------
   FAHRZEUG KARTE
   --------------------------------------------------------- */

@Composable
private fun VehicleCard(
    vehicle: Vehicle,
    active: Boolean,
    onSelect: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onWasHere: () -> Unit
) {

    Card(
        modifier =
            Modifier.fillMaxWidth(),
        shape =
            RoundedCornerShape(20.dp),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    if (active) {
                        Color(0xFF1B1E26)
                    } else {
                        Color(0xFF11141A)
                    }
            )
    ) {

        Column(
            modifier =
                Modifier.padding(16.dp)
        ) {

            VehicleImage(
                uri = vehicle.imageUri
            )

            Spacer(
                Modifier.height(12.dp)
            )

            Row(
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Column(
                    modifier =
                        Modifier.weight(1f)
                ) {

                    Text(
                        vehicle.name.ifBlank {
                            "Mein Fahrzeug"
                        },
                        color = Color.White,
                        fontWeight =
                            FontWeight.Bold,
                        fontSize = 20.sp
                    )

                    Text(
                        "${vehicle.brand} ${vehicle.model}"
                            .trim(),
                        color =
                            Color(0xFFB8BEC8)
                    )

                    if (vehicle.year.isNotBlank()) {

                        Text(
                            "Baujahr ${vehicle.year}",
                            color =
                                Color(0xFFB8BEC8)
                        )
                    }

                    if (vehicle.plate.isNotBlank()) {

                        Text(
                            "Kennzeichen: ${vehicle.plate}",
                            color = Color.White
                        )
                    }

                    if (vehicle.vin.isNotBlank()) {

                        Text(
                            "FIN/VIN: ${vehicle.vin}",
                            color =
                                Color(0xFFB8BEC8),
                            fontSize = 12.sp
                        )
                    }
                }

                if (active) {

                    Text(
                        "AKTIV",
                        color =
                            Color(0xFFE21D32),
                        fontWeight =
                            FontWeight.Bold
                    )
                }
            }

            Spacer(
                Modifier.height(10.dp)
            )

            Row(
                horizontalArrangement =
                    Arrangement.spacedBy(8.dp)
            ) {

                Button(
                    onClick = onSelect,
                    modifier =
                        Modifier.weight(1f)
                ) {

                    Text(
                        if (active) {
                            "Ausgewählt"
                        } else {
                            "Auswählen"
                        }
                    )
                }

                IconButton(
                    onClick = onEdit
                ) {

                    Icon(
                        Icons.Filled.Edit,
                        contentDescription =
                            "Bearbeiten",
                        tint = Color.White
                    )
                }

                IconButton(
                    onClick = onDelete
                ) {

                    Icon(
                        Icons.Filled.Delete,
                        contentDescription =
                            "Löschen",
                        tint =
                            Color(0xFFE21D32)
                    )
                }
            }

            Spacer(
                Modifier.height(4.dp)
            )

            OutlinedButton(
                onClick = onWasHere,
                modifier =
                    Modifier.fillMaxWidth()
            ) {

                Text(
                    if (vehicle.wasHere) {
                        "✓ Ich war hier"
                    } else {
                        "Ich war hier"
                    }
                )
            }
        }
    }
}

/* ---------------------------------------------------------
   FAHRZEUG FORMULAR
   --------------------------------------------------------- */

@Composable
private fun VehicleForm(
    existing: Vehicle?,
    onCancel: () -> Unit,
    onSave: (Vehicle) -> Unit
) {

    var name by remember {
        mutableStateOf(
            existing?.name ?: ""
        )
    }

    var brand by remember {
        mutableStateOf(
            existing?.brand ?: ""
        )
    }

    var model by remember {
        mutableStateOf(
            existing?.model ?: ""
        )
    }

    var year by remember {
        mutableStateOf(
            existing?.year ?: ""
        )
    }

    var plate by remember {
        mutableStateOf(
            existing?.plate ?: ""
        )
    }

    var vin by remember {
        mutableStateOf(
            existing?.vin ?: ""
        )
    }

    var imageUri by remember {
        mutableStateOf(
            existing?.imageUri ?: ""
        )
    }

    val imagePicker =
        rememberLauncherForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri ->

            if (uri != null) {
                imageUri = uri.toString()
            }
        }

    LazyColumn(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(18.dp),
        verticalArrangement =
            Arrangement.spacedBy(10.dp)
    ) {

        item {

            Text(
                if (existing == null) {
                    "Fahrzeug hinzufügen"
                } else {
                    "Fahrzeug bearbeiten"
                },
                fontSize = 26.sp,
                fontWeight =
                    FontWeight.Bold,
                color = Color.White
            )

            Spacer(
                Modifier.height(8.dp)
            )

            VehicleImage(
                uri = imageUri
            )

            Spacer(
                Modifier.height(8.dp)
            )

            OutlinedButton(
                onClick = {
                    imagePicker.launch(
                        "image/*"
                    )
                },
                modifier =
                    Modifier.fillMaxWidth()
            ) {

                Text(
                    "Fahrzeugbild auswählen"
                )
            }

            FormField(
                "Bezeichnung",
                name
            ) {
                name = it
            }

            FormField(
                "Marke",
                brand
            ) {
                brand = it
            }

            FormField(
                "Modell",
                model
            ) {
                model = it
            }

            FormField(
                "Baujahr",
                year
            ) {
                year = it
            }

            FormField(
                "Kennzeichen",
                plate
            ) {
                plate = it
            }

            FormField(
                "FIN / VIN",
                vin
            ) {
                vin = it
            }

            Spacer(
                Modifier.height(10.dp)
            )

            Row(
                horizontalArrangement =
                    Arrangement.spacedBy(10.dp)
            ) {

                OutlinedButton(
                    onClick = onCancel,
                    modifier =
                        Modifier.weight(1f)
                ) {

                    Text("Abbrechen")
                }

                Button(
                    enabled =
                        name.isNotBlank(),
                    onClick = {

                        onSave(
                            Vehicle(
                                id =
                                    existing?.id
                                        ?: UUID.randomUUID()
                                            .toString(),
                                name =
                                    name.trim(),
                                brand =
                                    brand.trim(),
                                model =
                                    model.trim(),
                                year =
                                    year.trim(),
                                plate =
                                    plate.trim(),
                                vin =
                                    vin.trim(),
                                imageUri =
                                    imageUri,
                                wasHere =
                                    existing?.wasHere
                                        ?: false
                            )
                        )
                    },
                    modifier =
                        Modifier.weight(1f)
                ) {

                    Text("Speichern")
                }
            }
        }
    }
}

/* ---------------------------------------------------------
   FAHRZEUG AUSWAHL
   --------------------------------------------------------- */

@Composable
private fun VehicleSelector(
    store: Store,
    activeVehicle: Vehicle?
) {

    var expanded by remember {
        mutableStateOf(false)
    }

    Box(
        modifier =
            Modifier.fillMaxWidth()
    ) {

        Button(
            onClick = {
                expanded = true
            },
            modifier =
                Modifier.fillMaxWidth()
        ) {

            Icon(
                Icons.Filled.DirectionsCar,
                contentDescription = null
            )

            Spacer(
                Modifier.width(8.dp)
            )

            Text(
                activeVehicle?.name
                    ?.ifBlank {
                        "Fahrzeug auswählen"
                    }
                    ?: "Fahrzeug auswählen"
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
            }
        ) {

            store.vehicles.forEach { vehicle ->

                DropdownMenuItem(

                    text = {
                        Text(
                            vehicle.name.ifBlank {
                                "${vehicle.brand} ${vehicle.model}"
                            }
                        )
                    },

                    onClick = {

                        store.setActiveVehicle(
                            vehicle.id
                        )

                        expanded = false
                    }
                )
            }
        }
    }
}

/* ---------------------------------------------------------
   REPARATUREN
   --------------------------------------------------------- */

@Composable
private fun RepairsScreen(
    store: Store,
    activeVehicle: Vehicle?
) {

    if (activeVehicle == null) {

        EmptyVehicleMessage()
        return
    }

    var adding by remember {
        mutableStateOf(false)
    }

    if (adding) {

        RepairForm(
            vehicleId = activeVehicle.id,
            onCancel = {
                adding = false
            },
            onSave = {

                store.saveRepairs(
                    store.repairs + it
                )

                adding = false
            }
        )

        return
    }

    LazyColumn(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(18.dp),
        verticalArrangement =
            Arrangement.spacedBy(10.dp)
    ) {

        item {

            VehicleSelector(
                store,
                activeVehicle
            )

            Spacer(
                Modifier.height(10.dp)
            )

            Button(
                onClick = {
                    adding = true
                },
                modifier =
                    Modifier.fillMaxWidth()
            ) {

                Icon(
                    Icons.Filled.Add,
                    contentDescription = null
                )

                Spacer(
                    Modifier.width(8.dp)
                )

                Text(
                    "Reparatur hinzufügen"
                )
            }
        }

        items(
            store.repairs.filter {
                it.vehicleId ==
                    activeVehicle.id
            },
            key = {
                it.id
            }
        ) { repair ->

            DataCard(
                title =
                    repair.description.ifBlank {
                        "Reparatur"
                    },
                lines =
                    listOf(
                        "Datum" to repair.date,
                        "Kilometerstand" to
                            repair.mileage,
                        "Kosten" to repair.cost,
                        "Werkstatt" to
                            repair.workshop
                    ),
                onDelete = {

                    store.saveRepairs(
                        store.repairs.filter {
                            it.id != repair.id
                        }
                    )
                }
            )
        }
    }
}

/* ---------------------------------------------------------
   REPARATUR FORMULAR
   --------------------------------------------------------- */

@Composable
private fun RepairForm(
    vehicleId: String,
    onCancel: () -> Unit,
    onSave: (Repair) -> Unit
) {

    var date by remember {
        mutableStateOf("")
    }

    var mileage by remember {
        mutableStateOf("")
    }

    var description by remember {
        mutableStateOf("")
    }

    var cost by remember {
        mutableStateOf("")
    }

    var workshop by remember {
        mutableStateOf("")
    }

    EntryForm(
        title = "Neue Reparatur",
        fields =
            listOf(
                "Datum" to date,
                "Kilometerstand" to mileage,
                "Beschreibung" to
                    description,
                "Kosten" to cost,
                "Werkstatt" to workshop
            ),
        setters =
            listOf(
                { date = it },
                { mileage = it },
                { description = it },
                { cost = it },
                { workshop = it }
            ),
        onCancel = onCancel,
        onSave = {

            onSave(
                Repair(
                    vehicleId = vehicleId,
                    date = date,
                    mileage = mileage,
                    description =
                        description,
                    cost = cost,
                    workshop =
                        workshop
                )
            )
        }
    )
}

/* ---------------------------------------------------------
   PICKERL
   --------------------------------------------------------- */

@Composable
private fun PickerlScreen(
    store: Store,
    activeVehicle: Vehicle?
) {

    if (activeVehicle == null) {

        EmptyVehicleMessage()
        return
    }

    val context =
        LocalContext.current

    val saved =
        store.pickerls.firstOrNull {
            it.vehicleId ==
                activeVehicle.id
        }

    var lastDate by remember(
        activeVehicle.id,
        saved?.lastDate
    ) {
        mutableStateOf(
            saved?.lastDate ?: ""
        )
    }

    var nextDate by remember(
        activeVehicle.id,
        saved?.nextDate
    ) {
        mutableStateOf(
            saved?.nextDate ?: ""
        )
    }

    var notes by remember(
        activeVehicle.id,
        saved?.notes
    ) {
        mutableStateOf(
            saved?.notes ?: ""
        )
    }

    var reminder by remember(
        activeVehicle.id,
        saved?.reminder
    ) {
        mutableStateOf(
            saved?.reminder ?: true
        )
    }

    LazyColumn(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(18.dp),
        verticalArrangement =
            Arrangement.spacedBy(10.dp)
    ) {

        item {

            VehicleSelector(
                store,
                activeVehicle
            )

            Spacer(
                Modifier.height(14.dp)
            )

            Text(
                "Pickerl / TÜV",
                fontSize = 26.sp,
                fontWeight =
                    FontWeight.Bold,
                color = Color.White
            )

            FormField(
                "Letzte Prüfung",
                lastDate
            ) {
                lastDate = it
            }

            FormField(
                "Nächste Prüfung",
                nextDate
            ) {
                nextDate = it
            }

            FormField(
                "Notizen",
                notes
            ) {
                notes = it
            }

            Spacer(
                Modifier.height(8.dp)
            )

            Row(
                modifier =
                    Modifier.fillMaxWidth(),
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Text(
                    "Erinnerung 3 Monate vorher",
                    color = Color.White,
                    modifier =
                        Modifier.weight(1f)
                )

                Switch(
                    checked = reminder,
                    onCheckedChange = {
                        reminder = it
                    }
                )
            }

            Spacer(
                Modifier.height(10.dp)
            )

            Button(
                onClick = {

                    val data =
                        PickerlData(
                            vehicleId =
                                activeVehicle.id,
                            lastDate =
                                lastDate,
                            nextDate =
                                nextDate,
                            notes =
                                notes,
                            reminder =
                                reminder
                        )

                    store.savePickerl(data)

                    if (
                        reminder &&
                        nextDate.isNotBlank()
                    ) {

                        schedulePickerReminder(
                            context =
                                context,
                            dateText =
                                nextDate,
                            vehicleId =
                                activeVehicle.id
                        )
                    }
                },
                modifier =
                    Modifier.fillMaxWidth()
            ) {

                Icon(
                    Icons.Filled.Notifications,
                    contentDescription = null
                )

                Spacer(
                    Modifier.width(8.dp)
                )

                Text(
                    "Pickerl speichern"
                )
            }

            if (nextDate.isNotBlank()) {

                Spacer(
                    Modifier.height(10.dp)
                )

                Card(
                    modifier =
                        Modifier.fillMaxWidth(),
                    shape =
                        RoundedCornerShape(20.dp),
                    colors =
                        CardDefaults.cardColors(
                            containerColor =
                                Color(0xFF11141A)
                        )
                ) {

                    Column(
                        modifier =
                            Modifier.padding(18.dp)
                    ) {

                        Text(
                            "Nächster Termin",
                            color =
                                Color(0xFFB8BEC8)
                        )

                        Text(
                            nextDate,
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight =
                                FontWeight.Bold
                        )

                        if (notes.isNotBlank()) {

                            Spacer(
                                Modifier.height(8.dp)
                            )

                            Text(
                                notes,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

/* ---------------------------------------------------------
   WARTUNGEN
   --------------------------------------------------------- */

@Composable
private fun MaintenanceScreen(
    store: Store,
    activeVehicle: Vehicle?
) {

    if (activeVehicle == null) {

        EmptyVehicleMessage()
        return
    }

    var adding by remember {
        mutableStateOf(false)
    }

    if (adding) {

        MaintenanceForm(
            vehicleId =
                activeVehicle.id,
            onCancel = {
                adding = false
            },
            onSave = {

                store.saveMaintenances(
                    store.maintenances + it
                )

                adding = false
            }
        )

        return
    }

    LazyColumn(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(18.dp),
        verticalArrangement =
            Arrangement.spacedBy(10.dp)
    ) {

        item {

            VehicleSelector(
                store,
                activeVehicle
            )

            Button(
                onClick = {
                    adding = true
                },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
            ) {

                Icon(
                    Icons.Filled.Add,
                    contentDescription = null
                )

                Spacer(
                    Modifier.width(8.dp)
                )

                Text(
                    "Wartung hinzufügen"
                )
            }
        }

        items(
            store.maintenances.filter {
                it.vehicleId ==
                    activeVehicle.id
            },
            key = {
                it.id
            }
        ) { maintenance ->

            DataCard(
                title =
                    maintenance.description
                        .ifBlank {
                            "Wartung"
                        },
                lines =
                    listOf(
                        "Datum" to
                            maintenance.date,
                        "Kilometerstand" to
                            maintenance.mileage,
                        "Kosten" to
                            maintenance.cost,
                        "Werkstatt" to
                            maintenance.workshop,
                        "Notizen" to
                            maintenance.notes
                    ),
                onDelete = {

                    store.saveMaintenances(
                        store.maintenances.filter {
                            it.id !=
                                maintenance.id
                        }
                    )
                }
            )
        }
    }
}

/* ---------------------------------------------------------
   WARTUNGS FORMULAR
   --------------------------------------------------------- */

@Composable
private fun MaintenanceForm(
    vehicleId: String,
    onCancel: () -> Unit,
    onSave: (Maintenance) -> Unit
) {

    var date by remember {
        mutableStateOf("")
    }

    var mileage by remember {
        mutableStateOf("")
    }

    var description by remember {
        mutableStateOf("")
    }

    var cost by remember {
        mutableStateOf("")
    }

    var workshop by remember {
        mutableStateOf("")
    }

    var notes by remember {
        mutableStateOf("")
    }

    EntryForm(
        title = "Neue Wartung",
        fields =
            listOf(
                "Datum" to date,
                "Kilometerstand" to mileage,
                "Beschreibung" to
                    description,
                "Kosten" to cost,
                "Werkstatt" to workshop,
                "Notizen" to notes
            ),
        setters =
            listOf(
                { date = it },
                { mileage = it },
                { description = it },
                { cost = it },
                { workshop = it },
                { notes = it }
            ),
        onCancel = onCancel,
        onSave = {

            onSave(
                Maintenance(
                    vehicleId =
                        vehicleId,
                    date = date,
                    mileage =
                        mileage,
                    description =
                        description,
                    cost = cost,
                    workshop =
                        workshop,
                    notes = notes
                )
            )
        }
    )
}

/* ---------------------------------------------------------
   REIFEN
   --------------------------------------------------------- */

@Composable
private fun TiresScreen(
    store: Store,
    activeVehicle: Vehicle?
) {

    if (activeVehicle == null) {

        EmptyVehicleMessage()
        return
    }

    var adding by remember {
        mutableStateOf(false)
    }

    if (adding) {

        TireForm(
            vehicleId =
                activeVehicle.id,
            onCancel = {
                adding = false
            },
            onSave = {

                store.saveTires(
                    store.tires + it
                )

                adding = false
            }
        )

        return
    }

    LazyColumn(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(18.dp),
        verticalArrangement =
            Arrangement.spacedBy(10.dp)
    ) {

        item {

            VehicleSelector(
                store,
                activeVehicle
            )

            Button(
                onClick = {
                    adding = true
                },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
            ) {

                Icon(
                    Icons.Filled.Add,
                    contentDescription = null
                )

                Spacer(
                    Modifier.width(8.dp)
                )

                Text(
                    "Reifensatz hinzufügen"
                )
            }
        }

        items(
            store.tires.filter {
                it.vehicleId ==
                    activeVehicle.id
            },
            key = {
                it.id
            }
        ) { tire ->

            DataCard(
                title =
                    tire.season.ifBlank {
                        "Reifensatz"
                    },
                lines =
                    listOf(
                        "Sommer / Winter" to
                            tire.season,
                        "Dimension" to
                            tire.dimension,
                        "Marke" to
                            tire.brand,
                        "DOT" to
                            tire.dot,
                        "Profiltiefe" to
                            tire.tread,
                        "Zustand" to
                            tire.condition,
                        "Lagerung" to
                            tire.storage
                    ),
                onDelete = {

                    store.saveTires(
                        store.tires.filter {
                            it.id != tire.id
                        }
                    )
                }
            )
        }
    }
}

/* ---------------------------------------------------------
   REIFEN FORMULAR
   --------------------------------------------------------- */

@Composable
private fun TireForm(
    vehicleId: String,
    onCancel: () -> Unit,
    onSave: (TireSet) -> Unit
) {

    var season by remember {
        mutableStateOf("")
    }

    var dimension by remember {
        mutableStateOf("")
    }

    var brand by remember {
        mutableStateOf("")
    }

    var dot by remember {
        mutableStateOf("")
    }

    var tread by remember {
        mutableStateOf("")
    }

    var condition by remember {
        mutableStateOf("")
    }

    var storage by remember {
        mutableStateOf("")
    }

    EntryForm(
        title = "Reifensatz",
        fields =
            listOf(
                "Sommer / Winter" to season,
                "Dimension" to dimension,
                "Marke" to brand,
                "DOT" to dot,
                "Profiltiefe" to tread,
                "Zustand" to condition,
                "Lagerung" to storage
            ),
        setters =
            listOf(
                { season = it },
                { dimension = it },
                { brand = it },
                { dot = it },
                { tread = it },
                { condition = it },
                { storage = it }
            ),
        onCancel = onCancel,
        onSave = {

            onSave(
                TireSet(
                    vehicleId =
                        vehicleId,
                    season = season,
                    dimension =
                        dimension,
                    brand = brand,
                    dot = dot,
                    tread = tread,
                    condition =
                        condition,
                    storage = storage
                )
            )
        }
    )
}

/* ---------------------------------------------------------
   GESAMTBLICK
   --------------------------------------------------------- */

@Composable
private fun OverviewScreen(
    store: Store,
    activeVehicle: Vehicle?
) {

    if (activeVehicle == null) {

        EmptyVehicleMessage()
        return
    }

    val repairs =
        store.repairs.count {
            it.vehicleId ==
                activeVehicle.id
        }

    val maintenance =
        store.maintenances.count {
            it.vehicleId ==
                activeVehicle.id
        }

    val tires =
        store.tires.count {
            it.vehicleId ==
                activeVehicle.id
        }

    val pickerl =
        store.pickerls.firstOrNull {
            it.vehicleId ==
                activeVehicle.id
        }

    LazyColumn(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(18.dp),
        verticalArrangement =
            Arrangement.spacedBy(10.dp)
    ) {

        item {

            VehicleSelector(
                store,
                activeVehicle
            )

            Spacer(
                Modifier.height(14.dp)
            )

            Text(
                "Gesamtblick",
                fontSize = 28.sp,
                fontWeight =
                    FontWeight.Bold,
                color = Color.White
            )

            Spacer(
                Modifier.height(8.dp)
            )

            OverviewCard(
                "Fahrzeug",
                "${activeVehicle.brand} ${activeVehicle.model}"
                    .trim()
                    .ifBlank {
                        activeVehicle.name
                    }
            )

            OverviewCard(
                "Kennzeichen",
                activeVehicle.plate.ifBlank {
                    "Nicht eingetragen"
                }
            )

            OverviewCard(
                "FIN / VIN",
                activeVehicle.vin.ifBlank {
                    "Nicht eingetragen"
                }
            )

            OverviewCard(
                "Reparaturen",
                repairs.toString()
            )

            OverviewCard(
                "Wartungen",
                maintenance.toString()
            )

            OverviewCard(
                "Reifensätze",
                tires.toString()
            )

            OverviewCard(
                "Nächster Pickerl-Termin",
                pickerl?.nextDate?.ifBlank {
                    "Nicht eingetragen"
                } ?: "Nicht eingetragen"
            )
        }
    }
}

/* ---------------------------------------------------------
   ALLGEMEINE FORM
   --------------------------------------------------------- */

@Composable
private fun EntryForm(
    title: String,
    fields: List<Pair<String, String>>,
    setters: List<(String) -> Unit>,
    onCancel: () -> Unit,
    onSave: () -> Unit
) {

    LazyColumn(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(18.dp),
        verticalArrangement =
            Arrangement.spacedBy(8.dp)
    ) {

        item {

            Text(
                title,
                fontSize = 26.sp,
                fontWeight =
                    FontWeight.Bold,
                color = Color.White
            )

            Spacer(
                Modifier.height(10.dp)
            )

            fields.forEachIndexed { index, field ->

                FormField(
                    label = field.first,
                    value = field.second,
                    onValueChange =
                        setters[index]
                )
            }

            Spacer(
                Modifier.height(12.dp)
            )

            Row(
                horizontalArrangement =
                    Arrangement.spacedBy(10.dp)
            ) {

                OutlinedButton(
                    onClick = onCancel,
                    modifier =
                        Modifier.weight(1f)
                ) {

                    Text("Abbrechen")
                }

                Button(
                    onClick = onSave,
                    modifier =
                        Modifier.weight(1f)
                ) {

                    Text("Speichern")
                }
            }
        }
    }
}

/* ---------------------------------------------------------
   FORM FELD
   --------------------------------------------------------- */

@Composable
private fun FormField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(label)
        },
        modifier =
            Modifier.fillMaxWidth(),
        singleLine = false
    )
}

/* ---------------------------------------------------------
   DATA CARD
   --------------------------------------------------------- */

@Composable
private fun DataCard(
    title: String,
    lines: List<Pair<String, String>>,
    onDelete: () -> Unit
) {

    Card(
        modifier =
            Modifier.fillMaxWidth(),
        shape =
            RoundedCornerShape(20.dp),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    Color(0xFF11141A)
            )
    ) {

        Column(
            modifier =
                Modifier.padding(18.dp)
        ) {

            Row(
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Text(
                    title,
                    fontSize = 20.sp,
                    fontWeight =
                        FontWeight.Bold,
                    color = Color.White,
                    modifier =
                        Modifier.weight(1f)
                )

                IconButton(
                    onClick = onDelete
                ) {

                    Icon(
                        Icons.Filled.Delete,
                        contentDescription =
                            "Löschen",
                        tint =
                            Color(0xFFE21D32)
                    )
                }
            }

            lines
                .filter {
                    it.second.isNotBlank()
                }
                .forEach {

                    Text(
                        "${it.first}: ${it.second}",
                        color = Color(0xFFD7D9DE),
                        modifier =
                            Modifier.padding(
                                vertical = 2.dp
                            )
                    )
                }
        }
    }
}

/* ---------------------------------------------------------
   ÜBERSICHTSKARTE
   --------------------------------------------------------- */

@Composable
private fun OverviewCard(
    title: String,
    value: String
) {

    Card(
        modifier =
            Modifier.fillMaxWidth(),
        shape =
            RoundedCornerShape(20.dp),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    Color(0xFF11141A)
            )
    ) {

        Column(
            modifier =
                Modifier.padding(18.dp)
        ) {

            Text(
                title,
                color =
                    Color(0xFFB8BEC8)
            )

            Text(
                value,
                color = Color.White,
                fontSize = 21.sp,
                fontWeight =
                    FontWeight.Bold
            )
        }
    }
}

/* ---------------------------------------------------------
   KEIN FAHRZEUG
   --------------------------------------------------------- */

@Composable
private fun EmptyVehicleMessage() {

    Box(
        modifier =
            Modifier.fillMaxSize(),
        contentAlignment =
            Alignment.Center
    ) {

        Column(
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            Icon(
                Icons.Filled.DirectionsCar,
                contentDescription = null,
                modifier =
                    Modifier.size(60.dp),
                tint = Color.White
            )

            Spacer(
                Modifier.height(12.dp)
            )

            Text(
                "Bitte zuerst ein Fahrzeug anlegen.",
                color = Color.White,
                fontSize = 18.sp
            )
        }
    }
}

/* ---------------------------------------------------------
   FAHRZEUGBILD
   --------------------------------------------------------- */

@Composable
private fun VehicleImage(
    uri: String
) {

    if (uri.isBlank()) {

        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(
                        RoundedCornerShape(18.dp)
                    )
                    .background(
                        Color(0xFF20232B)
                    ),
            contentAlignment =
                Alignment.Center
        ) {

            Icon(
                Icons.Filled.DirectionsCar,
                contentDescription = null,
                modifier =
                    Modifier.size(70.dp),
                tint =
                    Color(0xFF707784)
            )
        }

        return
    }

    val bitmap by produceState(
        initialValue = null,
        key1 = uri
    ) {

        value =
            withContext(Dispatchers.IO) {

                try {

                    val context = LocalContext.current

val bitmap by produceState(
    initialValue = null,
    key1 = uri
) {
    value = withContext(Dispatchers.IO) {
        try {
            val input = context.contentResolver.openInputStream(
                Uri.parse(uri)
            )

            input?.use {
                BitmapFactory.decodeStream(it)?.asImageBitmap()
            }
        } catch (_: Exception) {
            null
        }
    }
}

    if (bitmap != null) {

        Image(
            bitmap = bitmap!!,
            contentDescription =
                "Fahrzeugbild",
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(
                        RoundedCornerShape(18.dp)
                    ),
            contentScale =
                ContentScale.Crop
        )

    } else {

        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(
                        RoundedCornerShape(18.dp)
                    )
                    .background(
                        Color(0xFF20232B)
                    ),
            contentAlignment =
                Alignment.Center
        ) {

            Text(
                "Bild konnte nicht geladen werden",
                color =
                    Color(0xFFB8BEC8)
            )
        }
    }
}

/* ---------------------------------------------------------
   BENACHRICHTIGUNG
   --------------------------------------------------------- */

private fun createNotificationChannel(
    context: Context
) {

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
            context.getSystemService(
                NotificationManager::class.java
            )

        manager.createNotificationChannel(
            channel
        )
    }
}

/* ---------------------------------------------------------
   PICKERL ERINNERUNG
   --------------------------------------------------------- */

private fun schedulePickerReminder(
    context: Context,
    dateText: String,
    vehicleId: String
) {

    try {

        val date =
            LocalDate.parse(
                dateText,
                dateFormatter
            )

        val reminderDate =
            date.minusMonths(3)

        val trigger =
            reminderDate
                .atStartOfDay(
                    ZoneId.systemDefault()
                )
                .toInstant()
                .toEpochMilli()

        if (
            trigger <=
            System.currentTimeMillis()
        ) {
            return
        }

        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.TIRAMISU
        ) {

            if (
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) !=
                PackageManager.PERMISSION_GRANTED
            ) {

                if (
                    context is ComponentActivity
                ) {

                    ActivityCompat
                        .requestPermissions(
                            context,
                            arrayOf(
                                Manifest.permission
                                    .POST_NOTIFICATIONS
                            ),
                            500
                        )
                }
            }
        }

        val intent =
            Intent(
                context,
                PickerlReceiver::class.java
            ).apply {

                putExtra(
                    "vehicleId",
                    vehicleId
                )
            }

        val pendingIntent =
            PendingIntent.getBroadcast(
                context,
                vehicleId.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE
            )

        val alarmManager =
            context.getSystemService(
                Context.ALARM_SERVICE
            ) as AlarmManager

        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            trigger,
            pendingIntent
        )

    } catch (
        _: DateTimeParseException
    ) {
        // Ungültiges Datum.
    }
}

/* ---------------------------------------------------------
   PICKERL RECEIVER
   --------------------------------------------------------- */

class PickerlReceiver :
    BroadcastReceiver() {

    override fun onReceive(
        context: Context,
        intent: Intent
    ) {

        createNotificationChannel(
            context
        )

        val notification =
            NotificationCompat
                .Builder(
                    context,
                    "pickerl"
                )
                .setSmallIcon(
                    android.R.drawable
                        .ic_dialog_info
                )
                .setContentTitle(
                    "AutoCheck – Pickerl Erinnerung"
                )
                .setContentText(
                    "Dein Pickerl / TÜV ist in 3 Monaten fällig."
                )
                .setPriority(
                    NotificationCompat
                        .PRIORITY_DEFAULT
                )
                .setAutoCancel(true)
                .build()

        val manager =
            context.getSystemService(
                NotificationManager::class.java
            )

        manager.notify(
            intent
                .getStringExtra(
                    "vehicleId"
                )
                ?.hashCode()
                ?: 1001,
            notification
        )
    }
}
