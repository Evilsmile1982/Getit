
package de.autocheck.app

import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Sichert die lokalen CARLOCA-Einstellungen und JSON-Daten
 * getrennt pro angemeldetem Firebase-Nutzer.
 */
class FirestoreBackup(context: Context) {

    private val appContext = context.applicationContext

    private val preferences =
        appContext.getSharedPreferences(
            "autocheck",
            Context.MODE_PRIVATE
        )

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private var preferenceListener:
        SharedPreferences.OnSharedPreferenceChangeListener? = null

    private var started = false

    private fun userCollection() =
        firestore.collection("users")
            .document(
                auth.currentUser?.uid
                    ?: throw IllegalStateException(
                        "Kein Nutzer angemeldet"
                    )
            )
            .collection("appData")

    /**
     * Startet die erste Sicherung oder stellt vorhandene
     * Cloud-Daten wieder her.
     */
    fun start(
        onReady: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        if (started) {
            onReady()
            return
        }

        val user = auth.currentUser

        if (user == null) {
            onError(
                IllegalStateException(
                    "Bitte zuerst anmelden."
                )
            )
            return
        }

        val collection = userCollection()
        val metaDocument = collection.document("_meta")

        metaDocument.get()
            .addOnSuccessListener { meta ->

                if (meta.exists()) {
                    restoreCloudData(
                        onReady,
                        onError
                    )
                } else {
                    uploadLocalData(
                        onReady,
                        onError
                    )
                }
            }
            .addOnFailureListener { error ->
                onError(error)
            }
    }

    /**
     * Überträgt die vorhandenen lokalen Daten beim
     * allerersten Start in die persönliche Cloud.
     */
    private fun uploadLocalData(
        onReady: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val collection = userCollection()
        val batch = firestore.batch()

        val localData = preferences.all

        localData.forEach { (key, value) ->

            if (key.isNotBlank() && value != null) {
                val document = collection.document(key)

                batch.set(
                    document,
                    encodeValue(value)
                )
            }
        }

        batch.set(
            collection.document("_meta"),
            mapOf(
                "initialized" to true,
                "version" to 1
            )
        )

        batch.commit()
            .addOnSuccessListener {
                registerLocalChanges()
                started = true
                onReady()
            }
            .addOnFailureListener { error ->
                onError(error)
            }
    }

    /**
     * Lädt vorhandene Cloud-Daten auf das Gerät.
     */
    private fun restoreCloudData(
        onReady: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        userCollection()
            .get()
            .addOnSuccessListener { documents ->

                val editor = preferences.edit()

                documents.documents.forEach { document ->

                    if (document.id == "_meta") {
                        return@forEach
                    }

                    val data = document.data
                        ?: return@forEach

                    val type = data["type"] as? String
                        ?: return@forEach

                    when (type) {
                        "String" -> {
                            val value =
                                data["value"] as? String
                                    ?: ""

                            editor.putString(
                                document.id,
                                value
                            )
                        }

                        "Boolean" -> {
                            val value =
                                data["value"] as? Boolean
                                    ?: false

                            editor.putBoolean(
                                document.id,
                                value
                            )
                        }

                        "Int" -> {
                            val value =
                                (data["value"] as? Number)
                                    ?.toInt() ?: 0

                            editor.putInt(
                                document.id,
                                value
                            )
                        }

                        "Long" -> {
                            val value =
                                (data["value"] as? Number)
                                    ?.toLong() ?: 0L

                            editor.putLong(
                                document.id,
                                value
                            )
                        }

                        "Float" -> {
                            val value =
                                (data["value"] as? Number)
                                    ?.toFloat() ?: 0f

                            editor.putFloat(
                                document.id,
                                value
                            )
                        }
                    }
                }

                editor.apply()

                registerLocalChanges()
                started = true
                onReady()
            }
            .addOnFailureListener { error ->
                onError(error)
            }
    }

    /**
     * Beobachtet lokale Änderungen und überträgt sie
     * automatisch in die Cloud.
     */
    private fun registerLocalChanges() {

        if (preferenceListener != null) {
            return
        }

        val listener =
            SharedPreferences.OnSharedPreferenceChangeListener {
                    prefs,
                    key ->

                if (key.isNullOrBlank()) {
                    return@OnSharedPreferenceChangeListener
                }

                val value = prefs.all[key]
                    ?: return@OnSharedPreferenceChangeListener

                userCollection()
                    .document(key)
                    .set(encodeValue(value))
            }

        preferenceListener = listener

        preferences.registerOnSharedPreferenceChangeListener(
            listener
        )
    }

    /**
     * Wandelt lokale Werte in Firestore-kompatible Daten um.
     */
    private fun encodeValue(value: Any): Map<String, Any> {

        return when (value) {
            is String -> mapOf(
                "type" to "String",
                "value" to value
            )

            is Boolean -> mapOf(
                "type" to "Boolean",
                "value" to value
            )

            is Int -> mapOf(
                "type" to "Int",
                "value" to value
            )

            is Long -> mapOf(
                "type" to "Long",
                "value" to value
            )

            is Float -> mapOf(
                "type" to "Float",
                "value" to value.toDouble()
            )

            else -> mapOf(
                "type" to "String",
                "value" to value.toString()
            )
        }
    }

    /**
     * Beendet die lokale Änderungsüberwachung.
     */
    fun stop() {
        preferenceListener?.let { listener ->
            preferences.unregisterOnSharedPreferenceChangeListener(
                listener
            )
        }

        preferenceListener = null
        started = false
    }
}
