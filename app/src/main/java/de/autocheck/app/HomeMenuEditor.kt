package de.autocheck.app

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.statusBarsPadding

@Composable
fun HomeMenuEditor(
    items: List<HomeMenuItem>,
    onMove: (Int, Int) -> Unit,
    onReset: () -> Unit,
    onDone: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(
                start = 16.dp,
                end = 16.dp,
                top = 8.dp,
                bottom = 8.dp
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "MENÜ ANPASSEN",
            color = Color(0xFFD4AF37),
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(5.dp))

        Text(
            text = "Halte einen Menüpunkt gedrückt und ziehe ihn an die gewünschte Stelle. Die ersten vier Plätze gehören zu Seite 2, die nächsten vier zu Seite 3.",
            color = Color.White,
            fontSize = 11.sp,
            lineHeight = 15.sp,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(7.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            itemsIndexed(
                items = items,
                key = { _, item -> item.id }
            ) { index, item ->

                if (index == 0 || index == 4) {
                    Text(
                        text = if (index == 0) "SEITE 2" else "SEITE 3",
                        color = Color(0xFFD4AF37),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(
                            top = 4.dp,
                            bottom = 1.dp
                        )
                    )
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .pointerInput(item.id, items.size) {
                            var currentIndex = index
                            var accumulatedDrag = 0f
                            val step = with(LocalDensity.current) {
                                54.dp.toPx()
                            }

                            detectDragGesturesAfterLongPress(
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    accumulatedDrag += dragAmount.y

                                    while (
                                        accumulatedDrag > step &&
                                        currentIndex < items.size - 1
                                    ) {
                                        onMove(
                                            currentIndex,
                                            currentIndex + 1
                                        )
                                        currentIndex++
                                        accumulatedDrag -= step
                                    }

                                    while (
                                        accumulatedDrag < -step &&
                                        currentIndex > 0
                                    ) {
                                        onMove(
                                            currentIndex,
                                            currentIndex - 1
                                        )
                                        currentIndex--
                                        accumulatedDrag += step
                                    }
                                },
                                onDragEnd = {
                                    accumulatedDrag = 0f
                                },
                                onDragCancel = {
                                    accumulatedDrag = 0f
                                }
                            )
                        },
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF29251A)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${index + 1}.",
                            color = Color(0xFFD4AF37),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = item.title,
                            color = Color.White,
                            fontSize = 13.sp,
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 10.dp),
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "↕",
                            color = Color(0xFFD4AF37),
                            fontSize = 20.sp
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(7.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp),
                onClick = onReset,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF29251A)
                ),
                shape = RoundedCornerShape(24.dp)
            ) {
                Text(
                    "Zurücksetzen",
                    color = Color(0xFFD4AF37),
                    fontSize = 12.sp
                )
            }

            Button(
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp),
                onClick = onDone,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFD4AF37)
                ),
                shape = RoundedCornerShape(24.dp)
            ) {
                Text(
                    "Fertig",
                    color = Color.Black,
                    fontSize = 12.sp
                )
            }
        }
    }
}
