
package de.autocheck.app

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

private fun repairDisplayDate(value: String): String {

    val trimmed = value.trim()

    if (trimmed.isBlank()) return ""

    if (trimmed.matches(Regex("""\d{4}-\d{2}-\d{2}"""))) {
        val parts = trimmed.split("-")
        if (parts.size == 3) {
            return "${parts[2]}.${parts[1]}.${parts[0]}"
        }
    }

    if (trimmed.matches(Regex("""\d{1,2}/\d{1,2}/\d{4}"""))) {
        val parts = trimmed.split("/")
        if (parts.size == 3) {
            val month = parts[0].padStart(2, '0')
            val day = parts[1].padStart(2, '0')
            return "$day.$month.${parts[2]}"
        }
    }

    return trimmed
}

private fun repairMoneyValue(value: String): Double {

    val cleaned = value
        .replace("€", "")
        .replace(" ", "")
        .trim()

    if (cleaned.isBlank()) return 0.0

    return try {
        if (cleaned.contains(",") && cleaned.contains(".")) {
            cleaned.replace(".", "").replace(",", ".").toDouble()
        } else if (cleaned.contains(",")) {
            cleaned.replace(",", ".").toDouble()
        } else {
            cleaned.toDouble()
        }
    } catch (_: NumberFormatException) {
        0.0
    }
}

private fun repairMoneyText(value: Double): String {
    return String.format(Locale.GERMANY, "%.2f €", value)
}

private fun repairPartsTotal(repair: Repair): Double {
    return repair.parts.sumOf {
        repairMoneyValue(it.cost)
    }
}

private fun repairLaborTotal(repair: Repair): Double {

    val newLabor = repairMoneyValue(repair.laborCost)

    if (newLabor > 0.0) return newLabor

    if (repair.parts.isEmpty() && repair.cost.isNotBlank()) {
        return repairMoneyValue(repair.cost)
    }

    return 0.0
}

private fun repairTotal(repair: Repair): Double {

    val parts = repairPartsTotal(repair)
    val labor = repairLaborTotal(repair)

    if (
        repair.parts.isEmpty() &&
        repair.laborCost.isBlank() &&
        repair.cost.isNotBlank()
    ) {
        return repairMoneyValue(repair.cost)
    }

    return parts + labor
}

private fun createCameraUri(context: Context): Uri? {

    val values = android.content.ContentValues().apply {
        put(
            MediaStore.Images.Media.DISPLAY_NAME,
            "AutoCheck_${System.currentTimeMillis()}.jpg"
        )
        put(
            MediaStore.Images.Media.MIME_TYPE,
            "image/jpeg"
        )
    }

    return context.contentResolver.insert(
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        values
    )
}

/**
 * Lädt ein Bild in Supabase hoch, wenn es noch eine lokale URI ist.
 *
 * Bereits gespeicherte Supabase-Pfade werden unverändert übernommen.
 */
private fun uploadRepairImageIfNeeded(
    storage: SupabaseStorage,
    imageUri: String,
    vehicleId: String,
    repairId: Long,
    imageType: String,
    callback: (String?, Exception?) -> Unit
) {

    if (imageUri.isBlank()) {
        callback("", null)
        return
    }

    val parsedUri = Uri.parse(imageUri)

    if (parsedUri.scheme != "content" && parsedUri.scheme != "file") {
        callback(imageUri, null)
        return
    }

    if (vehicleId.isBlank()) {
        callback(
            null,
            IllegalStateException(
                "Die Fahrzeug-ID konnte nicht gefunden werden."
            )
        )
        return
    }

    storage.upload(
        sourceUri = parsedUri,
        category = "repairs",
        vehicleId = vehicleId,
        recordId = "${repairId}_$imageType",
        callback = object : SupabaseStorage.UploadCallback {

            override fun onSuccess(storagePath: String) {
                Handler(Looper.getMainLooper()).post {
                    callback(storagePath, null)
                }
            }

            override fun onError(error: Exception) {
                Handler(Looper.getMainLooper()).post {
                    callback(null, error)
                }
            }
        }
    )
}

/**
 * Lädt ein Reparaturbild aus Supabase oder vom lokalen Gerät.
 */
private suspend fun loadRepairImage(
    context: Context,
    storage: SupabaseStorage,
    imagePath: String
): Bitmap? {

    if (imagePath.isBlank()) return null

    val parsedUri = Uri.parse(imagePath)

    val localUri = when (parsedUri.scheme) {

        "content", "file" -> parsedUri

        else -> {
            val file = File(imagePath)

            if (file.exists() && file.isFile) {
                Uri.fromFile(file)
            } else {
                downloadRepairImage(
                    storage = storage,
                    storagePath = imagePath
                )
            }
        }
    }

    return withContext(Dispatchers.IO) {
        try {
            when (localUri.scheme) {

                "content" -> {
                    context.contentResolver
                        .openInputStream(localUri)
                        ?.use { stream ->
                            BitmapFactory.decodeStream(stream)
                        }
                }

                "file" -> {
                    localUri.path?.let {
                        BitmapFactory.decodeFile(it)
                    }
                }

                else -> null
            }
        } catch (_: Exception) {
            null
        }
    }
}

private suspend fun downloadRepairImage(
    storage: SupabaseStorage,
    storagePath: String
): Uri = suspendCancellableCoroutine { continuation ->

    storage.download(
        storagePath,
        object : SupabaseStorage.DownloadCallback {

            override fun onSuccess(localUri: Uri) {
                if (continuation.isActive) {
                    continuation.resume(localUri)
                }
            }

            override fun onError(error: Exception) {
                if (continuation.isActive) {
                    continuation.resumeWithException(error)
                }
            }
        }
    )
}

@Composable
fun RepairScreen(
    store: VehicleStore,
    vehicles: List<Vehicle>,
    activeVehicle: String,
    onActiveVehicle: (String) -> Unit,
    onVisited: () -> Unit
) {

    val context = androidx.compose.ui.platform.LocalContext.current

    val storage = remember(context) {
        SupabaseStorage(context)
    }

    LaunchedEffect(Unit) {
        onVisited()
    }

    var repairs by remember {
        mutableStateOf(store.loadRepairs())
    }

    var showForm by remember {
        mutableStateOf(false)
    }

    var editingId by remember {
        mutableStateOf<Long?>(null)
    }

    var detailRepair by remember {
        mutableStateOf<Repair?>(null)
    }

    var date by remember { mutableStateOf("") }
    var mileage by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var repairDescription by remember { mutableStateOf("") }
    var laborCost by remember { mutableStateOf("") }
    var workshop by remember { mutableStateOf("") }

    var parts by remember {
        mutableStateOf(emptyList<RepairPart>())
    }

    var newPartName by remember { mutableStateOf("") }
    var newPartCost by remember { mutableStateOf("") }

    var beforeImageUri by remember { mutableStateOf("") }
    var afterImageUri by remember { mutableStateOf("") }

    var cameraTarget by remember {
        mutableStateOf<String?>(null)
    }

    var cameraUri by remember {
        mutableStateOf<Uri?>(null)
    }

    var isSaving by remember {
        mutableStateOf(false)
    }

    var saveError by remember {
        mutableStateOf("")
    }

    val galleryLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.OpenDocument()
        ) { uri ->

            if (uri != null) {

                try {
                    context.contentResolver.takePersistableUriPermission(
                        uri,
                        android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (_: Exception) {
                    // Nicht jeder Anbieter unterstützt persistente Berechtigungen.
                }

                when (cameraTarget) {
                    "before" -> beforeImageUri = uri.toString()
                    "after" -> afterImageUri = uri.toString()
                }
            }

            cameraTarget = null
        }

    val cameraLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.TakePicture()
        ) { success ->

            val uri = cameraUri

            if (success && uri != null) {

                when (cameraTarget) {
                    "before" -> beforeImageUri = uri.toString()
                    "after" -> afterImageUri = uri.toString()
                }

            } else if (uri != null) {

                try {
                    context.contentResolver.delete(uri, null, null)
                } catch (_: Exception) {
                }
            }

            cameraTarget = null
            cameraUri = null
        }

    fun resetForm() {
        editingId = null
        date = ""
        mileage = ""
        description = ""
        repairDescription = ""
        laborCost = ""
        workshop = ""
        parts = emptyList()
        newPartName = ""
        newPartCost = ""
        beforeImageUri = ""
        afterImageUri = ""
        saveError = ""
        isSaving = false
        showForm = false
    }

    fun startNewRepair() {
        editingId = null
        date = ""
        mileage = ""
        description = ""
        repairDescription = ""
        laborCost = ""
        workshop = ""
        parts = emptyList()
        newPartName = ""
        newPartCost = ""
        beforeImageUri = ""
        afterImageUri = ""
        saveError = ""
        showForm = true
    }

    fun startEdit(repair: Repair) {

        editingId = repair.id

        date = repairDisplayDate(repair.date)
        mileage = repair.mileage
        description = repair.description
        repairDescription = repair.repairDescription

        laborCost = repair.laborCost.ifBlank {
            if (repair.parts.isEmpty()) repair.cost else ""
        }

        workshop = repair.workshop
        parts = repair.parts
        beforeImageUri = repair.beforeImageUri
        afterImageUri = repair.afterImageUri

        newPartName = ""
        newPartCost = ""
        saveError = ""
        showForm = true
    }

    /**
     * Speichert die Reparatur erst, wenn beide Bild-Uploads
     * erfolgreich abgeschlossen wurden.
     */
    fun saveRepairWithImages() {

        if (isSaving) return

        saveError = ""

        val vehicle = vehicles.firstOrNull {
            it.name == activeVehicle
        }

        if (vehicle == null) {
            saveError = "Das aktive Fahrzeug wurde nicht gefunden."
            return
        }

        val repairId = editingId ?: System.currentTimeMillis()

        val existingRepair = repairs.firstOrNull {
            it.id == repairId
        }

        val baseRepair = if (existingRepair != null) {

            existingRepair.copy(
                vehicle = activeVehicle,
                date = date.trim(),
                mileage = mileage.trim(),
                description = description.trim(),
                repairDescription = repairDescription.trim(),
                parts = parts,
                laborCost = laborCost.trim(),
                workshop = workshop.trim(),
                beforeImageUri = beforeImageUri,
                afterImageUri = afterImageUri
            )

        } else {

            Repair(
                id = repairId,
                vehicle = activeVehicle,
                date = date.trim(),
                mileage = mileage.trim(),
                description = description.trim(),
                repairDescription = repairDescription.trim(),
                parts = parts,
                laborCost = laborCost.trim(),
                cost = laborCost.trim(),
                workshop = workshop.trim(),
                beforeImageUri = beforeImageUri,
                afterImageUri = afterImageUri
            )
        }

        isSaving = true

        // Zuerst das Vorher-Bild hochladen.
        uploadRepairImageIfNeeded(
            storage = storage,
            imageUri = beforeImageUri,
            vehicleId = vehicle.id,
            repairId = repairId,
            imageType = "before"
        ) { uploadedBefore, beforeError ->

            if (beforeError != null || uploadedBefore == null) {

                isSaving = false
                saveError =
                    "Vorher-Bild konnte nicht hochgeladen werden: " +
                    (beforeError?.message ?: "Unbekannter Fehler")

            } else {

                // Anschließend das Nachher-Bild hochladen.
                uploadRepairImageIfNeeded(
                    storage = storage,
                    imageUri = afterImageUri,
                    vehicleId = vehicle.id,
                    repairId = repairId,
                    imageType = "after"
                ) { uploadedAfter, afterError ->

                    if (afterError != null || uploadedAfter == null) {

                        isSaving = false
                        saveError =
                            "Nachher-Bild konnte nicht hochgeladen werden: " +
                            (afterError?.message ?: "Unbekannter Fehler")

                    } else {

                        val savedRepair = baseRepair.copy(
                            beforeImageUri = uploadedBefore,
                            afterImageUri = uploadedAfter
                        )

                        val updatedRepairs = if (existingRepair != null) {

                            repairs.map {
                                if (it.id == repairId) savedRepair else it
                            }

                        } else {

                            repairs + savedRepair
                        }

                        repairs = updatedRepairs
                        store.saveRepairs(updatedRepairs)

                        resetForm()
                    }
                }
            }
        }
    }

    val list = repairs.filter {
        it.vehicle == activeVehicle
    }

    val totalRepairInvestment = list.sumOf {
        repairTotal(it)
    }

    if (detailRepair != null) {

        val repair = detailRepair!!

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(
                    onClick = {
                        detailRepair = null
                    }
                ) {
                    Text("← Zurück")
                }
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text = repair.description.ifBlank { "Reparatur" },
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    RepairDetailCard(
                        repair = repair,
                        context = context,
                        storage = storage
                    )
                }
            }
        }

        return
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        VehicleSelector(
            vehicles,
            activeVehicle,
            onActiveVehicle
        )

        Spacer(Modifier.height(10.dp))

        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF11141A)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp)
        ) {

            Column(
                modifier = Modifier.padding(16.dp)
            ) {

                Text(
                    "REPARATURHISTORIE",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    "Gesamte bisherige Reparaturkosten",
                    color = Color(0xFFB8BEC8)
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    repairMoneyText(totalRepairInvestment),
                    color = Color(0xFF4A90E2),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (vehicles.isEmpty()) {
            EmptyCard(
                "Bitte zuerst unter „Mein Auto“ ein Fahrzeug anlegen."
            )
            return@Column
        }

        if (showForm) {

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding =
                    androidx.compose.foundation.layout.PaddingValues(
                        bottom = 20.dp
                    )
            ) {

                item {

                    CardForm {

                        Text(
                            text = if (editingId != null) {
                                "Reparatur bearbeiten"
                            } else {
                                "Neue Reparatur"
                            },
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )

                        FormField("Datum (TT.MM.JJJJ)", date) {
                            date = it
                        }

                        FormField("Kilometerstand", mileage) {
                            mileage = it
                        }

                        FormField("Fehler / Reparatur", description) {
                            description = it
                        }

                        FormField(
                            "Durchgeführte Reparatur",
                            repairDescription
                        ) {
                            repairDescription = it
                        }

                        Spacer(Modifier.height(6.dp))

                        Text(
                            "Ersatzteile",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )

                        parts.forEachIndexed { index, part ->

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {

                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {

                                    Text(
                                        part.name.ifBlank { "Ersatzteil" },
                                        color = Color.White
                                    )

                                    Text(
                                        repairMoneyText(
                                            repairMoneyValue(part.cost)
                                        ),
                                        color = Color(0xFFB8BEC8)
                                    )
                                }

                                OutlinedButton(
                                    onClick = {
                                        parts = parts.filterIndexed {
                                            partIndex, _ ->
                                            partIndex != index
                                        }
                                    }
                                ) {
                                    Text("Entfernen")
                                }
                            }
                        }

                        OutlinedTextField(
                            value = newPartName,
                            onValueChange = { newPartName = it },
                            label = { Text("Ersatzteil") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = newPartCost,
                            onValueChange = { newPartCost = it },
                            label = { Text("Ersatzteilkosten") },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Decimal
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedButton(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {

                                if (newPartName.trim().isNotBlank()) {

                                    parts = parts + RepairPart(
                                        name = newPartName.trim(),
                                        cost = newPartCost.trim()
                                    )

                                    newPartName = ""
                                    newPartCost = ""
                                }
                            }
                        ) {
                            Text("Ersatzteil hinzufügen")
                        }

                        FormField(
                            "Einbaukosten / Arbeitskosten",
                            laborCost
                        ) {
                            laborCost = it
                        }

                        FormField("Werkstatt", workshop) {
                            workshop = it
                        }

                        Spacer(Modifier.height(4.dp))

                        Text(
                            "Gesamtkosten: ${
                                repairMoneyText(
                                    parts.sumOf {
                                        repairMoneyValue(it.cost)
                                    } + repairMoneyValue(laborCost)
                                )
                            }",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(Modifier.height(8.dp))

                        Text(
                            "Vorher-Bild",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )

                        RepairImageChooser(
                            imageUri = beforeImageUri,
                            label = "Vorher",
                            context = context,
                            storage = storage,
                            onGallery = {
                                cameraTarget = "before"
                                galleryLauncher.launch(arrayOf("image/*"))
                            },
                            onCamera = {

                                cameraTarget = "before"

                                val uri = createCameraUri(context)
                                cameraUri = uri

                                if (uri != null) {
                                    cameraLauncher.launch(uri)
                                }
                            },
                            onRemove = {
                                beforeImageUri = ""
                            }
                        )

                        Spacer(Modifier.height(8.dp))

                        Text(
                            "Nachher-Bild",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )

                        RepairImageChooser(
                            imageUri = afterImageUri,
                            label = "Nachher",
                            context = context,
                            storage = storage,
                            onGallery = {
                                cameraTarget = "after"
                                galleryLauncher.launch(arrayOf("image/*"))
                            },
                            onCamera = {

                                cameraTarget = "after"

                                val uri = createCameraUri(context)
                                cameraUri = uri

                                if (uri != null) {
                                    cameraLauncher.launch(uri)
                                }
                            },
                            onRemove = {
                                afterImageUri = ""
                            }
                        )

                        Spacer(Modifier.height(10.dp))

                        if (saveError.isNotBlank()) {
                            Text(
                                text = saveError,
                                color = Color(0xFFFF7777)
                            )

                            Spacer(Modifier.height(8.dp))
                        }

                        if (isSaving) {
                            Text(
                                "Bilder werden hochgeladen und Reparatur wird gespeichert …",
                                color = Color(0xFF4A90E2)
                            )

                            Spacer(Modifier.height(8.dp))
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {

                            Button(
                                modifier = Modifier.weight(1f),
                                enabled = !isSaving,
                                onClick = {
                                    saveRepairWithImages()
                                }
                            ) {
                                Text(
                                    if (isSaving) {
                                        "Bitte warten …"
                                    } else if (editingId != null) {
                                        "Änderung speichern"
                                    } else {
                                        "Speichern"
                                    }
                                )
                            }

                            OutlinedButton(
                                modifier = Modifier.weight(1f),
                                enabled = !isSaving,
                                onClick = {
                                    resetForm()
                                }
                            ) {
                                Text("Abbrechen")
                            }
                        }
                    }
                }
            }

        } else {

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding =
                    androidx.compose.foundation.layout.PaddingValues(
                        bottom = 10.dp
                    )
            ) {

                if (list.isEmpty()) {
                    item {
                        EmptyCard("Noch keine Reparatur eingetragen.")
                    }
                }

                items(
                    items = list,
                    key = { it.id }
                ) { repair ->

                    RepairRecordCard(
                        repair = repair,
                        onOpen = {
                            detailRepair = repair
                        },
                        onEdit = {
                            startEdit(repair)
                        },
                        onDelete = {

                            repairs = repairs.filterNot {
                                it.id == repair.id
                            }

                            store.saveRepairs(repairs)
                        }
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                Button(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        startNewRepair()
                    }
                ) {
                    Text("Reparatur hinzufügen")
                }
            }
        }
    }
}

@Composable
private fun RepairRecordCard(
    repair: Repair,
    onOpen: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {

    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF11141A)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Text(
                text = repair.description.ifBlank { "Reparatur" },
                color = Color.White,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable {
                    onOpen()
                }
            )

            Spacer(Modifier.height(8.dp))

            val laborTotal = repairLaborTotal(repair)
            val total = repairTotal(repair)

            if (repair.parts.isNotEmpty()) {

                repair.parts.forEach { part ->

                    Row(
                        modifier = Modifier.fillMaxWidth()
                    ) {

                        Text(
                            text = part.name.ifBlank { "Ersatzteil" },
                            color = Color(0xFFB8BEC8),
                            modifier = Modifier.weight(1f)
                        )

                        Text(
                            text = repairMoneyText(
                                repairMoneyValue(part.cost)
                            ),
                            color = Color.White
                        )
                    }
                }

            } else if (repair.cost.isNotBlank()) {

                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Kosten",
                        color = Color(0xFFB8BEC8),
                        modifier = Modifier.weight(1f)
                    )

                    Text(
                        repair.cost,
                        color = Color.White
                    )
                }
            }

            if (
                laborTotal > 0.0 &&
                (
                    repair.parts.isNotEmpty() ||
                    repair.laborCost.isNotBlank()
                )
            ) {

                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Text(
                        "Einbaukosten",
                        color = Color(0xFFB8BEC8),
                        modifier = Modifier.weight(1f)
                    )

                    Text(
                        repairMoneyText(laborTotal),
                        color = Color.White
                    )
                }
            }

            if (
                repair.parts.isNotEmpty() ||
                repair.laborCost.isNotBlank()
            ) {

                Spacer(Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Text(
                        "Gesamt",
                        color = Color(0xFF4A90E2),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )

                    Text(
                        repairMoneyText(total),
                        color = Color(0xFF4A90E2),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            if (repair.date.isNotBlank()) {
                Text(
                    "Datum: ${repairDisplayDate(repair.date)}",
                    color = Color(0xFF4A90E2)
                )
            }

            if (repair.mileage.isNotBlank()) {
                Text(
                    "Kilometerstand: ${repair.mileage}",
                    color = Color(0xFFB8BEC8)
                )
            }

            if (
                repair.beforeImageUri.isNotBlank() ||
                repair.afterImageUri.isNotBlank()
            ) {
                Text(
                    "Bilder vorhanden – antippen für Details",
                    color = Color(0xFFB8BEC8),
                    modifier = Modifier.clickable {
                        onOpen()
                    }
                )
            }

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    onClick = onOpen
                ) {
                    Text("Details")
                }

                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    onClick = onEdit
                ) {
                    Text("Bearbeiten")
                }

                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    onClick = onDelete
                ) {
                    Text("Löschen")
                }
            }
        }
    }
}

@Composable
private fun RepairDetailCard(
    repair: Repair,
    context: Context,
    storage: SupabaseStorage
) {

    CardForm {

        if (repair.date.isNotBlank()) {
            Text(
                "Datum: ${repairDisplayDate(repair.date)}",
                color = Color(0xFF4A90E2)
            )
        }

        if (repair.mileage.isNotBlank()) {
            Text(
                "Kilometerstand: ${repair.mileage}",
                color = Color(0xFFB8BEC8)
            )
        }

        if (repair.repairDescription.isNotBlank()) {

            Text(
                "Reparatur",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            Text(
                repair.repairDescription,
                color = Color(0xFFB8BEC8)
            )
        }

        if (repair.parts.isNotEmpty()) {

            Spacer(Modifier.height(8.dp))

            Text(
                "Ersatzteile",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            repair.parts.forEach { part ->

                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Text(
                        part.name.ifBlank { "Ersatzteil" },
                        color = Color(0xFFB8BEC8),
                        modifier = Modifier.weight(1f)
                    )

                    Text(
                        repairMoneyText(
                            repairMoneyValue(part.cost)
                        ),
                        color = Color.White
                    )
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        Text(
            "Kosten",
            color = Color.White,
            fontWeight = FontWeight.Bold
        )

        Text(
            "Ersatzteile: ${
                repairMoneyText(repairPartsTotal(repair))
            }",
            color = Color(0xFFB8BEC8)
        )

        Text(
            "Einbaukosten: ${
                repairMoneyText(repairLaborTotal(repair))
            }",
            color = Color(0xFFB8BEC8)
        )

        Text(
            "Gesamt: ${
                repairMoneyText(repairTotal(repair))
            }",
            color = Color(0xFF4A90E2),
            fontWeight = FontWeight.Bold
        )

        if (repair.workshop.isNotBlank()) {
            Text(
                "Werkstatt: ${repair.workshop}",
                color = Color(0xFFB8BEC8)
            )
        }

        Spacer(Modifier.height(12.dp))

        Text(
            "VORHER",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )

        RepairStoredImage(
            context = context,
            storage = storage,
            uriString = repair.beforeImageUri,
            emptyText = "Kein Vorher-Bild vorhanden."
        )

        Spacer(Modifier.height(16.dp))

        Text(
            "NACHHER",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )

        RepairStoredImage(
            context = context,
            storage = storage,
            uriString = repair.afterImageUri,
            emptyText = "Kein Nachher-Bild vorhanden."
        )
    }
}

@Composable
private fun RepairStoredImage(
    context: Context,
    storage: SupabaseStorage,
    uriString: String,
    emptyText: String
) {

    var bitmap by remember(uriString) {
        mutableStateOf<Bitmap?>(null)
    }

    var loading by remember(uriString) {
        mutableStateOf(uriString.isNotBlank())
    }

    LaunchedEffect(uriString) {

        bitmap = null
        loading = uriString.isNotBlank()

        if (uriString.isNotBlank()) {
            bitmap = try {
                loadRepairImage(
                    context = context,
                    storage = storage,
                    imagePath = uriString
                )
            } catch (_: Exception) {
                null
            }
        }

        loading = false
    }

    when {
        bitmap != null -> {
            Image(
                bitmap = bitmap!!.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp),
                contentScale = ContentScale.Fit
            )
        }

        loading -> {
            Text(
                "Bild wird geladen …",
                color = Color(0xFFB8BEC8)
            )
        }

        else -> {
            Text(
                emptyText,
                color = Color(0xFFB8BEC8)
            )
        }
    }
}

@Composable
private fun RepairImageChooser(
    imageUri: String,
    label: String,
    context: Context,
    storage: SupabaseStorage,
    onGallery: () -> Unit,
    onCamera: () -> Unit,
    onRemove: () -> Unit
) {

    var bitmap by remember(imageUri) {
        mutableStateOf<Bitmap?>(null)
    }

    var loading by remember(imageUri) {
        mutableStateOf(imageUri.isNotBlank())
    }

    LaunchedEffect(imageUri) {

        bitmap = null
        loading = imageUri.isNotBlank()

        if (imageUri.isNotBlank()) {
            bitmap = try {
                loadRepairImage(
                    context = context,
                    storage = storage,
                    imagePath = imageUri
                )
            } catch (_: Exception) {
                null
            }
        }

        loading = false
    }

    if (bitmap != null) {

        Image(
            bitmap = bitmap!!.asImageBitmap(),
            contentDescription = label,
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            contentScale = ContentScale.Fit
        )

    } else if (loading) {

        Text(
            "Bild wird geladen …",
            color = Color(0xFFB8BEC8)
        )
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        OutlinedButton(
            modifier = Modifier.weight(1f),
            onClick = onGallery
        ) {
            Text("Galerie")
        }

        OutlinedButton(
            modifier = Modifier.weight(1f),
            onClick = onCamera
        ) {
            Text("Kamera")
        }

        if (imageUri.isNotBlank()) {
            OutlinedButton(
                modifier = Modifier.weight(1f),
                onClick = onRemove
            ) {
                Text("Entfernen")
            }
        }
    }
}
