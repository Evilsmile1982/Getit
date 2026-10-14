package de.autocheck.app

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import de.autocheck.app.data.PickerlDocument

/**
 * Rendert Pickerl-Dokumente für die Übernahme in einen
 * zusammengeführten Fahrzeugbericht.
 *
 * PDF-Dateien werden seitenweise gerendert.
 * Bilddateien werden als einzelne Seiten behandelt.
 */
object PickerlPdfPages {

    private const val MAX_PAGE_WIDTH = 1200
    private const val MAX_PAGE_HEIGHT = 1600

    fun renderSelectedDocuments(
        context: Context,
        vehicleName: String
    ): List<Pair<String, Bitmap>> {

        val store = PickerlDocumentStore(context)

        val documents = store.load()
            .filter {
                it.vehicle == vehicleName &&
                    it.includeInPdf &&
                    it.uri.isNotBlank()
            }
            .sortedBy {
                documentSortDate(it.date)
            }

        val result = mutableListOf<Pair<String, Bitmap>>()

        documents.forEach { document ->
            val uri = runCatching {
                Uri.parse(document.uri)
            }.getOrNull() ?: return@forEach

            try {
                if (
                    document.mimeType.equals(
                        "application/pdf",
                        ignoreCase = true
                    ) ||
                    document.name.endsWith(
                        ".pdf",
                        ignoreCase = true
                    )
                ) {
                    result.addAll(
                        renderPdf(
                            context = context,
                            uri = uri,
                            document = document
                        )
                    )
                } else {
                    renderImage(
                        context = context,
                        uri = uri
                    )?.let { bitmap ->
                        result.add(
                            "${document.date} · ${document.type} · ${document.name}" to
                                bitmap
                        )
                    }
                }
            } catch (_: Exception) {
                // Ein defektes oder nicht mehr erreichbares Dokument
                // darf die übrigen Dokumente nicht blockieren.
            }
        }

        return result
    }

    private fun renderPdf(
        context: Context,
        uri: Uri,
        document: PickerlDocument
    ): List<Pair<String, Bitmap>> {

        val pages = mutableListOf<Pair<String, Bitmap>>()

        val descriptor: ParcelFileDescriptor =
            context.contentResolver.openFileDescriptor(uri, "r")
                ?: return emptyList()

        descriptor.use { fileDescriptor ->
            PdfRenderer(fileDescriptor).use { renderer ->

                for (pageIndex in 0 until renderer.pageCount) {
                    renderer.openPage(pageIndex).use { page ->

                        val scale = minOf(
                            MAX_PAGE_WIDTH.toFloat() / page.width,
                            MAX_PAGE_HEIGHT.toFloat() / page.height
                        )

                        val width = (page.width * scale)
                            .toInt()
                            .coerceAtLeast(1)

                        val height = (page.height * scale)
                            .toInt()
                            .coerceAtLeast(1)

                        val bitmap = Bitmap.createBitmap(
                            width,
                            height,
                            Bitmap.Config.ARGB_8888
                        )

                        bitmap.eraseColor(
                            android.graphics.Color.WHITE
                        )

                        page.render(
                            bitmap,
                            null,
                            null,
                            PdfRenderer.Page.RENDER_MODE_FOR_PRINT
                        )

                        val label = buildString {
                            append(document.date)
                            append(" · ")
                            append(document.type)
                            append(" · ")
                            append(document.name)
                            append(" · Seite ")
                            append(pageIndex + 1)
                            append("/")
                            append(renderer.pageCount)
                        }

                        pages.add(label to bitmap)
                    }
                }
            }
        }

        return pages
    }

    private fun renderImage(
        context: Context,
        uri: Uri
    ): Bitmap? {

        val source = context.contentResolver
            .openInputStream(uri)
            .use { input ->
                if (input == null) {
                    null
                } else {
                    BitmapFactory.decodeStream(input)
                }
            } ?: return null

        val scale = minOf(
            1f,
            MAX_PAGE_WIDTH.toFloat() / source.width,
            MAX_PAGE_HEIGHT.toFloat() / source.height
        )

        if (scale >= 1f) {
            return source
        }

        val width = (source.width * scale)
            .toInt()
            .coerceAtLeast(1)

        val height = (source.height * scale)
            .toInt()
            .coerceAtLeast(1)

        val scaled = Bitmap.createScaledBitmap(
            source,
            width,
            height,
            true
        )

        if (scaled !== source) {
            source.recycle()
        }

        return scaled
    }

    private fun documentSortDate(value: String): String {
        val trimmed = value.trim()

        if (
            trimmed.matches(
                Regex("""\d{2}\.\d{2}\.\d{4}""")
            )
        ) {
            val parts = trimmed.split(".")
            return "${parts[2]}-${parts[1]}-${parts[0]}"
        }

        if (
            trimmed.matches(
                Regex("""\d{4}-\d{2}-\d{2}""")
            )
        ) {
            return trimmed
        }

        return trimmed
    }
}
