
package de.autocheck.app

import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val PdfGold = Color(0xFFD4AF37)
private val PdfBackground = Color(0xFF101010)
private val PdfPanel = Color(0xFF222222)

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
        mutableStateOf("Wähle ein Fahrzeug und erstelle deinen PDF-Bericht.")
    }
    var pendingPdf by remember {
        mutableStateOf<ByteArray?>(null)
    }

    val selectedVehicle = vehicles.firstOrNull {
        it.id == selectedVehicleId
    }

    val savePdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri ->
        val bytes = pendingPdf

        if (uri != null && bytes != null) {
            try {
                val output = context.contentResolver.openOutputStream(uri)
                    ?: throw IllegalStateException("Datei konnte nicht geöffnet werden.")

                output.use {
                    it.write(bytes)
                    it.flush()
                }

                statusMessage = "PDF wurde erfolgreich gespeichert."
            } catch (error: Exception) {
                statusMessage =
                    "Speichern fehlgeschlagen: " +
                        (error.localizedMessage ?: "Unbekannter Fehler")
            }
        } else if (uri == null) {
            statusMessage = "Speichern abgebrochen."
        }

        pendingPdf = null
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PdfBackground)
            .padding(top = 28.dp, start = 18.dp, end = 18.dp, bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "PDF & Dokumente",
            color = PdfGold,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 8.dp)
        )

        Text(
            text = "Erstelle einen PDF-Bericht mit Fahrzeugdaten und gespeicherten Einträgen.",
            color = Color.LightGray,
            fontSize = 14.sp
        )

        if (vehicles.isEmpty()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PdfPanelCard {
                    Text(
                        "Es ist noch kein Fahrzeug gespeichert. Lege zuerst unter „Mein Auto“ ein Fahrzeug an.",
                        color = Color.White
                    )
                }

                PdfPanelCard {
                    Text(
                        statusMessage,
                        color = Color.White,
                        fontSize = 13.sp
                    )
                }
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

            selectedVehicle?.let { vehicle ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(
                        top = 8.dp,
                        bottom = 12.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        PdfPanelCard {
                            Text(
                                "Im PDF-Bericht enthalten",
                                color = PdfGold,
                                fontWeight = FontWeight.Bold
                            )
                            Text("• Fahrzeugstammdaten", color = Color.White)
                            Text("• Reparaturen und Wartungen", color = Color.White)
                            Text("• Pickerl / TÜV", color = Color.White)
                            Text("• Serviceintervalle", color = Color.White)
                            Text("• Reifen und erfasste Kosten", color = Color.White)
                        }
                    }

                    item {
                        PdfPanelCard {
                            Text(
                                "Fahrzeug",
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

                OutlinedButton(
                    onClick = {
                        try {
                            pendingPdf = createVehiclePdf(store, vehicle)

                            val safeName = vehicle.name
                                .replace(Regex("[^A-Za-z0-9_-]"), "_")
                                .ifBlank { "Fahrzeug" }

                            savePdfLauncher.launch(
                                "CARVITA_${safeName}_Bericht.pdf"
                            )

                            statusMessage =
                                "Wähle im nächsten Fenster den Speicherort."
                        } catch (error: Exception) {
                            pendingPdf = null
                            statusMessage =
                                "PDF konnte nicht erstellt werden: " +
                                    (error.localizedMessage ?: "Unbekannter Fehler")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "PDF erstellen und speichern",
                        color = PdfGold
                    )
                }
            }

            PdfPanelCard {
                Text(
                    statusMessage,
                    color = Color.White,
                    fontSize = 13.sp
                )
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

private fun createVehiclePdf(
    store: VehicleStore,
    vehicle: Vehicle
): ByteArray {
    val document = PdfDocument()
    val output = ByteArrayOutputStream()

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

    section("FAHRZEUGDATEN")
    line("Name: ${vehicle.name}")
    line("Marke / Modell: ${vehicle.make} ${vehicle.model}")
    line("Fahrzeugart: ${vehicle.vehicleType}")
    line("Baujahr: ${vehicle.year}")
    line("Motorisierung: ${vehicle.motorization.ifBlank { "–" }}")
    line("Leistung: ${vehicle.power.ifBlank { "–" }}")
    line("Kennzeichen: ${vehicle.plate.ifBlank { "–" }}")
    line("Fahrzeug-Identifizierungsnummer: ${vehicle.vin.ifBlank { "–" }}")
    line("Kaufpreis: ${vehicle.purchasePrice.ifBlank { "–" }}")
    line("")

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
        if (it.workshop.isNotBlank()) line("  Werkstatt: ${it.workshop}")
        if (it.mileage.isNotBlank()) line("  Kilometerstand: ${it.mileage} km")
        if (it.cost.isNotBlank()) line("  Kosten: ${it.cost} €")
    }
    line("")

    val maintenance = store.loadMaintenance()
        .filter { it.vehicle == vehicle.name }

    section("WARTUNGEN (${maintenance.size})")
    if (maintenance.isEmpty()) {
        line("Keine Wartungen gespeichert.")
    }

    maintenance.forEach {
        line("${it.date} · ${it.notes.ifBlank { "Wartung" }}")
        if (it.workshop.isNotBlank()) line("  Werkstatt: ${it.workshop}")
        if (it.mileage.isNotBlank()) line("  Kilometerstand: ${it.mileage} km")
        if (it.cost.isNotBlank()) line("  Kosten: ${it.cost} €")
    }
    line("")

    val pickerl = store.loadPickerl()
        .filter { it.vehicle == vehicle.name }

    section("PICKERL / TÜV (${pickerl.size})")
    if (pickerl.isEmpty()) {
        line("Keine Pickerl-/TÜV-Einträge gespeichert.")
    }

    pickerl.forEach {
        line("Letzte Prüfung: ${it.lastDate.ifBlank { "–" }}")
        line("Nächster Termin: ${it.nextDate.ifBlank { "–" }}")
        if (it.notes.isNotBlank()) line("Hinweise: ${it.notes}")
    }
    line("")

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
        if (it.cost.isNotBlank()) line("Kosten: ${it.cost} €")
    }
    line("")

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
        canvas.drawText(
            "CARVITA · Fahrzeugbericht",
            margin,
            y,
            titlePaint
        )
        y += 24f
    }

    canvas.drawText(
        "CARVITA · Fahrzeugbericht",
        margin,
        y,
        titlePaint
    )
    y += 22f

    canvas.drawText(
        "Erstellt am: $date",
        margin,
        y,
        bodyPaint
    )
    y += 26f

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
    document.writeTo(output)
    document.close()

    return output.toByteArray()
}
