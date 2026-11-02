
package de.autocheck.app

/**
 * Die drei CARLOCA-Tarife.
 *
 * null bedeutet: unbegrenzt viele Fahrzeuge.
 */
enum class CarLocaPlan(
    val displayName: String,
    val maxVehicles: Int?
) {
    FREE(
        displayName = "CARLOCA Free",
        maxVehicles = 1
    ),

    PREMIUM(
        displayName = "CARLOCA Premium",
        maxVehicles = 10
    ),

    BIG_PREMIUM(
        displayName = "CARLOCA Big-Premium",
        maxVehicles = null
    );

    fun canAddVehicle(currentCount: Int): Boolean {
        return maxVehicles == null || currentCount < maxVehicles
    }

    fun vehicleLimitText(): String {
        return maxVehicles?.let { "bis zu $it Fahrzeuge" }
            ?: "unbegrenzt viele Fahrzeuge"
    }
}

/**
 * Vorläufiger Tarifstatus.
 *
 * Bis Google Play Billing eingebaut ist, bleibt der Tarif FREE.
 * Später wird dieser Wert durch den verifizierten Abo-Status ersetzt.
 */
object CarLocaSubscription {
    var currentPlan: CarLocaPlan = CarLocaPlan.FREE
}
