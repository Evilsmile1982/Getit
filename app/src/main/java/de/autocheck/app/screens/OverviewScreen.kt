package de.autocheck.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val overviewPickerlFormatter =
    DateTimeFormatter.ofPattern(
        "MM/yyyy"
    )

private fun overviewPickerlMonthYear(
    value: String
): String {

    val text =
        value.trim()

    if (
        text.isBlank()
    ) {
        return "nicht eingetragen"
    }

    return try {

        LocalDate
            .parse(text)
            .format(
                overviewPickerlFormatter
            )

    } catch (_: Exception) {

        try {

            LocalDate
                .parse(
                    text.substring(
                        0,
                        10
                    )
                )
                .format(
                    overviewPickerlFormatter
                )

        } catch (_: Exception) {

            "nicht eingetragen"
        }
    }
}

private fun overviewPickerlText(
    nextDate: String?
): AnnotatedString {

    val bis =
        overviewPickerlMonthYear(
            nextDate ?: ""
        )

    return buildAnnotatedString {

        val title =
            "Nächste Überprüfung"

        append(
            title
        )

        addStyle(

            style =
                SpanStyle(

                    color =
                        Color(
                            0xFFB05CFF
                        ),

                    fontWeight =
                        FontWeight.Bold
                ),

            start =
                0,

            end =
                title.length
        )

        append(
            " bis "
        )

        val bisStart =
            length

        append(
            bis
        )

        addStyle(

            style =
                SpanStyle(

                    color =
                        Color(
                            0xFFF44336
                        )
                ),

            start =
                bisStart,

            end =
                length
        )
    }
}

@Composable
fun OverviewScreen(
    store: VehicleStore,
    vehicles: List<Vehicle>,
    activeVehicle: String,
    onActiveVehicle: (String) -> Unit,
    onVisited: () -> Unit
) {

    LaunchedEffect(
        Unit
    ) {
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
        modifier =
            Modifier.fillMaxSize()
    ) {

        VehicleSelector(

            vehicles =
                vehicles,

            activeVehicle =
                activeVehicle,

            onSelected =
                onActiveVehicle
        )

        Spacer(
            modifier =
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

                        text =
                            vehicle.name,

                        color =
                            Color.White,

                        fontSize =
                            24.sp,

                        fontWeight =
                            FontWeight.Bold
                    )

                    Text(

                        text =
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

                            text =
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

                            text =
                                "FIN/VIN: ${vehicle.vin}",

                            color =
                                Color.White
                        )
                    }

                    Spacer(
                        modifier =
                            Modifier.height(
                                8.dp
                            )
                    )

                    Text(

                        text =
                            "Reparaturen: $repairs",

                        color =
                            Color.White
                    )

                    Text(

                        text =
                            "Wartungen: $maintenance",

                        color =
                            Color.White
                    )

                    Text(

                        text =
                            "Reifensätze: $tires",

                        color =
                            Color.White
                    )

                    Text(

                        text =
                            overviewPickerlText(
                                nextDate =
                                    pickerl?.nextDate
                            )
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
