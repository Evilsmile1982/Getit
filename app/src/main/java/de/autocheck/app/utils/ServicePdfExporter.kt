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

fun exportServiceHistoryPdf(
    context: Context,
    uri: Uri,
    data: ServiceHistoryPdfData
) {

    val document =
        PdfDocument()

    val pageWidth =
        595

    val pageHeight =
        842

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
            textSize = 12f
            color = android.graphics.Color.BLACK
        }

    val smallPaint =
        Paint().apply {
            textSize = 10f
            color = android.graphics.Color.DKGRAY
        }

    val services =
        data.services.sortedWith(
            compareByDescending<ServiceInterval> {
                serviceSortValue(it)
            }.thenByDescending {
                it.id
            }
        )

    var pageNumber =
        1

    var page =
        document.startPage(
            PdfDocument.PageInfo.Builder(
                pageWidth,
                pageHeight,
                pageNumber
            ).create()
        )

    var canvas =
        page.canvas

    var y =
        drawHistoryPageHeader(
            canvas = canvas,
            titlePaint = titlePaint,
            sectionPaint = sectionPaint,
            smallPaint = smallPaint,
            vehicle = data.vehicle,
            pageNumber = pageNumber
        )

    val totalCost =
        services.sumOf {
            parseCost(
                it.cost
            )
        }

    y += 15f

    canvas.drawText(
        "ÜBERSICHT",
        40f,
        y,
        sectionPaint
    )

    y += 24f

    drawLabelValue(
        canvas = canvas,
        label = "Anzahl Services",
        value = services.size.toString(),
        labelPaint = labelPaint,
        textPaint = textPaint,
        y = y
    )

    y += 22f

    drawLabelValue(
        canvas = canvas,
        label = "Gesamtkosten",
        value = formatEuro(
            totalCost
        ),
        labelPaint = labelPaint,
        textPaint = textPaint,
        y = y
    )

    y += 35f

    if (
        services.isEmpty()
    ) {

        canvas.drawText(
            "Keine Service-Termine vorhanden.",
            40f,
            y,
            textPaint
        )

    } else {

        services.forEachIndexed { index, service ->

            val estimatedHeight =
                calculateHistoryEntryHeight(
                    service
                )

            if (
                y + estimatedHeight > 760f
            ) {

                document.finishPage(
                    page
                )

                pageNumber++

                page =
                    document.startPage(
                        PdfDocument.PageInfo.Builder(
                            pageWidth,
                            pageHeight,
                            pageNumber
                        ).create()
                    )

                canvas =
                    page.canvas

                y =
                    drawHistoryPageHeader(
                        canvas = canvas,
                        titlePaint = titlePaint,
                        sectionPaint = sectionPaint,
                        smallPaint = smallPaint,
                        vehicle = data.vehicle,
                        pageNumber = pageNumber
                    )

                y += 20f
            }

            canvas.drawText(
                "SERVICE ${index + 1}",
                40f,
                y,
                sectionPaint
            )

            y += 24f

            val serviceDate =
                if (
                    service.serviceMonth.isBlank() &&
                    service.serviceYear.isBlank()
                ) {
                    "—"
                } else {
                    "${service.serviceMonth} ${service.serviceYear}"
                }

            drawLabelValue(
                canvas = canvas,
                label = "Datum",
                value = serviceDate,
                labelPaint = labelPaint,
                textPaint = textPaint,
                y = y
            )

            y += 20f

            drawLabelValue(
                canvas = canvas,
                label = "Kilometer",
                value =
                    if (
                        service.serviceKm.isBlank()
                    ) {
                        "—"
                    } else {
                        "${service.serviceKm} km"
                    },
                labelPaint = labelPaint,
                textPaint = textPaint,
                y = y
            )

            y += 20f

            drawLabelValue(
                canvas = canvas,
                label = "Kosten",
                value =
                    if (
                        service.cost.isBlank()
                    ) {
                        "—"
                    } else {
                        service.cost
                    },
                labelPaint = labelPaint,
                textPaint = textPaint,
                y = y
            )

            y += 20f

            val nextServiceDate =
                if (
                    service.nextServiceMonth.isBlank() &&
                    service.nextServiceYear.isBlank()
                ) {
                    "—"
                } else {
                    "${service.nextServiceMonth} ${service.nextServiceYear}"
                }

            drawLabelValue(
                canvas = canvas,
                label = "Nächstes Service",
                value = nextServiceDate,
                labelPaint = labelPaint,
                textPaint = textPaint,
                y = y
            )

            y += 20f

            drawLabelValue(
                canvas = canvas,
                label = "Nächste km",
                value =
                    if (
                        service.nextServiceKm.isBlank()
                    ) {
                        "—"
                    } else {
                        "${service.nextServiceKm} km"
                    },
                labelPaint = labelPaint,
                textPaint = textPaint,
                y = y
            )

            y += 20f

            val reminderText =
                if (
                    service.reminderEnabled &&
                    service.reminderMonthsBefore > 0
                ) {

                    when (
                        service.reminderMonthsBefore
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
                label = "Erinnerung",
                value = reminderText,
                labelPaint = labelPaint,
                textPaint = textPaint,
                y = y
            )

            y += 25f

            canvas.drawText(
                "Dokumentation:",
                40f,
                y,
                labelPaint
            )

            y += 18f

            val documentation =
                if (
                   
