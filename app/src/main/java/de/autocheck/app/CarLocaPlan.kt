
package de.autocheck.app

/**
 * Die drei CARLOCA-Tarife.
 */
enum class CarLocaPlan(
    val displayName: String,
    val maxVehicles: Int
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
        maxVehicles = 30
    );

    fun canAddVehicle(currentCount: Int): Boolean {
        return currentCount < maxVehicles
    }

    fun vehicleLimitText(): String {
        return "bis zu $maxVehicles Fahrzeuge"
    }
}

/**
 * Vorläufiger Tarifstatus.
 *
 * Bis Google Play Billing eingebaut ist,
 * bleibt der Tarif FREE.
 */
object CarLocaSubscription {
    var currentPlan: CarLocaPlan = CarLocaPlan.FREE
}
