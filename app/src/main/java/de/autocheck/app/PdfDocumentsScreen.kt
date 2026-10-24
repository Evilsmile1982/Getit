
package de.autocheck.app

import android.content.Context
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
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
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.autocheck.app.data.PickerlDocument
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val PDF_MAINTENANCE_V3 = "[WARTUNG_ENTRY_V3]"

private val PdfGold = Color(0xFFD4AF37)
private val PdfBackground = Color(0xFF101010)
private val PdfPanel = Color(0xFF222222)

private val pdfSections = listOf(
    "Fahrzeugdaten",
    "Wartungen",
    "Reparaturen",
    "Pickerl / TÜV",
    "Pickerl-Dokumente",
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

    var previewPdfBytes by remember {
        mutableStateOf<ByteArray?>(null)
    }

    var previewDocumentName by remember { mutableStateOf("") }

    val documentStore = remember { PickerlDocumentStore(context) }

    var selectedVehicleId by remember(activeVehicleId) {
        mutableStateOf(activeVehicleId)
    }

    var vehicleMenuExpanded by remember { mutableStateOf(false) }

    var statusMessage by remember {
        mutableStateOf(
            "Wähle die gewünschten Inhalte und öffne anschließend die Berichtsvorschau."
        )
    }

    val selections = remember {
        mutableStateMapOf<String, Boolean>().apply {
            pdfSections.forEach { section ->
                this[section] =
                    section == "Fahrzeugdaten" || section == "Wartungen"
            }
        }
    }

    var pickerlDocuments by remember(selectedVehicleId) {
        mutableStateOf(
            documentStore.load().filter { document ->
                val vehicle = vehicles.firstOrNull {
                    it.id == selectedVehicleId
                }
                vehicle != null && document.vehicle == vehicle.name
            }
        )
    }

    val selectedVehicle = vehicles.firstOrNull {
        it.id == selectedVehicleId
    }

    fun updateDocumentSelection(
        document: PickerlDocument,
        checked: Boolean
    ) {
        val updated = documentStore.load().map { saved ->
            if (saved.id == document.id) {
                saved.copy(includeInPdf = checked)
            } else {
                saved
            }
        }

        documentStore.save(updated)
        pickerlDocuments = updated.filter {
            it.vehicle == selectedVehicle?.name
        }
    }

    fun openPdfPreview() {
        val vehicle = selectedVehicle

        if (vehicle == null) {
            statusMessage = "Bitte wähle ein Fahrzeug aus."
            return
        }

        if (selections.values.none { it }) {
            statusMessage = "Bitte wähle mindestens einen PDF-Inhalt aus."
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

            previewDocumentName = "CARVITA_${safeName}_Bericht"
            previewPdfBytes = pdfBytes
            statusMessage = "PDF wurde erstellt und für die Vorschau geöffnet."
        } catch (error: Exception) {
            statusMessage =
                "PDF konnte nicht erstellt werden: " +
                    (error.localizedMessage ?: "Unbekannter Fehler")
        }
    }

    previewPdfBytes?.let { pdfBytes ->
        PdfZoomPreview(
            pdfBytes = pdfBytes,
            documentName = previewDocumentName,
            onClose = { previewPdfBytes = null },
            onPrint = {
                openVehiclePrintPreview(
                    context = context,
                    pdfBytes = pdfBytes,
                    documentName = previewDocumentName
                )
            }
        )
        return
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
                onDismissRequest = { vehicleMenuExpanded = false }
            ) {
                vehicles.forEach { vehicle ->
                    DropdownMenuItem(
                        text = {
                            Text("${vehicle.name} · ${vehicle.make} ${vehicle.model}")
                        },
                        onClick = {
                            selectedVehicleId = vehicle.id
                            vehicleMenuExpanded = false
                            pickerlDocuments = documentStore.load().filter {
                                it.vehicle == vehicle.name
                            }
                        }
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(top = 4.dp, bottom = 12.dp),
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

                if (selections["Pickerl-Dokumente"] == true) {
                    item {
                        PdfPanelCard {
                            Text(
                                "Pickerl-Dokumente auswählen",
                                color = PdfGold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp
                            )

                            Text(
                                "Die Häkchen werden dauerhaft gespeichert. Nur markierte Dokumente werden an den Bericht angehängt.",
                                color = Color.LightGray,
                                fontSize = 13.sp
                            )

                            if (pickerlDocuments.isEmpty()) {
                                Text(
                                    "Für dieses Fahrzeug sind keine Pickerl-Dokumente gespeichert.",
                                    color = Color.White
                                )
                            } else {
                                pickerlDocuments
                                    .sortedByDescending { pdfSortDate(it.date) }
                                    .forEach { document ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 3.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Checkbox(
                                                checked = document.includeInPdf,
                                                onCheckedChange = { checked ->
                                                    updateDocumentSelection(
                                                        document,
                                                        checked
                                                    )
                                                }
                                            )

                                            Column(
                                                modifier = Modifier.weight(1f),
                                                verticalArrangement = Arrangement.spacedBy(2.dp)
                                            ) {
                                                Text(
                                                    document.type,
                                                    color = Color.White,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                                Text(
                                                    document.name,
                                                    color = Color.LightGray,
                                                    fontSize = 12.sp
                                                )
                                                Text(
                                                    document.date,
                                                    color = PdfGold,
                                                    fontSize = 12.sp
                                                )
                                            }
                                        }
                                    }
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
                            Text("Baujahr: ${vehicle.year}", color = Color.LightGray)
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
                        Text("Vorschau öffnen – PDF / Drucken", color = Color.Black)
                    }
                }

                item {
                    PdfPanelCard {
                        Text(statusMessage, color = Color.White, fontSize = 13.sp)
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
private fun PdfPanelCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = PdfPanel)
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

            val info = PrintDocumentInfo.Builder("$documentName.pdf")
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
                    callback.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
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
    val renderedPickerlPages =
        mutableListOf<Pair<String, android.graphics.Bitmap>>()

    try {
        val pageWidth = 595
        val pageHeight = 842
        val margin = 42f
        val contentWidth = pageWidth - margin * 2

        val gold = android.graphics.Color.rgb(120, 91, 14)
        val dark = android.graphics.Color.rgb(35, 35, 35)
        val muted = android.graphics.Color.rgb(105, 105, 105)
        val lightGold = android.graphics.Color.rgb(248, 243, 226)

        val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = dark
            textSize = 10f
        }

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = gold
            textSize = 20f
            isFakeBoldText = true
        }

        val sectionPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = gold
            textSize = 12f
            isFakeBoldText = true
        }

        val smallPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = muted
            textSize = 8f
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
            line("")
        }

        if (selections["Reparaturen"] == true) {
            val repairs = store.loadRepairs().filter { it.vehicle == vehicle.name }
            section("REPARATUREN (${repairs.size})")

            if (repairs.isEmpty()) line("Keine Reparaturen gespeichert.")

            repairs.forEach {
                line(
                    "${formatPdfDate(it.date)} · ${
                        it.description.ifBlank {
                            it.repairDescription.ifBlank { "Reparatur" }
                        }
                    }"
                )
                if (it.workshop.isNotBlank()) line("Werkstatt: ${it.workshop}")
                if (it.mileage.isNotBlank()) line("Kilometerstand: ${it.mileage} km")
                if (it.cost.isNotBlank()) line("Kosten: ${it.cost} €")
                line("")
            }
        }

        if (selections["Wartungen"] == true) {
            val maintenance = store.loadMaintenance()
                .filter { it.vehicle == vehicle.name }

            section("WARTUNGEN (${maintenance.size})")
            if (maintenance.isEmpty()) line("Keine Wartungen gespeichert.")

            maintenance.forEach {
                val decoded = decodePdfMaintenance(
                    encodedNotes = it.notes,
                    fallbackDate = it.date,
                    fallbackMileage = it.mileage,
                    fallbackCost = it.cost
                )

                line("${formatPdfDate(decoded.date)} · ${decoded.name}")
                if (it.workshop.isNotBlank()) line("Werkstatt: ${it.workshop}")
                if (decoded.mileage.isNotBlank()) {
                    line("Kilometerstand: ${decoded.mileage} km")
                }
                if (decoded.used.isNotBlank()) line("Verwendet: ${decoded.used}")
                if (decoded.partNumber.isNotBlank()) {
                    line("Teilenummer: ${decoded.partNumber}")
                }
                if (decoded.materialCost > 0.0) {
                    line("Materialkosten: ${pdfMoneyText(decoded.materialCost)}")
                }
                if (decoded.laborCost > 0.0) {
                    line("Arbeitskosten: ${pdfMoneyText(decoded.laborCost)}")
                }

                val total = decoded.materialCost + decoded.laborCost
                if (total > 0.0) {
                    line("Gesamtkosten: ${pdfMoneyText(total)}")
                } else if (
                    decoded.materialCost == 0.0 &&
                    decoded.laborCost == 0.0 &&
                    it.cost.isNotBlank()
                ) {
                    line("Kosten: ${it.cost} €")
                }

                if (decoded.notes.isNotBlank()) line("Notiz: ${decoded.notes}")
                line("")
            }
        }

        if (selections["Pickerl / TÜV"] == true) {
            val pickerl = store.loadPickerl().filter { it.vehicle == vehicle.name }
            section("PICKERL / TÜV (${pickerl.size})")
            if (pickerl.isEmpty()) line("Keine Pickerl-/TÜV-Einträge gespeichert.")

            pickerl.forEach {
                line("Letzte Prüfung: ${formatPdfDate(it.lastDate).ifBlank { "–" }}")
                line("Nächster Termin: ${formatPdfDate(it.nextDate).ifBlank { "–" }}")
                if (it.notes.isNotBlank()) line("Hinweise: ${it.notes}")
                line("")
            }
        }

        if (selections["Pickerl-Dokumente"] == true) {
            val selectedDocuments = PickerlDocumentStore(context)
                .load()
                .filter {
                    it.vehicle == vehicle.name &&
                        it.includeInPdf &&
                        it.uri.isNotBlank()
                }
                .sortedBy { pdfSortDate(it.date) }

            section("PICKERL-DOKUMENTE (${selectedDocuments.size})")

            if (selectedDocuments.isEmpty()) {
                line("Keine Pickerl-Dokumente für die PDF-Ausgabe ausgewählt.")
            } else {
                selectedDocuments.forEach { item ->
                    line("${formatPdfDate(item.date)} · ${item.type}")
                    line("Datei: ${item.name}")
                    line("")
                }

                renderedPickerlPages.addAll(
                    PickerlPdfPages.renderSelectedDocuments(
                        context = context,
                        vehicleName = vehicle.name
                    )
                )

                if (renderedPickerlPages.isEmpty()) {
                    line(
                        "Hinweis: Die ausgewählten Dateien konnten nicht als Seiten gerendert werden. Bitte Dateizugriff prüfen."
                    )
                } else if (renderedPickerlPages.size < selectedDocuments.size) {
                    line(
                        "Hinweis: Mindestens ein Dokument konnte nicht vollständig gerendert werden."
                    )
                }
            }
        }

        if (selections["Serviceintervalle"] == true) {
            val services = store.loadServiceIntervals()
                .filter { it.vehicle == vehicle.name }

            section("SERVICEINTERVALLE (${services.size})")
            if (services.isEmpty()) line("Keine Serviceintervalle gespeichert.")

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
                if (it.cost.isNotBlank()) line("Kosten: ${it.cost} €")
                line("")
            }
        }

        if (selections["Reifen"] == true) {
            val tires = store.loadTires().filter { it.vehicle == vehicle.name }
            section("REIFEN (${tires.size})")
            if (tires.isEmpty()) line("Keine Reifeneinträge gespeichert.")

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
            val json = preferences.getString("fuel_${vehicle.id}", "[]") ?: "[]"
            val fuelEntries = JSONArray(json)

            section("TANKHISTORIE (${fuelEntries.length()})")
            if (fuelEntries.length() == 0) line("Keine Tankvorgänge gespeichert.")

            for (index in 0 until fuelEntries.length()) {
                val entry = fuelEntries.optJSONObject(index) ?: continue
                line("Datum: ${formatFuelDateForPdf(entry.optString("date"))}")
                line("Kilometerstand: ${entry.optInt("odometer")} km")
                line(
                    "Getankt: ${
                        String.format(Locale.GERMANY, "%.2f l", entry.optDouble("liters"))
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

        if (lines.isEmpty()) lines.add("Keine Inhalte ausgewählt." to 0)

        val createdAt = SimpleDateFormat(
            "dd.MM.yyyy HH:mm",
            Locale.GERMANY
        ).format(Date())

        var pageNumber = 0
        var page: PdfDocument.Page? = null
        var canvas: android.graphics.Canvas? = null
        var y = 0f

        val footerY = pageHeight - 24f
        val contentBottom = footerY - 20f

        fun drawHeader(firstPage: Boolean) {
            val currentCanvas = canvas ?: return

            val banner = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = lightGold
                style = Paint.Style.FILL
            }

            currentCanvas.drawRect(
                0f,
                0f,
                pageWidth.toFloat(),
                82f,
                banner
            )

            currentCanvas.drawText(
                "CARVITA",
                margin,
                34f,
                titlePaint
            )

            val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = dark
                textSize = 10f
            }

            currentCanvas.drawText(
                "FAHRZEUGBERICHT",
                margin,
                53f,
                subtitlePaint
            )

            val rightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = muted
                textSize = 8f
                textAlign = Paint.Align.RIGHT
            }

            currentCanvas.drawText(
                if (firstPage) "Erstellt am: $createdAt" else vehicle.name,
                pageWidth - margin,
                34f,
                rightPaint
            )

            currentCanvas.drawLine(
                margin,
                82f,
                pageWidth - margin,
                82f,
                Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = gold
                    strokeWidth = 1.2f
                }
            )

            y = if (firstPage) 108f else 104f
        }

        fun drawFooter() {
            val currentCanvas = canvas ?: return

            val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = gold
                strokeWidth = 0.8f
            }

            currentCanvas.drawLine(
                margin,
                footerY - 12f,
                pageWidth - margin,
                footerY - 12f,
                linePaint
            )

            val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = muted
                textSize = 8f
            }

            currentCanvas.drawText(
                "CARVITA – Die App für all deine Fahrzeuge",
                margin,
                footerY,
                footerPaint
            )

            footerPaint.textAlign = Paint.Align.RIGHT
            currentCanvas.drawText(
                "Seite $pageNumber",
                pageWidth - margin,
                footerY,
                footerPaint
            )
        }

        fun finishCurrentPage() {
            val currentPage = page ?: return
            drawFooter()
            document.finishPage(currentPage)
            page = null
            canvas = null
        }

        fun startNewPage(firstPage: Boolean = false) {
            pageNumber++
            page = document.startPage(
                PdfDocument.PageInfo.Builder(
                    pageWidth,
                    pageHeight,
                    pageNumber
                ).create()
            )
            canvas = page!!.canvas
            drawHeader(firstPage)
        }

        fun ensureSpace(requiredHeight: Float) {
            if (y + requiredHeight > contentBottom) {
                finishCurrentPage()
                startNewPage()
            }
        }

        fun wrapText(text: String, paint: Paint): List<String> {
            if (text.isEmpty()) return listOf("")

            val result = mutableListOf<String>()
            val paragraphs = text.split("\n")

            paragraphs.forEach { paragraph ->
                if (paragraph.isEmpty()) {
                    result.add("")
                } else {
                    var current = ""

                    paragraph.split(Regex("\\s+")).forEach { word ->
                        val candidate = if (current.isEmpty()) {
                            word
                        } else {
                            "$current $word"
                        }

                        if (paint.measureText(candidate) <= contentWidth) {
                            current = candidate
                        } else {
                            if (current.isNotEmpty()) result.add(current)

                            if (paint.measureText(word) <= contentWidth) {
                                current = word
                            } else {
                                var part = ""
                                word.forEach { character ->
                                    val next = part + character
                                    if (
                                        part.isNotEmpty() &&
                                        paint.measureText(next) > contentWidth
                                    ) {
                                        result.add(part)
                                        part = character.toString()
                                    } else {
                                        part = next
                                    }
                                }
                                current = part
                            }
                        }
                    }

                    if (current.isNotEmpty()) result.add(current)
                }
            }

            return result.ifEmpty { listOf("") }
        }

        startNewPage(firstPage = true)

        lines.forEach { (text, type) ->
            if (type == 1) {
                ensureSpace(30f)
                y += 5f

                val currentCanvas = canvas ?: return@forEach
                val headingRect = RectF(
                    margin - 4f,
                    y - 12f,
                    pageWidth - margin + 4f,
                    y + 7f
                )

                val headingBackground = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = lightGold
                    style = Paint.Style.FILL
                }

                currentCanvas.drawRoundRect(
                    headingRect,
                    3f,
                    3f,
                    headingBackground
                )

                currentCanvas.drawText(text, margin, y, sectionPaint)
                y += 21f
            } else if (text.isBlank()) {
                y += 7f
            } else {
                val wrapped = wrapText(text, bodyPaint)

                wrapped.forEach { part ->
                    ensureSpace(15f)
                    canvas?.drawText(part, margin, y, bodyPaint)
                    y += 14f
                }
            }
        }

        finishCurrentPage()

        renderedPickerlPages.forEach { (label, bitmap) ->
            pageNumber++

            val attachmentPage = document.startPage(
                PdfDocument.PageInfo.Builder(
                    pageWidth,
                    pageHeight,
                    pageNumber
                ).create()
            )

            val attachmentCanvas = attachmentPage.canvas

            attachmentCanvas.drawRect(
                0f,
                0f,
                pageWidth.toFloat(),
                70f,
                Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = lightGold
                    style = Paint.Style.FILL
                }
            )

            attachmentCanvas.drawText(
                "CARVITA",
                margin,
                30f,
                titlePaint
            )

            val attachmentTitlePaint = Paint(bodyPaint).apply {
                textSize = 9f
                color = dark
            }

            attachmentCanvas.drawText(
                "PICKERL-DOKUMENTATION",
                margin,
                49f,
                attachmentTitlePaint
            )

            val labelPaint = Paint(bodyPaint).apply {
                textSize = 8f
                color = muted
            }

            attachmentCanvas.drawText(
                label.take(90),
                margin,
                85f,
                labelPaint
            )

            val available = RectF(
                margin,
                100f,
                pageWidth - margin,
                pageHeight - 55f
            )

            val scale = minOf(
                available.width() / bitmap.width.toFloat(),
                available.height() / bitmap.height.toFloat()
            )

            val drawWidth = bitmap.width * scale
            val drawHeight = bitmap.height * scale
            val left = available.left + (available.width() - drawWidth) / 2f
            val top = available.top + (available.height() - drawHeight) / 2f

            attachmentCanvas.drawBitmap(
                bitmap,
                null,
                RectF(left, top, left + drawWidth, top + drawHeight),
                Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    isFilterBitmap = true
                }
            )

            attachmentCanvas.drawLine(
                margin,
                footerY - 12f,
                pageWidth - margin,
                footerY - 12f,
                Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = gold
                    strokeWidth = 0.8f
                }
            )

            val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = muted
                textSize = 8f
            }

            attachmentCanvas.drawText(
                "CARVITA – Die App für all deine Fahrzeuge",
                margin,
                footerY,
                footerPaint
            )

            footerPaint.textAlign = Paint.Align.RIGHT
            attachmentCanvas.drawText(
                "Seite $pageNumber",
                pageWidth - margin,
                footerY,
                footerPaint
            )

            document.finishPage(attachmentPage)
        }

        val output = ByteArrayOutputStream()
        document.writeTo(output)
        return output.toByteArray()
    } finally {
        document.close()

        renderedPickerlPages.forEach { (_, bitmap) ->
            if (!bitmap.isRecycled) bitmap.recycle()
        }
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
        val json = JSONObject(notes.removePrefix(PDF_MAINTENANCE_V3))
        val month = json.optString("month", "").trim()
        val year = json.optString("year", "").trim()

        val date = if (month.isNotBlank() && year.isNotBlank()) {
            "$year-${month.padStart(2, '0')}"
        } else {
            fallbackDate
        }

        PdfMaintenanceData(
            name = json.optString("name", "Wartung")
                .trim().ifBlank { "Wartung" },
            date = date,
            mileage = json.optString("mileage", fallbackMileage)
                .trim().ifBlank { fallbackMileage },
            used = json.optString("used", "").trim(),
            partNumber = json.optString("partNumber", "").trim(),
            materialCost = pdfMoney(json.optString("materialCost", "")),
            laborCost = pdfMoney(json.optString("laborCost", "")),
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
    return String.format(Locale.GERMANY, "%.2f €", value)
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

private fun formatPdfDate(value: String): String {
    val trimmed = value.trim()

    if (trimmed.matches(Regex("""\d{4}-\d{2}-\d{2}"""))) {
        val parts = trimmed.split("-")
        return "${parts[2]}.${parts[1]}.${parts[0]}"
    }

    if (trimmed.matches(Regex("""\d{4}-\d{1,2}"""))) {
        val parts = trimmed.split("-")
        return "${parts[1].padStart(2, '0')}.${parts[0]}"
    }

    if (trimmed.matches(Regex("""\d{1,2}\.\d{1,2}\.\d{4}"""))) {
        val parts = trimmed.split(".")
        return "${parts[0].padStart(2, '0')}." +
            "${parts[1].padStart(2, '0')}." +
            parts[2]
    }

    return trimmed
}

private fun pdfSortDate(value: String): String {
    val trimmed = value.trim()

    if (trimmed.matches(Regex("""\d{4}-\d{2}-\d{2}"""))) {
        return trimmed
    }

    if (trimmed.matches(Regex("""\d{4}-\d{1,2}"""))) {
        val parts = trimmed.split("-")
        return "${parts[0]}-${parts[1].padStart(2, '0')}-01"
    }

    if (trimmed.matches(Regex("""\d{2}\.\d{2}\.\d{4}"""))) {
        val parts = trimmed.split(".")
        return "${parts[2]}-${parts[1]}-${parts[0]}"
    }

    return trimmed
}
