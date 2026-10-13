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
    var currentPage by remember {
        mutableStateOf(0)
    }

    var selectedAction by remember {
        mutableStateOf<String?>(null)
    }

    var showFuelScreen by remember {
        mutableStateOf(false)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CarvitaBackground)
            .pointerInput(Unit) {
                var dragDistance = 0f

                detectHorizontalDragGestures(
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        dragDistance += dragAmount
                    },
                    onDragEnd = {
                        if (dragDistance < -70f) {
                            currentPage = 1
                        } else if (dragDistance > 70f) {
                            currentPage = 0
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
                        if (action == "Tank und Kilometer – Verbrauch") {
                            showFuelScreen = true
                        } else {
                            selectedAction = action
                        }
                    }
                )
            }
        }

        if (!showFuelScreen) {
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
                if (currentPage == 0) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                currentPage = 1
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
                                text = "WEITERE FUNKTIONEN",
                                color = CarvitaGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(
                                modifier = Modifier.width(8.dp)
                            )

                            Text(
                                text = "›",
                                color = CarvitaGold,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
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
                top = 12.dp,
                bottom = 36.dp
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        Image(
            painter = painterResource(
                id = R.drawable.bild_4
            ),
            contentDescription = "CARVITA Logo",
            modifier = Modifier
                .fillMaxWidth()
                .height(65.dp),
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
                    .height(170.dp),
                contentScale = ContentScale.Crop
            )
        }

        Text(
            text = "DEINE FAHRZEUGE. ALLES IM BLICK.",
            color = CarvitaGold,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        SecondPageButton(
            number = "1",
            title = "Tank und Kilometer – Verbrauch",
            subtitle = "Tankvorgänge, Kilometer und Verbrauch",
            onClick = {
                onAction("Tank und Kilometer – Verbrauch")
            }
        )

        SecondPageButton(
            number = "2",
            title = "Fahrzeughistorie",
            subtitle = "Alle gespeicherten Fahrzeugereignisse",
            onClick = {
                onAction("Fahrzeughistorie")
            }
        )

        SecondPageButton(
            number = "3",
            title = "PDF & Dokumente",
            subtitle = "Berichte auswählen, speichern und drucken",
            onClick = {
                onAction("PDF & Dokumente")
            }
        )

        SecondPageButton(
            number = "4",
            title = "Kaufvertrag",
            subtitle = "Originalvorlage und digitaler Vertrag",
            onClick = {
                onAction("Kaufvertrag")
            }
        )
    }
}

@Composable
private fun SecondPageButton(
    number: String,
    title: String,
    subtitle: String,
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
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(CarvitaGold),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = number,
                    color = Color.Black,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

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
