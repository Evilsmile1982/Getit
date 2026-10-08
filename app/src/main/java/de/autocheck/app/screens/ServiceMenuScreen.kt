package de.autocheck.app

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.autocheck.app.data.ServiceInterval

@Composable
fun ServiceMenuScreen(
    store: VehicleStore,
    vehicles: List<Vehicle>,
    activeVehicle: String,
    activeVehicleData: Vehicle?,
    onActiveVehicle: (String) -> Unit,
    onVisited: () -> Unit
) {

    /*
     * ============================================================
     * SERVICE-BEREICH
     * ============================================================
     *
     * MENU      = Startseite mit Fahrzeugdaten + zwei Auswahlkarten
     * INTERVALS  = bestehender Bereich "Service und Intervalle"
     * HISTORY    = bestehender Bereich "Service Historie"
     */
    var section =
        remember {
            androidx.compose.runtime.mutableStateOf(
                ServiceMenuSection.MENU
            )
        }

    when (section.value) {

        /*
         * ========================================================
         * HAUPTMENÜ
         * ========================================================
         */
        ServiceMenuSection.MENU -> {

            ServiceMenuHome(

                vehicle =
                    activeVehicleData,

                onServiceAndIntervals = {
                    section.value =
                        ServiceMenuSection.INTERVALS
                },

                onServiceHistory = {
                    section.value =
                        ServiceMenuSection.HISTORY
                }
            )
        }

        /*
         * ========================================================
         * SERVICE UND INTERVALLE
         * ========================================================
         */
        ServiceMenuSection.INTERVALS -> {

            Column(
                modifier =
                    Modifier.fillMaxSize()
            ) {

                ServiceSubPageHeader(
                    title =
                        "Service und Intervalle",

                    onBack = {
                        section.value =
                            ServiceMenuSection.MENU
                    }
                )

                ServiceScreen(

                    store =
                        store,

                    vehicles =
                        vehicles,

                    activeVehicle =
                        activeVehicle,

                    onActiveVehicle =
                        onActiveVehicle,

                    onVisited =
                        onVisited
                )
            }
        }

        /*
         * ========================================================
         * SERVICE HISTORIE
         * ========================================================
         *
         * Die bestehende ServiceScreen enthält bereits die
         * komplette Historie und das separate Historien-PDF.
         *
         * Für den ersten Schritt verwenden wir bewusst diese
         * stabile bestehende Funktionalität weiter.
         */
        ServiceMenuSection.HISTORY -> {

            Column(
                modifier =
                    Modifier.fillMaxSize()
            ) {

                ServiceSubPageHeader(
                    title =
                        "Service Historie",

                    onBack = {
                        section.value =
                            ServiceMenuSection.MENU
                    }
                )

                ServiceScreen(

                    store =
                        store,

                    vehicles =
                        vehicles,

                    activeVehicle =
                        activeVehicle,

                    onActiveVehicle =
                        onActiveVehicle,

                    onVisited =
                        onVisited
                )
            }
        }
    }
}

/*
 * ================================================================
 * SERVICE-BEREICH
 * ================================================================
 */
private enum class ServiceMenuSection {
    MENU,
    INTERVALS,
    HISTORY
}

/*
 * ================================================================
 * SERVICE-HAUPTMENÜ
 * ================================================================
 */
@Composable
private fun ServiceMenuHome(
    vehicle: Vehicle?,
    onServiceAndIntervals: () -> Unit,
    onServiceHistory: () -> Unit
) {

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(
                    horizontal = 16.dp
                )
    ) {

        Spacer(
            modifier =
                Modifier.height(
                    8.dp
                )
        )

        /*
         * ========================================================
         * FAHRZEUGDATEN + FAHRZEUGBILD
         * ========================================================
         */
        VehicleServiceHeader(
            vehicle =
                vehicle
        )

        Spacer(
            modifier =
                Modifier.height(
                    16.dp
                )
        )

        /*
         * ========================================================
         * SERVICE UND INTERVALLE
         * ========================================================
         */
        ServiceMenuButton(

            title =
                "🔧  Service und Intervalle",

            subtitle =
                "Aktuelles Service, nächstes Service, Dokumentation und Erinnerungen",

            onClick =
                onServiceAndIntervals
        )

        Spacer(
            modifier =
                Modifier.height(
                    12.dp
                )
        )

        /*
         * ========================================================
         * SERVICE HISTORIE
         * ========================================================
         */
        ServiceMenuButton(

            title =
                "📋  Service Historie",

            subtitle =
                "Vergangene Services, Bearbeiten, Löschen und Historien-PDF",

            onClick =
                onServiceHistory
        )
    }
}

/*
 * ================================================================
 * FAHRZEUGDATEN / BILD
 * ================================================================
 */
@Composable
private fun VehicleServiceHeader(
    vehicle: Vehicle?
) {

    Card(
        modifier =
            Modifier.fillMaxWidth(),

        shape =
            RoundedCornerShape(
                18.dp
            ),

        colors =
            CardDefaults.cardColors(
                containerColor =
                    Color(
                        0xFF11141A
                    )
            )
    ) {

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(
                        210.dp
                    )
                    .padding(
                        12.dp
                    ),

            horizontalArrangement =
                Arrangement.spacedBy(
                    12.dp
                )
        ) {

            /*
             * ====================================================
             * LINKE HÄLFTE – AUTODATEN
             * ====================================================
             */
            Column(
                modifier =
                    Modifier
                        .weight(
                            1f
                        )
                        .fillMaxHeight(),

                verticalArrangement =
                    Arrangement.Center
            ) {

                Text(
                    text =
                        "AUTODATEN",

                    color =
                        Color(
                            0xFF9A4DFF
                        ),

                    fontSize =
                        14.sp,

                    fontWeight =
                        FontWeight.Bold
                )

                Spacer(
                    modifier =
                        Modifier.height(
                            8.dp
                        )
                )

                if (
                    vehicle == null
                ) {

                    Text(
                        text =
                            "Kein Fahrzeug ausgewählt",

                        color =
                            Color.White,

                        fontSize =
                            16.sp,

                        fontWeight =
                            FontWeight.Bold
                    )

                } else {

                    Text(
                        text =
                            vehicle.make.ifBlank {
                                "Unbekannte Marke"
                            },

                        color =
                            Color.White,

                        fontSize =
                            20.sp,

                        fontWeight =
                            FontWeight.Bold,

                        maxLines =
                            1,

                        overflow =
                            TextOverflow.Ellipsis
                    )

                    Text(
                        text =
                            vehicle.model.ifBlank {
                                "Unbekanntes Modell"
                            },

                        color =
                            Color.LightGray,

                        fontSize =
                            16.sp,

                        maxLines =
                            1,

                        overflow =
                            TextOverflow.Ellipsis
                    )

                    Spacer(
                        modifier =
                            Modifier.height(
                                8.dp
                            )
                    )

                    ServiceVehicleDataLine(
                        label =
                            "Baujahr",

                        value =
                            vehicle.year
                                .ifBlank {
                                    "—"
                                }
                    )

                    ServiceVehicleDataLine(
                        label =
                            "Kennzeichen",

                        value =
                            vehicle.plate
                                .ifBlank {
                                    "—"
                                }
                    )

                    ServiceVehicleDataLine(
                        label =
                            "Fahrzeug",

                        value =
                            vehicle.vehicleType
                                .ifBlank {
                                    "PKW"
                                }
                    )
                }
            }

            /*
             * ====================================================
             * RECHTE HÄLFTE – FAHRZEUGBILD
             * ====================================================
             */
            Box(
                modifier =
                    Modifier
                        .weight(
                            1f
                        )
                        .fillMaxHeight()
                        .clip(
                            RoundedCornerShape(
                                14.dp
                            )
                        )
                        .background(
                            Color(
                                0xFF1B1F26
                            )
                        ),

                contentAlignment =
                    Alignment.Center
            ) {

                if (
                    vehicle != null &&
                    vehicle.imageUri.isNotBlank()
                ) {

                    VehicleServiceImage(
                        imageUri =
                            vehicle.imageUri
                    )

                } else {

                    Column(
                        horizontalAlignment =
                            Alignment.CenterHorizontally
                    ) {

                        Text(
                            text =
                                "🚗",

                            fontSize =
                                52.sp
                        )

                        Spacer(
                            modifier =
                                Modifier.height(
                                    6.dp
                                )
                        )

                        Text(
                            text =
                                "Kein Fahrzeugbild",

                            color =
                                Color.LightGray,

                            fontSize =
                                13.sp
                        )
                    }
                }
            }
        }
    }
}

/*
 * ================================================================
 * FAHRZEUGDATEN-ZEILE
 * ================================================================
 */
@Composable
private fun ServiceVehicleDataLine(
    label: String,
    value: String
) {

    Row(
        modifier =
            Modifier.fillMaxWidth(),

        horizontalArrangement =
            Arrangement.spacedBy(
                6.dp
            )
    ) {

        Text(
            text =
                "$label:",

            color =
                Color.Gray,

            fontSize =
                12.sp
        )

        Text(
            modifier =
                Modifier.weight(
                    1f
                ),

            text =
                value,

            color =
                Color.White,

            fontSize =
                12.sp,

            maxLines =
                1,

            overflow =
                TextOverflow.Ellipsis
        )
    }
}

/*
 * ================================================================
 * FAHRZEUGBILD
 * ================================================================
 *
 * Wir verwenden absichtlich keine zusätzliche Bildbibliothek.
 *
 * Das bereits im Fahrzeug gespeicherte imageUri wird direkt über
 * den Android ContentResolver geladen.
 */
@Composable
private fun VehicleServiceImage(
    imageUri: String
) {

    val context =
        LocalContext.current

    val bitmap =
        remember(
            imageUri
        ) {

            try {

                context
                    .contentResolver
                    .openInputStream(
                        Uri.parse(
                            imageUri
                        )
                    )
                    ?.use {
                        BitmapFactory
                            .decodeStream(
                                it
                            )
                    }

            } catch (
                _: Exception
            ) {

                null
            }
        }

    if (
        bitmap != null
    ) {

        Image(
            bitmap =
                bitmap.asImageBitmap(),

            contentDescription =
                "Fahrzeugbild",

            modifier =
                Modifier.fillMaxSize(),

            contentScale =
                ContentScale.Crop
        )

    } else {

        Column(
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            Text(
                text =
                    "🚗",

                fontSize =
                    52.sp
            )

            Spacer(
                modifier =
                    Modifier.height(
                        6.dp
                    )
            )

            Text(
                text =
                    "Bild konnte nicht geladen werden",

                color =
                    Color.LightGray,

                fontSize =
                    12.sp
            )
        }
    }
}

/*
 * ================================================================
 * GROSSER SERVICE-MENÜ-BUTTON
 * ================================================================
 */
@Composable
private fun ServiceMenuButton(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {

    Button(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(
                    92.dp
                ),

        onClick =
            onClick,

        shape =
            RoundedCornerShape(
                18.dp
            ),

        colors =
            ButtonDefaults.buttonColors(
                containerColor =
                    Color(
                        0xFF9A4DFF
                    ),

                contentColor =
                    Color.White
            )
    ) {

        Column(
            modifier =
                Modifier.fillMaxWidth(),

            horizontalAlignment =
                Alignment.Start,

            verticalArrangement =
                Arrangement.Center
        ) {

            Text(
                text =
                    title,

                fontSize =
                    18.sp,

                fontWeight =
                    FontWeight.Bold
            )

            Spacer(
                modifier =
                    Modifier.height(
                        4.dp
                    )
            )

            Text(
                text =
                    subtitle,

                fontSize =
                    12.sp,

                color =
                    Color.White.copy(
                        alpha = 0.85f
                    ),

                maxLines =
                    2,

                overflow =
                    TextOverflow.Ellipsis
            )
        }
    }
}

/*
 * ================================================================
 * UNTERSEITEN-KOPF
 * ================================================================
 */
@Composable
private fun ServiceSubPageHeader(
    title: String,
    onBack: () -> Unit
) {

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 16.dp,
                    vertical = 4.dp
                ),

        verticalAlignment =
            Alignment.CenterVertically,

        horizontalArrangement =
            Arrangement.spacedBy(
                8.dp
            )
    ) {

        OutlinedButton(
            onClick =
                onBack
        ) {

            Text(
                "← Zurück"
            )
        }

        Text(
            text =
                title,

            modifier =
                Modifier.weight(
                    1f
                ),

            color =
                Color.White,

            fontSize =
                19.sp,

            fontWeight =
                FontWeight.Bold,

            maxLines =
                1,

            overflow =
                TextOverflow.Ellipsis
        )
    }
}
