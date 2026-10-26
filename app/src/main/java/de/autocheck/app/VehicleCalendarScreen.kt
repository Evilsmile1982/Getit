package de.autocheck.app

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.json.JSONArray
import org.json.JSONObject
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

private const val CALENDAR_PREFS = "carvita_year_calendar"
private const val CALENDAR_ENTRIES_KEY = "calendar_entries"

private val calendarGold = Color(0xFFD4AF37)
private val calendarBackground = Color(0xFF151515)
private val calendarCard = Color(0xFF242424)

private data class VehicleCalendarEntry(
    val id: Long,
    val date: String,
    val time: String,
    val title: String,
    val category: String,
    val notes: String
)

@Composable
fun VehicleCalendarScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val preferences = remember(context) {
        context.getSharedPreferences(
            CALENDAR_PREFS,
            Context.MODE_PRIVATE
        )
    }

    val entries = remember {
        mutableStateListOf<VehicleCalendarEntry>().apply {
            try {
                val json = JSONArray(
                    preferences.getString(
                        CALENDAR_ENTRIES_KEY,
                        "[]"
                    ) ?: "[]"
                )

                for (i in 0 until json.length()) {
                    val item = json.optJSONObject(i) ?: continue
                    add(
                        VehicleCalendarEntry(
                            id = item.optLong("id"),
                            date = item.optString("date"),
                            time = item.optString("time"),
                            title = item.optString("title"),
                            category = item.optString("category", "Eigener Termin"),
                            notes = item.optString("notes")
                        )
                    )
                }
            } catch (_: Exception) {
                // Bei beschädigten Daten startet der Kalender leer.
            }
        }
    }

    val today = remember { LocalDate.now() }
    var selectedYear by remember { mutableStateOf(today.year) }
    var selectedMonth by remember {
        mutableStateOf(YearMonth.from(today))
    }
    var selectedDate by remember {
        mutableStateOf(today.toString())
    }
    var showMonthView by remember { mutableStateOf(true) }
    var showEditor by remember { mutableStateOf(false) }
    var editingId by remember { mutableStateOf<Long?>(null) }

    var eventTitle by remember { mutableStateOf("") }
    var eventTime by remember { mutableStateOf("") }
    var eventCategory by remember {
        mutableStateOf("Eigener Termin")
    }
    var eventNotes by remember { mutableStateOf("") }
    var categoryMenuExpanded by remember { mutableStateOf(false) }
    var validationMessage by remember {
        mutableStateOf<String?>(null)
    }

    val categories = listOf(
        "Pickerl / TÜV",
        "Service",
        "Reparatur",
        "Reifenwechsel",
        "Elektro",
        "Eigener Termin"
    )

    fun saveEntries() {
        val array = JSONArray()
        entries.forEach { entry ->
            array.put(
                JSONObject().apply {
                    put("id", entry.id)
                    put("date", entry.date)
                    put("time", entry.time)
                    put("title", entry.title)
                    put("category", entry.category)
                    put("notes", entry.notes)
                }
            )
        }
        preferences.edit()
            .putString(CALENDAR_ENTRIES_KEY, array.toString())
            .apply()
    }

    fun startNewEntry() {
        editingId = null
        eventTitle = ""
        eventTime = ""
        eventCategory = "Eigener Termin"
        eventNotes = ""
        validationMessage = null
        showEditor = true
    }

    fun editEntry(entry: VehicleCalendarEntry) {
        editingId = entry.id
        selectedDate = entry.date
        eventTitle = entry.title
        eventTime = entry.time
        eventCategory = entry.category
        eventNotes = entry.notes
        validationMessage = null
        showEditor = true
    }

    DetailScaffold(
        title = "Kalender",
        onBack = onBack
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedYear.toString(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(onClick = {
                        selectedYear--
                        selectedMonth = YearMonth.of(
                            selectedYear,
                            selectedMonth.monthValue
                        )
                    }) {
                        Text("‹", color = calendarGold)
                    }
                    TextButton(onClick = {
                        selectedYear++
                        selectedMonth = YearMonth.of(
                            selectedYear,
                            selectedMonth.monthValue
                        )
                    }) {
                        Text("›", color = calendarGold)
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { showMonthView = false },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Jahresübersicht")
                }
                Button(
                    onClick = { showMonthView = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Monatsansicht")
                }
            }

            if (!showMonthView) {
                for (row in 0 until 4) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (col in 1..3) {
                            val monthNumber = row * 3 + col
                            val month = YearMonth.of(
                                selectedYear,
                                monthNumber
                            )
                            val hasEntries = entries.any {
                                runCatching {
                                    LocalDate.parse(it.date).year == selectedYear &&
                                        LocalDate.parse(it.date).monthValue == monthNumber
                                }.getOrDefault(false)
                            }

                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        selectedMonth = month
                                        selectedDate = month.atDay(1).toString()
                                        showMonthView = true
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = calendarCard
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 14.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = month.month.getDisplayName(
                                            TextStyle.SHORT,
                                            Locale.GERMAN
                                        ).uppercase(Locale.GERMAN),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (hasEntries) {
                                        Box(
                                            modifier = Modifier
                                                .padding(top = 5.dp)
                                                .size(7.dp)
                                                .background(
                                                    calendarGold,
                                                    RoundedCornerShape(50)
                                                )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                Text(
                    text = "Tippe auf einen Monat, um die Tage zu öffnen.",
                    color = Color.LightGray
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = {
                        selectedMonth = selectedMonth.minusMonths(1)
                        selectedYear = selectedMonth.year
                        selectedDate = selectedMonth.atDay(1).toString()
                    }) {
                        Text("‹", color = calendarGold)
                    }

                    Text(
                        text = selectedMonth.month.getDisplayName(
                            TextStyle.FULL,
                            Locale.GERMAN
                        ).replaceFirstChar {
                            it.titlecase(Locale.GERMAN)
                        } + " ${selectedMonth.year}",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )

                    TextButton(onClick = {
                        selectedMonth = selectedMonth.plusMonths(1)
                        selectedYear = selectedMonth.year
                        selectedDate = selectedMonth.atDay(1).toString()
                    }) {
                        Text("›", color = calendarGold)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    listOf("Mo", "Di", "Mi", "Do", "Fr", "Sa", "So")
                        .forEach { day ->
                            Text(
                                text = day,
                                color = calendarGold,
                                fontWeight = FontWeight.Bold
                            )
                        }
                }

                val firstOffset =
                    selectedMonth.atDay(1).dayOfWeek.value - 1
                val totalCells = ((firstOffset + selectedMonth.lengthOfMonth() + 6) / 7) * 7

                LazyVerticalGrid(
                    columns = GridCells.Fixed(7),
                    modifier = Modifier
                        .fillMaxWidth()
                        .size(
                            width = 1.dp,
                            height = 300.dp
                        ),
                    userScrollEnabled = false
                ) {
                    items(totalCells) { index ->
                        val dayNumber = index - firstOffset + 1
                        if (dayNumber in 1..selectedMonth.lengthOfMonth()) {
                            val date = selectedMonth.atDay(dayNumber)
                            val dateString = date.toString()
                            val dayEntries = entries.filter {
                                it.date == dateString
                            }
                            val isSelected = selectedDate == dateString
                            val isToday = date == today

                            Column(
                                modifier = Modifier
                                    .padding(2.dp)
                                    .background(
                                        when {
                                            isSelected -> calendarGold
                                            isToday -> Color(0xFF45402B)
                                            else -> calendarBackground
                                        },
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        selectedDate = dateString
                                    }
                                    .padding(vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = dayNumber.toString(),
                                    color = if (isSelected) Color.Black else Color.White
                                )
                                if (dayEntries.isNotEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .padding(top = 3.dp)
                                            .size(5.dp)
                                            .background(
                                                if (isSelected) Color.Black else calendarGold,
                                                RoundedCornerShape(50)
                                            )
                                    )
                                } else {
                                    Box(modifier = Modifier.size(8.dp))
                                }
                            }
                        } else {
                            Box(modifier = Modifier.padding(2.dp))
                        }
                    }
                }

                Text(
                    text = runCatching {
                        LocalDate.parse(selectedDate).format(
                            DateTimeFormatter.ofPattern(
                                "EEEE, dd. MMMM yyyy",
                                Locale.GERMAN
                            )
                        )
                    }.getOrDefault(selectedDate),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                Button(
                    onClick = { startNewEntry() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Termin für diesen Tag eintragen")
                }

                val selectedEntries = entries.filter {
                    it.date == selectedDate
                }.sortedBy { it.time }

                if (selectedEntries.isEmpty()) {
                    Text(
                        text = "Keine Termine an diesem Tag.",
                        color = Color.LightGray
                    )
                } else {
                    selectedEntries.forEach { entry ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = calendarCard
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = entry.title,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${entry.category} · ${entry.time.ifBlank { "ohne Uhrzeit" }}",
                                    color = calendarGold
                                )
                                if (entry.notes.isNotBlank()) {
                                    Text(
                                        text = entry.notes,
                                        color = Color.LightGray
                                    )
                                }
                                Row(
                                    horizontalArrangement = Arrangement.End,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    TextButton(onClick = {
                                        editEntry(entry)
                                    }) {
                                        Text("Bearbeiten")
                                    }
                                    TextButton(onClick = {
                                        entries.removeAll { it.id == entry.id }
                                        saveEntries()
                                    }) {
                                        Text("Löschen")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showEditor) {
        AlertDialog(
            onDismissRequest = { showEditor = false },
            title = {
                Text(
                    if (editingId == null) "Neuer Termin" else "Termin bearbeiten"
                )
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = eventTitle,
                        onValueChange = {
                            eventTitle = it
                            validationMessage = null
                        },
                        label = { Text("Titel") },
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = eventTime,
                        onValueChange = { eventTime = it },
                        label = { Text("Uhrzeit (z. B. 09:30)") },
                        singleLine = true
                    )

                    Box {
                        TextButton(
                            onClick = {
                                categoryMenuExpanded = true
                            }
                        ) {
                            Text("Kategorie: $eventCategory")
                        }

                        DropdownMenu(
                            expanded = categoryMenuExpanded,
                            onDismissRequest = {
                                categoryMenuExpanded = false
                            }
                        ) {
                            categories.forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(category) },
                                    onClick = {
                                        eventCategory = category
                                        categoryMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = eventNotes,
                        onValueChange = { eventNotes = it },
                        label = { Text("Notizen (optional)") },
                        minLines = 2
                    )

                    validationMessage?.let {
                        Text(
                            text = it,
                            color = Color(0xFFFF8A80)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (eventTitle.isBlank()) {
                        validationMessage = "Bitte einen Titel eingeben."
                    } else {
                        val entry = VehicleCalendarEntry(
                            id = editingId ?: System.currentTimeMillis(),
                            date = selectedDate,
                            time = eventTime.trim(),
                            title = eventTitle.trim(),
                            category = eventCategory,
                            notes = eventNotes.trim()
                        )

                        if (editingId != null) {
                            val index = entries.indexOfFirst {
                                it.id == editingId
                            }
                            if (index >= 0) {
                                entries[index] = entry
                            } else {
                                entries.add(entry)
                            }
                        } else {
                            entries.add(entry)
                        }

                        saveEntries()
                        showEditor = false
                    }
                }) {
                    Text("Speichern")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditor = false }) {
                    Text("Abbrechen")
                }
            }
        )
    }
}
