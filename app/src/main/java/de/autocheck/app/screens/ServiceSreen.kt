package de.autocheck.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate

private val serviceMonths =
    listOf(
        "JANUAR",
        "FEBRUAR",
        "MÄRZ",
        "APRIL",
        "MAI",
        "JUNI",
        "JULI",
        "AUGUST",
        "SEPTEMBER",
        "OKTOBER",
        "NOVEMBER",
        "DEZEMBER"
    )

private fun serviceMonthNumber(
    value: String
): Int {

    val index =
        serviceMonths.indexOf(
            value.uppercase()
        )

    return if (index >= 0) {
        index + 1
    } else {
        1
    }
}

private fun serviceMonthName(
    value: Int
): String {

    return serviceMonths.getOrElse(
        value.coerceIn(1, 12) - 1
    ) {
        "JANUAR"
    }
}

@Composable
fun ServiceScreen(
    store: VehicleStore,
    vehicles: List<Vehicle>,
    activeVehicle: String,
    onActiveVehicle: (String) -> Unit,
    onVisited: () -> Unit
) {

    val currentYear =
        remember {
            LocalDate
                .now()
                .year
                .toString()
        }

    val currentMonth =
        remember {
            LocalDate
                .now()
                .monthValue
        }

    var lastServiceMonth by remember {
        mutableStateOf(currentMonth)
    }

    var lastServiceYear by remember {
        mutableStateOf(currentYear)
    }

    var lastServiceKm by remember {
        mutableStateOf("")
    }

    var nextServiceMonth by remember {
        mutableStateOf(currentMonth)
    }

    var nextServiceYear by remember {
        mutableStateOf(currentYear)
    }

    var nextServiceKm by remember {
        mutableStateOf("")
    }

    var documentation by remember {
        mutableStateOf("")
    }

    var cost by remember {
        mutableStateOf("")
    }

    var reminderEnabled by remember {
        mutableStateOf(false)
    }

    var reminderMonthsBefore by remember {
        mutableStateOf(0)
    }

    var reminderKmBefore by remember {
        mutableStateOf(0)
    }

    var monthReminderExpanded by remember {
        mutableStateOf(false)
    }

    var kmReminderExpanded by remember {
        mutableStateOf(false)
    }

    var saved by remember {
        mutableStateOf(false)
    }

    var editing by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(Unit) {
        onVisited()
    }

    LaunchedEffect(activeVehicle) {

        val existing =
            store.loadServiceIntervals()
                .firstOrNull {
                    it.vehicle ==
                        activeVehicle
                }

        if (existing == null) {

            lastServiceMonth =
                currentMonth

            lastServiceYear =
                currentYear

            lastServiceKm =
                ""

            nextServiceMonth =
                currentMonth

            nextServiceYear =
                currentYear

            nextServiceKm =
                ""

            documentation =
                ""

            cost =
                ""

            reminderEnabled =
                false

            reminderMonthsBefore =
                0

            reminderKmBefore =
                0

            saved =
                false

            editing =
                true

        } else {

            lastServiceMonth =
                serviceMonthNumber(
                    existing.lastServiceMonth
                )

            lastServiceYear =
                existing.lastServiceYear
                    .ifBlank {
                        currentYear
                    }

            lastServiceKm =
                existing.lastServiceKm

            nextServiceMonth =
                serviceMonthNumber(
                    existing.nextServiceMonth
                )

            nextServiceYear =
                existing.nextServiceYear
                    .ifBlank {
                        currentYear
                    }

            nextServiceKm =
                existing.nextServiceKm

            documentation =
                existing.documentation

            cost =
                existing.cost

            reminderEnabled =
                existing.reminderEnabled

            reminderMonthsBefore =
                existing.reminderMonthsBefore
                    .coerceIn(
                        0,
                        3
                    )

            reminderKmBefore =
                when (
                    existing.reminderKmBefore
                ) {

                    500,
                    1000,
                    2000 ->
                        existing.reminderKmBefore

                    else ->
                        0
                }

            saved =
                true

            editing =
                false
        }
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
            vehicles.isEmpty()
        ) {

            Card(
                modifier =
                    Modifier.fillMaxWidth(),

                colors =
                    CardDefaults.cardColors(
                        containerColor =
                            Color(
                                0xFF11141A
                            )
                    )
            ) {

                Text(
                    modifier =
                        Modifier.padding(
                            16.dp
                        ),

                    text =
                        "Bitte zuerst unter „Mein Auto“ ein Fahrzeug anlegen.",

                    color =
                        Color.White
                )
            }

        } else {

            LazyColumn(
                modifier =
                    Modifier.fillMaxSize(),

                verticalArrangement =
                    Arrangement.spacedBy(
                        10.dp
                    )
            ) {

                item {

                    if (
                        editing
                    ) {

                        ServiceEditCard(

                            lastServiceMonth =
                                lastServiceMonth,

                            lastServiceYear =
                                lastServiceYear,

                            lastServiceKm =
                                lastServiceKm,

                            nextServiceMonth =
                                nextServiceMonth,

                            nextServiceYear =
                                nextServiceYear,

                            nextServiceKm =
                                nextServiceKm,

                            documentation =
                                documentation,

                            cost =
                                cost,

                            reminderEnabled =
                                reminderEnabled,

                            reminderMonthsBefore =
                                reminderMonthsBefore,

                            reminderKmBefore =
                                reminderKmBefore,

                            monthReminderExpanded =
                                monthReminderExpanded,

                            kmReminderExpanded =
                                kmReminderExpanded,

                            onLastServiceMonth = {
                                lastServiceMonth =
                                    it
                            },

                            onLastServiceYear = {
                                lastServiceYear =
                                    it
                            },

                            onLastServiceKm = {
                                lastServiceKm =
                                    it
                            },

                            onNextServiceMonth = {
                                nextServiceMonth =
                                    it
                            },

                            onNextServiceYear = {
                                nextServiceYear =
                                    it
                            },

                            onNextServiceKm = {
                                nextServiceKm =
                                    it
                            },

                            onDocumentation = {
                                documentation =
                                    it
                            },

                            onCost = {
                                cost =
                                    it
                            },

                            onReminderEnabled = {
                                reminderEnabled =
                                    it
                            },

                            onReminderMonthsBefore = {
                                reminderMonthsBefore =
                                    it
                            },

                            onReminderKmBefore = {
                                reminderKmBefore =
                                    it
                            },

                            onMonthReminderExpanded = {
                                monthReminderExpanded =
                                    it
                            },

                            onKmReminderExpanded = {
                                kmReminderExpanded =
                                    it
                            },

                            onSave = {

                                val existing =
                                    store.loadServiceIntervals()
                                        .firstOrNull {
                                            it.vehicle ==
                                                activeVehicle
                                        }

                                val entry =
                                    de.autocheck.app.data.ServiceInterval(

                                        id =
                                            existing?.id
                                                ?: System.currentTimeMillis(),

                                        vehicle =
                                            activeVehicle,

                                        lastServiceMonth =
                                            serviceMonthName(
                                                lastServiceMonth
                                            ),

                                        lastServiceYear =
                                            lastServiceYear.trim(),

                                        lastServiceKm =
                                            lastServiceKm.trim(),

                                        nextServiceMonth =
                                            serviceMonthName(
                                                nextServiceMonth
                                            ),

                                        nextServiceYear =
                                            nextServiceYear.trim(),

                                        nextServiceKm =
                                            nextServiceKm.trim(),

                                        documentation =
                                            documentation.trim(),

                                        cost =
                                            cost.trim(),

                                        reminderEnabled =
                                            reminderEnabled,

                                        reminderMonthsBefore =
                                            reminderMonthsBefore
                                                .coerceIn(
                                                    0,
                                                    3
                                                ),

                                        reminderKmBefore =
                                            when (
                                                reminderKmBefore
                                            ) {

                                                500,
                                                1000,
                                                2000 ->
                                                    reminderKmBefore

                                                else ->
                                                    0
                                            }
                                    )

                                val updated =
                                    store.loadServiceIntervals()
                                        .filterNot {
                                            it.vehicle ==
                                                activeVehicle
                                        } +
                                        entry

                                store.saveServiceIntervals(
                                    updated
                                )

                                saved =
                                    true

                                editing =
                                    false
                            },

                            onCancel = {

                                if (
                                    saved
                                ) {
                                    editing =
                                        false
                                }
                            },

                            showCancel =
                                saved
                        )

                    } else {

                        ServiceSummaryCard(

                            vehicle =
                                activeVehicle,

                            lastServiceMonth =
                                lastServiceMonth,

                            lastServiceYear =
                                lastServiceYear,

                            lastServiceKm =
                                lastServiceKm,

                            nextServiceMonth =
                                nextServiceMonth,

                            nextServiceYear =
                                nextServiceYear,

                            nextServiceKm =
                                nextServiceKm,

                            documentation =
                                documentation,

                            cost =
                                cost,

                            reminderEnabled =
                                reminderEnabled,

                            reminderMonthsBefore =
                                reminderMonthsBefore,

                            reminderKmBefore =
                                reminderKmBefore,

                            onEdit = {
                                editing =
                                    true
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ServiceEditCard(
    lastServiceMonth: Int,
    lastServiceYear: String,
    lastServiceKm: String,
    nextServiceMonth: Int,
    nextServiceYear: String,
    nextServiceKm: String,
    documentation: String,
    cost: String,
    reminderEnabled: Boolean,
    reminderMonthsBefore: Int,
    reminderKmBefore: Int,
    monthReminderExpanded: Boolean,
    kmReminderExpanded: Boolean,
    onLastServiceMonth: (Int) -> Unit,
    onLastServiceYear: (String) -> Unit,
    onLastServiceKm: (String) -> Unit,
    onNextServiceMonth: (Int) -> Unit,
    onNextServiceYear: (String) -> Unit,
    onNextServiceKm: (String) -> Unit,
    onDocumentation: (String) -> Unit,
    onCost: (String) -> Unit,
    onReminderEnabled: (Boolean) -> Unit,
    onReminderMonthsBefore: (Int) -> Unit,
    onReminderKmBefore: (Int) -> Unit,
    onMonthReminderExpanded: (Boolean) -> Unit,
    onKmReminderExpanded: (Boolean) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    showCancel: Boolean
) {

    Card(
        modifier =
            Modifier.fillMaxWidth(),

        colors =
            CardDefaults.cardColors(
                containerColor =
                    Color(
                        0xFF11141A
                    )
            )
    ) {

        Column(
            modifier =
                Modifier.padding(
                    16.dp
                ),

            verticalArrangement =
                Arrangement.spacedBy(
                    8.dp
                )
        ) {

            Text(
                text =
                    "Service und Intervalle",

                color =
                    Color.White,

                fontWeight =
                    FontWeight.Bold,

                fontSize =
                    22.sp
            )

            Text(
                text =
                    "Letztes Service",

                color =
                    Color.White,

                fontWeight =
                    FontWeight.Bold,

                fontSize =
                    17.sp
            )

            ServiceMonthYear(

                month =
                    lastServiceMonth,

                year =
                    lastServiceYear,

                onMonthChanged =
                    onLastServiceMonth,

                onYearChanged =
                    onLastServiceYear
            )

            OutlinedTextField(

                modifier =
                    Modifier.fillMaxWidth(),

                value =
                    lastServiceKm,

                onValueChange = { value ->

                    if (
                        value.length <= 7 &&
                        value.all {
                            it.isDigit()
                        }
                    ) {

                        onLastServiceKm(
                            value
                        )
                    }
                },

                label = {
                    Text(
                        "Kilometerstand beim letzten Service"
                    )
                },

                singleLine =
                    true
            )

            Spacer(
                modifier =
                    Modifier.height(
                        6.dp
                    )
            )

            Text(
                text =
                    "Nächstes Service",

                color =
                    Color.White,

                fontWeight =
                    FontWeight.Bold,

                fontSize =
                    17.sp
            )

            ServiceMonthYear(

                month =
                    nextServiceMonth,

                year =
                    nextServiceYear,

                onMonthChanged =
                    onNextServiceMonth,

                onYearChanged =
                    onNextServiceYear
            )

            OutlinedTextField(

                modifier =
                    Modifier.fillMaxWidth(),

                value =
                    nextServiceKm,

                onValueChange = { value ->

                    if (
                        value.length <= 7 &&
                        value.all {
                            it.isDigit()
                        }
                    ) {

                        onNextServiceKm(
                            value
                        )
                    }
                },

                label = {
                    Text(
                        "Nächster Service bei Kilometerstand"
                    )
                },

                singleLine =
                    true
            )

            OutlinedTextField(

                modifier =
                    Modifier.fillMaxWidth(),

                value =
                    documentation,

                onValueChange =
                    onDocumentation,

                label = {
                    Text(
                        "Dokumentation"
                    )
                },

                minLines =
                    4
            )

            OutlinedTextField(

                modifier =
                    Modifier.fillMaxWidth(),

                value =
                    cost,

                onValueChange = { value ->

                    if (
                        value.length <= 12
                    ) {

                        onCost(
                            value
                        )
                    }
                },

                label = {
                    Text(
                        "Kosten (€)"
                    )
                },

                singleLine =
                    true
            )

            Spacer(
                modifier =
                    Modifier.height(
                        4.dp
                    )
            )

            Row(

                modifier =
                    Modifier.fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.SpaceBetween,

                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Text(
                    text =
                        "Erinnerung",

                    color =
                        Color.White,

                    fontWeight =
                        FontWeight.Bold
                )

                Switch(
                    checked =
                        reminderEnabled,

                    onCheckedChange =
                        onReminderEnabled
                )
            }

            if (
                reminderEnabled
            ) {

                Text(
                    text =
                        "Zeitliche Erinnerung",

                    color =
                        Color.White,

                    fontWeight =
                        FontWeight.Bold
                )

                Box {

                    OutlinedButton(
                        onClick = {
                            onMonthReminderExpanded(
                                true
                            )
                        }
                    ) {

                        Text(

                            when (
                                reminderMonthsBefore
                            ) {

                                1 ->
                                    "1 Monat vorher"

                                2 ->
                                    "2 Monate vorher"

                                3 ->
                                    "3 Monate vorher"

                                else ->
                                    "Keine zeitliche Erinnerung"
                            }
                        )
                    }

                    DropdownMenu(

                        expanded =
                            monthReminderExpanded,

                        onDismissRequest = {
                            onMonthReminderExpanded(
                                false
                            )
                        }
                    ) {

                        listOf(
                            0,
                            1,
                            2,
                            3
                        ).forEach {
                            months ->

                            DropdownMenuItem(

                                text = {

                                    Text(

                                        when (
                                            months
                                        ) {

                                            1 ->
                                                "1 Monat vorher"

                                            2 ->
                                                "2 Monate vorher"

                                            3 ->
                                                "3 Monate vorher"

                                            else ->
                                                "Keine zeitliche Erinnerung"
                                        }
                                    )
                                },

                                onClick = {

                                    onReminderMonthsBefore(
                                        months
                                    )

                                    onMonthReminderExpanded(
                                        false
                                    )
                                }
                            )
                        }
                    }
                }

                Text(
                    text =
                        "Kilometer-Erinnerung",

                    color =
                        Color.White,

                    fontWeight =
                        FontWeight.Bold
                )

                Box {

                    OutlinedButton(
                        onClick = {
                            onKmReminderExpanded(
                                true
                            )
                        }
                    ) {

                        Text(

                            when (
                                reminderKmBefore
                            ) {

                                500 ->
                                    "500 km vorher"

                                1000 ->
                                    "1.000 km vorher"

                                2000 ->
                                    "2.000 km vorher"

                                else ->
                                    "Keine km-Erinnerung"
                            }
                        )
                    }

                    DropdownMenu(

                        expanded =
                            kmReminderExpanded,

                        onDismissRequest = {
                            onKmReminderExpanded(
                                false
                            )
                        }
                    ) {

                        listOf(
                            0,
                            500,
                            1000,
                            2000
                        ).forEach {
                            km ->

                            DropdownMenuItem(

                                text = {

                                    Text(

                                        when (
                                            km
                                        ) {

                                            500 ->
                                                "500 km vorher"

                                            1000 ->
                                                "1.000 km vorher"

                                            2000 ->
                                                "2.000 km vorher"

                                            else ->
                                                "Keine km-Erinnerung"
                                        }
                                    )
                                },

                                onClick = {

                                    onReminderKmBefore(
                                        km
                                    )

                                    onKmReminderExpanded(
                                        false
                                    )
                                }
                            )
                        }
                    }
                }

                Text(
                    text =
                        "Die Erinnerung wird später ausgelöst, sobald das zeitliche oder Kilometer-Intervall erreicht ist.",

                    color =
                        Color(
                            0xFF9AA0AA
                        ),

                    fontSize =
                        13.sp
                )
            }

            Spacer(
                modifier =
                    Modifier.height(
                        6.dp
                    )
            )

            Button(

                modifier =
                    Modifier.fillMaxWidth(),

                onClick =
                    onSave
            ) {

                Text(
                    "Service speichern"
                )
            }

            if (
                showCancel
            ) {

                TextButton(

                    modifier =
                        Modifier.fillMaxWidth(),

                    onClick =
                        onCancel
                ) {

                    Text(
                        "Abbrechen"
                    )
                }
            }
        }
    }
}

@Composable
private fun ServiceSummaryCard(
    vehicle: String,
    lastServiceMonth: Int,
    lastServiceYear: String,
    lastServiceKm: String,
    nextServiceMonth: Int,
    nextServiceYear: String,
    nextServiceKm: String,
    documentation: String,
    cost: String,
    reminderEnabled: Boolean,
    reminderMonthsBefore: Int,
    reminderKmBefore: Int,
    onEdit: () -> Unit
) {

    Card(

        modifier =
            Modifier.fillMaxWidth(),

        colors =
            CardDefaults.cardColors(
                containerColor =
                    Color(
                        0xFF11141A
                    )
            )
    ) {

        Column(

            modifier =
                Modifier.padding(
                    16.dp
                ),

            verticalArrangement =
                Arrangement.spacedBy(
                    8.dp
                )
        ) {

            Text(
                text =
                    "Gespeichertes Service",

                color =
                    Color.White,

                fontWeight =
                    FontWeight.Bold,

                fontSize =
                    22.sp
            )

            Text(
                text =
                    "Fahrzeug: $vehicle",

                color =
                    Color.White,

                fontSize =
                    17.sp
            )

            Text(
                text =
                    "Letztes Service: " +
                        serviceMonthName(
                            lastServiceMonth
                        ) +
                        " " +
                        lastServiceYear,

                color =
                    Color.White
            )

            if (
                lastServiceKm.isNotBlank()
            ) {

                Text(
                    text =
                        "Letztes Service bei: " +
                            lastServiceKm +
                            " km",

                    color =
                        Color.White
                )
            }

            Text(
                text =
                    "Nächstes Service: " +
                        serviceMonthName(
                            nextServiceMonth
                        ) +
                        " " +
                        nextServiceYear,

                color =
                    Color.White,

                fontWeight =
                    FontWeight.Bold,

                fontSize =
                    18.sp
            )

            if (
                nextServiceKm.isNotBlank()
            ) {

                Text(
                    text =
                        "Nächstes Service bei: " +
                            nextServiceKm +
                            " km",

                    color =
                        Color.White
                )
            }

            if (
                cost.isNotBlank()
            ) {

                Text(
                    text =
                        "Kosten: " +
                            cost +
                            " €",

                    color =
                        Color.White
                )
            }

            if (
                documentation.isNotBlank()
            ) {

                Text(
                    text =
                        "Dokumentation: " +
                            documentation,

                    color =
                        Color.White
                )
            }

            Text(

                text =
                    if (
                        reminderEnabled
                    ) {

                        val timeText =
                            when (
                                reminderMonthsBefore
                            ) {

                                1 ->
                                    "1 Monat"

                                2 ->
                                    "2 Monate"

                                3 ->
                                    "3 Monate"

                                else ->
                                    "keine Zeit"
                            }

                        val kmText =
                            when (
                                reminderKmBefore
                            ) {

                                500 ->
                                    "500 km"

                                1000 ->
                                    "1.000 km"

                                2000 ->
                                    "2.000 km"

                                else ->
                                    "keine km"
                            }

                        "Erinnerung: " +
                            timeText +
                            " / " +
                            kmText +
                            " vorher"

                    } else {

                        "Erinnerung: ausgeschaltet"
                    },

                color =
                    Color.White
            )

            Spacer(
                modifier =
                    Modifier.height(
                        6.dp
                    )
            )

            Button(

                modifier =
                    Modifier.fillMaxWidth(),

                onClick =
                    onEdit
            ) {

                Text(
                    "Ändern"
                )
            }

            Text(
                text =
                    "Die Service-Daten sind lokal auf diesem Gerät gespeichert.",

                color =
                    Color(
                        0xFF9AA0AA
                    ),

                fontSize =
                    13.sp
            )
        }
    }
}

@Composable
private fun ServiceMonthYear(
    month: Int,
    year: String,
    onMonthChanged: (Int) -> Unit,
    onYearChanged: (String) -> Unit
) {

    var expanded by remember {
        mutableStateOf(false)
    }

    Row(

        modifier =
            Modifier.fillMaxWidth(),

        horizontalArrangement =
            Arrangement.spacedBy(
                8.dp
            ),

        verticalAlignment =
            Alignment.CenterVertically
    ) {

        Box(
            modifier =
                Modifier.weight(
                    1f
                )
        ) {

            OutlinedButton(

                modifier =
                    Modifier.fillMaxWidth(),

                onClick = {
                    expanded =
                        true
                }
            ) {

                Text(
                    serviceMonthName(
                        month
                    )
                )
            }

            DropdownMenu(

                expanded =
                    expanded,

                onDismissRequest = {
                    expanded =
                        false
                }
            ) {

                serviceMonths
                    .forEachIndexed {
                        index,
                        name ->

                        DropdownMenuItem(

                            text = {
                                Text(
                                    name
                                )
                            },

                            onClick = {

                                onMonthChanged(
                                    index + 1
                                )

                                expanded =
                                    false
                            }
                        )
                    }
            }
        }

        OutlinedTextField(

            modifier =
                Modifier.weight(
                    1f
                ),

            value =
                year,

            onValueChange = { value ->

                if (
                    value.length <= 4 &&
                    value.all {
                        it.isDigit()
                    }
                ) {

                    onYearChanged(
                        value
                    )
                }
            },

            label = {
                Text(
                    "Jahr"
                )
            },

            singleLine =
                true
        )
    }
}
