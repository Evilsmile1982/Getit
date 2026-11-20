
package de.autocheck.app

import android.content.Context
import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.UUID
import java.util.concurrent.Executors

class SupabaseStorage(context: Context) {

    companion object {
        private const val SUPABASE_URL =
            "https://dpkvxszzhaucdymaekkx.supabase.co"

        private const val SUPABASE_KEY =
            "sb_publishable_VyllOzxYb1q-iRfTe3UI8w_mk1jsqxO"

        private const val BUCKET =
            "carloca-files"
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

    /**
     * Lädt ein lokales Bild oder PDF in den privaten
     * CARLOCA-Speicher hoch.
     *
     * Jeder Dateipfad beginnt mit der Firebase-UID.
     */
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
                        val path = uploadFile(
                            sourceUri = sourceUri,
                            userId = user.uid,
                            token = token,
                            category = category,
                            vehicleId = vehicleId,
                            recordId = recordId
                        )

                        callback.onSuccess(path)

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
        userId: String,
        token: String,
        category: String,
        vehicleId: String,
        recordId: String
    ): String {

        val mimeType =
            appContext.contentResolver.getType(sourceUri)
                ?: "application/octet-stream"

        val extension = when (mimeType) {
            "image/jpeg" -> "jpg"
            "image/png" -> "png"
            "image/webp" -> "webp"
            "application/pdf" -> "pdf"
            else -> "bin"
        }

        val safeCategory = safeSegment(category)
        val safeVehicle = safeSegment(vehicleId)
        val safeRecord = safeSegment(recordId)

        val fileName =
            "${UUID.randomUUID()}.$extension"

        val storagePath =
            "$userId/$safeCategory/$safeVehicle/$safeRecord/$fileName"

        val encodedPath = storagePath
            .split("/")
            .joinToString("/") {
                URLEncoder.encode(it, "UTF-8")
                    .replace("+", "%20")
            }

        val url =
            "$SUPABASE_URL/storage/v1/object/$BUCKET/$encodedPath"

        val connection =
            URL(url).openConnection() as HttpURLConnection

        connection.requestMethod = "POST"
        connection.doOutput = true
        connection.connectTimeout = 30000
        connection.readTimeout = 30000

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
            mimeType
        )

        connection.setRequestProperty(
            "x-upsert",
            "true"
        )

        try {
            val input =
                appContext.contentResolver.openInputStream(sourceUri)
                    ?: throw IllegalStateException(
                        "Die ausgewählte Datei kann nicht gelesen werden."
                    )

            input.use { source ->
                connection.outputStream.use { output ->
                    source.copyTo(output)
                }
            }

            val responseCode = connection.responseCode

            if (responseCode !in 200..299) {
                val error =
                    connection.errorStream
                        ?.bufferedReader()
                        ?.use { it.readText() }
                        ?: "HTTP $responseCode"

                throw IllegalStateException(
                    "Supabase-Upload fehlgeschlagen: $error"
                )
            }

            return storagePath

        } finally {
            connection.disconnect()
        }
    }

    /**
     * Lädt eine private Datei anhand ihres Speicherpfades
     * wieder auf das Gerät.
     */
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

        val encodedPath = storagePath
            .split("/")
            .joinToString("/") {
                URLEncoder.encode(it, "UTF-8")
                    .replace("+", "%20")
            }

        val url =
            "$SUPABASE_URL/storage/v1/object/authenticated/$BUCKET/$encodedPath"

        val connection =
            URL(url).openConnection() as HttpURLConnection

        connection.requestMethod = "GET"
        connection.connectTimeout = 30000
        connection.readTimeout = 30000

        connection.setRequestProperty(
            "apikey",
            SUPABASE_KEY
        )

        connection.setRequestProperty(
            "Authorization",
            "Bearer $token"
        )

        try {
            val responseCode = connection.responseCode

            if (responseCode !in 200..299) {
                val error =
                    connection.errorStream
                        ?.bufferedReader()
                        ?.use { it.readText() }
                        ?: "HTTP $responseCode"

                throw IllegalStateException(
                    "Supabase-Download fehlgeschlagen: $error"
                )
            }

            val fileName =
                storagePath.substringAfterLast("/")

            val localFile =
                File(
                    appContext.cacheDir,
                    "carloca_$fileName"
                )

            connection.inputStream.use { input ->
                FileOutputStream(localFile).use { output ->
                    input.copyTo(output)
                }
            }

            return Uri.fromFile(localFile)

        } finally {
            connection.disconnect()
        }
    }

    private fun safeSegment(value: String): String {
        return value
            .replace(Regex("[^a-zA-Z0-9_-]"), "_")
            .take(100)
            .ifBlank { "allgemein" }
    }

    fun shutdown() {
        executor.shutdown()
    }
}
