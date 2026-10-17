package de.autocheck.app

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.ui.graphics.Color
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
    introEnabled: Boolean,
    onIntroEnabledChange: (Boolean) -> Unit,
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

    var showPremiumDialog by remember {
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
                showPurchaseContractScreen,
                showPremiumDialog
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
                            !showPurchaseContractScreen &&
                            !showPremiumDialog
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
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 42.dp)
                ) {
                    HomeScreen(
                        visible = visible,
                        visitedStore = visitedStore,
                        onSelect = onSelect
                    )
                }
            }

            else -> {
                SecondHomePageContent(
                    visible = visible,
                    onPremiumClick = {
                        showPremiumDialog = true
                    },
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

        // Kreisrunder Intro-Button auf Seite 1.
        if (
            currentPage == 0 &&
            !showFuelScreen &&
            !showHistoryScreen &&
            !showPdfScreen &&
            !showPurchaseContractScreen &&
            !showPremiumDialog
        ) {
            Card(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(
                        start = 22.dp,
                        top = 88.dp
                    )
                    .size(52.dp)
                    .clickable {
                        onIntroEnabledChange(!introEnabled)
                    },
                shape = CircleShape,
                colors = CardDefaults.cardColors(
                    containerColor = if (introEnabled) {
                        Color(0xFF29251A)
                    } else {
                        Color(0xFF202020)
                    }
                ),
                border = BorderStroke(
                    1.5.dp,
                    CarvitaGold
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 3.dp
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "▶",
                            color = CarvitaGold,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = if (introEnabled) "AN" else "AUS",
                            color = CarvitaGold,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
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
                    .background(CarvitaBackground)
                    .navigationBarsPadding()
                    .padding(
                        start = 28.dp,
                        end = 28.dp,
                        top = 8.dp,
                        bottom = 8.dp
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

    if (showPremiumDialog) {
        AlertDialog(
            onDismissRequest = {
                showPremiumDialog = false
            },
            title = {
                Text(
                    text = "CARVITA Premium",
                    color = CarvitaGold
                )
            },
            text = {
                Text(
                    "Werde CARVITA-Premiummitglied und entdecke " +
                        "zusätzliche Funktionen. Die Mitgliedschaft " +
                        "und Bezahlung müssen noch eingerichtet werden."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showPremiumDialog = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showPremiumDialog = false
                    }
                ) {
                    Text("Schließen")
                }
            }
        )
    }
}

@Composable
private fun SecondHomePageContent(
    visible: Boolean,
    onPremiumClick: () -> Unit,
    onAction: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                start = 18.dp,
                end = 18.dp,
                top = 24.dp,
                bottom = 110.dp
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Spacer(
            modifier = Modifier.height(8.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.Black
            )
        ) {
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                Image(
                    painter = painterResource(
                        id = R.drawable.menuebild_seite2
                    ),
                    contentDescription = "CARVITA Dashboard und Lenkrad",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(230.dp),
                    contentScale = ContentScale.FillBounds
                )

                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 12.dp)
                        .fillMaxWidth(0.58f)
                        .height(42.dp)
                        .clickable(onClick = onPremiumClick),
                    shape = RoundedCornerShape(50.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF17120A)
                    ),
                    border = BorderStroke(
                        1.5.dp,
                        CarvitaGold
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 4.dp
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "♛",
                            color = CarvitaGold,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(
                            modifier = Modifier.width(6.dp)
                        )

                        Text(
                            text = "PREMIUM",
                            color = CarvitaGold,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Text(
            text = "DEINE FAHRZEUGE. ALLES IM BLICK.",
            color = CarvitaGold,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 4.dp)
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

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        AnimatedVisibility(
            visible = visible,
            enter = slideInHorizontally(
                animationSpec = tween(2000),
                initialOffsetX = { fullWidth -> -fullWidth }
            )
        ) {
            Image(
                painter = painterResource(
                    id = R.drawable.bild_4
                ),
                contentDescription = "CARVITA Logo",
                modifier = Modifier
                    .fillMaxWidth(0.52f)
                    .height(58.dp),
                contentScale = ContentScale.Fit
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
