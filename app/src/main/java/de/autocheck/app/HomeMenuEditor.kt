package de.autocheck.app

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
            .padding(
                start = 18.dp,
                end = 18.dp,
                top = 48.dp,
                bottom = 24.dp
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "MENÜ ANPASSEN",
            color = Color(0xFFD4AF37),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Halte einen Menüpunkt gedrückt und ziehe ihn nach oben oder unten. Die ersten vier Plätze gehören zu Seite 2, die nächsten vier zu Seite 3.",
            color = Color.White,
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(14.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(
                items = items,
                key = { _, item -> item.id }
            ) { index, item ->

                if (index == 0 || index == 4) {
                    Text(
                        text = if (index == 0) "SEITE 2" else "SEITE 3",
                        color = Color(0xFFD4AF37),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(
                            top = 8.dp,
                            bottom = 2.dp
                        )
                    )
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .pointerInput(items.size) {
                            var currentIndex = index
                            var accumulatedDrag = 0f
                            val step = 72.dp.toPx()

                            detectDragGesturesAfterLongPress(
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    accumulatedDrag += dragAmount.y

                                    while (accumulatedDrag > step &&
                                        currentIndex < items.size - 1
                                    ) {
                                        onMove(
                                            currentIndex,
                                            currentIndex + 1
                                        )
                                        currentIndex++
                                        accumulatedDrag -= step
                                    }

                                    while (accumulatedDrag < -step &&
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
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF29251A)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${index + 1}.",
                            color = Color(0xFFD4AF37),
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = item.title,
                            color = Color.White,
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 12.dp),
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "↕",
                            color = Color(0xFFD4AF37),
                            fontSize = 22.sp
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                modifier = Modifier.weight(1f),
                onClick = onReset,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF29251A)
                )
            ) {
                Text("Zurücksetzen", color = Color(0xFFD4AF37))
            }

            Button(
                modifier = Modifier.weight(1f),
                onClick = onDone,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFD4AF37)
                )
            ) {
                Text("Fertig", color = Color.Black)
            }
        }
    }
}
