package de.autocheck.app

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

private val pickerlIsoFormatter =
    DateTimeFormatter.ofPattern(
        "yyyy-MM-dd"
    )

private val pickerlEuropeanFormatter =
    DateTimeFormatter.ofPattern(
        "dd.MM.yyyy"
    )

private fun pickerlToDisplayDate(
    value: String
): String {

    val trimmed =
        value.trim()

    if (trimmed.isBlank()) {
        return ""
    }

    return try {

        LocalDate
            .parse(
                trimmed,
                pickerlIsoFormatter
            )
            .format(
                pickerlEuropeanFormatter
            )

    } catch (_: DateTimeParseException) {

        try {

            LocalDate
                .parse(
                    trimmed,
                    pickerlEuropeanFormatter
                )
                .format(
                    pickerlEuropeanFormatter
                )

        } catch (_: DateTimeParseException) {

            trimmed
        }
    }
}

private fun pickerlToStorageDate(
    value: String
): String {

    val trimmed =
        value.trim()

    if (trimmed.isBlank()) {
        return ""
    }

    return try {

        LocalDate
            .parse(
                trimmed,
                pickerlEuropeanFormatter
            )
            .format(
                pickerlIsoFormatter
            )

    } catch (_: DateTimeParseException) {

        try {

            LocalDate
                .parse(
                    trimmed,
                    pickerlIsoFormatter
                )
                .format(
                    pickerlIsoFormatter
                )

        } catch (_: DateTimeParseException) {

            trimmed
        }
    }
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

    var lastDate by remember {
        mutableStateOf("")
    }

    var nextDate by remember {
        mutableStateOf("")
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
        mutableStateOf<android.graphics.Bitmap?>(
            null
        )
    }

    LaunchedEffect(
        photoUri
    ) {

        photoBitmap = null

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

        lastDate =
            pickerlToDisplayDate(
                entry?.lastDate
                    ?: ""
            )

        nextDate =
            pickerlToDisplayDate(
                entry?.nextDate
                    ?: ""
            )

        notes =
            entry?.notes
                ?: ""

        reminder =
            entry?.reminder
                ?: true

        reminderMonths =
            entry?.reminderMonths
                ?.coerceIn(
                    1,
                    5
                )
                ?: 3

        photoUri =
            entry?.photoUri
                ?: ""

        saved =
            entry != null
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
                            4.dp
                        )
                    )

                    FormField(

                        "Letzte Prüfung (TT.MM.JJJJ)",

                        lastDate

                    ) {

                        lastDate =
                            it
                    }

                    FormField(

                        "Nächste Prüfung (TT.MM.JJJJ)",

                        nextDate

                    ) {

                        nextDate =
                            it
                    }

                    Spacer(
                        Modifier.height(
                            6.dp
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

                                (1..5).forEach { months ->

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
                            8.dp
                        )
                    )

                    Button(

                        modifier =
                            Modifier.fillMaxWidth(),

                        onClick = {

                            val storageLastDate =
                                pickerlToStorageDate(
                                    lastDate
                                )

                            val storageNextDate =
                                pickerlToStorageDate(
                                    nextDate
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
                                        reminderMonths
                                            .coerceIn(
                                                1,
                                                5
                                            )
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

                            /*
                             * Die eigentliche Erinnerung wird
                             * im nächsten Schritt an die neue
                             * Auswahl 1–5 Monate angepasst.
                             *
                             * Bis dahin bleibt der bestehende
                             * Reminder-Aufruf kompatibel.
                             */

                            if (
                                reminder &&
                                storageNextDate.isNotBlank()
                            ) {

                                schedulePickerlReminder(

                                    context,

                                    activeVehicle,

                                    storageNextDate
                                )

                            } else {

                                cancelPickerlReminder(

                                    context,

                                    activeVehicle
                                )
                            }

                            saved =
                                true
                        }

                    ) {

                        Text(

                            if (
                                saved
                            ) {
                                "Pickerl gespeichert"
                            } else {
                                "Pickerl speichern"
                            }
                        )
                    }
                }
            }
        }
    }
}
