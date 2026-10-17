
package de.autocheck.app

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfDocument
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.ByteArrayOutputStream

private val ContractGold = Color(0xFFD4AF37)
private val ContractBackground = Color(0xFF101010)
private val ContractPanel = Color(0xFF222222)

@Composable
fun PurchaseContractScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current

    var statusMessage by remember {
        mutableStateOf("Der leere CARVITA-Kaufvertrag ist bereit.")
    }

    var previewPdfBytes by remember {
        mutableStateOf<ByteArray?>(null)
    }

    previewPdfBytes?.let { pdfBytes ->
        PdfZoomPreview(
            pdfBytes = pdfBytes,
            documentName = "CARVITA_Fahrzeugkaufvertrag.pdf",
            onClose = {
                previewPdfBytes = null
            }
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .background(ContractBackground)
            .padding(
                start = 20.dp,
                end = 20.dp,
                top = 28.dp,
                bottom = 12.dp
            ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = 8.dp,
                    bottom = 18.dp
                ),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Column {
                        Image(
                            painter = painterResource(id = R.drawable.bild_4),
                            contentDescription = "CARVITA-Logo",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(105.dp),
                            contentScale = ContentScale.Fit
                        )

                        Text(
                            text = "Fahrzeugkaufvertrag",
                            modifier = Modifier.padding(top = 8.dp),
                            color = Color.White,
                            fontSize = 25.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "Universelle, leere PDF-Vorlage für Fahrzeuge aller Art.",
                            modifier = Modifier.padding(top = 8.dp),
                            color = Color.LightGray,
                            fontSize = 14.sp
                        )
                    }
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = ContractPanel
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "Inhalt der PDF-Vorlage",
                                color = ContractGold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )

                            Text("• Verkäufer- und Käuferdaten", color = Color.White)
                            Text("• Fahrzeugdaten und Identifikationsnummer", color = Color.White)
                            Text("• Kaufpreis und Zahlungsvereinbarung", color = Color.White)
                            Text("• Zustand, bekannte Mängel und Zubehör", color = Color.White)
                            Text("• Übergabe, Datum und Kilometerstand", color = Color.White)
                            Text("• Unterschriften beider Vertragsparteien", color = Color.White)
                        }
                    }
                }

                item {
                    Button(
                        onClick = {
                            try {
                                previewPdfBytes =
                                    createBlankVehiclePurchaseContract(context)

                                statusMessage =
                                    "Prüfe den Vertrag in der Vorschau, bevor du ihn speicherst oder druckst."
                            } catch (error: Exception) {
                                previewPdfBytes = null
                                statusMessage =
                                    "PDF-Erstellung fehlgeschlagen: ${
                                        error.localizedMessage
                                            ?: "Unbekannter Fehler"
                                    }"
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ContractGold,
                            contentColor = Color.Black
                        )
                    ) {
                        Text(
                            text = "Kaufvertrag vorher ansehen",
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }

                    Text(
                        text = "In der Vorschau kannst du die Seiten prüfen, zoomen und anschließend speichern oder drucken.",
                        modifier = Modifier.padding(top = 8.dp),
                        color = Color.LightGray,
                        fontSize = 12.sp
                    )
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = ContractPanel
                        )
                    ) {
                        Text(
                            text = statusMessage,
                            modifier = Modifier.padding(14.dp),
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

private fun createBlankVehiclePurchaseContract(
    context: android.content.Context
): ByteArray {
    val document = PdfDocument()
    val output = ByteArrayOutputStream()

    val logo = BitmapFactory.decodeResource(
        context.resources,
        R.drawable.bild_4
    ) ?: throw IllegalStateException(
        "Das CARVITA-Logo bild_4.png konnte nicht geladen werden."
    )

    try {
        val pageWidth = 595
        val pageHeight = 842
        val margin = 42f
        val contentWidth = pageWidth - margin * 2

        val goldPaint = Paint().apply {
            color = android.graphics.Color.rgb(145, 108, 20)
            textSize = 18f
            isFakeBoldText = true
            isAntiAlias = true
        }

        val sectionPaint = Paint().apply {
            color = android.graphics.Color.rgb(40, 40, 40)
            textSize = 11f
            isFakeBoldText = true
            isAntiAlias = true
        }

        val textPaint = Paint().apply {
            color = android.graphics.Color.rgb(35, 35, 35)
            textSize = 9.5f
            isAntiAlias = true
        }

        val smallPaint = Paint().apply {
            color = android.graphics.Color.rgb(90, 90, 90)
            textSize = 8f
            isAntiAlias = true
        }

        val linePaint = Paint().apply {
            color = android.graphics.Color.rgb(150, 150, 150)
            strokeWidth = 0.8f
            isAntiAlias = true
        }

        val footerPaint = Paint().apply {
            color = android.graphics.Color.rgb(90, 90, 90)
            textSize = 8.5f
            isAntiAlias = true
        }

        var pageNumber = 1

        var page: PdfDocument.Page = document.startPage(
            PdfDocument.PageInfo.Builder(
                pageWidth,
                pageHeight,
                pageNumber
            ).create()
        )

        var canvas: Canvas = page.canvas
        var y = 38f

        fun drawHeader() {
            val logoRect = Rect(
                0,
                0,
                logo.width,
                logo.height
            )

            val maxLogoWidth = 125f
            val maxLogoHeight = 42f
            val scale = minOf(
                maxLogoWidth / logo.width,
                maxLogoHeight / logo.height
            )

            val drawWidth = logo.width * scale
            val drawHeight = logo.height * scale

            val destination = RectF(
                margin,
                y - 20f,
                margin + drawWidth,
                y - 20f + drawHeight
            )

            canvas.drawBitmap(
                logo,
                logoRect,
                destination,
                Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
            )

            canvas.drawText(
                "FAHRZEUGKAUFVERTRAG",
                margin,
                y + 34f,
                sectionPaint
            )

            canvas.drawLine(
                margin,
                y + 44f,
                pageWidth - margin,
                y + 44f,
                linePaint
            )

            y += 61f
        }

        fun drawFooter() {
            val footerY = pageHeight - 24f
            val dividerY = footerY - 14f

            canvas.drawLine(
                margin,
                dividerY,
                pageWidth - margin,
                dividerY,
                linePaint
            )

            canvas.drawText(
                "CARVITA – Fahrzeugkaufvertrag",
                margin,
                footerY,
                footerPaint
            )

            val pageText = "Seite $pageNumber"
            val pageTextWidth = footerPaint.measureText(pageText)

            canvas.drawText(
                pageText,
                pageWidth - margin - pageTextWidth,
                footerY,
                footerPaint
            )
        }

        fun newPage() {
            drawFooter()
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
            y = 38f
            drawHeader()
        }

        fun ensureSpace(height: Float) {
            if (y + height > pageHeight - 58f) {
                newPage()
            }
        }

        fun section(title: String) {
            ensureSpace(29f)
            y += 5f
            canvas.drawText(title, margin, y, sectionPaint)
            y += 7f
            canvas.drawLine(
                margin,
                y,
                pageWidth - margin,
                y,
                linePaint
            )
            y += 15f
        }

        fun blankField(
            label: String,
            width: Float = contentWidth,
            height: Float = 25f
        ) {
            ensureSpace(height)

            if (label.isNotBlank()) {
                canvas.drawText(label, margin, y, textPaint)
            }

            canvas.drawLine(
                margin,
                y + 14f,
                margin + width,
                y + 14f,
                linePaint
            )

            y += height
        }

        fun twoFields(
            leftLabel: String,
            rightLabel: String
        ) {
            val gap = 18f
            val halfWidth = (contentWidth - gap) / 2f
            ensureSpace(27f)

            canvas.drawText(leftLabel, margin, y, textPaint)
            canvas.drawLine(
                margin,
                y + 14f,
                margin + halfWidth,
                y + 14f,
                linePaint
            )

            val rightX = margin + halfWidth + gap

            canvas.drawText(rightLabel, rightX, y, textPaint)
            canvas.drawLine(
                rightX,
                y + 14f,
                rightX + halfWidth,
                y + 14f,
                linePaint
            )

            y += 27f
        }

        fun paragraph(text: String) {
            ensureSpace(25f)
            canvas.drawText(text, margin, y, smallPaint)
            y += 14f
        }

        drawHeader()

        paragraph(
            "Bitte alle Angaben leserlich eintragen. Nicht zutreffende Felder können gestrichen werden."
        )

        section("1. VERTRAGSPARTEIEN")
        blankField("Verkäufer/in – vollständiger Name")
        blankField("Straße, Hausnummer")
        twoFields("Postleitzahl / Ort", "Land")
        twoFields("Telefon / E-Mail", "Ausweisnummer (optional)")

        y += 4f

        blankField("Käufer/in – vollständiger Name")
        blankField("Straße, Hausnummer")
        twoFields("Postleitzahl / Ort", "Land")
        twoFields("Telefon / E-Mail", "Ausweisnummer (optional)")

        section("2. FAHRZEUGDATEN")
        twoFields("Fahrzeugart", "Hersteller / Marke")
        twoFields("Modell / Typ", "Erstzulassung / Baujahr")
        twoFields("Fahrzeug-Identifizierungsnummer (FIN)", "Kennzeichen")
        twoFields("Kilometerstand bei Übergabe", "Anzahl Vorbesitzer")
        twoFields("Antriebsart / Kraftstoff", "Getriebe")
        twoFields("Farbe", "Leistung / Hubraum")
        blankField("Mitverkauftes Zubehör")

        section("3. KAUFPREIS UND ZAHLUNG")
        twoFields("Kaufpreis gesamt", "Währung")
        twoFields("Anzahlung", "Restbetrag")
        blankField("Zahlungsart / Zahlungsvereinbarung")
        blankField("Fälligkeit / vereinbarter Zahlungstermin")

        section("4. ZUSTAND UND BESONDERE VEREINBARUNGEN")
        paragraph(
            "Fahrzeugzustand (z. B. gebraucht, fahrbereit, nicht fahrbereit):"
        )
        blankField("")
        paragraph("Bekannte Mängel / Schäden:")
        blankField("")
        blankField("")
        paragraph("Weitere Vereinbarungen:")
        blankField("")
        blankField("")

        section("5. ERKLÄRUNGEN DER VERTRAGSPARTEIEN")
        paragraph(
            "Die Vertragsparteien bestätigen, die Angaben und Vereinbarungen dieses Vertrags geprüft zu haben."
        )
        paragraph(
            "Zusätzliche Bedingungen, Gewährleistungsausschlüsse oder Garantien müssen ausdrücklich und rechtlich wirksam vereinbart werden."
        )

        section("6. ÜBERGABE")
        twoFields("Übergabedatum", "Uhrzeit")
        twoFields("Übergabeort", "Übergebene Schlüssel (Anzahl)")
        blankField("Übergebene Dokumente / Zubehör")
        twoFields("Kilometerstand bei Übergabe", "Tankfüllstand / Ladezustand")

        section("7. UNTERSCHRIFTEN")
        paragraph(
            "Mit ihrer Unterschrift bestätigen die Parteien den Abschluss des Vertrags."
        )
        y += 10f
        twoFields("Ort, Datum", "Ort, Datum")
        y += 16f
        twoFields("Unterschrift Verkäufer/in", "Unterschrift Käufer/in")
        y += 12f
        paragraph(
            "CARVITA – neutrale Vertragsvorlage. Die Vorlage ersetzt keine rechtliche Beratung."
        )

        drawFooter()
        document.finishPage(page)

        document.writeTo(output)
        return output.toByteArray()
    } finally {
        logo.recycle()
        document.close()
    }
}
