package de.autocheck.app

import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import de.autocheck.app.data.PickerlDocument
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@Composable
fun PickerlDocumentsScreen(
    vehicle: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    val store = remember {
        PickerlDocumentStore(context)
    }

    var documents by remember {
        mutableStateOf(
            store.load().filter {
                it.vehicle == vehicle
            }
        )
    }

    var documentDate by remember {
        mutableStateOf(
            SimpleDateFormat(
                "dd.MM.yyyy",
                Locale.GERMANY
            ).format(Date())
        )
    }

    var documentType by remember {
        mutableStateOf("Prüfbericht")
    }

    var message by remember {
        mutableStateOf("")
    }

    val documentPicker =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.OpenDocument()
        ) { uri: Uri? ->

            if (uri != null) {
                try {
                    context.contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (_: Exception) {
                    // Manche Dateianbieter unterstützen keine dauerhafte Berechtigung.
                }

                val name = context.contentResolver.query(
                    uri,
                    arrayOf(OpenableColumns.DISPLAY_NAME),
                    null,
                    null,
                    null
                )?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        cursor.getString(0) ?: "Dokument"
                    } else {
                        "Dokument"
                    }
                } ?: "Dokument"

                val mimeType =
                    context.contentResolver.getType(uri)
                        ?: "application/pdf"

                val document = PickerlDocument(
                    id = UUID.randomUUID().toString(),
                    vehicle = vehicle,
                    date = documentDate.trim(),
                    type = documentType,
                    name = name,
                    uri = uri.toString(),
                    mimeType = mimeType,
                    includeInPdf = true
                )

                val allDocuments = store.load()
                    .filterNot {
                        it.id == document.id
                    } + document

                store.save(allDocuments)

                documents = allDocuments.filter {
                    it.vehicle == vehicle
                }

                message = "Dokument gespeichert."
            }
        }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Pickerl-Dokumentation",
            style = MaterialTheme.typography.headlineSmall
        )

        Text("Fahrzeug: $vehicle")

        OutlinedTextField(
            value = documentDate,
            onValueChange = {
                documentDate = it
            },
            label = {
                Text("Dokumentdatum (TT.MM.JJJJ)")
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Text("Dokumentart")

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = documentType == "Prüfbericht",
                onClick = {
                    documentType = "Prüfbericht"
                }
            )
            Text("Prüfbericht")

            RadioButton(
                selected = documentType == "Rechnung",
                onClick = {
                    documentType = "Rechnung"
                }
            )
            Text("Rechnung")
        }

        Button(
            onClick = {
                val validDate = runCatching {
                    val format = SimpleDateFormat(
                        "dd.MM.yyyy",
                        Locale.GERMANY
                    )
                    format.isLenient = false
                    format.parse(documentDate.trim()) != null
                }.getOrDefault(false)

                if (validDate) {
                    documentPicker.launch(
                        arrayOf(
                            "application/pdf",
                            "image/*"
                        )
                    )
                } else {
                    message =
                        "Bitte ein gültiges Datum als TT.MM.JJJJ eingeben."
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("PDF oder Foto hinzufügen")
        }

        if (message.isNotBlank()) {
            Text(message)
        }

        Text(
            text = "Gespeicherte Dokumente",
            style = MaterialTheme.typography.titleMedium
        )

        Text(
            text = "Häkchen setzen, wenn das Dokument in der PDF-Auswahl berücksichtigt werden soll."
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(
                items = documents.sortedByDescending {
                    it.date
                },
                key = {
                    it.id
                }
            ) { document ->

                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = document.includeInPdf,
                            onCheckedChange = { checked ->
                                val allDocuments = store.load().map {
                                    if (it.id == document.id) {
                                        it.copy(
                                            includeInPdf = checked
                                        )
                                    } else {
                                        it
                                    }
                                }

                                store.save(allDocuments)

                                documents = allDocuments.filter {
                                    it.vehicle == vehicle
                                }
                            }
                        )

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(document.type)
                            Text(document.name)
                            Text(document.date)
                        }
                    }
                }
            }
        }

        Spacer(
            modifier = Modifier.height(4.dp)
        )

        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Zurück")
        }
    }
}
