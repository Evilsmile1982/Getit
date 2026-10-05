package de.autocheck.app

data class RepairPart(
    val name: String = "",
    val cost: String = ""
)

data class Repair(
    val id: Long,
    val vehicle: String,
    val date: String,
    val mileage: String,
    val description: String,
    val repairDescription: String = "",
    val parts: List<RepairPart> = emptyList(),
    val laborCost: String = "",
    val cost: String = "",
    val workshop: String = "",
    val beforeImageUri: String = "",
    val afterImageUri: String = ""
)
