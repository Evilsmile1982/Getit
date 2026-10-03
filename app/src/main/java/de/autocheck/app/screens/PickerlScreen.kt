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
import androidx.compose.foundation.layout.padding
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

private fun monthNumber(
    monthName: String
): Int {

    val index =
        pickerlMonths.indexOf(
            monthName.uppercase(
                Locale.GERMAN
            )
        )

    return if (
        index >= 0
    ) {
        index + 1
    } else {
        1
    }
}

private fun monthName(
    monthNumber: Int
): String {

    return pickerlMonths.getOrElse(
        monthNumber - 1
    ) {
        "JANUAR"
    }
}

private fun parseStoredPickerlDate(
    value: String
): LocalDate? {

    val trimmed =
        value.trim()

    if (
        trimmed.isBlank()
    ) {
        return null
    }

    return try {

        LocalDate.parse(
            trimmed,
            pickerlIsoFormatter
        )

    } catch (_: DateTimeParseException) {

        try {

            LocalDate.parse(
                trimmed
            )

        } catch (_: Exception) {

            null
        }
    }
}

private fun createPickerlStorageDate(
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
        numericYear < 1900 ||
        numericYear > 2200
    ) {
        return ""
    }

    return "%04d-%02d-01".format(
        Locale.US,
        numericYear,
        month
    )
}

private fun displayPickerlMonthYear(
    month: Int,
    year: String
): String {

    if (
        year.length != 4
    ) {
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
                context.contentResolver
                    .getType(uri)
            ) {

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

    var lastMonthMenuExpanded by remember {
        mutableStateOf(false)
    }

    var nextMonthMenuExpanded by remember {
        mutableStateOf(false)
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

    var saved by remember {
        mutableStateOf(false)
    }

    var savedPickerl by remember {
        mutableStateOf<Pickerl?>(null)
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

    var photoBitmap by remember {
        mutableStateOf<Bitmap?>(null)
    }

    LaunchedEffect(
        photoUri
    ) {

        photoBitmap =
            null

        if (
            photoUri.isBlank()
        ) {
            return@LaunchedEffect
        }

        photoBitmap =
            try {

                BitmapFactory.decodeFile(
                    photoUri
                )

            } catch (_: Exception) {

                null
            }
    }

    val galleryLauncher =
        rememberLauncherForActivityResult(
            contract =
                ActivityResultContracts.GetContent()
        ) { uri ->

            if (
                uri != null
            ) {

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
            contract =
                ActivityResultContracts.TakePicturePreview()
        ) { bitmap ->

            if (
                bitmap != null
            ) {

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

    LaunchedEffect(
        activeVehicle
    ) {

        val entry =
            all.value.firstOrNull {
                it.vehicle ==
                    activeVehicle
            }

        if (
            entry == null
        ) {

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

            saved =
                false

            savedPickerl =
                null

            return@LaunchedEffect
        }

        val storedLastDate =
            parseStoredPickerlDate(
                entry.lastDate
            )

        val storedNextDate =
            parseStoredPickerlDate(
                entry.nextDate
            )

        if (
            storedLastDate != null
        ) {

            lastMonth =
                storedLastDate.monthValue

            lastYear =
                storedLastDate.year.toString()
        }

        if (
            storedNextDate != null
        ) {

            nextMonth =
                storedNextDate.monthValue

            nextYear =
                storedNextDate.year.toString()
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

        saved =
            true

        savedPickerl =
            entry
    }

    Column(
        Modifier.fillMaxSize()
    ) {

        VehicleSelector(
            vehicles,
            activeVehicle,
            onActiveVehicle
        )

        Spacer(
            Modifier.height(
                10.dp
            )
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
                Modifier.weight(
                    1f
                ),

            verticalArrangement =
                Arrangement.spacedBy(
                    10.dp
                )
        ) {

            item {

                CardForm {

                    Text(

                        text =
                            "Pickerl / TÜV",

                        color =
                            Color.White,

                        fontWeight =
                            FontWeight.Bold,

                        fontSize =
                            20.sp
                    )

                    Spacer(
                        Modifier.height(
                            8.dp
                        )
                    )

                    Text(

                        text =
                            "Letzter Termin",

                        color =
                            Color.White,

                        fontWeight =
                            FontWeight.Bold,

                        fontSize =
                            16.sp
                    )

                    Spacer(
                        Modifier.height(
                            6.dp
                        )
                    )

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

                                    lastMonthMenuExpanded =
                                        true
                                }

                            ) {

                                Text(
                                    monthName(
                                        lastMonth
                                    )
                                )
                            }

                            DropdownMenu(

                                expanded =
                                    lastMonthMenuExpanded,

                                onDismissRequest = {

                                    lastMonthMenuExpanded =
                                        false
                                }

                            ) {

                                pickerlMonths.forEachIndexed {
                                        index,
                                        month ->

                                    DropdownMenuItem(

                                        text = {

                                            Text(
                                                month
                                            )
                                        },

                                        onClick = {

                                            lastMonth =
                                                index + 1

                                            lastMonthMenuExpanded =
                                                false
                                        }
                                    )
                                }
                            }
                        }

                        OutlinedTextField(

                            value =
                                lastYear,

                            onValueChange = {

                                if (
                                    it.length <= 4 &&
                                    it.all {
                                        char ->
                                        char.isDigit()
                                    }
                                ) {

                                    lastYear =
                                        it
                                }
                            },

                            label = {
                                Text(
                                    "Jahr"
                                )
                            },

                            singleLine =
                                true,

                            keyboardOptions =
                                KeyboardOptions(
                                    keyboardType =
                                        KeyboardType.Number
                                ),

                            modifier =
                                Modifier.weight(
                                    1f
                                )
                        )
                    }

                    Spacer(
                        Modifier.height(
                            14.dp
                        )
                    )

                    Text(

                        text =
                            "Nächster Termin",

                        color =
                            Color.White,

                        fontWeight =
                            FontWeight.Bold,

                        fontSize =
                            16.sp
                    )

                    Spacer(
                        Modifier.height(
                            6.dp
                        )
                    )

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

                                    nextMonthMenuExpanded =
                                        true
                                }

                            ) {

                                Text(
                                    monthName(
                                        nextMonth
                                    )
                                )
                            }

                            DropdownMenu(

                                expanded =
                                    nextMonthMenuExpanded,

                                onDismissRequest = {

                                    nextMonthMenuExpanded =
                                        false
                                }

                            ) {

                                pickerlMonths.forEachIndexed {
                                        index,
                                        month ->

                                    DropdownMenuItem(

                                        text = {

                                            Text(
                                                month
                                            )
                                        },

                                        onClick = {

                                            nextMonth =
                                                index + 1

                                            nextMonthMenuExpanded =
                                                false
                                        }
                                    )
                                }
                            }
                        }

                        OutlinedTextField(

                            value =
                                nextYear,

                            onValueChange = {

                                if (
                                    it.length <= 4 &&
                                    it.all {
                                        char ->
                                        char.isDigit()
                                    }
                                ) {

                                    nextYear =
                                        it
                                }
                            },

                            label = {
                                Text(
                                    "Jahr"
                                )
                            },

                            singleLine =
                                true,

                            keyboardOptions =
                                KeyboardOptions(
                                    keyboardType =
                                        KeyboardType.Number
                                ),

                            modifier =
                                Modifier.weight(
                                    1f
                                )
                        )
                    }

                    Spacer(
                        Modifier.height(
                            6.dp
                        )
                    )

                    Text(

                        text =
                            "Nächster Termin: ${
                                displayPickerlMonthYear(
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
                        Modifier.height(
                            10.dp
                        )
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
                        Modifier.height(
                            6.dp
                        )
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
                                Modifier.weight(
                                    1f
                                ),

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
                                Modifier.size(
                                    6.dp
                                )
                            )

                            Text(
                                "Galerie"
                            )
                        }

                        OutlinedButton(

                            modifier =
                                Modifier.weight(
                                    1f
                                ),

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
                                Modifier.size(
                                    6.dp
                                )
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
                            Modifier.height(
                                10.dp
                            )
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
                                        .height(
                                            220.dp
                                        ),

                                contentScale =
                                    ContentScale.Crop
                            )
                        }

                        Spacer(
                            Modifier.height(
                                4.dp
                            )
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

                        notes =
                            it
                    }

                    Spacer(
                        Modifier.height(
                            6.dp
                        )
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

                                reminder =
                                    it
                            }
                        )
                    }

                    if (
                        reminder
                    ) {

                        Spacer(
                            Modifier.height(
                                6.dp
                            )
                        )

                        Text(

                            text =
                                "Erinnerung vor Ablauf",

                            color =
                                Color.White
                        )

                        Spacer(
                            Modifier.height(
                                4.dp
                            )
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
                        Modifier.height(
                            12.dp
                        )
                    )

                    Button(

                        modifier =
                            Modifier.fillMaxWidth(),

                        onClick = {

                            val storageLastDate =
                                createPickerlStorageDate(
                                    lastMonth,
                                    lastYear
                                )

                            val storageNextDate =
                                createPickerlStorageDate(
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

                            val updated =
                                all.value
                                    .filterNot {
                                        it.vehicle ==
                                            activeVehicle
                                    } +
                                    entry

                            all.value =
                                updated

                            store.savePickerl(
                                updated
                            )

                            savedPickerl =
                                entry

                            saved =
                                true

                            if (
                                reminder
                            ) {

                                schedulePickerlReminder(

                                    context,

                                    activeVehicle,

                                    storageNextDate,

                                    selectedMonths
                                )

                            } else {

                                cancelPickerlReminder(

                                    context,

                                    activeVehicle
                                )
                            }
                        }

                    ) {

                        Text(
                            "Pickerl speichern"
                        )
                    }
                }
            }

            if (
                saved &&
                savedPickerl != null
            ) {

                item {

                    CardForm {

                        Text(

                            text =
                                "Gespeichert",

                            color =
                                Color.White,

                            fontWeight =
                                FontWeight.Bold,

                            fontSize =
                                20.sp
                        )

                        Spacer(
                            Modifier.height(
                                8.dp
                            )
                        )

                        Text(

                            text =
                                "Fahrzeug: $activeVehicle",

                            color =
                                Color.White
                        )

                        Spacer(
                            Modifier.height(
                                4.dp
                            )
                        )

                        Text(

                            text =
                                "Letzter Termin: ${
                                    displayPickerlMonthYear(
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
                                    displayPickerlMonthYear(
                                        nextMonth,
                                        nextYear
                                    )
                                }",

                            color =
                                Color.White,

                            fontWeight =
                                FontWeight.Bold
                        )

                        Text(

                            text =
                                if (
                                    photoUri.isNotBlank()
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

                            Spacer(
                                Modifier.height(
                                    6.dp
                                )
                            )

                            Text(

                                text =
                                    "Notizen: $notes",

                                color =
                                    Color.White
                            )
                        }

                        Spacer(
                            Modifier.height(
                                10.dp
                            )
                        )

                        if (
                            photoBitmap != null
                        ) {

                            Image(

                                bitmap =
                                    photoBitmap!!
                                        .asImageBitmap(),

                                contentDescription =
                                    "Gespeichertes Pickerl-Foto",

                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .height(
                                            180.dp
                                        ),

                                contentScale =
                                    ContentScale.Crop
                            )
                        }

                        Spacer(
                            Modifier.height(
                                8.dp
                            )
                        )

                        Text(

                            text =
                                "Die Daten wurden lokal auf diesem Gerät gespeichert.",

                            color =
                                Muted
                        )
                    }
                }
            }
        }
    }
}
