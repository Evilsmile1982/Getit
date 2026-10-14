
package de.autocheck.app

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONArray
import org.json.JSONObject
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val PDF_MAINTENANCE_V3 =
    "[WARTUNG_ENTRY_V3]"

private val PdfGold = Color(0xFFD4AF37)
private val PdfBackground = Color(0xFF101010)
private val PdfPanel = Color(0xFF222222)

private val pdfSections = listOf(
    "Fahrzeugdaten",
    "Wartungen",
    "Reparaturen",
    "Pickerl / TÜV",
    "Serviceintervalle",
    "Reifen",
    "Tankhistorie"
)

@Composable
fun PdfDocumentsScreen(
    store: VehicleStore,
    vehicles: List<Vehicle>,
    activeVehicleId: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    var selectedVehicleId by remember(activeVehicleId) {
        mutableStateOf(activeVehicleId)
    }

    var vehicleMenuExpanded by remember {
        mutableStateOf(false)
    }

    var statusMessage by remember {
        mutableStateOf(
            "Wähle die gewünschten Inhalte und öffne anschließend die Berichtsvorschau."
        )
    }

    val selections = remember {
        mutableStateMapOf<String, Boolean>().apply {
            pdfSections.forEach { section ->
                this[section] = section == "Fahrzeugdaten" ||
                    section == "Wartungen"
            }
        }
    }

    val selectedVehicle = vehicles.firstOrNull {
        it.id == selectedVehicleId
    }

    fun openPdfPreview() {
        val vehicle = selectedVehicle

        if (vehicle == null) {
            statusMessage = "Bitte wähle ein Fahrzeug aus."
            return
        }

        if (selections.values.none { it }) {
            statusMessage =
                "Bitte wähle mindestens einen PDF-Inhalt aus."
            return
        }

        try {
            val pdfBytes = createVehiclePdf(
                context = context,
                store = store,
                vehicle = vehicle,
                selections = selections.toMap()
            )

            val safeName = vehicle.name
                .replace(Regex("[^A-Za-z0-9_-]"), "_")
                .ifBlank { "Fahrzeug" }

            openVehiclePrintPreview(
                context = context,
                pdfBytes = pdfBytes,
                documentName = "CARVITA_${safeName}_Bericht"
            )

            statusMessage =
                "Die Druckvorschau wurde geöffnet. Dort kannst du den Bericht prüfen, als PDF speichern oder einen Drucker auswählen."
        } catch (error: Exception) {
            statusMessage =
                "PDF konnte nicht erstellt werden: " +
                    (error.localizedMessage ?: "Unbekannter Fehler")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .background(PdfBackground)
            .padding(
                top = 28.dp,
                start = 18.dp,
                end = 18.dp,
                bottom = 12.dp
            ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "PDF erstellen",
            color = PdfGold,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 8.dp)
        )

        Text(
            text = "Wähle die Inhalte für deinen Fahrzeugbericht. Danach kannst du ihn vor dem Speichern oder Drucken prüfen.",
            color = Color.LightGray,
            fontSize = 14.sp
        )

        if (vehicles.isEmpty()) {
            PdfPanelCard {
                Text(
                    "Es ist noch kein Fahrzeug gespeichert. Lege zuerst unter „Mein Auto“ ein Fahrzeug an.",
                    color = Color.White
                )
            }
        } else {
            OutlinedButton(
                onClick = { vehicleMenuExpanded = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    selectedVehicle?.let {
                        "${it.name} · ${it.make} ${it.model}"
                    } ?: "Fahrzeug auswählen"
                )
            }

            DropdownMenu(
                expanded = vehicleMenuExpanded,
                onDismissRequest = {
                    vehicleMenuExpanded = false
                }
            ) {
                vehicles.forEach { vehicle ->
                    DropdownMenuItem(
                        text = {
                            Text("${vehicle.name} · ${vehicle.make} ${vehicle.model}")
                        },
                        onClick = {
                            selectedVehicleId = vehicle.id
                            vehicleMenuExpanded = false
                        }
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(
                    top = 4.dp,
                    bottom = 12.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    PdfPanelCard {
                        Text(
                            "Inhalte auswählen",
                            color = PdfGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )

                        pdfSections.forEach { section ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = selections[section] == true,
                                    onCheckedChange = { checked ->
                                        selections[section] = checked
                                    }
                                )

                                Text(
                                    text = section,
                                    color = Color.White,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                selectedVehicle?.let { vehicle ->
                    item {
                        PdfPanelCard {
                            Text(
                                "Fahrzeugvorschau",
                                color = PdfGold,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                "${vehicle.name} – ${vehicle.make} ${vehicle.model}",
                                color = Color.White
                            )

                            Text(
                                "Fahrzeugart: ${vehicle.vehicleType}",
                                color = Color.LightGray
                            )

                            Text(
                                "Baujahr: ${vehicle.year}",
                                color = Color.LightGray
                            )

                            Text(
                                "Kennzeichen: ${vehicle.plate.ifBlank { "–" }}",
                                color = Color.LightGray
                            )

                            Text(
                                "FIN: ${vehicle.vin.ifBlank { "–" }}",
                                color = Color.LightGray
                            )
                        }
                    }
                }

                item {
                    Button(
                        onClick = ::openPdfPreview,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = selectedVehicle != null
                    ) {
                        Text(
                            "Vorschau öffnen – PDF / Drucken",
                            color = Color.Black
                        )
                    }
                }

                item {
                    PdfPanelCard {
                        Text(
                            statusMessage,
                            color = Color.White,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Zurück zur zweiten Startseite")
        }
    }
}

@Composable
private fun PdfPanelCard(
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = PdfPanel
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            content()
        }
    }
}

/**
 * Öffnet die native Android-Druckvorschau.
 *
 * Das PDF wird zunächst nur im Arbeitsspeicher erzeugt.
 * Erst wenn der Nutzer in der Druckansicht einen Speicherort
 * auswählt, wird dort eine PDF-Datei angelegt.
 */
private fun openVehiclePrintPreview(
    context: Context,
    pdfBytes: ByteArray,
    documentName: String
) {
    val printManager = context.getSystemService(
        Context.PRINT_SERVICE
    ) as PrintManager

    val adapter = object : PrintDocumentAdapter() {

        override fun onLayout(
            oldAttributes: PrintAttributes?,
            newAttributes: PrintAttributes?,
            cancellationSignal: CancellationSignal?,
            callback: LayoutResultCallback,
            extras: Bundle?
        ) {
            if (cancellationSignal?.isCanceled == true) {
                callback.onLayoutCancelled()
                return
            }

            val info = PrintDocumentInfo.Builder(
                "$documentName.pdf"
            )
                .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                .setPageCount(PrintDocumentInfo.PAGE_COUNT_UNKNOWN)
                .build()

            callback.onLayoutFinished(info, true)
        }

        override fun onWrite(
            pages: Array<out PageRange>,
            destination: ParcelFileDescriptor,
            cancellationSignal: CancellationSignal,
            callback: WriteResultCallback
        ) {
            try {
                if (cancellationSignal.isCanceled) {
                    callback.onWriteCancelled()
                    return
                }

                FileOutputStream(destination.fileDescriptor).use { output ->
                    output.write(pdfBytes)
                    output.flush()
                }

                if (cancellationSignal.isCanceled) {
                    callback.onWriteCancelled()
                } else {
                    callback.onWriteFinished(
                        arrayOf(PageRange.ALL_PAGES)
                    )
                }
            } catch (error: Exception) {
                callback.onWriteFailed(
                    error.localizedMessage ?: "PDF konnte nicht ausgegeben werden."
                )
            }
        }
    }

    printManager.print(
        documentName,
        adapter,
        PrintAttributes.Builder()
            .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
            .setColorMode(PrintAttributes.COLOR_MODE_COLOR)
            .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
            .build()
    )
}

private fun createVehiclePdf(
    context: Context,
    store: VehicleStore,
    vehicle: Vehicle,
    selections: Map<String, Boolean>
): ByteArray {
    val document = PdfDocument()

    try {
        val pageWidth = 595
        val pageHeight = 842
        val margin = 42f

        val bodyPaint = Paint().apply {
            color = android.graphics.Color.rgb(35, 35, 35)
            textSize = 10f
            isAntiAlias = true
        }

        val titlePaint = Paint().apply {
            color = android.graphics.Color.rgb(120, 91, 14)
            textSize = 18f
            isFakeBoldText = true
            isAntiAlias = true
        }

        val sectionPaint = Paint().apply {
            color = android.graphics.Color.rgb(120, 91, 14)
            textSize = 12f
            isFakeBoldText = true
            isAntiAlias = true
        }

        val lines = mutableListOf<Pair<String, Int>>()

        fun section(title: String) {
            lines.add(title to 1)
        }

        fun line(value: String) {
            lines.add(value to 0)
        }

        if (selections["Fahrzeugdaten"] == true) {
            section("FAHRZEUGDATEN")
            line("Name: ${vehicle.name}")
            line("Marke / Modell: ${vehicle.make} ${vehicle.model}")
            line("Fahrzeugart: ${vehicle.vehicleType}")
            line("Baujahr: ${vehicle.year}")
            line("Motorisierung: ${vehicle.motorization.ifBlank { "–" }}")
            line("Leistung: ${vehicle.power.ifBlank { "–" }}")
            line("Kennzeichen: ${vehicle.plate.ifBlank { "–" }}")
            line("FIN: ${vehicle.vin.ifBlank { "–" }}")
            line("Kaufpreis: ${vehicle.purchasePrice.ifBlank { "–" }}")
            line("")
        }

        if (selections["Reparaturen"] == true) {
            val repairs = store.loadRepairs()
                .filter { it.vehicle == vehicle.name }

            section("REPARATUREN (${repairs.size})")

            if (repairs.isEmpty()) {
                line("Keine Reparaturen gespeichert.")
            }

            repairs.forEach {
                line(
                    "${it.date} · ${
                        it.description.ifBlank {
                            it.repairDescription.ifBlank { "Reparatur" }
                        }
                    }"
                )

                if (it.workshop.isNotBlank()) {
                    line("Werkstatt: ${it.workshop}")
                }

                if (it.mileage.isNotBlank()) {
                    line("Kilometerstand: ${it.mileage} km")
                }

                if (it.cost.isNotBlank()) {
                    line("Kosten: ${it.cost} €")
                }

                line("")
            }
        }

        if (selections["Wartungen"] == true) {
            val maintenance = store.loadMaintenance()
                .filter { it.vehicle == vehicle.name }

            section("WARTUNGEN (${maintenance.size})")

            if (maintenance.isEmpty()) {
                line("Keine Wartungen gespeichert.")
            }

            maintenance.forEach {
                val decoded = decodePdfMaintenance(
                    encodedNotes = it.notes,
                    fallbackDate = it.date,
                    fallbackMileage = it.mileage,
                    fallbackCost = it.cost
                )

                line("${decoded.date} · ${decoded.name}")

                if (it.workshop.isNotBlank()) {
                    line("Werkstatt: ${it.workshop}")
                }

                if (decoded.mileage.isNotBlank()) {
                    line("Kilometerstand: ${decoded.mileage} km")
                }

                if (decoded.used.isNotBlank()) {
                    line("Verwendet: ${decoded.used}")
                }

                if (decoded.partNumber.isNotBlank()) {
                    line("Teilenummer: ${decoded.partNumber}")
                }

                if (decoded.materialCost > 0.0) {
                    line(
                        "Materialkosten: ${
                            pdfMoneyText(decoded.materialCost)
                        }"
                    )
                }

                if (decoded.laborCost > 0.0) {
                    line(
                        "Arbeitskosten: ${
                            pdfMoneyText(decoded.laborCost)
                        }"
                    )
                }

                val total = decoded.materialCost + decoded.laborCost

                if (total > 0.0) {
                    line("Gesamtkosten: ${pdfMoneyText(total)}")
                } else if (decoded.materialCost == 0.0 &&
                    decoded.laborCost == 0.0 &&
                    it.cost.isNotBlank()
                ) {
                    line("Kosten: ${it.cost} €")
                }

                if (decoded.notes.isNotBlank()) {
                    line("Notiz: ${decoded.notes}")
                }

                line("")
            }
        }

        if (selections["Pickerl / TÜV"] == true) {
            val pickerl = store.loadPickerl()
                .filter { it.vehicle == vehicle.name }

            section("PICKERL / TÜV (${pickerl.size})")

            if (pickerl.isEmpty()) {
                line("Keine Pickerl-/TÜV-Einträge gespeichert.")
            }

            pickerl.forEach {
                line("Letzte Prüfung: ${it.lastDate.ifBlank { "–" }}")
                line("Nächster Termin: ${it.nextDate.ifBlank { "–" }}")

                if (it.notes.isNotBlank()) {
                    line("Hinweise: ${it.notes}")
                }

                line("")
            }
        }

        if (selections["Serviceintervalle"] == true) {
            val services = store.loadServiceIntervals()
                .filter { it.vehicle == vehicle.name }

            section("SERVICEINTERVALLE (${services.size})")

            if (services.isEmpty()) {
                line("Keine Serviceintervalle gespeichert.")
            }

            services.forEach {
                line(
                    "Durchgeführt: ${it.serviceMonth}/${it.serviceYear} · " +
                        "${it.serviceKm.ifBlank { "–" }} km"
                )

                line(
                    "Nächster Service: " +
                        "${it.nextServiceMonth.ifBlank { "–" }}/" +
                        "${it.nextServiceYear.ifBlank { "–" }} · " +
                        "${it.nextServiceKm.ifBlank { "–" }} km"
                )

                if (it.documentation.isNotBlank()) {
                    line("Dokumentation: ${it.documentation}")
                }

                if (it.cost.isNotBlank()) {
                    line("Kosten: ${it.cost} €")
                }

                line("")
            }
        }

        if (selections["Reifen"] == true) {
            val tires = store.loadTires()
                .filter { it.vehicle == vehicle.name }

            section("REIFEN (${tires.size})")

            if (tires.isEmpty()) {
                line("Keine Reifeneinträge gespeichert.")
            }

            tires.forEach {
                line("${it.season} · ${it.brand} · ${it.dimension}")

                if (it.rimType.isNotBlank()) line("Felgen: ${it.rimType}")
                if (it.dot.isNotBlank()) line("DOT: ${it.dot}")
                if (it.tread.isNotBlank()) line("Profil: ${it.tread}")
                if (it.condition.isNotBlank()) line("Zustand: ${it.condition}")
                if (it.storage.isNotBlank()) line("Lagerung: ${it.storage}")
                if (it.price.isNotBlank()) line("Preis: ${it.price} €")

                line("")
            }
        }

        if (selections["Tankhistorie"] == true) {
            val preferences = context.getSharedPreferences(
                "carvita_fuel_data",
                Context.MODE_PRIVATE
            )

            val json = preferences.getString(
                "fuel_${vehicle.id}",
                "[]"
            ) ?: "[]"

            val fuelEntries = JSONArray(json)

            section("TANKHISTORIE (${fuelEntries.length()})")

            if (fuelEntries.length() == 0) {
                line("Keine Tankvorgänge gespeichert.")
            }

            for (index in 0 until fuelEntries.length()) {
                val entry = fuelEntries.optJSONObject(index) ?: continue

                val rawDate = entry.optString("date")
                val date = formatFuelDateForPdf(rawDate)

                line("Datum: $date")
                line("Kilometerstand: ${entry.optInt("odometer")} km")
                line(
                    "Getankt: ${
                        String.format(
                            Locale.GERMANY,
                            "%.2f l",
                            entry.optDouble("liters")
                        )
                    }"
                )
                line(
                    "Gesamtpreis: ${
                        String.format(
                            Locale.GERMANY,
                            "%.2f €",
                            entry.optDouble("totalCost")
                        )
                    }"
                )
                line("")
            }
        }

        if (lines.isEmpty()) {
            lines.add("Keine Inhalte ausgewählt." to 0)
        }

        val date = SimpleDateFormat(
            "dd.MM.yyyy HH:mm",
            Locale.GERMANY
        ).format(Date())

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

        fun drawHeader(includeDate: Boolean) {
            canvas.drawText(
                "CARVITA · Fahrzeugbericht",
                margin,
                y,
                titlePaint
            )

            y += 22f

            if (includeDate) {
                canvas.drawText(
                    "Erstellt am: $date",
                    margin,
                    y,
                    bodyPaint
                )

                y += 26f
            } else {
                y += 4f
            }
        }

        fun startPage() {
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
            drawHeader(includeDate = false)
        }

        drawHeader(includeDate = true)

        lines.forEach { (text, type) ->
            if (y > pageHeight - 45f) {
                startPage()
            }

            if (text.isBlank()) {
                y += 8f
            } else {
                val paint = if (type == 1) sectionPaint else bodyPaint

                if (type == 1) y += 5f

                val maxChars = if (type == 1) 78 else 95

                text.chunked(maxChars).forEach { part ->
                    if (y > pageHeight - 45f) {
                        startPage()
                    }

                    canvas.drawText(part, margin, y, paint)
                    y += if (type == 1) 17f else 14f
                }
            }
        }

        document.finishPage(page)

        val output = java.io.ByteArrayOutputStream()
        document.writeTo(output)

        return output.toByteArray()
    } finally {
        document.close()
    }
}

private data class PdfMaintenanceData(
    val name: String,
    val date: String,
    val mileage: String,
    val used: String,
    val partNumber: String,
    val materialCost: Double,
    val laborCost: Double,
    val notes: String
)

private fun decodePdfMaintenance(
    encodedNotes: String,
    fallbackDate: String,
    fallbackMileage: String,
    fallbackCost: String
): PdfMaintenanceData {
    val notes = encodedNotes.trim()

    if (!notes.startsWith(PDF_MAINTENANCE_V3)) {
        return PdfMaintenanceData(
            name = notes.ifBlank { "Wartung" },
            date = fallbackDate,
            mileage = fallbackMileage,
            used = "",
            partNumber = "",
            materialCost = pdfMoney(fallbackCost),
            laborCost = 0.0,
            notes = ""
        )
    }

    return try {
        val json = JSONObject(
            notes.removePrefix(PDF_MAINTENANCE_V3)
        )

        val month = json.optString("month", "").trim()
        val year = json.optString("year", "").trim()

        val date = if (month.isNotBlank() && year.isNotBlank()) {
            "${year}-${month.padStart(2, '0')}"
        } else {
            fallbackDate
        }

        PdfMaintenanceData(
            name = json.optString("name", "Wartung")
                .trim()
                .ifBlank { "Wartung" },
            date = date,
            mileage = json.optString(
                "mileage",
                fallbackMileage
            ).trim().ifBlank { fallbackMileage },
            used = json.optString("used", "").trim(),
            partNumber = json.optString("partNumber", "").trim(),
            materialCost = pdfMoney(
                json.optString("materialCost", "")
            ),
            laborCost = pdfMoney(
                json.optString("laborCost", "")
            ),
            notes = json.optString("notes", "").trim()
        )
    } catch (_: Exception) {
        PdfMaintenanceData(
            name = "Wartung",
            date = fallbackDate,
            mileage = fallbackMileage,
            used = "",
            partNumber = "",
            materialCost = pdfMoney(fallbackCost),
            laborCost = 0.0,
            notes = ""
        )
    }
}

private fun pdfMoney(value: String): Double {
    val cleaned = value
        .replace("€", "")
        .replace("EUR", "", ignoreCase = true)
        .replace(" ", "")
        .trim()

    if (cleaned.isBlank()) return 0.0

    return try {
        if (cleaned.contains(",") && cleaned.contains(".")) {
            cleaned.replace(".", "").replace(",", ".").toDouble()
        } else {
            cleaned.replace(",", ".").toDouble()
        }
    } catch (_: NumberFormatException) {
        0.0
    }
}

private fun pdfMoneyText(value: Double): String {
    return String.format(
        Locale.GERMANY,
        "%.2f €",
        value
    )
}

private fun formatFuelDateForPdf(value: String): String {
    val formats = listOf(
        SimpleDateFormat("yyyy-MM-dd", Locale.ROOT),
        SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY)
    )

    formats.forEach { format ->
        format.isLenient = false

        try {
            val parsed = format.parse(value)
            if (parsed != null) {
                return SimpleDateFormat(
                    "dd.MM.yyyy",
                    Locale.GERMANY
                ).format(parsed)
            }
        } catch (_: Exception) {
            // Nächstes Format probieren.
        }
    }

    return value
}
