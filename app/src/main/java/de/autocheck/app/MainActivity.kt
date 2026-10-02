package de.autocheck.app

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CarRepair
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TireRepair
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.autocheck.app.ui.AutoCheckTheme
import org.json.JSONArray
import org.json.JSONObject

data class Vehicle(
    val name: String,
    val make: String,
    val model: String,
    val year: String
)

class VehicleStore(context: Context) {
    private val prefs = context.getSharedPreferences("autocheck", Context.MODE_PRIVATE)

    fun load(): List<Vehicle> {
        val raw = prefs.getString("vehicles", "[]") ?: "[]"
        val array = JSONArray(raw)
        return buildList {
            for (i in 0 until array.length()) {
                val o = array.getJSONObject(i)
                add(
                    Vehicle(
                        o.optString("name"),
                        o.optString("make"),
                        o.optString("model"),
                        o.optString("year")
                    )
                )
            }
        }
    }

    fun save(list: List<Vehicle>) {
        val array = JSONArray()
        list.forEach {
            array.put(JSONObject().apply {
                put("name", it.name)
                put("make", it.make)
                put("model", it.model)
                put("year", it.year)
            })
        }
        prefs.edit().putString("vehicles", array.toString()).apply()
    }
}

private enum class Screen {
    HOME, AUTO, REPARATUREN, PICKERL, WARTUNGEN, GESAMTBLICK, REIFEN
}

private data class MenuItemData(
    val title: String,
    val subtitle: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val screen: Screen
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { AutoCheckTheme { AutoCheckApp() } }
    }
}

@Composable
private fun AutoCheckApp() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val store = remember { VehicleStore(context) }
    val vehicles = remember { mutableStateListOf<Vehicle>().apply { addAll(store.load()) } }
    var screen by remember { mutableStateOf(Screen.HOME) }
    var homeReady by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(2500)
        homeReady = true
    }

    if (screen == Screen.HOME) {
        HomeScreen(
            visible = homeReady,
            onSelect = { screen = it }
        )
    } else {
        DetailScaffold(
            title = when (screen) {
                Screen.AUTO -> "Mein Auto"
                Screen.REPARATUREN -> "Reparaturen"
                Screen.PICKERL -> "Pickerl / TÜV"
                Screen.WARTUNGEN -> "Wartungen"
                Screen.GESAMTBLICK -> "Gesamtblick"
                Screen.REIFEN -> "Reifen"
                else -> "AutoCheck"
            },
            onBack = { screen = Screen.HOME }
        ) {
            when (screen) {
                Screen.AUTO -> VehicleScreen(
                    vehicles = vehicles,
                    onAdd = { vehicle ->
                        if (vehicles.size < 5) {
                            vehicles.add(vehicle)
                            store.save(vehicles)
                        }
                    },
                    onDelete = { vehicle ->
                        vehicles.remove(vehicle)
                        store.save(vehicles)
                    }
                )
                Screen.REPARATUREN -> InfoScreen(
                    "Reparaturen",
                    "Hier werden Reparaturen, Kosten, Datum und Kilometerstand gesammelt."
                )
                Screen.PICKERL -> InfoScreen(
                    "Pickerl / TÜV",
                    "Fristen und Termine für die nächste §57a-Überprüfung bzw. TÜV-Prüfung."
                )
                Screen.WARTUNGEN -> InfoScreen(
                    "Wartungen",
                    "Öl, Filter, Bremsen und weitere Wartungsarbeiten übersichtlich erfassen."
                )
                Screen.GESAMTBLICK -> InfoScreen(
                    "Gesamtblick",
                    "Eine kompakte Übersicht der wichtigsten Fahrzeugdaten und offenen Punkte."
                )
                Screen.REIFEN -> InfoScreen(
                    "Reifen",
                    "Reifengröße, Dimensionen, Alter und saisonale Informationen verwalten."
                )
                else -> Unit
            }
        }
    }
}

@Composable
private fun HomeScreen(
    visible: Boolean,
    onSelect: (Screen) -> Unit
) {
    val items = listOf(
        MenuItemData("Mein Auto", "Fahrzeug & Details", Icons.Filled.DirectionsCar, Screen.AUTO),
        MenuItemData("Reparaturen", "Reparaturen verwalten", Icons.Filled.CarRepair, Screen.REPARATUREN),
        MenuItemData("Pickerl/TÜV", "Termine & Fristen", Icons.Filled.Event, Screen.PICKERL),
        MenuItemData("Wartungen", "Verschiedenes", Icons.Filled.Build, Screen.WARTUNGEN),
        MenuItemData("Gesamtblick", "Die wichtigsten Infos", Icons.Filled.Visibility, Screen.GESAMTBLICK),
        MenuItemData("Reifen", "Größen, Dimensionen und Alter", Icons.Filled.TireRepair, Screen.REIFEN)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF050608))
            .padding(horizontal = 14.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(28.dp))
            Text(
                "AutoCheck",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(Modifier.height(8.dp))

            Image(
                painter = painterResource(R.drawable.bild_3),
                contentDescription = "AutoCheck Hauptbild",
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentScale = ContentScale.Fit
            )

            Spacer(Modifier.height(10.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                for (row in 0..2) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        for (col in 0..1) {
                            val item = items[row * 2 + col]
                            AnimatedVisibility(
                                visible = visible,
                                enter = slideInHorizontally(
                                    animationSpec = tween(2500),
                                    initialOffsetX = { if (col == 0) -it else it }
                                )
                            ) {
                                MenuCard(
                                    item = item,
                                    modifier = Modifier.weight(1f),
                                    onClick = { onSelect(item.screen) }
                                )
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
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
        modifier = modifier,
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF11141A))
    ) {
        Row(
            modifier = Modifier.padding(13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFE21D32)),
                contentAlignment = Alignment.Center
            ) {
                Icon(item.icon, item.title, tint = Color.White)
            }
            Spacer(Modifier.width(10.dp))
            Column {
                Text(item.title, fontWeight = FontWeight.Bold, color = Color.White)
                Text(
                    item.subtitle,
                    fontSize = 11.sp,
                    color = Color(0xFFB8BEC8),
                    maxLines = 2
                )
            }
        }
    }
}

@Composable
private fun DetailScaffold(
    title: String,
    onBack: () -> Unit,
    content: @Composable () -> Unit
) {
    Scaffold(
        containerColor = Color(0xFF050608),
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.Menu, "Zurück")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF050608),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
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
    var adding by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var make by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            "${vehicles.size}/5 Fahrzeuge",
            color = Color(0xFFB8BEC8),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (vehicles.isEmpty()) {
            Text(
                "Noch kein Fahrzeug angelegt.",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(vehicles) { vehicle ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF11141A)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(vehicle.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(
                            "${vehicle.make} ${vehicle.model} ${vehicle.year}".trim(),
                            color = Color(0xFFB8BEC8)
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedButton(onClick = { onDelete(vehicle) }) {
                            Text("Fahrzeug entfernen")
                        }
                    }
                }
            }

            if (adding) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF11141A)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("Fahrzeug hinzufügen", color = Color.White, fontWeight = FontWeight.Bold)
                            OutlinedTextField(name, { name = it }, label = { Text("Bezeichnung") })
                            OutlinedTextField(make, { make = it }, label = { Text("Marke") })
                            OutlinedTextField(model, { model = it }, label = { Text("Modell") })
                            OutlinedTextField(year, { year = it }, label = { Text("Baujahr") })
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    enabled = name.isNotBlank(),
                                    onClick = {
                                        onAdd(Vehicle(name.trim(), make.trim(), model.trim(), year.trim()))
                                        name = ""; make = ""; model = ""; year = ""
                                        adding = false
                                    }
                                ) { Text("Speichern") }
                                OutlinedButton(onClick = { adding = false }) { Text("Abbrechen") }
                            }
                        }
                    }
                }
            }
        }

        if (!adding && vehicles.size < 5) {
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = { adding = true }
            ) {
                Text("Fahrzeug hinzufügen")
            }
        }
    }
}

@Composable
private fun InfoScreen(title: String, text: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Top
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF11141A)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(20.dp)) {
                Text(title, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(10.dp))
                Text(text, color = Color(0xFFB8BEC8), fontSize = 16.sp)
                Spacer(Modifier.height(18.dp))
                Text(
                    "Dieser Bereich ist vorbereitet und kann im nächsten Entwicklungsschritt mit den gewünschten Eingabefeldern und Erinnerungen ausgebaut werden.",
                    color = Color.White
                )
            }
        }
    }
}
