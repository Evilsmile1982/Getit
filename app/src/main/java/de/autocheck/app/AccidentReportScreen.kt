package de.autocheck.app

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Environment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val AccidentGold = Color(0xFFD4AF37)
private val AccidentDark = Color(0xFF101010)

private data class ShortAccidentField(
    val id: String,
    val label: String,
    val multiline: Boolean = false
)

private data class ShortAccidentSection(
    val title: String,
    val fields: List<ShortAccidentField>
)

private data class AccidentPhotoCategory(
    val id: String,
    val title: String
)

private fun accidentField(
    id: String,
    label: String,
    multiline: Boolean = false
) = ShortAccidentField(id, label, multiline)

private val shortAccidentSections = listOf(
    ShortAccidentSection(
        "1. Unfall",
        listOf(
            accidentField("date", "Unfalldatum"),
            accidentField("time", "Uhrzeit"),
            accidentField("location", "Unfallort"),
            accidentField("street", "Straße / Kreuzung"),
            accidentField("accident_type", "Unfallart"),
            accidentField("weather", "Wetter / Straßenverhältnisse")
        )
    ),
    ShortAccidentSection(
        "2. Beteiligter A",
        listOf(
            accidentField("a_name", "Vor- und Nachname"),
            accidentField("a_address", "Adresse"),
            accidentField("a_phone", "Telefon"),
            accidentField("a_vehicle", "Fahrzeug / Marke / Modell"),
            accidentField("a_plate", "Kennzeichen / Zulassungsland"),
            accidentField("a_insurance", "Versicherung"),
            accidentField("a_policy", "Polizzennummer")
        )
    ),
    ShortAccidentSection(
        "Beteiligter B",
        listOf(
            accidentField("b_name", "Vor- und Nachname"),
            accidentField("b_address", "Adresse"),
            accidentField("b_phone", "Telefon"),
            accidentField("b_vehicle", "Fahrzeug / Marke / Modell"),
            accidentField("b_plate", "Kennzeichen / Zulassungsland"),
            accidentField("b_insurance", "Versicherung"),
            accidentField("b_policy", "Polizzennummer")
        )
    ),
    ShortAccidentSection(
        "3. Unfallhergang und Schäden",
        listOf(
            accidentField("description", "Wie ist der Unfall passiert?", true),
            accidentField("a_direction", "Fahrtrichtung A"),
            accidentField("b_direction", "Fahrtrichtung B"),
            accidentField("damage_a", "Schäden Fahrzeug A", true),
            accidentField("damage_b", "Schäden Fahrzeug B", true),
            accidentField("other_damage", "Sonstige Schäden", true),
            accidentField("injuries", "Verletzte Personen? Anzahl / Angaben"),
            accidentField("emergency", "Rettung oder Polizei verständigt?")
        )
    ),
    ShortAccidentSection(
        "4. Weitere Angaben",
        listOf(
            accidentField("witnesses", "Zeugen – Name und Kontakt", true),
            accidentField("police", "Polizei – Dienststelle / Aktenzeichen"),
            accidentField("remarks", "Weitere Bemerkungen", true)
        )
    ),
    ShortAccidentSection(
        "5. Bestätigung",
        listOf(
            accidentField("signature_a", "Name Beteiligter A"),
            accidentField("signature_b", "Name Beteiligter B")
        )
    )
)

private val europeanCircumstancesShort = listOf(
    "Fahrzeug geparkt oder am Straßenrand",
    "Parkplatz verlassen oder Tür geöffnet",
    "In einen Parkplatz eingefahren",
    "Aus Parkplatz oder Grundstück gekommen",
    "In Grundstück oder Parkplatz eingefahren",
    "In einen Kreisverkehr eingefahren",
    "Im Kreisverkehr gefahren",
    "Auf ein Fahrzeug in gleicher Richtung aufgefahren",
    "Gleiche Richtung auf verschiedenen Spuren",
    "Fahrstreifen gewechselt",
    "Überholt",
    "Rechts abgebogen",
    "Links abgebogen",
    "Rückwärts gefahren",
    "Auf Gegenfahrbahn gefahren",
    "Fahrzeug kam von rechts",
    "Vorrangzeichen oder Rotlicht nicht beachtet"
)

private val participantPhotoCategories = listOf(
    AccidentPhotoCategory("id_a", "Ausweis / Führerschein – A"),
    AccidentPhotoCategory("insurance_a", "Versicherungsnachweis – A"),
    AccidentPhotoCategory("registration_a", "Zulassungsschein – A"),
    AccidentPhotoCategory("plate_a", "Kennzeichen – A"),
    AccidentPhotoCategory("id_b", "Ausweis / Führerschein – B"),
    AccidentPhotoCategory("insurance_b", "Versicherungsnachweis – B"),
    AccidentPhotoCategory("registration_b", "Zulassungsschein – B"),
    AccidentPhotoCategory("plate_b", "Kennzeichen – B")
)

private val damagePhotoCategories = listOf(
    AccidentPhotoCategory("vehicle_a", "Gesamtansichten Fahrzeug A"),
    AccidentPhotoCategory("vehicle_b", "Gesamtansichten Fahrzeug B"),
    AccidentPhotoCategory("damage_a", "Nahaufnahmen Schäden Fahrzeug A"),
    AccidentPhotoCategory("damage_b", "Nahaufnahmen Schäden Fahrzeug B"),
    AccidentPhotoCategory("other_damage", "Sonstige beschädigte Gegenstände"),
    AccidentPhotoCategory("scene", "Übersicht der Unfallstelle"),
    AccidentPhotoCategory("signs", "Verkehrszeichen und Ampeln"),
    AccidentPhotoCategory("markings", "Fahrbahnmarkierungen und Spuren")
)

@Composable
fun AccidentReportScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current

    val preferences = remember {
        context.getSharedPreferences(
            "carloca_accident_report",
            Context.MODE_PRIVATE
        )
    }

    val values = remember {
        mutableStateMapOf<String, String>().apply {
            val saved = preferences.getString("report_data", null)

            if (!saved.isNullOrBlank()) {
                try {
                    val json = JSONObject(saved)
                    json.keys().forEach { key ->
                        put(key, json.optString(key, ""))
                    }
                } catch (_: Exception) {
                    // Ungültige gespeicherte Daten ignorieren.
                }
            }
        }
    }

    val circumstancesA = remember {
        mutableStateMapOf<Int, Boolean>().apply {
            preferences.getString("circumstances_a", "")
                .orEmpty()
                .split(",")
                .forEach { item ->
                    item.toIntOrNull()?.let { put(it, true) }
                }
        }
    }

    val circumstancesB = remember {
        mutableStateMapOf<Int, Boolean>().apply {
            preferences.getString("circumstances_b", "")
                .orEmpty()
                .split(",")
                .forEach { item ->
                    item.toIntOrNull()?.let { put(it, true) }
                }
        }
    }

    val photos = remember {
        mutableStateMapOf<String, List<String>>().apply {
            val saved = preferences.getString("photo_data", null)

            if (!saved.isNullOrBlank()) {
                try {
                    val json = JSONObject(saved)
                    json.keys().forEach { key ->
                        val array = json.optJSONArray(key) ?: JSONArray()
                        val paths = mutableListOf<String>()

                        for (index in 0 until array.length()) {
                            val path = array.optString(index)
                            if (path.isNotBlank() && File(path).exists()) {
                                paths.add(path)
                            }
                        }

                        if (paths.isNotEmpty()) {
                            put(key, paths)
                        }
                    }
                } catch (_: Exception) {
                    // Ungültige Fotodaten ignorieren.
                }
            }
        }
    }

    var currentStep by remember { mutableStateOf(0) }
    var confirmedA by remember { mutableStateOf(false) }
    var confirmedB by remember { mutableStateOf(false) }
    var pdfMessage by remember { mutableStateOf("") }
    var generatedPdfPath by remember { mutableStateOf<String?>(null) }
    var selectedPhotoCategory by remember { mutableStateOf<String?>(null) }
    var photoMessage by remember { mutableStateOf("") }
    var showPhotoSourceDialog by remember { mutableStateOf(false) }
    var pendingCameraFile by remember { mutableStateOf<File?>(null) }

    fun savePhotos() {
        val json = JSONObject()

        photos.forEach { (category, paths) ->
            val array = JSONArray()
            paths.forEach { array.put(it) }
            json.put(category, array)
        }

        preferences.edit()
            .putString("photo_data", json.toString())
            .apply()
    }

    fun saveLocally() {
        val json = JSONObject()

        values.forEach { (key, value) ->
            json.put(key, value)
        }

        preferences.edit()
            .putString("report_data", json.toString())
            .putString(
                "circumstances_a",
                circumstancesA.filterValues { it }
                    .keys.sorted().joinToString(",")
            )
            .putString(
                "circumstances_b",
                circumstancesB.filterValues { it }
                    .keys.sorted().joinToString(",")
            )
            .apply()

        savePhotos()
    }

    fun addPhotoFile(file: File) {
        val category = selectedPhotoCategory ?: return

        if (file.exists() && file.length() > 0L) {
            val currentPaths = photos[category].orEmpty().toMutableList()
            currentPaths.add(file.absolutePath)
            photos[category] = currentPaths
            savePhotos()

            photoMessage = "Foto erfolgreich hinzugefügt."
        } else {
            photoMessage = "Das Foto konnte nicht gespeichert werden."
            file.delete()
        }

        selectedPhotoCategory = null
    }

    fun copySelectedPhotos(uris: List<Uri>) {
        val category = selectedPhotoCategory ?: return
        val destination = File(context.filesDir, "accident_photos")

        if (!destination.exists()) {
            destination.mkdirs()
        }

        val currentPaths = photos[category].orEmpty().toMutableList()
        var added = 0

        uris.forEach { uri ->
            try {
                val file = File(
                    destination,
                    "photo_${System.currentTimeMillis()}_${currentPaths.size}.jpg"
                )

                context.contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(file).use { output ->
                        input.copyTo(output)
                    }
                }

                if (file.exists() && file.length() > 0L) {
                    currentPaths.add(file.absolutePath)
                    added++
                } else {
                    file.delete()
                }
            } catch (_: Exception) {
                // Nicht lesbare Bilder überspringen.
            }
        }

        photos[category] = currentPaths
        savePhotos()

        photoMessage = if (added > 0) {
            "$added Foto(s) hinzugefügt."
        } else {
            "Die ausgewählten Fotos konnten nicht übernommen werden."
        }

        selectedPhotoCategory = null
    }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isNotEmpty()) {
            copySelectedPhotos(uris)
        } else {
            selectedPhotoCategory = null
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val file = pendingCameraFile

        if (success && file != null) {
            addPhotoFile(file)
        } else {
            file?.delete()
            pendingCameraFile = null
            selectedPhotoCategory = null
            photoMessage = "Die Kameraaufnahme wurde abgebrochen."
        }
    }

    fun openCamera() {
        val category = selectedPhotoCategory ?: return

        try {
            val directory = File(context.filesDir, "accident_photos")

            if (!directory.exists() && !directory.mkdirs()) {
                photoMessage = "Fotoordner konnte nicht erstellt werden."
                selectedPhotoCategory = null
                return
            }

            val file = File(
                directory,
                "camera_${System.currentTimeMillis()}.jpg"
            )

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            pendingCameraFile = file
            cameraLauncher.launch(uri)
        } catch (exception: Exception) {
            pendingCameraFile = null
            selectedPhotoCategory = null
            photoMessage = "Kamera konnte nicht geöffnet werden: " +
                (exception.message ?: "Unbekannter Fehler")
        }
    }

    if (showPhotoSourceDialog) {
        AlertDialog(
            onDismissRequest = {
                showPhotoSourceDialog = false
                selectedPhotoCategory = null
            },
            title = {
                Text("Foto hinzufügen")
            },
            text = {
                Text("Möchtest du ein neues Foto aufnehmen oder ein Bild aus deiner Galerie auswählen?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showPhotoSourceDialog = false
                        openCamera()
                    }
                ) {
                    Text("Kamera öffnen")
                }
            },
            dismissButton = {
                Row {
                    TextButton(
                        onClick = {
                            showPhotoSourceDialog = false
                            selectedPhotoCategory = null
                        }
                    ) {
                        Text("Abbrechen")
                    }

                    TextButton(
                        onClick = {
                            showPhotoSourceDialog = false
                            photoPicker.launch("image/*")
                        }
                    ) {
                        Text("Galerie")
                    }
                }
            }
        )
    }

    val steps = listOf(
        "Unfall",
        "Beteiligte",
        "Unfallhergang",
        "Weitere Angaben",
        "Bestätigung"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AccidentDark)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "CARLOCA",
            color = AccidentGold,
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = "SCHNELLUNFALLBERICHT",
            color = Color.White,
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Am Europäischen Unfallbericht orientierte Dokumentation",
            color = Color.LightGray,
            fontSize = 11.sp
        )

        Spacer(Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF29251A)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Bei Verletzten oder Gefahr zuerst Rettung " +
                    "und Polizei verständigen. Nur Tatsachen eintragen. " +
                    "Eine Unterschrift ist kein Schuldeingeständnis.",
                color = Color.White,
                modifier = Modifier.padding(12.dp),
                fontSize = 12.sp
            )
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = "SCHRITT ${currentStep + 1} VON 5",
            color = AccidentGold,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            repeat(5) { index ->
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (index <= currentStep) {
                            AccidentGold
                        } else {
                            Color.DarkGray
                        }
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {}
            }
        }

        Spacer(Modifier.height(16.dp))

        when (currentStep) {
            0 -> {
                ShortAccidentFormSection(
                    section = shortAccidentSections[0],
                    values = values,
                    onValueChange = { id, value ->
                        values[id] = value
                        saveLocally()
                    }
                )
            }

            1 -> {
                ShortAccidentFormSection(
                    section = shortAccidentSections[1],
                    values = values,
                    onValueChange = { id, value ->
                        values[id] = value
                        saveLocally()
                    }
                )

                Spacer(Modifier.height(12.dp))

                ShortAccidentFormSection(
                    section = shortAccidentSections[2],
                    values = values,
                    onValueChange = { id, value ->
                        values[id] = value
                        saveLocally()
                    }
                )

                Spacer(Modifier.height(16.dp))

                PhotoCategorySection(
                    title = "FOTOS ZU BETEILIGTEN",
                    categories = participantPhotoCategories,
                    photos = photos,
                    onAdd = { category ->
                        selectedPhotoCategory = category
                        showPhotoSourceDialog = true
                    },
                    onRemove = { category, path ->
                        val updated = photos[category].orEmpty() - path
                        photos[category] = updated
                        File(path).delete()
                        savePhotos()
                    }
                )
            }

            2 -> {
                ShortAccidentFormSection(
                    section = shortAccidentSections[3],
                    values = values,
                    onValueChange = { id, value ->
                        values[id] = value
                        saveLocally()
                    }
                )

                Spacer(Modifier.height(16.dp))

                PhotoCategorySection(
                    title = "FOTOS ZU UNFALL UND SCHÄDEN",
                    categories = damagePhotoCategories,
                    photos = photos,
                    onAdd = { category ->
                        selectedPhotoCategory = category
                        showPhotoSourceDialog = true
                    },
                    onRemove = { category, path ->
                        val updated = photos[category].orEmpty() - path
                        photos[category] = updated
                        File(path).delete()
                        savePhotos()
                    }
                )
            }

            3 -> {
                ShortAccidentFormSection(
                    section = shortAccidentSections[4],
                    values = values,
                    onValueChange = { id, value ->
                        values[id] = value
                        saveLocally()
                    }
                )

                Spacer(Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF1D1D1D)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "Europäische Unfallumstände",
                            color = AccidentGold,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "Nur zutreffende Umstände auswählen.",
                            color = Color.LightGray,
                            fontSize = 12.sp
                        )

                        Spacer(Modifier.height(8.dp))

                        Text(
                            text = "Fahrzeug A",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )

                        europeanCircumstancesShort.forEachIndexed { index, label ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = circumstancesA[index] == true,
                                    onCheckedChange = {
                                        circumstancesA[index] = it
                                        saveLocally()
                                    }
                                )
                                Text(
                                    text = label,
                                    color = Color.White,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        Text(
                            text = "Fahrzeug B",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )

                        europeanCircumstancesShort.forEachIndexed { index, label ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = circumstancesB[index] == true,
                                    onCheckedChange = {
                                        circumstancesB[index] = it
                                        saveLocally()
                                    }
                                )
                                Text(
                                    text = label,
                                    color = Color.White,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }

            4 -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF1D1D1D)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp)
                    ) {
                        Text(
                            text = "Angaben kontrollieren",
                            color = AccidentGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )

                        Spacer(Modifier.height(8.dp))

                        Text(
                            text = "Bitte prüfe alle Angaben. Beide Beteiligten " +
                                "sollten den Bericht vor der Bestätigung lesen.",
                            color = Color.White,
                            fontSize = 13.sp
                        )

                        Spacer(Modifier.height(12.dp))

                        shortAccidentSections.forEach { section ->
                            Text(
                                text = section.title,
                                color = AccidentGold,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 8.dp)
                            )

                            section.fields.forEach { item ->
                                val value = values[item.id].orEmpty()

                                if (value.isNotBlank()) {
                                    Text(
                                        text = "${item.label}: $value",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(top = 3.dp)
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        Text(
                            text = "Fotos im Bericht",
                            color = AccidentGold,
                            fontWeight = FontWeight.Bold
                        )

                        val totalPhotos = photos.values.sumOf { it.size }

                        Text(
                            text = "$totalPhotos Foto(s) werden an das PDF angehängt.",
                            color = Color.White,
                            fontSize = 12.sp
                        )

                        Spacer(Modifier.height(12.dp))

                        Text(
                            text = "Bestätigung A",
                            color = AccidentGold,
                            fontWeight = FontWeight.Bold
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = confirmedA,
                                onCheckedChange = { confirmedA = it }
                            )
                            Text(
                                text = "Beteiligter A hat die Angaben geprüft.",
                                color = Color.White,
                                fontSize = 12.sp
                            )
                        }

                        Text(
                            text = "Bestätigung B",
                            color = AccidentGold,
                            fontWeight = FontWeight.Bold
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = confirmedB,
                                onCheckedChange = { confirmedB = it }
                            )
                            Text(
                                text = "Beteiligter B hat die Angaben geprüft.",
                                color = Color.White,
                                fontSize = 12.sp
                            )
                        }

                        Text(
                            text = "Diese Bestätigungen sind noch keine " +
                                "gezeichneten Unterschriften. Die echten " +
                                "Unterschriftsfelder ergänzen wir später.",
                            color = Color.LightGray,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                Button(
                    onClick = {
                        saveLocally()
                        generatedPdfPath = null

                        if (confirmedA && confirmedB) {
                            pdfMessage = createShortAccidentPdf(
                                context = context,
                                values = values.toMap(),
                                selectedA = circumstancesA
                                    .filterValues { it }.keys.toSet(),
                                selectedB = circumstancesB
                                    .filterValues { it }.keys.toSet(),
                                photos = photos.toMap()
                            )

                            if (pdfMessage.startsWith("PDF gespeichert:")) {
                                val fileName = pdfMessage
                                    .substringAfter("PDF gespeichert:")
                                    .substringBefore("\n")
                                    .trim()

                                val directory = File(
                                    context.getExternalFilesDir(
                                        Environment.DIRECTORY_DOCUMENTS
                                    ),
                                    "CARLOCA"
                                )

                                val pdfFile = File(directory, fileName)

                                if (pdfFile.exists()) {
                                    generatedPdfPath = pdfFile.absolutePath
                                } else {
                                    pdfMessage =
                                        "PDF wurde gemeldet, aber die Datei " +
                                            "konnte nicht gefunden werden."
                                }
                            }
                        } else {
                            pdfMessage =
                                "Bitte beide Bestätigungen aktivieren."
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = confirmedA && confirmedB
                ) {
                    Text("PDF mit Fotos erstellen")
                }

                if (pdfMessage.isNotBlank()) {
                    Text(
                        text = pdfMessage,
                        color = AccidentGold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 10.dp)
                    )
                }

                if (generatedPdfPath != null) {
                    Spacer(Modifier.height(16.dp))

                    Text(
                        text = "UNFALLBERICHT VERSENDEN",
                        color = AccidentGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )

                    Text(
                        text = "Wähle aus, wie du den fertigen PDF-Bericht " +
                            "versenden möchtest.",
                        color = Color.LightGray,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(
                            top = 5.dp,
                            bottom = 10.dp
                        )
                    )

                    Button(
                        onClick = {
                            generatedPdfPath?.let { path ->
                                shareAccidentPdfWhatsApp(context, path)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF25D366),
                            contentColor = Color.White
                        )
                    ) {
                        Text("Über WhatsApp senden")
                    }

                    Spacer(Modifier.height(8.dp))

                    Button(
                        onClick = {
                            generatedPdfPath?.let { path ->
                                shareAccidentPdfEmail(context, path)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF0078D4),
                            contentColor = Color.White
                        )
                    ) {
                        Text("Per E-Mail senden")
                    }
                }
            }
        }

        if (photoMessage.isNotBlank()) {
            Text(
                text = photoMessage,
                color = AccidentGold,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(Modifier.height(18.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (currentStep == 0) {
                TextButton(
                    onClick = onBack,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Abbrechen", color = AccidentGold)
                }
            } else {
                Button(
                    onClick = {
                        currentStep--
                        pdfMessage = ""
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Zurück")
                }
            }

            if (currentStep < 4) {
                Button(
                    onClick = {
                        saveLocally()
                        currentStep++
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Weiter")
                }
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun PhotoCategorySection(
    title: String,
    categories: List<AccidentPhotoCategory>,
    photos: Map<String, List<String>>,
    onAdd: (String) -> Unit,
    onRemove: (String, String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1D1D1D)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = title,
                color = AccidentGold,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Mehrere Bilder pro Kategorie möglich.",
                color = Color.LightGray,
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 10.dp)
            )

            categories.forEach { category ->
                val categoryPhotos = photos[category.id].orEmpty()

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 9.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF292929)
                    ),
                    shape = RoundedCornerShape(9.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp)
                    ) {
                        Text(
                            text = category.title,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )

                        Text(
                            text = "${categoryPhotos.size} Foto(s)",
                            color = Color.LightGray,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 3.dp)
                        )

                        categoryPhotos.forEachIndexed { index, path ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Bild ${index + 1}",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    modifier = Modifier.weight(1f)
                                )

                                TextButton(
                                    onClick = {
                                        onRemove(category.id, path)
                                    }
                                ) {
                                    Text(
                                        "Entfernen",
                                        color = AccidentGold,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = { onAdd(category.id) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 5.dp)
                        ) {
                            Text("Fotos auswählen")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ShortAccidentFormSection(
    section: ShortAccidentSection,
    values: Map<String, String>,
    onValueChange: (String, String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1D1D1D)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = section.title,
                color = AccidentGold,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(10.dp))

            section.fields.forEach { item ->
                OutlinedTextField(
                    value = values[item.id].orEmpty(),
                    onValueChange = {
                        onValueChange(item.id, it)
                    },
                    label = { Text(item.label) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    minLines = if (item.multiline) 3 else 1,
                    maxLines = if (item.multiline) 5 else 1,
                    shape = RoundedCornerShape(10.dp)
                )
            }
        }
    }
}

private fun decodeAccidentPhoto(path: String): Bitmap? {
    return try {
        val bounds = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }

        BitmapFactory.decodeFile(path, bounds)

        var sampleSize = 1
        val maxDimension = 1600

        while (
            bounds.outWidth / sampleSize > maxDimension ||
            bounds.outHeight / sampleSize > maxDimension
        ) {
            sampleSize *= 2
        }

        val options = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
        }

        BitmapFactory.decodeFile(path, options)
    } catch (_: Exception) {
        null
    }
}

private fun createShortAccidentPdf(
    context: Context,
    values: Map<String, String>,
    selectedA: Set<Int>,
    selectedB: Set<Int>,
    photos: Map<String, List<String>>
): String {
    val document = PdfDocument()
    val pageWidth = 595
    val pageHeight = 842
    val margin = 40f
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    var pageNumber = 1
    var page = document.startPage(
        PdfDocument.PageInfo.Builder(
            pageWidth,
            pageHeight,
            pageNumber
        ).create()
    )

    var canvas = page.canvas
    var y = 48f

    fun startNewPage() {
        document.finishPage(page)
        pageNumber++

        page = document.startPage(
            PdfDocument.PageInfo.Builder(
                pageWidth,
                pageHeight,
                pageNumber
            ).create()
        )

        canvas = page.canvas
        y = 48f
    }

    fun ensureSpace(height: Float) {
        if (y + height > pageHeight - 48f) {
            startNewPage()
        }
    }

    fun drawText(
        text: String,
        size: Float = 10f,
        bold: Boolean = false
    ) {
        paint.color = android.graphics.Color.BLACK
        paint.textSize = size
        paint.typeface = if (bold) {
            android.graphics.Typeface.DEFAULT_BOLD
        } else {
            android.graphics.Typeface.DEFAULT
        }

        val maxWidth = pageWidth - margin * 2
        val words = text.split(" ")
        var line = ""

        words.forEach { word ->
            val candidate = if (line.isEmpty()) word else "$line $word"

            if (paint.measureText(candidate) > maxWidth) {
                ensureSpace(16f)
                canvas.drawText(line, margin, y, paint)
                y += 14f
                line = word
            } else {
                line = candidate
            }
        }

        if (line.isNotEmpty()) {
            ensureSpace(16f)
            canvas.drawText(line, margin, y, paint)
            y += 15f
        }
    }

    try {
        paint.color = android.graphics.Color.rgb(20, 20, 20)
        paint.textSize = 19f
        paint.typeface = android.graphics.Typeface.DEFAULT_BOLD
        canvas.drawText("CARLOCA – UNFALLBERICHT", margin, y, paint)
        y += 28f

        drawText(
            "Am Europäischen Unfallbericht orientierte Dokumentation",
            9f
        )

        shortAccidentSections.forEach { section ->
            ensureSpace(30f)
            y += 8f
            drawText(section.title, 13f, true)

            section.fields.forEach { item ->
                val value = values[item.id].orEmpty()

                if (value.isNotBlank()) {
                    drawText("${item.label}: $value", 9f)
                }
            }
        }

        ensureSpace(35f)
        y += 8f
        drawText("Europäische Unfallumstände – Fahrzeug A", 12f, true)

        selectedA.sorted().forEach { index ->
            europeanCircumstancesShort.getOrNull(index)?.let {
                drawText("• $it", 9f)
            }
        }

        drawText("Europäische Unfallumstände – Fahrzeug B", 12f, true)

        selectedB.sorted().forEach { index ->
            europeanCircumstancesShort.getOrNull(index)?.let {
                drawText("• $it", 9f)
            }
        }

        ensureSpace(55f)
        y += 15f

        drawText(
            "Bestätigung: Die Beteiligten bestätigen die Prüfung " +
                "der Angaben. Dies ist kein Schuldeingeständnis und " +
                "keine verbindliche Feststellung der Haftung.",
            9f
        )

        drawText(
            "Erstellt mit CARLOCA am " +
                SimpleDateFormat(
                    "dd.MM.yyyy HH:mm",
                    Locale.GERMANY
                ).format(Date()),
            8f
        )

        drawText(
            "Hinweis: Dieses Dokument ist eine digitale " +
                "Unfalldokumentation und kein amtlich zugelassenes " +
                "Formular oder eine qualifizierte elektronische Signatur.",
            8f
        )

        document.finishPage(page)

        val allCategories =
            participantPhotoCategories + damagePhotoCategories

        var photoPageCount = 0

        allCategories.forEach { category ->
            val paths = photos[category.id].orEmpty()

            paths.forEachIndexed { index, path ->
                val bitmap = decodeAccidentPhoto(path)

                if (bitmap != null) {
                    pageNumber++

                    val photoPage = document.startPage(
                        PdfDocument.PageInfo.Builder(
                            pageWidth,
                            pageHeight,
                            pageNumber
                        ).create()
                    )

                    val photoCanvas = photoPage.canvas
                    val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = android.graphics.Color.BLACK
                        textSize = 16f
                        typeface = android.graphics.Typeface.DEFAULT_BOLD
                    }

                    val detailPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = android.graphics.Color.DKGRAY
                        textSize = 10f
                    }

                    photoCanvas.drawText(
                        category.title,
                        40f,
                        45f,
                        titlePaint
                    )

                    photoCanvas.drawText(
                        "Foto ${index + 1}",
                        40f,
                        65f,
                        detailPaint
                    )

                    val availableWidth = pageWidth - 80f
                    val availableHeight = pageHeight - 130f

                    val scale = minOf(
                        availableWidth / bitmap.width.toFloat(),
                        availableHeight / bitmap.height.toFloat()
                    )

                    val displayWidth = bitmap.width * scale
                    val displayHeight = bitmap.height * scale

                    val left = (pageWidth - displayWidth) / 2f
                    val top = 90f +
                        (availableHeight - displayHeight) / 2f

                    val destination = RectF(
                        left,
                        top,
                        left + displayWidth,
                        top + displayHeight
                    )

                    val imagePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        isFilterBitmap = true
                    }

                    photoCanvas.drawBitmap(
                        bitmap,
                        null,
                        destination,
                        imagePaint
                    )

                    bitmap.recycle()
                    document.finishPage(photoPage)
                    photoPageCount++
                }
            }
        }

        val directory = File(
            context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
            "CARLOCA"
        )

        if (!directory.exists() && !directory.mkdirs()) {
            return "PDF-Ordner konnte nicht erstellt werden."
        }

        val fileName = "CARLOCA_Unfallbericht_" +
            SimpleDateFormat(
                "yyyyMMdd_HHmmss",
                Locale.GERMANY
            ).format(Date()) + ".pdf"

        val file = File(directory, fileName)

        FileOutputStream(file).use { output ->
            document.writeTo(output)
        }

        return "PDF gespeichert: $fileName\n" +
            "$photoPageCount Fotoseite(n) eingefügt."
    } catch (exception: Exception) {
        return "PDF konnte nicht erstellt werden: " +
            (exception.message ?: "Unbekannter Fehler")
    } finally {
        document.close()
    }
}
