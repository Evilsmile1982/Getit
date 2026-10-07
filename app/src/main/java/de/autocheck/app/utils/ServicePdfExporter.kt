package de.autocheck.app.utils

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri

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

fun exportServicePdf(
    context: Context,
    uri: Uri,
    data: ServicePdfData
) {

    val document =
        PdfDocument()

    val pageWidth =
        595

    val pageHeight =
        842

    val pageInfo =
        PdfDocument.PageInfo.Builder(
            pageWidth,
            pageHeight,
            1
        ).create()

    val page =
        document.startPage(
            pageInfo
        )

    val canvas =
        page.canvas

    val titlePaint =
        Paint().apply {
            textSize = 24f
            isFakeBoldText = true
            color = android.graphics.Color.BLACK
        }

    val sectionPaint =
        Paint().apply {
            textSize = 17f
            isFakeBoldText = true
            color = android.graphics.Color.rgb(
                154,
                77,
                255
            )
        }

    val labelPaint =
        Paint().apply {
            textSize = 13f
            isFakeBoldText = true
            color = android.graphics.Color.DKGRAY
        }

    val textPaint =
        Paint().apply {
            textSize = 13f
            color = android.graphics.Color.BLACK
        }

    val smallPaint =
        Paint().apply {
            textSize = 11f
            color = android.graphics.Color.DKGRAY
        }

    var y =
        45f

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
        data.vehicle.ifBlank {
            "Fahrzeug"
        },
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

    y += 24f

    drawLabelValue(
        canvas = canvas,
        label = "Datum",
        value =
            "${data.lastServiceMonth} ${data.lastServiceYear}",
        labelPaint = labelPaint,
        textPaint = textPaint,
        y = y
    )

    y += 22f

    drawLabelValue(
        canvas = canvas,
        label = "Kilometer",
        value =
            if (
                data.lastServiceKm.isBlank()
            ) {
                "—"
            } else {
                "${data.lastServiceKm} km"
            },
        labelPaint = labelPaint,
        textPaint = textPaint,
        y = y
    )

    y += 40f

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
        value =
            "${data.nextServiceMonth} ${data.nextServiceYear}",
        labelPaint = labelPaint,
        textPaint = textPaint,
        y = y
    )

    y += 22f

    drawLabelValue(
        canvas = canvas,
        label = "Kilometer",
        value =
            if (
                data.nextServiceKm.isBlank()
            ) {
                "—"
            } else {
                "${data.nextServiceKm} km"
            },
        labelPaint = labelPaint,
        textPaint = textPaint,
        y = y
    )

    y += 40f

    canvas.drawText(
        "DOKUMENTATION",
        40f,
        y,
        sectionPaint
    )

    y += 24f

    val documentationText =
        if (
            data.documentation.isBlank()
        ) {
            "Keine Dokumentation hinterlegt."
        } else {
            data.documentation
        }

    y =
        drawWrappedText(
            canvas = canvas,
            text = documentationText,
            paint = textPaint,
            x = 40f,
            y = y,
            maxWidth = 515f
        )

    y += 20f

    drawLabelValue(
        canvas = canvas,
        label = "Kosten",
        value =
            if (
                data.cost.isBlank()
            ) {
                "—"
            } else {
                data.cost
            },
        labelPaint = labelPaint,
        textPaint = textPaint,
        y = y
    )

    y += 40f

    canvas.drawText(
        "ERINNERUNG",
        40f,
        y,
        sectionPaint
    )

    y += 24f

    val reminderText =
        if (
            data.reminderEnabled &&
            data.reminderMonthsBefore > 0
        ) {

            when (
                data.reminderMonthsBefore
            ) {

                1 ->
                    "1 Monat vorher"

                2 ->
                    "2 Monate vorher"

                3 ->
                    "3 Monate vorher"

                else ->
                    "Aktiv"
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

    document.finishPage(
        page
    )

    try {

        context.contentResolver
            .openOutputStream(uri)
            ?.use { outputStream ->

                document.writeTo(
                    outputStream
                )
            }

    } finally {

        document.close()
    }
}

private fun drawLabelValue(
    canvas: android.graphics.Canvas,
    label: String,
    value: String,
    labelPaint: Paint,
    textPaint: Paint,
    y: Float
) {

    canvas.drawText(
        "$label:",
        40f,
        y,
        labelPaint
    )

    canvas.drawText(
        value,
        190f,
        y,
        textPaint
    )
}

private fun drawWrappedText(
    canvas: android.graphics.Canvas,
    text: String,
    paint: Paint,
    x: Float,
    y: Float,
    maxWidth: Float
): Float {

    val words =
        text
            .replace(
                "\n",
                " \n "
            )
            .split(
                " "
            )

    var currentLine =
        ""

    var currentY =
        y

    val lineHeight =
        18f

    for (
        word in words
    ) {

        if (
            word == "\n"
        ) {

            canvas.drawText(
                currentLine,
                x,
                currentY,
                paint
            )

            currentLine =
                ""

            currentY +=
                lineHeight

            continue
        }

        val testLine =
            if (
                currentLine.isBlank()
            ) {
                word
            } else {
                "$currentLine $word"
            }

        if (
            paint.measureText(
                testLine
            ) <= maxWidth
        ) {

            currentLine =
                testLine

        } else {

            if (
                currentLine.isNotBlank()
            ) {

                canvas.drawText(
                    currentLine,
                    x,
                    currentY,
                    paint
                )

                currentY +=
                    lineHeight
            }

            currentLine =
                word
        }
    }

    if (
        currentLine.isNotBlank()
    ) {

        canvas.drawText(
            currentLine,
            x,
            currentY,
            paint
        )

        currentY +=
            lineHeight
    }

    return currentY
}
