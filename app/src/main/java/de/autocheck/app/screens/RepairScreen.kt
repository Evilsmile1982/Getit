package de.autocheck.app

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.IOException
import java.util.Locale

private enum class RepairPdfMode {
    WITH_IMAGES,
    WITHOUT_IMAGES,
    PARTS_ONLY
}

private fun repairDisplayDate(value: String): String {

    val trimmed = value.trim()

    if (trimmed.isBlank()) {
        return ""
    }

    if (
        trimmed.matches(
            Regex("""\d{4}-\d{2}-\d{2}""")
        )
    ) {

        val parts = trimmed.split("-")

        if (parts.size == 3) {
            return "${parts[2]}.${parts[1]}.${parts[0]}"
        }
    }

    if (
        trimmed.matches(
            Regex("""\d{1,2}/\d{1,2}/\d{4}""")
        )
    ) {

        val parts = trimmed.split("/")

        if (parts.size == 3) {

            val month =
                parts[0].padStart(2, '0')

            val day =
                parts[1].padStart(2, '0')

            return "$day.$month.${parts[2]}"
        }
    }

    return trimmed
}

private fun repairMoneyValue(value: String): Double {

    val cleaned =
        value
            .replace("€", "")
            .replace(" ", "")
            .trim()

    if (cleaned.isBlank()) {
        return 0.0
    }

    return try {

        if (
            cleaned.contains(",") &&
            cleaned.contains(".")
        ) {

            cleaned
                .replace(".", "")
                .replace(",", ".")
                .toDouble()

        } else if (
            cleaned.contains(",")
        ) {

            cleaned
                .replace(",", ".")
                .toDouble()

        } else {

            cleaned.toDouble()
        }

    } catch (
        _: NumberFormatException
    ) {

        0.0
    }
}

private fun repairMoneyText(value: Double): String {

    return String.format(
        Locale.GERMANY,
        "%.2f €",
        value
    )
}

private fun repairPartsTotal(
    repair: Repair
): Double {

    return repair.parts.sumOf {
        repairMoneyValue(
            it.cost
        )
    }
}

private fun repairLaborTotal(
    repair: Repair
): Double {

    val newLabor =
        repairMoneyValue(
            repair.laborCost
        )

    if (newLabor > 0.0) {
        return newLabor
    }

    /*
     * Alte Reparaturen hatten nur ein gemeinsames
     * Kostenfeld. Dieses bleibt deshalb als Fallback
     * erhalten.
     */
    if (
        repair.parts.isEmpty() &&
        repair.cost.isNotBlank()
    ) {
        return repairMoneyValue(
            repair.cost
        )
    }

    return 0.0
}

private fun repairTotal(
    repair: Repair
): Double {

    val parts =
        repairPartsTotal(
            repair
        )

    val labor =
        repairLaborTotal(
            repair
        )

    if (
        repair.parts.isEmpty() &&
        repair.laborCost.isBlank() &&
        repair.cost.isNotBlank()
    ) {
        return repairMoneyValue(
            repair.cost
        )
    }

    return parts + labor
}

private fun loadRepairBitmap(
    context: Context,
    uriString: String
): Bitmap? {

    if (uriString.isBlank()) {
        return null
    }

    return try {

        context.contentResolver.openInputStream(
            Uri.parse(uriString)
        )?.use {

            BitmapFactory.decodeStream(
                it
            )
        }

    } catch (
        _: Exception
    ) {

        null
    }
}

private fun createCameraUri(
    context: Context
): Uri? {

    val values =
        android.content.ContentValues().apply {

            put(
                MediaStore.Images.Media.DISPLAY_NAME,
                "AutoCheck_${System.currentTimeMillis()}.jpg"
            )

            put(
                MediaStore.Images.Media.MIME_TYPE,
                "image/jpeg"
            )
        }

    return context.contentResolver.insert(
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        values
    )
}

private fun drawPdfText(
    canvas: Canvas,
    text: String,
    x: Float,
    y: Float,
    paint: Paint
) {

    if (text.isBlank()) {
        return
    }

    canvas.drawText(
        text,
        x,
        y,
        paint
    )
}

private fun drawPdfWrappedText(
    canvas: Canvas,
    text: String,
    x: Float,
    startY: Float,
    maxWidth: Float,
    paint: Paint
): Float {

    if (text.isBlank()) {
        return startY
    }

    val words =
        text.split(
            Regex("\\s+")
        )

    var currentLine = ""
    var y = startY

    for (word in words) {

        val candidate =
            if (currentLine.isBlank()) {
                word
            } else {
                "$currentLine $word"
            }

        if (
            paint.measureText(
                candidate
            ) <= maxWidth
        ) {

            currentLine =
                candidate

        } else {

            if (currentLine.isNotBlank()) {

                canvas.drawText(
                    currentLine,
                    x,
                    y,
                    paint
                )

                y +=
                    paint.textSize +
                        6f
            }

            currentLine =
                word
        }
    }

    if (currentLine.isNotBlank()) {

        canvas.drawText(
            currentLine,
            x,
            y,
            paint
        )

        y +=
            paint.textSize +
                6f
    }

    return y
}

private fun drawPdfBitmap(
    canvas: Canvas,
    bitmap: Bitmap,
    left: Float,
    top: Float,
    maxWidth: Float,
    maxHeight: Float
): Float {

    val scale =
        minOf(
            maxWidth / bitmap.width.toFloat(),
            maxHeight / bitmap.height.toFloat()
        )

    val width =
        bitmap.width * scale

    val height =
        bitmap.height * scale

    val destination =
        android.graphics.RectF(
            left,
            top,
            left + width,
            top + height
        )

    canvas.drawBitmap(
        bitmap,
        null,
        destination,
        null
    )

    return top + height
}

private fun createRepairPdf(
    context: Context,
    outputUri: Uri,
    vehicle: Vehicle?,
    repairs: List<Repair>,
    mode: RepairPdfMode
) {

    val document =
        PdfDocument()

    val pageWidth = 595
    val pageHeight = 842

    val titlePaint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {

            color =
                android.graphics.Color.BLACK

            textSize =
                22f

            typeface =
                Typeface.DEFAULT_BOLD
        }

    val headingPaint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {

            color =
                android.graphics.Color.BLACK

            textSize =
                16f

            typeface =
                Typeface.DEFAULT_BOLD
        }

    val normalPaint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {

            color =
                android.graphics.Color.BLACK

            textSize =
                12f
        }

    val smallPaint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {

            color =
                android.graphics.Color.DKGRAY

            textSize =
                10f
        }

    var pageNumber = 1

    fun startPage(): PdfDocument.Page {

        return document.startPage(
            PdfDocument.PageInfo.Builder(
                pageWidth,
                pageHeight,
                pageNumber++
            ).create()
        )
    }

    if (
        mode ==
        RepairPdfMode.PARTS_ONLY
    ) {

        var page =
            startPage()

        var canvas =
            page.canvas

        var y = 45f

        drawPdfText(
            canvas,
            "AutoCheck – Ersatzteilkosten",
            40f,
            y,
            titlePaint
        )

        y += 35f

        if (vehicle != null) {

            drawPdfText(
                canvas,
                "Fahrzeug: ${vehicle.name}",
                40f,
                y,
                headingPaint
            )

            y += 22f

            val details =
                listOf(
                    vehicle.make,
                    vehicle.model,
                    vehicle.year,
                    vehicle.plate
                )
                    .filter {
                        it.isNotBlank()
                    }
                    .joinToString(
                        " · "
                    )

            if (details.isNotBlank()) {

                drawPdfText(
                    canvas,
                    details,
                    40f,
                    y,
                    normalPaint
                )

                y += 25f
            }
        }

        var totalParts = 0.0

        for (repair in repairs) {

            for (part in repair.parts) {

                if (
                    y >
                    pageHeight - 70
                ) {

                    document.finishPage(
                        page
                    )

                    page =
                        startPage()

                    canvas =
                        page.canvas

                    y = 45f
                }

                val partCost =
                    repairMoneyValue(
                        part.cost
                    )

                totalParts +=
                    partCost

                val date =
                    repairDisplayDate(
                        repair.date
                    )

                drawPdfText(
                    canvas,
                    date,
                    40f,
                    y,
                    smallPaint
                )

                drawPdfText(
                    canvas,
                    part.name.ifBlank {
                        "Ersatzteil"
                    },
                    115f,
                    y,
                    normalPaint
                )

                drawPdfText(
                    canvas,
                    repairMoneyText(
                        partCost
                    ),
                    460f,
                    y,
                    normalPaint
                )

                y += 20f
            }
        }

        if (
            repairs.none {
                it.parts.isNotEmpty()
            }
        ) {

            drawPdfText(
                canvas,
                "Keine einzelnen Ersatzteile gespeichert.",
                40f,
                y,
                normalPaint
            )

            y += 25f
        }

        if (
            y >
            pageHeight - 70
        ) {

            document.finishPage(
                page
            )

            page =
                startPage()

            canvas =
                page.canvas

            y = 45f
        }

        y += 20f

        drawPdfText(
            canvas,
            "Gesamtkosten Ersatzteile: ${
                repairMoneyText(
                    totalParts
                )
            }",
            40f,
            y,
            headingPaint
        )

        document.finishPage(
            page
        )

    } else {

        var page =
            startPage()

        var canvas =
            page.canvas

        var y = 45f

        fun newPageIfNeeded(
            required: Float
        ) {

            if (
                y + required >
                pageHeight - 45
            ) {

                document.finishPage(
                    page
                )

                page =
                    startPage()

                canvas =
                    page.canvas

                y = 45f
            }
        }

        drawPdfText(
            canvas,
            "AutoCheck – Reparaturhistorie",
            40f,
            y,
            titlePaint
        )

        y += 35f

        if (vehicle != null) {

            drawPdfText(
                canvas,
                "Fahrzeug: ${vehicle.name}",
                40f,
                y,
                headingPaint
            )

            y += 22f

            val details =
                listOf(
                    vehicle.make,
                    vehicle.model,
                    vehicle.year,
                    vehicle.plate
                )
                    .filter {
                        it.isNotBlank()
                    }
                    .joinToString(
                        " · "
                    )

            if (details.isNotBlank()) {

                drawPdfText(
                    canvas,
                    details,
                    40f,
                    y,
                    normalPaint
                )

                y += 20f
            }

            if (
                vehicle.vin.isNotBlank()
            ) {

                drawPdfText(
                    canvas,
                    "VIN/FIN: ${vehicle.vin}",
                    40f,
                    y,
                    smallPaint
                )

                y += 20f
            }
        }

        y += 10f

        var allPartsTotal = 0.0
        var allLaborTotal = 0.0

        for (repair in repairs) {

            newPageIfNeeded(
                100f
            )

            drawPdfText(
                canvas,
                repair.description.ifBlank {
                    "Reparatur"
                },
                40f,
                y,
                headingPaint
            )

            y += 20f

            drawPdfText(
                canvas,
                "Datum: ${
                    repairDisplayDate(
                        repair.date
                    )
                }",
                40f,
                y,
                normalPaint
            )

            y += 18f

            if (
                repair.mileage.isNotBlank()
            ) {

                drawPdfText(
                    canvas,
                    "Kilometerstand: ${repair.mileage}",
                    40f,
                    y,
                    normalPaint
                )

                y += 18f
            }

            if (
                repair.repairDescription.isNotBlank()
            ) {

                y =
                    drawPdfWrappedText(
                        canvas,
                        repair.repairDescription,
                        40f,
                        y,
                        510f,
                        normalPaint
                    )
            }

            y += 5f

            if (
                repair.parts.isNotEmpty()
            ) {

                drawPdfText(
                    canvas,
                    "Ersatzteile:",
                    40f,
                    y,
                    normalPaint
                )

                y += 17f

                for (part in repair.parts) {

                    newPageIfNeeded(
                        25f
                    )

                    val amount =
                        repairMoneyValue(
                            part.cost
                        )

                    allPartsTotal +=
                        amount

                    drawPdfText(
                        canvas,
                        "• ${part.name.ifBlank { "Ersatzteil" }}",
                        55f,
                        y,
                        normalPaint
                    )

                    drawPdfText(
                        canvas,
                        repairMoneyText(
                            amount
                        ),
                        440f,
                        y,
                        normalPaint
                    )

                    y += 17f
                }
            }

            val labor =
                repairLaborTotal(
                    repair
                )

            allLaborTotal +=
                labor

            newPageIfNeeded(
                30f
            )

            drawPdfText(
                canvas,
                "Arbeits-/Einbaukosten:",
                40f,
                y,
                normalPaint
            )

            drawPdfText(
                canvas,
                repairMoneyText(
                    labor
                ),
                440f,
                y,
                normalPaint
            )

            y += 20f

            val total =
                repairTotal(
                    repair
                )

            newPageIfNeeded(
                30f
            )

            drawPdfText(
                canvas,
                "Gesamtkosten:",
                40f,
                y,
                headingPaint
            )

            drawPdfText(
                canvas,
                repairMoneyText(
                    total
                ),
                440f,
                y,
                headingPaint
            )

            y += 25f

            if (
                mode ==
                RepairPdfMode.WITH_IMAGES
            ) {

                val before =
                    loadRepairBitmap(
                        context,
                        repair.beforeImageUri
                    )

                val after =
                    loadRepairBitmap(
                        context,
                        repair.afterImageUri
                    )

                if (
                    before != null ||
                    after != null
                ) {

                    newPageIfNeeded(
                        280f
                    )

                    if (before != null) {

                        drawPdfText(
                            canvas,
                            "Vorher",
                            40f,
                            y,
                            headingPaint
                        )

                        y += 10f

                        y =
                            drawPdfBitmap(
                                canvas,
                                before,
                                40f,
                                y,
                                240f,
                                210f
                            )

                        y += 15f
                    }

                    if (after != null) {

                        newPageIfNeeded(
                            260f
                        )

                        drawPdfText(
                            canvas,
                            "Nachher",
                            40f,
                            y,
                            headingPaint
                        )

                        y += 10f

                        y =
                            drawPdfBitmap(
                                canvas,
                                after,
                                40f,
                                y,
                                240f,
                                210f
                            )

                        y += 15f
                    }
                }
            }

            y += 15f
        }

        newPageIfNeeded(
            100f
        )

        drawPdfText(
            canvas,
            "Gesamtkostenübersicht",
            40f,
            y,
            headingPaint
        )

        y += 25f

        drawPdfText(
            canvas,
            "Ersatzteile: ${
                repairMoneyText(
                    allPartsTotal
                )
            }",
            40f,
            y,
            normalPaint
        )

        y += 20f

        drawPdfText(
            canvas,
            "Reparatur / Einbau: ${
                repairMoneyText(
                    allLaborTotal
                )
            }",
            40f,
            y,
            normalPaint
        )

        y += 25f

        drawPdfText(
            canvas,
            "Gesamtkosten: ${
                repairMoneyText(
                    allPartsTotal +
                        allLaborTotal
                )
            }",
            40f,
            y,
            headingPaint
        )

        document.finishPage(
            page
        )
    }

    try {

        context.contentResolver.openOutputStream(
            outputUri
        )?.use { output ->

            document.writeTo(
                output
            )
        }

    } catch (
        _: IOException
    ) {
        // Ausgabe konnte nicht geschrieben werden.
    } finally {

        document.close()
    }
}

@Composable
fun RepairScreen(
    store: VehicleStore,
    vehicles: List<Vehicle>,
    activeVehicle: String,
    onActiveVehicle: (String) -> Unit,
    onVisited: () -> Unit
) {

    val context =
        androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(Unit) {
        onVisited()
    }

    var repairs by remember {
        mutableStateOf(
            store.loadRepairs()
        )
    }

    var showForm by remember {
        mutableStateOf(false)
    }

    var editingId by remember {
        mutableStateOf<Long?>(null)
    }

    var detailRepair by remember {
        mutableStateOf<Repair?>(null)
    }

    var showPdfDialog by remember {
        mutableStateOf(false)
    }

    var pendingPdfMode by remember {
        mutableStateOf<RepairPdfMode?>(null)
    }

    var date by remember {
        mutableStateOf("")
    }

    var mileage by remember {
        mutableStateOf("")
    }

    var description by remember {
        mutableStateOf("")
    }

    var repairDescription by remember {
        mutableStateOf("")
    }

    var laborCost by remember {
        mutableStateOf("")
    }

    var workshop by remember {
        mutableStateOf("")
    }

    var parts by remember {
        mutableStateOf(
            emptyList<RepairPart>()
        )
    }

    var newPartName by remember {
        mutableStateOf("")
    }

    var newPartCost by remember {
        mutableStateOf("")
    }

    var beforeImageUri by remember {
        mutableStateOf("")
    }

    var afterImageUri by remember {
        mutableStateOf("")
    }

    var cameraTarget by remember {
        mutableStateOf<String?>(null)
    }

    var cameraUri by remember {
        mutableStateOf<Uri?>(null)
    }

    val galleryLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.OpenDocument()
        ) { uri ->

            if (uri != null) {

                try {

                    context.contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )

                } catch (
                    _: Exception
                ) {
                    // Nicht jeder Anbieter unterstützt
                    // persistente Berechtigungen.
                }

                if (
                    cameraTarget ==
                    "before"
                ) {

                    beforeImageUri =
                        uri.toString()

                } else if (
                    cameraTarget ==
                    "after"
                ) {

                    afterImageUri =
                        uri.toString()
                }
            }

            cameraTarget = null
        }

    val cameraLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.TakePicture()
        ) { success ->

            val uri =
                cameraUri

            if (
                success &&
                uri != null
            ) {

                if (
                    cameraTarget ==
                    "before"
                ) {

                    beforeImageUri =
                        uri.toString()

                } else if (
                    cameraTarget ==
                    "after"
                ) {

                    afterImageUri =
                        uri.toString()
                }

            } else if (
                uri != null
            ) {

                try {
                    context.contentResolver.delete(
                        uri,
                        null,
                        null
                    )
                } catch (
                    _: Exception
                ) {
                }
            }

            cameraTarget = null
            cameraUri = null
        }

    val pdfLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.CreateDocument(
                "application/pdf"
            )
        ) { uri ->

            val mode =
                pendingPdfMode

            if (
                uri != null &&
                mode != null
            ) {

                val active =
                    vehicles.firstOrNull {
                        it.name ==
                            activeVehicle
                    }

                createRepairPdf(
                    context =
                        context,
                    outputUri =
                        uri,
                    vehicle =
                        active,
                    repairs =
                        repairs.filter {
                            it.vehicle ==
                                activeVehicle
                        },
                    mode =
                        mode
                )

                /*
                 * Nach dem Speichern öffnen wir die PDF,
                 * damit sie direkt angesehen und über die
                 * Android-Druckfunktion ausgedruckt werden kann.
                 */
                try {

                    val intent =
                        Intent(
                            Intent.ACTION_VIEW
                        ).apply {

                            setDataAndType(
                                uri,
                                "application/pdf"
                            )

                            addFlags(
                                Intent.FLAG_GRANT_READ_URI_PERMISSION
                            )
                        }

                    context.startActivity(
                        intent
                    )

                } catch (
                    _: Exception
                ) {
                    // Kein PDF-Viewer installiert.
                }
            }

            pendingPdfMode = null
        }

    fun resetForm() {

        editingId = null
        date = ""
        mileage = ""
        description = ""
        repairDescription = ""
        laborCost = ""
        workshop = ""
        parts = emptyList()
        newPartName = ""
        newPartCost = ""
        beforeImageUri = ""
        afterImageUri = ""
        showForm = false
    }

    fun startNewRepair() {

        editingId = null
        date = ""
        mileage = ""
        description = ""
        repairDescription = ""
        laborCost = ""
        workshop = ""
        parts = emptyList()
        newPartName = ""
        newPartCost = ""
        beforeImageUri = ""
        afterImageUri = ""
        showForm = true
    }

    fun startEdit(
        repair: Repair
    ) {

        editingId =
            repair.id

        date =
            repairDisplayDate(
                repair.date
            )

        mileage =
            repair.mileage

        description =
            repair.description

        repairDescription =
            repair.repairDescription

        laborCost =
            repair.laborCost.ifBlank {
                if (
                    repair.parts.isEmpty()
                ) {
                    repair.cost
                } else {
                    ""
                }
            }

        workshop =
            repair.workshop

        parts =
            repair.parts

        beforeImageUri =
            repair.beforeImageUri

        afterImageUri =
            repair.afterImageUri

        newPartName = ""
        newPartCost = ""

        showForm = true
    }

    val list =
        repairs.filter {
            it.vehicle ==
                activeVehicle
        }

    /*
     * Gesamte bisherige Reparaturinvestition
     * ausschließlich für das aktuell aktive Fahrzeug.
     */
    val totalRepairInvestment =
        list.sumOf {
            repairTotal(
                it
            )
        }

    if (
        detailRepair != null
    ) {

        val repair =
            detailRepair!!

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(16.dp)
        ) {

            Row(
                modifier =
                    Modifier.fillMaxWidth()
            ) {

                OutlinedButton(
                    onClick = {
                        detailRepair = null
                    }
                ) {

                    Text(
                        "← Zurück"
                    )
                }
            }

            Spacer(
                modifier =
                    Modifier.height(12.dp)
            )

            Text(
                text =
                    repair.description.ifBlank {
                        "Reparatur"
                    },
                color =
                    Color.White,
                fontSize =
                    22.sp,
                fontWeight =
                    FontWeight.Bold
            )

            Spacer(
                modifier =
                    Modifier.height(16.dp)
            )

            LazyColumn(
                modifier =
                    Modifier.weight(1f),
                verticalArrangement =
                    Arrangement.spacedBy(
                        12.dp
                    )
            ) {

                item {

                    RepairDetailCard(
                        repair =
                            repair,
                        context =
                            context
                    )
                }
            }
        }

        return
    }

    Column(
        modifier =
            Modifier.fillMaxSize()
    ) {

        VehicleSelector(
            vehicles,
            activeVehicle,
            onActiveVehicle
        )

        Spacer(
            modifier =
                Modifier.height(10.dp)
        )

        /*
         * Gesamtinvestition Reparaturen
         * für das aktuell aktive Fahrzeug.
         * Die Farbe orientiert sich am blauen
         * Reparaturbereich.
         */
        Card(
            colors =
                CardDefaults.cardColors(
                    containerColor =
                        Color(
                            0xFF11141A
                        )
                ),
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        bottom = 10.dp
                    )
        ) {

            Column(
                modifier =
                    Modifier.padding(
                        16.dp
                    )
            ) {

                Text(
                    "REPARATURHISTORIE",
                    color =
                        Color.White,
                    fontSize =
                        18.sp,
                    fontWeight =
                        FontWeight.Bold
                )

                Spacer(
                    modifier =
                        Modifier.height(
                            6.dp
                        )
                )

                Text(
                    "Gesamte bisherige Reparaturkosten",
                    color =
                        Color(
                            0xFFB8BEC8
                        )
                )

                Spacer(
                    modifier =
                        Modifier.height(
                            4.dp
                        )
                )

                Text(
                    repairMoneyText(
                        totalRepairInvestment
                    ),
                    color =
                        Color(
                            0xFF4A90E2
                        ),
                    fontSize =
                        22.sp,
                    fontWeight =
                        FontWeight.Bold
                )
            }
        }

        if (
            vehicles.isEmpty()
        ) {

            EmptyCard(
                "Bitte zuerst unter „Mein Auto“ ein Fahrzeug anlegen."
            )

            return@Column
        }

        if (
            showForm
        ) {

            LazyColumn(
                modifier =
                    Modifier.weight(1f),
                verticalArrangement =
                    Arrangement.spacedBy(
                        10.dp
                    ),
                contentPadding =
                    androidx.compose.foundation.layout.PaddingValues(
                        bottom = 20.dp
                    )
            ) {

                item {

                    CardForm {

                        Text(
                            text =
                                if (
                                    editingId !=
                                    null
                                ) {
                                    "Reparatur bearbeiten"
                                } else {
                                    "Neue Reparatur"
                                },
                            color =
                                Color.White,
                            fontSize =
                                20.sp,
                            fontWeight =
                                FontWeight.Bold
                        )

                        FormField(
                            "Datum (TT.MM.JJJJ)",
                            date
                        ) {
                            date = it
                        }

                        FormField(
                            "Kilometerstand",
                            mileage
                        ) {
                            mileage = it
                        }

                        FormField(
                            "Fehler / Reparatur",
                            description
                        ) {
                            description = it
                        }

                        FormField(
                            "Durchgeführte Reparatur",
                            repairDescription
                        ) {
                            repairDescription = it
                        }

                        Spacer(
                            modifier =
                                Modifier.height(6.dp)
                        )

                        Text(
                            "Ersatzteile",
                            color =
                                Color.White,
                            fontWeight =
                                FontWeight.Bold
                        )

                        parts.forEachIndexed {
                            index,
                            part ->

                            Row(
                                modifier =
                                    Modifier.fillMaxWidth(),
                                horizontalArrangement =
                                    Arrangement.spacedBy(
                                        8.dp
                                    )
                            ) {

                                Column(
                                    modifier =
                                        Modifier.weight(
                                            1f
                                        )
                                ) {

                                    Text(
                                        part.name.ifBlank {
                                            "Ersatzteil"
                                        },
                                        color =
                                            Color.White
                                    )

                                    Text(
                                        repairMoneyText(
                                            repairMoneyValue(
                                                part.cost
                                            )
                                        ),
                                        color =
                                            Color(
                                                0xFFB8BEC8
                                            )
                                    )
                                }

                                OutlinedButton(
                                    onClick = {

                                        parts =
                                            parts.filterIndexed {
                                                partIndex,
                                                _ ->
                                                partIndex !=
                                                    index
                                            }
                                    }
                                ) {

                                    Text(
                                        "Entfernen"
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value =
                                newPartName,
                            onValueChange = {
                                newPartName = it
                            },
                            label = {
                                Text(
                                    "Ersatzteil"
                                )
                            },
                            modifier =
                                Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value =
                                newPartCost,
                            onValueChange = {
                                newPartCost = it
                            },
                            label = {
                                Text(
                                    "Ersatzteilkosten"
                                )
                            },
                            keyboardOptions =
                                KeyboardOptions(
                                    keyboardType =
                                        KeyboardType.Decimal
                                ),
                            modifier =
                                Modifier.fillMaxWidth()
                        )

                        OutlinedButton(
                            modifier =
                                Modifier.fillMaxWidth(),
                            onClick = {

                                if (
                                    newPartName
                                        .trim()
                                        .isNotBlank()
                                ) {

                                    parts =
                                        parts +
                                            RepairPart(
                                                name =
                                                    newPartName
                                                        .trim(),
                                                cost =
                                                    newPartCost
                                                        .trim()
                                            )

                                    newPartName = ""
                                    newPartCost = ""
                                }
                            }
                        ) {

                            Text(
                                "Ersatzteil hinzufügen"
                            )
                        }

                        FormField(
                            "Einbaukosten / Arbeitskosten",
                            laborCost
                        ) {
                            laborCost = it
                        }

                        FormField(
                            "Werkstatt",
                            workshop
                        ) {
                            workshop = it
                        }

                        Spacer(
                            modifier =
                                Modifier.height(4.dp)
                        )

                        Text(
                            "Gesamtkosten: ${
                                repairMoneyText(
                                    parts.sumOf {
                                        repairMoneyValue(
                                            it.cost
                                        )
                                    } +
                                        repairMoneyValue(
                                            laborCost
                                        )
                                )
                            }",
                            color =
                                Color.White,
                            fontSize =
                                18.sp,
                            fontWeight =
                                FontWeight.Bold
                        )

                        Spacer(
                            modifier =
                                Modifier.height(8.dp)
                        )

                        Text(
                            "Vorher-Bild",
                            color =
                                Color.White,
                            fontWeight =
                                FontWeight.Bold
                        )

                        RepairImageChooser(
                            imageUri =
                                beforeImageUri,
                            label =
                                "Vorher",
                            context =
                                context,
                            onGallery = {

                                cameraTarget =
                                    "before"

                                galleryLauncher.launch(
                                    arrayOf(
                                        "image/*"
                                    )
                                )
                            },
                            onCamera = {

                                cameraTarget =
                                    "before"

                                val uri =
                                    createCameraUri(
                                        context
                                    )

                                cameraUri =
                                    uri

                                if (
                                    uri != null
                                ) {

                                    cameraLauncher.launch(
                                        uri
                                    )
                                }
                            },
                            onRemove = {
                                beforeImageUri = ""
                            }
                        )

                        Spacer(
                            modifier =
                                Modifier.height(8.dp)
                        )

                        Text(
                            "Nachher-Bild",
                            color =
                                Color.White,
                            fontWeight =
                                FontWeight.Bold
                        )

                        RepairImageChooser(
                            imageUri =
                                afterImageUri,
                            label =
                                "Nachher",
                            context =
                                context,
                            onGallery = {

                                cameraTarget =
                                    "after"

                                galleryLauncher.launch(
                                    arrayOf(
                                        "image/*"
                                    )
                                )
                            },
                            onCamera = {

                                cameraTarget =
                                    "after"

                                val uri =
                                    createCameraUri(
                                        context
                                    )

                                cameraUri =
                                    uri

                                if (
                                    uri != null
                                ) {

                                    cameraLauncher.launch(
                                        uri
                                    )
                                }
                            },
                            onRemove = {
                                afterImageUri = ""
                            }
                        )

                        Spacer(
                            modifier =
                                Modifier.height(10.dp)
                        )

                        Row(
                            modifier =
                                Modifier.fillMaxWidth(),
                            horizontalArrangement =
                                Arrangement.spacedBy(
                                    10.dp
                                )
                        ) {

                            Button(
                                modifier =
                                    Modifier.weight(
                                        1f
                                    ),
                                onClick = {

                                    val newRepair =

                                        if (
                                            editingId !=
                                            null
                                        ) {

                                            repairs.map {
                                                existing ->

                                                if (
                                                    existing.id ==
                                                    editingId
                                                ) {

                                                    existing.copy(
                                                        vehicle =
                                                            activeVehicle,
                                                        date =
                                                            date.trim(),
                                                        mileage =
                                                            mileage.trim(),
                                                        description =
                                                            description.trim(),
                                                        repairDescription =
                                                            repairDescription.trim(),
                                                        parts =
                                                            parts,
                                                        laborCost =
                                                            laborCost.trim(),
                                                        workshop =
                                                            workshop.trim(),
                                                        beforeImageUri =
                                                            beforeImageUri,
                                                        afterImageUri =
                                                            afterImageUri
                                                    )

                                                } else {

                                                    existing
                                                }
                                            }

                                        } else {

                                            repairs +
                                                Repair(
                                                    id =
                                                        System.currentTimeMillis(),
                                                    vehicle =
                                                        activeVehicle,
                                                    date =
                                                        date.trim(),
                                                    mileage =
                                                        mileage.trim(),
                                                    description =
                                                        description.trim(),
                                                    repairDescription =
                                                        repairDescription.trim(),
                                                    parts =
                                                        parts,
                                                    laborCost =
                                                        laborCost.trim(),
                                                    cost =
                                                        laborCost.trim(),
                                                    workshop =
                                                        workshop.trim(),
                                                    beforeImageUri =
                                                        beforeImageUri,
                                                    afterImageUri =
                                                        afterImageUri
                                                )
                                        }

                                    repairs =
                                        newRepair

                                    store.saveRepairs(
                                        newRepair
                                    )

                                    resetForm()
                                }
                            ) {

                                Text(
                                    if (
                                        editingId !=
                                        null
                                    ) {
                                        "Änderung speichern"
                                    } else {
                                        "Speichern"
                                    }
                                )
                            }

                            OutlinedButton(
                                modifier =
                                    Modifier.weight(
                                        1f
                                    ),
                                onClick = {
                                    resetForm()
                                }
                            ) {

                                Text(
                                    "Abbrechen"
                                )
                            }
                        }
                    }
                }
            }

        } else {

            LazyColumn(
                modifier =
                    Modifier.weight(1f),
                verticalArrangement =
                    Arrangement.spacedBy(
                        10.dp
                    ),
                contentPadding =
                    androidx.compose.foundation.layout.PaddingValues(
                        bottom = 10.dp
                    )
            ) {

                if (
                    list.isEmpty()
                ) {

                    item {

                        EmptyCard(
                            "Noch keine Reparatur eingetragen."
                        )
                    }
                }

                items(
                    items =
                        list,
                    key = {
                        it.id
                    }
                ) { repair ->

                    RepairRecordCard(
                        repair =
                            repair,
                        onOpen = {
                            detailRepair =
                                repair
                        },
                        onEdit = {
                            startEdit(
                                repair
                            )
                        },
                        onDelete = {

                            repairs =
                                repairs.filterNot {
                                    it.id ==
                                        repair.id
                                }

                            store.saveRepairs(
                                repairs
                            )
                        }
                    )
                }
            }

            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(
                            top = 6.dp
                        ),
                horizontalArrangement =
                    Arrangement.spacedBy(
                        8.dp
                    )
            ) {

                Button(
                    modifier =
                        Modifier.weight(
                            1f
                        ),
                    onClick = {
                        startNewRepair()
                    }
                ) {

                    Text(
                        "Reparatur hinzufügen"
                    )
                }

                OutlinedButton(
                    modifier =
                        Modifier.weight(
                            1f
                        ),
                    onClick = {
                        showPdfDialog = true
                    }
                ) {

                    Text(
                        "PDF"
                    )
                }
            }
        }
    }

    if (
        showPdfDialog
    ) {

        AlertDialog(

            onDismissRequest = {
                showPdfDialog = false
            },

            title = {
                Text(
                    "Reparaturhistorie als PDF"
                )
            },

            text = {

                Column(
                    verticalArrangement =
                        Arrangement.spacedBy(
                            10.dp
                        )
                ) {

                    Text(
                        "Welche Übersicht möchtest du erstellen?"
                    )

                    Button(
                        modifier =
                            Modifier.fillMaxWidth(),
                        onClick = {

                            showPdfDialog =
                                false

                            pendingPdfMode =
                                RepairPdfMode.WITH_IMAGES

                            pdfLauncher.launch(
                                "AutoCheck_Reparaturhistorie_mit_Bildern.pdf"
                            )
                        }
                    ) {

                        Text(
                            "Gesamtkosten + Bilder"
                        )
                    }

                    Button(
                        modifier =
                            Modifier.fillMaxWidth(),
                        onClick = {

                            showPdfDialog =
                                false

                            pendingPdfMode =
                                RepairPdfMode.WITHOUT_IMAGES

                            pdfLauncher.launch(
                                "AutoCheck_Reparaturhistorie_ohne_Bilder.pdf"
                            )
                        }
                    ) {

                        Text(
                            "Gesamtkosten ohne Bilder"
                        )
                    }

                    Button(
                        modifier =
                            Modifier.fillMaxWidth(),
                        onClick = {

                            showPdfDialog =
                                false

                            pendingPdfMode =
                                RepairPdfMode.PARTS_ONLY

                            pdfLauncher.launch(
                                "AutoCheck_Ersatzteilkosten.pdf"
                            )
                        }
                    ) {

                        Text(
                            "Nur Ersatzteilkosten"
                        )
                    }
                }
            },

            confirmButton = {
                OutlinedButton(
                    onClick = {
                        showPdfDialog = false
                    }
                ) {

                    Text(
                        "Abbrechen"
                    )
                }
            }
        )
    }
}

@Composable
private fun RepairRecordCard(
    repair: Repair,
    onOpen: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {

    Card(
        colors =
            CardDefaults.cardColors(
                containerColor =
                    Color(
                        0xFF11141A
                    )
            ),
        modifier =
            Modifier.fillMaxWidth()
    ) {

        Column(
            modifier =
                Modifier.padding(
                    16.dp
                )
        ) {

            Text(
                text =
                    repair.description.ifBlank {
                        "Reparatur"
                    },
                color =
                    Color.White,
                fontSize =
                    19.sp,
                fontWeight =
                    FontWeight.Bold,
                modifier =
                    Modifier.clickable {
                        onOpen()
                    }
            )

            Spacer(
                modifier =
                    Modifier.height(8.dp)
            )

            val partsTotal =
                repairPartsTotal(
                    repair
                )

            val laborTotal =
                repairLaborTotal(
                    repair
                )

            val total =
                repairTotal(
                    repair
                )

            if (
                repair.parts.isNotEmpty()
            ) {

                repair.parts.forEach { part ->

                    Row(
                        modifier =
                            Modifier.fillMaxWidth()
                    ) {

                        Text(
                            text =
                                part.name.ifBlank {
                                    "Ersatzteil"
                                },
                            color =
                                Color(
                                    0xFFB8BEC8
                                ),
                            modifier =
                                Modifier.weight(
                                    1f
                                )
                        )

                        Text(
                            text =
                                repairMoneyText(
                                    repairMoneyValue(
                                        part.cost
                                    )
                                ),
                            color =
                                Color.White
                        )
                    }
                }
            } else if (
                repair.cost.isNotBlank()
            ) {

                Row(
                    modifier =
                        Modifier.fillMaxWidth()
                ) {

                    Text(
                        "Kosten",
                        color =
                            Color(
                                0xFFB8BEC8
                            ),
                        modifier =
                            Modifier.weight(
                                1f
                            )
                    )

                    Text(
                        repair.cost,
                        color =
                            Color.White
                    )
                }
            }

            if (
                laborTotal > 0.0 &&
                (
                    repair.parts.isNotEmpty() ||
                        repair.laborCost.isNotBlank()
                )
            ) {

                Row(
                    modifier =
                        Modifier.fillMaxWidth()
                ) {

                    Text(
                        "Einbaukosten",
                        color =
                            Color(
                                0xFFB8BEC8
                            ),
                        modifier =
                            Modifier.weight(
                                1f
                            )
                    )

                    Text(
                        repairMoneyText(
                            laborTotal
                        ),
                        color =
                            Color.White
                    )
                }
            }

            if (
                repair.parts.isNotEmpty() ||
                repair.laborCost.isNotBlank()
            ) {

                Spacer(
                    modifier =
                        Modifier.height(4.dp)
                )

                Row(
                    modifier =
                        Modifier.fillMaxWidth()
                ) {

                    Text(
                        "Gesamt",
                        color =
                            Color(
                                0xFF4A90E2
                            ),
                        fontWeight =
                            FontWeight.Bold,
                        modifier =
                            Modifier.weight(
                                1f
                            )
                    )

                    Text(
                        repairMoneyText(
                            total
                        ),
                        color =
                            Color(
                                0xFF4A90E2
                            ),
                        fontWeight =
                            FontWeight.Bold
                    )
                }
            }

            Spacer(
                modifier =
                    Modifier.height(8.dp)
            )

            if (
                repair.date.isNotBlank()
            ) {

                Text(
                    "Datum: ${
                        repairDisplayDate(
                            repair.date
                        )
                    }",
                    color =
                        Color(
                            0xFF4A90E2
                        )
                )
            }

            if (
                repair.mileage.isNotBlank()
            ) {

                Text(
                    "Kilometerstand: ${repair.mileage}",
                    color =
                        Color(
                            0xFFB8BEC8
                        )
                )
            }

            if (
                repair.beforeImageUri.isNotBlank() ||
                repair.afterImageUri.isNotBlank()
            ) {

                Text(
                    "Bilder vorhanden – antippen für Details",
                    color =
                        Color(
                            0xFFB8BEC8
                        ),
                    modifier =
                        Modifier.clickable {
                            onOpen()
                        }
                )
            }

            Spacer(
                modifier =
                    Modifier.height(10.dp)
            )

            Row(
                modifier =
                    Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.spacedBy(
                        10.dp
                    )
            ) {

                OutlinedButton(
                    modifier =
                        Modifier.weight(
                            1f
                        ),
                    onClick =
                        onOpen
                ) {

                    Text(
                        "Details"
                    )
                }

                OutlinedButton(
                    modifier =
                        Modifier.weight(
                            1f
                        ),
                    onClick =
                        onEdit
                ) {

                    Text(
                        "Bearbeiten"
                    )
                }

                OutlinedButton(
                    modifier =
                        Modifier.weight(
                            1f
                        ),
                    onClick =
                        onDelete
                ) {

                    Text(
                        "Löschen"
                    )
                }
            }
        }
    }
}

@Composable
private fun RepairDetailCard(
    repair: Repair,
    context: Context
) {

    CardForm {

        if (
            repair.date.isNotBlank()
        ) {

            Text(
                "Datum: ${
                    repairDisplayDate(
                        repair.date
                    )
                }",
                color =
                    Color(
                        0xFF4A90E2
                    )
            )
        }

        if (
            repair.mileage.isNotBlank()
        ) {

            Text(
                "Kilometerstand: ${repair.mileage}",
                color =
                    Color(
                        0xFFB8BEC8
                    )
            )
        }

        if (
            repair.repairDescription.isNotBlank()
        ) {

            Text(
                "Reparatur",
                color =
                    Color.White,
                fontWeight =
                    FontWeight.Bold
            )

            Text(
                repair.repairDescription,
                color =
                    Color(
                        0xFFB8BEC8
                    )
            )
        }

        if (
            repair.parts.isNotEmpty()
        ) {

            Spacer(
                modifier =
                    Modifier.height(8.dp)
            )

            Text(
                "Ersatzteile",
                color =
                    Color.White,
                fontWeight =
                    FontWeight.Bold
            )

            repair.parts.forEach { part ->

                Row(
                    modifier =
                        Modifier.fillMaxWidth()
                ) {

                    Text(
                        part.name.ifBlank {
                            "Ersatzteil"
                        },
                        color =
                            Color(
                                0xFFB8BEC8
                            ),
                        modifier =
                            Modifier.weight(
                                1f
                            )
                    )

                    Text(
                        repairMoneyText(
                            repairMoneyValue(
                                part.cost
                            )
                        ),
                        color =
                            Color.White
                    )
                }
            }
        }

        Spacer(
            modifier =
                Modifier.height(10.dp)
        )

        Text(
            "Kosten",
            color =
                Color.White,
            fontWeight =
                FontWeight.Bold
        )

        Text(
            "Ersatzteile: ${
                repairMoneyText(
                    repairPartsTotal(
                        repair
                    )
                )
            }",
            color =
                Color(
                    0xFFB8BEC8
                )
        )

        Text(
            "Einbaukosten: ${
                repairMoneyText(
                    repairLaborTotal(
                        repair
                    )
                )
            }",
            color =
                Color(
                    0xFFB8BEC8
                )
        )

        Text(
            "Gesamt: ${
                repairMoneyText(
                    repairTotal(
                        repair
                    )
                )
            }",
            color =
                Color(
                    0xFF4A90E2
                ),
            fontWeight =
                FontWeight.Bold
        )

        if (
            repair.workshop.isNotBlank()
        ) {

            Text(
                "Werkstatt: ${repair.workshop}",
                color =
                    Color(
                        0xFFB8BEC8
                    )
            )
        }

        Spacer(
            modifier =
                Modifier.height(12.dp)
        )

        Text(
            "VORHER",
            color =
                Color.White,
            fontSize =
                16.sp,
            fontWeight =
                FontWeight.Bold
        )

        RepairStoredImage(
            context =
                context,
            uriString =
                repair.beforeImageUri,
            emptyText =
                "Kein Vorher-Bild vorhanden."
        )

        Spacer(
            modifier =
                Modifier.height(16.dp)
        )

        Text(
            "NACHHER",
            color =
                Color.White,
            fontSize =
                16.sp,
            fontWeight =
                FontWeight.Bold
        )

        RepairStoredImage(
            context =
                context,
            uriString =
                repair.afterImageUri,
            emptyText =
                "Kein Nachher-Bild vorhanden."
        )
    }
}

@Composable
private fun RepairStoredImage(
    context: Context,
    uriString: String,
    emptyText: String
) {

    val bitmap =
        remember(
            uriString
        ) {

            loadRepairBitmap(
                context,
                uriString
            )
        }

    if (
        bitmap != null
    ) {

        Image(
            bitmap =
                bitmap.asImageBitmap(),
            contentDescription =
                null,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(230.dp),
            contentScale =
                ContentScale.Fit
        )

    } else {

        Text(
            emptyText,
            color =
                Color(
                    0xFFB8BEC8
                )
        )
    }
}

@Composable
private fun RepairImageChooser(
    imageUri: String,
    label: String,
    context: Context,
    onGallery: () -> Unit,
    onCamera: () -> Unit,
    onRemove: () -> Unit
) {

    val bitmap =
        remember(
            imageUri
        ) {

            loadRepairBitmap(
                context,
                imageUri
            )
        }

    if (
        bitmap != null
    ) {

        Image(
            bitmap =
                bitmap.asImageBitmap(),
            contentDescription =
                label,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(180.dp),
            contentScale =
                ContentScale.Fit
        )
    }

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
            onClick =
                onGallery
        ) {

            Text(
                "Galerie"
            )
        }

        OutlinedButton(
            modifier =
                Modifier.weight(
                    1f
                ),
            onClick =
                onCamera
        ) {

            Text(
                "Kamera"
            )
        }

        if (
            imageUri.isNotBlank()
        ) {

            OutlinedButton(
                modifier =
                    Modifier.weight(
                        1f
                    ),
                onClick =
                    onRemove
            ) {

                Text(
                    "Entfernen"
                )
            }
        }
    }
}
