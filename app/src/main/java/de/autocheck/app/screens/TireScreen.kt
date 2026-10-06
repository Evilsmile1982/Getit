package de.autocheck.app

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val tireMonths =
    (1..12).map {
        it.toString()
    }

private val tireSeasons =
    listOf(
        "Sommer",
        "Winter"
    )

private val tireRimTypes =
    listOf(
        "Alufelgen",
        "Stahlfelgen"
    )

@Composable
fun TireScreen(
    store: VehicleStore,
    vehicles: List<Vehicle>,
    activeVehicle: String,
    onActiveVehicle: (String) -> Unit,
    onVisited: () -> Unit
) {

    LaunchedEffect(
        Unit
    ) {
        onVisited()
    }

    var tires by remember {
        mutableStateOf(
            store.loadTires()
        )
    }

    var showForm by remember {
        mutableStateOf(
            false
        )
    }

    var editingId by remember {
        mutableStateOf<Long?>(
            null
        )
    }

    var season by remember {
        mutableStateOf(
            "Sommer"
        )
    }

    /*
     * ============================================================
     * FELGENART
     * ============================================================
     */

    var rimType by remember {
        mutableStateOf(
            ""
        )
    }

    var dimension by remember {
        mutableStateOf(
            ""
        )
    }

    var brand by remember {
        mutableStateOf(
            ""
        )
    }

    var dot by remember {
        mutableStateOf(
            ""
        )
    }

    var boltPattern by remember {
        mutableStateOf(
            ""
        )
    }

    var offset by remember {
        mutableStateOf(
            ""
        )
    }

    var purchaseMonth by remember {
        mutableStateOf(
            ""
        )
    }

    var purchaseYear by remember {
        mutableStateOf(
            ""
        )
    }

    var price by remember {
        mutableStateOf(
            ""
        )
    }

    var tread by remember {
        mutableStateOf(
            ""
        )
    }

    var condition by remember {
        mutableStateOf(
            ""
        )
    }

    var storage by remember {
        mutableStateOf(
            ""
        )
    }

    val vehicleTires =
        tires.filter {
            it.vehicle ==
                activeVehicle
        }

    val summerTire =
        vehicleTires
            .lastOrNull {
                it.season.equals(
                    "Sommer",
                    ignoreCase =
                        true
                )
            }

    val winterTire =
        vehicleTires
            .lastOrNull {
                it.season.equals(
                    "Winter",
                    ignoreCase =
                        true
                )
            }

    /*
     * ============================================================
     * FORMULAR ZURÜCKSETZEN
     * ============================================================
     */

    fun resetForm() {

        showForm =
            false

        editingId =
            null

        season =
            "Sommer"

        rimType =
            ""

        dimension =
            ""

        brand =
            ""

        dot =
            ""

        boltPattern =
            ""

        offset =
            ""

        purchaseMonth =
            ""

        purchaseYear =
            ""

        price =
            ""

        tread =
            ""

        condition =
            ""

        storage =
            ""
    }

    /*
     * ============================================================
     * NEUEN REIFEN ANLEGEN
     * ============================================================
     */

    fun startNewTire(
        selectedSeason: String
    ) {

        editingId =
            null

        season =
            selectedSeason

        rimType =
            ""

        dimension =
            ""

        brand =
            ""

        dot =
            ""

        boltPattern =
            ""

        offset =
            ""

        purchaseMonth =
            ""

        purchaseYear =
            ""

        price =
            ""

        tread =
            ""

        condition =
            ""

        storage =
            ""

        showForm =
            true
    }

    /*
     * ============================================================
     * REIFEN BEARBEITEN
     * ============================================================
     */

    fun editTire(
        tire: TireSet
    ) {

        editingId =
            tire.id

        season =
            if (
                tire.season.equals(
                    "Winter",
                    ignoreCase =
                        true
                )
            ) {
                "Winter"
            } else {
                "Sommer"
            }

        rimType =
            tire.rimType

        dimension =
            tire.dimension

        brand =
            tire.brand

        dot =
            tire.dot

        boltPattern =
            tire.boltPattern

        offset =
            tire.offset

        purchaseMonth =
            tire.purchaseMonth

        purchaseYear =
            tire.purchaseYear

        price =
            tire.price

        tread =
            tire.tread

        condition =
            tire.condition

        storage =
            tire.storage

        showForm =
            true
    }

    /*
     * ============================================================
     * REIFEN SPEICHERN
     * ============================================================
     */

    fun saveCurrentTire() {

        val newTire =
            TireSet(

                id =
                    editingId
                        ?: System.currentTimeMillis(),

                vehicle =
                    activeVehicle,

                season =
                    season.trim(),

                rimType =
                    rimType.trim(),

                dimension =
                    dimension.trim(),

                brand =
                    brand.trim(),

                dot =
                    dot.trim(),

                tread =
                    tread.trim(),

                condition =
                    condition.trim(),

                storage =
                    storage.trim(),

                boltPattern =
                    boltPattern.trim(),

                offset =
                    offset.trim(),

                purchaseMonth =
                    purchaseMonth.trim(),

                purchaseYear =
                    purchaseYear.trim(),

                price =
                    price.trim()
            )

        val updated =
            if (
                editingId != null
            ) {

                tires.map {
                    existing ->

                    if (
                        existing.id ==
                            editingId
                    ) {

                        newTire

                    } else {

                        existing
                    }
                }

            } else {

                tires
                    .filterNot {

                        it.vehicle ==
                            activeVehicle &&

                        it.season.equals(
                            season,
                            ignoreCase =
                                true
                        )
                    } +
                    newTire
            }

        tires =
            updated

        store.saveTires(
            updated
        )

        resetForm()
    }

    /*
     * ============================================================
     * HAUPTBEREICH
     * ============================================================
     */

    Column(
        modifier =
            Modifier.fillMaxSize()
    ) {

        VehicleSelector(
            vehicles =
                vehicles,

            activeVehicle =
                activeVehicle,

            onSelected =
                onActiveVehicle
        )

        Spacer(
            modifier =
                Modifier.height(
                    10.dp
                )
        )

        if (
            vehicles.isEmpty()
        ) {

            return@Column
        }

        LazyColumn(

            modifier =
                Modifier.weight(
                    1f
                ),

            verticalArrangement =
                Arrangement.spacedBy(
                    12.dp
                )
        ) {

            /*
             * ========================================================
             * REIFENKARTEN
             * ========================================================
             */

            if (
                !showForm
            ) {

                /*
                 * SOMMERREIFEN
                 */

                item {

                    TireSetCard(
                        title =
                            "Sommerreifen",

                        tire =
                            summerTire,

                        imageRes =
                            R.drawable.sommerreifen,

                        onAdd = {

                            startNewTire(
                                "Sommer"
                            )
                        },

                        onEdit = {

                            if (
                                summerTire != null
                            ) {

                                editTire(
                                    summerTire
                                )
                            }
                        },

                        onDelete = {

                            if (
                                summerTire != null
                            ) {

                                val updated =
                                    tires.filterNot {

                                        it.id ==
                                            summerTire.id
                                    }

                                tires =
                                    updated

                                store.saveTires(
                                    updated
                                )
                            }
                        }
                    )
                }

                /*
                 * WINTERREIFEN
                 */

                item {

                    TireSetCard(
                        title =
                            "Winterreifen",

                        tire =
                            winterTire,

                        imageRes =
                            R.drawable.winterreifen,

                        onAdd = {

                            startNewTire(
                                "Winter"
                            )
                        },

                        onEdit = {

                            if (
                                winterTire != null
                            ) {

                                editTire(
                                    winterTire
                                )
                            }
                        },

                        onDelete = {

                            if (
                                winterTire != null
                            ) {

                                val updated =
                                    tires.filterNot {

                                        it.id ==
                                            winterTire.id
                                    }

                                tires =
                                    updated

                                store.saveTires(
                                    updated
                                )
                            }
                        }
                    )
                }

            } else {

                /*
                 * ====================================================
                 * FORMULAR
                 * ====================================================
                 */

                item {

                    TireFormCard(

                        season =
                            season,

                        onSeasonChanged = {
                            season =
                                it
                        },

                        rimType =
                            rimType,

                        onRimTypeChanged = {
                            rimType =
                                it
                        },

                        dimension =
                            dimension,

                        onDimensionChanged = {
                            dimension =
                                it
                        },

                        brand =
                            brand,

                        onBrandChanged = {
                            brand =
                                it
                        },

                        dot =
                            dot,

                        onDotChanged = {
                            dot =
                                it
                        },

                        boltPattern =
                            boltPattern,

                        onBoltPatternChanged = {
                            boltPattern =
                                it
                        },

                        offset =
                            offset,

                        onOffsetChanged = {
                            offset =
                                it
                        },

                        purchaseMonth =
                            purchaseMonth,

                        onPurchaseMonthChanged = {
                            purchaseMonth =
                                it
                        },

                        purchaseYear =
                            purchaseYear,

                        onPurchaseYearChanged = {
                            purchaseYear =
                                it
                        },

                        price =
                            price,

                        onPriceChanged = {
                            price =
                                it
                        },

                        tread =
                            tread,

                        onTreadChanged = {
                            tread =
                                it
                        },

                        condition =
                            condition,

                        onConditionChanged = {
                            condition =
                                it
                        },

                        storage =
                            storage,

                        onStorageChanged = {
                            storage =
                                it
                        },

                        isEditing =
                            editingId != null,

                        onSave = {
                            saveCurrentTire()
                        },

                        onCancel = {
                            resetForm()
                        }
                    )
                }
            }
        }

        /*
         * ========================================================
         * REIFEN HINZUFÜGEN
         * ========================================================
         */

        if (
            !showForm
        ) {

            Button(

                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(
                            top = 8.dp
                        ),

                onClick = {

                    startNewTire(
                        "Sommer"
                    )
                }
            ) {

                Text(
                    "Reifen bearbeiten / hinzufügen"
                )
            }
        }
    }
}

/*
 * ================================================================
 * REIFENKARTE
 * ================================================================
 */

@Composable
private fun TireSetCard(
    title:
        String,

    tire:
        TireSet?,

    imageRes:
        Int,

    onAdd:
        () -> Unit,

    onEdit:
        () -> Unit,

    onDelete:
        () -> Unit
) {

    /*
     * NEU:
     * Menü wird erst geöffnet, wenn der extra Button
     * gedrückt wird.
     */
    var menuExpanded by remember {
        mutableStateOf(
            false
        )
    }

    Card(

        modifier =
            Modifier.fillMaxWidth(),

        colors =
            CardDefaults.cardColors(
                containerColor =
                    Color(
                        0xFF11141A
                    )
            )
    ) {

        Row(

            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        12.dp
                    ),

            horizontalArrangement =
                Arrangement.spacedBy(
                    12.dp
                ),

            verticalAlignment =
                Alignment.Top
        ) {

            Column(

                modifier =
                    Modifier.weight(
                        1f
                    ),

                verticalArrangement =
                    Arrangement.spacedBy(
                        6.dp
                    )
            ) {

                Text(

                    text =
                        title,

                    fontSize =
                        22.sp,

                    fontWeight =
                        FontWeight.Bold,

                    color =
                        if (
                            title.equals(
                                "Sommerreifen",
                                ignoreCase =
                                    true
                            )
                        ) {

                            Color(
                                0xFF4CAF50
                            )

                        } else if (
                            title.equals(
                                "Winterreifen",
                                ignoreCase =
                                    true
                            )
                        ) {

                            Color(
                                0xFF4A90E2
                            )

                        } else {

                            Color.White
                        }
                )

                if (
                    tire == null
                ) {

                    Text(

                        "Noch keine Daten gespeichert.",

                        color =
                            Color(
                                0xFFB8BEC8
                            )
                    )

                    Spacer(
                        modifier =
                            Modifier.height(
                                4.dp
                            )
                    )

                    OutlinedButton(
                        onClick =
                            onAdd
                    ) {

                        Text(
                            "Daten hinzufügen"
                        )
                    }

                } else {

                    /*
                     * ==================================================
                     * FELGENART
                     * ==================================================
                     */

                    TireDataLine(

                        label =
                            "Felgenart",

                        value =
                            tire.rimType
                    )

                    /*
                     * ==================================================
                     * REIFENGRÖSSE
                     * ==================================================
                     */

                    TireDataLine(

                        label =
                            "Größe",

                        value =
                            tire.dimension
                    )

                    TireDataLine(

                        label =
                            "DOT",

                        value =
                            tire.dot
                    )

                    TireDataLine(

                        label =
                            "Lochkreis",

                        value =
                            tire.boltPattern
                    )

                    TireDataLine(

                        label =
                            "Einpresstiefe",

                        value =
                            tire.offset
                    )

                    TireDataLine(

                        label =
                            "Gekauft",

                        value =
                            purchaseDisplay(
                                tire.purchaseMonth,
                                tire.purchaseYear
                            )
                    )

                    TireDataLine(

                        label =
                            "Preis",

                        value =
                            tire.price
                    )

                    if (
                        tire.brand.isNotBlank()
                    ) {

                        TireDataLine(

                            label =
                                "Marke",

                            value =
                                tire.brand
                        )
                    }

                    if (
                        tire.tread.isNotBlank()
                    ) {

                        TireDataLine(

                            label =
                                "Profiltiefe",

                            value =
                                tire.tread
                        )
                    }

                    if (
                        tire.condition.isNotBlank()
                    ) {

                        TireDataLine(

                            label =
                                "Zustand",

                            value =
                                tire.condition
                        )
                    }

                    if (
                        tire.storage.isNotBlank()
                    ) {

                        TireDataLine(

                            label =
                                "Lagerung",

                            value =
                                tire.storage
                        )
                    }

                    Spacer(
                        modifier =
                            Modifier.height(
                                4.dp
                            )
                    )

                    /*
                     * ==================================================
                     * SICHERES BEARBEITEN / LÖSCHEN MENÜ
                     * ==================================================
                     *
                     * Früher waren hier zwei direkt anklickbare
                     * Buttons:
                     *
                     * Ändern | Löschen
                     *
                     * Jetzt gibt es nur noch einen Button.
                     * Erst nach dem Öffnen erscheint das Menü.
                     */

                    Box {

                        OutlinedButton(

                            onClick = {

                                menuExpanded =
                                    true
                            }
                        ) {

                            Text(
                                "☰  Bearbeiten / Löschen"
                            )
                        }

                        DropdownMenu(

                            expanded =
                                menuExpanded,

                            onDismissRequest = {

                                menuExpanded =
                                    false
                            }
                        ) {

                            /*
                             * BEARBEITEN
                             */

                            DropdownMenuItem(

                                text = {
                                    Text(
                                        "Bearbeiten"
                                    )
                                },

                                onClick = {

                                    menuExpanded =
                                        false

                                    onEdit()
                                }
                            )

                            /*
                             * LÖSCHEN
                             */

                            DropdownMenuItem(

                                text = {
                                    Text(
                                        "Löschen"
                                    )
                                },

                                onClick = {

                                    menuExpanded =
                                        false

                                    onDelete()
                                }
                            )
                        }
                    }
                }
            }

            Image(

                painter =
                    painterResource(
                        id =
                            imageRes
                    ),

                contentDescription =
                    title,

                modifier =
                    Modifier.size(
                        width =
                            135.dp,

                        height =
                            190.dp
                    ),

                contentScale =
                    ContentScale.Fit
            )
        }
    }
}

/*
 * ================================================================
 * DATENZEILE
 * ================================================================
 */

@Composable
private fun TireDataLine(
    label:
        String,

    value:
        String
) {

    if (
        value.isBlank()
    ) {
        return
    }

    Row(
        modifier =
            Modifier.fillMaxWidth()
    ) {

        Text(

            text =
                "$label: ",

            fontWeight =
                FontWeight.SemiBold
        )

        Text(
            text =
                value
        )
    }
}

/*
 * ================================================================
 * KAUFDATUM
 * ================================================================
 */

private fun purchaseDisplay(
    month:
        String,

    year:
        String
): String {

    return when {

        month.isNotBlank() &&
            year.isNotBlank() ->

            "$month/$year"

        year.isNotBlank() ->

            year

        month.isNotBlank() ->

            month

        else ->

            ""
    }
}

/*
 * ================================================================
 * REIFEN-FORMULAR
 * ================================================================
 */

@Composable
private fun TireFormCard(

    season:
        String,

    onSeasonChanged:
        (String) -> Unit,

    rimType:
        String,

    onRimTypeChanged:
        (String) -> Unit,

    dimension:
        String,

    onDimensionChanged:
        (String) -> Unit,

    brand:
        String,

    onBrandChanged:
        (String) -> Unit,

    dot:
        String,

    onDotChanged:
        (String) -> Unit,

    boltPattern:
        String,

    onBoltPatternChanged:
        (String) -> Unit,

    offset:
        String,

    onOffsetChanged:
        (String) -> Unit,

    purchaseMonth:
        String,

    onPurchaseMonthChanged:
        (String) -> Unit,

    purchaseYear:
        String,

    onPurchaseYearChanged:
        (String) -> Unit,

    price:
        String,

    onPriceChanged:
        (String) -> Unit,

    tread:
        String,

    onTreadChanged:
        (String) -> Unit,

    condition:
        String,

    onConditionChanged:
        (String) -> Unit,

    storage:
        String,

    onStorageChanged:
        (String) -> Unit,

    isEditing:
        Boolean,

    onSave:
        () -> Unit,

    onCancel:
        () -> Unit
) {

    Card(

        modifier =
            Modifier.fillMaxWidth(),

        colors =
            CardDefaults.cardColors(
                containerColor =
                    Color(
                        0xFF11141A
                    )
            )
    ) {

        Column(

            modifier =
                Modifier.padding(
                    16.dp
                ),

            verticalArrangement =
                Arrangement.spacedBy(
                    10.dp
                )
        ) {

            Text(

                text =
                    if (
                        isEditing
                    ) {

                        "Reifen ändern"

                    } else {

                        "Reifen hinzufügen"
                    },

                fontSize =
                    21.sp,

                fontWeight =
                    FontWeight.Bold
            )

            /*
             * ==================================================
             * REIFENTYP
             * ==================================================
             */

            Text(
                "Reifentyp"
            )

            TireSeasonDropdown(

                season =
                    season,

                onSeasonChanged =
                    onSeasonChanged
            )

            /*
             * ==================================================
             * FELGENART
             * ==================================================
             */

            Text(
                "Felgenart"
            )

            TireRimTypeDropdown(

                rimType =
                    rimType,

                onRimTypeChanged =
                    onRimTypeChanged
            )

            /*
             * ==================================================
             * REIFENGRÖSSE
             * ==================================================
             */

            TireTextField(

                label =
                    "Größe",

                value =
                    dimension,

                placeholder =
                    "205/55 R16",

                onValueChanged =
                    onDimensionChanged
            )

            /*
             * ==================================================
             * MARKE
             * ==================================================
             */

            TireTextField(

                label =
                    "Marke",

                value =
                    brand,

                placeholder =
                    "z. B. Michelin",

                onValueChanged =
                    onBrandChanged
            )

            /*
             * ==================================================
             * DOT
             * ==================================================
             */

            TireTextField(

                label =
                    "DOT",

                value =
                    dot,

                placeholder =
                    "z. B. 2424",

                onValueChanged =
                    onDotChanged
            )

            /*
             * ==================================================
             * LOCHKREIS
             * ==================================================
             */

            TireTextField(

                label =
                    "Lochkreis",

                value =
                    boltPattern,

                placeholder =
                    "z. B. 5x112",

                onValueChanged =
                    onBoltPatternChanged
            )

            /*
             * ==================================================
             * EINPRESSTIEFE
             * ==================================================
             */

            TireTextField(

                label =
                    "Einpresstiefe",

                value =
                    offset,

                placeholder =
                    "z. B. ET45",

                onValueChanged =
                    onOffsetChanged
            )

            /*
             * ==================================================
             * GEKAUFT
             * ==================================================
             */

            Text(
                "Gekauft"
            )

            Row(

                modifier =
                    Modifier.fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.spacedBy(
                        8.dp
                    )
            ) {

                TireMonthDropdown(

                    modifier =
                        Modifier.weight(
                            1f
                        ),

                    month =
                        purchaseMonth,

                    onMonthChanged =
                        onPurchaseMonthChanged
                )

                OutlinedTextField(

                    value =
                        purchaseYear,

                    onValueChange = {
                        value ->

                        onPurchaseYearChanged(

                            value
                                .filter {
                                    it.isDigit()
                                }
                                .take(
                                    4
                                )
                        )
                    },

                    modifier =
                        Modifier.weight(
                            1f
                        ),

                    label = {
                        Text(
                            "Jahr"
                        )
                    },

                    placeholder = {
                        Text(
                            "2025"
                        )
                    },

                    singleLine =
                        true
                )
            }

            /*
             * ==================================================
             * PREIS
             * ==================================================
             */

            TireTextField(

                label =
                    "Preis",

                value =
                    price,

                placeholder =
                    "z. B. 450 €",

                onValueChanged =
                    onPriceChanged
            )

            /*
             * ==================================================
             * PROFILTIEFE
             * ==================================================
             */

            TireTextField(

                label =
                    "Profiltiefe",

                value =
                    tread,

                placeholder =
                    "z. B. 6 mm",

                onValueChanged =
                    onTreadChanged
            )

            /*
             * ==================================================
             * ZUSTAND
             * ==================================================
             */

            TireTextField(

                label =
                    "Zustand",

                value =
                    condition,

                placeholder =
                    "z. B. sehr gut",

                onValueChanged =
                    onConditionChanged
            )

            /*
             * ==================================================
             * LAGERUNG
             * ==================================================
             */

            TireTextField(

                label =
                    "Lagerung",

                value =
                    storage,

                placeholder =
                    "z. B. Keller / Reifenhotel",

                onValueChanged =
                    onStorageChanged
            )

            /*
             * ==================================================
             * ABBRECHEN / SPEICHERN
             * ==================================================
             */

            Row(

                modifier =
                    Modifier.fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.spacedBy(
                        8.dp
                    )
            ) {

                OutlinedButton(

                    modifier =
                        Modifier.weight(
                            1f
                        ),

                    onClick =
                        onCancel
                ) {

                    Text(
                        "Abbrechen"
                    )
                }

                Button(

                    modifier =
                        Modifier.weight(
                            1f
                        ),

                    onClick =
                        onSave
                ) {

                    Text(

                        if (
                            isEditing
                        ) {

                            "Änderung speichern"

                        } else {

                            "Speichern"
                        }
                    )
                }
            }
        }
    }
}

/*
 * ================================================================
 * TEXTFELD
 * ================================================================
 */

@Composable
private fun TireTextField(

    label:
        String,

    value:
        String,

    placeholder:
        String,

    onValueChanged:
        (String) -> Unit
) {

    OutlinedTextField(

        value =
            value,

        onValueChange =
            onValueChanged,

        modifier =
            Modifier.fillMaxWidth(),

        label = {
            Text(
                label
            )
        },

        placeholder = {
            Text(
                placeholder
            )
        },

        singleLine =
            true
    )
}

/*
 * ================================================================
 * REIFENTYP AUSWAHL
 * ================================================================
 */

@Composable
private fun TireSeasonDropdown(

    season:
        String,

    onSeasonChanged:
        (String) -> Unit
) {

    var expanded by remember {
        mutableStateOf(
            false
        )
    }

    Box(
        modifier =
            Modifier.fillMaxWidth()
    ) {

        OutlinedButton(

            modifier =
                Modifier.fillMaxWidth(),

            onClick = {
                expanded =
                    true
            }
        ) {

            Text(
                season
            )
        }

        DropdownMenu(

            expanded =
                expanded,

            onDismissRequest = {
                expanded =
                    false
            }
        ) {

            tireSeasons.forEach {
                option ->

                DropdownMenuItem(

                    text = {
                        Text(
                            option
                        )
                    },

                    onClick = {

                        onSeasonChanged(
                            option
                        )

                        expanded =
                            false
                    }
                )
            }
        }
    }
}

/*
 * ================================================================
 * FELGENART AUSWAHL
 * ================================================================
 */

@Composable
private fun TireRimTypeDropdown(

    rimType:
        String,

    onRimTypeChanged:
        (String) -> Unit
) {

    var expanded by remember {
        mutableStateOf(
            false
        )
    }

    Box(
        modifier =
            Modifier.fillMaxWidth()
    ) {

        OutlinedButton(

            modifier =
                Modifier.fill
