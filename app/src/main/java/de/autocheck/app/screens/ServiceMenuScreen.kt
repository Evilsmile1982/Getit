package de.autocheck.app

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.autocheck.app.data.ServiceInterval
import de.autocheck.app.utils.ServiceHistoryPdfData
import de.autocheck.app.utils.ServicePdfData
import de.autocheck.app.utils.exportServiceHistoryPdf
import de.autocheck.app.utils.exportServicePdf
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

private val goldColor =
    Color(
        0xFFFFD700
    )

private val darkCardColor =
    Color(
        0xFF11141A
    )

private enum class ServiceMenuSection {

    MENU,

    INTERVALS,

    HISTORY
}


/*
 * ================================================================
 * HAUPT-EINSTIEG SERVICE
 * ================================================================
 */
@Composable
fun ServiceMenuScreen(
    store: VehicleStore,
    vehicles: List<Vehicle>,
    activeVehicle: String,
    activeVehicleData: Vehicle?,
    onActiveVehicle: (String) -> Unit,
    onVisited: () -> Unit
) {

    var section by remember {
        mutableStateOf(
            ServiceMenuSection.MENU
        )
    }

    /*
     * Wird beim Öffnen des Service-Bereichs einmal ausgelöst.
     */
    LaunchedEffect(Unit) {

        onVisited()
    }

    when (
        section
    ) {

        /*
         * ========================================================
         * SERVICE-MENÜ
         * ========================================================
         */
        ServiceMenuSection.MENU -> {

            ServiceMenuHome(

                vehicle =
                    activeVehicleData,

                onServiceAndIntervals = {

                    section =
                        ServiceMenuSection.INTERVALS
                },

                onServiceHistory = {

                    section =
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

            ServiceIntervalsScreen(

                store =
                    store,

                activeVehicle =
                    activeVehicle,

                onBack = {

                    section =
                        ServiceMenuSection.MENU
                }
            )
        }

        /*
         * ========================================================
         * SERVICE HISTORIE
         * ========================================================
         */
        ServiceMenuSection.HISTORY -> {

            ServiceHistoryScreen(

                store =
                    store,

                activeVehicle =
                    activeVehicle,

                onBack = {

                    section =
                        ServiceMenuSection.MENU
                }
            )
        }
    }
}


/*
 * ================================================================
 * SERVICE HAUPTMENÜ
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

        VehicleServiceHeader(
            vehicle =
                vehicle
        )

        Spacer(
            modifier =
                Modifier.height(
                    18.dp
                )
        )

        /*
         * ========================================================
         * SERVICE UND INTERVALLE
         * ========================================================
         */
        ServiceMenuGoldButton(

            title =
                "🔧  Service und Intervalle",

            subtitle =
                "Service, Intervalle und Erinnerungen",

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
        ServiceMenuGoldButton(

            title =
                "📋  Service Historie",

            subtitle =
                "Vergangene Services ansehen und als PDF speichern",

            onClick =
                onServiceHistory
        )
    }
}


/*
 * ================================================================
 * FAHRZEUGKOPF
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
                    darkCardColor
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
             * LINKE HÄLFTE
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
                            "Kein Fahrzeug",

                        color =
                            Color.White,

                        fontSize =
                            18.sp,

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
                            vehicle.year.ifBlank {
                                "—"
                            }
                    )

                    ServiceVehicleDataLine(
                        label =
                            "Kennzeichen",

                        value =
                            vehicle.plate.ifBlank {
                                "—"
                            }
                    )

                    ServiceVehicleDataLine(
                        label =
                            "Fahrzeug",

                        value =
                            vehicle.vehicleType.ifBlank {
                                "PKW"
                            }
                    )
                }
            }

            /*
             * ====================================================
             * RECHTE HÄLFTE – BILD
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

                        Text(
                            text =
                                "Kein Fahrzeugbild",

                            color =
                                Color.LightGray,

                            fontSize =
                                12.sp
                        )
                    }
                }
            }
        }
    }
}


/*
 * ================================================================
 * FAHRZEUGDATEN
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
 * GOLDENE HAUPTBUTTONS
 * ================================================================
 */
@Composable
private fun ServiceMenuGoldButton(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {

    Button(
        modifier =
            Modifier
                .fillMaxWidth(
                    0.92f
                )
                .height(
                    72.dp
                )
                .align(
                    Alignment.CenterHorizontally
                ),

        onClick =
            onClick,

        shape =
            RoundedCornerShape(
                50.dp
            ),

        colors =
            ButtonDefaults.buttonColors(
                containerColor =
                    goldColor,

                contentColor =
                    Color.Black
            ),

        contentPadding =
            PaddingValues(
                horizontal = 22.dp,
                vertical = 8.dp
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
                    17.sp,

                fontWeight =
                    FontWeight.Bold
            )

            Text(
                text =
                    subtitle,

                fontSize =
                    11.sp,

                maxLines =
                    1,

                overflow =
                    TextOverflow.Ellipsis
            )
        }
    }
}


/*
 * ================================================================
 * SEITENKOPF
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
            Alignment.CenterVertically
    ) {

        OutlinedButton(
            onClick =
                onBack
        ) {

            Text(
                "← Zurück"
            )
        }

        Spacer(
            modifier =
                Modifier.width(
                    10.dp
                )
        )

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
                FontWeight.Bold
        )
    }
}


/*
 * ================================================================
 * SERVICE UND INTERVALLE
 * ================================================================
 */
@Composable
private fun ServiceIntervalsScreen(
    store: VehicleStore,
    activeVehicle: String,
    onBack: () -> Unit
) {

    val context =
        LocalContext.current

    val currentDate =
        remember {
            LocalDate.now()
        }

    val currentYear =
        currentDate.year.toString()

    val currentMonth =
        currentDate.monthValue

    var services by remember {
        mutableStateOf(
            emptyList<ServiceInterval>()
        )
    }

    var addingService by remember {
        mutableStateOf(false)
    }

    var editingId by remember {
        mutableStateOf<Long?>(null)
    }

    var serviceMonth by remember {
        mutableStateOf(
            currentMonth
        )
    }

    var serviceYear by remember {
        mutableStateOf(
            currentYear
        )
    }

    var serviceKm by remember {
        mutableStateOf("")
    }

    var nextMonth by remember {
        mutableStateOf(
            currentMonth
        )
    }

    var nextYear by remember {
        mutableStateOf(
            currentYear
        )
    }

    var nextKm by remember {
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

    var reminderMonths by remember {
        mutableStateOf(0)
    }

    var reminderExpanded by remember {
        mutableStateOf(false)
    }

    var pdfData by remember {
        mutableStateOf<ServicePdfData?>(null)
    }

    val pdfLauncher =
        rememberLauncherForActivityResult(
            contract =
                ActivityResultContracts.CreateDocument(
                    "application/pdf"
                )
        ) { uri ->

            if (
                uri != null &&
                pdfData != null
            ) {

                exportServicePdf(
                    context =
                        context,

                    uri =
                        uri,

                    data =
                        pdfData!!
                )
            }
        }

    fun reload() {

        services =
            store
                .loadServiceIntervals()
                .filter {
                    it.vehicle ==
                        activeVehicle
                }
                .sortedByDescending {
                    serviceSortValue(
                        it
                    )
                }
    }

    fun clearForm() {

        editingId =
            null

        serviceMonth =
            currentMonth

        serviceYear =
            currentYear

        serviceKm =
            ""

        nextMonth =
            currentMonth

        nextYear =
            currentYear

        nextKm =
            ""

        documentation =
            ""

        cost =
            ""

        reminderEnabled =
            false

        reminderMonths =
            0
    }

    fun loadService(
        service: ServiceInterval
    ) {

        editingId =
            service.id

        serviceMonth =
            serviceMonthNumber(
                service.serviceMonth
            )

        serviceYear =
            service.serviceYear

        serviceKm =
            service.serviceKm

        nextMonth =
            serviceMonthNumber(
                service.nextServiceMonth
            )

        nextYear =
            service.nextServiceYear

        nextKm =
            service.nextServiceKm

        documentation =
            service.documentation

        cost =
            service.cost

        reminderEnabled =
            service.reminderEnabled

        reminderMonths =
            service.reminderMonthsBefore
                .coerceIn(
                    0,
                    3
                )

        addingService =
            true
    }

    fun save() {

        val all =
            store.loadServiceIntervals()

        val existing =
            all.firstOrNull {
                it.id ==
                    editingId
            }

        val entry =
            ServiceInterval(

                id =
                    existing?.id
                        ?: System.currentTimeMillis(),

                vehicle =
                    activeVehicle,

                serviceMonth =
                    serviceMonthName(
                        serviceMonth
                    ),

                serviceYear =
                    serviceYear.trim(),

                serviceKm =
                    serviceKm.trim(),

                nextServiceMonth =
                    serviceMonthName(
                        nextMonth
                    ),

                nextServiceYear =
                    nextYear.trim(),

                nextServiceKm =
                    nextKm.trim(),

                documentation =
                    documentation.trim(),

                cost =
                    cost.trim(),

                reminderEnabled =
                    reminderEnabled,

                reminderMonthsBefore =
                    if (
                        reminderEnabled
                    ) {
                        reminderMonths
                    } else {
                        0
                    }
            )

        val updated =
            if (
                existing != null
            ) {

                all.map {
                    if (
                        it.id ==
                            existing.id
                    ) {
                        entry
                    } else {
                        it
                    }
                }

            } else {

                all + entry
            }

        store.saveServiceIntervals(
            updated
        )

        reload()

        val newest =
            services.firstOrNull()

        if (
            newest != null
        ) {

            scheduleServiceReminder(

                context =
                    context,

                vehicle =
                    activeVehicle,

                nextServiceMonth =
                    newest.nextServiceMonth,

                nextServiceYear =
                    newest.nextServiceYear,

                reminderMonths =
                    if (
                        newest.reminderEnabled
                    ) {
                        newest.reminderMonthsBefore
                    } else {
                        0
                    }
            )
        }

        addingService =
            false

        clearForm()
    }

    fun exportCurrentPdf(
        service: ServiceInterval
    ) {

        pdfData =
            ServicePdfData(

                vehicle =
                    activeVehicle,

                lastServiceMonth =
                    service.serviceMonth,

                lastServiceYear =
                    service.serviceYear,

                lastServiceKm =
                    service.serviceKm,

                nextServiceMonth =
                    service.nextServiceMonth,

                nextServiceYear =
                    service.nextServiceYear,

                nextServiceKm =
                    service.nextServiceKm,

                documentation =
                    service.documentation,

                cost =
                    service.cost,

                reminderEnabled =
                    service.reminderEnabled,

                reminderMonthsBefore =
                    service.reminderMonthsBefore
            )

        pdfLauncher.launch(
            "Service_${activeVehicle.ifBlank { "Fahrzeug" }}.pdf"
        )
    }

    LaunchedEffect(
        activeVehicle
    ) {

        reload()

        addingService =
            false

        clearForm()
    }

    Column(
        modifier =
            Modifier.fillMaxSize()
    ) {

        ServiceSubPageHeader(
            title =
                "Service und Intervalle",

            onBack =
                onBack
        )

        if (
            addingService
        ) {

            LazyColumn(
                modifier =
                    Modifier.fillMaxSize(),

                contentPadding =
                    PaddingValues(
                        16.dp,
                        8.dp,
                        16.dp,
                        24.dp
                    ),

                verticalArrangement =
                    Arrangement.spacedBy(
                        10.dp
                    )
            ) {

                item {

                    ServiceEditForm(

                        isNew =
                            editingId == null,

                        serviceMonth =
                            serviceMonth,

                        serviceYear =
                            serviceYear,

                        serviceKm =
                            serviceKm,

                        nextMonth =
                            nextMonth,

                        nextYear =
                            nextYear,

                        nextKm =
                            nextKm,

                        documentation =
                            documentation,

                        cost =
                            cost,

                        reminderEnabled =
                            reminderEnabled,

                        reminderMonths =
                            reminderMonths,

                        reminderExpanded =
                            reminderExpanded,

                        onServiceMonth = {
                            serviceMonth =
                                it
                        },

                        onServiceYear = {
                            serviceYear =
                                it
                        },

                        onServiceKm = {
                            serviceKm =
                                it
                        },

                        onNextMonth = {
                            nextMonth =
                                it
                        },

                        onNextYear = {
                            nextYear =
                                it
                        },

                        onNextKm = {
                            nextKm =
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

                        onReminderMonths = {
                            reminderMonths =
                                it
                        },

                        onReminderExpanded = {
                            reminderExpanded =
                                it
                        },

                        onSave =
                            ::save,

                        onCancel = {

                            addingService =
                                false

                            clearForm()
                        }
                    )
                }
            }

        } else {

            LazyColumn(
                modifier =
                    Modifier.fillMaxSize(),

                contentPadding =
                    PaddingValues(
                        16.dp,
                        8.dp,
                        16.dp,
                        24.dp
                    ),

                verticalArrangement =
                    Arrangement.spacedBy(
                        10.dp
                    )
            ) {

                item {

                    val latest =
                        services.firstOrNull()

                    ServiceCurrentCard(

                        service =
                            latest,

                        onNew = {

                            clearForm()

                            addingService =
                                true
                        },

                        onEdit = {

                            if (
                                latest != null
                            ) {
                                loadService(
                                    latest
                                )
                            }
                        },

                        onPdf = {

                            if (
                                latest != null
                            ) {
                                exportCurrentPdf(
                                    latest
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}


/*
 * ================================================================
 * AKTUELLES SERVICE
 * ================================================================
 */
@Composable
private fun ServiceCurrentCard(
    service: ServiceInterval?,
    onNew: () -> Unit,
    onEdit: () -> Unit,
    onPdf: () -> Unit
) {

    Card(
        modifier =
            Modifier.fillMaxWidth(),

        colors =
            CardDefaults.cardColors(
                containerColor =
                    darkCardColor
            ),

        shape =
            RoundedCornerShape(
                18.dp
            )
    ) {

        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        16.dp
                    ),

            verticalArrangement =
                Arrangement.spacedBy(
                    10.dp
                )
        ) {

            Text(
                text =
                    "SERVICE UND INTERVALLE",

                color =
                    goldColor,

                fontSize =
                    17.sp,

                fontWeight =
                    FontWeight.Bold
            )

            if (
                service == null
            ) {

                Text(
                    text =
                        "Noch kein Service gespeichert.",

                    color =
                        Color.LightGray,

                    fontSize =
                        15.sp
                )

            } else {

                Text(
                    text =
                        "Letztes Service",

                    color =
                        Color(
                            0xFF4CAF50
                        ),

                    fontSize =
                        16.sp,

                    fontWeight =
                        FontWeight.Bold
                )

                Text(
                    text =
                        "${service.serviceMonth} ${service.serviceYear}",

                    color =
                        Color.White,

                    fontSize =
                        18.sp,

                    fontWeight =
                        FontWeight.Bold
                )

                if (
                    service.serviceKm.isNotBlank()
                ) {

                    Text(
                        text =
                            "Kilometer: ${service.serviceKm} km",

                        color =
                            Color.LightGray,

                        fontSize =
                            14.sp
                    )
                }

                Spacer(
                    modifier =
                        Modifier.height(
                            4.dp
                        )
                )

                Text(
                    text =
                        "Nächstes Service",

                    color =
                        Color(
                            0xFFF44336
                        ),

                    fontSize =
                        16.sp,

                    fontWeight =
                        FontWeight.Bold
                )

                Text(
                    text =
                        if (
                            service.nextServiceYear.isBlank()
                        ) {
                            "—"
                        } else {
                            "${service.nextServiceMonth} ${service.nextServiceYear}"
                        },

                    color =
                        Color.White,

                    fontSize =
                        18.sp,

                    fontWeight =
                        FontWeight.Bold
                )

                if (
                    service.nextServiceKm.isNotBlank()
                ) {

                    Text(
                        text =
                            "Nächstes Service bei: ${service.nextServiceKm} km",

                        color =
                            Color.LightGray,

                        fontSize =
                            14.sp
                    )
                }

                if (
                    service.documentation.isNotBlank()
                ) {

                    Spacer(
                        modifier =
                            Modifier.height(
                                2.dp
                            )
                    )

                    Text(
                        text =
                            "Dokumentation",

                        color =
                            goldColor,

                        fontSize =
                            16.sp,

                        fontWeight =
                            FontWeight.Bold
                    )

                    Text(
                        text =
                            service.documentation,

                        color =
                            Color.White,

                        fontSize =
                            14.sp
                    )
                }

                Spacer(
                    modifier =
                        Modifier.height(
                            2.dp
                        )
                )

                Text(
                    text =
                        "Erinnerung",

                    color =
                        Color(
                            0xFFF44336
                        ),

                    fontSize =
                        16.sp,

                    fontWeight =
                        FontWeight.Bold
                )

                Text(
                    text =
                        if (
                            service.reminderEnabled &&
                            service.reminderMonthsBefore > 0
                        ) {

                            when (
                                service.reminderMonthsBefore
                            ) {

                                1 ->
                                    "1 Monat vorher"

                                2 ->
                                    "2 Monate vorher"

                                3 ->
                                    "3 Monate vorher"

                                else ->
                                    "${service.reminderMonthsBefore} Monate vorher"
                            }

                        } else {

                            "Keine Erinnerung"
                        },

                    color =
                        Color.White,

                    fontSize =
                        14.sp
                )
            }

            Spacer(
                modifier =
                    Modifier.height(
                        4.dp
                    )
            )

            /*
             * ====================================================
             * NEUES SERVICE
             * ====================================================
             */
            Button(
                modifier =
                    Modifier.fillMaxWidth(),

                onClick =
                    onNew,

                colors =
                    ButtonDefaults.buttonColors(
                        containerColor =
                            goldColor,

                        contentColor =
                            Color.Black
                    ),

                shape =
                    RoundedCornerShape(
                        50.dp
                    )
            ) {

                Text(
                    "＋ Neues Service",

                    fontWeight =
                        FontWeight.Bold
                )
            }

            if (
                service != null
            ) {

                Row(
                    modifier =
                        Modifier.fillMaxWidth(),

                    horizontalArrangement =
                        Arrangement.spacedBy(
                            8.dp
                        )
                ) {

                    OutlinedButton(
                        modifier =
                            Modifier.weight(
                                1f
                            ),

                        onClick =
                            onEdit
                    ) {

                        Text(
                            "Bearbeiten"
                        )
                    }

                    OutlinedButton(
                        modifier =
                            Modifier.weight(
                                1f
                            ),

                        onClick =
                            onPdf
                    ) {

                        Text(
                            "PDF"
                        )
                    }
                }
            }
        }
    }
}


/*
 * ================================================================
 * SERVICE FORMULAR
 * ================================================================
 */
@Composable
private fun ServiceEditForm(
    isNew: Boolean,
    serviceMonth: Int,
    serviceYear: String,
    serviceKm: String,
    nextMonth: Int,
    nextYear: String,
    nextKm: String,
    documentation: String,
    cost: String,
    reminderEnabled: Boolean,
    reminderMonths: Int,
    reminderExpanded: Boolean,
    onServiceMonth: (Int) -> Unit,
    onServiceYear: (String) -> Unit,
    onServiceKm: (String) -> Unit,
    onNextMonth: (Int) -> Unit,
    onNextYear: (String) -> Unit,
    onNextKm: (String) -> Unit,
    onDocumentation: (String) -> Unit,
    onCost: (String) -> Unit,
    onReminderEnabled: (Boolean) -> Unit,
    onReminderMonths: (Int) -> Unit,
    onReminderExpanded: (Boolean) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {

    Card(
        modifier =
            Modifier.fillMaxWidth(),

        colors =
            CardDefaults.cardColors(
                containerColor =
                    darkCardColor
            ),

        shape =
            RoundedCornerShape(
                18.dp
            )
    ) {

        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        16.dp
                    ),

            verticalArrangement =
                Arrangement.spacedBy(
                    10.dp
                )
        ) {

            Text(
                text =
                    if (
                        isNew
                    ) {
                        "Neues Service"
                    } else {
                        "Service bearbeiten"
                    },

                color =
                    Color.White,

                fontSize =
                    21.sp,

                fontWeight =
                    FontWeight.Bold
            )

            Text(
                text =
                    "Durchgeführtes Service",

                color =
                    Color(
                        0xFF4CAF50
                    ),

                fontSize =
                    17.sp,

                fontWeight =
                    FontWeight.Bold
            )

            ServiceMonthYearRow(

                month =
                    serviceMonth,

                year =
                    serviceYear,

                onMonth =
                    onServiceMonth,

                onYear =
                    onServiceYear
            )

            OutlinedTextField(
                modifier =
                    Modifier.fillMaxWidth(),

                value =
                    serviceKm,

                onValueChange =
                    onServiceKm,

                label = {
                    Text(
                        "Kilometer beim Service"
                    )
                },

                singleLine =
                    true
            )

            Text(
                text =
                    "Nächstes Service",

                color =
                    Color(
                        0xFFF44336
                    ),

                fontSize =
                    17.sp,

                fontWeight =
                    FontWeight.Bold
            )

            ServiceMonthYearRow(

                month =
                    nextMonth,

                year =
                    nextYear,

                onMonth =
                    onNextMonth,

                onYear =
                    onNextYear
            )

            OutlinedTextField(
                modifier =
                    Modifier.fillMaxWidth(),

                value =
                    nextKm,

                onValueChange =
                    onNextKm,

                label = {
                    Text(
                        "Nächstes Service bei Kilometer"
                    )
                },

                singleLine =
                    true
            )

            Text(
                text =
                    "Die Kilometerangabe dient nur als Information. Es gibt keine Kilometer-Erinnerung.",

                color =
                    Color.LightGray,

                fontSize =
                    12.sp
            )

            Text(
                text =
                    "Dokumentation",

                color =
                    goldColor,

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

                singleLine =
                    true
            )

            Text(
                text =
                    "Erinnerung",

                color =
                    Color(
                        0xFFF44336
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
                            15.sp,

                        fontWeight =
                            FontWeight.Medium
                    )

                    Text(
                        text =
                            if (
                                reminderEnabled
                            ) {
                                "Benachrichtigung aktiviert"
                            } else {
                                "Keine Benachrichtigung"
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
                            onReminderExpanded(
                                true
                            )
                        }
                    ) {

                        Text(
                            when (
                                reminderMonths
                            ) {

                                1 ->
                                    "1 Monat vorher"

                                2 ->
                                    "2 Monate vorher"

                                3 ->
                                    "3 Monate vorher"

                                else ->
                                    "Zeitraum auswählen"
                            }
                        )
                    }

                    DropdownMenu(
                        expanded =
                            reminderExpanded,

                        onDismissRequest = {
                            onReminderExpanded(
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

                                onReminderMonths(
                                    1
                                )

                                onReminderExpanded(
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

                                onReminderMonths(
                                    2
                                )

                                onReminderExpanded(
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

                                onReminderMonths(
                                    3
                                )

                                onReminderExpanded(
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

                                onReminderMonths(
                                    0
                                )

                                onReminderEnabled(
                                    false
                                )

                                onReminderExpanded(
                                    false
                                )
                            }
                        )
                    }
                }
            }

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
                    Arrangement.spacedBy(
                        10.dp
                    )
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

                Button(
                    modifier =
                        Modifier.weight(
                            1f
                        ),

                    onClick =
                        onSave,

                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor =
                                goldColor,

                            contentColor =
                                Color.Black
                        )
                ) {

                    Text(
                        "Speichern",

                        fontWeight =
                            FontWeight.Bold
                    )
                }
            }
        }
    }
}


/*
 * ================================================================
 * SERVICE HISTORIE
 * ================================================================
 */
@Composable
private fun ServiceHistoryScreen(
    store: VehicleStore,
    activeVehicle: String,
    onBack: () -> Unit
) {

    val context =
        LocalContext.current

    var services by remember {
        mutableStateOf(
            emptyList<ServiceInterval>()
        )
    }

    var historyPdfData by remember {
        mutableStateOf<ServiceHistoryPdfData?>(null)
    }

    val historyPdfLauncher =
        rememberLauncherForActivityResult(
            contract =
                ActivityResultContracts.CreateDocument(
                    "application/pdf"
                )
        ) { uri ->

            if (
                uri != null &&
                historyPdfData != null
            ) {

                exportServiceHistoryPdf(
                    context =
                        context,

                    uri =
                        uri,

                    data =
                        historyPdfData!!
                )
            }
        }

    fun reload() {

        services =
            store
                .loadServiceIntervals()
                .filter {
                    it.vehicle ==
                        activeVehicle
                }
                .sortedWith(
                    compareByDescending<ServiceInterval> {
                        serviceSortValue(
                            it
                        )
                    }.thenByDescending {
                        it.id
                    }
                )
    }

    fun exportPdf() {

        if (
            services.isEmpty()
        ) {
            return
        }

        historyPdfData =
            ServiceHistoryPdfData(

                vehicle =
                    activeVehicle,

                services =
                    services
            )

        historyPdfLauncher.launch(
            "Service_Historie_${activeVehicle.ifBlank { "Fahrzeug" }}.pdf"
        )
    }

    LaunchedEffect(
        activeVehicle
    ) {

        reload()
    }

    val totalCost =
        services.sumOf {
            parseCost(
                it.cost
            )
        }

    Column(
        modifier =
            Modifier.fillMaxSize()
    ) {

        ServiceSubPageHeader(
            title =
                "Service Historie",

            onBack =
                onBack
        )

        LazyColumn(
            modifier =
                Modifier.fillMaxSize(),

            contentPadding =
                PaddingValues(
                    16.dp,
                    8.dp,
                    16.dp,
                    24.dp
                ),

            verticalArrangement =
                Arrangement.spacedBy(
                    10.dp
                )
        ) {

            /*
             * ====================================================
             * ÜBERSICHT
             * ====================================================
             */
            item {

                Card(
                    modifier =
                        Modifier.fillMaxWidth(),

                    colors =
                        CardDefaults.cardColors(
                            containerColor =
                                darkCardColor
                        ),

                    shape =
                        RoundedCornerShape(
                            18.dp
                        )
                ) {

                    Column(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(
                                    16.dp
                                ),

                        verticalArrangement =
                            Arrangement.spacedBy(
                                6.dp
                            )
                    ) {

                        Text(
                            text =
                                "SERVICE HISTORIE",

                            color =
                                goldColor,

                            fontSize =
                                18.sp,

                            fontWeight =
                                FontWeight.Bold
                        )

                        Text(
                            text =
                                "${services.size} Service-Einträge",

                            color =
                                Color.White,

                            fontSize =
                                15.sp
                        )

                        Text(
                            text =
                                if (
                                    totalCost > 0.0
                                ) {
                                    "Gesamtkosten: ${formatEuro(totalCost)}"
                                } else {
                                    "Gesamtkosten: —"
                                },

                            color =
                                Color.LightGray,

                            fontSize =
                                14.sp
                        )
                    }
                }
            }

            /*
             * ====================================================
             * PDF
             * ====================================================
             */
            item {

                Button(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(
                                58.dp
                            ),

                    onClick =
                        ::exportPdf,

                    enabled =
                        services.isNotEmpty(),

                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor =
                                goldColor,

                            contentColor =
                                Color.Black,

                            disabledContainerColor =
                                Color(
                                    0xFF555555
                                ),

                            disabledContentColor =
                                Color(
                                    0xFFBBBBBB
                                )
                        ),

                    shape =
                        RoundedCornerShape(
                            50.dp
                        )
                ) {

                    Text(
                        "📄  Service-Historie als PDF",

                        fontWeight =
                            FontWeight.Bold
                    )
                }
            }

            /*
             * ====================================================
             * LEERE HISTORIE
             * ====================================================
             */
            if (
                services.isEmpty()
            ) {

                item {

                    Card(
                        modifier =
                            Modifier.fillMaxWidth(),

                        colors =
                            CardDefaults.cardColors(
                                containerColor =
                                    darkCardColor
                            )
                    ) {

                        Text(
                            modifier =
                                Modifier.padding(
                                    16.dp
                                ),

                            text =
                                "Noch keine Service-Einträge vorhanden.",

                            color =
                                Color.LightGray,

                            fontSize =
                                15.sp
                        )
                    }
                }

            } else {

                /*
                 * =================================================
                 * ALLE HISTORISCHEN SERVICES
                 * =================================================
                 */
                items(
                    items =
                        services,

                    key = {
                        it.id
                    }
                ) { service ->

                    ServiceHistoryEntry(
                        service =
                            service
                    )
                }
            }
        }
    }
}


/*
 * ================================================================
 * HISTORIEN-EINTRAG
 * ================================================================
 *
 * WICHTIG:
 *
 * Hier gibt es absichtlich KEIN "Bearbeiten" und KEIN "Löschen".
 *
 * Die Historie ist eine reine Ansicht.
 * ================================================================
 */
@Composable
private fun ServiceHistoryEntry(
    service: ServiceInterval
) {

    Card(
        modifier =
            Modifier.fillMaxWidth(),

        colors =
            CardDefaults.cardColors(
                containerColor =
                    darkCardColor
            ),

        shape =
            RoundedCornerShape(
                16.dp
            )
    ) {

        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        16.dp
                    ),

            verticalArrangement =
                Arrangement.spacedBy(
                    6.dp
                )
        ) {

            Text(
                text =
                    "${service.serviceMonth} ${service.serviceYear}",

                color =
                    Color(
                        0xFF4CAF50
                    ),

                fontSize =
                    18.sp,

                fontWeight =
                    FontWeight.Bold
            )

            if (
                service.serviceKm.isNotBlank()
            ) {

                Text(
                    text =
                        "Kilometer: ${service.serviceKm} km",

                    color =
                        Color.White,

                    fontSize =
                        14.sp
                )
            }

            if (
                service.cost.isNotBlank()
            ) {

                Text(
                    text =
                        "Kosten: ${service.cost}",

                    color =
                        Color.White,

                    fontSize =
                        14.sp
                )
            }

            if (
                service.documentation.isNotBlank()
            ) {

                Spacer(
                    modifier =
                        Modifier.height(
                            2.dp
                        )
                )

                Text(
                    text =
                        "Dokumentation",

                    color =
                        goldColor,

                    fontSize =
                        14.sp,

                    fontWeight =
                        FontWeight.Bold
                )

                Text(
                    text =
                        service.documentation,

                    color =
                        Color.LightGray,

                    fontSize =
                        14.sp
                )
            }

            if (
                service.nextServiceYear.isNotBlank()
            ) {

                Spacer(
                    modifier =
                        Modifier.height(
                            2.dp
                        )
                )

                Text(
                    text =
                        "Nächstes Service: ${service.nextServiceMonth} ${service.nextServiceYear}",

                    color =
                        Color(
                            0xFFF44336
                        ),

                    fontSize =
                        13.sp
                )
            }

            if (
                service.reminderEnabled &&
                service.reminderMonthsBefore > 0
            ) {

                Text(
                    text =
                        "Erinnerung: ${service.reminderMonthsBefore} Monate vorher",

                    color =
                        Color(
                            0xFFF44336
                        ),

                    fontSize =
                        13.sp
                )
            }
        }
    }
}


/*
 * ================================================================
 * MONAT + JAHR
 * ================================================================
 */
@Composable
private fun ServiceMonthYearRow(
    month: Int,
    year: String,
    onMonth: (Int) -> Unit,
    onYear: (String) -> Unit
) {

    var expanded by remember {
        mutableStateOf(
            false
        )
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

                serviceMonths.forEachIndexed {
                    index,
                    name ->

                    DropdownMenuItem(
                        text = {
                            Text(
                                name
                            )
                        },

                        onClick = {

                            onMonth(
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
                    0.65f
                ),

            value =
                year,

            onValueChange =
                onYear,

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


/*
 * ================================================================
 * MONAT HILFSFUNKTIONEN
 * ================================================================
 */
private fun serviceMonthNumber(
    value: String
): Int {

    val index =
        serviceMonths.indexOf(
            value
                .trim()
                .uppercase()
        )

    return if (
        index >= 0
    ) {
        index + 1
    } else {
        1
    }
}


private fun serviceMonthName(
    value: Int
): String {

    return serviceMonths.getOrElse(
        value.coerceIn(
            1,
            12
        ) - 1
    ) {
        "JANUAR"
    }
}


/*
 * ================================================================
 * SORTIERUNG
 * ================================================================
 */
private fun serviceSortValue(
    service: ServiceInterval
): Int {

    val year =
        service.serviceYear
            .trim()
            .toIntOrNull()
            ?: 0

    val month =
        serviceMonthNumber(
            service.serviceMonth
        )

    return (
        year * 12
    ) + month
}


/*
 * ================================================================
 * KOSTEN AUSWERTEN
 * ================================================================
 */
private fun parseCost(
    value: String
): Double {

    if (
        value.isBlank()
    ) {
        return 0.0
    }

    return try {

        val cleaned =
            value
                .replace(
                    "€",
                    ""
                )
                .replace(
                    "EUR",
                    "",
                    ignoreCase = true
                )
                .replace(
                    " ",
                    ""
                )

        /*
         * Österreichische Schreibweise:
         *
         * 1.250,50
         */
        if (
            cleaned.contains(",") &&
            cleaned.contains(".")
        ) {

            cleaned
                .replace(
                    ".",
                    ""
                )
                .replace(
                    ",",
                    "."
                )
                .toDoubleOrNull()
                ?: 0.0

        } else {

            cleaned
                .replace(
                    ",",
                    "."
                )
                .toDoubleOrNull()
                ?: 0.0
        }

    } catch (
        _: Exception
    ) {

        0.0
    }
}


/*
 * ================================================================
 * EURO FORMATIERUNG
 * ================================================================
 */
private fun formatEuro(
    value: Double
): String {

    return String.format(
        java.util.Locale.GERMANY,
        "%.2f €",
        value
    )
}
