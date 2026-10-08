package de.autocheck.app.utils

import android.content.Context
import android.graphics.Canvas
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

fun exportServicePdf(
    context: Context,
    uri: Uri,
    data: ServicePdfData
) {
    val document = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
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

    canvas.drawText("AutoCheck", 40f, y, smallPaint)
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

    canvas.drawText(
        "LETZTES SERVICE",
        40f,
        y,
        sectionPaint
    )
    y += 25f

    drawLabelValue(
        canvas,
        "Datum",
        buildDate(
            data.lastServiceMonth,
            data.lastServiceYear
        ),
        labelPaint,
        textPaint,
        y
    )
    y += 23f

    drawLabelValue(
        canvas,
        "Kilometer",
        if (data.lastServiceKm.isBlank()) {
            "—"
        } else {
            "${data.lastServiceKm} km"
        },
        labelPaint,
        textPaint,
        y
    )
    y += 40f

    canvas.drawText(
        "NÄCHSTES SERVICE",
        40f,
        y,
        sectionPaint
    )
    y += 25f

    drawLabelValue(
        canvas,
        "Datum",
        buildDate(
            data.nextServiceMonth,
            data.nextServiceYear
        ),
        labelPaint,
        textPaint,
        y
    )
    y += 23f

    drawLabelValue(
        canvas,
        "Kilometer",
        if (data.nextServiceKm.isBlank()) {
            "—"
        } else {
            "${data.nextServiceKm} km"
        },
        labelPaint,
        textPaint,
        y
    )
    y += 40f

    canvas.drawText(
        "DOKUMENTATION",
        40f,
        y,
        sectionPaint
    )
    y += 25f

    y = drawWrappedText(
        canvas,
        if (data.documentation.isBlank()) {
            "Keine Dokumentation hinterlegt."
        } else {
            data.documentation
        },
        textPaint,
        40f,
        y,
        515f
    )

    y += 20f

    drawLabelValue(
        canvas,
        "Kosten",
        if (data.cost.isBlank()) {
            "—"
        } else {
            data.cost
        },
        labelPaint,
        textPaint,
        y
    )
    y += 40f

    canvas.drawText(
        "ERINNERUNG",
        40f,
        y,
        sectionPaint
    )
    y += 25f

    val reminderText = if (
        data.reminderEnabled &&
        data.reminderMonthsBefore > 0
    ) {
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
        canvas,
        "Service-Erinnerung",
        reminderText,
        labelPaint,
        textPaint,
        y
    )
    y += 25f

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
    y += 35f

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
            ?.use { output ->
                document.writeTo(output)
            }
    } finally {
        document.close()
    }
}

fun exportServiceHistoryPdf(
    context: Context,
    uri: Uri,
    data: ServiceHistoryPdfData
) {
    val document = PdfDocument()

    val services = data.services.sortedWith(
        compareByDescending<ServiceInterval> {
            serviceSortValue(it)
        }.thenByDescending {
            it.id
        }
    )

    // =========================================================
    // ÜBERSICHT
    // =========================================================

    run {
        val pageInfo = PdfDocument.PageInfo.Builder(
            595,
            842,
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
            "Service-Historie",
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
        y += 40f

        canvas.drawText(
            "ÜBERSICHT",
            40f,
            y,
            sectionPaint
        )
        y += 28f

        drawLabelValue(
            canvas,
            "Serviceeinträge",
            services.size.toString(),
            labelPaint,
            textPaint,
            y
        )
        y += 25f

        val totalCost = services.sumOf {
            parseCost(it.cost)
        }

        drawLabelValue(
            canvas,
            "Gesamtkosten",
            formatEuro(totalCost),
            labelPaint,
            textPaint,
            y
        )
        y += 40f

        canvas.drawText(
            "SERVICE-HISTORIE",
            40f,
            y,
            sectionPaint
        )
        y += 28f

        if (services.isEmpty()) {

            canvas.drawText(
                "Noch keine Serviceeinträge vorhanden.",
                40f,
                y,
                textPaint
            )

        } else {

            services.forEachIndexed { index, service ->

                if (y > 750f) {
                    return@forEachIndexed
                }

                drawLabelValue(
                    canvas,
                    "${index + 1}. Service",
                    buildDate(
                        service.serviceMonth,
                        service.serviceYear
                    ),
                    labelPaint,
                    textPaint,
                    y
                )

                y += 22f

                drawLabelValue(
                    canvas,
                    "Kosten",
                    if (service.cost.isBlank()) {
                        "—"
                    } else {
                        service.cost
                    },
                    labelPaint,
                    textPaint,
                    y
                )

                y += 30f
            }
        }

        y += 25f

        canvas.drawText(
            "Erstellt mit AutoCheck",
            40f,
            y,
            smallPaint
        )

        document.finishPage(page)
    }

    // =========================================================
    // EINZELNE SERVICE-EINTRÄGE
    // =========================================================

    services.forEachIndexed { index, service ->

        val pageNumber = index + 2

        val pageInfo = PdfDocument.PageInfo.Builder(
            595,
            842,
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
            data.vehicle.ifBlank { "Fahrzeug" },
            40f,
            y,
            sectionPaint
        )

        y += 40f

        canvas.drawText(
            "SERVICE ${index + 1}",
            40f,
            y,
            sectionPaint
        )

        y += 28f

        // -----------------------------------------------------
        // SERVICE
        // -----------------------------------------------------

        drawLabelValue(
            canvas,
            "Datum",
            buildDate(
                service.serviceMonth,
                service.serviceYear
            ),
            labelPaint,
            textPaint,
            y
        )
        y += 23f

        drawLabelValue(
            canvas,
            "Kilometer",
            if (service.serviceKm.isBlank()) {
                "—"
            } else {
                "${service.serviceKm} km"
            },
            labelPaint,
            textPaint,
            y
        )
        y += 23f

        drawLabelValue(
            canvas,
            "Kosten",
            if (service.cost.isBlank()) {
                "—"
            } else {
                service.cost
            },
            labelPaint,
            textPaint,
            y
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
            canvas,
            "Datum",
            buildDate(
                service.nextServiceMonth,
                service.nextServiceYear
            ),
            labelPaint,
            textPaint,
            y
        )
        y += 23f

        drawLabelValue(
            canvas,
            "Kilometer",
            if (service.nextServiceKm.isBlank()) {
                "—"
            } else {
                "${service.nextServiceKm} km"
            },
            labelPaint,
            textPaint,
            y
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

        val reminderText = if (
            service.reminderEnabled &&
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
            canvas,
            "Service-Erinnerung",
            reminderText,
            labelPaint,
            textPaint,
            y
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

        drawWrappedText(
            canvas,
            documentation,
            textPaint,
            40f,
            y,
            515f
        )

        y = minOf(
            y + 150f,
            760f
        )

        canvas.drawText(
            "Erstellt mit AutoCheck",
            40f,
            y,
            smallPaint
        )

        document.finishPage(page)
    }

    try {
        context.contentResolver
            .openOutputStream(uri)
            ?.use { output ->
                document.writeTo(output)
            }
    } finally {
        document.close()
    }
}

private fun drawLabelValue(
    canvas: Canvas,
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

private fun drawWrappedText(
    canvas: Canvas,
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
    val lineHeight = paint.textSize + 6f

    val paragraphs = text
        .replace("\r\n", "\n")
        .replace("\r", "\n")
        .split("\n")

    for (paragraph in paragraphs) {

        if (paragraph.isBlank()) {
            currentY += lineHeight
            continue
        }

        val words = paragraph
            .trim()
            .split(Regex("\\s+"))

        var line = ""

        for (word in words) {

            val candidate =
                if (line.isBlank()) {
                    word
                } else {
                    "$line $word"
                }

            if (
                paint.measureText(candidate) <= maxWidth
            ) {
                line = candidate
            } else {

                if (line.isNotBlank()) {
                    canvas.drawText(
                        line,
                        x,
                        currentY,
                        paint
                    )

                    currentY += lineHeight
                }

                line = word
            }
        }

        if (line.isNotBlank()) {
            canvas.drawText(
                line,
                x,
                currentY,
                paint
            )

            currentY += lineHeight
        }
    }

    return currentY
}

private fun serviceSortValue(
    service: ServiceInterval
): Long {
    val year = service.serviceYear
        .filter { it.isDigit() }
        .toIntOrNull()
        ?: 0

    val month = monthNumber(
        service.serviceMonth
    )

    return year.toLong() * 100L +
        month.toLong()
}

private fun monthNumber(
    month: String
): Int {
    return when (
        month
            .trim()
            .lowercase(Locale.GERMANY)
    ) {
        "1", "01", "jänner", "januar" -> 1
        "2", "02", "februar" -> 2
        "3", "03", "märz", "maerz" -> 3
        "4", "04", "april" -> 4
        "5", "05", "mai" -> 5
        "6", "06", "juni" -> 6
        "7", "07", "juli" -> 7
        "8", "08", "august" -> 8
        "9", "09", "september" -> 9
        "10", "10.", "oktober" -> 10
        "11", "11.", "november" -> 11
        "12", "12.", "dezember" -> 12
        else -> {
            month
                .filter { it.isDigit() }
                .toIntOrNull()
                ?.coerceIn(0, 12)
                ?: 0
        }
    }
}

private fun buildDate(
    month: String,
    year: String
): String {
    val m = month.trim()
    val y = year.trim()

    return when {
        m.isNotBlank() && y.isNotBlank() -> "$m $y"
        m.isNotBlank() -> m
        y.isNotBlank() -> y
        else -> "—"
    }
}

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

    val comma = value.lastIndexOf(',')
    val dot = value.lastIndexOf('.')

    return try {

        if (comma >= 0 && dot >= 0) {

            if (comma > dot) {
                value = value
                    .replace(".", "")
                    .replace(",", ".")
            } else {
                value = value.replace(",", "")
            }

        } else if (comma >= 0) {

            val decimals =
                value.length - comma - 1

            value =
                if (decimals in 1..2) {
                    value.replace(",", ".")
                } else {
                    value.replace(",", "")
                }

        } else if (dot >= 0) {

            val decimals =
                value.length - dot - 1

            if (decimals > 2) {
                value = value.replace(".", "")
            }
        }

        value.toDoubleOrNull() ?: 0.0

    } catch (_: Exception) {
        0.0
    }
}

private fun formatEuro(
    value: Double
): String {
    return String.format(
        Locale.GERMANY,
        "%.2f €",
        value
    )
}
