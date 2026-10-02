package de.autocheck.app

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
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CarRepair
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TireRepair
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import de.autocheck.app.ui.AutoCheckTheme
import java.time.LocalDate
import java.time.ZoneId

data class Vehicle(
    val name: String,
    val make: String,
    val model: String,
    val year: String,
    val plate: String = "",
    val vin: String = "",
    val imageUri: String? = null
)

data class Repair(
    val id: Long = System.currentTimeMillis(),
    val vehicleName: String,
    val date: String,
    val mileage: String,
    val description: String,
    val cost: String,
    val workshop: String
)

data class Maintenance(
    val id: Long = System.currentTimeMillis(),
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
    val id: Long = System.currentTimeMillis(),
    val vehicleName: String,
    val season: String,
    val dimension: String,
    val brand: String,
    val dot: String,
    val treadDepth: String,
    val condition: String,
    val storage: String
)

enum class Screen {
    HOME,
    VEHICLES,
    REPAIRS,
    PICKERL,
    MAINTENANCE,
    OVERVIEW,
    TIRES
}

data class MenuItemData(
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val screen: Screen
)

class VehicleStore(context: Context) {

    private val prefs =
        context.getSharedPreferences(
            "autocheck_store",
            Context.MODE_PRIVATE
        )

    val vehicles = mutableStateListOf<Vehicle>()
    val repairs = mutableStateListOf<Repair>()
    val maintenances = mutableStateListOf<Maintenance>()
    val pickerls = mutableStateListOf<Pickerl>()
    val tires = mutableStateListOf<TireSet>()

    var activeVehicleName by mutableStateOf("")

    init {
        load()
    }

    private fun load() {
        loadVehicles()
        loadRepairs()
        loadMaintenances()
        loadPickerls()
        loadTires()

        activeVehicleName =
            prefs.getString(
                "activeVehicle",
                ""
            ) ?: ""

        if (
            activeVehicleName.isBlank() &&
            vehicles.isNotEmpty()
        ) {
            activeVehicleName =
                vehicles.first().name
        }
    }

    private fun loadVehicles() {
        val data =
            prefs.getString(
                "vehicles",
                ""
            ) ?: ""

        if (data.isBlank()) return

        data.split("|||").forEach { item ->
            val p = item.split("|")

            if (p.size >= 7) {
                vehicles.add(
                    Vehicle(
                        name = p[0],
                        make = p[1],
                        model = p[2],
                        year = p[3],
                        plate = p[4],
                        vin = p[5],
                        imageUri =
                            p[6].ifBlank {
                                null
                            }
                    )
                )
            }
        }
    }

    private fun loadRepairs() {
        val data =
            prefs.getString(
                "repairs",
                ""
            ) ?: ""

        if (data.isBlank()) return

        data.split("|||").forEach { item ->
            val p = item.split("|")

            if (p.size >= 7) {
                repairs.add(
                    Repair(
                        id =
                            p[0].toLongOrNull()
                                ?: System.currentTimeMillis(),
                        vehicleName = p[1],
                        date = p[2],
                        mileage = p[3],
                        description = p[4],
                        cost = p[5],
                        workshop = p[6]
                    )
                )
            }
        }
    }

    private fun loadMaintenances() {
        val data =
            prefs.getString(
                "maintenances",
                ""
            ) ?: ""

        if (data.isBlank()) return

        data.split("|||").forEach { item ->
            val p = item.split("|")

            if (p.size >= 7) {
                maintenances.add(
                    Maintenance(
                        id =
                            p[0].toLongOrNull()
                                ?: System.currentTimeMillis(),
                        vehicleName = p[1],
                        date = p[2],
                        mileage = p[3],
                        cost = p[4],
                        workshop = p[5],
                        notes = p[6]
                    )
                )
            }
        }
    }

    private fun loadPickerls() {
        val data =
            prefs.getString(
                "pickerls",
                ""
            ) ?: ""

        if (data.isBlank()) return

        data.split("|||").forEach { item ->
            val p = item.split("|")

            if (p.size >= 5) {
                pickerls.add(
                    Pickerl(
                        vehicleName = p[0],
                        lastDate = p[1],
                        nextDate = p[2],
                        notes = p[3],
                        reminder =
                            p[4].toBoolean()
                    )
                )
            }
        }
    }

    private fun loadTires() {
        val data =
            prefs.getString(
                "tires",
                ""
            ) ?: ""

        if (data.isBlank()) return

        data.split("|||").forEach { item ->
            val p = item.split("|")

            if (p.size >= 9) {
                tires.add(
                    TireSet(
                        id =
                            p[0].toLongOrNull()
                                ?: System.currentTimeMillis(),
                        vehicleName = p[1],
                        season = p[2],
                        dimension = p[3],
                        brand = p[4],
                        dot = p[5],
                        treadDepth = p[6],
                        condition = p[7],
                        storage = p[8]
                    )
                )
            }
        }
    }

    private fun saveAll() {
        prefs.edit()
            .putString(
                "vehicles",
                vehicles.joinToString("|||") {
                    listOf(
                        it.name,
                        it.make,
                        it.model,
                        it.year,
                        it.plate,
                        it.vin,
                        it.imageUri ?: ""
                    ).joinToString("|")
                }
            )
            .putString(
                "repairs",
                repairs.joinToString("|||") {
                    listOf(
                        it.id,
                        it.vehicleName,
                        it.date,
                        it.mileage,
                        it.description,
                        it.cost,
                        it.workshop
                    ).joinToString("|")
                }
            )
            .putString(
                "maintenances",
                maintenances.joinToString("|||") {
                    listOf(
                        it.id,
                        it.vehicleName,
                        it.date,
                        it.mileage,
                        it.cost,
                        it.workshop,
                        it.notes
                    ).joinToString("|")
                }
            )
            .putString(
                "pickerls",
                pickerls.joinToString("|||") {
                    listOf(
                        it.vehicleName,
                        it.lastDate,
                        it.nextDate,
                        it.notes,
                        it.reminder
                    ).joinToString("|")
                }
            )
            .putString(
                "tires",
                tires.joinToString("|||") {
                    listOf(
                        it.id,
                        it.vehicleName,
                        it.season,
                        it.dimension,
                        it.brand,
                        it.dot,
                        it.treadDepth,
                        it.condition,
                        it.storage
                    ).joinToString("|")
                }
            )
            .putString(
                "activeVehicle",
                activeVehicleName
            )
            .apply()
    }

    fun addVehicle(
        vehicle: Vehicle
    ): Boolean {
        if (vehicles.size >= 5) return false

        if (
            vehicles.any {
                it.name.equals(
                    vehicle.name,
                    ignoreCase = true
                )
            }
        ) {
            return false
        }

        vehicles.add(vehicle)

        if (activeVehicleName.isBlank()) {
            activeVehicleName =
                vehicle.name
        }

        saveAll()
        return true
    }

    fun updateVehicle(
        index: Int,
        vehicle: Vehicle
    ) {
        if (index !in vehicles.indices) return

        val oldName =
            vehicles[index].name

        vehicles[index] = vehicle

        if (
            activeVehicleName == oldName
        ) {
            activeVehicleName =
                vehicle.name
        }

        for (i in repairs.indices) {
            if (
                repairs[i].vehicleName ==
                oldName
            ) {
                repairs[i] =
                    repairs[i].copy(
                        vehicleName =
                            vehicle.name
                    )
            }
        }

        for (i in maintenances.indices) {
            if (
                maintenances[i].vehicleName ==
                oldName
            ) {
                maintenances[i] =
                    maintenances[i].copy(
                        vehicleName =
                            vehicle.name
                    )
            }
        }

        for (i in pickerls.indices) {
            if (
                pickerls[i].vehicleName ==
                oldName
            ) {
                pickerls[i] =
                    pickerls[i].copy(
                        vehicleName =
                            vehicle.name
                    )
            }
        }

        for (i in tires.indices) {
            if (
                tires[i].vehicleName ==
                oldName
            ) {
                tires[i] =
                    tires[i].copy(
                        vehicleName =
                            vehicle.name
                    )
            }
        }

        saveAll()
    }

    fun deleteVehicle(
        index: Int
    ) {
        if (index !in vehicles.indices) return

        val name =
            vehicles[index].name

        vehicles.removeAt(index)

        repairs.removeAll {
            it.vehicleName == name
        }

        maintenances.removeAll {
            it.vehicleName == name
        }

        pickerls.removeAll {
            it.vehicleName == name
        }

        tires.removeAll {
            it.vehicleName == name
        }

        if (
            activeVehicleName == name
        ) {
            activeVehicleName =
                vehicles.firstOrNull()
                    ?.name
                    ?: ""
        }

        saveAll()
    }

    fun save() {
        saveAll()
    }
}

class MainActivity :
    ComponentActivity() {

    private lateinit var store: VehicleStore

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(
            savedInstanceState
        )

        store =
            VehicleStore(this)

        createNotificationChannel()

        setContent {
            AutoCheckTheme {
                AutoCheckApp(store)
            }
        }

        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.TIRAMISU
        ) {
            if (
                checkSelfPermission(
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(
                        android.Manifest.permission
                            .POST_NOTIFICATIONS
                    ),
                    1001
                )
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

class PickerlReceiver :
    BroadcastReceiver() {

    override fun onReceive(
        context: Context,
        intent: Intent?
    ) {
        val vehicle =
            intent?.getStringExtra(
                "vehicle"
            ) ?: "Dein Fahrzeug"

        val notification =
            NotificationCompat.Builder(
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
                .setPriority(
                    NotificationCompat
                        .PRIORITY_DEFAULT
                )
                .setAutoCancel(true)
                .build()

        val manager =
            context.getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager

        manager.notify(
            vehicle.hashCode(),
            notification
        )
    }
}

@Composable
fun AutoCheckApp(
    store: VehicleStore
) {
    var screen by remember {
        mutableStateOf(
            Screen.HOME
        )
    }

    when (screen) {

        Screen.HOME -> {
            HomeScreen(
                onMenuClick = {
                    screen = it
                }
            )
        }

        Screen.VEHICLES -> {
            VehicleScreen(
                vehicles =
                    store.vehicles,
                activeVehicle =
                    store.activeVehicleName,
                onBack = {
                    screen =
                        Screen.HOME
                },
                onActiveVehicle = {
                    store.activeVehicleName =
                        it
                    store.save()
                },
                onAdd = {
                    store.addVehicle(it)
                },
                onUpdate = {
                    index,
                    vehicle ->
                    store.updateVehicle(
                        index,
                        vehicle
                    )
                },
                onDelete = {
                    index ->
                    store.deleteVehicle(
                        index
                    )
                }
            )
        }

        Screen.REPAIRS -> {
            RepairsScreen(
                store = store,
                onBack = {
                    screen =
                        Screen.HOME
                }
            )
        }

        Screen.PICKERL -> {
            PickerlScreen(
                store = store,
                onBack = {
                    screen =
                        Screen.HOME
                }
            )
        }

        Screen.MAINTENANCE -> {
            MaintenanceScreen(
                store = store,
                onBack = {
                    screen =
                        Screen.HOME
                }
            )
        }

        Screen.OVERVIEW -> {
            OverviewScreen(
                store = store,
                onBack = {
                    screen =
                        Screen.HOME
                }
            )
        }

        Screen.TIRES -> {
            TiresScreen(
                store = store,
                onBack = {
                    screen =
                        Screen.HOME
                }
            )
        }
    }
}

@Composable
fun HomeScreen(
    onMenuClick: (Screen) -> Unit
) {
    val menuItems =
        listOf(
            MenuItemData(
                "Mein Auto",
                Icons.Default.Settings,
                Screen.VEHICLES
            ),
            MenuItemData(
                "Reparaturen",
                Icons.Default.CarRepair,
                Screen.REPAIRS
            ),
            MenuItemData(
                "Pickerl/TÜV",
                Icons.Default.CalendarMonth,
                Screen.PICKERL
            ),
            MenuItemData(
                "Wartungen",
                Icons.Default.CheckCircle,
                Screen.MAINTENANCE
            ),
            MenuItemData(
                "Gesamtblick",
                Icons.Default.Home,
                Screen.OVERVIEW
            ),
            MenuItemData(
                "Reifen",
                Icons.Default.TireRepair,
                Screen.TIRES
            )
        )

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(
                    Color.Black
                )
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(
                        rememberScrollState()
                    )
                    .padding(16.dp),
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            Spacer(
                modifier =
                    Modifier.height(40.dp)
            )

            Text(
                text = "AutoCheck",
                style =
                    MaterialTheme.typography
                        .headlineLarge,
                fontWeight =
                    FontWeight.Bold,
                color = Color.White
            )

            Spacer(
                modifier =
                    Modifier.height(24.dp)
            )

            /*
             * CUT1-GRUNDGERÜST
             * bleibt bestehen.
             */

            Card(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                shape =
                    RoundedCornerShape(20.dp)
            ) {
                Box(
                    modifier =
                        Modifier.fillMaxSize(),
                    contentAlignment =
                        Alignment.Center
                ) {
                    Text(
                        text =
                            "Mein Fahrzeug",
                        style =
                            MaterialTheme.typography
                                .headlineMedium,
                        color =
                            Color.White
                    )
                }
            }

            Spacer(
                modifier =
                    Modifier.height(24.dp)
            )

            menuItems.forEachIndexed {
                    index,
                    item ->

                var visible by remember {
                    mutableStateOf(
                        false
                    )
                }

                LaunchedEffect(
                    Unit
                ) {
                    kotlinx.coroutines.delay(
                        2500L +
                            index * 150L
                    )
                    visible = true
                }

                AnimatedVisibility(
                    visible = visible,
                    enter =
                        slideInHorizontally(
                            animationSpec =
                                tween(600),
                            initialOffsetX = {
                                it
                            }
                        ) +
                            fadeIn(
                                animationSpec =
                                    tween(600)
                            )
                ) {
                    MenuCard(
                        title =
                            item.title,
                        icon =
                            item.icon,
                        onClick = {
                            onMenuClick(
                                item.screen
                            )
                        }
                    )
                }

                Spacer(
                    modifier =
                        Modifier.height(12.dp)
                )
            }
        }
    }
}

@Composable
fun MenuCard(
    title: String,
    icon:
        androidx.compose.ui.graphics.vector
            .ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable {
                    onClick()
                },
        shape =
            RoundedCornerShape(18.dp),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    Color.DarkGray
            )
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Icon(
                imageVector =
                    icon,
                contentDescription =
                    null,
                tint =
                    Color.White,
                modifier =
                    Modifier.size(32.dp)
            )

            Spacer(
                modifier =
                    Modifier.width(16.dp)
            )

            Text(
                text = title,
                color =
                    Color.White,
                style =
                    MaterialTheme.typography
                        .titleLarge
            )

            Spacer(
                modifier =
                    Modifier.weight(1f)
            )

            Icon(
                imageVector =
                    Icons.Default
                        .KeyboardArrowRight,
                contentDescription =
                    null,
                tint =
                    Color.White
            )
        }
    }
}

@OptIn(
    ExperimentalMaterial3Api::class
)
@Composable
fun DetailScaffold(
    title: String,
    onBack: () -> Unit,
    content:
        @Composable
        ColumnScope.() -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = title,
                        color =
                            Color.White
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick =
                            onBack
                    ) {
                        Icon(
                            imageVector =
                                Icons.Default
                                    .ArrowBack,
                            contentDescription =
                                "Zurück",
                            tint =
                                Color.White
                        )
                    }
                }
            )
        },
        containerColor =
            Color.Black
    ) { paddingValues ->

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(
                        paddingValues
                    )
                    .padding(16.dp)
                    .verticalScroll(
                        rememberScrollState()
                    ),
            content = content
        )
    }
}

@Composable
fun VehicleSelector(
    vehicles: List<Vehicle>,
    selectedVehicle: String,
    onVehicleSelected:
        (String) -> Unit
) {
    var expanded by remember {
        mutableStateOf(
            false
        )
    }

    Column(
        modifier =
            Modifier.fillMaxWidth()
    ) {

        Text(
            text = "Fahrzeug",
            color = Color.White,
            fontWeight =
                FontWeight.Bold
        )

        Spacer(
            modifier =
                Modifier.height(6.dp)
        )

        Box(
            modifier =
                Modifier.fillMaxWidth()
        ) {

            OutlinedButton(
                onClick = {
                    expanded = true
                },
                modifier =
                    Modifier.fillMaxWidth()
            ) {

                Text(
                    text =
                        if (
                            selectedVehicle
                                .isBlank()
                        ) {
                            "Fahrzeug auswählen"
                        } else {
                            selectedVehicle
                        }
                )

                Spacer(
                    modifier =
                        Modifier.weight(1f)
                )

                Icon(
                    imageVector =
                        Icons.Default
                            .KeyboardArrowDown,
                    contentDescription =
                        null
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
                            onVehicleSelected(
                                vehicle.name
                            )
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun VehicleScreen(
    vehicles: MutableList<Vehicle>,
    activeVehicle: String,
    onBack: () -> Unit,
    onActiveVehicle:
        (String) -> Unit,
    onAdd:
        (Vehicle) -> Boolean,
    onUpdate:
        (Int, Vehicle) -> Unit,
    onDelete:
        (Int) -> Unit
) {

    var showForm by remember {
        mutableStateOf(
            false
        )
    }

    var editIndex by remember {
        mutableStateOf<Int?>(
            null
        )
    }

    DetailScaffold(
        title = "Mein Auto",
        onBack = onBack
    ) {

        Text(
            text =
                "Deine Fahrzeuge",
            color =
                Color.White,
            style =
                MaterialTheme.typography
                    .headlineSmall,
            fontWeight =
                FontWeight.Bold
        )

        Spacer(
            modifier =
                Modifier.height(16.dp)
        )

        if (
            vehicles.isEmpty()
        ) {

            Card(
                modifier =
                    Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier =
                        Modifier.padding(
                            20.dp
                        )
                ) {
                    Text(
                        text =
                            "Noch kein Fahrzeug angelegt."
                    )

                    Spacer(
                        modifier =
                            Modifier.height(8.dp)
                    )

                    Text(
                        text =
                            "Du kannst bis zu 5 Fahrzeuge speichern."
                    )
                }
            }

        } else {

            vehicles.forEachIndexed {
                    index,
                    vehicle ->

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
                        editIndex =
                            index
                        showForm = true
                    },
                    onDelete = {
                        onDelete(index)
                    }
                )

                Spacer(
                    modifier =
                        Modifier.height(12.dp)
                )
            }
        }

        Spacer(
            modifier =
                Modifier.height(16.dp)
        )

        if (
            vehicles.size < 5
        ) {

            Button(
                onClick = {
                    editIndex =
                        null
                    showForm = true
                },
                modifier =
                    Modifier.fillMaxWidth()
            ) {

                Icon(
                    imageVector =
                        Icons.Default.Add,
                    contentDescription =
                        null
                )

                Spacer(
                    modifier =
                        Modifier.width(8.dp)
                )

                Text(
                    "Fahrzeug hinzufügen"
                )
            }
        }

        if (showForm) {

            Spacer(
                modifier =
                    Modifier.height(20.dp)
            )

            VehicleForm(
                vehicle =
                    editIndex?.let {
                        vehicles[it]
                    },
                onCancel = {
                    showForm = false
                    editIndex = null
                },
                onSave = {
                    vehicle ->

                    val index =
                        editIndex

                    if (
                        index == null
                    ) {
                        if (
                            onAdd(vehicle)
                        ) {
                            showForm =
                                false
                        }
                    } else {
                        onUpdate(
                            index,
                            vehicle
                        )

                        showForm =
                            false
                        editIndex =
                            null
                    }
                }
            )
        }
    }
}

@Composable
fun VehicleCard(
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
                .clickable {
                    onSelect()
                },
        colors =
            CardDefaults.cardColors(
                containerColor =
                    if (active) {
                        Color(
                            0xFF263238
                        )
                    } else {
                        Color.DarkGray
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
                modifier =
                    Modifier.fillMaxWidth(),
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                VehicleImage(
                    uri =
                        vehicle.imageUri,
                    modifier =
                        Modifier.size(
                            80.dp
                        )
                )

                Spacer(
                    modifier =
                        Modifier.width(14.dp)
                )

                Column(
                    modifier =
                        Modifier.weight(1f)
                ) {

                    Text(
                        text =
                            vehicle.name,
                        color =
                            Color.White,
                        fontWeight =
                            FontWeight.Bold,
                        style =
                            MaterialTheme.typography
                                .titleLarge
                    )

                    Text(
                        text =
                            "${vehicle.make} ${vehicle.model}",
                        color =
                            Color.LightGray
                    )

                    if (
                        vehicle.year
                            .isNotBlank()
                    ) {
                        Text(
                            text =
                                "Baujahr: ${vehicle.year}",
                            color =
                                Color.LightGray
                        )
                    }

                    if (
                        vehicle.plate
                            .isNotBlank()
                    ) {
                        Text(
                            text =
                                "Kennzeichen: ${vehicle.plate}",
                            color =
                                Color.LightGray
                        )
                    }
                }

                if (active) {
                    Icon(
                        imageVector =
                            Icons.Default
                                .CheckCircle,
                        contentDescription =
                            "Aktiv",
                        tint =
                            Color.Green
                    )
                }
            }

            Spacer(
                modifier =
                    Modifier.height(12.dp)
            )

            Row(
                modifier =
                    Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.End
            ) {

                OutlinedButton(
                    onClick =
                        onEdit
                ) {
                    Icon(
                        imageVector =
                            Icons.Default
                                .Edit,
                        contentDescription =
                            null
                    )

                    Spacer(
                        modifier =
                            Modifier.width(4.dp)
                    )

                    Text(
                        "Bearbeiten"
                    )
                }

                Spacer(
                    modifier =
                        Modifier.width(8.dp)
                )

                IconButton(
                    onClick =
                        onDelete
                ) {
                    Icon(
                        imageVector =
                            Icons.Default
                                .Delete,
                        contentDescription =
                            "Löschen",
                        tint =
                            Color.Red
                    )
                }
            }
        }
    }
}

@Composable
fun VehicleForm(
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
            vehicle?.imageUri
        )
    }

    var error by remember {
        mutableStateOf("")
    }

    val imagePicker =
        rememberLauncherForActivityResult(
            ActivityResultContracts
                .GetContent()
        ) { uri ->
            if (uri != null) {
                imageUri =
                    uri.toString()
            }
        }

    Card(
        modifier =
            Modifier.fillMaxWidth()
    ) {

        Column(
            modifier =
                Modifier.padding(
                    16.dp
                )
        ) {

            Text(
                text =
                    if (
                        vehicle == null
                    ) {
                        "Fahrzeug hinzufügen"
                    } else {
                        "Fahrzeug bearbeiten"
                    },
                style =
                    MaterialTheme.typography
                        .titleLarge,
                fontWeight =
                    FontWeight.Bold
            )

            Spacer(
                modifier =
                    Modifier.height(12.dp)
            )

            VehicleImage(
                uri = imageUri,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(180.dp)
            )

            Spacer(
                modifier =
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

            Spacer(
                modifier =
                    Modifier.height(12.dp)
            )

            FormField(
                label = "Name",
                value = name,
                onValueChange = {
                    name = it
                }
            )

            FormField(
                label = "Marke",
                value = make,
                onValueChange = {
                    make = it
                }
            )

            FormField(
                label = "Modell",
                value = model,
                onValueChange = {
                    model = it
                }
            )

            FormField(
                label = "Baujahr",
                value = year,
                onValueChange = {
                    year = it
                }
            )

            FormField(
                label =
                    "Kennzeichen",
                value = plate,
                onValueChange = {
                    plate = it
                }
            )

            FormField(
                label = "VIN / FIN",
                value = vin,
                onValueChange = {
                    vin = it
                }
            )

            if (
                error.isNotBlank()
            ) {

                Spacer(
                    modifier =
                        Modifier.height(8.dp)
                )

                Text(
                    text = error,
                    color =
                        Color.Red
                )
            }

            Spacer(
                modifier =
                    Modifier.height(16.dp)
            )

            Row(
                modifier =
                    Modifier.fillMaxWidth()
            ) {

                OutlinedButton(
                    onClick =
                        onCancel,
                    modifier =
                        Modifier.weight(
                            1f
                        )
                ) {
                    Text(
                        "Abbrechen"
                    )
                }

                Spacer(
                    modifier =
                        Modifier.width(8.dp)
                )

                Button(
                    onClick = {

                        if (
                            name.isBlank()
                        ) {
                            error =
                                "Bitte einen Namen eingeben."
                            return@Button
                        }

                        if (
                            make.isBlank()
                        ) {
                            error =
                                "Bitte die Marke eingeben."
                            return@Button
                        }

                        if (
                            model.isBlank()
                        ) {
                            error =
                                "Bitte das Modell eingeben."
                            return@Button
                        }

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
                    },
                    modifier =
                        Modifier.weight(
                            1f
                        )
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
fun FormField(
    label: String,
    value: String,
    onValueChange:
        (String) -> Unit,
    singleLine: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange =
            onValueChange,
        label = {
            Text(label)
        },
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    vertical = 5.dp
                ),
        singleLine =
            singleLine
    )
}

@Composable
fun VehicleImage(
    uri: String?,
    modifier: Modifier =
        Modifier
) {

    if (uri.isNullOrBlank()) {

        Card(
            modifier = modifier,
            shape =
                RoundedCornerShape(
                    16.dp
                )
        ) {
            Box(
                modifier =
                    Modifier.fillMaxSize(),
                contentAlignment =
                    Alignment.Center
            ) {
                Icon(
                    imageVector =
                        Icons.Default
                            .Settings,
                    contentDescription =
                        null,
                    modifier =
                        Modifier.size(
                            48.dp
                        )
                )
            }
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

    LaunchedEffect(uri) {

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
                                Uri.parse(uri)
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

    Card(
        modifier = modifier,
        shape =
            RoundedCornerShape(
                16.dp
            )
    ) {

        if (
            bitmap != null
        ) {

            Image(
                bitmap =
                    bitmap!!,
                contentDescription =
                    "Fahrzeugbild",
                modifier =
                    Modifier.fillMaxSize(),
                contentScale =
                    ContentScale.Crop
            )

        } else {

            Box(
                modifier =
                    Modifier.fillMaxSize(),
                contentAlignment =
                    Alignment.Center
            ) {
                Text(
                    "Bild wird geladen..."
                )
            }
        }
    }
}

@Composable
fun RepairsScreen(
    store: VehicleStore,
    onBack: () -> Unit
) {

    var selectedVehicle by remember {
        mutableStateOf(
            store.activeVehicleName
        )
    }

    var showForm by remember {
        mutableStateOf(false)
    }

    var editingRepair by remember {
        mutableStateOf<Repair?>(
            null
        )
    }

    DetailScaffold(
        title = "Reparaturen",
        onBack = onBack
    ) {

        VehicleSelector(
            vehicles =
                store.vehicles,
            selectedVehicle =
                selectedVehicle,
            onVehicleSelected = {
                selectedVehicle = it
                store.activeVehicleName =
                    it
                store.save()
            }
        )

        Spacer(
            modifier =
                Modifier.height(16.dp)
        )

        Button(
            onClick = {
                editingRepair = null
                showForm = true
            },
            modifier =
                Modifier.fillMaxWidth()
        ) {

            Icon(
                imageVector =
                    Icons.Default.Add,
                contentDescription =
                    null
            )

            Spacer(
                modifier =
                    Modifier.width(8.dp)
            )

            Text(
                "Reparatur hinzufügen"
            )
        }

        Spacer(
            modifier =
                Modifier.height(16.dp)
        )

        val list =
            store.repairs.filter {
                it.vehicleName ==
                    selectedVehicle
            }

        if (list.isEmpty()) {

            Text(
                text =
                    "Für dieses Fahrzeug sind noch keine Reparaturen gespeichert.",
                color =
                    Color.LightGray
            )

        } else {

            list.forEach { repair ->

                RepairCard(
                    repair =
                        repair,
                    onDelete = {
                        store.repairs
                            .removeAll {
                                it.id ==
                                    repair.id
                            }
                        store.save()
                    },
                    onEdit = {
                        editingRepair =
                            repair
                        showForm = true
                    }
                )

                Spacer(
                    modifier =
                        Modifier.height(
                            10.dp
                        )
                )
            }
        }

        if (showForm) {

            Spacer(
                modifier =
                    Modifier.height(
                        20.dp
                    )
            )

            RepairForm(
                vehicleName =
                    selectedVehicle,
                repair =
                    editingRepair,
                onCancel = {
                    showForm =
                        false
                    editingRepair =
                        null
                },
                onSave = { repair ->

                    val old =
                        editingRepair

                    if (old == null) {

                        store.repairs.add(
                            repair
                        )

                    } else {

                        val index =
                            store.repairs
                                .indexOfFirst {
                                    it.id ==
                                        old.id
                                }

                        if (index >= 0) {
                            store.repairs[index] =
                                repair.copy(
                                    id =
                                        old.id
                                )
                        }
                    }

                    store.save()

                    showForm =
                        false
                    editingRepair =
                        null
                }
            )
        }
    }
}

@Composable
fun RepairCard(
    repair: Repair,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {

    Card(
        modifier =
            Modifier.fillMaxWidth()
    ) {

        Column(
            modifier =
                Modifier.padding(
                    16.dp
                )
        ) {

            Text(
                text =
                    repair.description,
                fontWeight =
                    FontWeight.Bold,
                style =
                    MaterialTheme.typography
                        .titleMedium
            )

            Spacer(
                modifier =
                    Modifier.height(
                        6.dp
                    )
            )

            Text(
                text =
                    "Datum: ${repair.date}"
            )

            Text(
                text =
                    "Kilometerstand: ${repair.mileage}"
            )

            Text(
                text =
                    "Kosten: ${repair.cost} €"
            )

            if (
                repair.workshop
                    .isNotBlank()
            ) {
                Text(
                    text =
                        "Werkstatt: ${repair.workshop}"
                )
            }

            Spacer(
                modifier =
                    Modifier.height(
                        10.dp
                    )
            )

            Row(
                modifier =
                    Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.End
            ) {

                OutlinedButton(
                    onClick =
                        onEdit
                ) {
                    Text(
                        "Bearbeiten"
                    )
                }

                Spacer(
                    modifier =
                        Modifier.width(
                            8.dp
                        )
                )

                IconButton(
                    onClick =
                        onDelete
                ) {
                    Icon(
                        imageVector =
                            Icons.Default
                                .Delete,
                        contentDescription =
                            "Löschen",
                        tint =
                            Color.Red
                    )
                }
            }
        }
    }
}

@Composable
fun RepairForm(
    vehicleName: String,
    repair: Repair?,
    onCancel: () -> Unit,
    onSave:
        (Repair) -> Unit
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
        modifier =
            Modifier.fillMaxWidth()
    ) {

        Column(
            modifier =
                Modifier.padding(
                    16.dp
                )
        ) {

            Text(
                text =
                    if (
                        repair == null
                    ) {
                        "Neue Reparatur"
                    } else {
                        "Reparatur bearbeiten"
                    },
                style =
                    MaterialTheme.typography
                        .titleLarge,
                fontWeight =
                    FontWeight.Bold
            )

            Spacer(
                modifier =
                    Modifier.height(
                        10.dp
                    )
            )

            FormField(
                label = "Datum",
                value = date,
                onValueChange = {
                    date = it
                }
            )

            FormField(
                label =
                    "Kilometerstand",
                value =
                    mileage,
                onValueChange = {
                    mileage = it
                }
            )

            FormField(
                label =
                    "Beschreibung",
                value =
                    description,
                onValueChange = {
                    description = it
                },
                singleLine =
                    false
            )

            FormField(
                label =
                    "Kosten in €",
                value =
                    cost,
                onValueChange = {
                    cost = it
                }
            )

            FormField(
                label =
                    "Werkstatt",
                value =
                    workshop,
                onValueChange = {
                    workshop = it
                }
            )

            Spacer(
                modifier =
                    Modifier.height(
                        12.dp
                    )
            )

            Row(
                modifier =
                    Modifier.fillMaxWidth()
            ) {

                OutlinedButton(
                    onClick =
                        onCancel,
                    modifier =
                        Modifier.weight(
                            1f
                        )
                ) {
                    Text(
                        "Abbrechen"
                    )
                }

                Spacer(
                    modifier =
                        Modifier.width(
                            8.dp
                        )
                )

                Button(
                    onClick = {

                        if (
                            description
                                .isBlank()
                        ) {
                            return@Button
                        }

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
                    },
                    modifier =
                        Modifier.weight(
                            1f
                        )
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
fun PickerlScreen(
    store: VehicleStore,
    onBack: () -> Unit
) {

    var selectedVehicle by remember {
        mutableStateOf(
            store.activeVehicleName
        )
    }

    var lastDate by remember {
        mutableStateOf("")
    }

    var nextDate by remember {
        mutableStateOf("")
    }

    var notes by remember {
        mutableStateOf("")
    }

    var reminder by remember {
        mutableStateOf(true)
    }

    val context =
        LocalContext.current

    LaunchedEffect(
        selectedVehicle
    ) {

        val existing =
            store.pickerls.firstOrNull {
                it.vehicleName ==
                    selectedVehicle
            }

        if (existing != null) {
            lastDate =
                existing.lastDate
            nextDate =
                existing.nextDate
            notes =
                existing.notes
            reminder =
                existing.reminder
        } else {
            lastDate = ""
            nextDate = ""
            notes = ""
            reminder = true
        }
    }

    DetailScaffold(
        title = "Pickerl / TÜV",
        onBack = onBack
    ) {

        VehicleSelector(
            vehicles =
                store.vehicles,
            selectedVehicle =
                selectedVehicle,
            onVehicleSelected = {
                selectedVehicle = it
                store.activeVehicleName =
                    it
                store.save()
            }
        )

        Spacer(
            modifier =
                Modifier.height(
                    16.dp
                )
        )

        if (
            selectedVehicle.isBlank()
        ) {

            Text(
                text =
                    "Bitte zuerst ein Fahrzeug auswählen.",
                color =
                    Color.LightGray
            )

        } else {

            Card(
                modifier =
                    Modifier.fillMaxWidth()
            ) {

                Column(
                    modifier =
                        Modifier.padding(
                            16.dp
                        )
                ) {

                    Text(
                        text =
                            "Pickerl-Daten",
                        style =
                            MaterialTheme.typography
                                .titleLarge,
                        fontWeight =
                            FontWeight.Bold
                    )

                    Spacer(
                        modifier =
                            Modifier.height(
                                12.dp
                            )
                    )

                    FormField(
                        label =
                            "Letztes Pickerl",
                        value =
                            lastDate,
                        onValueChange = {
                            lastDate = it
                        }
                    )

                    FormField(
                        label =
                            "Nächstes Pickerl",
                        value =
                            nextDate,
                        onValueChange = {
                            nextDate = it
                        }
                    )

                    FormField(
                        label =
                            "Notizen",
                        value =
                            notes,
                        onValueChange = {
                            notes = it
                        },
                        singleLine =
                            false
                    )

                    Spacer(
                        modifier =
                            Modifier.height(
                                8.dp
                            )
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
                                text =
                                    "Erinnerung aktiv"
                            )

                            Text(
                                text =
                                    "3 Monate vor dem Pickerl",
                                color =
                                    Color.Gray
                            )
                        }

                        Switch(
                            checked =
                                reminder,
                            onCheckedChange = {
                                reminder = it
                            }
                        )
                    }

                    Spacer(
                        modifier =
                            Modifier.height(
                                16.dp
                            )
                    )

                    Button(
                        onClick = {

                            val picker =
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

                            store.pickerls
                                .removeAll {
                                    it.vehicleName ==
                                        selectedVehicle
                                }

                            store.pickerls.add(
                                picker
                            )

                            store.save()

                            if (
                                reminder &&
                                nextDate
                                    .isNotBlank()
                            ) {
                                schedulePickerlReminder(
                                    context =
                                        context,
                                    vehicleName =
                                        selectedVehicle,
                                    nextDate =
                                        nextDate
                                )
                            }

                        },
                        modifier =
                            Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "Pickerl speichern"
                        )
                    }
                }
            }

            Spacer(
                modifier =
                    Modifier.height(
                        16.dp
                    )
            )

            val existing =
                store.pickerls.firstOrNull {
                    it.vehicleName ==
                        selectedVehicle
                }

            if (
                existing != null
            ) {

                Card(
                    modifier =
                        Modifier.fillMaxWidth()
                ) {

                    Column(
                        modifier =
                            Modifier.padding(
                                16.dp
                            )
                    ) {

                        Text(
                            text =
                                "Gespeicherte Daten",
                            style =
                                MaterialTheme.typography
                                    .titleMedium,
                            fontWeight =
                                FontWeight.Bold
                        )

                        Spacer(
                            modifier =
                                Modifier.height(
                                    8.dp
                                )
                        )

                        Text(
                            text =
                                "Letztes Pickerl: " +
                                    existing.lastDate
                        )

                        Text(
                            text =
                                "Nächstes Pickerl: " +
                                    existing.nextDate
                        )

                        if (
                            existing.notes
                                .isNotBlank()
                        ) {
                            Text(
                                text =
                                    "Notizen: " +
                                        existing.notes
                            )
                        }

                        Text(
                            text =
                                if (
                                    existing
                                        .reminder
                                ) {
                                    "Erinnerung: aktiv"
                                } else {
                                    "Erinnerung: deaktiviert"
                                }
                        )
                    }
                }
            }
        }
    }
}

fun schedulePickerlReminder(
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
            date.minusMonths(3)

        val triggerTime =
            reminderDate
                .atTime(9, 0)
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

        val pendingIntent =
            PendingIntent.getBroadcast(
                context,
                vehicleName.hashCode(),
                intent,
                PendingIntent
                    .FLAG_UPDATE_CURRENT or
                    PendingIntent
                        .FLAG_IMMUTABLE
            )

        val alarmManager =
            context.getSystemService(
                Context.ALARM_SERVICE
            ) as AlarmManager

        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerTime,
            pendingIntent
        )

    } catch (
        _: Exception
    ) {
    }
}

@Composable
fun MaintenanceScreen(
    store: VehicleStore,
    onBack: () -> Unit
) {

    var selectedVehicle by remember {
        mutableStateOf(
            store.activeVehicleName
        )
    }

    var showForm by remember {
        mutableStateOf(false)
    }

    var editingMaintenance by remember {
        mutableStateOf<
            Maintenance?
        >(null)
    }

    DetailScaffold(
        title = "Wartungen",
        onBack = onBack
    ) {

        VehicleSelector(
            vehicles =
                store.vehicles,
            selectedVehicle =
                selectedVehicle,
            onVehicleSelected = {
                selectedVehicle = it
                store.activeVehicleName =
                    it
                store.save()
            }
        )

        Spacer(
            modifier =
                Modifier.height(
                    16.dp
                )
        )

        Button(
            onClick = {
                editingMaintenance =
                    null
                showForm = true
            },
            modifier =
                Modifier.fillMaxWidth()
        ) {

            Icon(
                imageVector =
                    Icons.Default.Add,
                contentDescription =
                    null
            )

            Spacer(
                modifier =
                    Modifier.width(
                        8.dp
                    )
            )

            Text(
                "Wartung hinzufügen"
            )
        }

        Spacer(
            modifier =
                Modifier.height(
                    16.dp
                )
        )

        val list =
            store.maintenances.filter {
                it.vehicleName ==
                    selectedVehicle
            }

        if (
            list.isEmpty()
        ) {

            Text(
                text =
                    "Für dieses Fahrzeug sind noch keine Wartungen gespeichert.",
                color =
                    Color.LightGray
            )

        } else {

            list.forEach {
                    maintenance ->

                MaintenanceCard(
                    maintenance =
                        maintenance,
                    onEdit = {
                        editingMaintenance =
                            maintenance
                        showForm = true
                    },
                    onDelete = {
                        store.maintenances
                            .removeAll {
                                it.id ==
                                    maintenance.id
                            }
                        store.save()
                    }
                )

                Spacer(
                    modifier =
                        Modifier.height(
                            10.dp
                        )
                )
            }
        }

        if (showForm) {

            Spacer(
                modifier =
                    Modifier.height(
                        20.dp
                    )
            )

            MaintenanceForm(
                vehicleName =
                    selectedVehicle,
                maintenance =
                    editingMaintenance,
                onCancel = {
                    showForm =
                        false
                    editingMaintenance =
                        null
                },
                onSave = {
                    maintenance ->

                    val old =
                        editingMaintenance

                    if (old == null) {
                        store.maintenances
                            .add(
                                maintenance
                            )
                    } else {

                        val index =
                            store.maintenances
                                .indexOfFirst {
                                    it.id ==
                                        old.id
                                }

                        if (index >= 0) {
                            store.maintenances[
                                index
                            ] =
                                maintenance.copy(
                                    id =
                                        old.id
                                )
                        }
                    }

                    store.save()

                    showForm =
                        false
                    editingMaintenance =
                        null
                }
            )
        }
    }
}

@Composable
fun MaintenanceCard(
    maintenance: Maintenance,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {

    Card(
        modifier =
            Modifier.fillMaxWidth()
    ) {

        Column(
            modifier =
                Modifier.padding(
                    16.dp
                )
        ) {

            Text(
                text =
                    "Wartung",
                style =
                    MaterialTheme.typography
                        .titleMedium,
                fontWeight =
                    FontWeight.Bold
            )

            Spacer(
                modifier =
                    Modifier.height(
                        6.dp
                    )
            )

            Text(
                text =
                    "Datum: ${maintenance.date}"
            )

            Text(
                text =
                    "Kilometerstand: ${maintenance.mileage}"
            )

            Text(
                text =
                    "Kosten: ${maintenance.cost} €"
            )

            if (
                maintenance.workshop
                    .isNotBlank()
            ) {
                Text(
                    text =
                        "Werkstatt: " +
                            maintenance.workshop
                )
            }

            if (
                maintenance.notes
                    .isNotBlank()
            ) {
                Text(
                    text =
                        "Notizen: " +
                            maintenance.notes
                )
            }

            Spacer(
                modifier =
                    Modifier.height(
                        10.dp
                    )
            )

            Row(
                modifier =
                    Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.End
            ) {

                OutlinedButton(
                    onClick =
                        onEdit
                ) {
                    Text(
                        "Bearbeiten"
                    )
                }

                Spacer(
                    modifier =
                        Modifier.width(
                            8.dp
                        )
                )

                IconButton(
                    onClick =
                        onDelete
                ) {
                    Icon(
                        imageVector =
                            Icons.Default
                                .Delete,
                        contentDescription =
                            "Löschen",
                        tint =
                            Color.Red
                    )
                }
            }
        }
    }
}

@Composable
fun MaintenanceForm(
    vehicleName: String,
    maintenance: Maintenance?,
    onCancel: () -> Unit,
    onSave:
        (Maintenance) -> Unit
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
            maintenance?.mileage ?: ""
        )
    }

    var cost by remember {
        mutableStateOf(
            maintenance?.cost ?: ""
        )
    }

    var workshop by remember {
        mutableStateOf(
            maintenance?.workshop ?: ""
        )
    }

    var notes by remember {
        mutableStateOf(
            maintenance?.notes ?: ""
        )
    }

    Card(
        modifier =
            Modifier.fillMaxWidth()
    ) {

        Column(
            modifier =
                Modifier.padding(
                    16.dp
                )
        ) {

            Text(
                text =
                    if (
                        maintenance ==
                        null
                    ) {
                        "Neue Wartung"
                    } else {
                        "Wartung bearbeiten"
                    },
                style =
                    MaterialTheme.typography
                        .titleLarge,
                fontWeight =
                    FontWeight.Bold
            )

            Spacer(
                modifier =
                    Modifier.height(
                        12.dp
                    )
            )

            FormField(
                label = "Datum",
                value = date,
                onValueChange = {
                    date = it
                }
            )

            FormField(
                label =
                    "Kilometerstand",
                value =
                    mileage,
                onValueChange = {
                    mileage = it
                }
            )

            FormField(
                label =
                    "Kosten in €",
                value =
                    cost,
                onValueChange = {
                    cost = it
                }
            )

            FormField(
                label =
                    "Werkstatt",
                value =
                    workshop,
                onValueChange = {
                    workshop = it
                }
            )

            FormField(
                label =
                    "Notizen",
                value =
                    notes,
                onValueChange = {
                    notes = it
                },
                singleLine =
                    false
            )

            Spacer(
                modifier =
                    Modifier.height(
                        12.dp
                    )
            )

            Row(
                modifier =
                    Modifier.fillMaxWidth()
            ) {

                OutlinedButton(
                    onClick =
                        onCancel,
                    modifier =
                        Modifier.weight(
                            1f
                        )
                ) {
                    Text(
                        "Abbrechen"
                    )
                }

                Spacer(
                    modifier =
                        Modifier.width(
                            8.dp
                        )
                )

                Button(
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
                    },
                    modifier =
                        Modifier.weight(
                            1f
                        )
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
fun TiresScreen(
    store: VehicleStore,
    onBack: () -> Unit
) {

    var selectedVehicle by remember {
        mutableStateOf(
            store.activeVehicleName
        )
    }

    var showForm by remember {
        mutableStateOf(false)
    }

    var editingTire by remember {
        mutableStateOf<TireSet?>(
            null
        )
    }

    DetailScaffold(
        title = "Reifen",
        onBack = onBack
    ) {

        VehicleSelector(
            vehicles =
                store.vehicles,
            selectedVehicle =
                selectedVehicle,
            onVehicleSelected = {
                selectedVehicle = it
                store.activeVehicleName =
                    it
                store.save()
            }
        )

        Spacer(
            modifier =
                Modifier.height(
                    16.dp
                )
        )

        Button(
            onClick = {
                editingTire = null
                showForm = true
            },
            modifier =
                Modifier.fillMaxWidth()
        ) {

            Icon(
                imageVector =
                    Icons.Default.Add,
                contentDescription =
                    null
            )

            Spacer(
                modifier =
                    Modifier.width(
                        8.dp
                    )
            )

            Text(
                "Reifensatz hinzufügen"
            )
        }

        Spacer(
            modifier =
                Modifier.height(
                    16.dp
                )
        )

        val list =
            store.tires.filter {
                it.vehicleName ==
                    selectedVehicle
            }

        if (
            list.isEmpty()
        ) {

            Text(
                text =
                    "Für dieses Fahrzeug sind noch keine Reifendaten gespeichert.",
                color =
                    Color.LightGray
            )

        } else {

            list.forEach { tire ->

                TireCard(
                    tire = tire,
                    onEdit = {
                        editingTire =
                            tire
                        showForm = true
                    },
                    onDelete = {
                        store.tires
                            .removeAll {
                                it.id ==
                                    tire.id
                            }
                        store.save()
                    }
                )

                Spacer(
                    modifier =
                        Modifier.height(
                            10.dp
                        )
                )
            }
        }

        if (showForm) {

            Spacer(
                modifier =
                    Modifier.height(
                        20.dp
                    )
            )

            TireForm(
                vehicleName =
                    selectedVehicle,
                tire =
                    editingTire,
                onCancel = {
                    showForm =
                        false
                    editingTire =
                        null
                },
                onSave = {
                    tire ->

                    val old =
                        editingTire

                    if (old == null) {
                        store.tires.add(
                            tire
                        )
                    } else {

                        val index =
                            store.tires
                                .indexOfFirst {
                                    it.id ==
                                        old.id
                                }

                        if (index >= 0) {
                            store.tires[
                                index
                            ] =
                                tire.copy(
                                    id =
                                        old.id
                                )
                        }
                    }

                    store.save()

                    showForm =
                        false
                    editingTire =
                        null
                }
            )
        }
    }
}

@Composable
fun TireCard(
    tire: TireSet,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {

    Card(
        modifier =
            Modifier.fillMaxWidth()
    ) {

        Column(
            modifier =
                Modifier.padding(
                    16.dp
                )
        ) {

            Text(
                text =
                    tire.season,
                style =
                    MaterialTheme.typography
                        .titleMedium,
                fontWeight =
                    FontWeight.Bold
            )

            Spacer(
                modifier =
                    Modifier.height(
                        6.dp
                    )
            )

            Text(
                text =
                    "Dimension: ${tire.dimension}"
            )

            Text(
                text =
                    "Marke: ${tire.brand}"
            )

            Text(
                text =
                    "DOT: ${tire.dot}"
            )

            Text(
                text =
                    "Profiltiefe: ${tire.treadDepth}"
            )

            Text(
                text =
                    "Zustand: ${tire.condition}"
            )

            Text(
                text =
                    "Lagerung: ${tire.storage}"
            )

            Spacer(
                modifier =
                    Modifier.height(
                        10.dp
                    )
            )

            Row(
                modifier =
                    Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.End
            ) {

                OutlinedButton(
                    onClick =
                        onEdit
                ) {
                    Text(
                        "Bearbeiten"
                    )
                }

                Spacer(
                    modifier =
                        Modifier.width(
                            8.dp
                        )
                )

                IconButton(
                    onClick =
                        onDelete
                ) {
                    Icon(
                        imageVector =
                            Icons.Default
                                .Delete,
                        contentDescription =
                            "Löschen",
                        tint =
                            Color.Red
                    )
                }
            }
        }
    }
}

@Composable
fun TireForm(
    vehicleName: String,
    tire: TireSet?,
    onCancel: () -> Unit,
    onSave:
        (TireSet) -> Unit
) {

    var season by remember {
        mutableStateOf(
            tire?.season
                ?: "Sommer"
        )
    }

    var dimension by remember {
        mutableStateOf(
            tire?.dimension ?: ""
        )
    }

    var brand by remember {
        mutableStateOf(
            tire?.brand ?: ""
        )
    }

    var dot by remember {
        mutableStateOf(
            tire?.dot ?: ""
        )
    }

    var treadDepth by remember {
        mutableStateOf(
            tire?.treadDepth ?: ""
        )
    }

    var condition by remember {
        mutableStateOf(
            tire?.condition ?: ""
        )
    }

    var storage by remember {
        mutableStateOf(
            tire?.storage ?: ""
        )
    }

    var seasonExpanded by remember {
        mutableStateOf(false)
    }

    Card(
        modifier =
            Modifier.fillMaxWidth()
    ) {

        Column(
            modifier =
                Modifier.padding(
                    16.dp
                )
        ) {

            Text(
                text =
                    if (
                        tire == null
                    ) {
                        "Neuer Reifensatz"
                    } else {
                        "Reifensatz bearbeiten"
                    },
                style =
                    MaterialTheme.typography
                        .titleLarge,
                fontWeight =
                    FontWeight.Bold
            )

            Spacer(
                modifier =
                    Modifier.height(
                        12.dp
                    )
            )

            Box(
                modifier =
                    Modifier.fillMaxWidth()
            ) {

                OutlinedButton(
                    onClick = {
                        seasonExpanded =
                            true
                    },
                    modifier =
                        Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Saison: $season"
                    )

                    Spacer(
                        modifier =
                            Modifier.weight(
                                1f
                            )
                    )

                    Icon(
                        imageVector =
                            Icons.Default
                                .KeyboardArrowDown,
                        contentDescription =
                            null
                    )
                }

                DropdownMenu(
                    expanded =
                        seasonExpanded,
                    onDismissRequest = {
                        seasonExpanded =
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
                            seasonExpanded =
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
                            seasonExpanded =
                                false
                        }
                    )
                }
            }

            FormField(
                label =
                    "Dimension",
                value =
                    dimension,
                onValueChange = {
                    dimension = it
                }
            )

            FormField(
                label =
                    "Marke",
                value =
                    brand,
                onValueChange = {
                    brand = it
                }
            )

            FormField(
                label =
                    "DOT",
                value =
                    dot,
                onValueChange = {
                    dot = it
                }
            )

            FormField(
                label =
                    "Profiltiefe",
                value =
                    treadDepth,
                onValueChange = {
                    treadDepth = it
                }
            )

            FormField(
                label =
                    "Zustand",
                value =
                    condition,
                onValueChange = {
                    condition = it
                }
            )

            FormField(
                label =
                    "Lagerung",
                value =
                    storage,
                onValueChange = {
                    storage = it
                }
            )

            Spacer(
                modifier =
                    Modifier.height(
                        12.dp
                    )
            )

            Row(
                modifier =
                    Modifier.fillMaxWidth()
            ) {

                OutlinedButton(
                    onClick =
                        onCancel,
                    modifier =
                        Modifier.weight(
                            1f
                        )
                ) {
                    Text(
                        "Abbrechen"
                    )
                }

                Spacer(
                    modifier =
                        Modifier.width(
                            8.dp
                        )
                )

                Button(
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
                                    treadDepth,
                                condition =
                                    condition,
                                storage =
                                    storage
                            )
                        )
                    },
                    modifier =
                        Modifier.weight(
                            1f
                        )
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
fun OverviewScreen(
    store: VehicleStore,
    onBack: () -> Unit
) {

    var selectedVehicle by remember {
        mutableStateOf(
            store.activeVehicleName
        )
    }

    DetailScaffold(
        title = "Gesamtblick",
        onBack = onBack
    ) {

        VehicleSelector(
            vehicles =
                store.vehicles,
            selectedVehicle =
                selectedVehicle,
            onVehicleSelected = {
                selectedVehicle = it
                store.activeVehicleName =
                    it
                store.save()
            }
        )

        Spacer(
            modifier =
                Modifier.height(
                    20.dp
                )
        )

        val vehicle =
            store.vehicles.firstOrNull {
                it.name ==
                    selectedVehicle
            }

        if (vehicle == null) {

            Text(
                text =
                    "Bitte zuerst ein Fahrzeug anlegen.",
                color =
                    Color.LightGray
            )

        } else {

            VehicleOverviewCard(
                vehicle =
                    vehicle
            )

            Spacer(
                modifier =
                    Modifier.height(
                        12.dp
                    )
            )

            val repairCount =
                store.repairs.count {
                    it.vehicleName ==
                        selectedVehicle
                }

            val maintenanceCount =
                store.maintenances.count {
                    it.vehicleName ==
                        selectedVehicle
                }

            val tireCount =
                store.tires.count {
                    it.vehicleName ==
                        selectedVehicle
                }

            val pickerl =
                store.pickerls.firstOrNull {
                    it.vehicleName ==
                        selectedVehicle
                }

            OverviewStatCard(
                title =
                    "Reparaturen",
                value =
                    repairCount.toString()
            )

            OverviewStatCard(
                title =
                    "Wartungen",
                value =
                    maintenanceCount
                        .toString()
            )

            OverviewStatCard(
                title =
                    "Reifensätze",
                value =
                    tireCount.toString()
            )

            if (pickerl != null) {

                Spacer(
                    modifier =
                        Modifier.height(
                            12.dp
                        )
                )

                Card(
                    modifier =
                        Modifier.fillMaxWidth()
                ) {

                    Column(
                        modifier =
                            Modifier.padding(
                                16.dp
                            )
                    ) {

                        Text(
                            text =
                                "Pickerl",
                            style =
                                MaterialTheme.typography
                                    .titleMedium,
                            fontWeight =
                                FontWeight.Bold
                        )

                        Spacer(
                            modifier =
                                Modifier.height(
                                    6.dp
                                )
                        )

                        Text(
                            text =
                                "Nächster Termin: " +
                                    pickerl.nextDate
                        )

                        Text(
                            text =
                                if (
                                    pickerl
                                        .reminder
                                ) {
                                    "Erinnerung aktiviert"
                                } else {
                                    "Erinnerung deaktiviert"
                                }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun VehicleOverviewCard(
    vehicle: Vehicle
) {

    Card(
        modifier =
            Modifier.fillMaxWidth()
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
                            200.dp
                        )
            )

            Spacer(
                modifier =
                    Modifier.height(
                        12.dp
                    )
            )

            Text(
                text =
                    vehicle.name,
                style =
                    MaterialTheme.typography
                        .headlineSmall,
                fontWeight =
                    FontWeight.Bold
            )

            Text(
                text =
                    "${vehicle.make} ${vehicle.model}"
            )

            if (
                vehicle.year
                    .isNotBlank()
            ) {
                Text(
                    text =
                        "Baujahr: " +
                            vehicle.year
                )
            }

            if (
                vehicle.plate
                    .isNotBlank()
            ) {
                Text(
                    text =
                        "Kennzeichen: " +
                            vehicle.plate
                )
            }

            if (
                vehicle.vin
                    .isNotBlank()
            ) {
                Spacer(
                    modifier =
                        Modifier.height(
                            4.dp
                        )
                )

                Text(
                    text =
                        "VIN / FIN: " +
                            vehicle.vin
                )
            }
        }
    }
}

@Composable
fun OverviewStatCard(
    title: String,
    value: String
) {

    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    vertical = 4.dp
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
                text =
                    title,
                style =
                    MaterialTheme.typography
                        .titleMedium,
                modifier =
                    Modifier.weight(
                        1f
                    )
            )

            Text(
                text =
                    value,
                style =
                    MaterialTheme.typography
                        .headlineSmall,
                fontWeight =
                    FontWeight.Bold
            )
        }
    }
}
