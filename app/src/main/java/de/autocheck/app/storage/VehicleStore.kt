package de.autocheck.app

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class VehicleStore(
    context: Context
) {

    private val prefs =
        context.getSharedPreferences(
            "autocheck",
            Context.MODE_PRIVATE
        )

    /*
     * ============================================================
     * FAHRZEUGE
     * ============================================================
     */

    fun load(): List<Vehicle> {

        val array =
            JSONArray(
                prefs.getString(
                    "vehicles",
                    "[]"
                ) ?: "[]"
            )

        val vehicles =
            buildList {

                for (
                    i in 0 until array.length()
                ) {

                    val o =
                        array.getJSONObject(i)

                    val storedId =
                        o.optString("id")

                    add(
                        Vehicle(
                            id =
                                if (storedId.isNotBlank()) {
                                    storedId
                                } else {
                                    UUID.randomUUID().toString()
                                },

                            name =
                                o.optString(
                                    "name"
                                ),

                            make =
                                o.optString(
                                    "make"
                                ),

                            model =
                                o.optString(
                                    "model"
                                ),

                            year =
                                o.optString(
                                    "year"
                                ),

                            plate =
                                o.optString(
                                    "plate"
                                ),

                            vin =
                                o.optString(
                                    "vin"
                                ),

                            imageUri =
                                o.optString(
                                    "imageUri"
                                )
                        )
                    )
                }
            }

        /*
         * Falls alte Fahrzeuge noch keine ID hatten,
         * werden die neu erzeugten IDs sofort dauerhaft gespeichert.
         */
        if (
            vehicles.any {
                it.id.isBlank()
            } ||
            vehicles.size != array.length()
        ) {

            save(
                vehicles
            )

        } else {

            var needsMigration =
                false

            for (
                i in vehicles.indices
            ) {

                val storedId =
                    array
                        .optJSONObject(i)
                        ?.optString("id")
                        ?: ""

                if (
                    storedId.isBlank() ||
                    storedId != vehicles[i].id
                ) {

                    needsMigration =
                        true

                    break
                }
            }

            if (needsMigration) {

                save(
                    vehicles
                )
            }
        }

        /*
         * Alte aktive Fahrzeugauswahl wurde bisher über den Namen
         * gespeichert. Falls noch keine ID vorhanden ist, wird der
         * alte Name einmalig auf die neue ID umgestellt.
         */
        val storedActiveId =
            prefs.getString(
                "activeVehicleId",
                ""
            ) ?: ""

        if (
            storedActiveId.isBlank() &&
            vehicles.isNotEmpty()
        ) {

            val oldActiveName =
                prefs.getString(
                    "activeVehicle",
                    ""
                ) ?: ""

            val matchingVehicle =
                vehicles.firstOrNull {
                    it.name == oldActiveName
                }

            val vehicleToActivate =
                matchingVehicle
                    ?: vehicles.first()

            prefs.edit()
                .putString(
                    "activeVehicleId",
                    vehicleToActivate.id
                )
                .apply()
        }

        return vehicles
    }

    fun save(
        list: List<Vehicle>
    ) {

        val array =
            JSONArray()

        list.forEach { vehicle ->

            array.put(
                JSONObject().apply {

                    put(
                        "id",
                        vehicle.id
                    )

                    put(
                        "name",
                        vehicle.name
                    )

                    put(
                        "make",
                        vehicle.make
                    )

                    put(
                        "model",
                        vehicle.model
                    )

                    put(
                        "year",
                        vehicle.year
                    )

                    put(
                        "plate",
                        vehicle.plate
                    )

                    put(
                        "vin",
                        vehicle.vin
                    )

                    put(
                        "imageUri",
                        vehicle.imageUri
                    )
                }
            )
        }

        prefs.edit()
            .putString(
                "vehicles",
                array.toString()
            )
            .apply()
    }

    /*
     * ------------------------------------------------------------
     * BISHERIGE NAMENS-FUNKTIONEN
     * ------------------------------------------------------------
     */

    fun activeVehicle(): String {

        val vehicles =
            load()

        val activeId =
            prefs.getString(
                "activeVehicleId",
                ""
            ) ?: ""

        val activeById =
            vehicles.firstOrNull {
                it.id == activeId
            }

        if (activeById != null) {
            return activeById.name
        }

        return prefs.getString(
            "activeVehicle",
            ""
        ) ?: ""
    }

    fun setActiveVehicle(
        name: String
    ) {

        val vehicles =
            load()

        val vehicle =
            vehicles.firstOrNull {
                it.name == name
            }

        prefs.edit()
            .putString(
                "activeVehicle",
                name
            )
            .apply()

        if (vehicle != null) {

            prefs.edit()
                .putString(
                    "activeVehicleId",
                    vehicle.id
                )
                .apply()
        }
    }

    /*
     * ------------------------------------------------------------
     * ID-BASIERTE AKTIVE FAHRZEUGAUSWAHL
     * ------------------------------------------------------------
     */

    fun activeVehicleId(): String {

        val vehicles =
            load()

        val storedId =
            prefs.getString(
                "activeVehicleId",
                ""
            ) ?: ""

        val validVehicle =
            vehicles.firstOrNull {
                it.id == storedId
            }

        if (validVehicle != null) {
            return validVehicle.id
        }

        if (vehicles.isNotEmpty()) {

            val firstVehicle =
                vehicles.first()

            prefs.edit()
                .putString(
                    "activeVehicleId",
                    firstVehicle.id
                )
                .putString(
                    "activeVehicle",
                    firstVehicle.name
                )
                .apply()

            return firstVehicle.id
        }

        return ""
    }

    fun setActiveVehicleId(
        id: String
    ) {

        val vehicles =
            load()

        val vehicle =
            vehicles.firstOrNull {
                it.id == id
            }

        if (vehicle == null) {
            return
        }

        /*
         * Es existiert immer nur genau EINE globale activeVehicleId.
         */
        prefs.edit()
            .putString(
                "activeVehicleId",
                vehicle.id
            )
            .putString(
                "activeVehicle",
                vehicle.name
            )
            .apply()
    }

    /*
     * ============================================================
     * REPARATUREN
     * ============================================================
     */

    fun loadRepairs(): List<Repair> {

        val array =
            JSONArray(
                prefs.getString(
                    "repairs",
                    "[]"
                ) ?: "[]"
            )

        return buildList {

            for (
                i in 0 until array.length()
            ) {

                val o =
                    array.getJSONObject(i)

                val partsArray =
                    o.optJSONArray(
                        "parts"
                    )

                val parts =
                    buildList {

                        if (partsArray != null) {

                            for (
                                partIndex in
                                0 until partsArray.length()
                            ) {

                                val partObject =
                                    partsArray.optJSONObject(
                                        partIndex
                                    )

                                if (partObject != null) {

                                    add(
                                        RepairPart(
                                            name =
                                                partObject.optString(
                                                    "name"
                                                ),

                                            cost =
                                                partObject.optString(
                                                    "cost"
                                                )
                                        )
                                    )
                                }
                            }
                        }
                    }

                add(
                    Repair(
                        id =
                            o.optLong(
                                "id"
                            ),

                        vehicle =
                            o.optString(
                                "vehicle"
                            ),

                        date =
                            o.optString(
                                "date"
                            ),

                        mileage =
                            o.optString(
                                "mileage"
                            ),

                        description =
                            o.optString(
                                "description"
                            ),

                        repairDescription =
                            o.optString(
                                "repairDescription",
                                ""
                            ),

                        parts =
                            parts,

                        laborCost =
                            o.optString(
                                "laborCost",
                                ""
                            ),

                        cost =
                            o.optString(
                                "cost"
                            ),

                        workshop =
                            o.optString(
                                "workshop"
                            ),

                        beforeImageUri =
                            o.optString(
                                "beforeImageUri",
                                ""
                            ),

                        afterImageUri =
                            o.optString(
                                "afterImageUri",
                                ""
                            )
                    )
                )
            }
        }
    }

    fun saveRepairs(
        list: List<Repair>
    ) {

        val array =
            JSONArray()

        list.forEach { repair ->

            val partsArray =
                JSONArray()

            repair.parts.forEach { part ->

                partsArray.put(
                    JSONObject().apply {

                        put(
                            "name",
                            part.name
                        )

                        put(
                            "cost",
                            part.cost
                        )
                    }
                )
            }

            array.put(
                JSONObject().apply {

                    put(
                        "id",
                        repair.id
                    )

                    put(
                        "vehicle",
                        repair.vehicle
                    )

                    put(
                        "date",
                        repair.date
                    )

                    put(
                        "mileage",
                        repair.mileage
                    )

                    put(
                        "description",
                        repair.description
                    )

                    put(
                        "repairDescription",
                        repair.repairDescription
                    )

                    put(
                        "parts",
                        partsArray
                    )

                    put(
                        "laborCost",
                        repair.laborCost
                    )

                    put(
                        "cost",
                        repair.cost
                    )

                    put(
                        "workshop",
                        repair.workshop
                    )

                    put(
                        "beforeImageUri",
                        repair.beforeImageUri
                    )

                    put(
                        "afterImageUri",
                        repair.afterImageUri
                    )
                }
            )
        }

        prefs.edit()
            .putString(
                "repairs",
                array.toString()
            )
            .apply()
    }

    /*
     * ============================================================
     * WARTUNGEN
     * ============================================================
     */

    fun loadMaintenance(): List<Maintenance> {

        val array =
            JSONArray(
                prefs.getString(
                    "maintenance",
                    "[]"
                ) ?: "[]"
            )

        return buildList {

            for (
                i in 0 until array.length()
            ) {

                val o =
                    array.getJSONObject(i)

                add(
                    Maintenance(
                        id =
                            o.optLong(
                                "id"
                            ),

                        vehicle =
                            o.optString(
                                "vehicle"
                            ),

                        date =
                            o.optString(
                                "date"
                            ),

                        mileage =
                            o.optString(
                                "mileage"
                            ),

                        cost =
                            o.optString(
                                "cost"
                            ),

                        workshop =
                            o.optString(
                                "workshop"
                            ),

                        notes =
                            o.optString(
                                "notes"
                            )
                    )
                )
            }
        }
    }

    fun saveMaintenance(
        list: List<Maintenance>
    ) {

        val array =
            JSONArray()

        list.forEach {

            array.put(
                JSONObject().apply {

                    put(
                        "id",
                        it.id
                    )

                    put(
                        "vehicle",
                        it.vehicle
                    )

                    put(
                        "date",
                        it.date
                    )

                    put(
                        "mileage",
                        it.mileage
                    )

                    put(
                        "cost",
                        it.cost
                    )

                    put(
                        "workshop",
                        it.workshop
                    )

                    put(
                        "notes",
                        it.notes
                    )
                }
            )
        }

        prefs.edit()
            .putString(
                "maintenance",
                array.toString()
            )
            .apply()
    }

    /*
     * ============================================================
     * PICKERL / TÜV
     * ============================================================
     */

    fun loadPickerl(): List<Pickerl> {

        val array =
            JSONArray(
                prefs.getString(
                    "pickerl",
                    "[]"
                ) ?: "[]"
            )

        return buildList {

            for (
                i in 0 until array.length()
            ) {

                val o =
                    array.getJSONObject(i)

                add(
                    Pickerl(
                        vehicle =
                            o.optString(
                                "vehicle"
                            ),

                        lastDate =
                            o.optString(
                                "lastDate"
                            ),

                        nextDate =
                            o.optString(
                                "nextDate"
                            ),

                        notes =
                            o.optString(
                                "notes"
                            ),

                        reminder =
                            o.optBoolean(
                                "reminder"
                            ),

                        photoUri =
                            o.optString(
                                "photoUri",
                                ""
                            ),

                        reminderMonths =
                            o.optInt(
                                "reminderMonths",
                                3
                            ).coerceIn(
                                1,
                                5
                            )
                    )
                )
            }
        }
    }

    fun savePickerl(
        list: List<Pickerl>
    ) {

        val array =
            JSONArray()

        list.forEach {

            array.put(
                JSONObject().apply {

                    put(
                        "vehicle",
                        it.vehicle
                    )

                    put(
                        "lastDate",
                        it.lastDate
                    )

                    put(
                        "nextDate",
                        it.nextDate
                    )

                    put(
                        "notes",
                        it.notes
                    )

                    put(
                        "reminder",
                        it.reminder
                    )

                    put(
                        "photoUri",
                        it.photoUri
                    )

                    put(
                        "reminderMonths",
                        it.reminderMonths
                            .coerceIn(
                                1,
                                5
                            )
                    )
                }
            )
        }

        prefs.edit()
            .putString(
                "pickerl",
                array.toString()
            )
            .apply()
    }

    /*
     * ============================================================
     * SERVICE UND INTERVALLE
     * ============================================================
     */

    fun loadServiceIntervals(): List<de.autocheck.app.data.ServiceInterval> {

        val array =
            JSONArray(
                prefs.getString(
                    "serviceIntervals",
                    "[]"
                ) ?: "[]"
            )

        return buildList {

            for (
                i in 0 until array.length()
            ) {

                val o =
                    array.getJSONObject(i)

                add(
                    de.autocheck.app.data.ServiceInterval(

                        id =
                            o.optLong(
                                "id"
                            ),

                        vehicle =
                            o.optString(
                                "vehicle"
                            ),

                        lastServiceMonth =
                            o.optString(
                                "lastServiceMonth",
                                ""
                            ),

                        lastServiceYear =
                            o.optString(
                                "lastServiceYear",
                                ""
                            ),

                        lastServiceKm =
                            o.optString(
                                "lastServiceKm",
                                ""
                            ),

                        nextServiceMonth =
                            o.optString(
                                "nextServiceMonth",
                                ""
                            ),

                        nextServiceYear =
                            o.optString(
                                "nextServiceYear",
                                ""
                            ),

                        nextServiceKm =
                            o.optString(
                                "nextServiceKm",
                                ""
                            ),

                        documentation =
                            o.optString(
                                "documentation",
                                ""
                            ),

                        cost =
                            o.optString(
                                "cost",
                                ""
                            ),

                        reminderEnabled =
                            o.optBoolean(
                                "reminderEnabled",
                                false
                            ),

                        reminderMonthsBefore =
                            o.optInt(
                                "reminderMonthsBefore",
                                0
                            )
                    )
                )
            }
        }
    }

    fun saveServiceIntervals(
        list: List<de.autocheck.app.data.ServiceInterval>
    ) {

        val array =
            JSONArray()

        list.forEach {

            array.put(
                JSONObject().apply {

                    put(
                        "id",
                        it.id
                    )

                    put(
                        "vehicle",
                        it.vehicle
                    )

                    put(
                        "lastServiceMonth",
                        it.lastServiceMonth
                    )

                    put(
                        "lastServiceYear",
                        it.lastServiceYear
                    )

                    put(
                        "lastServiceKm",
                        it.lastServiceKm
                    )

                    put(
                        "nextServiceMonth",
                        it.nextServiceMonth
                    )

                    put(
                        "nextServiceYear",
                        it.nextServiceYear
                    )

                    put(
                        "nextServiceKm",
                        it.nextServiceKm
                    )

                    put(
                        "documentation",
                        it.documentation
                    )

                    put(
                        "cost",
                        it.cost
                    )

                    put(
                        "reminderEnabled",
                        it.reminderEnabled
                    )

                    put(
                        "reminderMonthsBefore",
                        it.reminderMonthsBefore
                    )
                }
            )
        }

        prefs.edit()
            .putString(
                "serviceIntervals",
                array.toString()
            )
            .apply()
    }

    /*
     * ============================================================
     * REIFEN
     * ============================================================
     */

    fun loadTires(): List<TireSet> {

        val array =
            JSONArray(
                prefs.getString(
                    "tires",
                    "[]"
                ) ?: "[]"
            )

        return buildList {

            for (
                i in 0 until array.length()
            ) {

                val o =
                    array.getJSONObject(i)

                add(
                    TireSet(

                        id =
                            o.optLong(
                                "id"
                            ),

                        vehicle =
                            o.optString(
                                "vehicle"
                            ),

                        season =
                            o.optString(
                                "season"
                            ),

                        rimType =
                            o.optString(
                                "rimType",
                                ""
                            ),

                        dimension =
                            o.optString(
                                "dimension"
                            ),

                        brand =
                            o.optString(
                                "brand"
                            ),

                        dot =
                            o.optString(
                                "dot"
                            ),

                        tread =
                            o.optString(
                                "tread"
                            ),

                        condition =
                            o.optString(
                                "condition"
                            ),

                        storage =
                            o.optString(
                                "storage"
                            ),

                        boltPattern =
                            o.optString(
                                "boltPattern",
                                ""
                            ),

                        offset =
                            o.optString(
                                "offset",
                                ""
                            ),

                        purchaseMonth =
                            o.optString(
                                "purchaseMonth",
                                ""
                            ),

                        purchaseYear =
                            o.optString(
                                "purchaseYear",
                                ""
                            ),

                        price =
                            o.optString(
                                "price",
                                ""
                            )
                    )
                )
            }
        }
    }

    fun saveTires(
        list: List<TireSet>
    ) {

        val array =
            JSONArray()

        list.forEach {

            array.put(
                JSONObject().apply {

                    put(
                        "id",
                        it.id
                    )

                    put(
                        "vehicle",
                        it.vehicle
                    )

                    put(
                        "season",
                        it.season
                    )

                    put(
                        "rimType",
                        it.rimType
                    )

                    put(
                        "dimension",
                        it.dimension
                    )

                    put(
                        "brand",
                        it.brand
                    )

                    put(
                        "dot",
                        it.dot
                    )

                    put(
                        "tread",
                        it.tread
                    )

                    put(
                        "condition",
                        it.condition
                    )

                    put(
                        "storage",
                        it.storage
                    )

                    put(
                        "boltPattern",
                        it.boltPattern
                    )

                    put(
                        "offset",
                        it.offset
                    )

                    put(
                        "purchaseMonth",
                        it.purchaseMonth
                    )

                    put(
                        "purchaseYear",
                        it.purchaseYear
                    )

                    put(
                        "price",
                        it.price
                    )
                }
            )
        }

        prefs.edit()
            .putString(
                "tires",
                array.toString()
            )
            .apply()
    }

    /*
     * ============================================================
     * FAHRZEUGDATEN LÖSCHEN
     * ============================================================
     */

    fun deleteVehicleData(
        name: String
    ) {

        saveRepairs(
            loadRepairs()
                .filterNot {
                    it.vehicle == name
                }
        )

        saveMaintenance(
            loadMaintenance()
                .filterNot {
                    it.vehicle == name
                }
        )

        savePickerl(
            loadPickerl()
                .filterNot {
                    it.vehicle == name
                }
        )

        saveServiceIntervals(
            loadServiceIntervals()
                .filterNot {
                    it.vehicle == name
                }
        )

        saveTires(
            loadTires()
                .filterNot {
                    it.vehicle == name
                }
        )
    }
}
