
package de.autocheck.app

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
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

    var showPremiumScreen by remember {
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
                showPremiumScreen
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
                            !showPremiumScreen
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
            showPremiumScreen -> {
                PremiumScreen(
                    onBack = {
                        showPremiumScreen = false
                    }
                )
            }

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
                    onPremiumClick = {
                        showPremiumScreen = true
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
            !showPurchaseContractScreen &&
            !showPremiumScreen
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(
                        CarvitaBackground.copy(alpha = 0.96f)
                    )
                    .padding(
                        start = 28.dp,
                        end = 28.dp,
                        top = 6.dp,
                        bottom = 44.dp
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(5.dp)
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
                            .height(34.dp)
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
    onPremiumClick: () -> Unit,
    onAction: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(
                start = 18.dp,
                end = 18.dp,
                top = 8.dp,
                bottom = 100.dp
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Image(
            painter = painterResource(
                id = R.drawable.bild_4
            ),
            contentDescription = "CARVITA Logo",
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            contentScale = ContentScale.Fit
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
                    contentDescription = "CARVITA Dashboard mit Lenkrad",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(205.dp),
                    contentScale = ContentScale.Fit
                )

                Button(
                    onClick = onPremiumClick,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 10.dp)
                        .fillMaxWidth(0.52f)
                        .height(39.dp),
                    shape = RoundedCornerShape(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF17130C),
                        contentColor = CarvitaGold
                    ),
                    border = BorderStroke(
                        1.dp,
                        CarvitaGold
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 5.dp
                    )
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "♛",
                            color = CarvitaGold,
                            fontSize = 17.sp
                        )

                        Spacer(
                            modifier = Modifier.width(7.dp)
                        )

                        Text(
                            text = "PREMIUM",
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
            .height(64.dp)
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
                    .size(40.dp)
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
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = subtitle,
                    color = Color(0xFFBBBBBB),
                    fontSize = 10.sp
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

@Composable
private fun PremiumScreen(
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CarvitaBackground)
            .verticalScroll(rememberScrollState())
            .padding(
                horizontal = 24.dp,
                vertical = 28.dp
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Text(
            text = "CARVITA",
            color = CarvitaGold,
            fontSize = 30.sp,
            fontWeight = FontWeight.ExtraBold
        )

        Text(
            text = "♛",
            color = CarvitaGold,
            fontSize = 58.sp
        )

        Text(
            text = "CARVITA PREMIUM",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Deine Fahrzeuggeschichte. Noch mehr Möglichkeiten.",
            color = CarvitaGold,
            fontSize = 15.sp,
            textAlign = TextAlign.Center
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1D1B16)
            ),
            border = BorderStroke(
                1.dp,
                CarvitaGold
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Deine Premium-Mitgliedschaft",
                    color = CarvitaGold,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                PremiumBenefit(
                    text = "Mehr Möglichkeiten für deine Fahrzeugverwaltung"
                )

                PremiumBenefit(
                    text = "Deine Fahrzeugdaten übersichtlich im Blick"
                )

                PremiumBenefit(
                    text = "Eine zentrale Anlaufstelle für deine Fahrzeugunterlagen"
                )
            }
        }

        Text(
            text = "Die Premium-Vorteile und Konditionen werden hier " +
                "vor dem Start des Mitgliedschaftsangebots festgelegt.",
            color = Color(0xFFBBBBBB),
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )

        Button(
            onClick = {
                // Der Kaufprozess wird ergänzt, sobald Preis und
                // Abrechnungsmodell festgelegt sind.
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = CarvitaGold,
                contentColor = Color.Black
            ),
            border = BorderStroke(
                1.dp,
                Color(0xFFF2D98D)
            )
        ) {
            Text(
                text = "PREMIUM MITGLIED WERDEN",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }

        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(50.dp),
            border = BorderStroke(
                1.dp,
                CarvitaGold
            )
        ) {
            Text(
                text = "Zurück",
                color = CarvitaGold
            )
        }
    }
}

@Composable
private fun PremiumBenefit(
    text: String
) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "✓",
            color = CarvitaGold,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = text,
            color = Color.White,
            fontSize = 14.sp
        )
    }
}
