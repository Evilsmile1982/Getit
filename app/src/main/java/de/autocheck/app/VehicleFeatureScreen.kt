package de.autocheck.app

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import org.json.JSONArray
import org.json.JSONObject

private const val FEATURE_PREFS = "carvita_feature_entries"

private val TUNING_CATEGORIES = listOf(
    "Motor- und Leistungstuning",
    "Fahrwerk und Bremsen",
    "Karosserie und Optik (Styling)",
    "Innenraum und Sound",
    "Reifen/Felgen",
    "Zulassung & Eintragungen"
)

private data class CarvitaFeatureEntry(
    val title: String,
    val details: String,
    val date: String,
    val amount: String
)

private data class TuningEntry(
    val id: Long,
    val category: String,
    val title: String,
    val details: String,
    val date: String,
    val amount: String,
    val documents: List<String>
)

private val CarvitaGold = Color(0xFFD4AF37)
private val CarvitaCard = Color(0xFF202020)

@Composable
fun VehicleFeatureScreen(
    featureName: String,
    onBack: () -> Unit
) {
    if (featureName == "Tuning und Extras") {
        TuningExtrasContent(onBack = onBack)
    } else {
        GenericVehicleFeatureContent(
            featureName = featureName,
            onBack = onBack
        )
    }
}

@Composable
private fun TuningExtrasContent(
    onBack: () -> Unit
) {
    val context = LocalContext.current

    val preferences = remember(context) {
        context.getSharedPreferences(
            FEATURE_PREFS,
            Context.MODE_PRIVATE
        )
    }

    val entries = remember(preferences) {
        mutableStateListOf<TuningEntry>().apply {
            val json = preferences.getString(
                "tuning_extras_entries",
                "[]"
            ) ?: "[]"

            try {
                val array = JSONArray(json)

                for (i in 0 until array.length()) {
                    val item = array.optJSONObject(i) ?: continue
                    val docsJson = item.optJSONArray("documents")
                    val documents = mutableListOf<String>()

                    if (docsJson != null) {
                        for (j in 0 until docsJson.length()) {
                            val uri = docsJson.optString(j)
                            if (uri.isNotBlank()) documents.add(uri)
                        }
                    }

                    add(
                        TuningEntry(
                            id = item.optLong("id", i.toLong()),
                            category = item.optString("category"),
                            title = item.optString("title"),
                            details = item.optString("details"),
                            date = item.optString("date"),
                            amount = item.optString("amount"),
                            documents = documents
                        )
                    )
                }
            } catch (_: Exception) {
                // Bei beschädigten Daten mit leerer Liste starten.
            }
        }
    }

    var selectedCategory by remember {
        mutableStateOf(TUNING_CATEGORIES.first())
    }
    var title by remember { mutableStateOf("") }
    var details by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var draftDocuments by remember {
        mutableStateOf<List<String>>(emptyList())
    }
    var editingId by remember { mutableStateOf<Long?>(null) }
    var message by remember { mutableStateOf<String?>(null) }

    fun persistEntries() {
        val array = JSONArray()

        entries.forEach { entry ->
            array.put(
                JSONObject().apply {
                    put("id", entry.id)
                    put("category", entry.category)
                    put("title", entry.title)
                    put("details", entry.details)
                    put("date", entry.date)
                    put("amount", entry.amount)
                    put(
                        "documents",
                        JSONArray().apply {
                            entry.documents.forEach { put(it) }
                        }
                    )
                }
            )
        }

        preferences.edit()
            .putString("tuning_extras_entries", array.toString())
            .apply()
    }

    val documentPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {
                // Manche Dateianbieter erlauben keine dauerhafte Freigabe.
            }

            if (!draftDocuments.contains(uri.toString())) {
                draftDocuments = draftDocuments + uri.toString()
            }
            message = "Dokument ausgewählt. Speichere den Eintrag."
        }
    }

    fun clearForm() {
        title = ""
        details = ""
        date = ""
        amount = ""
        draftDocuments = emptyList()
        editingId = null
        message = null
    }

    DetailScaffold(
        title = "Tuning und Extras",
        onBack = onBack
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Deine Umbauten, Extras und Kosten im Überblick.",
                color = Color.White
            )

            Text(
                text = "1. Kategorie auswählen",
                color = CarvitaGold
            )

            TUNING_CATEGORIES.forEachIndexed { index, category ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            selectedCategory = category
                            clearForm()
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = if (selectedCategory == category) {
                            Color(0xFF39301A)
                        } else {
                            CarvitaCard
                        }
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (selectedCategory == category) {
                            CarvitaGold
                        } else {
                            Color.DarkGray
                        }
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp)
                    ) {
                        Text(
                            text = "${index + 1}. $category",
                            color = if (selectedCategory == category) {
                                CarvitaGold
                            } else {
                                Color.White
                            }
                        )
                        Text(
                            text = if (selectedCategory == category) {
                                "Ausgewählt"
                            } else {
                                "Antippen zum Auswählen"
                            },
                            color = Color.LightGray
                        )
                    }
                }
            }

            Text(
                text = if (editingId == null) {
                    "2. Neuen Eintrag hinzufügen"
                } else {
                    "2. Eintrag bearbeiten"
                },
                color = CarvitaGold
            )

            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it
                    message = null
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Was wurde gemacht?") },
                placeholder = {
                    Text("z. B. Sportauspuff eingebaut")
                },
                singleLine = true
            )

            OutlinedTextField(
                value = details,
                onValueChange = { details = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Details der Arbeiten") },
                placeholder = {
                    Text("Hersteller, Modell, Werkstatt, Teilenummer …")
                },
                minLines = 3
            )

            OutlinedTextField(
                value = date,
                onValueChange = { date = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Datum (optional)") },
                placeholder = { Text("z. B. 26.10.2026") },
                singleLine = true
            )

            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Gesamtkosten in € (optional)") },
                placeholder = { Text("z. B. 850,00") },
                singleLine = true
            )

            if (selectedCategory == "Zulassung & Eintragungen") {
                Text(
                    text = "Dokumente und Nachweise",
                    color = CarvitaGold
                )
                Text(
                    text = "Wähle beispielsweise Gutachten, Rechnungen " +
                        "oder Eintragungsunterlagen aus.",
                    color = Color.LightGray
                )

                Button(
                    onClick = {
                        documentPicker.launch(
                            arrayOf(
                                "application/pdf",
                                "image/*",
                                "text/plain"
                            )
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Datei vom Handy auswählen")
                }

                draftDocuments.forEachIndexed { index, document ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = CarvitaCard
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Dokument ${index + 1}: " +
                                    Uri.parse(document).lastPathSegment
                                        ?.substringAfterLast('/')
                                        ?.take(45).orEmpty(),
                                color = Color.White,
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(
                                onClick = {
                                    draftDocuments =
                                        draftDocuments - document
                                }
                            ) {
                                Text("Entfernen")
                            }
                        }
                    }
                }
            }

            message?.let {
                Text(text = it, color = CarvitaGold)
            }

            Button(
                onClick = {
                    if (title.isBlank()) {
                        message = "Bitte gib an, was gemacht wurde."
                    } else {
                        val id = editingId ?: System.currentTimeMillis()
                        val updated = TuningEntry(
                            id = id,
                            category = selectedCategory,
                            title = title.trim(),
                            details = details.trim(),
                            date = date.trim(),
                            amount = amount.trim(),
                            documents = if (
                                selectedCategory == "Zulassung & Eintragungen"
                            ) {
                                draftDocuments
                            } else {
                                emptyList()
                            }
                        )

                        val existingIndex = entries.indexOfFirst {
                            it.id == id
                        }

                        if (existingIndex >= 0) {
                            entries[existingIndex] = updated
                        } else {
                            entries.add(0, updated)
                        }

                        persistEntries()
                        clearForm()
                        message = "Eintrag gespeichert."
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    if (editingId == null) {
                        "Eintrag speichern"
                    } else {
                        "Änderungen speichern"
                    }
                )
            }

            if (editingId != null) {
                TextButton(
                    onClick = { clearForm() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Bearbeiten abbrechen")
                }
            }

            Text(
                text = "Gespeicherte Einträge (${entries.size})",
                color = CarvitaGold
            )

            if (entries.isEmpty()) {
                Text(
                    text = "Noch keine Tuning- oder Extra-Einträge vorhanden.",
                    color = Color.LightGray
                )
            }

            entries.forEach { entry ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = CarvitaCard
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(7.dp)
                    ) {
                        Text(
                            text = entry.category,
                            color = CarvitaGold
                        )
                        Text(
                            text = entry.title,
                            color = Color.White
                        )

                        if (entry.details.isNotBlank()) {
                            Text(
                                text = entry.details,
                                color = Color.LightGray
                            )
                        }

                        if (entry.date.isNotBlank()) {
                            Text(
                                text = "Datum: ${entry.date}",
                                color = Color.LightGray
                            )
                        }

                        if (entry.amount.isNotBlank()) {
                            Text(
                                text = "Kosten: ${entry.amount} €",
                                color = Color.White
                            )
                        }

                        if (entry.documents.isNotEmpty()) {
                            Text(
                                text = "Angehängte Dokumente: " +
                                    entry.documents.size,
                                color = CarvitaGold
                            )

                            entry.documents.forEachIndexed { index, uriString ->
                                TextButton(
                                    onClick = {
                                        try {
                                            val uri = Uri.parse(uriString)
                                            val intent = Intent(
                                                Intent.ACTION_VIEW
                                            ).apply {
                                                setDataAndType(
                                                    uri,
                                                    context.contentResolver
                                                        .getType(uri) ?: "*/*"
                                                )
                                                addFlags(
                                                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                                                )
                                            }
                                            context.startActivity(intent)
                                        } catch (_: Exception) {
                                            message =
                                                "Datei konnte nicht geöffnet werden."
                                        }
                                    }
                                ) {
                                    Text("Dokument ${index + 1} öffnen")
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = {
                                    selectedCategory = entry.category
                                    title = entry.title
                                    details = entry.details
                                    date = entry.date
                                    amount = entry.amount
                                    draftDocuments = entry.documents
                                    editingId = entry.id
                                    message = null
                                }
                            ) {
                                Text("Bearbeiten")
                            }

                            TextButton(
                                onClick = {
                                    entries.removeAll { it.id == entry.id }
                                    persistEntries()
                                    if (editingId == entry.id) clearForm()
                                }
                            ) {
                                Text("Löschen")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GenericVehicleFeatureContent(
    featureName: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    val preferences = remember(context) {
        context.getSharedPreferences(
            FEATURE_PREFS,
            Context.MODE_PRIVATE
        )
    }

    val storageKey = remember(featureName) {
        "entries_" + featureName.lowercase()
            .replace(" ", "_")
            .replace("ü", "ue")
            .replace("ä", "ae")
            .replace("ö", "oe")
            .replace("ß", "ss")
    }

    val entries = remember(preferences, storageKey) {
        mutableStateListOf<CarvitaFeatureEntry>().apply {
            val json = preferences.getString(storageKey, "[]") ?: "[]"

            try {
                val array = JSONArray(json)
                for (index in 0 until array.length()) {
                    val item = array.optJSONObject(index) ?: continue
                    add(
                        CarvitaFeatureEntry(
                            title = item.optString("title"),
                            details = item.optString("details"),
                            date = item.optString("date"),
                            amount = item.optString("amount")
                        )
                    )
                }
            } catch (_: Exception) {
                // Bei ungültigen Daten mit einer leeren Liste starten.
            }
        }
    }

    var title by remember(featureName) { mutableStateOf("") }
    var details by remember(featureName) { mutableStateOf("") }
    var date by remember(featureName) { mutableStateOf("") }
    var amount by remember(featureName) { mutableStateOf("") }
    var validationMessage by remember(featureName) {
        mutableStateOf<String?>(null)
    }

    fun persistEntries() {
        val array = JSONArray()
        entries.forEach { entry ->
            array.put(
                JSONObject().apply {
                    put("title", entry.title)
                    put("details", entry.details)
                    put("date", entry.date)
                    put("amount", entry.amount)
                }
            )
        }
        preferences.edit().putString(storageKey, array.toString()).apply()
    }

    DetailScaffold(
        title = featureName,
        onBack = onBack
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = when (featureName) {
                    "Geplante Vorhaben" ->
                        "Plane kommende Arbeiten und Verbesserungen an deinem Fahrzeug."
                    "Elektro und Verbrauch" ->
                        "Notiere elektrische Arbeiten, Verbrauchswerte und zugehörige Kosten."
                    "Kalender" ->
                        "Speichere wichtige Fahrzeugtermine und Erinnerungen."
                    else -> "Verwalte deine Fahrzeug-Einträge."
                },
                color = Color.White
            )

            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it
                    validationMessage = null
                },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text(
                        when (featureName) {
                            "Kalender" -> "Termin"
                            "Elektro und Verbrauch" -> "Messung oder Eintrag"
                            else -> "Vorhaben"
                        }
                    )
                },
                singleLine = true
            )

            OutlinedTextField(
                value = details,
                onValueChange = { details = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Beschreibung / Notizen") },
                minLines = 2
            )

            OutlinedTextField(
                value = date,
                onValueChange = { date = it },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text(
                        if (featureName == "Kalender") {
                            "Datum und Uhrzeit (z. B. 12.11.2026, 10:30)"
                        } else {
                            "Datum (optional)"
                        }
                    )
                },
                singleLine = true
            )

            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text(
                        if (featureName == "Elektro und Verbrauch") {
                            "Verbrauch / Wert / Kosten (optional)"
                        } else {
                            "Kosten in € (optional)"
                        }
                    )
                },
                singleLine = true
            )

            validationMessage?.let {
                Text(text = it, color = Color(0xFFFF8A80))
            }

            Button(
                onClick = {
                    if (title.isBlank()) {
                        validationMessage = "Bitte gib einen Titel ein."
                    } else {
                        entries.add(
                            0,
                            CarvitaFeatureEntry(
                                title = title.trim(),
                                details = details.trim(),
                                date = date.trim(),
                                amount = amount.trim()
                            )
                        )
                        persistEntries()
                        title = ""
                        details = ""
                        date = ""
                        amount = ""
                        validationMessage = null
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Eintrag speichern")
            }

            Text(
                text = "Gespeicherte Einträge (${entries.size})",
                color = CarvitaGold
            )

            if (entries.isEmpty()) {
                Text(
                    text = "Noch keine Einträge vorhanden.",
                    color = Color.LightGray
                )
            } else {
                entries.forEachIndexed { index, entry ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = CarvitaCard
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(entry.title, color = Color.White)

                            if (entry.details.isNotBlank()) {
                                Text(entry.details, color = Color.LightGray)
                            }

                            if (entry.date.isNotBlank()) {
                                Text(
                                    "Datum: ${entry.date}",
                                    color = CarvitaGold
                                )
                            }

                            if (entry.amount.isNotBlank()) {
                                Text(
                                    "Wert: ${entry.amount}",
                                    color = Color.LightGray
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(
                                    onClick = {
                                        entries.removeAt(index)
                                        persistEntries()
                                    }
                                ) {
                                    Text("Löschen")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
