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
fun HomeScreen(
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
