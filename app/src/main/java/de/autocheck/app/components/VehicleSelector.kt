package de.autocheck.app

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun VehicleSelector(
    vehicles: List<Vehicle>,
    activeVehicle: String,
    onSelected: (String) -> Unit
) {

    var expanded by remember {
        mutableStateOf(
            false
        )
    }

    if (
        vehicles.isEmpty()
    ) {

        Text(
            "Bitte zuerst unter „Mein Auto“ ein Fahrzeug anlegen.",
            color =
                Color(
                    0xFFB8BEC8
                )
        )

        return
    }

    val selected =
        vehicles.firstOrNull {
            it.name ==
                activeVehicle
        }
            ?: vehicles.first()

    val vehicleDetails =
        listOf(
            selected.make.trim(),
            selected.model.trim()
        )
            .filter {
                it.isNotBlank()
            }
            .joinToString(
                " · "
            )

    Column(
        modifier =
            Modifier.fillMaxWidth(),

        verticalArrangement =
            Arrangement.spacedBy(
                4.dp
            )
    ) {

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 4.dp,
                        end = 4.dp
                    )
        ) {

            Column(
                modifier =
                    Modifier.weight(
                        1f
                    )
            ) {

                Text(
                    text =
                        selected.name,

                    color =
                        Color.White,

                    fontSize =
                        20.sp,

                    fontWeight =
                        FontWeight.Bold
                )

                if (
                    vehicleDetails.isNotBlank()
                ) {

                    Text(
                        text =
                            vehicleDetails,

                        color =
                            Color(
                                0xFFB8BEC8
                            ),

                        fontSize =
                            14.sp
                    )
                }
            }
        }

        Box(
            modifier =
                Modifier.fillMaxWidth()
        ) {

            OutlinedTextField(
                value =
                    if (
                        vehicleDetails.isNotBlank()
                    ) {
                        "${selected.name} · $vehicleDetails"
                    } else {
                        selected.name
                    },

                onValueChange = {},

                readOnly =
                    true,

                label = {
                    Text(
                        "Aktives Fahrzeug"
                    )
                },

                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clickable {
                            expanded =
                                true
                        },

                singleLine =
                    true
            )

            DropdownMenu(
                expanded =
                    expanded,

                onDismissRequest = {
                    expanded =
                        false
                }
            ) {

                vehicles.forEach {
                    vehicle ->

                    val details =
                        listOf(
                            vehicle.make.trim(),
                            vehicle.model.trim()
                        )
                            .filter {
                                it.isNotBlank()
                            }
                            .joinToString(
                                " · "
                            )

                    val displayText =
                        if (
                            details.isNotBlank()
                        ) {
                            "${vehicle.name} · $details"
                        } else {
                            vehicle.name
                        }

                    DropdownMenuItem(
                        text = {

                            Text(
                                displayText
                            )
                        },

                        onClick = {

                            expanded =
                                false

                            onSelected(
                                vehicle.name
                            )
                        }
                    )
                }
            }
        }
    }
}
