
package de.autocheck.app

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.ByteArrayOutputStream

private val ContractGold = Color(0xFFD4AF37)
private val ContractBackground = Color(0xFF101010)
private val ContractPanel = Color(0xFF222222)

private data class ContractField(
    val label: String,
    val key: String,
    val singleLine: Boolean = true
)

private data class ContractFieldRow(
    val left: ContractField,
    val right: ContractField? = null
)

private val contractSections: List<Pair<String, List<ContractFieldRow>>> = listOf(
    "1. VERTRAGSPARTEIEN" to listOf(
        ContractFieldRow(ContractField("Verkäufer/in – vollständiger Name", "sellerName")),
        ContractFieldRow(ContractField("Straße, Hausnummer", "sellerAddress")),
        ContractFieldRow(
            ContractField("Postleitzahl / Ort", "sellerCity"),
            ContractField("Land", "sellerCountry")
        ),
        ContractFieldRow(
            ContractField("Telefon / E-Mail", "sellerContact"),
            ContractField("Ausweisnummer (optional)", "sellerId")
        ),
        ContractFieldRow(ContractField("Käufer/in – vollständiger Name", "buyerName")),
        ContractFieldRow(ContractField("Straße, Hausnummer", "buyerAddress")),
        ContractFieldRow(
            ContractField("Postleitzahl / Ort", "buyerCity"),
            ContractField("Land", "buyerCountry")
        ),
        ContractFieldRow(
            ContractField("Telefon / E-Mail", "buyerContact"),
            ContractField("Ausweisnummer (optional)", "buyerId")
        )
    ),
    "2. FAHRZEUGDATEN" to listOf(
        ContractFieldRow(
            ContractField("Fahrzeugart", "vehicleType"),
            ContractField("Hersteller / Marke", "vehicleMake")
        ),
        ContractFieldRow(
            ContractField("Modell / Typ", "vehicleModel"),
            ContractField("Erstzulassung / Baujahr", "vehicleYear")
        ),
        ContractFieldRow(
            ContractField("Fahrzeug-Identifizierungsnummer (FIN)", "vehicleVin"),
            ContractField("Kennzeichen", "vehiclePlate")
        ),
        ContractFieldRow(
            ContractField("Kilometerstand bei Übergabe", "vehicleMileage"),
            ContractField("Anzahl Vorbesitzer", "vehicleOwners")
        ),
        ContractFieldRow(
            ContractField("Antriebsart / Kraftstoff", "vehicleFuel"),
            ContractField("Getriebe", "vehicleTransmission")
        ),
        ContractFieldRow(
            ContractField("Farbe", "vehicleColor"),
            ContractField("Leistung / Hubraum", "vehiclePower")
        ),
        ContractFieldRow(ContractField("Mitverkauftes Zubehör", "vehicleAccessories"))
    ),
    "3. KAUFPREIS UND ZAHLUNG" to listOf(
        ContractFieldRow(
            ContractField("Kaufpreis gesamt", "purchasePrice"),
            ContractField("Währung", "currency")
        ),
        ContractFieldRow(
            ContractField("Anzahlung", "deposit"),
            ContractField("Restbetrag", "remainingAmount")
        ),
        ContractFieldRow(ContractField("Zahlungsart / Zahlungsvereinbarung", "paymentMethod")),
        ContractFieldRow(ContractField("Fälligkeit / vereinbarter Zahlungstermin", "paymentDue"))
    ),
    "4. ZUSTAND UND BESONDERE VEREINBARUNGEN" to listOf(
        ContractFieldRow(
            ContractField(
                "Fahrzeugzustand (z. B. gebraucht, fahrbereit, nicht fahrbereit)",
                "vehicleCondition"
            )
        ),
        ContractFieldRow(
            ContractField("Bekannte Mängel / Schäden", "knownDefects", false)
        ),
        ContractFieldRow(
            ContractField("Weitere Vereinbarungen", "otherAgreements", false)
        )
    ),
    "5. ERKLÄRUNGEN DER VERTRAGSPARTEIEN" to listOf(
        ContractFieldRow(
            ContractField("Zusätzliche Erklärungen", "declarations", false)
        )
    ),
    "6. ÜBERGABE" to listOf(
        ContractFieldRow(
            ContractField("Übergabedatum", "handoverDate"),
            ContractField("Uhrzeit", "handoverTime")
        ),
        ContractFieldRow(
            ContractField("Übergabeort", "handoverPlace"),
            ContractField("Übergebene Schlüssel (Anzahl)", "keyCount")
        ),
        ContractFieldRow(
            ContractField("Übergebene Dokumente / Zubehör", "handoverDocuments")
        ),
        ContractFieldRow(
            ContractField("Kilometerstand bei Übergabe", "handoverMileage"),
            ContractField("Tankfüllstand / Ladezustand", "fuelLevel")
        )
    ),
    "7. UNTERSCHRIFTEN" to listOf(
        ContractFieldRow(
            ContractField("Ort, Datum Verkäufer/in", "sellerSignaturePlace")
        ),
        ContractFieldRow(
            ContractField("Ort, Datum Käufer/in", "buyerSignaturePlace")
        )
    )
)

@Composable
fun PurchaseContractScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current

    var statusMessage by remember {
        mutableStateOf("Der CARVITA-Kaufvertrag ist bereit.")
    }

    var isFillingForm by remember {
        mutableStateOf(false)
    }

    var contractValues by remember {
        mutableStateOf<Map<String, String>>(emptyMap())
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
            },
            onBackToMenu = {
                previewPdfBytes = null
                onBack()
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
                start = 16.dp,
                end = 16.dp,
                top = 12.dp,
                bottom = 8.dp
            ),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = 4.dp,
                    bottom = 12.dp
                ),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Image(
                        painter = painterResource(id = R.drawable.bild_4),
                        contentDescription = "CARVITA-Logo",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(72.dp),
                        contentScale = ContentScale.Fit
                    )

                    Text(
                        text = if (isFillingForm) {
                            "Kaufvertrag ausfüllen"
                        } else {
                            "Fahrzeugkaufvertrag"
                        },
                        modifier = Modifier.padding(top = 4.dp),
                        color = Color.White,
                        fontSize = 23.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = if (isFillingForm) {
                            "Trage die Angaben ein. Leere Felder bleiben im Vertrag frei."
                        } else {
                            "Erstelle eine leere Vorlage oder fülle den Vertrag direkt in der App aus."
                        },
                        modifier = Modifier.padding(top = 4.dp),
                        color = Color.LightGray,
                        fontSize = 13.sp
                    )
                }

                if (!isFillingForm) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = ContractPanel
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Inhalt des Vertrags",
                                    color = ContractGold,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )

                                Text("• Verkäufer- und Käuferdaten", color = Color.White)
                                Text("• Fahrzeugdaten und FIN", color = Color.White)
                                Text("• Kaufpreis und Zahlungsvereinbarung", color = Color.White)
                                Text("• Zustand, Mängel und Vereinbarungen", color = Color.White)
                                Text("• Übergabe und Unterschriften", color = Color.White)
                            }
                        }
                    }

                    item {
                        Button(
                            onClick = {
                                isFillingForm = true
                                statusMessage = "Du kannst jetzt die Vertragsdaten eingeben."
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ContractGold,
                                contentColor = Color.Black
                            )
                        ) {
                            Text(
                                text = "Kaufvertrag ausfüllen",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    item {
                        OutlinedButton(
                            onClick = {
                                try {
                                    previewPdfBytes =
                                        createVehiclePurchaseContract(
                                            context,
                                            emptyMap()
                                        )
                                    statusMessage = "Die leere Vertragsvorlage wurde erstellt."
                                } catch (error: Exception) {
                                    statusMessage =
                                        "PDF-Erstellung fehlgeschlagen: ${
                                            error.localizedMessage
                                                ?: "Unbekannter Fehler"
                                        }"
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Leere Vorlage ansehen")
                        }
                    }
                } else {
                    contractSections.forEach { (sectionTitle, rows) ->
                        item(key = sectionTitle) {
                            Text(
                                text = sectionTitle,
                                modifier = Modifier.padding(top = 6.dp),
                                color = ContractGold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }

                        rows.forEach { row ->
                            item(key = row.left.key) {
                                if (row.right == null) {
                                    ContractInputField(
                                        field = row.left,
                                        value = contractValues[row.left.key].orEmpty(),
                                        onValueChange = { newValue ->
                                            contractValues = contractValues +
                                                (row.left.key to newValue)
                                        }
                                    )
                                } else {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        BoxedContractInput(
                                            field = row.left,
                                            value = contractValues[row.left.key].orEmpty(),
                                            modifier = Modifier.weight(1f),
                                            onValueChange = { newValue ->
                                                contractValues = contractValues +
                                                    (row.left.key to newValue)
                                            }
                                        )

                                        BoxedContractInput(
                                            field = row.right,
                                            value = contractValues[row.right.key].orEmpty(),
                                            modifier = Modifier.weight(1f),
                                            onValueChange = { newValue ->
                                                contractValues = contractValues +
                                                    (row.right.key to newValue)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = ContractPanel
                            )
                        ) {
                            Text(
                                text = "Hinweis: Unterschriften können nach dem Drucken handschriftlich eingetragen werden. Die eingegebenen Angaben werden in die PDF übernommen.",
                                modifier = Modifier.padding(12.dp),
                                color = Color.White,
                                fontSize = 12.sp
                            )
                        }
                    }

                    item {
                        Button(
                            onClick = {
                                try {
                                    previewPdfBytes =
                                        createVehiclePurchaseContract(
                                            context,
                                            contractValues
                                        )
                                    statusMessage =
                                        "Die ausgefüllte PDF wurde erstellt."
                                } catch (error: Exception) {
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
                                text = "PDF-Vorschau öffnen",
                                fontWeight = FontWeight.Bold
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                isFillingForm = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Zurück zur Vertragsauswahl")
                        }
                    }
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
                            modifier = Modifier.padding(12.dp),
                            color = Color.White,
                            fontSize = 12.sp
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
private fun ContractInputField(
    field: ContractField,
    value: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(field.label)
        },
        modifier = Modifier.fillMaxWidth(),
        singleLine = field.singleLine,
        minLines = if (field.singleLine) 1 else 3,
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Sentences
        )
    )
}

@Composable
private fun BoxedContractInput(
    field: ContractField,
    value: String,
    modifier: Modifier = Modifier,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(field.label, fontSize = 11.sp)
        },
        modifier = modifier,
        singleLine = field.singleLine,
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Sentences
        )
    )
}

private fun createVehiclePurchaseContract(
    context: Context,
    values: Map<String, String>
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

        var page = document.startPage(
            PdfDocument.PageInfo.Builder(
                pageWidth,
                pageHeight,
                pageNumber
            ).create()
        )

        var canvas: Canvas = page.canvas
        var y = 38f

        fun drawHeader() {
            val logoRect = Rect(0, 0, logo.width, logo.height)

            val scale = minOf(
                125f / logo.width,
                42f / logo.height
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

            canvas.drawLine(
                margin,
                footerY - 14f,
                pageWidth - margin,
                footerY - 14f,
                linePaint
            )

            canvas.drawText(
                "CARVITA – Fahrzeugkaufvertrag",
                margin,
                footerY,
                footerPaint
            )

            val pageText = "Seite $pageNumber"
            val width = footerPaint.measureText(pageText)

            canvas.drawText(
                pageText,
                pageWidth - margin - width,
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

        fun wrappedText(
            text: String,
            x: Float,
            startY: Float,
            maxWidth: Float,
            paint: Paint
        ): Float {
            if (text.isBlank()) return startY

            var currentY = startY
            var currentLine = ""

            text.split(Regex("\\s+")).forEach { word ->
                val candidate = if (currentLine.isEmpty()) {
                    word
                } else {
                    "$currentLine $word"
                }

                if (paint.measureText(candidate) > maxWidth &&
                    currentLine.isNotEmpty()
                ) {
                    ensureSpace(13f)
                    canvas.drawText(currentLine, x, currentY, paint)
                    currentY += 12f
                    currentLine = word
                } else {
                    currentLine = candidate
                }
            }

            if (currentLine.isNotEmpty()) {
                ensureSpace(13f)
                canvas.drawText(currentLine, x, currentY, paint)
                currentY += 12f
            }

            return currentY
        }

        fun field(
            label: String,
            key: String,
            width: Float = contentWidth,
            multiline: Boolean = false
        ) {
            val value = values[key].orEmpty().trim()
            val estimatedHeight = if (multiline) 52f else 26f

            ensureSpace(estimatedHeight)

            canvas.drawText(label, margin, y, textPaint)

            if (value.isNotEmpty()) {
                val valueY = y + 13f
                val nextY = wrappedText(
                    text = value,
                    x = margin,
                    startY = valueY,
                    maxWidth = width,
                    paint = smallPaint
                )

                val lineY = maxOf(y + 18f, nextY + 1f)

                canvas.drawLine(
                    margin,
                    lineY,
                    margin + width,
                    lineY,
                    linePaint
                )

                y = lineY + 9f
            } else {
                canvas.drawLine(
                    margin,
                    y + 14f,
                    margin + width,
                    y + 14f,
                    linePaint
                )

                y += if (multiline) 42f else 25f
            }
        }

        fun twoFields(
            leftLabel: String,
            leftKey: String,
            rightLabel: String,
            rightKey: String
        ) {
            val gap = 18f
            val halfWidth = (contentWidth - gap) / 2f
            val leftValue = values[leftKey].orEmpty().trim()
            val rightValue = values[rightKey].orEmpty().trim()

            ensureSpace(32f)

            canvas.drawText(leftLabel, margin, y, textPaint)
            canvas.drawText(
                rightLabel,
                margin + halfWidth + gap,
                y,
                textPaint
            )

            if (leftValue.isNotEmpty()) {
                canvas.drawText(
                    leftValue.take(48),
                    margin,
                    y + 12f,
                    smallPaint
                )
            }

            if (rightValue.isNotEmpty()) {
                canvas.drawText(
                    rightValue.take(48),
                    margin + halfWidth + gap,
                    y + 12f,
                    smallPaint
                )
            }

            canvas.drawLine(
                margin,
                y + 15f,
                margin + halfWidth,
                y + 15f,
                linePaint
            )

            canvas.drawLine(
                margin + halfWidth + gap,
                y + 15f,
                pageWidth - margin,
                y + 15f,
                linePaint
            )

            y += 27f
        }

        fun paragraph(text: String) {
            y = wrappedText(
                text = text,
                x = margin,
                startY = y,
                maxWidth = contentWidth,
                paint = smallPaint
            )
            y += 2f
        }

        drawHeader()

        paragraph(
            "Bitte alle Angaben leserlich eintragen. Nicht zutreffende Felder können gestrichen werden."
        )

        contractSections.forEach { (sectionTitle, rows) ->
            section(sectionTitle)

            rows.forEach { row ->
                val left = row.left
                val right = row.right

                if (right == null) {
                    field(
                        label = left.label,
                        key = left.key,
                        multiline = !left.singleLine
                    )
                } else {
                    twoFields(
                        left.label,
                        left.key,
                        right.label,
                        right.key
                    )
                }
            }

            if (sectionTitle.startsWith("5.")) {
                paragraph(
                    "Zusätzliche Bedingungen, Gewährleistungsausschlüsse oder Garantien müssen ausdrücklich und rechtlich wirksam vereinbart werden."
                )
            }

            if (sectionTitle.startsWith("7.")) {
                paragraph(
                    "Unterschriften bitte nach dem Ausdrucken handschriftlich ergänzen."
                )
                y += 12f

                twoFields(
                    "Unterschrift Verkäufer/in",
                    "sellerSignature",
                    "Unterschrift Käufer/in",
                    "buyerSignature"
                )
            }
        }

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
