
package de.autocheck.app

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val AccidentGold = Color(0xFFD4AF37)
private val AccidentDark = Color(0xFF101010)

private data class AccidentField(
    val id: String,
    val label: String,
    val multiline: Boolean = false
)

private data class AccidentSection(
    val title: String,
    val fields: List<AccidentField>
)

private fun field(
    id: String,
    label: String,
    multiline: Boolean = false
) = AccidentField(id, label, multiline)

private val accidentSections = listOf(
    AccidentSection(
        "1. Angaben zum Unfall",
        listOf(
            field("report_number", "Unfallnummer"),
            field("date", "Unfalldatum"),
            field("time", "Uhrzeit"),
            field("country", "Unfallland"),
            field("location", "Unfallort"),
            field("street", "Straße / Kreuzung"),
            field("postal_city", "PLZ / Ort"),
            field("gps", "GPS-Koordinaten"),
            field("accident_type", "Unfallart: Kreuzung, Kreisverkehr, Gerade Strecke, Parkplatz oder Sonstiges"),
            field("weather", "Wetter"),
            field("road_condition", "Straßenverhältnisse"),
            field("visibility", "Sichtverhältnisse")
        )
    ),
    AccidentSection(
        "2. Unfallbeteiligter A",
        listOf(
            field("a_first", "Vorname"),
            field("a_last", "Nachname"),
            field("a_birth", "Geburtsdatum"),
            field("a_address", "Adresse"),
            field("a_phone", "Telefon"),
            field("a_email", "E-Mail"),
            field("a_owner", "Fahrzeughalter"),
            field("a_make", "Fahrzeugmarke"),
            field("a_model", "Fahrzeugmodell"),
            field("a_plate", "Kennzeichen"),
            field("a_country", "Zulassungsland"),
            field("a_insurer", "Versicherungsgesellschaft"),
            field("a_policy", "Polizzennummer"),
            field("a_insurance_from", "Versicherungsbeginn"),
            field("a_insurance_to", "Versicherung gültig bis"),
            field("a_license", "Führerscheinnummer"),
            field("a_license_country", "Ausstellungsland"),
            field("a_license_class", "Führerscheinklasse"),
            field("a_license_date", "Ausstellungsdatum")
        )
    ),
    AccidentSection(
        "3. Unfallbeteiligter B",
        listOf(
            field("b_first", "Vorname"),
            field("b_last", "Nachname"),
            field("b_birth", "Geburtsdatum"),
            field("b_address", "Adresse"),
            field("b_phone", "Telefon"),
            field("b_email", "E-Mail"),
            field("b_owner", "Fahrzeughalter"),
            field("b_make", "Fahrzeugmarke"),
            field("b_model", "Fahrzeugmodell"),
            field("b_plate", "Kennzeichen"),
            field("b_country", "Zulassungsland"),
            field("b_insurer", "Versicherungsgesellschaft"),
            field("b_policy", "Polizzennummer"),
            field("b_insurance_from", "Versicherungsbeginn"),
            field("b_insurance_to", "Versicherung gültig bis"),
            field("b_license", "Führerscheinnummer"),
            field("b_license_country", "Ausstellungsland"),
            field("b_license_class", "Führerscheinklasse"),
            field("b_license_date", "Ausstellungsdatum")
        )
    ),
    AccidentSection(
        "4. Unfallhergang",
        listOf(
            field("description", "Gemeinsame Beschreibung des Unfallhergangs", true),
            field("a_direction", "Fahrtrichtung A"),
            field("b_direction", "Fahrtrichtung B"),
            field("a_manoeuvre", "Fahrmanöver A"),
            field("b_manoeuvre", "Fahrmanöver B"),
            field("collision_point", "Kollisionspunkt"),
            field("traffic_lights", "Ampeln / Verkehrszeichen"),
            field("road_markings", "Straßenmarkierungen"),
            field("a_statement", "Eigene Schilderung Beteiligter A", true),
            field("b_statement", "Eigene Schilderung Beteiligter B", true)
        )
    ),
    AccidentSection(
        "5. Schäden",
        listOf(
            field("a_damaged_parts", "Beschädigte Fahrzeugteile A", true),
            field("a_damage", "Schadensbeschreibung A", true),
            field("b_damaged_parts", "Beschädigte Fahrzeugteile B", true),
            field("b_damage", "Schadensbeschreibung B", true),
            field("other_damage", "Sonstige Sachschäden", true),
            field("other_damage_description", "Beschreibung sonstiger Schäden", true)
        )
    ),
    AccidentSection(
        "6. Unfallskizze",
        listOf(
            field("road_layout", "Straßenverlauf / Kreuzung / Kreisverkehr", true),
            field("vehicle_positions", "Positionen der Fahrzeuge A und B", true),
            field("directions_sketch", "Fahrtrichtungen und Kollisionspunkt", true),
            field("signs_sketch", "Verkehrszeichen / Ampeln in der Skizze", true)
        )
    ),
    AccidentSection(
        "7. Verletzte Personen",
        listOf(
            field("injuries", "Gab es Verletzte? Ja / Nein"),
            field("injured_count", "Anzahl verletzter Personen"),
            field("injured_names", "Namen der verletzten Personen", true),
            field("passengers", "Mitfahrer / Insassen", true),
            field("emergency_services", "Rettung / Notarzt verständigt? Ja / Nein")
        )
    ),
    AccidentSection(
        "8. Zeugen",
        listOf(
            field("witness_names", "Namen der Zeugen", true),
            field("witness_addresses", "Adressen der Zeugen", true),
            field("witness_phones", "Telefonnummern der Zeugen", true),
            field("witness_statements", "Zeugenaussagen", true)
        )
    ),
    AccidentSection(
        "9. Polizei",
        listOf(
            field("police_called", "Polizei verständigt? Ja / Nein"),
            field("police_station", "Polizeidienststelle"),
            field("police_number", "Aktenzeichen"),
            field("police_officers", "Namen / Dienstnummern der Beamten")
        )
    ),
    AccidentSection(
        "10. Fotodokumentation",
        listOf(
            field("photos_scene", "Fotos der Unfallstelle – Dateinamen / Hinweise", true),
            field("photos_a", "Fotos Fahrzeug A – Dateinamen / Hinweise", true),
            field("photos_b", "Fotos Fahrzeug B – Dateinamen / Hinweise", true),
            field("photos_damage", "Fotos der Schäden – Dateinamen / Hinweise", true),
            field("photos_plates", "Fotos der Kennzeichen – Dateinamen / Hinweise", true),
            field("photos_signs", "Fotos von Verkehrszeichen – Dateinamen / Hinweise", true)
        )
    ),
    AccidentSection(
        "11. Unfallumstände – Europäischer Standard",
        listOf(
            field("circumstances_a", "Anzahl markierter Umstände A"),
            field("circumstances_b", "Anzahl markierter Umstände B"),
            field("circumstances_notes", "Zusätzliche Erläuterungen", true)
        )
    ),
    AccidentSection(
        "12. Abweichende Angaben / Bemerkungen",
        listOf(
            field("different_accounts", "Abweichende Schilderungen", true),
            field("remarks", "Weitere Bemerkungen", true)
        )
    ),
    AccidentSection(
        "13. Bestätigung und Unterschriften",
        listOf(
            field("a_signature_name", "Name Beteiligter A"),
            field("a_signature_date", "Datum / Uhrzeit Unterschrift A"),
            field("b_signature_name", "Name Beteiligter B"),
            field("b_signature_date", "Datum / Uhrzeit Unterschrift B"),
            field("signature_note", "Hinweis zur Bestätigung", true)
        )
    ),
    AccidentSection(
        "14. Zusätzliche Versicherungsangaben",
        listOf(
            field("a_green_card", "Grüne Karte / Versicherungsbestätigung A"),
            field("b_green_card", "Grüne Karte / Versicherungsbestätigung B"),
            field("vehicle_driveable", "Fahrzeuge fahrbereit?"),
            field("trailers", "Anhänger / weitere Fahrzeuge", true)
        )
    ),
    AccidentSection(
        "15. Dokumentinformation",
        listOf(
            field("created_by", "Erstellt mit CARLOCA"),
            field("document_version", "Dokumentversion"),
            field("document_date", "Erstellungsdatum")
        )
    )
)

private val europeanCircumstances = listOf(
    "1. Fahrzeug war geparkt oder stand am Straßenrand",
    "2. Fahrzeug verließ einen Parkplatz / öffnete eine Tür",
    "3. Fahrzeug fuhr in einen Parkplatz ein",
    "4. Fahrzeug kam aus einem Parkplatz oder Grundstück",
    "5. Fahrzeug fuhr in ein Grundstück / einen Parkplatz",
    "6. Fahrzeug fuhr in einen Kreisverkehr ein",
    "7. Fahrzeug fuhr im Kreisverkehr",
    "8. Auffahren auf ein Fahrzeug in gleicher Richtung und Spur",
    "9. Fahrzeuge fuhren in gleicher Richtung auf verschiedenen Spuren",
    "10. Fahrstreifenwechsel",
    "11. Überholvorgang",
    "12. Rechtsabbiegen",
    "13. Linksabbiegen",
    "14. Rückwärtsfahren",
    "15. Fahren auf der Gegenfahrbahn",
    "16. Fahrzeug kam von rechts",
    "17. Vorrangzeichen / Rotlicht nicht beachtet"
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
                    // Beschädigte lokale Daten werden nicht übernommen.
                }
            }
        }
    }

    val checkedCircumstancesA = remember {
        mutableStateMapOf<Int, Boolean>()
    }
    val checkedCircumstancesB = remember {
        mutableStateMapOf<Int, Boolean>()
    }

    fun saveLocally() {
        val json = JSONObject()
        values.forEach { (key, value) ->
            json.put(key, value)
        }
        preferences.edit()
            .putString("report_data", json.toString())
            .apply()
    }

    LaunchedEffect(values.size) {
        saveLocally()
    }

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

        Spacer(Modifier.height(5.dp))

        Text(
            text = "DIGITALER UNFALLBERICHT",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Am Europäischen Unfallbericht orientierte Dokumentation",
            color = Color.LightGray,
            fontSize = 12.sp
        )

        Spacer(Modifier.height(16.dp))

        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF29251A)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Wichtig: Bei Verletzten oder Gefahr zuerst Rettung " +
                    "und Polizei verständigen. Nur Tatsachen eintragen. " +
                    "Nichts unterschreiben, was nicht verstanden wurde. " +
                    "Eine Unterschrift ist kein Schuldeingeständnis.",
                color = Color.White,
                modifier = Modifier.padding(14.dp),
                fontSize = 13.sp
            )
        }

        Spacer(Modifier.height(16.dp))

        accidentSections.forEach { section ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1D1D1D)
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp)
                ) {
                    Text(
                        text = section.title,
                        color = AccidentGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )

                    Spacer(Modifier.height(10.dp))

                    section.fields.forEach { item ->
                        OutlinedTextField(
                            value = values[item.id] ?: "",
                            onValueChange = {
                                values[item.id] = it
                                saveLocally()
                            },
                            label = {
                                Text(item.label)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            minLines = if (item.multiline) 3 else 1,
                            maxLines = if (item.multiline) 6 else 1,
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    if (section.title.startsWith("11.")) {
                        Text(
                            text = "Bitte nur die Umstände markieren, " +
                                "die auf das jeweilige Fahrzeug zutreffen.",
                            color = Color.LightGray,
                            fontSize = 12.sp
                        )

                        Spacer(Modifier.height(8.dp))

                        Text(
                            text = "Fahrzeug A",
                            color = AccidentGold,
                            fontWeight = FontWeight.Bold
                        )

                        europeanCircumstances.forEachIndexed { index, label ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = checkedCircumstancesA[index] == true,
                                    onCheckedChange = {
                                        checkedCircumstancesA[index] = it
                                        values["circumstances_a"] =
                                            checkedCircumstancesA.values.count { v -> v }.toString()
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

                        Text(
                            text = "Fahrzeug B",
                            color = AccidentGold,
                            fontWeight = FontWeight.Bold
                        )

                        europeanCircumstances.forEachIndexed { index, label ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = checkedCircumstancesB[index] == true,
                                    onCheckedChange = {
                                        checkedCircumstancesB[index] = it
                                        values["circumstances_b"] =
                                            checkedCircumstancesB.values.count { v -> v }.toString()
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
        }

        Text(
            text = "Bestätigung",
            color = AccidentGold,
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp
        )

        Text(
            text = "Die Unterschriften bestätigen die Kenntnisnahme und " +
                "Dokumentation der Angaben. Sie sind kein Schuldeingeständnis " +
                "und keine verbindliche Feststellung der Haftung.",
            color = Color.LightGray,
            fontSize = 12.sp,
            modifier = Modifier.padding(vertical = 10.dp)
        )

        Button(
            onClick = {
                saveLocally()
                createAccidentPdf(context, values.toMap())
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("PDF erstellen")
        }

        Spacer(Modifier.height(8.dp))

        TextButton(
            onClick = {
                saveLocally()
                onBack()
            }
        ) {
            Text(
                text = "Zurück",
                color = AccidentGold
            )
        }

        Spacer(Modifier.height(24.dp))
    }
}

private fun createAccidentPdf(
    context: Context,
    values: Map<String, String>
) {
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

    fun drawHeader() {
        paint.color = android.graphics.Color.rgb(20, 20, 20)
        paint.textSize = 19f
        paint.typeface = android.graphics.Typeface.DEFAULT_BOLD
        canvas.drawText("CARLOCA – UNFALLBERICHT", margin, y, paint)
        y += 25f

        paint.textSize = 9f
        paint.typeface = android.graphics.Typeface.DEFAULT
        canvas.drawText(
            "Am Europäischen Unfallbericht orientierte Dokumentation",
            margin,
            y,
            paint
        )
        y += 22f
    }

    fun newPage() {
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
        drawHeader()
    }

    fun ensureSpace(height: Float) {
        if (y + height > pageHeight - 48f) {
            newPage()
        }
    }

    fun drawWrappedText(
        text: String,
        textSize: Float,
        bold: Boolean = false
    ) {
        paint.color = android.graphics.Color.BLACK
        paint.textSize = textSize
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
                ensureSpace(15f)
                canvas.drawText(line, margin, y, paint)
                y += 13f
                line = word
            } else {
                line = candidate
            }
        }

        if (line.isNotEmpty()) {
            ensureSpace(15f)
            canvas.drawText(line, margin, y, paint)
            y += 14f
        }
    }

    drawHeader()

    accidentSections.forEach { section ->
        ensureSpace(28f)
        y += 8f

        drawWrappedText(section.title, 12f, true)

        section.fields.forEach { item ->
            val value = values[item.id].orEmpty()
            if (value.isNotBlank()) {
                drawWrappedText("${item.label}: $value", 9f)
            }
        }

        if (section.title.startsWith("11.")) {
            drawWrappedText(
                "Markierte Umstände Fahrzeug A: " +
                    values["circumstances_a"].orEmpty(),
                9f
            )
            drawWrappedText(
                "Markierte Umstände Fahrzeug B: " +
                    values["circumstances_b"].orEmpty(),
                9f
            )
        }
    }

    y += 15f
    drawWrappedText(
        "Hinweis: Die Unterschriften bestätigen die Kenntnisnahme " +
            "und Dokumentation der Angaben. Sie sind kein " +
            "Schuldeingeständnis und keine verbindliche Feststellung " +
            "der Haftung.",
        9f
    )

    drawWrappedText(
        "Erstellt mit CARLOCA am " +
            SimpleDateFormat(
                "dd.MM.yyyy HH:mm",
                Locale.GERMANY
            ).format(Date()),
        8f
    )

    document.finishPage(page)

    val directory = File(
        context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
        "CARLOCA"
    )

    if (!directory.exists()) {
        directory.mkdirs()
    }

    val fileName = "CARLOCA_Unfallbericht_" +
        SimpleDateFormat("yyyyMMdd_HHmmss", Locale.GERMANY)
            .format(Date()) + ".pdf"

    val file = File(directory, fileName)

    try {
        FileOutputStream(file).use { output ->
            document.writeTo(output)
        }
    } catch (_: Exception) {
        // Fehlerbehandlung und Rückmeldung werden im nächsten Schritt ergänzt.
    } finally {
        document.close()
    }
}
