
package de.autocheck.app

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Fahrzeugbild-Anzeige für AutoCheck.
 *
 * Unterstützt:
 * - Dauerhaft gespeicherte lokale Bilder
 * - Content-URIs
 * - Bildanzeige nach erneutem Öffnen der App
 * - Platzhalter bei fehlenden Bildern
 */

@Composable
fun VehicleImage(
    uri: String,
    modifier: Modifier = Modifier
) {

    val context = LocalContext.current

    var bitmap by remember(uri) {
        mutableStateOf<ImageBitmap?>(null)
    }

    var loading by remember(uri) {
        mutableStateOf(true)
    }

    LaunchedEffect(uri) {

        loading = true
        bitmap = null

        bitmap = withContext(Dispatchers.IO) {

            if (uri.isBlank()) {
                null
            } else {
                try {

                    val parsedUri = Uri.parse(uri)

                    when (parsedUri.scheme) {

                        "file" -> {
                            BitmapFactory.decodeFile(
                                parsedUri.path
                            )?.asImageBitmap()
                        }

                        "content" -> {
                            context.contentResolver
                                .openInputStream(parsedUri)
                                ?.use { stream ->
                                    BitmapFactory
                                        .decodeStream(stream)
                                        ?.asImageBitmap()
                                }
                        }

                        else -> {

                            val file = File(uri)

                            if (file.exists()) {
                                BitmapFactory.decodeFile(
                                    file.absolutePath
                                )?.asImageBitmap()
                            } else {
                                null
                            }
                        }
                    }

                } catch (e: Exception) {
                    null
                }
            }
        }

        loading = false
    }

    if (bitmap != null) {

        Image(
            bitmap = bitmap!!,
            contentDescription = "Fahrzeugbild",
            modifier = modifier.clip(
                RoundedCornerShape(12.dp)
            ),
            contentScale = ContentScale.Crop
        )

    } else {

        Box(
            modifier = modifier
                .clip(
                    RoundedCornerShape(12.dp)
                )
                .background(
                    Color(0xFF20242D)
                ),
            contentAlignment = Alignment.Center
        ) {

            Icon(
                imageVector = Icons.Filled.DirectionsCar,
                contentDescription = "Kein Fahrzeugbild",
                tint = Color.White,
                modifier = Modifier.size(36.dp)
            )
        }
    }
}
