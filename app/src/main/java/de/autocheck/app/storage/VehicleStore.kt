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
