package de.autocheck.app

import android.content.Context
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.autocheck.app.utils.scheduleServiceReminder
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
            value.trim().uppercase()
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

    val context =
        LocalContext.current

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
        mutableStateOf(
            currentMonth
        )
    }

    var lastServiceYear by remember {
        mutableStateOf(
            currentYear
        )
    }

    var lastServiceKm by remember {
        mutableStateOf("")
    }

    var nextServiceMonth by remember {
        mutableStateOf(
            currentMonth
        )
    }

    var nextServiceYear by remember {
        mutableStateOf(
            currentYear
        )
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

    var monthReminderExpanded by remember {
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
            store
                .loadServiceIntervals()
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
                    Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 16.dp
                        ),

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
                        Color.White,

                    fontSize =
                        16.sp
                )
            }

        } else {

            LazyColumn(
                modifier =
                    Modifier.fillMaxSize(),

                contentPadding =
                    androidx.compose.foundation.layout.PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 24.dp
                    ),

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

                            monthReminderExpanded =
                                monthReminderExpanded,

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

                            onMonthReminderExpanded = {
                                monthReminderExpanded =
                                    it
                            },

                            onSave = {

                                val existing =
                                    store
                                        .loadServiceIntervals()
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
                                            lastServiceYear
                                                .trim(),

                                        lastServiceKm =
                                            lastServiceKm
                                                .trim(),

                                        nextServiceMonth =
                                            serviceMonthName(
                                                nextServiceMonth
                                            ),

                                        nextServiceYear =
                                            nextServiceYear
                                                .trim(),

                                        nextServiceKm =
                                            nextServiceKm
                                                .trim(),

                                        documentation =
                                            documentation
                                                .trim(),

                                        cost =
                                            cost
                                                .trim(),

                                        reminderEnabled =
                                            reminderEnabled,

                                        reminderMonthsBefore =
                                            reminderMonthsBefore
                                                .coerceIn(
                                                    0,
                                                    3
                                                )
                                    )

                                val updated =
                                    store
                                        .loadServiceIntervals()
                                        .filterNot {
                                            it.vehicle ==
                                                activeVehicle
                                        } +
                                        entry

                                store.saveServiceIntervals(
                                    updated
                                )

                                scheduleServiceReminder(
                                    context =
                                        context,

                                    vehicle =
                                        activeVehicle,

                                    nextServiceMonth =
                                        entry.nextServiceMonth,

                                    nextServiceYear =
                                        entry.nextServiceYear,

                                    reminderMonths =
                                        if (
                                            entry.reminderEnabled
                                        ) {
                                            entry.reminderMonthsBefore
                                        } else {
                                            0
                                        }
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
    monthReminderExpanded: Boolean,
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
    onMonthReminderExpanded: (Boolean) -> Unit,
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
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),

            verticalArrangement =
                Arrangement.spacedBy(
                    10.dp
                )
        ) {

            Text(
                text =
                    "Service und Intervalle",

                color =
                    Color.White,

                fontSize =
                    21.sp,

                fontWeight =
                    FontWeight.Bold
            )

            Text(
                text =
                    "Letztes Service",

                color =
                    Color(
                        0xFF9A4DFF
                    ),

                fontSize =
                    17.sp,

                fontWeight =
                    FontWeight.Bold
            )

            ServiceMonthYear(
                month =
                    lastServiceMonth,

                year =
                    lastServiceYear,

                onMonth =
                    onLastServiceMonth,

                onYear =
                    onLastServiceYear
            )

            OutlinedTextField(
                modifier =
                    Modifier.fillMaxWidth(),

                value =
                    lastServiceKm,

                onValueChange =
                    onLastServiceKm,

                label = {
                    Text(
                        "Kilometer beim letzten Service"
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
                    Color(
                        0xFF9A4DFF
                    ),

                fontSize =
                    17.sp,

                fontWeight =
                    FontWeight.Bold
            )

            ServiceMonthYear(
                month =
                    nextServiceMonth,

                year =
                    nextServiceYear,

                onMonth =
                    onNextServiceMonth,

                onYear =
                    onNextServiceYear
            )

            OutlinedTextField(
                modifier =
                    Modifier.fillMaxWidth(),

                value =
                    nextServiceKm,

                onValueChange =
                    onNextServiceKm,

                label = {
                    Text(
                        "Nächstes Service bei Kilometer"
                    )
                },

                placeholder = {
                    Text(
                        "z. B. 150000"
                    )
                },

                singleLine =
                    true
            )

            Text(
                text =
                    "Die Kilometerangabe dient als Information. Es wird keine Kilometer-Erinnerung ausgelöst.",

                color =
                    Color.LightGray,

                fontSize =
                    12.sp
            )

            Spacer(
                modifier =
                    Modifier.height(
                        6.dp
                    )
            )

            Text(
                text =
                    "Dokumentation",

                color =
                    Color(
                        0xFF9A4DFF
                    ),

                fontSize =
                    17.sp,

                fontWeight =
                    FontWeight.Bold
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

                placeholder = {
                    Text(
                        "z. B. Öl, Filter und Bremsen erneuert"
                    )
                },

                minLines =
                    3,

                maxLines =
                    6
            )

            OutlinedTextField(
                modifier =
                    Modifier.fillMaxWidth(),

                value =
                    cost,

                onValueChange =
                    onCost,

                label = {
                    Text(
                        "Kosten"
                    )
                },

                placeholder = {
                    Text(
                        "z. B. 350 €"
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
                    "Erinnerung",

                color =
                    Color(
                        0xFF9A4DFF
                    ),

                fontSize =
                    17.sp,

                fontWeight =
                    FontWeight.Bold
            )

            Row(
                modifier =
                    Modifier.fillMaxWidth(),

                verticalAlignment =
                    Alignment.CenterVertically,

                horizontalArrangement =
                    Arrangement.SpaceBetween
            ) {

                Column(
                    modifier =
                        Modifier.weight(
                            1f
                        )
                ) {

                    Text(
                        text =
                            "Service-Erinnerung",

                        color =
                            Color.White,

                        fontSize =
                            16.sp,

                        fontWeight =
                            FontWeight.Medium
                    )

                    Text(
                        text =
                            if (
                                reminderEnabled
                            ) {
                                "Benachrichtigung ist aktiviert."
                            } else {
                                "Keine automatische Benachrichtigung."
                            },

                        color =
                            Color.LightGray,

                        fontSize =
                            12.sp
                    )
                }

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

                Box(
                    modifier =
                        Modifier.fillMaxWidth()
                ) {

                    OutlinedButton(
                        modifier =
                            Modifier.fillMaxWidth(),

                        onClick = {
                            onMonthReminderExpanded(
                                true
                            )
                        }
                    ) {

                        Text(
                            text =
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
                                        "Erinnerungszeitraum auswählen"
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

                        DropdownMenuItem(
                            text = {
                                Text(
                                    "1 Monat vorher"
                                )
                            },

                            onClick = {

                                onReminderMonthsBefore(
                                    1
                                )

                                onMonthReminderExpanded(
                                    false
                                )
                            }
                        )

                        DropdownMenuItem(
                            text = {
                                Text(
                                    "2 Monate vorher"
                                )
                            },

                            onClick = {

                                onReminderMonthsBefore(
                                    2
                                )

                                onMonthReminderExpanded(
                                    false
                                )
                            }
                        )

                        DropdownMenuItem(
                            text = {
                                Text(
                                    "3 Monate vorher"
                                )
                            },

                            onClick = {

                                onReminderMonthsBefore(
                                    3
                                )

                                onMonthReminderExpanded(
                                    false
                                )
                            }
                        )

                        DropdownMenuItem(
                            text = {
                                Text(
                                    "Keine Erinnerung"
                                )
                            },

                            onClick = {

                                onReminderMonthsBefore(
                                    0
                                )

                                onReminderEnabled(
                                    false
                                )

                                onMonthReminderExpanded(
                                    false
                                )
                            }
                        )
                    }
                }

                Text(
                    text =
                        "Die Erinnerung erfolgt am eingestellten Tag um 09:00 Uhr.",

                    color =
                        Color.LightGray,

                    fontSize =
                        12.sp
                )
            }

            Spacer(
                modifier =
                    Modifier.height(
                        6.dp
                    )
            )

            Row(
                modifier =
                    Modifier.fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.spacedBy(
                        10.dp
                    )
            ) {

                if (
                    showCancel
                ) {

                    OutlinedButton(
                        modifier =
                            Modifier.weight(
                                1f
                            ),

                        onClick =
                            onCancel
                    ) {

                        Text(
                            "Abbrechen"
                        )
                    }
                }

                Button(
                    modifier =
                        Modifier.weight(
                            1f
                        ),

                    onClick =
                        onSave
                ) {

                    Text(
                        "Speichern"
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
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),

            verticalArrangement =
                Arrangement.spacedBy(
                    10.dp
                )
        ) {

            Text(
                text =
                    "Service und Intervalle",

                color =
                    Color.White,

                fontSize =
                    21.sp,

                fontWeight =
                    FontWeight.Bold
            )

            Text(
                text =
                    vehicle,

                color =
                    Color(
                        0xFF9A4DFF
                    ),

                fontSize =
                    15.sp,

                fontWeight =
                    FontWeight.Medium
            )

            ServiceSummarySectionTitle(
                text =
                    "Letztes Service"
            )

            ServiceSummaryRow(
                label =
                    "Datum",

                value =
                    "${serviceMonthName(lastServiceMonth)} $lastServiceYear"
            )

            ServiceSummaryRow(
                label =
                    "Kilometer",

                value =
                    if (
                        lastServiceKm.isBlank()
                    ) {
                        "—"
                    } else {
                        "$lastServiceKm km"
                    }
            )

            ServiceSummarySectionTitle(
                text =
                    "Nächstes Service"
            )

            ServiceSummaryRow(
                label =
                    "Datum",

                value =
                    "${serviceMonthName(nextServiceMonth)} $nextServiceYear"
            )

            ServiceSummaryRow(
                label =
                    "Kilometer",

                value =
                    if (
                        nextServiceKm.isBlank()
                    ) {
                        "—"
                    } else {
                        "$nextServiceKm km"
                    }
            )

            ServiceSummarySectionTitle(
                text =
                    "Dokumentation"
            )

            Text(
                text =
                    if (
                        documentation.isBlank()
                    ) {
                        "Keine Dokumentation hinterlegt."
                    } else {
                        documentation
                    },

                color =
                    if (
                        documentation.isBlank()
                    ) {
                        Color.Gray
                    } else {
                        Color.White
                    },

                fontSize =
                    14.sp
            )

            ServiceSummaryRow(
                label =
                    "Kosten",

                value =
                    if (
                        cost.isBlank()
                    ) {
                        "—"
                    } else {
                        cost
                    }
            )

            ServiceSummarySectionTitle(
                text =
                    "Erinnerung"
            )

            val reminderText =
                if (
                    reminderEnabled &&
                    reminderMonthsBefore > 0
                ) {

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
                            "Aktiv"
                    }

                } else {
                    "Keine Erinnerung"
                }

            ServiceSummaryRow(
                label =
                    "Service-Erinnerung",

                value =
                    reminderText
            )

            Text(
                text =
                    "Es werden keine Kilometer-Erinnerungen verwendet.",

                color =
                    Color.Gray,

                fontSize =
                    12.sp
            )

            Spacer(
                modifier =
                    Modifier.height(
                        4.dp
                    )
            )

            Button(
                modifier =
                    Modifier.fillMaxWidth(),

                onClick =
                    onEdit
            ) {

                Text(
                    "Service bearbeiten"
                )
            }
        }
    }
}

@Composable
private fun ServiceSummarySectionTitle(
    text: String
) {

    Spacer(
        modifier =
            Modifier.height(
                4.dp
            )
    )

    Text(
        text =
            text,

        color =
            Color(
                0xFF9A4DFF
            ),

        fontSize =
            16.sp,

        fontWeight =
            FontWeight.Bold
    )
}

@Composable
private fun ServiceSummaryRow(
    label: String,
    value: String
) {

    Row(
        modifier =
            Modifier.fillMaxWidth(),

        horizontalArrangement =
            Arrangement.SpaceBetween,

        verticalAlignment =
            Alignment.Top
    ) {

        Text(
            modifier =
                Modifier.weight(
                    0.45f
                ),

            text =
                label,

            color =
                Color.LightGray,

            fontSize =
                14.sp
        )

        Text(
            modifier =
                Modifier.weight(
                    0.55f
                ),

            text =
                value,

            color =
                Color.White,

            fontSize =
                14.sp,

            fontWeight =
                FontWeight.Medium
        )
    }
}

@Composable
private fun ServiceMonthYear(
    month: Int,
    year: String,
    onMonth: (Int) -> Unit,
    onYear: (String) -> Unit
) {

    var monthExpanded by remember {
        mutableStateOf(false)
    }

    Row(
        modifier =
            Modifier.fillMaxWidth(),

        horizontalArrangement =
            Arrangement.spacedBy(
                10.dp
            )
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
                    monthExpanded =
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
                    monthExpanded,

                onDismissRequest = {
                    monthExpanded =
                        false
                }
            ) {

                serviceMonths.forEachIndexed {
                    index,
                    monthName ->

                    DropdownMenuItem(
                        text = {
                            Text(
                                monthName
                            )
                        },

                        onClick = {

                            onMonth(
                                index + 1
                            )

                            monthExpanded =
                                false
                        }
                    )
                }
            }
        }

        OutlinedTextField(
            modifier =
                Modifier.weight(
                    0.65f
                ),

            value =
                year,

            onValueChange = {
                onYear(
                    it
                )
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
