
package de.autocheck.app

import android.content.Context

data class HomeMenuItem(
    val id: String,
    val title: String,
    val contentDescription: String,
    val iconRes: Int,
    val action: String
)

class HomeMenuLayoutStore(context: Context) {

    private val preferences = context.getSharedPreferences(
        "carloca_home_menu_layout",
        Context.MODE_PRIVATE
    )

    fun load(): List<String> {
        val saved = preferences.getString(KEY_ORDER, null)
            ?: return DEFAULT_ORDER

        val validIds = DEFAULT_ORDER.toSet()

        val cleaned = saved.split("|")
            .filter { it in validIds }
            .distinct()

        return cleaned + DEFAULT_ORDER.filterNot {
            it in cleaned
        }
    }

    fun save(ids: List<String>) {
        preferences.edit()
            .putString(KEY_ORDER, ids.joinToString("|"))
            .apply()
    }

    fun reset() {
        preferences.edit()
            .remove(KEY_ORDER)
            .apply()
    }

    private companion object {
        const val KEY_ORDER = "button_order"

        val DEFAULT_ORDER = listOf(
            "fuel",
            "history",
            "pdf",
            "contract",
            "plans",
            "tuning",
            "electric",
            "calendar"
        )
    }
}
