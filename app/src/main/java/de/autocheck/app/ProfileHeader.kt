
package de.autocheck.app

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.InputStream
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

private const val PROFILE_PREFS = "autocheck"
private const val PROFILE_NAME_KEY = "profile_name"
private const val PROFILE_IMAGE_KEY = "profile_image_path"

@Composable
fun CarLocaProfileButton() {
    val context = LocalContext.current
    val preferences = remember {
        context.getSharedPreferences(
            PROFILE_PREFS,
            android.content.Context.MODE_PRIVATE
        )
    }

    val storage = remember {
        SupabaseStorage(context)
    }

    val scope = rememberCoroutineScope()

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

    var selectedImage by remember {
        mutableStateOf<Uri?>(null)
    }

    var downloadedBitmap by remember {
        mutableStateOf<Bitmap?>(null)
    }

    var showDialog by remember {
        mutableStateOf(false)
    }

    var editName by remember {
        mutableStateOf(profileName)
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
            downloadedBitmap = null
            errorMessage = null
        }
    }

    LaunchedEffect(imagePath) {
        downloadedBitmap = null

        if (imagePath.isBlank()) return@LaunchedEffect

        try {
            val localUri = suspendCancellableCoroutine<Uri> { continuation ->
                storage.download(
                    imagePath,
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

            val bitmap = withContext(Dispatchers.IO) {
                context.contentResolver.openInputStream(localUri)?.use {
                    BitmapFactory.decodeStream(it)
                }
            }

            downloadedBitmap = bitmap
        } catch (_: Exception) {
            downloadedBitmap = null
        }
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

        uploading = true
        errorMessage = null

        if (selectedImage == null) {
            preferences.edit()
                .putString(PROFILE_NAME_KEY, cleanName)
                .apply()

            profileName = cleanName
            showDialog = false
            uploading = false
            return
        }

        val imageUri = selectedImage ?: return

        storage.upload(
            sourceUri = imageUri,
            category = "profiles",
            vehicleId = user.uid,
            recordId = "avatar",
            callback = object : SupabaseStorage.UploadCallback {
                override fun onSuccess(storagePath: String) {
                    preferences.edit()
                        .putString(PROFILE_NAME_KEY, cleanName)
                        .putString(PROFILE_IMAGE_KEY, storagePath)
                        .apply()

                    profileName = cleanName
                    imagePath = storagePath
                    selectedImage = null
                    downloadedBitmap = null
                    uploading = false
                    showDialog = false
                }

                override fun onError(error: Exception) {
                    uploading = false
                    errorMessage =
                        error.localizedMessage
                            ?: "Das Profilbild konnte nicht hochgeladen werden."
                }
            }
        )
    }

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
                .background(Color(0xFFD4AF37))
                .clickable {
                    editName = profileName
                    selectedImage = null
                    errorMessage = null
                    showDialog = true
                },
            contentAlignment = Alignment.Center
        ) {
            val previewBitmap = downloadedBitmap

            when {
                selectedImage != null -> {
                    // Die neu ausgewählte Datei wird im Dialog angezeigt.
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = "Profil bearbeiten",
                        tint = Color.Black,
                        modifier = Modifier.size(28.dp)
                    )
                }

                previewBitmap != null -> {
                    Image(
                        bitmap = previewBitmap.asImageBitmap(),
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

    if (showDialog) {
        AlertDialog(
            onDismissRequest = {
                if (!uploading) showDialog = false
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
                            .background(Color(0xFFD4AF37))
                            .clickable {
                                if (!uploading) {
                                    imagePicker.launch("image/*")
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        when {
                            selectedImage != null -> {
                                Text(
                                    "Bild ausgewählt",
                                    color = Color.Black
                                )
                            }

                            downloadedBitmap != null -> {
                                Image(
                                    bitmap = downloadedBitmap!!.asImageBitmap(),
                                    contentDescription = "Profilbild",
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
                        }
                    ) {
                        Text("Profilbild auswählen")
                    }

                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Dein Name") },
                        singleLine = true,
                        enabled = !uploading,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (uploading) {
                        Spacer(Modifier.height(16.dp))
                        CircularProgressIndicator()
                        Text("Profil wird gespeichert …")
                    }

                    errorMessage?.let {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { saveProfile() },
                    enabled = !uploading
                ) {
                    Text("Speichern")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        if (!uploading) showDialog = false
                    },
                    enabled = !uploading
                ) {
                    Text("Abbrechen")
                }
            }
        )
    }
}
