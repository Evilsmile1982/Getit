
package de.autocheck.app

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.print.PrintManager
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PageRange
import android.os.CancellationSignal
import android.os.Bundle
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

@Composable
fun PdfZoomPreview(
    pdfBytes: ByteArray,
    documentName: String,
    onClose: () -> Unit,
    onPrint: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var currentPage by remember { mutableIntStateOf(0) }
    var pageCount by remember { mutableIntStateOf(0) }
    var pageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var loading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var zoom by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    var pdfFile by remember { mutableStateOf<File?>(null) }

    val saveLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                try {
                    withContext(Dispatchers.IO) {
                        context.contentResolver.openOutputStream(uri)?.use { output ->
                            output.write(pdfBytes)
                            output.flush()
                        } ?: throw IOException(
                            "Die PDF-Datei konnte nicht gespeichert werden."
                        )
                    }
                } catch (e: Exception) {
                    errorMessage =
                        "Speichern fehlgeschlagen: " +
                        (e.localizedMessage ?: "Unbekannter Fehler")
                }
            }
        }
    }

    LaunchedEffect(pdfBytes) {
        loading = true
        errorMessage = null

        try {
            val file = withContext(Dispatchers.IO) {
                File.createTempFile(
                    "pdf_preview_",
                    ".pdf",
                    context.cacheDir
                ).also {
                    it.writeBytes(pdfBytes)
                }
            }

            pdfFile?.delete()
            pdfFile = file

            val count = withContext(Dispatchers.IO) {
                val descriptor = ParcelFileDescriptor.open(
                    file,
                    ParcelFileDescriptor.MODE_READ_ONLY
                )

                PdfRenderer(descriptor).use { renderer ->
                    renderer.pageCount
                }
            }

            pageCount = count
            currentPage = 0
        } catch (e: Exception) {
            errorMessage =
                "PDF konnte nicht geöffnet werden: " +
                (e.localizedMessage ?: "Unbekannter Fehler")
        } finally {
            loading = false
        }
    }

    LaunchedEffect(pdfFile, currentPage) {
        val file = pdfFile ?: return@LaunchedEffect

        loading = true
        errorMessage = null

        try {
            val bitmap = withContext(Dispatchers.IO) {
                val descriptor = ParcelFileDescriptor.open(
                    file,
                    ParcelFileDescriptor.MODE_READ_ONLY
                )

                PdfRenderer(descriptor).use { renderer ->
                    if (currentPage !in 0 until renderer.pageCount) {
                        throw IOException(
                            "Diese PDF-Seite ist nicht verfügbar."
                        )
                    }

                    renderer.openPage(currentPage).use { page ->
                        val scale = 2

                        val bitmap = Bitmap.createBitmap(
                            (page.width * scale).coerceAtLeast(1),
                            (page.height * scale).coerceAtLeast(1),
                            Bitmap.Config.ARGB_8888
                        )

                        bitmap.eraseColor(android.graphics.Color.WHITE)

                        page.render(
                            bitmap,
                            null,
                            null,
                            PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY
                        )

                        bitmap
                    }
                }
            }

            pageBitmap?.recycle()
            pageBitmap = bitmap

            zoom = 1f
            offsetX = 0f
            offsetY = 0f
        } catch (e: Exception) {
            errorMessage =
                "Seite konnte nicht angezeigt werden: " +
                (e.localizedMessage ?: "Unbekannter Fehler")
        } finally {
            loading = false
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            pageBitmap?.recycle()
            pdfFile?.delete()
        }
    }

    val transformState = rememberTransformableState {
            zoomChange, panChange, _ ->

        zoom = (zoom * zoomChange).coerceIn(1f, 5f)

        if (zoom > 1f) {
            offsetX += panChange.x
            offsetY += panChange.y
        } else {
            offsetX = 0f
            offsetY = 0f
        }
    }

    Scaffold(
        topBar = {
            Surface(
                tonalElevation = 3.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Vorschau schließen"
                        )
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp)
                    ) {
                        Text(
                            text = documentName,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1
                        )

                        Text(
                            text = if (pageCount > 0) {
                                "Seite ${currentPage + 1} von $pageCount"
                            } else {
                                "PDF-Vorschau"
                            },
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    IconButton(
                        onClick = {
                            zoom = (zoom * 1.25f).coerceAtMost(5f)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ZoomIn,
                            contentDescription = "Vergrößern"
                        )
                    }
                }
            }
        },
        bottomBar = {
            Surface(
                tonalElevation = 4.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = {
                                if (currentPage > 0) currentPage--
                            },
                            enabled = currentPage > 0 && !loading
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = null
                            )
                            Spacer(Modifier.width(6.dp))
                            Text("Zurück")
                        }

                        Spacer(Modifier.width(18.dp))

                        Text(
                            text = if (pageCount > 0) {
                                "${currentPage + 1} / $pageCount"
                            } else {
                                "– / –"
                            },
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(Modifier.width(18.dp))

                        OutlinedButton(
                            onClick = {
                                if (currentPage < pageCount - 1) {
                                    currentPage++
                                }
                            },
                            enabled = pageCount > 0 &&
                                currentPage < pageCount - 1 &&
                                !loading
                        ) {
                            Text("Weiter")
                            Spacer(Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = null
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            modifier = Modifier.weight(1f),
                            onClick = {
                                saveLauncher.launch(
                                    documentName
                                        .removeSuffix(".pdf")
                                        .ifBlank { "Fahrzeugbericht" } +
                                        ".pdf"
                                )
                            },
                            enabled = !loading && pdfBytes.isNotEmpty()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = null
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("PDF speichern")
                        }

                        Button(
                            modifier = Modifier.weight(1f),
                            onClick = {
                                if (onPrint != null) {
                                    onPrint()
                                } else {
                                    printPdfBytes(
                                        context = context,
                                        pdfBytes = pdfBytes,
                                        documentName = documentName
                                    )
                                }
                            },
                            enabled = !loading && pdfBytes.isNotEmpty()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Print,
                                contentDescription = null
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Drucken")
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            when {
                loading -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator()
                        Text("PDF wird geladen …")
                    }
                }

                errorMessage != null -> {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = errorMessage.orEmpty(),
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )

                        OutlinedButton(onClick = onClose) {
                            Text("Schließen")
                        }
                    }
                }

                pageBitmap != null -> {
                    Image(
                        bitmap = pageBitmap!!.asImageBitmap(),
                        contentDescription = "PDF-Seite ${currentPage + 1}",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                androidx.compose.ui.graphics.Color.White
                            )
                            .graphicsLayer {
                                scaleX = zoom
                                scaleY = zoom
                                translationX = offsetX
                                translationY = offsetY
                            }
                            .transformable(state = transformState)
                            .pointerInput(Unit) {
                                // Die transformable-Geste übernimmt Zoom und Verschieben.
                            }
                    )
                }

                else -> {
                    Text("Keine PDF-Seite verfügbar.")
                }
            }
        }
    }
}

private fun printPdfBytes(
    context: Context,
    pdfBytes: ByteArray,
    documentName: String
) {
    val printManager =
        context.getSystemService(Context.PRINT_SERVICE) as PrintManager

    printManager.print(
        documentName,
        object : PrintDocumentAdapter() {

            override fun onLayout(
                oldAttributes: PrintAttributes?,
                newAttributes: PrintAttributes?,
                cancellationSignal: CancellationSignal?,
                callback: PrintDocumentAdapter.LayoutResultCallback,
                extras: Bundle?
            ) {
                if (cancellationSignal?.isCanceled == true) {
                    callback.onLayoutCancelled()
                    return
                }

                val info = PrintDocumentInfo.Builder(
                    documentName.removeSuffix(".pdf") + ".pdf"
                )
                    .setContentType(
                        PrintDocumentInfo.CONTENT_TYPE_DOCUMENT
                    )
                    .setPageCount(
                        PrintDocumentInfo.PAGE_COUNT_UNKNOWN
                    )
                    .build()

                callback.onLayoutFinished(info, true)
            }

            override fun onWrite(
                pages: Array<out PageRange>,
                destination: ParcelFileDescriptor,
                cancellationSignal: CancellationSignal,
                callback: PrintDocumentAdapter.WriteResultCallback
            ) {
                if (cancellationSignal.isCanceled) {
                    callback.onWriteCancelled()
                    return
                }

                try {
                    FileOutputStream(destination.fileDescriptor).use { output ->
                        output.write(pdfBytes)
                        output.flush()
                    }

                    callback.onWriteFinished(
                        arrayOf(PageRange.ALL_PAGES)
                    )
                } catch (e: Exception) {
                    callback.onWriteFailed(
                        e.localizedMessage ?: "Drucken fehlgeschlagen."
                    )
                }
            }
        },
        null
    )
}
