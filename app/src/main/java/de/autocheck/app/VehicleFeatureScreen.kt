
package de.autocheck.app

import android.content.Context
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

private data class CarvitaFeatureEntry(
    val title: String,
    val details: String,
    val date: String,
    val amount: String
)

@Composable
fun VehicleFeatureScreen(
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

    var title by remember(featureName) {
        mutableStateOf("")
    }

    var details by remember(featureName) {
        mutableStateOf("")
    }

    var date by remember(featureName) {
        mutableStateOf("")
    }

    var amount by remember(featureName) {
        mutableStateOf("")
    }

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

        preferences.edit()
            .putString(storageKey, array.toString())
            .apply()
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

                    "Tuning und Extras" ->
                        "Halte Umbauten, Zubehör und zusätzliche Ausstattung fest."

                    "Elektro und Verbrauch" ->
                        "Notiere elektrische Arbeiten, Verbrauchswerte und zugehörige Kosten."

                    "Kalender" ->
                        "Speichere wichtige Fahrzeugtermine und Erinnerungen."

                    else ->
                        "Verwalte deine Fahrzeug-Einträge."
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
                            "Tuning und Extras" -> "Umbau oder Extra"
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
                        when (featureName) {
                            "Elektro und Verbrauch" ->
                                "Verbrauch / Wert / Kosten (optional)"

                            else ->
                                "Kosten in € (optional)"
                        }
                    )
                },
                singleLine = true
            )

            validationMessage?.let { message ->
                Text(
                    text = message,
                    color = Color(0xFFFF8A80)
                )
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
                text = "Gespeicherte Einträge (\${entries.size})",
                color = Color(0xFFD4AF37)
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
                            containerColor = Color(0xFF202020)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
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
                                    text = "Datum: \${entry.date}",
                                    color = Color(0xFFD4AF37)
                                )
                            }

                            if (entry.amount.isNotBlank()) {
                                Text(
                                    text = "Wert: \${entry.amount}",
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
