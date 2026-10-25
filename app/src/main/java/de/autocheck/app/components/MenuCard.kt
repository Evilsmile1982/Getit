
package de.autocheck.app

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private data class CarvitaIconCrop(
    val column: Int,
    val row: Int
)

private fun getCarvitaIconCrop(title: String): CarvitaIconCrop {
    return when (title) {
        "Mein Auto" -> CarvitaIconCrop(0, 0)
        "Reparaturen" -> CarvitaIconCrop(1, 0)
        "Pickerl/TÜV" -> CarvitaIconCrop(2, 0)
        "Wartungen" -> CarvitaIconCrop(0, 1)
        "Service und Intervalle" -> CarvitaIconCrop(1, 1)
        "Reifen" -> CarvitaIconCrop(2, 1)
        else -> CarvitaIconCrop(0, 0)
    }
}

@Composable
private fun CarvitaSheetIcon(
    title: String,
    modifier: Modifier = Modifier
) {
    val bitmap: ImageBitmap =
        ImageBitmap.imageResource(R.drawable.bild_5)

    val crop = remember(title, bitmap.width, bitmap.height) {
        getCarvitaIconCrop(title)
    }

    val cellWidth = bitmap.width / 3
    val cellHeight = bitmap.height / 2

    // Bildausschnitt innerhalb der jeweiligen Kachel.
    val left = crop.column * cellWidth + cellWidth / 30
    val top = crop.row * cellHeight + cellHeight / 30
    val right = (crop.column + 1) * cellWidth - cellWidth / 30
    val bottom = (crop.row + 1) * cellHeight - cellHeight / 4

    val sourceWidth = (right - left).coerceAtLeast(1)
    val sourceHeight = (bottom - top).coerceAtLeast(1)

    Canvas(modifier = modifier) {
        drawImage(
            image = bitmap,
            srcOffset = IntOffset(left, top),
            srcSize = IntSize(sourceWidth, sourceHeight),
            dstOffset = IntOffset.Zero,
            dstSize = IntSize(
                size.width.toInt().coerceAtLeast(1),
                size.height.toInt().coerceAtLeast(1)
            ),
            filterQuality = FilterQuality.High
        )
    }
}

@Composable
fun MenuCard(
    item: MenuItemData,
    visited: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(104.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(17.dp),
        colors = CardDefaults.cardColors(
            containerColor = Surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 5.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            item.accent.copy(alpha = 0.20f),
                            Color.Transparent
                        )
                    )
                )
                .padding(11.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(item.accent.copy(alpha = 0.90f)),
                contentAlignment = Alignment.Center
            ) {
                CarvitaSheetIcon(
                    title = item.title,
                    modifier = Modifier.size(29.dp)
                )
            }

            Spacer(
                modifier = Modifier.width(10.dp)
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.title,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = item.subtitle,
                    color = Muted,
                    fontSize = 10.sp,
                    lineHeight = 12.sp,
                    maxLines = 2
                )

                if (visited) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = item.accent,
                            modifier = Modifier.size(13.dp)
                        )

                        Spacer(
                            modifier = Modifier.width(3.dp)
                        )

                        Text(
                            text = "Ich war hier",
                            color = item.accent,
                            fontSize = 9.sp
                        )
                    }
                }
            }

            Icon(
                imageVector = Icons.Filled.KeyboardArrowRight,
                contentDescription = "Öffnen",
                tint = item.accent,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
