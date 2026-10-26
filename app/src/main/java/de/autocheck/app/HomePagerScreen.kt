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
                currentPage,
                showFuelScreen,
                showHistoryScreen,
                showPdfScreen,
                showPurchaseContractScreen,
                showPremiumDialog,
                selectedAction
            ) {
                var dragDistance = 0f

                detectHorizontalDragGestures(
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        dragDistance += dragAmount
                    },
                    onDragEnd = {
                        val overlayOpen =
                            showFuelScreen ||
                            showHistoryScreen ||
                            showPdfScreen ||
                            showPurchaseContractScreen ||
                            showPremiumDialog ||
                            selectedAction != null

                        if (!overlayOpen) {
                            if (dragDistance < -70f) {
                                currentPage =
                                    (currentPage + 1).coerceAtMost(2)
                            } else if (dragDistance > 70f) {
                                currentPage =
                                    (currentPage - 1).coerceAtLeast(0)
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
                        introEnabled = introEnabled,
                        onIntroEnabledChange = onIntroEnabledChange,
                        onSelect = onSelect
                    )
                }
            }

            // SEITE 2: Menübild 3 mit vier Platzhalter-Buttons
            currentPage == 1 -> {
                ThirdHomePageContent(
                    visible = visible,
                    onAction = { action ->
                        selectedAction = action
                    }
                )
            }

            // SEITE 3: Fahrzeugfunktionen im 2x2-Raster
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
                        start = 20.dp,
                        end = 20.dp,
                        top = 8.dp,
                        bottom = 8.dp
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentPage > 0) {
                        PageNavigationButton(
                            title = "ZURÜCK",
                            arrow = "‹",
                            modifier = Modifier.weight(1f),
                            onClick = {
                                currentPage =
                                    (currentPage - 1).coerceAtLeast(0)
                            }
                        )
                    }

                    if (currentPage < 2) {
                        PageNavigationButton(
                            title = "WEITERE FUNKTIONEN",
                            arrow = "›",
                            modifier = Modifier.weight(1f),
                            onClick = {
                                currentPage =
                                    (currentPage + 1).coerceAtMost(2)
                            }
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(3) { page ->
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
                        text = "Seite ${currentPage + 1} von 3",
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
private fun PageNavigationButton(
    title: String,
    arrow: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(38.dp)
            .clickable(onClick = onClick),
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
                .fillMaxSize()
                .padding(horizontal = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (arrow == "‹") {
                Text(
                    text = arrow,
                    color = CarvitaGold,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.width(5.dp))
            }

            Text(
                text = title,
                color = CarvitaGold,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            if (arrow == "›") {
                Spacer(Modifier.width(5.dp))

                Text(
                    text = arrow,
                    color = CarvitaGold,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
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
                top = 16.dp,
                bottom = 150.dp
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Spacer(
            modifier = Modifier.height(2.dp)
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
                        .height(200.dp),
                    contentScale = ContentScale.FillBounds
                )

                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 10.dp)
                        .fillMaxWidth(0.58f)
                        .height(38.dp)
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
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(
                            modifier = Modifier.width(6.dp)
                        )

                        Text(
                            text = "PREMIUM",
                            color = CarvitaGold,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Text(
            text = "DEINE FAHRZEUGE. ALLES IM BLICK.",
            color = CarvitaGold,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 2.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ThirdPageButton(
                modifier = Modifier.weight(1f),
                iconRes = R.drawable.tank_icoon,
                title = "Tanken & Verbrauch",
                contentDescription = "Tank-Icon",
                onClick = {
                    onAction("Tank und Kilometer – Verbrauch")
                }
            )

            ThirdPageButton(
                modifier = Modifier.weight(1f),
                iconRes = R.drawable.fahrzeughistorie_icoon,
                title = "Fahrzeughistorie",
                contentDescription = "Fahrzeughistorie-Icon",
                onClick = {
                    onAction("Fahrzeughistorie")
                }
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ThirdPageButton(
                modifier = Modifier.weight(1f),
                iconRes = R.drawable.pdf_icoon,
                title = "PDF & Dokumente",
                contentDescription = "PDF-Icon",
                onClick = {
                    onAction("PDF & Dokumente")
                }
            )

            ThirdPageButton(
                modifier = Modifier.weight(1f),
                iconRes = R.drawable.kaufvertrag_icoon,
                title = "CARVITA Kaufvertrag",
                contentDescription = "Kaufvertrag-Icon",
                onClick = {
                    onAction("Kaufvertrag")
                }
            )
        }

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
                    .fillMaxWidth(0.62f)
                    .height(46.dp),
                contentScale = ContentScale.Fit
            )
        }
    }
}

@Composable
private fun ThirdHomePageContent(
    visible: Boolean,
    onAction: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                start = 18.dp,
                end = 18.dp,
                top = 16.dp,
                bottom = 150.dp
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Spacer(
            modifier = Modifier.height(2.dp)
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
                    id = R.drawable.menuebild_seite3
                ),
                contentDescription = "CARVITA Menü Seite 3",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentScale = ContentScale.FillBounds
            )
        }

        Text(
            text = "WEITERE FAHRZEUGFUNKTIONEN",
            color = CarvitaGold,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 2.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ThirdPageButton(
                modifier = Modifier.weight(1f),
                iconRes = R.drawable.geplantevorhaben_icoon,
                title = "Geplante Vorhaben",
                contentDescription = "Geplante Vorhaben",
                onClick = {
                    onAction("Geplante Vorhaben")
                }
            )

            ThirdPageButton(
                modifier = Modifier.weight(1f),
                iconRes = R.drawable.tuningextras_icoon,
                title = "Tuning und Extras",
                contentDescription = "Tuning und Extras",
                onClick = {
                    onAction("Tuning und Extras")
                }
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ThirdPageButton(
                modifier = Modifier.weight(1f),
                iconRes = R.drawable.elektro_icoon,
                title = "Elektro und Verbrauch",
                contentDescription = "Elektro und Verbrauch",
                onClick = {
                    onAction("Elektro und Verbrauch")
                }
            )

            ThirdPageButton(
                modifier = Modifier.weight(1f),
                iconRes = R.drawable.kalender_icoon,
                title = "Kalender",
                contentDescription = "Kalender",
                onClick = {
                    onAction("Kalender")
                }
            )
        }

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
                    .fillMaxWidth(0.62f)
                    .height(46.dp),
                contentScale = ContentScale.Fit
            )
        }
    }
}

@Composable
private fun ThirdPageButton(
    modifier: Modifier = Modifier,
    iconRes: Int,
    title: String,
    contentDescription: String,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxSize()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xE6222222)
        ),
        border = BorderStroke(
            1.dp,
            CarvitaGold.copy(alpha = 0.8f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 5.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = contentDescription,
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Fit
            )

            Spacer(
                modifier = Modifier.height(7.dp)
            )

            Text(
                text = title,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(
                modifier = Modifier.height(3.dp)
            )

            Text(
                text = "Öffnen ›",
                color = CarvitaGold,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
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
