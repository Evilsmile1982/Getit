package de.autocheck.app

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import de.autocheck.app.ui.theme.AutoCheckTheme
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

private val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

data class Vehicle(
    val id: String = UUID.randomUUID().toString(),
    var name: String = "",
    var brand: String = "",
    var model: String = "",
    var year: String = "",
    var plate: String = "",
    var vin: String = "",
    var imageUri: String = ""
)

data class Repair(
    val id: String = UUID.randomUUID().toString(),
    val vehicleId: String,
    var date: String = "",
    var mileage: String = "",
    var description: String = "",
    var cost: String = "",
    var workshop: String = ""
)

data class Maintenance(
    val id: String = UUID.randomUUID().toString(),
    val vehicleId: String,
    var date: String = "",
    var mileage: String = "",
    var description: String = "",
    var cost: String = "",
    var workshop: String = ""
)

data class TireSet(
    val id: String = UUID.randomUUID().toString(),
    val vehicleId: String,
    var season: String = "",
    var dimension: String = "",
    var brand: String = "",
    var dot: String = "",
    var tread: String = "",
    var condition: String = "",
    var storage: String = ""
)

class Store(private val context: Context) {

    private val prefs =
        context.getSharedPreferences("autocheck_data", Context.MODE_PRIVATE)

    var vehicles by mutableStateOf(loadVehicles())
        private set

    var repairs by mutableStateOf(loadRepairs())
        private set

    var maintenances by mutableStateOf(loadMaintenances())
        private set

    var tires by mutableStateOf(loadTires())
        private set

    var activeVehicleId by mutableStateOf(
        prefs.getString("active_vehicle", null)
    )
        private set

    fun saveVehicles(value: List<Vehicle>) {
        vehicles = value
        val array = JSONArray()

        value.forEach {
            array.put(
                JSONObject().apply {
                    put("id", it.id)
                    put("name", it.name)
                    put("brand", it.brand)
                    put("model", it.model)
                    put("year", it.year)
                    put("plate", it.plate)
                    put("vin", it.vin)
                    put("imageUri", it.imageUri)
                }
            )
        }

        prefs.edit()
            .putString("vehicles", array.toString())
            .apply()
    }

    fun saveRepairs(value: List<Repair>) {
        repairs = value
        val array = JSONArray()

        value.forEach {
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

    fun saveMaintenances(value: List<Maintenance>) {
        maintenances = value
        val array = JSONArray()

        value.forEach {
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
            .putString("maintenances", array.toString())
            .apply()
    }

    fun saveTires(value: List<TireSet>) {
        tires = value
        val array = JSONArray()

        value.forEach {
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

    fun setActiveVehicle(id: String) {
        activeVehicleId = id
        prefs.edit().putString("active_vehicle", id).apply()
    }

    private fun loadVehicles(): List<Vehicle> {
        val result = mutableListOf<Vehicle>()
        val array = JSONArray(prefs.getString("vehicles", "[]"))

        for (i in 0 until array.length()) {
            val o = array.getJSONObject(i)
            result.add(
                Vehicle(
                    id = o.optString("id"),
                    name = o.optString("name"),
                    brand = o.optString("brand"),
                    model = o.optString("model"),
                    year = o.optString("year"),
                    plate = o.optString("plate"),
                    vin = o.optString("vin"),
                    imageUri = o.optString("imageUri")
                )
            )
        }

        return result
    }

    private fun loadRepairs(): List<Repair> {
        val result = mutableListOf<Repair>()
        val array = JSONArray(prefs.getString("repairs", "[]"))

        for (i in 0 until array.length()) {
            val o = array.getJSONObject(i)
            result.add(
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

        return result
    }

    private fun loadMaintenances(): List<Maintenance> {
        val result = mutableListOf<Maintenance>()
        val array = JSONArray(prefs.getString("maintenances", "[]"))

        for (i in 0 until array.length()) {
            val o = array.getJSONObject(i)
            result.add(
                Maintenance(
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

        return result
    }

    private fun loadTires(): List<TireSet> {
        val result = mutableListOf<TireSet>()
        val array = JSONArray(prefs.getString("tires", "[]"))

        for (i in 0 until array.length()) {
            val o = array.getJSONObject(i)
            result.add(
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

        return result
    }
}

enum class Screen {
    HOME,
    VEHICLES,
    REPAIRS,
    PICKERL,
    MAINTENANCE,
    OVERVIEW,
    TIRES
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)

        createNotificationChannel(this)

        setContent {
            AutoCheckTheme {
                AutoCheckApp()
            }
        }
    }
}

@Composable
fun AutoCheckApp() {

    val context = LocalContext.current
    val store = remember { Store(context) }

    var screen by remember { mutableStateOf(Screen.HOME) }

    val activeVehicle =
        store.vehicles.firstOrNull { it.id == store.activeVehicleId }
            ?: store.vehicles.firstOrNull()

    LaunchedEffect(store.vehicles) {
        if (store.vehicles.isNotEmpty() &&
            store.activeVehicleId == null
        ) {
            store.setActiveVehicle(store.vehicles.first().id)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            if (screen != Screen.HOME) {
                TopAppBar(
                    title = {
                        Text(
                            when (screen) {
                                Screen.VEHICLES -> "Mein Auto"
                                Screen.REPAIRS -> "Reparaturen"
                                Screen.PICKERL -> "Pickerl / TÜV"
                                Screen.MAINTENANCE -> "Wartungen"
                                Screen.OVERVIEW -> "Gesamtblick"
                                Screen.TIRES -> "Reifen"
                                Screen.HOME -> "AutoCheck"
                            }
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                screen = Screen.HOME
                            }
                        ) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Zurück"
                            )
                        }
                    }
                )
            }

            when (screen) {

                Screen.HOME -> HomeScreen(
                    onVehicles = { screen = Screen.VEHICLES },
                    onRepairs = { screen = Screen.REPAIRS },
                    onPickerl = { screen = Screen.PICKERL },
                    onMaintenance = { screen = Screen.MAINTENANCE },
                    onOverview = { screen = Screen.OVERVIEW },
                    onTires = { screen = Screen.TIRES }
                )

                Screen.VEHICLES -> VehiclesScreen(
                    store = store
                )

                Screen.REPAIRS -> RepairsScreen(
                    store = store,
                    activeVehicle = activeVehicle
                )

                Screen.PICKERL -> PickerlScreen(
                    store = store,
                    activeVehicle = activeVehicle
                )

                Screen.MAINTENANCE -> MaintenanceScreen(
                    store = store,
                    activeVehicle = activeVehicle
                )

                Screen.OVERVIEW -> OverviewScreen(
                    store = store,
                    activeVehicle = activeVehicle
                )

                Screen.TIRES -> TiresScreen(
                    store = store,
                    activeVehicle = activeVehicle
                )
            }
        }
    }
}

@Composable
fun HomeScreen(
    onVehicles: () -> Unit,
    onRepairs: () -> Unit,
    onPickerl: () -> Unit,
    onMaintenance: () -> Unit,
    onOverview: () -> Unit,
    onTires: () -> Unit
) {

    val buttons = listOf(
        Triple("Mein Auto", Icons.Default.DirectionsCar, onVehicles),
        Triple("Reparaturen", Icons.Default.Build, onRepairs),
        Triple("Pickerl / TÜV", Icons.Default.Event, onPickerl),
        Triple("Wartungen", Icons.Default.Construction, onMaintenance),
        Triple("Gesamtblick", Icons.Default.Dashboard, onOverview),
        Triple("Reifen", Icons.Default.TireRepair, onTires)
    )

    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        item {

            Spacer(modifier = Modifier.height(12.dp))

            Image(
                painter = painterResource(
                    id = de.autocheck.app.R.drawable.bild_3
                ),
                contentDescription = "AutoCheck",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp)
                    .clip(RoundedCornerShape(28.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                "AutoCheck",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                "Dein Fahrzeug. Deine Daten. Dein Überblick.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(22.dp))
        }

        items(buttons) { button ->

            AnimatedVisibility(
                visible = visible,
                enter = slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = androidx.compose.animation.core.tween(
                        durationMillis = 2500
                    )
                )
            ) {

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clickable { button.third() },
                    shape = RoundedCornerShape(22.dp)
                ) {

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Icon(
                            button.second,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp)
                        )

                        Spacer(modifier = Modifier.width(18.dp))

                        Text(
                            button.first,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = null
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun VehicleSelector(
    store: Store,
    activeVehicle: Vehicle?
) {

    if (store.vehicles.isEmpty()) {
        Text("Noch kein Fahrzeug angelegt.")
        return
    }

    var expanded by remember { mutableStateOf(false) }

    Box {

        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                activeVehicle?.let {
                    "${it.brand} ${it.model}".trim()
                        .ifBlank { it.name.ifBlank { "Fahrzeug wählen" } }
                } ?: "Fahrzeug wählen"
            )

            Spacer(modifier = Modifier.weight(1f))

            Icon(
                Icons.Default.ArrowDropDown,
                contentDescription = null
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {

            store.vehicles.forEach { vehicle ->

                DropdownMenuItem(
                    text = {
                        Text(
                            "${vehicle.brand} ${vehicle.model}"
                                .trim()
                                .ifBlank {
                                    vehicle.name.ifBlank {
                                        "Fahrzeug"
                                    }
                                }
                        )
                    },
                    onClick = {
                        store.setActiveVehicle(vehicle.id)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun VehiclesScreen(store: Store) {

    var editing by remember { mutableStateOf<Vehicle?>(null) }
    var adding by remember { mutableStateOf(false) }

    if (adding || editing != null) {

        VehicleForm(
            vehicle = editing,
            onCancel = {
                adding = false
                editing = null
            },
            onSave = { vehicle ->

                val updated =
                    if (editing == null) {
                        store.vehicles + vehicle
                    } else {
                        store.vehicles.map {
                            if (it.id == vehicle.id) vehicle else it
                        }
                    }

                store.saveVehicles(updated)

                if (store.activeVehicleId == null) {
                    store.setActiveVehicle(vehicle.id)
                }

                adding = false
                editing = null
            }
        )

        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(18.dp)
    ) {

        item {

            Button(
                onClick = {
                    if (store.vehicles.size < 5) {
                        adding = true
                    }
                },
                enabled = store.vehicles.size < 5,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, null)
                Spacer(Modifier.width(8.dp))
                Text("Fahrzeug hinzufügen")
            }

            Spacer(Modifier.height(16.dp))
        }

        items(store.vehicles) { vehicle ->

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                shape = RoundedCornerShape(20.dp)
            ) {

                Column(
                    modifier = Modifier.padding(18.dp)
                ) {

                    Text(
                        "${vehicle.brand} ${vehicle.model}"
                            .trim()
                            .ifBlank {
                                vehicle.name.ifBlank { "Mein Fahrzeug" }
                            },
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Bold
                    )

                    if (vehicle.plate.isNotBlank()) {
                        Text("Kennzeichen: ${vehicle.plate}")
                    }

                    if (vehicle.year.isNotBlank()) {
                        Text("Baujahr: ${vehicle.year}")
                    }

                    Spacer(Modifier.height(12.dp))

                    Row {

                        Button(
                            onClick = {
                                store.setActiveVehicle(vehicle.id)
                            }
                        ) {
                            Text(
                                if (store.activeVehicleId == vehicle.id)
                                    "Aktiv"
                                else
                                    "Auswählen"
                            )
                        }

                        Spacer(Modifier.width(8.dp))

                        OutlinedButton(
                            onClick = {
                                editing = vehicle
                            }
                        ) {
                            Text("Bearbeiten")
                        }

                        Spacer(Modifier.width(8.dp))

                        IconButton(
                            onClick = {
                                store.saveVehicles(
                                    store.vehicles.filter {
                                        it.id != vehicle.id
                                    }
                                )
                            }
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Löschen"
                            )
                        }
                    }
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

    var name by remember { mutableStateOf(vehicle?.name ?: "") }
    var brand by remember { mutableStateOf(vehicle?.brand ?: "") }
    var model by remember { mutableStateOf(vehicle?.model ?: "") }
    var year by remember { mutableStateOf(vehicle?.year ?: "") }
    var plate by remember { mutableStateOf(vehicle?.plate ?: "") }
    var vin by remember { mutableStateOf(vehicle?.vin ?: "") }
    var imageUri by remember { mutableStateOf(vehicle?.imageUri ?: "") }

    val launcher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            uri?.let {
                imageUri = it.toString()
            }
        }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(18.dp)
    ) {

        item {

            Text(
                if (vehicle == null)
                    "Neues Fahrzeug"
                else
                    "Fahrzeug bearbeiten",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(16.dp))

            FormField("Bezeichnung", name) {
                name = it
            }

            FormField("Marke", brand) {
                brand = it
            }

            FormField("Modell", model) {
                model = it
            }

            FormField("Baujahr", year) {
                year = it
            }

            FormField("Kennzeichen", plate) {
                plate = it
            }

            FormField("FIN / VIN", vin) {
                vin = it
            }

            Spacer(Modifier.height(8.dp))

            OutlinedButton(
                onClick = {
                    launcher.launch("image/*")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Image, null)
                Spacer(Modifier.width(8.dp))
                Text(
                    if (imageUri.isBlank())
                        "Fahrzeugbild auswählen"
                    else
                        "Fahrzeugbild ausgewählt"
                )
            }

            Spacer(Modifier.height(18.dp))

            Row {

                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Abbrechen")
                }

                Spacer(Modifier.width(10.dp))

                Button(
                    onClick = {
                        onSave(
                            Vehicle(
                                id = vehicle?.id
                                    ?: UUID.randomUUID().toString(),
                                name = name,
                                brand = brand,
                                model = model,
                                year = year,
                                plate = plate,
                                vin = vin,
                                imageUri = imageUri
                            )
                        )
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Speichern")
                }
            }

            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
fun FormField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        singleLine = true
    )
}

@Composable
fun RepairsScreen(
    store: Store,
    activeVehicle: Vehicle?
) {

    if (activeVehicle == null) {
        EmptyVehicleMessage()
        return
    }

    var adding by remember { mutableStateOf(false) }

    if (adding) {

        RepairForm(
            vehicleId = activeVehicle.id,
            onCancel = {
                adding = false
            },
            onSave = {
                store.saveRepairs(store.repairs + it)
                adding = false
            }
        )

        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(18.dp)
    ) {

        item {

            VehicleSelector(store, activeVehicle)

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = {
                    adding = true
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, null)
                Spacer(Modifier.width(8.dp))
                Text("Reparatur hinzufügen")
            }

            Spacer(Modifier.height(12.dp))
        }

        items(
            store.repairs.filter {
                it.vehicleId == activeVehicle.id
            }
        ) { repair ->

            DataCard(
                title = repair.description.ifBlank {
                    "Reparatur"
                },
                lines = listOf(
                    "Datum" to repair.date,
                    "Kilometerstand" to repair.mileage,
                    "Kosten" to repair.cost,
                    "Werkstatt" to repair.workshop
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

@Composable
fun RepairForm(
    vehicleId: String,
    onCancel: () -> Unit,
    onSave: (Repair) -> Unit
) {

    var date by remember { mutableStateOf("") }
    var mileage by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var cost by remember { mutableStateOf("") }
    var workshop by remember { mutableStateOf("") }

    EntryForm(
        title = "Neue Reparatur",
        fields = listOf(
            "Datum" to date,
            "Kilometerstand" to mileage,
            "Beschreibung" to description,
            "Kosten" to cost,
            "Werkstatt" to workshop
        ),
        setters = listOf(
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
                    description = description,
                    cost = cost,
                    workshop = workshop
                )
            )
        }
    )
}

@Composable
fun MaintenanceScreen(
    store: Store,
    activeVehicle: Vehicle?
) {

    if (activeVehicle == null) {
        EmptyVehicleMessage()
        return
    }

    var adding by remember { mutableStateOf(false) }

    if (adding) {

        MaintForm(
            vehicleId = activeVehicle.id,
            onCancel = {
                adding = false
            },
            onSave = {
                store.saveMaintenances(store.maintenances + it)
                adding = false
            }
        )

        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(18.dp)
    ) {

        item {

            VehicleSelector(store, activeVehicle)

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = {
                    adding = true
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, null)
                Spacer(Modifier.width(8.dp))
                Text("Wartung hinzufügen")
            }

            Spacer(Modifier.height(12.dp))
        }

        items(
            store.maintenances.filter {
                it.vehicleId == activeVehicle.id
            }
        ) { maintenance ->

            DataCard(
                title = maintenance.description.ifBlank {
                    "Wartung"
                },
                lines = listOf(
                    "Datum" to maintenance.date,
                    "Kilometerstand" to maintenance.mileage,
                    "Kosten" to maintenance.cost,
                    "Werkstatt" to maintenance.workshop
                ),
                onDelete = {
                    store.saveMaintenances(
                        store.maintenances.filter {
                            it.id != maintenance.id
                        }
                    )
                }
            )
        }
    }
}

@Composable
fun MaintForm(
    vehicleId: String,
    onCancel: () -> Unit,
    onSave: (Maintenance) -> Unit
) {

    var date by remember { mutableStateOf("") }
    var mileage by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var cost by remember { mutableStateOf("") }
    var workshop by remember { mutableStateOf("") }

    EntryForm(
        title = "Neue Wartung",
        fields = listOf(
            "Datum" to date,
            "Kilometerstand" to mileage,
            "Beschreibung" to description,
            "Kosten" to cost,
            "Werkstatt" to workshop
        ),
        setters = listOf(
            { date = it },
            { mileage = it },
            { description = it },
            { cost = it },
            { workshop = it }
        ),
        onCancel = onCancel,
        onSave = {
            onSave(
                Maintenance(
                    vehicleId = vehicleId,
                    date = date,
                    mileage = mileage,
                    description = description,
                    cost = cost,
                    workshop = workshop
                )
            )
        }
    )
}

@Composable
fun EntryForm(
    title: String,
    fields: List<Pair<String, String>>,
    setters: List<(String) -> Unit>,
    onCancel: () -> Unit,
    onSave: () -> Unit
) {

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(18.dp)
    ) {

        item {

            Text(
                title,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(15.dp))

            fields.forEachIndexed { index, field ->

                FormField(
                    label = field.first,
                    value = field.second,
                    onValueChange = setters[index]
                )
            }

            Spacer(Modifier.height(15.dp))

            Row {

                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Abbrechen")
                }

                Spacer(Modifier.width(10.dp))

                Button(
                    onClick = onSave,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Speichern")
                }
            }
        }
    }
}

@Composable
fun PickerlScreen(
    store: Store,
    activeVehicle: Vehicle?
) {

    if (activeVehicle == null) {
        EmptyVehicleMessage()
        return
    }

    var lastDate by remember { mutableStateOf("") }
    var nextDate by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var reminder by remember { mutableStateOf(true) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(18.dp)
    ) {

        item {

            VehicleSelector(store, activeVehicle)

            Spacer(Modifier.height(18.dp))

            Text(
                "Pickerl / TÜV",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(12.dp))

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

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    "Erinnerung 3 Monate vorher",
                    modifier = Modifier.weight(1f)
                )

                Switch(
                    checked = reminder,
                    onCheckedChange = {
                        reminder = it
                    }
                )
            }

            Spacer(Modifier.height(18.dp))

            Button(
                onClick = {
                    if (reminder && nextDate.isNotBlank()) {
                        schedulePickerReminder(
                            context = context,
                            dateText = nextDate,
                            vehicleId = activeVehicle.id
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Notifications, null)
                Spacer(Modifier.width(8.dp))
                Text("Erinnerung speichern")
            }

            Spacer(Modifier.height(15.dp))

            if (nextDate.isNotBlank()) {

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp)
                ) {

                    Column(
                        modifier = Modifier.padding(18.dp)
                    ) {

                        Text(
                            "Nächster Termin",
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            nextDate,
                            fontSize = 24.sp
                        )

                        if (notes.isNotBlank()) {
                            Spacer(Modifier.height(8.dp))
                            Text(notes)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TiresScreen(
    store: Store,
    activeVehicle: Vehicle?
) {

    if (activeVehicle == null) {
        EmptyVehicleMessage()
        return
    }

    var adding by remember { mutableStateOf(false) }

    if (adding) {

        TireForm(
            vehicleId = activeVehicle.id,
            onCancel = {
                adding = false
            },
            onSave = {
                store.saveTires(store.tires + it)
                adding = false
            }
        )

        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(18.dp)
    ) {

        item {

            VehicleSelector(store, activeVehicle)

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = {
                    adding = true
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, null)
                Spacer(Modifier.width(8.dp))
                Text("Reifensatz hinzufügen")
            }

            Spacer(Modifier.height(12.dp))
        }

        items(
            store.tires.filter {
                it.vehicleId == activeVehicle.id
            }
        ) { tire ->

            DataCard(
                title = tire.season.ifBlank {
                    "Reifensatz"
                },
                lines = listOf(
                    "Dimension" to tire.dimension,
                    "Marke" to tire.brand,
                    "DOT" to tire.dot,
                    "Profiltiefe" to tire.tread,
                    "Zustand" to tire.condition,
                    "Lagerung" to tire.storage
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

@Composable
fun TireForm(
    vehicleId: String,
    onCancel: () -> Unit,
    onSave: (TireSet) -> Unit
) {

    var season by remember { mutableStateOf("") }
    var dimension by remember { mutableStateOf("") }
    var brand by remember { mutableStateOf("") }
    var dot by remember { mutableStateOf("") }
    var tread by remember { mutableStateOf("") }
    var condition by remember { mutableStateOf("") }
    var storage by remember { mutableStateOf("") }

    EntryForm(
        title = "Reifensatz",
        fields = listOf(
            "Sommer / Winter" to season,
            "Dimension" to dimension,
            "Marke" to brand,
            "DOT" to dot,
            "Profiltiefe" to tread,
            "Zustand" to condition,
            "Lagerung" to storage
        ),
        setters = listOf(
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
                    vehicleId = vehicleId,
                    season = season,
                    dimension = dimension,
                    brand = brand,
                    dot = dot,
                    tread = tread,
                    condition = condition,
                    storage = storage
                )
            )
        }
    )
}

@Composable
fun OverviewScreen(
    store: Store,
    activeVehicle: Vehicle?
) {

    if (activeVehicle == null) {
        EmptyVehicleMessage()
        return
    }

    val repairs =
        store.repairs.count {
            it.vehicleId == activeVehicle.id
        }

    val maintenance =
        store.maintenances.count {
            it.vehicleId == activeVehicle.id
        }

    val tires =
        store.tires.count {
            it.vehicleId == activeVehicle.id
        }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(18.dp)
    ) {

        item {

            VehicleSelector(store, activeVehicle)

            Spacer(Modifier.height(20.dp))

            Text(
                "Gesamtblick",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(15.dp))

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
        }
    }
}

@Composable
fun OverviewCard(
    title: String,
    value: String
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(20.dp)
    ) {

        Column(
            modifier = Modifier.padding(18.dp)
        ) {

            Text(
                title,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                value,
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun DataCard(
    title: String,
    lines: List<Pair<String, String>>,
    onDelete: () -> Unit
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(20.dp)
    ) {

        Column(
            modifier = Modifier.padding(18.dp)
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Löschen"
                    )
                }
            }

            lines
                .filter { it.second.isNotBlank() }
                .forEach {
                    Text("${it.first}: ${it.second}")
                }
        }
    }
}

@Composable
fun EmptyVehicleMessage() {

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Icon(
                Icons.Default.DirectionsCar,
                contentDescription = null,
                modifier = Modifier.size(60.dp)
            )

            Spacer(Modifier.height(12.dp))

            Text(
                "Bitte zuerst ein Fahrzeug anlegen.",
                fontSize = 18.sp
            )
        }
    }
}

fun createNotificationChannel(context: Context) {

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

        val channel = NotificationChannel(
            "pickerl",
            "Pickerl Erinnerungen",
            NotificationManager.IMPORTANCE_DEFAULT
        )

        context.getSystemService(
            NotificationManager::class.java
        ).createNotificationChannel(channel)
    }
}

fun schedulePickerReminder(
    context: Context,
    dateText: String,
    vehicleId: String
) {

    try {

        val date = LocalDate.parse(
            dateText,
            dateFormatter
        )

        val reminderDate = date.minusMonths(3)

        val trigger =
            reminderDate
                .atStartOfDay(
                    java.time.ZoneId.systemDefault()
                )
                .toInstant()
                .toEpochMilli()

        val intent =
            Intent(
                context,
                PickerlReceiver::class.java
            ).apply {
                putExtra("vehicleId", vehicleId)
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

    } catch (_: Exception) {
        // Ungültiges Datum wird später über die UI behandelt.
    }
}

class PickerlReceiver : BroadcastReceiver() {

    override fun onReceive(
        context: Context,
        intent: Intent
    ) {

        val notification =
            NotificationCompat.Builder(
                context,
                "pickerl"
            )
                .setSmallIcon(
                    android.R.drawable.ic_dialog_info
                )
                .setContentTitle(
                    "AutoCheck – Pickerl Erinnerung"
                )
                .setContentText(
                    "Dein Pickerl / TÜV ist in 3 Monaten fällig."
                )
                .setPriority(
                    NotificationCompat.PRIORITY_DEFAULT
                )
                .setAutoCancel(true)
                .build()

        val manager =
            context.getSystemService(
                NotificationManager::class.java
            )

        manager.notify(
            intent.getStringExtra("vehicleId")
                ?.hashCode()
                ?: 1001,
            notification
        )
    }
}
