
package de.autocheck.app

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val CarvitaGold = Color(0xFFD4AF37)
private val CarvitaBackground = Color(0xFF101010)

@Composable
fun HomePagerScreen(
    visible: Boolean,
    visitedStore: VisitedStore,
    onSelect: (Screen) -> Unit
) {
    val context = LocalContext.current

    val vehicleStore = remember {
        VehicleStore(context)
    }

    var currentPage by remember {
        mutableStateOf(0)
    }

    var selectedAction by remember {
        mutableStateOf<String?>(null)
    }

    var showFuelScreen by remember {
        mutableStateOf(false)
    }

    var showHistoryScreen by remember {
        mutableStateOf(false)
    }

    var showPdfScreen by remember {
        mutableStateOf(false)
    }

    var showPurchaseContractScreen by remember {
        mutableStateOf(false)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CarvitaBackground)
            .pointerInput(
                showFuelScreen,
                showHistoryScreen,
                showPdfScreen,
                showPurchaseContractScreen
            ) {
                var dragDistance = 0f

                detectHorizontalDragGestures(
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        dragDistance += dragAmount
                    },
                    onDragEnd = {
                        if (
                            !showFuelScreen &&
                            !showHistoryScreen &&
                            !showPdfScreen &&
                            !showPurchaseContractScreen
                        ) {
                            if (dragDistance < -70f) {
                                currentPage = 1
                            } else if (dragDistance > 70f) {
                                currentPage = 0
                            }
                        }

                        dragDistance = 0f
                    },
                    onDragCancel = {
                        dragDistance = 0f
                    }
                )
            }
    ) {
        when {
            showFuelScreen -> {
                FuelConsumptionScreen(
                    onBack = {
                        showFuelScreen = false
                    }
                )
            }

            showHistoryScreen -> {
                val vehicles = remember {
                    vehicleStore.load()
                }

                VehicleHistoryScreen(
                    store = vehicleStore,
                    vehicles = vehicles,
                    activeVehicleId = vehicleStore.activeVehicleId(),
                    onBack = {
                        showHistoryScreen = false
                    }
                )
            }

            showPdfScreen -> {
                val vehicles = remember {
                    vehicleStore.load()
                }

                PdfDocumentsScreen(
                    store = vehicleStore,
                    vehicles = vehicles,
                    activeVehicleId = vehicleStore.activeVehicleId(),
                    onBack = {
                        showPdfScreen = false
                    }
                )
            }

            showPurchaseContractScreen -> {
                PurchaseContractScreen(
                    onBack = {
                        showPurchaseContractScreen = false
                    }
                )
            }

            currentPage == 0 -> {
                HomeScreen(
                    visible = visible,
                    visitedStore = visitedStore,
                    onSelect = onSelect
                )
            }

            else -> {
                SecondHomePageContent(
                    onAction = { action ->
                        when (action) {
                            "Tank und Kilometer – Verbrauch" -> {
                                showFuelScreen = true
                            }

                            "Fahrzeughistorie" -> {
                                showHistoryScreen = true
                            }

                            "PDF & Dokumente" -> {
                                showPdfScreen = true
                            }

                            "Kaufvertrag" -> {
                                showPurchaseContractScreen = true
                            }

                            else -> {
                                selectedAction = action
                            }
                        }
                    }
                )
            }
        }

        if (
            !showFuelScreen &&
            !showHistoryScreen &&
            !showPdfScreen &&
            !showPurchaseContractScreen
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(
                        start = 28.dp,
                        end = 28.dp,
                        bottom = 28.dp
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            currentPage =
                                if (currentPage == 0) 1 else 0
                        },
                    shape = RoundedCornerShape(50.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF29251A)
                    ),
                    border = BorderStroke(
                        1.dp,
                        CarvitaGold
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 2.dp
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(38.dp)
                            .padding(horizontal = 14.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (currentPage == 0) {
                                "WEITERE FUNKTIONEN"
                            } else {
                                "HAUPTFUNKTIONEN"
                            },
                            color = CarvitaGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(
                            modifier = Modifier.width(8.dp)
                        )

                        Text(
                            text = if (currentPage == 0) "›" else "‹",
                            color = CarvitaGold,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(2) { page ->
                        Box(
                            modifier = Modifier
                                .size(
                                    if (currentPage == page) 9.dp else 7.dp
                                )
                                .clip(CircleShape)
                                .background(
                                    if (currentPage == page) {
                                        CarvitaGold
                                    } else {
                                        Color.LightGray
                                    }
                                )
                                .clickable {
                                    currentPage = page
                                }
                        )
                    }

                    Text(
                        text = if (currentPage == 0) {
                            "Seite 1 von 2"
                        } else {
                            "Seite 2 von 2"
                        },
                        color = Color(0xFFBBBBBB),
                        fontSize = 10.sp
                    )
                }
            }
        }
    }

    selectedAction?.let { action ->
        AlertDialog(
            onDismissRequest = {
                selectedAction = null
            },
            title = {
                Text(action)
            },
            text = {
                Text(
                    "Dieser Bereich wird im nächsten Schritt " +
                        "mit seiner eigentlichen Funktion ausgestattet."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedAction = null
                    }
                ) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
private fun SecondHomePageContent(
    onAction: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                start = 18.dp,
                end = 18.dp,
                top = 8.dp,
                bottom = 100.dp
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Image(
            painter = painterResource(
                id = R.drawable.bild_4
            ),
            contentDescription = "CARVITA Logo",
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp),
            contentScale = ContentScale.Fit
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.Black
            )
        ) {
            Image(
                painter = painterResource(
                    id = R.drawable.menuebild_seite2
                ),
                contentDescription = "CARVITA Garagenbild",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                contentScale = ContentScale.Crop
            )
        }

        Text(
            text = "DEINE FAHRZEUGE. ALLES IM BLICK.",
            color = CarvitaGold,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 2.dp)
        )

        SecondPageButton(
            iconRes = R.drawable.tank_icoon,
            title = "Tank und Kilometer – Verbrauch",
            subtitle = "Tankvorgänge, Kilometer und Verbrauch",
            contentDescription = "Tank-Icon",
            onClick = {
                onAction("Tank und Kilometer – Verbrauch")
            }
        )

        SecondPageButton(
            iconRes = R.drawable.fahrzeughistorie_icoon,
            title = "Fahrzeughistorie",
            subtitle = "Alle gespeicherten Fahrzeugereignisse",
            contentDescription = "Fahrzeughistorie-Icon",
            onClick = {
                onAction("Fahrzeughistorie")
            }
        )

        SecondPageButton(
            iconRes = R.drawable.pdf_icoon,
            title = "PDF & Dokumente",
            subtitle = "Berichte auswählen, speichern und drucken",
            contentDescription = "PDF-Icon",
            onClick = {
                onAction("PDF & Dokumente")
            }
        )

        SecondPageButton(
            iconRes = R.drawable.kaufvertrag_icoon,
            title = "CARVITA Kaufvertrag",
            subtitle = "Leere Fahrzeugkaufvertrag-Vorlage als PDF",
            contentDescription = "Kaufvertrag-Icon",
            onClick = {
                onAction("Kaufvertrag")
            }
        )

        CarvitaFuturisticCar(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )
    }
}

@Composable
private fun CarvitaFuturisticCar(
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(
        label = "carvita_car_animation"
    )

    val scanPosition by transition.animateFloat(
        initialValue = 0.12f,
        targetValue = 0.88f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400),
            repeatMode = RepeatMode.Reverse
        ),
        label = "carvita_scan_position"
    )

    val glowPulse by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "carvita_glow_pulse"
    )

    val gold = CarvitaGold
    val blue = Color(0xFF29D9FF)
    val dimBlue = Color(0xFF14617A)
    val darkMetal = Color(0xFF151B20)

    Canvas(
        modifier = modifier
    ) {
        val w = size.width
        val h = size.height

        if (w <= 0f || h <= 0f) {
            return@Canvas
        }

        val centerX = w / 2f
        val centerY = h * 0.55f

        val carWidth = w * 0.70f
        val carHeight = h * 0.48f

        val left = centerX - carWidth / 2f
        val right = centerX + carWidth / 2f
        val top = centerY - carHeight * 0.38f
        val bottom = centerY + carHeight * 0.36f

        // Technische Grundlinie.
        drawLine(
            color = dimBlue.copy(alpha = 0.45f),
            start = Offset(
                w * 0.10f,
                bottom + carHeight * 0.30f
            ),
            end = Offset(
                w * 0.90f,
                bottom + carHeight * 0.30f
            ),
            strokeWidth = 1f
        )

        // Blaues Bodenlicht.
        drawLine(
            color = blue.copy(alpha = glowPulse * 0.55f),
            start = Offset(
                left + carWidth * 0.12f,
                bottom + carHeight * 0.16f
            ),
            end = Offset(
                right - carWidth * 0.12f,
                bottom + carHeight * 0.16f
            ),
            strokeWidth = 3f,
            cap = StrokeCap.Round
        )

        // Fahrzeugkarosserie.
        val body = Path().apply {
            moveTo(left + carWidth * 0.04f, centerY)
            lineTo(left + carWidth * 0.12f, centerY - carHeight * 0.08f)
            lineTo(left + carWidth * 0.27f, top + carHeight * 0.18f)
            quadraticTo(
                centerX - carWidth * 0.13f,
                top - carHeight * 0.03f,
                centerX,
                top
            )
            quadraticTo(
                centerX + carWidth * 0.16f,
                top + carHeight * 0.02f,
                right - carWidth * 0.20f,
                centerY - carHeight * 0.02f
            )
            lineTo(right - carWidth * 0.06f, centerY + carHeight * 0.06f)
            lineTo(right, centerY + carHeight * 0.23f)
            lineTo(right - carWidth * 0.04f, bottom)
            lineTo(left + carWidth * 0.05f, bottom)
            lineTo(left, centerY + carHeight * 0.20f)
            close()
        }

        drawPath(
            path = body,
            color = darkMetal.copy(alpha = 0.92f)
        )

        drawPath(
            path = body,
            color = gold.copy(alpha = 0.95f),
            style = Stroke(width = 1.8f)
        )

        // Windschutzscheibe.
        val windshield = Path().apply {
            moveTo(
                left + carWidth * 0.30f,
                centerY - carHeight * 0.06f
            )
            lineTo(
                centerX - carWidth * 0.12f,
                top + carHeight * 0.10f
            )
            quadraticTo(
                centerX,
                top + carHeight * 0.04f,
                centerX + carWidth * 0.10f,
                top + carHeight * 0.12f
            )
            lineTo(
                right - carWidth * 0.24f,
                centerY - carHeight * 0.03f
            )
            close()
        }

        drawPath(
            path = windshield,
            color = dimBlue.copy(alpha = 0.55f)
        )

        drawPath(
            path = windshield,
            color = blue.copy(alpha = 0.85f),
            style = Stroke(width = 1.3f)
        )

        // Linie an der Fahrzeugfront.
        drawLine(
            color = gold.copy(alpha = 0.8f),
            start = Offset(
                left + carWidth * 0.16f,
                centerY + carHeight * 0.12f
            ),
            end = Offset(
                right - carWidth * 0.12f,
                centerY + carHeight * 0.12f
            ),
            strokeWidth = 1.1f
        )

        // Linker Scheinwerfer.
        drawLine(
            color = blue.copy(alpha = glowPulse),
            start = Offset(
                left + carWidth * 0.07f,
                centerY + carHeight * 0.08f
            ),
            end = Offset(
                left + carWidth * 0.22f,
                centerY + carHeight * 0.05f
            ),
            strokeWidth = 3f,
            cap = StrokeCap.Round
        )

        // Rechter Scheinwerfer.
        drawLine(
            color = blue.copy(alpha = glowPulse),
            start = Offset(
                right - carWidth * 0.22f,
                centerY + carHeight * 0.05f
            ),
            end = Offset(
                right - carWidth * 0.07f,
                centerY + carHeight * 0.08f
            ),
            strokeWidth = 3f,
            cap = StrokeCap.Round
        )

        // Räder.
        val wheelRadius = carHeight * 0.18f
        val wheelY = bottom - carHeight * 0.01f

        listOf(
            left + carWidth * 0.25f,
            right - carWidth * 0.25f
        ).forEach { wheelX ->
            drawCircle(
                color = Color(0xFF080A0D),
                radius = wheelRadius,
                center = Offset(wheelX, wheelY)
            )

            drawCircle(
                color = gold,
                radius = wheelRadius,
                center = Offset(wheelX, wheelY),
                style = Stroke(width = 1.5f)
            )

            drawCircle(
                color = blue.copy(alpha = glowPulse),
                radius = wheelRadius * 0.42f,
                center = Offset(wheelX, wheelY),
                style = Stroke(width = 1.2f)
            )

            drawCircle(
                color = gold.copy(alpha = 0.9f),
                radius = wheelRadius * 0.12f,
                center = Offset(wheelX, wheelY)
            )
        }

        // Bewegter blauer Scan-Strahl.
        val scanX = w * scanPosition

        drawLine(
            color = blue.copy(alpha = 0.08f),
            start = Offset(scanX, h * 0.12f),
            end = Offset(scanX, h * 0.92f),
            strokeWidth = 14f
        )

        drawLine(
            color = blue.copy(alpha = glowPulse),
            start = Offset(scanX, h * 0.18f),
            end = Offset(scanX, h * 0.86f),
            strokeWidth = 1.2f
        )

        // Goldene Statuspunkte.
        val dotY = h * 0.12f

        listOf(0.43f, 0.50f, 0.57f).forEachIndexed { index, xFactor ->
            drawCircle(
                color = gold.copy(
                    alpha = if (index == 1) glowPulse else 0.45f
                ),
                radius = if (index == 1) 3f else 2f,
                center = Offset(
                    w * xFactor,
                    dotY
                )
            )
        }
    }
}

@Composable
private fun SecondPageButton(
    iconRes: Int,
    title: String,
    subtitle: String,
    contentDescription: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF222222)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 3.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = contentDescription,
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(9.dp)),
                contentScale = ContentScale.Fit
            )

            Spacer(
                modifier = Modifier.width(12.dp)
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = subtitle,
                    color = Color(0xFFBBBBBB),
                    fontSize = 11.sp
                )
            }

            Text(
                text = "›",
                color = CarvitaGold,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
