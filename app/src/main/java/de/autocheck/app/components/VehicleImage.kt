
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
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Composable
fun VehicleImage(
    uri: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val storage = remember(context) {
        SupabaseStorage(context)
    }

    var bitmap by remember(uri) {
        mutableStateOf<ImageBitmap?>(null)
    }

    LaunchedEffect(uri) {
        bitmap = null

        if (uri.isNotBlank()) {
            bitmap = try {
                loadVehicleImage(
                    context = context,
                    storage = storage,
                    imagePath = uri
                )
            } catch (_: Exception) {
                null
            }
        }
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
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF20242D)),
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

private suspend fun loadVehicleImage(
    context: Context,
    storage: SupabaseStorage,
    imagePath: String
): ImageBitmap? {

    val parsedUri = Uri.parse(imagePath)

    val localUri = when (parsedUri.scheme) {
        "content", "file" -> {
            parsedUri
        }

        else -> {
            val localFile = File(imagePath)

            if (localFile.exists() && localFile.isFile) {
                Uri.fromFile(localFile)
            } else {
                downloadVehicleImage(
                    storage = storage,
                    storagePath = imagePath
                )
            }
        }
    }

    return withContext(Dispatchers.IO) {
        try {
            when (localUri.scheme) {
                "content" -> {
                    context.contentResolver
                        .openInputStream(localUri)
                        ?.use { stream ->
                            BitmapFactory
                                .decodeStream(stream)
                                ?.asImageBitmap()
                        }
                }

                "file" -> {
                    val path = localUri.path
                        ?: return@withContext null

                    BitmapFactory
                        .decodeFile(path)
                        ?.asImageBitmap()
                }

                else -> null
            }
        } catch (_: Exception) {
            null
        }
    }
}

private suspend fun downloadVehicleImage(
    storage: SupabaseStorage,
    storagePath: String
): Uri = suspendCancellableCoroutine { continuation ->

    storage.download(
        storagePath,
        object : SupabaseStorage.DownloadCallback {

            override fun onSuccess(localUri: Uri) {
                if (continuation.isActive) {
                    continuation.resume(localUri)
                }
            }

            override fun onError(error: Exception) {
                if (continuation.isActive) {
                    continuation.resumeWithException(error)
                }
            }
        }
    )
}
