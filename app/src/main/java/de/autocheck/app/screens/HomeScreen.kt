
package de.autocheck.app

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CarRepair
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.TireRepair
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

private val PremiumGold = Color(0xFFD4AF37)

private const val PROFILE_PREFS = "autocheck"
private const val PROFILE_NAME_KEY = "profile_name"
private const val PROFILE_IMAGE_KEY = "profile_image_path"

@Composable
fun HomeScreen(
    visible: Boolean,
    visitedStore: VisitedStore,
    introEnabled: Boolean,
    onIntroEnabledChange: (Boolean) -> Unit,
    onSelect: (Screen) -> Unit,
    onPremiumClick: () -> Unit
) {
    val menuItems = listOf(
        MenuItemData(
            "Mein Auto",
            "Fahrzeug & Details",
            Icons.Filled.DirectionsCar,
            Color(0xFFE51B2A),
            Screen.AUTO
        ),
        MenuItemData(
            "Reparaturen",
            "Reparaturen verwalten",
            Icons.Filled.CarRepair,
            Color(0xFF1688E8),
            Screen.REPARATUREN
        ),
        MenuItemData(
            "Pickerl/TÜV",
            "Termine & Fristen",
            Icons.Filled.Event,
            Color(0xFF20C75A),
            Screen.PICKERL
        ),
        MenuItemData(
            "Wartungen",
            "Verschiedenes",
            Icons.Filled.Build,
            Color(0xFFE59A18),
            Screen.WARTUNGEN
        ),
        MenuItemData(
            "Service und Intervalle",
            "Service, Intervalle & Erinnerungen",
            Icons.Filled.Build,
            Color(0xFF9A4DFF),
            Screen.SERVICE
        ),
        MenuItemData(
            "Reifen",
            "Größen, Dimensionen und Alter",
            Icons.Filled.TireRepair,
            Color(0xFF17C8BD),
            Screen.REIFEN
        )
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Background),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(
            start = 14.dp,
            end = 14.dp,
            top = 8.dp,
            bottom = 176.dp
        ),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                contentAlignment = Alignment.Center
            ) {
                AnimatedVisibility(
                    visible = visible,
                    enter = slideInVertically(
                        animationSpec = tween(2000),
                        initialOffsetY = { fullHeight -> -fullHeight }
                    ),
                    modifier = Modifier.fillMaxSize()
                ) {
                    Image(
                        painter = painterResource(R.drawable.bild_4),
                        contentDescription = "CARLOCA",
                        modifier = Modifier.fillMaxWidth(),
                        contentScale = ContentScale.Fit
                    )
                }

                // Intro-Schalter links
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 2.dp)
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.linearGradient(
                                listOf(
                                    Color(0xFF5A5A5A),
                                    Color(0xFFE8E8E8),
                                    Color.White,
                                    Color(0xFF8A8A8A),
                                    Color(0xFF4A4A4A)
                                )
                            )
                        )
                        .padding(3.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(
                                if (introEnabled) {
                                    Color(0xFF218838)
                                } else {
                                    Color(0xFFD93025)
                                }
                            )
                            .clickable {
                                onIntroEnabledChange(!introEnabled)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "▶",
                                color = if (introEnabled) {
                                    Color.Black
                                } else {
                                    Color.White
                                },
                                fontSize = 17.sp
                            )

                            Text(
                                text = if (introEnabled) {
                                    "INTRO AN"
                                } else {
                                    "INTRO AUS"
                                },
                                color = if (introEnabled) {
                                    Color.Black
                                } else {
                                    Color.White
                                },
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                maxLines = 1
                            )
                        }
                    }
                }

                // Neues persönliches Profil rechts
                AnimatedVisibility(
                    visible = visible,
                    enter = slideInHorizontally(
                        animationSpec = tween(2000),
                        initialOffsetX = { fullWidth -> fullWidth }
                    ),
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 2.dp)
                ) {
                    CarLocaProfileButton()
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Black
                )
            ) {
                Image(
                    painter = painterResource(R.drawable.bild_3),
                    contentDescription = "CARLOCA Fahrzeug",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(210.dp),
                    contentScale = ContentScale.Crop
                )
            }
        }

        items(menuItems.chunked(2)) { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                row.forEachIndexed { index, item ->
                    AnimatedVisibility(
                        visible = visible,
                        modifier = Modifier.weight(1f),
                        enter = slideInHorizontally(
                            animationSpec = tween(2000),
                            initialOffsetX = { fullWidth ->
                                if (index == 0) -fullWidth else fullWidth
                            }
                        )
                    ) {
                        MenuCard(
                            item = item,
                            visited = visitedStore.isVisited(item.screen),
                            onClick = {
                                onSelect(item.screen)
                            }
                        )
                    }
                }

                if (row.size == 1) {
                    Spacer(
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        item {
            AnimatedVisibility(
                visible = visible,
                enter = slideInVertically(
                    animationSpec = tween(900),
                    initialOffsetY = { height -> height / 2 }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        top = 2.dp,
                        bottom = 4.dp
                    )
            ) {
                PremiumHomeButton(
                    onClick = onPremiumClick
                )
            }
        }
    }
}

/* ============================================================
   PROFILBUTTON UND PROFILVERWALTUNG
   ============================================================ */

@Composable
private fun CarLocaProfileButton() {
    val context = LocalContext.current
    val mainHandler = remember {
        Handler(Looper.getMainLooper())
    }

    val preferences = remember(context) {
        context.getSharedPreferences(
            PROFILE_PREFS,
            Context.MODE_PRIVATE
        )
    }

    val storage = remember(context) {
        SupabaseStorage(context)
    }

    var profileName by remember {
        mutableStateOf(
            preferences.getString(PROFILE_NAME_KEY, "") ?: ""
        )
    }

    var imagePath by remember {
        mutableStateOf(
            preferences.getString(PROFILE_IMAGE_KEY, "") ?: ""
        )
    }

    var profileBitmap by remember {
        mutableStateOf<android.graphics.Bitmap?>(null)
    }

    var selectedImage by remember {
        mutableStateOf<Uri?>(null)
    }

    var selectedBitmap by remember {
        mutableStateOf<android.graphics.Bitmap?>(null)
    }

    var editName by remember {
        mutableStateOf(profileName)
    }

    var showDialog by remember {
        mutableStateOf(false)
    }

    var uploading by remember {
        mutableStateOf(false)
    }

    var errorMessage by remember {
        mutableStateOf<String?>(null)
    }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            selectedImage = uri
            errorMessage = null
        }
    }

    // Gewähltes Bild für die Vorschau laden.
    LaunchedEffect(selectedImage) {
        val uri = selectedImage

        selectedBitmap = if (uri != null) {
            withContext(Dispatchers.IO) {
                try {
                    context.contentResolver
                        .openInputStream(uri)
                        ?.use { input ->
                            BitmapFactory.decodeStream(input)
                        }
                } catch (_: Exception) {
                    null
                }
            }
        } else {
            null
        }
    }

    // Bereits gespeichertes Profilbild aus Supabase laden.
    LaunchedEffect(imagePath) {
        val path = imagePath

        if (path.isBlank()) {
            profileBitmap = null
            return@LaunchedEffect
        }

        storage.download(
            path,
            object : SupabaseStorage.DownloadCallback {
                override fun onSuccess(localUri: Uri) {
                    mainHandler.post {
                        try {
                            val bitmap =
                                context.contentResolver
                                    .openInputStream(localUri)
                                    ?.use { input ->
                                        BitmapFactory.decodeStream(input)
                                    }

                            profileBitmap = bitmap
                        } catch (_: Exception) {
                            profileBitmap = null
                        }
                    }
                }

                override fun onError(error: Exception) {
                    mainHandler.post {
                        profileBitmap = null
                    }
                }
            }
        )
    }

    fun saveProfile() {
        val cleanName = editName.trim()

        if (cleanName.isBlank()) {
            errorMessage = "Bitte gib deinen Namen ein."
            return
        }

        val user = FirebaseAuth.getInstance().currentUser

        if (user == null) {
            errorMessage = "Bitte zuerst anmelden."
            return
        }

        // Nur den Namen speichern, wenn kein neues Bild ausgewählt wurde.
        if (selectedImage == null) {
            preferences.edit()
                .putString(PROFILE_NAME_KEY, cleanName)
                .apply()

            profileName = cleanName
            showDialog = false
            errorMessage = null
            return
        }

        val imageUri = selectedImage ?: return

        uploading = true
        errorMessage = null

        storage.upload(
            sourceUri = imageUri,
            category = "profiles",
            vehicleId = user.uid,
            recordId = "avatar",
            callback = object : SupabaseStorage.UploadCallback {

                override fun onSuccess(storagePath: String) {
                    mainHandler.post {
                        preferences.edit()
                            .putString(PROFILE_NAME_KEY, cleanName)
                            .putString(PROFILE_IMAGE_KEY, storagePath)
                            .apply()

                        profileName = cleanName
                        imagePath = storagePath
                        selectedImage = null
                        selectedBitmap = null
                        uploading = false
                        showDialog = false
                    }
                }

                override fun onError(error: Exception) {
                    mainHandler.post {
                        uploading = false
                        errorMessage =
                            error.localizedMessage
                                ?: "Das Profilbild konnte nicht hochgeladen werden."
                    }
                }
            }
        )
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.widthIn(max = 110.dp)
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color(0xFF5A5A5A),
                            Color(0xFFE8E8E8),
                            Color.White,
                            Color(0xFF8A8A8A),
                            Color(0xFF4A4A4A)
                        )
                    )
                )
                .padding(3.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(PremiumGold)
                    .clickable {
                        editName = profileName
                        selectedImage = null
                        selectedBitmap = null
                        errorMessage = null
                        showDialog = true
                    },
                contentAlignment = Alignment.Center
            ) {
                when {
                    profileBitmap != null -> {
                        Image(
                            bitmap = profileBitmap!!.asImageBitmap(),
                            contentDescription = "Profilbild",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    else -> {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = "Profil erstellen",
                            tint = Color.Black,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }

        if (profileName.isNotBlank()) {
            Text(
                text = profileName,
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = {
                if (!uploading) {
                    showDialog = false
                }
            },
            title = {
                Text("Mein CARLOCA-Profil")
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(112.dp)
                            .clip(CircleShape)
                            .background(PremiumGold)
                            .clickable {
                                if (!uploading) {
                                    imagePicker.launch("image/*")
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        when {
                            selectedBitmap != null -> {
                                Image(
                                    bitmap = selectedBitmap!!.asImageBitmap(),
                                    contentDescription = "Ausgewähltes Profilbild",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }

                            profileBitmap != null -> {
                                Image(
                                    bitmap = profileBitmap!!.asImageBitmap(),
                                    contentDescription = "Aktuelles Profilbild",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }

                            else -> {
                                Icon(
                                    imageVector = Icons.Filled.PhotoCamera,
                                    contentDescription = "Bild auswählen",
                                    tint = Color.Black,
                                    modifier = Modifier.size(42.dp)
                                )
                            }
                        }
                    }

                    TextButton(
                        onClick = {
                            if (!uploading) {
                                imagePicker.launch("image/*")
                            }
                        },
                        enabled = !uploading
                    ) {
                        Text("Profilbild auswählen")
                    }

                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = editName,
                        onValueChange = {
                            editName = it
                        },
                        label = {
                            Text("Dein Name")
                        },
                        singleLine = true,
                        enabled = !uploading,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (uploading) {
                        Spacer(Modifier.height(16.dp))

                        CircularProgressIndicator()

                        Spacer(Modifier.height(8.dp))

                        Text("Profilbild wird hochgeladen …")
                    }

                    if (errorMessage != null) {
                        Spacer(Modifier.height(8.dp))

                        Text(
                            text = errorMessage ?: "",
                            color = Color.Red
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        saveProfile()
                    },
                    enabled = !uploading
                ) {
                    Text("Speichern")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        if (!uploading) {
                            showDialog = false
                        }
                    },
                    enabled = !uploading
                ) {
                    Text("Abbrechen")
                }
            }
        )
    }
}

/* ============================================================
   PREMIUM-BUTTON
   ============================================================ */

@Composable
private fun PremiumHomeButton(
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(15.dp)

    val goldBorder = Brush.linearGradient(
        colors = listOf(
            Color(0xFF6D470D),
            Color(0xFFFFF0B5),
            Color(0xFFD49B24),
            Color(0xFFFFF7D2),
            Color(0xFFB87912),
            Color(0xFFFFE9A0),
            Color(0xFF6D470D)
        )
    )

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .border(
                border = BorderStroke(
                    width = 2.dp,
                    brush = goldBorder
                ),
                shape = shape
            )
            .padding(2.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF090705)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF211707),
                            Color(0xFF080604),
                            Color(0xFF30200A),
                            Color(0xFF080604),
                            Color(0xFF211707)
                        )
                    )
                )
                .padding(horizontal = 9.dp),
            horizontalArrangement = Arrangement.spacedBy(7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(38.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "♛",
                    color = Color(0xFFFFE9A0),
                    fontSize = 33.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )
            }

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(30.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color(0xFFFFE9A0),
                                Color(0xFFD4AF37),
                                Color.Transparent
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "PREMIUM",
                    color = Color(0xFFFFDF80),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.8.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )

                Text(
                    text = "JETZT EXKLUSIVE FUNKTIONEN ENTDECKEN",
                    color = Color(0xFFFFE8B0),
                    fontSize = 7.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.3.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }

            Text(
                text = "❯",
                color = Color(0xFFFFE28A),
                fontSize = 25.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}
