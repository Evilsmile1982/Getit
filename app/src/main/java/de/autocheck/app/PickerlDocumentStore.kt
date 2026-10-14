package de.autocheck.app

import android.content.Context
import de.autocheck.app.data.PickerlDocument
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class PickerlDocumentStore(context: Context) {

private val prefs = context.getSharedPreferences(
    "autocheck",
    Context.MODE_PRIVATE
)

fun load(): List<PickerlDocument> {
    val array = JSONArray(
        prefs.getString("pickerl_documents", "[]") ?: "[]"
    )

    return buildList {
        for (i in 0 until array.length()) {
            val item = array.getJSONObject(i)

            add(
                PickerlDocument(
                    id = item.optString("id").ifBlank {
                        UUID.randomUUID().toString()
                    },
                    vehicle = item.optString("vehicle"),
                    date = item.optString("date"),
                    type = item.optString("type", "Prüfbericht"),
                    name = item.optString("name"),
                    uri = item.optString("uri"),
                    mimeType = item.optString(
                        "mimeType",
                        "application/pdf"
                    ),
                    includeInPdf = item.optBoolean(
                        "includeInPdf",
                        true
                    )
                )
            )
        }
    }
}

fun save(documents: List<PickerlDocument>) {
    val array = JSONArray()

    documents.forEach { document ->
        array.put(
            JSONObject().apply {
                put("id", document.id)
                put("vehicle", document.vehicle)
                put("date", document.date)
                put("type", document.type)
                put("name", document.name)
                put("uri", document.uri)
                put("mimeType", document.mimeType)
                put("includeInPdf", document.includeInPdf)
            }
        )
    }

    prefs.edit()
        .putString("pickerl_documents", array.toString())
        .apply()
}

}
