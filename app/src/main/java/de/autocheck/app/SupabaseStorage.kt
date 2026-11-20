
package de.autocheck.app

import android.content.Context
import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import java.util.UUID
import java.util.concurrent.Executors

class SupabaseStorage(context: Context) {

    companion object {
        private const val SUPABASE_URL =
            "https://dpkvxszzhaucdymaekkx.supabase.co"

        private const val SUPABASE_KEY =
            "sb_publishable_VyllOzxYb1q-iRfTe3UI8w_mk1jsqxO"

        private const val FUNCTION_URL =
            "$SUPABASE_URL/functions/v1/carloca-upload"
    }

    private val appContext = context.applicationContext
    private val auth = FirebaseAuth.getInstance()
    private val executor = Executors.newSingleThreadExecutor()

    interface UploadCallback {
        fun onSuccess(storagePath: String)
        fun onError(error: Exception)
    }

    interface DownloadCallback {
        fun onSuccess(localUri: Uri)
        fun onError(error: Exception)
    }

    // ============================================================
    // BILD HOCHLADEN
    // ============================================================

    fun upload(
        sourceUri: Uri,
        category: String,
        vehicleId: String,
        recordId: String,
        callback: UploadCallback
    ) {
        val user = auth.currentUser

        if (user == null) {
            callback.onError(
                IllegalStateException("Bitte zuerst anmelden.")
            )
            return
        }

        user.getIdToken(false)
            .addOnSuccessListener { result ->

                val token = result.token

                if (token.isNullOrBlank()) {
                    callback.onError(
                        IllegalStateException(
                            "Firebase-Anmeldetoken fehlt."
                        )
                    )
                    return@addOnSuccessListener
                }

                executor.execute {
                    try {
                        val storagePath = uploadFile(
                            sourceUri = sourceUri,
                            token = token,
                            category = category,
                            vehicleId = vehicleId,
                            recordId = recordId
                        )

                        callback.onSuccess(storagePath)

                    } catch (e: Exception) {
                        callback.onError(e)
                    }
                }
            }
            .addOnFailureListener {
                callback.onError(it)
            }
    }

    private fun uploadFile(
        sourceUri: Uri,
        token: String,
        category: String,
        vehicleId: String,
        recordId: String
    ): String {

        val detectedMimeType =
            appContext.contentResolver.getType(sourceUri)
                ?: "image/jpeg"

        val mimeType = when (detectedMimeType) {
            "image/jpg" -> "image/jpeg"
            else -> detectedMimeType
        }

        if (mimeType !in listOf(
                "image/jpeg",
                "image/png",
                "image/webp"
            )
        ) {
            throw IllegalArgumentException(
                "Bitte ein JPG-, PNG- oder WEBP-Bild auswählen."
            )
        }

        val extension = when (mimeType) {
            "image/png" -> "png"
            "image/webp" -> "webp"
            else -> "jpg"
        }

        val boundary =
            "CARLOCA_${UUID.randomUUID().toString().replace("-", "")}"

        val connection =
            URL(FUNCTION_URL).openConnection() as HttpURLConnection

        connection.requestMethod = "POST"
        connection.doOutput = true
        connection.connectTimeout = 30000
        connection.readTimeout = 60000
        connection.setChunkedStreamingMode(64 * 1024)

        connection.setRequestProperty(
            "apikey",
            SUPABASE_KEY
        )

        connection.setRequestProperty(
            "Authorization",
            "Bearer $token"
        )

        connection.setRequestProperty(
            "Content-Type",
            "multipart/form-data; boundary=$boundary"
        )

        connection.setRequestProperty(
            "Accept",
            "application/json"
        )

        try {
            connection.outputStream.use { output ->

                writeFormField(
                    output, boundary, "category", category
                )

                writeFormField(
                    output, boundary, "vehicleId", vehicleId
                )

                writeFormField(
                    output, boundary, "recordId", recordId
                )

                output.write(
                    "--$boundary\r\n".toByteArray(Charsets.UTF_8)
                )

                output.write(
                    (
                        "Content-Disposition: form-data; " +
                            "name=\"file\"; filename=\"image.$extension\"\r\n"
                        ).toByteArray(Charsets.UTF_8)
                )

                output.write(
                    "Content-Type: $mimeType\r\n\r\n"
                        .toByteArray(Charsets.UTF_8)
                )

                val input =
                    appContext.contentResolver.openInputStream(sourceUri)
                        ?: throw IllegalStateException(
                            "Das ausgewählte Bild kann nicht gelesen werden."
                        )

                input.use {
                    it.copyTo(output)
                }

                output.write(
                    "\r\n--$boundary--\r\n"
                        .toByteArray(Charsets.UTF_8)
                )

                output.flush()
            }

            val responseCode = connection.responseCode

            val responseText = readResponseText(
                if (responseCode in 200..299) {
                    connection.inputStream
                } else {
                    connection.errorStream
                }
            )

            if (responseCode !in 200..299) {
                throw IllegalStateException(
                    "CARLOCA-Bild-Upload fehlgeschlagen " +
                        "(HTTP $responseCode): $responseText"
                )
            }

            val json = JSONObject(responseText)

            val storagePath = json.optString("storagePath")

            if (
                !json.optBoolean("success", false) ||
                storagePath.isBlank()
            ) {
                throw IllegalStateException(
                    "Supabase hat keinen gültigen Bildpfad zurückgegeben: " +
                        responseText
                )
            }

            return storagePath

        } finally {
            connection.disconnect()
        }
    }

    private fun writeFormField(
        output: java.io.OutputStream,
        boundary: String,
        name: String,
        value: String
    ) {
        output.write(
            "--$boundary\r\n".toByteArray(Charsets.UTF_8)
        )

        output.write(
            "Content-Disposition: form-data; name=\"$name\"\r\n\r\n"
                .toByteArray(Charsets.UTF_8)
        )

        output.write(value.toByteArray(Charsets.UTF_8))

        output.write(
            "\r\n".toByteArray(Charsets.UTF_8)
        )
    }

    // ============================================================
    // BILD HERUNTERLADEN
    // ============================================================

    fun download(
        storagePath: String,
        callback: DownloadCallback
    ) {
        val user = auth.currentUser

        if (user == null) {
            callback.onError(
                IllegalStateException("Bitte zuerst anmelden.")
            )
            return
        }

        if (!storagePath.startsWith("${user.uid}/")) {
            callback.onError(
                SecurityException(
                    "Diese Datei gehört nicht zum angemeldeten Profil."
                )
            )
            return
        }

        user.getIdToken(false)
            .addOnSuccessListener { result ->

                val token = result.token

                if (token.isNullOrBlank()) {
                    callback.onError(
                        IllegalStateException(
                            "Firebase-Anmeldetoken fehlt."
                        )
                    )
                    return@addOnSuccessListener
                }

                executor.execute {
                    try {
                        val localUri =
                            downloadFile(storagePath, token)

                        callback.onSuccess(localUri)

                    } catch (e: Exception) {
                        callback.onError(e)
                    }
                }
            }
            .addOnFailureListener {
                callback.onError(it)
            }
    }

    private fun downloadFile(
        storagePath: String,
        token: String
    ): Uri {

        val downloadDirectory =
            File(appContext.filesDir, "carloca_storage")

        if (
            !downloadDirectory.exists() &&
            !downloadDirectory.mkdirs()
        ) {
            throw IllegalStateException(
                "Lokaler Bildspeicher konnte nicht erstellt werden."
            )
        }

        val hash = sha256(storagePath)

        val localFile =
            File(downloadDirectory, "$hash.img")

        if (
            localFile.exists() &&
            localFile.length() > 0L
        ) {
            return Uri.fromFile(localFile)
        }

        val connection =
            URL(FUNCTION_URL).openConnection() as HttpURLConnection

        connection.requestMethod = "POST"
        connection.doOutput = true
        connection.connectTimeout = 30000
        connection.readTimeout = 60000

        connection.setRequestProperty(
            "apikey",
            SUPABASE_KEY
        )

        connection.setRequestProperty(
            "Authorization",
            "Bearer $token"
        )

        connection.setRequestProperty(
            "Content-Type",
            "application/json"
        )

        connection.setRequestProperty(
            "Accept",
            "image/*"
        )

        val temporaryFile =
            File(downloadDirectory, "$hash.tmp")

        try {
            val requestBody = JSONObject()
                .put("action", "download")
                .put("storagePath", storagePath)
                .toString()

            connection.outputStream.use { output ->
                output.write(
                    requestBody.toByteArray(Charsets.UTF_8)
                )
                output.flush()
            }

            val responseCode = connection.responseCode

            if (responseCode !in 200..299) {
                val errorText = readResponseText(
                    connection.errorStream
                )

                throw IllegalStateException(
                    "CARLOCA-Bild-Download fehlgeschlagen " +
                        "(HTTP $responseCode): $errorText"
                )
            }

            val contentType =
                connection.contentType ?: ""

            if (contentType.contains("application/json")) {
                val errorText = readResponseText(
                    connection.inputStream
                )

                throw IllegalStateException(
                    "Unerwartete Serverantwort: $errorText"
                )
            }

            connection.inputStream.use { input ->
                FileOutputStream(temporaryFile).use { output ->
                    input.copyTo(output)
                }
            }

            if (
                !temporaryFile.exists() ||
                temporaryFile.length() == 0L
            ) {
                throw IllegalStateException(
                    "Das heruntergeladene Bild ist leer."
                )
            }

            if (localFile.exists()) {
                localFile.delete()
            }

            if (!temporaryFile.renameTo(localFile)) {
                temporaryFile.copyTo(
                    localFile,
                    overwrite = true
                )
                temporaryFile.delete()
            }

            return Uri.fromFile(localFile)

        } finally {
            connection.disconnect()

            if (temporaryFile.exists()) {
                temporaryFile.delete()
            }
        }
    }

    // ============================================================
    // HILFSFUNKTIONEN
    // ============================================================

    private fun readResponseText(
        input: InputStream?
    ): String {
        if (input == null) return ""

        return input.bufferedReader(Charsets.UTF_8).use {
            it.readText()
        }
    }

    private fun sha256(value: String): String {
        val bytes = MessageDigest
            .getInstance("SHA-256")
            .digest(value.toByteArray(Charsets.UTF_8))

        return bytes.joinToString("") {
            "%02x".format(it)
        }
    }

    fun shutdown() {
        executor.shutdown()
    }
}
