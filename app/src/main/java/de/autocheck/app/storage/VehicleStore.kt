package de.autocheck.app

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class VehicleStore(
    context: Context
) {

    private val prefs =
        context.getSharedPreferences(
            "autocheck",
            Context.MODE_PRIVATE
        )

    fun load(): List<Vehicle> {

        val array =
            JSONArray(
                prefs.getString(
                    "vehicles",
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
                    Vehicle(
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

    fun activeVehicle(): String =
        prefs.getString(
            "activeVehicle",
            ""
        ) ?: ""

    fun setActiveVehicle(
        name: String
    ) {

        prefs.edit()
            .putString(
                "activeVehicle",
                name
            )
            .apply()
    }

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

                        cost =
                            o.optString(
                                "cost"
                            ),

                        workshop =
                            o.optString(
                                "workshop"
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
                        "description",
                        it.description
                    )

                    put(
                        "cost",
                        it.cost
                    )

                    put(
                        "workshop",
                        it.workshop
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

        saveTires(
            loadTires()
                .filterNot {
                    it.vehicle == name
                }
        )
    }
}
