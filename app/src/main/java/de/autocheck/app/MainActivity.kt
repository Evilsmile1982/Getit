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

                           
