package de.autocheck.app

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val pickerlIsoFormatter =
    DateTimeFormatter.ofPattern(
        "yyyy-MM-dd"
    )

private val pickerlMonths =
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

private fun pickerlMonthName(
    month: Int
): String {

    return pickerlMonths.getOrElse(
        month - 1
    ) {
        "JANUAR"
    }
}

private fun pickerlParseDate(
    value: String
): LocalDate? {

    val text =
        value.trim()

    if (text.isBlank()) {
        return null
    }

    return try {

        LocalDate.parse(
            text,
            pickerlIsoFormatter
        )

    } catch (_: Exception) {

        try {

            LocalDate.parse(
                text
            )

        } catch (_: Exception) {

            null
        }
    }
}

private fun pickerlStorageDate(
    month: Int,
    year: String
): String {

    val cleanYear =
        year.trim()

    if (
        cleanYear.length != 4
    ) {
        return ""
    }

    val numericYear =
        cleanYear.toIntOrNull()
            ?: return ""

    if (
        numericYear !in 1900..2200
    ) {
        return ""
    }

    return String.format(
        Locale.US,
        "%04d-%02d-01",
        numericYear,
        month.coerceIn(
            1,
            12
        )
    )
}

private fun pickerlMonthYear(
    month: Int,
    year: String
): String {

    if (
        year.length != 4
    ) {
        return pickerlMonthName(
            month
        )
    }

    return "${pickerlMonthName(month)} $year"
}

private fun copyPickerlImage(
    context: Context,
    uri: Uri
): String? {

    return try {

        val type =
            context.contentResolver
                .getType(uri)

        val extension =
            when (type) {
                "image/png" ->
                    "png"

                "image/webp" ->
                    "webp"

                else ->
                    "jpg"
            }

        val file =
            File(
                context.filesDir,
                "pickerl_${System.currentTimeMillis()}.$extension"
            )

        context.contentResolver
            .openInputStream(uri)
            ?.use { input ->

                FileOutputStream(file)
                    .use { output ->

                        input.copyTo(
                            output
                        )
                    }
            }
            ?: return null

        file.absolutePath

    } catch (_: Exception) {

        null
    }
}

private fun savePickerlCameraImage(
    context: Context,
    bitmap: Bitmap
): String? {

    return try {

        val file =
            File(
                context.filesDir,
                "pickerl_${System.currentTimeMillis()}.jpg"
            )

        FileOutputStream(file)
            .use { output ->

                bitmap.compress(
                    Bitmap.CompressFormat.JPEG,
                    92,
                    output
                )
            }

        file.absolutePath

    } catch (_: Exception) {

        null
    }
}

@Composable
private fun PickerlMonthYear(
    title: String,
    month: Int,
    year: String,
    onMonthChanged: (Int) -> Unit,
    onYearChanged: (String) -> Unit
) {

    var expanded by remember {
        mutableStateOf(false)
    }

    Text(
        text = title,
        color = Color.White,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp
    )

    Spacer(
        modifier = Modifier.height(6.dp)
    )

    Row(
        modifier =
            Modifier.fillMaxWidth(),
        horizontalArrangement =
            Arrangement.spacedBy(8.dp),
        verticalAlignment =
            Alignment.CenterVertically
    ) {

        Box(
            modifier =
                Modifier.weight(1f)
        ) {

            OutlinedButton(
                modifier =
                    Modifier.fillMaxWidth(),
                onClick = {
                    expanded = true
                }
            ) {

                Text(
                    pickerlMonthName(
                        month
                    )
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = {
                    expanded = false
                }
            ) {

                pickerlMonths
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
                Modifier.weight(1f),
            value = year,
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
            singleLine = true,
            keyboardOptions =
                KeyboardOptions(
                    keyboardType =
                        KeyboardType.Number
                )
        )
    }
}

@Composable
fun PickerlScreen(
    store: VehicleStore,
    vehicles: List<Vehicle>,
    activeVehicle: String,
    onActiveVehicle: (String) -> Unit,
    onVisited: () -> Unit
) {

    val context =
        LocalContext.current

    LaunchedEffect(Unit) {
        onVisited()
    }

    val currentYear =
        remember {
            LocalDate
                .now()
                .year
                .toString()
        }

    var lastMonth by remember {
        mutableStateOf(1)
    }

    var lastYear by remember {
        mutableStateOf(currentYear)
    }

    var nextMonth by remember {
        mutableStateOf(1)
    }

    var nextYear by remember {
        mutableStateOf(currentYear)
    }

    var notes by remember {
        mutableStateOf("")
    }

    var reminder by remember {
        mutableStateOf(true)
    }

    var reminderMonths by remember {
        mutableStateOf(3)
    }

    var photoPath by remember {
        mutableStateOf("")
    }

    var photoBitmap by remember {
        mutableStateOf<Bitmap?>(null)
    }

    var saved by remember {
        mutableStateOf(false)
    }

    var editing by remember {
        mutableStateOf(false)
    }

    var reminderMenuExpanded by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(
        activeVehicle
    ) {

        val existing =
            store.loadPickerl()
                .firstOrNull {
                    it.vehicle ==
                        activeVehicle
                }

        if (
            existing == null
        ) {

            lastMonth = 1
            lastYear = currentYear

            nextMonth = 1
            nextYear = currentYear

            notes = ""
            reminder = true
            reminderMonths = 3
            photoPath = ""

            saved = false
            editing = true

        } else {

            pickerlParseDate(
                existing.lastDate
            )?.let { date ->

                lastMonth =
                    date.monthValue

                lastYear =
                    date.year.toString()
            }

            pickerlParseDate(
                existing.nextDate
            )?.let { date ->

                nextMonth =
                    date.monthValue

                nextYear =
                    date.year.toString()
            }

            notes =
                existing.notes

            reminder =
                existing.reminder

            reminderMonths =
                existing.reminderMonths
                    .coerceIn(
                        1,
                        5
                    )

            photoPath =
                existing.photoUri

            saved = true
            editing = false
        }
    }

    LaunchedEffect(
        photoPath
    ) {

        photoBitmap =
            if (
                photoPath.isBlank()
            ) {
                null
            } else {

                try {

                    BitmapFactory
                        .decodeFile(
                            photoPath
                        )

                } catch (_: Exception) {

                    null
                }
            }
    }

    val galleryLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri ->

            if (
                uri != null
            ) {

                copyPickerlImage(
                    context,
                    uri
                )?.let { path ->

                    photoPath =
                        path
                }
            }
        }

    val cameraLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.TakePicturePreview()
        ) { bitmap ->

            if (
                bitmap != null
            ) {

                savePickerlCameraImage(
                    context,
                    bitmap
                )?.let { path ->

                    photoPath =
                        path
                }
            }
        }

    Column(
        modifier =
            Modifier.fillMaxSize()
    ) {

        VehicleSelector(
            vehicles = vehicles,
            activeVehicle = activeVehicle,
            onSelected = onActiveVehicle
        )

        Spacer(
            modifier =
                Modifier.height(10.dp)
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

                        PickerlEditCard(
                            lastMonth = lastMonth,
                            lastYear = lastYear,
                            nextMonth = nextMonth,
                            nextYear = nextYear,
                            notes = notes,
                            reminder = reminder,
                            reminderMonths =
                                reminderMonths,
                            reminderMenuExpanded =
                                reminderMenuExpanded,
                            photoBitmap =
                                photoBitmap,
                            onLastMonth = {
                                lastMonth = it
                            },
                            onLastYear = {
                                lastYear = it
                            },
                            onNextMonth = {
                                nextMonth = it
                            },
                            onNextYear = {
                                nextYear = it
                            },
                            onNotes = {
                                notes = it
                            },
                            onReminder = {
                                reminder = it
                            },
                            onReminderMonths = {
                                reminderMonths =
                                    it
                            },
                            onReminderMenu =
                                {
                                    reminderMenuExpanded =
                                        it
                                },
                            onGallery = {
                                galleryLauncher.launch(
                                    "image/*"
                                )
                            },
                            onCamera = {
                                cameraLauncher.launch(
                                    null
                                )
                            },
                            onRemovePhoto = {
                                photoPath = ""
                                photoBitmap = null
                            },
                            onSave = {

                                val lastDate =
                                    pickerlStorageDate(
                                        lastMonth,
                                        lastYear
                                    )

                                val nextDate =
                                    pickerlStorageDate(
                                        nextMonth,
                                        nextYear
                                    )

                                if (
                                    lastDate.isBlank() ||
                                    nextDate.isBlank()
                                ) {

                                    return@PickerlEditCard
                                }

                                val months =
                                    reminderMonths
                                        .coerceIn(
                                            1,
                                            5
                                        )

                                val newEntry =
                                    Pickerl(
                                        vehicle =
                                            activeVehicle,
                                        lastDate =
                                            lastDate,
                                        nextDate =
                                            nextDate,
                                        notes =
                                            notes.trim(),
                                        reminder =
                                            reminder,
                                        photoUri =
                                            photoPath,
                                        reminderMonths =
                                            months
                                    )

                                val newList =
                                    store.loadPickerl()
                                        .filterNot {
                                            it.vehicle ==
                                                activeVehicle
                                        } +
                                        newEntry

                                store.savePickerl(
                                    newList
                                )

                                if (
                                    reminder
                                ) {

                                    schedulePickerlReminder(
                                        context,
                                        activeVehicle,
                                        nextDate,
                                        months
                                    )

                                } else {

                                    cancelPickerlReminder(
                                        context,
                                        activeVehicle
                                    )
                                }

                                saved = true
                                editing = false
                            },
                            onCancel = {

                                if (
                                    saved
                                ) {
                                    editing = false
                                }
                            },
                            showCancel =
                                saved
                        )

                    } else {

                        PickerlSummaryCard(
                            vehicle =
                                activeVehicle,
                            lastMonth =
                                lastMonth,
                            lastYear =
                                lastYear,
                            nextMonth =
                                nextMonth,
                            nextYear =
                                nextYear,
                            reminder =
                                reminder,
                            reminderMonths =
                                reminderMonths,
                            notes =
                                notes,
                            photoBitmap =
                                photoBitmap,
                            onEdit = {
                                editing = true
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PickerlEditCard(
    lastMonth: Int,
    lastYear: String,
    nextMonth: Int,
    nextYear: String,
    notes: String,
    reminder: Boolean,
    reminderMonths: Int,
    reminderMenuExpanded: Boolean,
    photoBitmap: Bitmap?,
    onLastMonth: (Int) -> Unit,
    onLastYear: (String) -> Unit,
    onNextMonth: (Int) -> Unit,
    onNextYear: (String) -> Unit,
    onNotes: (String) -> Unit,
    onReminder: (Boolean) -> Unit,
    onReminderMonths: (Int) -> Unit,
    onReminderMenu: (Boolean) -> Unit,
    onGallery: () -> Unit,
    onCamera: () -> Unit,
    onRemovePhoto: () -> Unit,
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
                    "Pickerl / TÜV",
                color =
                    Color.White,
                fontWeight =
                    FontWeight.Bold,
                fontSize =
                    22.sp
            )

            PickerlMonthYear(
                title =
                    "Letzter Termin",
                month =
                    lastMonth,
                year =
                    lastYear,
                onMonthChanged =
                    onLastMonth,
                onYearChanged =
                    onLastYear
            )

            Spacer(
                modifier =
                    Modifier.height(6.dp)
            )

            PickerlMonthYear(
                title =
                    "Nächster Termin",
                month =
                    nextMonth,
                year =
                    nextYear,
                onMonthChanged =
                    onNextMonth,
                onYearChanged =
                    onNextYear
            )

            Text(
                text =
                    "Nächster Termin: ${
                        pickerlMonthYear(
                            nextMonth,
                            nextYear
                        )
                    }",
                color =
                    Color.White,
                fontWeight =
                    FontWeight.Bold
            )

            Spacer(
                modifier =
                    Modifier.height(6.dp)
            )

            Text(
                text =
                    "Pickerl-Foto",
                color =
                    Color.White,
                fontWeight =
                    FontWeight.Bold
            )

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
                        Modifier.weight(1f),
                    onClick =
                        onGallery
                ) {

                    Icon(
                        imageVector =
                            Icons.Default.PhotoLibrary,
                        contentDescription =
                            "Galerie"
                    )

                    Spacer(
                        modifier =
                            Modifier.size(6.dp)
                    )

                    Text(
                        "Galerie"
                    )
                }

                OutlinedButton(
                    modifier =
                        Modifier.weight(1f),
                    onClick =
                        onCamera
                ) {

                    Icon(
                        imageVector =
                            Icons.Default.PhotoCamera,
                        contentDescription =
                            "Kamera"
                    )

                    Spacer(
                        modifier =
                            Modifier.size(6.dp)
                    )

                    Text(
                        "Kamera"
                    )
                }
            }

            if (
                photoBitmap != null
            ) {

                Image(
                    bitmap =
                        photoBitmap.asImageBitmap(),
                    contentDescription =
                        "Pickerl Foto",
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                    contentScale =
                        ContentScale.Crop
                )

                TextButton(
                    modifier =
                        Modifier.align(
                            Alignment.End
                        ),
                    onClick =
                        onRemovePhoto
                ) {

                    Text(
                        "Foto entfernen"
                    )
                }
            }

            OutlinedTextField(
                modifier =
                    Modifier.fillMaxWidth(),
                value =
                    notes,
                onValueChange =
                    onNotes,
                label = {
                    Text(
                        "Notizen"
                    )
                }
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
                        reminder,
                    onCheckedChange =
                        onReminder
                )
            }

            if (
                reminder
            ) {

                Text(
                    text =
                        "Erinnerung vor Ablauf",
                    color =
                        Color.White
                )

                Box {

                    OutlinedButton(
                        onClick = {
                            onReminderMenu(
                                true
                            )
                        }
                    ) {

                        Text(
                            "$reminderMonths " +
                                if (
                                    reminderMonths == 1
                                ) {
                                    "Monat vorher"
                                } else {
                                    "Monate vorher"
                                }
                        )
                    }

                    DropdownMenu(
                        expanded =
                            reminderMenuExpanded,
                        onDismissRequest = {
                            onReminderMenu(
                                false
                            )
                        }
                    ) {

                        (1..5).forEach {
                            months ->

                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "$months " +
                                            if (
                                                months == 1
                                            ) {
                                                "Monat vorher"
                                            } else {
                                                "Monate vorher"
                                            }
                                    )
                                },
                                onClick = {

                                    onReminderMonths(
                                        months
                                    )

                                    onReminderMenu(
                                        false
                                    )
                                }
                            )
                        }
                    }
                }
            }

            Spacer(
                modifier =
                    Modifier.height(6.dp)
            )

            Button(
                modifier =
                    Modifier.fillMaxWidth(),
                onClick =
                    onSave
            ) {

                Text(
                    "Pickerl speichern"
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
private fun PickerlSummaryCard(
    vehicle: String,
    lastMonth: Int,
    lastYear: String,
    nextMonth: Int,
    nextYear: String,
    reminder: Boolean,
    reminderMonths: Int,
    notes: String,
    photoBitmap: Bitmap?,
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
                    "Gespeichertes Pickerl",
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
                    "Letzter Termin: ${
                        pickerlMonthYear(
                            lastMonth,
                            lastYear
                        )
                    }",
                color =
                    Color.White
            )

            Text(
                text =
                    "Nächster Termin: ${
                        pickerlMonthYear(
                            nextMonth,
                            nextYear
                        )
                    }",
                color =
                    Color.White,
                fontWeight =
                    FontWeight.Bold,
                fontSize =
                    18.sp
            )

            Text(
                text =
                    if (
                        photoBitmap != null
                    ) {
                        "Pickerl-Foto: gespeichert"
                    } else {
                        "Pickerl-Foto: kein Foto"
                    },
                color =
                    Color.White
            )

            Text(
                text =
                    if (
                        reminder
                    ) {
                        "Erinnerung: $reminderMonths " +
                            if (
                                reminderMonths == 1
                            ) {
                                "Monat vorher"
                            } else {
                                "Monate vorher"
                            }
                    } else {
                        "Erinnerung: ausgeschaltet"
                    },
                color =
                    Color.White
            )

            if (
                notes.isNotBlank()
            ) {

                Text(
                    text =
                        "Notizen: $notes",
                    color =
                        Color.White
                )
            }

            if (
                photoBitmap != null
            ) {

                Spacer(
                    modifier =
                        Modifier.height(4.dp)
                )

                Image(
                    bitmap =
                        photoBitmap.asImageBitmap(),
                    contentDescription =
                        "Gespeichertes Pickerl-Foto",
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                    contentScale =
                        ContentScale.Crop
                )
            }

            Spacer(
                modifier =
                    Modifier.height(6.dp)
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
                    "Die Daten sind lokal auf diesem Gerät gespeichert.",
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
