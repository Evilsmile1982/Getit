
package de.autocheck.app

import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.auth.FirebaseAuth
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.concurrent.Executors

class SupabaseBackup(context: Context) {

    companion object {
        private const val SUPABASE_URL =
            "https://dpkvxszzhaucdymaekkx.supabase.co"

        private const val SUPABASE_KEY =
            "sb_publishable_VyllOzxYb1q-iRfTe3UI8w_mk1jsqxO"

        private const val TABLE =
            "carloca_user_data"
    }

    private val appContext = context.applicationContext
    private val auth = FirebaseAuth.getInstance()
    private val executor = Executors.newSingleThreadExecutor()

    private val preferences =
        appContext.getSharedPreferences(
            "autocheck",
            Context.MODE_PRIVATE
        )

    private var listener:
        SharedPreferences.OnSharedPreferenceChangeListener? = null

    @Volatile
    private var started = false

    fun start(
        onReady: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val user = auth.currentUser

        if (user == null) {
            onError(IllegalStateException("Nicht angemeldet"))
            return
        }

        user.getIdToken(false)
            .addOnSuccessListener { result ->

                val token = result.token

                if (token.isNullOrBlank()) {
                    onError(
                        IllegalStateException(
                            "Firebase-Anmeldetoken fehlt"
                        )
                    )
                    return@addOnSuccessListener
                }

                executor.execute {
                    try {
                        restoreOrUpload(
                            user.uid,
                            token
                        )

                        registerListener(user.uid)

                        started = true
                        onReady()

                    } catch (e: Exception) {
                        onError(e)
                    }
                }
            }
            .addOnFailureListener { error ->
                onError(error)
            }
    }

    private fun restoreOrUpload(
        userId: String,
        token: String
    ) {
        val encodedId =
            URLEncoder.encode(userId, "UTF-8")

        val url =
            "$SUPABASE_URL/rest/v1/$TABLE" +
            "?user_id=eq.$encodedId&select=data"

        val connection =
            URL(url).openConnection() as HttpURLConnection

        connection.requestMethod = "GET"
        connection.connectTimeout = 20000
        connection.readTimeout = 20000

        connection.setRequestProperty(
            "apikey",
            SUPABASE_KEY
        )

        connection.setRequestProperty(
            "Authorization",
            "Bearer $token"
        )

        connection.setRequestProperty(
            "Accept",
            "application/json"
        )

        try {
            val responseCode =
                connection.responseCode

            val response =
                if (responseCode in 200..299) {
                    connection.inputStream
                        .bufferedReader()
                        .use { it.readText() }
                } else {
                    val errorText =
                        connection.errorStream
                            ?.bufferedReader()
                            ?.use { it.readText() }
                            ?: "HTTP $responseCode"

                    throw IllegalStateException(errorText)
                }

            val rows = org.json.JSONArray(response)

            if (rows.length() > 0) {
                val data =
                    rows.getJSONObject(0)
                        .getJSONObject("data")

                val editor = preferences.edit()

                val keys = data.keys()

                while (keys.hasNext()) {
                    val key = keys.next()
                    val value = data.get(key)

                    when (value) {
                        is String ->
                            editor.putString(key, value)

                        is Boolean ->
                            editor.putBoolean(key, value)

                        is Int ->
                            editor.putInt(key, value)

                        is Long ->
                            editor.putLong(key, value)

                        is Number ->
                            editor.putFloat(
                                key,
                                value.toFloat()
                            )
                    }
                }

                editor.apply()
            } else {
                uploadAll(userId, token)
            }

        } finally {
            connection.disconnect()
        }
    }

    private fun uploadAll(
        userId: String,
        token: String
    ) {
        val data = JSONObject()

        preferences.all.forEach { (key, value) ->
            when (value) {
                is String ->
                    data.put(key, value)

                is Boolean ->
                    data.put(key, value)

                is Int ->
                    data.put(key, value)

                is Long ->
                    data.put(key, value)

                is Float ->
                    data.put(key, value.toDouble())
            }
        }

        val body = JSONObject().apply {
            put("user_id", userId)
            put("data", data)
            put("updated_at", System.currentTimeMillis())
        }

        val connection =
            URL("$SUPABASE_URL/rest/v1/$TABLE")
                .openConnection() as HttpURLConnection

        connection.requestMethod = "POST"
        connection.doOutput = true
        connection.connectTimeout = 20000
        connection.readTimeout = 20000

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
            "Prefer",
            "resolution=merge-duplicates"
        )

        try {
            connection.outputStream.use {
                it.write(body.toString().toByteArray(Charsets.UTF_8))
            }

            val code = connection.responseCode

            if (code !in 200..299) {
                val error =
                    connection.errorStream
                        ?.bufferedReader()
                        ?.use { it.readText() }
                        ?: "HTTP $code"

                throw IllegalStateException(error)
            }
        } finally {
            connection.disconnect()
        }
    }

    private fun registerListener(userId: String) {
        if (listener != null) return

        val newListener =
            SharedPreferences.OnSharedPreferenceChangeListener {
                    prefs, _ ->

                auth.currentUser?.getIdToken(false)
                    ?.addOnSuccessListener { result ->

                        val token = result.token
                            ?: return@addOnSuccessListener

                        executor.execute {
                            try {
                                uploadAll(userId, token)
                            } catch (_: Exception) {
                                // Fehler beim nächsten Speichern erneut versuchen
                            }
                        }
                    }
            }

        listener = newListener

        preferences.registerOnSharedPreferenceChangeListener(
            newListener
        )
    }

    fun stop() {
        listener?.let {
            preferences.unregisterOnSharedPreferenceChangeListener(it)
        }

        listener = null
        started = false
    }
}
