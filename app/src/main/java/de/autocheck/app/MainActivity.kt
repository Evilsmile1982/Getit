package de.autocheck.app

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
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
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CarRepair
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.TireRepair
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Brush
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

private val Background = Color(0xFF050608)
private val Surface = Color(0xFF11151C)
private val Muted = Color(0xFF9AA3B2)

data class Vehicle(
    val name: String,
    val make: String,
    val model: String,
    val year: String,
    val imageUri: String = ""
)

class VehicleStore(context: Context) {

    private val prefs =
        context.getSharedPreferences("autocheck", Context.MODE_PRIVATE)

    fun load(): List<Vehicle> {
        val array =
            JSONArray(prefs.getString("vehicles", "[]") ?: "[]")

        return buildList {
            for (i in 0 until array.length()) {
                val o = array.getJSONObject(i)

                add(
                    Vehicle(
                        o.optString("name"),
                        o.optString("make"),
                        o.optString("model"),
                        o.optString("year"),
                        o.optString("imageUri")
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
                    put("imageUri", vehicle.imageUri)
                }
            )
        }

        prefs.edit()
            .putString("vehicles", array.toString())
            .apply()
    }
}

private class VisitedStore(context: Context) {

    private val prefs =
        context.getSharedPreferences(
            "autocheck_visited",
            Context.MODE_PRIVATE
        )

    fun isVisited(screen: Screen): Boolean =
        prefs.getBoolean(screen.name, false)

    fun markVisited(screen: Screen) {

        prefs.edit()
            .putBoolean(screen.name, true)
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
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val accent: Color,
    val screen: Screen
)

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContent {

            AutoCheckTheme {
                AutoCheckApp()
            }
        }
    }
}

@Composable
private fun AutoCheckApp() {

    val context = LocalContext.current

    val vehicleStore =
        remember {
            VehicleStore(context)
        }

    val visitedStore =
        remember {
            VisitedStore(context)
        }

    val vehicles =
        remember {

            mutableStateListOf<Vehicle>().apply {

                addAll(
                    vehicleStore.load()
                )
            }
        }

    var screen by remember {
        mutableStateOf(Screen.HOME)
    }

    var homeReady by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(Unit) {

        kotlinx.coroutines.delay(300)

        homeReady = true
    }

    if (screen == Screen.HOME) {

        HomeScreen(
            visible = homeReady,
            visitedStore = visitedStore,
            onSelect = {
                screen = it
            }
        )

    } else {

        val title = when (screen) {

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
            title = title,
            onBack = {
                screen = Screen.HOME
            }
        ) {

            when (screen) {

                Screen.AUTO -> {

                    VehicleScreen(

                        vehicles = vehicles,

                        onAdd = {

                            if (vehicles.size < 5) {

                                vehicles.add(it)

                                vehicleStore.save(
                                    vehicles
                                )
                            }
                        },

                        onDelete = {

                            vehicles.remove(it)

                            vehicleStore.save(
                                vehicles
                            )
                        }
                    )
                }

                else -> {

                    InfoScreen(

                        screen = screen,

                        title = title,

                        visited =
                            visitedStore.isVisited(
                                screen
                            ),

                        onVisited = {

                            visitedStore.markVisited(
                                screen
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeScreen(
    visible: Boolean,
    visitedStore: VisitedStore,
    onSelect: (Screen) -> Unit
) {

    val items = listOf(

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
                .background(Background),

        horizontalAlignment =
            Alignment.CenterHorizontally,

        contentPadding =
            androidx.compose.foundation.layout.PaddingValues(
                start = 14.dp,
                end = 14.dp,
                top = 22.dp,
                bottom = 24.dp
            ),

        verticalArrangement =
            Arrangement.spacedBy(14.dp)
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
                        .height(72.dp),

                contentScale =
                    ContentScale.Fit
            )
        }

        item {

            Card(

                modifier =
                    Modifier.fillMaxWidth(),

                shape =
                    RoundedCornerShape(22.dp),

                colors =
                    CardDefaults.cardColors(
                        containerColor = Color.Black
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
                            .height(245.dp),

                    contentScale =
                        ContentScale.Crop
                )
            }
        }

        items(
            items.chunked(2)
        ) { row ->

            Row(

                modifier =
                    Modifier.fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.spacedBy(10.dp)
            ) {

                row.forEach { item ->

                    AnimatedVisibility(

                        visible = visible,

                        modifier =
                            Modifier.weight(1f),

                        enter =
                            slideInHorizontally(

                                animationSpec =
                                    tween(2500),

                                initialOffsetX = { fullWidth ->

                                    if (
                                        row.indexOf(item) == 0
                                    ) {
                                        -fullWidth
                                    } else {
                                        fullWidth
                                    }
                                }
                            )
                    ) {

                        MenuCard(

                            item = item,

                            visited =
                                visitedStore.isVisited(
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

                if (row.size == 1) {

                    Spacer(
                        Modifier.weight(1f)
                    )
                }
            }
        }

        item {

            Text(

                "AutoCheck • Verliere nicht die Übersicht",

                color = Muted,

                fontSize = 12.sp,

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
                .height(104.dp)
                .clickable(
                    onClick = onClick
                ),

        shape =
            RoundedCornerShape(17.dp),

        colors =
            CardDefaults.cardColors(
                containerColor = Surface
            ),

        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 5.dp
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
                                    alpha = 0.20f
                                ),

                                Color.Transparent
                            )
                        )
                    )
                    .padding(11.dp),

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Box(

                modifier =
                    Modifier
                        .size(52.dp)
                        .clip(
                            RoundedCornerShape(14.dp)
                        )
                        .background(
                            item.accent.copy(
                                alpha = 0.90f
                            )
                        ),

                contentAlignment =
                    Alignment.Center
            ) {

                Icon(

                    item.icon,

                    item.title,

                    tint = Color.White,

                    modifier =
                        Modifier.size(29.dp)
                )
            }

            Spacer(
                Modifier.width(10.dp)
            )

            Column(
                modifier =
                    Modifier.weight(1f)
            ) {

                Text(

                    item.title,

                    color = Color.White,

                    fontSize = 15.sp,

                    fontWeight =
                        FontWeight.Bold
                )

                Text(

                    item.subtitle,

                    color = Muted,

                    fontSize = 10.sp,

                    lineHeight = 12.sp,

                    maxLines = 2
                )

                if (visited) {

                    Row(
                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {

                        Icon(

                            Icons.Filled.CheckCircle,

                            contentDescription = null,

                            tint = item.accent,

                            modifier =
                                Modifier.size(13.dp)
                        )

                        Spacer(
                            Modifier.width(3.dp)
                        )

                        Text(

                            "Ich war hier",

                            color = item.accent,

                            fontSize = 9.sp
                        )
                    }
                }
            }

            Icon(

                Icons.Filled.KeyboardArrowRight,

                contentDescription = "Öffnen",

                tint = item.accent,

                modifier =
                    Modifier.size(24.dp)
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
            Background,

        topBar = {

            TopAppBar(

                title = {

                    Text(

                        title,

                        color = Color.White,

                        fontWeight =
                            FontWeight.Bold
                    )
                },

                navigationIcon = {

                    IconButton(
                        onClick = onBack
                    ) {

                        Icon(

                            Icons.Filled.ArrowBack,

                            "Zurück",

                            tint = Color.White
                        )
                    }
                },

                colors =
                    TopAppBarDefaults.topAppBarColors(

                        containerColor =
                            Background,

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
                    .padding(
                        horizontal = 16.dp
                    )
        ) {

            content()
        }
    }
}

@Composable
private fun VehicleScreen(
    vehicles: List<Vehicle>,
    onAdd: (Vehicle) -> Unit,
    onDelete: (Vehicle) -> Unit
) {

    var adding by remember {
        mutableStateOf(false)
    }

    var name by remember {
        mutableStateOf("")
    }

    var make by remember {
        mutableStateOf("")
    }

    var model by remember {
        mutableStateOf("")
    }

    var year by remember {
        mutableStateOf("")
    }

    var selectedImageUri by remember {
        mutableStateOf("")
    }

    val picker =
        androidx.activity.compose.rememberLauncherForActivityResult(

            ActivityResultContracts.GetContent()

        ) { uri: Uri? ->

            selectedImageUri =
                uri?.toString() ?: ""
        }

    Column(
        modifier =
            Modifier.fillMaxSize()
    ) {

        Text(

            "Bis zu 5 Fahrzeuge • eigene Bilder nur in diesem Bereich",

            color = Muted,

            fontSize = 12.sp,

            modifier =
                Modifier.padding(
                    bottom = 12.dp
                )
        )

        LazyColumn(

            modifier =
                Modifier.weight(1f),

            verticalArrangement =
                Arrangement.spacedBy(10.dp)
        ) {

            items(vehicles) { vehicle ->

                VehicleCard(

                    vehicle = vehicle,

                    onDelete = {
                        onDelete(vehicle)
                    }
                )
            }

            if (adding) {

                item {

                    Card(

                        modifier =
                            Modifier.fillMaxWidth(),

                        shape =
                            RoundedCornerShape(18.dp),

                        colors =
                            CardDefaults.cardColors(
                                containerColor =
                                    Surface
                            )
                    ) {

                        Column(

                            modifier =
                                Modifier.padding(16.dp),

                            verticalArrangement =
                                Arrangement.spacedBy(9.dp)
                        ) {

                            Text(

                                "Fahrzeug hinzufügen",

                                color = Color.White,

                                fontWeight =
                                    FontWeight.Bold
                            )

                            OutlinedTextField(

                                value = name,

                                onValueChange = {
                                    name = it
                                },

                                modifier =
                                    Modifier.fillMaxWidth(),

                                label = {
                                    Text("Bezeichnung")
                                },

                                singleLine = true
                            )

                            OutlinedTextField(

                                value = make,

                                onValueChange = {
                                    make = it
                                },

                                modifier =
                                    Modifier.fillMaxWidth(),

                                label = {
                                    Text("Marke")
                                },

                                singleLine = true
                            )

                            OutlinedTextField(

                                value = model,

                                onValueChange = {
                                    model = it
                                },

                                modifier =
                                    Modifier.fillMaxWidth(),

                                label = {
                                    Text("Modell")
                                },

                                singleLine = true
                            )

                            OutlinedTextField(

                                value = year,

                                onValueChange = {
                                    year = it
                                },

                                modifier =
                                    Modifier.fillMaxWidth(),

                                label = {
                                    Text("Baujahr")
                                },

                                singleLine = true
                            )

                            OutlinedButton(

                                onClick = {
                                    picker.launch(
                                        "image/*"
                                    )
                                },

                                modifier =
                                    Modifier.fillMaxWidth()
                            ) {

                                Text(

                                    if (
                                        selectedImageUri.isBlank()
                                    ) {
                                        "Eigenes Fahrzeugbild wählen"
                                    } else {
                                        "Fahrzeugbild ausgewählt"
                                    }
                                )
                            }

                            Row(

                                horizontalArrangement =
                                    Arrangement.spacedBy(8.dp)
                            ) {

                                Button(

                                    enabled =
                                        name.isNotBlank(),

                                    onClick = {

                                        onAdd(

                                            Vehicle(

                                                name.trim(),

                                                make.trim(),

                                                model.trim(),

                                                year.trim(),

                                                selectedImageUri
                                            )
                                        )

                                        name = ""
                                        make = ""
                                        model = ""
                                        year = ""
                                        selectedImageUri = ""
                                        adding = false
                                    }

                                ) {

                                    Text("Speichern")
                                }

                                OutlinedButton(

                                    onClick = {
                                        adding = false
                                    }

                                ) {

                                    Text("Abbrechen")
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

                onClick = {
                    adding = true
                },

                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(
                            vertical = 12.dp
                        )
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
    onDelete: () -> Unit
) {

    val context =
        LocalContext.current

    val bitmap =
        remember(vehicle.imageUri) {

            if (
                vehicle.imageUri.isBlank()
            ) {
                null
            } else {

                runCatching {

                    context
                        .contentResolver
                        .openInputStream(
                            Uri.parse(
                                vehicle.imageUri
                            )
                        )
                        .use { stream ->

                            stream
                                ?.let {
                                    BitmapFactory
                                        .decodeStream(it)
                                        ?.asImageBitmap()
                                }
                        }

                }.getOrNull()
            }
        }

    Card(

        modifier =
            Modifier.fillMaxWidth(),

        shape =
            RoundedCornerShape(18.dp),

        colors =
            CardDefaults.cardColors(
                containerColor =
                    Surface
            )
    ) {

        Row(

            modifier =
                Modifier.padding(12.dp),

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            if (bitmap != null) {

                Image(

                    bitmap = bitmap,

                    contentDescription =
                        vehicle.name,

                    modifier =
                        Modifier
                            .size(82.dp)
                            .clip(
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
                        Modifier
                            .size(82.dp)
                            .clip(
                                RoundedCornerShape(
                                    14.dp
                                )
                            )
                            .background(
                                Color(0xFF1B2028)
                            ),

                    contentAlignment =
                        Alignment.Center
                ) {

                    Icon(

                        Icons.Filled.DirectionsCar,

                        contentDescription =
                            null,

                        tint =
                            Color(0xFFE51B2A),

                        modifier =
                            Modifier.size(40.dp)
                    )
                }
            }

            Spacer(
                Modifier.width(12.dp)
            )

            Column(
                modifier =
                    Modifier.weight(1f)
            ) {

                Text(

                    vehicle.name,

                    color = Color.White,

                    fontWeight =
                        FontWeight.Bold,

                    fontSize = 17.sp
                )

                Text(

                    listOf(

                        vehicle.make,

                        vehicle.model,

                        vehicle.year

                    )
                        .filter {
                            it.isNotBlank()
                        }
                        .joinToString(" • "),

                    color = Muted,

                    fontSize = 12.sp
                )

                Spacer(
                    Modifier.height(6.dp)
                )

                OutlinedButton(
                    onClick = onDelete
                ) {

                    Text("Entfernen")
                }
            }
        }
    }
}

@Composable
private fun InfoScreen(
    screen: Screen,
    title: String,
    visited: Boolean,
    onVisited: () -> Unit
) {

    val description =
        when (screen) {

            Screen.REPARATUREN ->
                "Reparaturen, Kosten, Datum und Kilometerstand sauber dokumentieren."

            Screen.PICKERL ->
                "Pickerl-/TÜV-Termine, Fristen und Erinnerungen im Blick behalten."

            Screen.WARTUNGEN ->
                "Öl, Filter, Bremsen und andere Wartungsarbeiten dokumentieren."

            Screen.GESAMTBLICK ->
                "Eine zentrale Übersicht über offene Punkte und wichtige Fahrzeugdaten."

            Screen.REIFEN ->
                "Sommer-/Winterreifen, Dimensionen, Alter und Zustand verwalten."

            else -> ""
        }

    val accent =
        when (screen) {

            Screen.REPARATUREN ->
                Color(0xFF1688E8)

            Screen.PICKERL ->
                Color(0xFF20C75A)

            Screen.WARTUNGEN ->
                Color(0xFFE59A18)

            Screen.GESAMTBLICK ->
                Color(0xFF9A4DFF)

            Screen.REIFEN ->
                Color(0xFF17C8BD)

            else ->
                Color(0xFFE51B2A)
        }

    Column(

        modifier =
            Modifier.fillMaxSize(),

        verticalArrangement =
            Arrangement.spacedBy(14.dp)
    ) {

        Card(

            modifier =
                Modifier.fillMaxWidth(),

            shape =
                RoundedCornerShape(20.dp),

            colors =
                CardDefaults.cardColors(
                    containerColor =
                        Surface
                )
        ) {

            Column(

                modifier =
                    Modifier
                        .fillMaxWidth()
                        .background(

                            Brush.linearGradient(

                                listOf(

                                    accent.copy(
                                        alpha = 0.24f
                                    ),

                                    Color.Transparent
                                )
                            )
                        )
                        .padding(20.dp)
            ) {

                Text(

                    title,

                    color = Color.White,

                    fontSize = 25.sp,

                    fontWeight =
                        FontWeight.Bold
                )

                Spacer(
                    Modifier.height(10.dp)
                )

                Text(

                    description,

                    color = Muted,

                    fontSize = 15.sp,

                    lineHeight = 21.sp
                )

                Spacer(
                    Modifier.height(20.dp)
                )

                Text(

                    "Hier entsteht die vollständige Dokumentation für diesen Bereich. Die Oberfläche bleibt lokal auf dem Gerät.",

                    color = Color.White,

                    fontSize = 14.sp,

                    lineHeight = 20.sp
                )
            }
        }

        Button(

            onClick = onVisited,

            modifier =
                Modifier.fillMaxWidth()
        ) {

            Icon(

                Icons.Filled.CheckCircle,

                contentDescription = null
            )

            Spacer(
                Modifier.width(8.dp)
            )

            Text(

                if (visited) {
                    "Ich war hier ✓"
                } else {
                    "Ich war hier"
                }
            )
        }
    }
}
