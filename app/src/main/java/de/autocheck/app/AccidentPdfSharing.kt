
package de.autocheck.app

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File

private fun createAccidentPdfUri(
    context: Context,
    path: String
): android.net.Uri? {
    val file = File(path)

    if (!file.exists()) {
        Toast.makeText(
            context,
            "Der Unfallbericht wurde nicht gefunden.",
            Toast.LENGTH_LONG
        ).show()
        return null
    }

    return try {
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    } catch (exception: Exception) {
        Toast.makeText(
            context,
            "PDF-Freigabe fehlgeschlagen: ${exception.message}",
            Toast.LENGTH_LONG
        ).show()
        null
    }
}

fun shareAccidentPdfWhatsApp(
    context: Context,
    path: String
) {
    val uri = createAccidentPdfUri(context, path) ?: return

    try {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            setPackage("com.whatsapp")
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(
                Intent.EXTRA_SUBJECT,
                "CARLOCA Unfallbericht"
            )
            clipData = android.content.ClipData.newUri(
                context.contentResolver,
                "CARLOCA Unfallbericht",
                uri
            )
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        Toast.makeText(
            context,
            "WhatsApp ist nicht installiert.",
            Toast.LENGTH_LONG
        ).show()
    } catch (exception: Exception) {
        Toast.makeText(
            context,
            "WhatsApp konnte nicht geöffnet werden: ${exception.message}",
            Toast.LENGTH_LONG
        ).show()
    }
}

fun shareAccidentPdfEmail(
    context: Context,
    path: String
) {
    val uri = createAccidentPdfUri(context, path) ?: return

    val emailPackages = listOf(
        "com.google.android.gm",
        "com.microsoft.office.outlook",
        "com.samsung.android.email.provider"
    )

    for (packageName in emailPackages) {
        try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                setPackage(packageName)
                putExtra(
                    Intent.EXTRA_SUBJECT,
                    "CARLOCA Unfallbericht"
                )
                putExtra(
                    Intent.EXTRA_TEXT,
                    "Anbei der CARLOCA Unfallbericht."
                )
                putExtra(Intent.EXTRA_STREAM, uri)
                clipData = android.content.ClipData.newUri(
                    context.contentResolver,
                    "CARLOCA Unfallbericht",
                    uri
                )
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(intent)
            return
        } catch (_: ActivityNotFoundException) {
            // Die nächste unterstützte E-Mail-App versuchen.
        }
    }

    Toast.makeText(
        context,
        "Keine unterstützte E-Mail-App gefunden. " +
            "Bitte Gmail, Outlook oder Samsung E-Mail installieren.",
        Toast.LENGTH_LONG
    ).show()
}
