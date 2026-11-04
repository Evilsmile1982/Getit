package de.autocheck.app

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
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
import androidx.core.content.FileProvider
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

private val GloveboxGold = Color(0xFFD4AF37)
private val GloveboxDark = Color(0xFF101010)

private data class GloveboxDocument(
    val id: String,
    val title: String,
    val paths: List<String>
)

@Composable
fun DigitalGloveboxScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current

    val prefs = remember {
        context.getSharedPreferences(
            "carloca_digital_glovebox",
            Context.MODE_PRIVATE
        )
    }

    val documents = remember {
        mutableStateListOf<GloveboxDocument>().apply {
            val saved = prefs.getString("documents", null)

            if (!saved.isNullOrBlank()) {
                try {
                    val array = JSONArray(saved)

                    for (i in 0 until array.length()) {
                        val item = array.getJSONObject(i)
                        val pathsArray =
                            item.optJSONArray("paths") ?: JSONArray()

                        val paths = mutableListOf<String>()

                        for (j in 0 until pathsArray.length()) {
                            val path = pathsArray.optString(j)

                            if (
                                path.isNotBlank() &&
                                File(path).exists()
                            ) {
                                paths.add(path)
                            }
                        }

                        add(
                            GloveboxDocument(
                                id = item.optString("id"),
                                title = item.optString("title"),
                                paths = paths
                            )
                        )
                    }
                } catch (_: Exception) {
                    // Fehlerhafte lokale Metadaten ignorieren.
                }
            }

            val defaults = listOf(
                "Führerschein – Vorderseite",
                "Führerschein – Rückseite",
                "Zulassungsschein",
                "Versicherungsdaten",
                "Kennzeichen"
            )

            defaults.forEachIndexed { index, title ->
                if (none { it.id == "standard_$index" }) {
                    add(
                        index,
                        GloveboxDocument(
                            "standard_$index",
                            title,
                            emptyList()
                        )
                    )
                }
            }
        }
    }

    var addDialog by remember { mutableStateOf(false) }
    var newDocumentName by remember { mutableStateOf("") }
    var selectedDocumentId by remember { mutableStateOf<String?>(null) }
    var message by remember { mutableStateOf("") }
    var previewPath by remember { mutableStateOf<String?>(null) }
    var showPhotoSourceDialog by remember { mutableStateOf(false) }
    var pendingCameraFile by remember { mutableStateOf<File?>(null) }

    fun saveDocuments() {
        val array = JSONArray()

        documents.forEach { document ->
            val item = JSONObject()
            item.put("id", document.id)
            item.put("title", document.title)
            item.put("paths", JSONArray(document.paths))
            array.put(item)
        }

        prefs.edit()
            .putString("documents", array.toString())
            .apply()
    }

    fun addCameraPhoto(file: File) {
        val id = selectedDocumentId

        if (id == null) {
            file.delete()
            return
        }

        val index = documents.indexOfFirst { it.id == id }

        if (index < 0) {
            file.delete()
            selectedDocumentId = null
            return
        }

        if (file.exists() && file.length() > 0L) {
            val paths = documents[index].paths.toMutableList()
            paths.add(file.absolutePath)

            documents[index] = documents[index].copy(
                paths = paths
            )

            saveDocuments()
            message = "Foto erfolgreich gespeichert."
        } else {
            file.delete()
            message = "Das Kamera-Foto konnte nicht gespeichert werden."
        }

        selectedDocumentId = null
        pendingCameraFile = null
    }

    fun importImages(uris: List<Uri>) {
        val id = selectedDocumentId ?: return
        val index = documents.indexOfFirst { it.id == id }

        if (index < 0) {
            selectedDocumentId = null
            return
        }

        val folder = File(
            context.filesDir,
            "digital_glovebox"
        )

        if (!folder.exists() && !folder.mkdirs()) {
            message = "Dokumentenordner konnte nicht erstellt werden."
            selectedDocumentId = null
            return
        }

        val paths = documents[index].paths.toMutableList()
        var imported = 0

        uris.forEach { uri ->
            try {
                val file = File(
                    folder,
                    "document_${System.currentTimeMillis()}_${paths.size}.jpg"
                )

                context.contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(file).use { output ->
                        input.copyTo(output)
                    }
                }

                if (file.exists() && file.length() > 0L) {
                    paths.add(file.absolutePath)
                    imported++
                } else {
                    file.delete()
                }
            } catch (_: Exception) {
                // Nicht lesbare Datei überspringen.
            }
        }

        documents[index] = documents[index].copy(paths = paths)
        saveDocuments()

        message = if (imported > 0) {
            "$imported Bild(er) lokal gespeichert."
        } else {
            "Bilder konnten nicht übernommen werden."
        }

        selectedDocumentId = null
    }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isNotEmpty()) {
            importImages(uris)
        } else {
            selectedDocumentId = null
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val file = pendingCameraFile

        if (success && file != null) {
            addCameraPhoto(file)
        } else {
            file?.delete()
            pendingCameraFile = null
            selectedDocumentId = null
            message = "Die Kameraaufnahme wurde abgebrochen."
        }
    }

    fun openCamera() {
        if (selectedDocumentId == null) return

        try {
            val folder = File(
                context.filesDir,
                "digital_glovebox"
            )

            if (!folder.exists() && !folder.mkdirs()) {
                message = "Dokumentenordner konnte nicht erstellt werden."
                selectedDocumentId = null
                return
            }

            val file = File(
                folder,
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
            selectedDocumentId = null

            message = "Kamera konnte nicht geöffnet werden: " +
                (exception.message ?: "Unbekannter Fehler")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GloveboxDark)
            .safeDrawingPadding()
            .imePadding()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "DIGITALES HANDSCHUHFACH",
            color = GloveboxGold,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF29251A)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(Modifier.padding(12.dp)) {
                Text(
                    "DATENSCHUTZ – LOKALE SPEICHERUNG",
                    color = GloveboxGold,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(5.dp))

                Text(
                    "Alle Bilder und Dokumente werden ausschließlich " +
                        "im privaten Speicher dieser App auf diesem " +
                        "Smartphone abgelegt. Es erfolgt kein automatischer " +
                        "Upload und keine Cloud-Synchronisierung. Bei einem " +
                        "Handywechsel müssen die Dokumente auf dem neuen " +
                        "Gerät erneut hinzugefügt werden. Bei Deinstallation " +
                        "oder Verlust des Geräts können die Daten verloren " +
                        "gehen. Schütze dein Smartphone mit einer " +
                        "Bildschirmsperre.",
                    color = Color.White,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            items(
                documents,
                key = { it.id }
            ) { document ->

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF1D1D1D)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text(
                            document.title,
                            color = GloveboxGold,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            "${document.paths.size} Bild(er) gespeichert",
                            color = Color.LightGray,
                            fontSize = 11.sp
                        )

                        document.paths.forEachIndexed { index, path ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Bild ${index + 1}",
                                    color = Color.White,
                                    modifier = Modifier.weight(1f),
                                    fontSize = 12.sp
                                )

                                TextButton(
                                    onClick = {
                                        previewPath = path
                                    }
                                ) {
                                    Text(
                                        "Ansehen",
                                        color = GloveboxGold
                                    )
                                }

                                TextButton(
                                    onClick = {
                                        File(path).delete()

                                        val currentIndex =
                                            documents.indexOfFirst {
                                                it.id == document.id
                                            }

                                        if (currentIndex >= 0) {
                                            documents[currentIndex] =
                                                document.copy(
                                                    paths = document.paths - path
                                                )

                                            saveDocuments()
                                        }
                                    }
                                ) {
                                    Text(
                                        "Löschen",
                                        color = GloveboxGold
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = {
                                selectedDocumentId = document.id
                                showPhotoSourceDialog = true
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Bild(er) hinzufügen")
                        }
                    }
                }
            }

            item {
                Button(
                    onClick = {
                        newDocumentName = ""
                        addDialog = true
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Eigenes Dokument anlegen")
                }
            }
        }

        if (message.isNotBlank()) {
            Text(
                message,
                color = GloveboxGold,
                fontSize = 12.sp,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Zurück")
        }
    }

    if (showPhotoSourceDialog) {
        AlertDialog(
            onDismissRequest = {
                showPhotoSourceDialog = false
                selectedDocumentId = null
            },
            title = {
                Text("Bild hinzufügen")
            },
            text = {
                Text(
                    "Möchtest du ein neues Foto aufnehmen " +
                        "oder ein vorhandenes Bild aus deiner Galerie auswählen?"
                )
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
                            selectedDocumentId = null
                        }
                    ) {
                        Text("Abbrechen")
                    }

                    TextButton(
                        onClick = {
                            showPhotoSourceDialog = false
                            imagePicker.launch("image/*")
                        }
                    ) {
                        Text("Galerie")
                    }
                }
            }
        )
    }

    if (addDialog) {
        AlertDialog(
            onDismissRequest = {
                addDialog = false
            },
            title = {
                Text("Eigenes Dokument")
            },
            text = {
                OutlinedTextField(
                    value = newDocumentName,
                    onValueChange = {
                        newDocumentName = it
                    },
                    label = {
                        Text("Name des Dokuments")
                    },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val name = newDocumentName.trim()

                        if (name.isNotEmpty()) {
                            documents.add(
                                GloveboxDocument(
                                    id = "custom_${System.currentTimeMillis()}",
                                    title = name,
                                    paths = emptyList()
                                )
                            )

                            saveDocuments()
                        }

                        addDialog = false
                    }
                ) {
                    Text("Anlegen")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        addDialog = false
                    }
                ) {
                    Text("Abbrechen")
                }
            }
        )
    }

    previewPath?.let { path ->
        val bitmap = remember(path) {
            BitmapFactory.decodeFile(path)
        }

        AlertDialog(
            onDismissRequest = {
                previewPath = null
            },
            title = {
                Text("Dokument ansehen")
            },
            text = {
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Gespeichertes Dokument",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(360.dp),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Text("Bild kann nicht angezeigt werden.")
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        previewPath = null
                    }
                ) {
                    Text("Schließen")
                }
            }
        )
    }
}
