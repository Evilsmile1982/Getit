package de.autocheck.app.utils

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import de.autocheck.app.data.ServiceInterval
import java.util.Locale

data class ServicePdfData(
    val vehicle: String,
    val lastServiceMonth: String,
    val lastServiceYear: String,
    val lastServiceKm: String,
    val nextServiceMonth: String,
    val nextServiceYear: String,
    val nextServiceKm: String,
    val documentation: String,
    val cost: String,
    val reminderEnabled: Boolean,
    val reminderMonthsBefore: Int
)

data class ServiceHistoryPdfData(
    val vehicle: String,
    val services: List<ServiceInterval>
)

/**
 * Exportiert das aktuell ausgewählte Service als PDF.
 */
fun exportServicePdf(
    context: Context,
    uri: Uri,
    data: ServicePdfData
) {
    val document = PdfDocument()

    val pageWidth = 595
    val pageHeight = 842

    val pageInfo = PdfDocument.PageInfo.Builder(
        pageWidth,
        pageHeight,
        1
    ).create()

    val page = document.startPage(pageInfo)
    val canvas = page.canvas

    val titlePaint = Paint().apply {
        textSize = 24f
        isFakeBoldText = true
        color = android.graphics.Color.BLACK
    }

    val sectionPaint = Paint().apply {
        textSize = 17f
        isFakeBoldText = true
        color = android.graphics.Color.rgb(154, 77, 255)
    }

    val labelPaint = Paint().apply {
        textSize = 13f
        isFakeBoldText = true
        color = android.graphics.Color.DKGRAY
    }

    val textPaint = Paint().apply {
        textSize = 13f
        color = android.graphics.Color.BLACK
    }

    val smallPaint = Paint().apply {
        textSize = 11f
        color = android.graphics.Color.DKGRAY
    }

    var y = 45f

    canvas.drawText(
        "AutoCheck",
        40f,
        y,
        smallPaint
    )

    y += 35f

    canvas.drawText(
        "Service und Intervalle",
        40f,
        y,
        titlePaint
    )

    y += 28f

    canvas.drawText(
        data.vehicle.ifBlank { "Fahrzeug" },
        40f,
        y,
        sectionPaint
    )

    y += 35f

    // ---------------------------------------------------------
    // LETZTES SERVICE
    // ---------------------------------------------------------

    canvas.drawText(
        "LETZTES SERVICE",
        40f,
        y,
        sectionPaint
    )

    y += 24f

    drawLabelValue(
        canvas = canvas,
        label = "Datum",
        value = buildDate(
            data.lastServiceMonth,
            data.lastServiceYear
        ),
        labelPaint = labelPaint,
        textPaint = textPaint,
        y = y
    )

    y += 22f

    drawLabelValue(
        canvas = canvas,
        label = "Kilometer",
        value = if (data.lastServiceKm.isBlank()) {
            "—"
        } else {
            "${data.lastServiceKm} km"
        },
        labelPaint = labelPaint,
        textPaint = textPaint,
        y = y
    )

    y += 40f

    // ---------------------------------------------------------
    // NÄCHSTES SERVICE
    // ---------------------------------------------------------

    canvas.drawText(
        "NÄCHSTES SERVICE",
        40f,
        y,
        sectionPaint
    )

    y += 24f

    drawLabelValue(
        canvas = canvas,
        label = "Datum",
        value = buildDate(
            data.nextServiceMonth,
            data.nextServiceYear
        ),
        labelPaint = labelPaint,
        textPaint = textPaint,
        y = y
    )

    y += 22f

    drawLabelValue(
        canvas = canvas,
        label = "Kilometer",
        value = if (data.nextServiceKm.isBlank()) {
            "—"
        } else {
            "${data.nextServiceKm} km"
        },
        labelPaint = labelPaint,
        textPaint = textPaint,
        y = y
    )

    y += 40f

    // ---------------------------------------------------------
    // DOKUMENTATION
    // ---------------------------------------------------------

    canvas.drawText(
        "DOKUMENTATION",
        40f,
        y,
        sectionPaint
    )

    y += 24f

    val documentationText =
        if (data.documentation.isBlank()) {
            "Keine Dokumentation hinterlegt."
        } else {
            data.documentation
        }

    y = drawWrappedText(
        canvas = canvas,
        text = documentationText,
        paint = textPaint,
        x = 40f,
        y = y,
        maxWidth = 515f
    )

    y += 20f

    // ---------------------------------------------------------
    // KOSTEN
    // ---------------------------------------------------------

    drawLabelValue(
        canvas = canvas,
        label = "Kosten",
        value = if (data.cost.isBlank()) {
            "—"
        } else {
            data.cost
        },
        labelPaint = labelPaint,
        textPaint = textPaint,
        y = y
    )

    y += 40f

    // ---------------------------------------------------------
    // ERINNERUNG
    // ---------------------------------------------------------

    canvas.drawText(
        "ERINNERUNG",
        40f,
        y,
        sectionPaint
    )

    y += 24f

    val reminderText =
        if (data.reminderEnabled && data.reminderMonthsBefore > 0) {
            when (data.reminderMonthsBefore) {
                1 -> "1 Monat vorher"
                2 -> "2 Monate vorher"
                3 -> "3 Monate vorher"
                else -> "${data.reminderMonthsBefore} Monate vorher"
            }
        } else {
            "Keine Erinnerung"
        }

    drawLabelValue(
        canvas = canvas,
        label = "Service-Erinnerung",
        value = reminderText,
        labelPaint = labelPaint,
        textPaint = textPaint,
        y = y
    )

    y += 22f

    canvas.drawText(
        "Die Erinnerung erfolgt zeitbasiert.",
        40f,
        y,
        smallPaint
    )

    y += 18f

    canvas.drawText(
        "Es werden keine Kilometer-Erinnerungen verwendet.",
        40f,
        y,
        smallPaint
    )

    y += 45f

    canvas.drawText(
        "Erstellt mit AutoCheck",
        40f,
        y,
        smallPaint
    )

    document.finishPage(page)

    try {
        context.contentResolver
            .openOutputStream(uri)
            ?.use { outputStream ->
                document.writeTo(outputStream)
            }
    } finally {
        document.close()
    }
}

/**
 * Exportiert die komplette Service-Historie als eigenes PDF.
 *
 * Das PDF enthält:
 * - Fahrzeug
 * - Anzahl der Serviceeinträge
 * - Gesamtkosten
 * - alle Serviceeinträge
 * - Kilometer
 * - nächstes Service
 * - nächste Kilometer
 * - Erinnerung
 * - Dokumentation
 */
fun exportServiceHistoryPdf(
    context: Context,
    uri: Uri,
    data: ServiceHistoryPdfData
) {
    val document = PdfDocument()

    val pageWidth = 595
    val pageHeight = 842

    val sortedServices = data.services
        .sortedWith(
            compareByDescending<ServiceInterval> {
                serviceSortValue(it)
            }.thenByDescending {
                it.id
            }
        )

    // =========================================================
    // SEITE 1 – ÜBERSICHT
    // =========================================================

    run {
        val pageInfo = PdfDocument.PageInfo.Builder(
            pageWidth,
            pageHeight,
            1
        ).create()

        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val titlePaint = Paint().apply {
            textSize = 24f
            isFakeBoldText = true
            color = android.graphics.Color.BLACK
        }

        val sectionPaint = Paint().apply {
            textSize = 17f
            isFakeBoldText = true
            color = android.graphics.Color.rgb(154, 77, 255)
        }

        val labelPaint = Paint().apply {
            textSize = 13f
            isFakeBoldText = true
            color = android.graphics.Color.DKGRAY
        }

        val textPaint = Paint().apply {
            textSize = 13f
            color = android.graphics.Color.BLACK
        }

        val smallPaint = Paint().apply {
            textSize = 11f
            color = android.graphics.Color.DKGRAY
        }

        var y = drawHistoryPageHeader(
            canvas = canvas,
            vehicle = data.vehicle,
            pageNumber = 1,
            titlePaint = titlePaint,
            sectionPaint = sectionPaint,
            smallPaint = smallPaint
        )

        canvas.drawText(
            "ÜBERSICHT",
            40f,
            y,
            sectionPaint
        )

        y += 30f

        drawLabelValue(
            canvas = canvas,
            label = "Serviceeinträge",
            value = sortedServices.size.toString(),
            labelPaint = labelPaint,
            textPaint = textPaint,
            y = y
        )

        y += 30f

        val totalCost = sortedServices.sumOf {
            parseCost(it.cost)
        }

        drawLabelValue(
            canvas = canvas,
            label = "Gesamtkosten",
            value = formatEuro(totalCost),
            labelPaint = labelPaint,
            textPaint = textPaint,
            y = y
        )

        y += 45f

        canvas.drawText(
            "SERVICE-HISTORIE",
            40f,
            y,
            sectionPaint
        )

        y += 28f

        if (sortedServices.isEmpty()) {
            canvas.drawText(
                "Noch keine Serviceeinträge vorhanden.",
                40f,
                y,
                textPaint
            )

            y += 25f

            canvas.drawText(
                "Sobald ein Service gespeichert wurde,",
                40f,
                y,
                smallPaint
            )

            y += 17f

            canvas.drawText(
                "erscheint es hier in der Service-Historie.",
                40f,
                y,
                smallPaint
            )
        } else {
            sortedServices.forEachIndexed { index, service ->

                val date = buildDate(
                    service.serviceMonth,
                    service.serviceYear
                )

                val cost = if (service.cost.isBlank()) {
                    "—"
                } else {
                    service.cost
                }

                drawLabelValue(
                    canvas = canvas,
                    label = "${index + 1}. Service",
                    value = date,
                    labelPaint = labelPaint,
                    textPaint = textPaint,
                    y = y
                )

                y += 21f

                drawLabelValue(
                    canvas = canvas,
                    label = "Kosten",
                    value = cost,
                    labelPaint = labelPaint,
                    textPaint = textPaint,
                    y = y
                )

                y += 32f

                if (y > 760f && index < sortedServices.lastIndex) {
                    break
                }
            }
        }

        y += 30f

        canvas.drawText(
            "Erstellt mit AutoCheck",
            40f,
            y,
            smallPaint
        )

        document.finishPage(page)
    }

    // =========================================================
    // JEDE SERVICE-HISTORIE AUF EIGENER SEITE
    // =========================================================

    sortedServices.forEachIndexed { index, service ->

        val pageNumber = index + 2

        val pageInfo = PdfDocument.PageInfo.Builder(
            pageWidth,
            pageHeight,
            pageNumber
        ).create()

        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val titlePaint = Paint().apply {
            textSize = 22f
            isFakeBoldText = true
            color = android.graphics.Color.BLACK
        }

        val sectionPaint = Paint().apply {
            textSize = 17f
            isFakeBoldText = true
            color = android.graphics.Color.rgb(154, 77, 255)
        }

        val labelPaint = Paint().apply {
            textSize = 13f
            isFakeBoldText = true
            color = android.graphics.Color.DKGRAY
        }

        val textPaint = Paint().apply {
            textSize = 13f
            color = android.graphics.Color.BLACK
        }

        val smallPaint = Paint().apply {
            textSize = 11f
            color = android.graphics.Color.DKGRAY
        }

        var y = drawHistoryPageHeader(
            canvas = canvas,
            vehicle = data.vehicle,
            pageNumber = pageNumber,
            titlePaint = titlePaint,
            sectionPaint = sectionPaint,
            smallPaint = smallPaint
        )

        canvas.drawText(
            "SERVICE ${index + 1}",
            40f,
            y,
            sectionPaint
        )

        y += 32f

        // -----------------------------------------------------
        // SERVICE-DATEN
        // -----------------------------------------------------

        drawLabelValue(
            canvas = canvas,
            label = "Datum",
            value = buildDate(
                service.serviceMonth,
                service.serviceYear
            ),
            labelPaint = labelPaint,
            textPaint = textPaint,
            y = y
        )

        y += 23f

        drawLabelValue(
            canvas = canvas,
            label = "Kilometer",
            value = if (service.serviceKm.isBlank()) {
                "—"
            } else {
                "${service.serviceKm} km"
            },
            labelPaint = labelPaint,
            textPaint = textPaint,
            y = y
        )

        y += 23f

        drawLabelValue(
            canvas = canvas,
            label = "Kosten",
            value = if (service.cost.isBlank()) {
                "—"
            } else {
                service.cost
            },
            labelPaint = labelPaint,
            textPaint = textPaint,
            y = y
        )

        y += 40f

        // -----------------------------------------------------
        // NÄCHSTES SERVICE
        // -----------------------------------------------------

        canvas.drawText(
            "NÄCHSTES SERVICE",
            40f,
            y,
            sectionPaint
        )

        y += 25f

        drawLabelValue(
            canvas = canvas,
            label = "Datum",
            value = buildDate(
                service.nextServiceMonth,
                service.nextServiceYear
            ),
            labelPaint = labelPaint,
            textPaint = textPaint,
            y = y
        )

        y += 23f

        drawLabelValue(
            canvas = canvas,
            label = "Kilometer",
            value = if (service.nextServiceKm.isBlank()) {
                "—"
            } else {
                "${service.nextServiceKm} km"
            },
            labelPaint = labelPaint,
            textPaint = textPaint,
            y = y
        )

        y += 40f

        // -----------------------------------------------------
        // ERINNERUNG
        // -----------------------------------------------------

        canvas.drawText(
            "ERINNERUNG",
            40f,
            y,
            sectionPaint
        )

        y += 25f

        val reminderText =
            if (service.reminderEnabled &&
                service.reminderMonthsBefore > 0
            ) {
                when (service.reminderMonthsBefore) {
                    1 -> "1 Monat vorher"
                    2 -> "2 Monate vorher"
                    3 -> "3 Monate vorher"
                    else ->
                        "${service.reminderMonthsBefore} Monate vorher"
                }
            } else {
                "Keine Erinnerung"
            }

        drawLabelValue(
            canvas = canvas,
            label = "Service-Erinnerung",
            value = reminderText,
            labelPaint = labelPaint,
            textPaint = textPaint,
            y = y
        )

        y += 23f

        canvas.drawText(
            "Zeitbasierte Erinnerung.",
            40f,
            y,
            smallPaint
        )

        y += 40f

        // -----------------------------------------------------
        // DOKUMENTATION
        // -----------------------------------------------------

        canvas.drawText(
            "DOKUMENTATION",
            40f,
            y,
            sectionPaint
        )

        y += 25f

        val documentation =
            if (service.documentation.isBlank()) {
                "Keine Dokumentation hinterlegt."
            } else {
                service.documentation
            }

        y = drawWrappedText(
            canvas = canvas,
            text = documentation,
            paint = textPaint,
            x = 40f,
            y = y,
            maxWidth = 515f
        )

        y += 45f

        canvas.drawText(
            "Erstellt mit AutoCheck",
            40f,
            y,
            smallPaint
        )

        document.finishPage(page)
    }

    // =========================================================
    // PDF SPEICHERN
    // =========================================================

    try {
        context.contentResolver
            .openOutputStream(uri)
            ?.use { outputStream ->
                document.writeTo(outputStream)
            }
    } finally {
        document.close()
    }
}

// =============================================================
// HILFSFUNKTIONEN
// =============================================================

/**
 * Zeichnet ein Label und den dazugehörigen Wert.
 */
private fun drawLabelValue(
    canvas: android.graphics.Canvas,
    label: String,
    value: String,
    labelPaint: Paint,
    textPaint: Paint,
    y: Float
) {
    canvas.drawText(
        label,
        40f,
        y,
        labelPaint
    )

    canvas.drawText(
        value.ifBlank { "—" },
        180f,
        y,
        textPaint
    )
}

/**
 * Zeichnet längeren Text mit automatischem Zeilenumbruch.
 */
private fun drawWrappedText(
    canvas: android.graphics.Canvas,
    text: String,
    paint: Paint,
    x: Float,
    y: Float,
    maxWidth: Float
): Float {
    if (text.isBlank()) {
        return y
    }

    var currentY = y

    val lineHeight =
        paint.textSize + 6f

    val paragraphs = text
        .replace("\r\n", "\n")
        .replace("\r", "\n")
        .split("\n")

    for (paragraph in paragraphs) {

        if (paragraph.isBlank()) {
            currentY += lineHeight
            continue
        }

        val words = paragraph.trim().split(
            Regex("\\s+")
        )

        var currentLine = ""

        for (word in words) {

            val candidate =
                if (currentLine.isBlank()) {
                    word
                } else {
                    "$currentLine $word"
                }

            if (
                paint.measureText(candidate) <= maxWidth
            ) {
                currentLine = candidate
            } else {

                if (currentLine.isNotBlank()) {
                    canvas.drawText(
                        currentLine,
                        x,
                        currentY,
                        paint
                    )

                    currentY += lineHeight
                }

                currentLine = word
            }
        }

        if (currentLine.isNotBlank()) {
            canvas.drawText(
                currentLine,
                x,
                currentY,
                paint
            )

            currentY += lineHeight
        }
    }

    return currentY
}

/**
 * Erstellt die Kopfzeile einer Historienseite.
 */
private fun drawHistoryPageHeader(
    canvas: android.graphics.Canvas,
    vehicle: String,
    pageNumber: Int,
    titlePaint: Paint,
    sectionPaint: Paint,
    smallPaint: Paint
): Float {
    var y = 45f

    canvas.drawText(
        "AutoCheck",
        40f,
        y,
        smallPaint
    )

    canvas.drawText(
        "Seite $pageNumber",
        485f,
        y,
        smallPaint
    )

    y += 35f

    canvas.drawText(
        "Service-Historie",
        40f,
        y,
        titlePaint
    )

    y += 28f

    canvas.drawText(
        vehicle.ifBlank { "Fahrzeug" },
        40f,
        y,
        sectionPaint
    )

    y += 35f

    return y
}

/**
 * Sortierwert für die Service-Historie.
 *
 * Jahr + Monat werden zu einem numerischen Wert kombiniert.
 */
private fun serviceSortValue(
    service: ServiceInterval
): Long {
    val year = service.serviceYear
        .filter { it.isDigit() }
        .toIntOrNull()
        ?: 0

    val month = serviceMonthToNumber(
        service.serviceMonth
    )

    return year.toLong() * 100L + month.toLong()
}

/**
 * Wandelt einen Monatsnamen oder eine Monatszahl in eine Zahl um.
 */
private fun serviceMonthToNumber(
    month: String
): Int {
    val normalized = month
        .trim()
        .lowercase(Locale.GERMANY)

    return when (normalized) {
        "1", "01", "jänner", "januar" -> 1
        "2", "02", "februar" -> 2
        "3", "03", "märz", "maerz" -> 3
        "4", "04", "april" -> 4
        "5", "05", "mai" -> 5
        "6", "06", "juni" -> 6
        "7", "07", "juli" -> 7
        "8", "08", "august" -> 8
        "9", "09", "september" -> 9
        "10", "oktober" -> 10
        "11", "november" -> 11
        "12", "dezember" -> 12
        else -> {
            normalized
                .filter { it.isDigit() }
                .toIntOrNull()
                ?.coerceIn(0, 12)
                ?: 0
        }
    }
}

/**
 * Erstellt eine lesbare Datumsanzeige.
 */
private fun buildDate(
    month: String,
    year: String
): String {
    val cleanMonth = month.trim()
    val cleanYear = year.trim()

    return when {
        cleanMonth.isNotBlank() &&
            cleanYear.isNotBlank() -> {
            "$cleanMonth $cleanYear"
        }

        cleanMonth.isNotBlank() -> {
            cleanMonth
        }

        cleanYear.isNotBlank() -> {
            cleanYear
        }

        else -> {
            "—"
        }
    }
}

/**
 * Versucht verschiedene Kostenformate zu erkennen.
 *
 * Beispiele:
 * 350
 * 350 €
 * 350,50
 * 350.50
 * 1.250,50
 * 1,250.50
 */
private fun parseCost(
    cost: String
): Double {
    if (cost.isBlank()) {
        return 0.0
    }

    var value = cost
        .trim()
        .replace("€", "")
        .replace("EUR", "", ignoreCase = true)
        .replace(" ", "")

    if (value.isBlank()) {
        return 0.0
    }

    val lastComma = value.lastIndexOf(',')
    val lastDot = value.lastIndexOf('.')

    try {
        if (
            lastComma >= 0 &&
            lastDot >= 0
        ) {
            // Beide Zeichen vorhanden.
            // Das zuletzt vorkommende Zeichen wird als Dezimaltrennzeichen
            // interpretiert.

            if (lastComma > lastDot) {
                // Beispiel: 1.250,50
                value = value
                    .replace(".", "")
                    .replace(",", ".")
            } else {
                // Beispiel: 1,250.50
                value = value
                    .replace(",", "")
            }
        } else if (lastComma >= 0) {
            val decimals =
                value.length - lastComma - 1

            value =
                if (decimals in 1..2) {
                    // Beispiel: 350,50
                    value.replace(",", ".")
                } else {
                    // Beispiel: 1,250
                    value.replace(",", "")
                }
        } else if (lastDot >= 0) {
            val decimals =
                value.length - lastDot - 1

            value =
                if (decimals in 1..2) {
                    // Beispiel: 350.50
                    value
                } else {
                    // Beispiel: 1.250
                    value.replace(".", "")
                }
        }

        return value.toDoubleOrNull()
            ?: 0.0

    } catch (_: Exception) {
        return 0.0
    }
}

/**
 * Formatiert einen Betrag als Euro.
 */
private fun formatEuro(
    value: Double
): String {
    return String.format(
        Locale.GERMANY,
        "%.2f €",
        value
    )
}

/**
 * Berechnet eine ungefähre Höhe eines Historieneintrags.
 *
 * Die Funktion bleibt bewusst im Exporter vorhanden,
 * damit die PDF-Erstellung auch bei späteren Layout-
 * Erweiterungen verwendet werden kann.
 */
private fun calculateHistoryEntryHeight(
    service: ServiceInterval
): Float {
    val documentationLength =
        service.documentation.length

    val additionalLines =
        documentationLength / 70

    return 260f +
        additionalLines * 17f
}
