package de.autocheck.app

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

private val pickerlIsoFormatter =
    DateTimeFormatter.ofPattern("yyyy-MM-dd")

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

private fun monthName(month: Int): String {
    return pickerlMonths.getOrElse(month - 1) {
        "JANUAR"
    }
}

private fun parsePickerlDate(
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
    } catch (_: DateTimeParseException) {
        try {
            LocalDate.parse(text)
        } catch (_: Exception) {
            null
        }
    }
}

private fun createStorageDate(
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

    return "%04d-%02d-01".format(
        Locale.US,
        numericYear,
        month.coerceIn(1, 12)
    )
}

private fun monthYearText(
    month: Int,
    year: String
): String {

    if (year.length != 4) {
        return ""
    }

    return "${monthName(month)} $year"
}

private fun copyPickerlImage(
    context: android.content.Context,
    uri: Uri
): String? {

    return try {

        val extension =
            when (
                context.contentResolver.getType(uri)
            ) {
                "image/png" -> "png"
                "image/webp" -> "webp"
                else -> "jpg"
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

                        input.copyTo(output)
                    }
            }
            ?: return null

        file.absolutePath

    } catch (_: Exception) {

        null
    }
}

private fun saveCameraBitmap(
    context: android.content.Context,
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
private fun MonthYearRow(
    title: String,
    month: Int,
    year: String,
    onMonth: (Int) -> Unit,
    onYear: (String) -> Unit
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
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement =
            Arrangement.spacedBy(8.dp),
        verticalAlignment =
            Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier.weight(1f)
        ) {

            OutlinedButton(
                modifier =
                    Modifier.fillMaxWidth(),
                onClick = {
                    expanded = true
                }
            ) {

                Text(
                    monthName(month)
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = {
                    expanded = false
                }
            ) {

                pickerlMonths.forEachIndexed {
                        index,
                        name ->

                    DropdownMenuItem(
                        text = {
                            Text(name)
                        },
                        onClick = {

                            onMonth(
                                index + 1
                            )

                            expanded = false
                        }
                    )
                }
            }
        }

        OutlinedTextField(
            value = year,
            onValueChange = { value ->

                if (
                    value.length <= 4 &&
                    value.all(Char::isDigit)
                ) {

                    onYear(value)
                }
            },
            label = {
                Text("Jahr")
            },
            singleLine = true,
            keyboardOptions =
                KeyboardOptions(
                    keyboardType =
                        KeyboardType.Number
                ),
            modifier =
                Modifier.weight(1f)
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

    var photoUri by remember {
        mutableStateOf("")
    }

    var photoBitmap by remember {
        mutableStateOf<Bitmap?>(null)
    }

    var hasSavedPickerl by remember {
        mutableStateOf(false)
    }

    var editMode by remember {
        mutableStateOf(false)
    }

    var reminderMenuExpanded by remember {
        mutableStateOf(false)
    }

    val all =
        remember {
            mutableStateOf(
                store.loadPickerl()
            )
        }

    LaunchedEffect(
        activeVehicle
    ) {

        val entry =
            all.value.firstOrNull {
                it.vehicle == activeVehicle
            }

        if (entry == null) {

            lastMonth =
                1

            lastYear =
                currentYear

            nextMonth =
                1

            nextYear =
                currentYear

            notes =
                ""

            reminder =
                true

            reminderMonths =
                3

            photoUri =
                ""

            hasSavedPickerl =
                false

            editMode =
                true

            return@LaunchedEffect
        }

        parsePickerlDate(
            entry.lastDate
        )?.let { date ->

            lastMonth =
                date.monthValue

            lastYear =
                date.year.toString()
        }

        parsePickerlDate(
            entry.nextDate
        )?.let { date ->

            nextMonth =
                date.monthValue

            nextYear =
                date.year.toString()
        }

        notes =
            entry.notes

        reminder =
            entry.reminder

        reminderMonths =
            entry.reminderMonths
                .coerceIn(
                    1,
                    5
                )

        photoUri =
            entry.photoUri

        hasSavedPickerl =
            true

        editMode =
            false
    }

    LaunchedEffect(
        photoUri
    ) {

        photoBitmap =
            if (
                photoUri.isBlank()
            ) {
                null
            } else {
                try {
                    BitmapFactory.decodeFile(
                        photoUri
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

            if (uri != null) {

                val savedPath =
                    copyPickerlImage(
                        context,
                        uri
                    )

                if (
                    savedPath != null
                ) {

                    photoUri =
                        savedPath
                }
            }
        }

    val cameraLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.TakePicturePreview()
        ) { bitmap ->

            if (bitmap != null) {

                val savedPath =
                    saveCameraBitmap(
                        context,
                        bitmap
                    )

                if (
                    savedPath != null
                ) {

                    photoUri =
                        savedPath
                }
            }
        }

    Column(
        modifier =
            Modifier.fillMaxSize()
    ) {

        VehicleSelector(
            vehicles,
            activeVehicle,
            onActiveVehicle
        )

        Spacer(
            modifier =
                Modifier.height(10.dp)
        )

        if (
            vehicles.isEmpty()
        ) {

            EmptyCard(
                "Bitte zuerst ein Fahrzeug anlegen."
            )

            return@Column
        }

        LazyColumn(
            modifier =
                Modifier.weight(1f),
            verticalArrangement =
                Arrangement.spacedBy(10.dp)
        ) {

            if (
                !hasSavedPickerl ||
                editMode
            ) {

                item {

                    CardForm {

                        Text(
                            text =
                                if (
                                    hasSavedPickerl
                                ) {
                                    "Pickerl / TÜV ändern"
                                } else {
                                    "Pickerl / TÜV"
                                },
                            color =
                                Color.White,
                            fontWeight =
                                FontWeight.Bold,
                            fontSize =
                                20.sp
                        )

                        Spacer(
                            modifier =
                                Modifier.height(10.dp)
                        )

                        MonthYearRow(
                            title =
                                "Letzter Termin",
                            month =
                                lastMonth,
                            year =
                                lastYear,
                            onMonth = {
                                lastMonth = it
                            },
                            onYear = {
                                lastYear = it
                            }
                        )

                        Spacer(
                            modifier =
                                Modifier.height(14.dp)
                        )

                        MonthYearRow(
                            title =
                                "Nächster Termin",
                            month =
                                nextMonth,
                            year =
                                nextYear,
                            onMonth = {
                                nextMonth = it
                            },
                            onYear = {
                                nextYear = it
                            }
                        )

                        Spacer(
                            modifier =
                                Modifier.height(6.dp)
                        )

                        Text(
                            text =
                                "Nächster Termin: ${
                                    monthYearText(
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
                                Modifier.height(12.dp)
                        )

                        Text(
                            text =
                                "Pickerl-Foto",
                            color =
                                Color.White,
                            fontWeight =
                                FontWeight.Bold,
                            fontSize =
                                16.sp
                        )

                        Spacer(
                            modifier =
                                Modifier.height(6.dp)
                        )

                        Row(
                            modifier =
                                Modifier.fillMaxWidth(),
                            horizontalArrangement =
                                Arrangement.spacedBy(8.dp)
                        ) {

                            OutlinedButton(
                                modifier =
                                    Modifier.weight(1f),
                                onClick = {
                                    galleryLauncher.launch(
                                        "image/*"
                                    )
                                }
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
                                onClick = {
                                    cameraLauncher.launch(
                                        null
                                    )
                                }
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

                            Spacer(
                                modifier =
                                    Modifier.height(10.dp)
                            )

                            Card(
                                modifier =
                                    Modifier.fillMaxWidth()
                            ) {

                                Image(
                                    bitmap =
                                        photoBitmap!!
                                            .asImageBitmap(),
                                    contentDescription =
                                        "Pickerl Foto",
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
                                    Modifier.height(4.dp)
                            )

                            TextButton(
                                modifier =
                                    Modifier.align(
                                        Alignment.End
                                    ),
                                onClick = {

                                    photoUri =
                                        ""

                                    photoBitmap =
                                        null
                                }
                            ) {

                                Text(
                                    "Foto entfernen"
                                )
                            }
                        }

                        FormField(
                            "Notizen",
                            notes
                        ) {
                            notes = it
                        }

                        Spacer(
                            modifier =
                                Modifier.height(8.dp)
                        )

                        Row(
                            modifier =
                                Modifier.fillMaxWidth(),
                            verticalAlignment =
                                Alignment.CenterVertically,
                            horizontalArrangement =
                                Arrangement.SpaceBetween
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
                                onCheckedChange = {
                                    reminder = it
                                }
                            )
                        }

                        if (
                            reminder
                        ) {

                            Spacer(
                                modifier =
                                    Modifier.height(6.dp)
                            )

                            Text(
                                text =
                                    "Erinnerung vor Ablauf",
                                color =
                                    Color.White
                            )

                            Spacer(
                                modifier =
                                    Modifier.height(4.dp)
                            )

                            Box {

                                OutlinedButton(
                                    onClick = {
                                        reminderMenuExpanded =
                                            true
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
                                        reminderMenuExpanded =
                                            false
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

                                                reminderMonths =
                                                    months

                                                reminderMenuExpanded =
                                                    false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(
                            modifier =
                                Modifier.height(14.dp)
                        )

                        Button(
                            modifier =
                                Modifier.fillMaxWidth(),
                            onClick = {

                                val storageLastDate =
                                    createStorageDate(
                                        lastMonth,
                                        lastYear
                                    )

                                val storageNextDate =
                                    createStorageDate(
                                        nextMonth,
                                        nextYear
                                    )

                                if (
                                    storageLastDate.isBlank() ||
                                    storageNextDate.isBlank()
                                ) {
                                    return@Button
                                }

                                val selectedMonths =
                                    reminderMonths
                                        .coerceIn(
                                            1,
                                            5
                                        )

                                val entry =
                                    Pickerl(
                                        vehicle =
                                            activeVehicle,
                                        lastDate =
                                            storageLastDate,
                                        nextDate =
                                            storageNextDate,
                                        notes =
                                            notes.trim(),
                                        reminder =
                                            reminder,
                                        photoUri =
                                            photoUri,
                                        reminderMonths =
                                            selectedMonths
                                    )

                                val updated
