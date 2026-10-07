package de.autocheck.app

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CarRepair
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.TireRepair
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.autocheck.app.ui.AutoCheckTheme
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.ZoneId

private const val PURCHASE_CONTRACT_PREFS =
    "carvita_purchase_contract"

private const val PURCHASE_CONTRACT_URI =
    "purchase_contract_uri"

@Composable
fun HomeScreen(
    visible: Boolean,
    visitedStore: VisitedStore,
    onSelect: (Screen) -> Unit
) {

    val context =
        LocalContext.current

    val purchaseContractPreferences =
        remember {
            context.getSharedPreferences(
                PURCHASE_CONTRACT_PREFS,
                Context.MODE_PRIVATE
            )
        }

    var purchaseContractUri by remember {
        mutableStateOf(
            purchaseContractPreferences.getString(
                PURCHASE_CONTRACT_URI,
                null
            )
        )
    }

    var purchaseContractMenuExpanded by remember {
        mutableStateOf(false)
    }

    val purchaseContractPicker =
        androidx.activity.compose.rememberLauncherForActivityResult(
            contract =
                ActivityResultContracts.OpenDocument()
        ) { uri ->

            if (uri != null) {

                try {

                    context.contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )

                } catch (
                    _: SecurityException
                ) {
                    // Manche Anbieter unterstützen keine dauerhafte URI-Freigabe.
                }

                purchaseContractPreferences
                    .edit()
                    .putString(
                        PURCHASE_CONTRACT_URI,
                        uri.toString()
                    )
                    .apply()

                purchaseContractUri =
                    uri.toString()
            }
        }

    val items =
        listOf(

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

    var profileMenuExpanded by remember {
        mutableStateOf(false)
    }

    var profileDialog by remember {
        mutableStateOf<ProfileDialog?>(null)
    }

    LazyColumn(

        modifier =
            Modifier
                .fillMaxSize()
                .background(
                    Background
                ),

        horizontalAlignment =
            Alignment.CenterHorizontally,

        contentPadding =
            androidx.compose.foundation
                .layout
                .PaddingValues(
                    start = 14.dp,
                    end = 14.dp,
                    top = 22.dp,
                    bottom = 24.dp
                ),

        verticalArrangement =
            Arrangement.Center
    ) {

        item {

            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(72.dp)
            ) {

                AnimatedVisibility(

                    visible =
                        visible,

                    enter =
                        slideInVertically(

                            animationSpec =
                                tween(
                                    2000
                                ),

                            initialOffsetY = {
                                fullHeight ->
                                -fullHeight
                            }
                        ),

                    modifier =
                        Modifier.fillMaxSize()
                ) {

                    Image(

                        painter =
                            painterResource(
                                R.drawable.bild_4
                            ),

                        contentDescription =
                            "CARVITA",

                        modifier =
                            Modifier.fillMaxWidth(),

                        contentScale =
                            ContentScale.Fit
                    )
                }

                AnimatedVisibility(

                    visible =
                        visible,

                    enter =
                        slideInHorizontally(

                            animationSpec =
                                tween(
                                    2000
                                ),

                            initialOffsetX = {
                                fullWidth ->
                                -fullWidth
                            }
                        ),

                    modifier =
                        Modifier
                            .align(
                                Alignment.TopStart
                            )
                            .padding(
                                start = 2.dp
                            )
                ) {

                    Box(

                        modifier =
                            Modifier
                                .size(
                                    54.dp
                                )
                                .clip(
                                    CircleShape
                                )
                                .background(

                                    brush =
                                        Brush.linearGradient(
                                            listOf(
                                                Color(0xFF5A0000),
                                                Color(0xFFFF1A1A),
                                                Color(0xFFFF6A6A),
                                                Color(0xFF8B0000),
                                                Color(0xFF3A0000)
                                            )
                                        )
                                )
                                .padding(
                                    3.dp
                                )
                    ) {

                        Box(

                            modifier =
                                Modifier
                                    .fillMaxSize()
                                    .clip(
                                        CircleShape
                                    )
                                    .background(
                                        Color.White
                                    )
                        ) {

                            IconButton(

                                onClick = {

                                    purchaseContractMenuExpanded =
                                        !purchaseContractMenuExpanded
                                },

                                modifier =
                                    Modifier.fillMaxSize()
                            ) {

                                Icon(

                                    imageVector =
                                        Icons.Filled.Download,

                                    contentDescription =
                                        "Kaufvertrag",

                                    tint =
                                        Color.Black,

                                    modifier =
                                        Modifier.size(
                                            28.dp
                                        )
                                )
                            }
                        }

                        DropdownMenu(

                            expanded =
                                purchaseContractMenuExpanded,

                            onDismissRequest = {

                                purchaseContractMenuExpanded =
                                    false
                            }
                        ) {

                            DropdownMenuItem(

                                text = {

                                    Text(
                                        if (
                                            purchaseContractUri ==
                                            null
                                        ) {
                                            "Kaufvertrag hinzufügen"
                                        } else {
                                            "Kaufvertrag öffnen"
                                        }
                                    )
                                },

                                leadingIcon = {

                                    Icon(
                                        Icons.Filled.Download,
                                        contentDescription =
                                            null
                                    )
                                },

                                onClick = {

                                    purchaseContractMenuExpanded =
                                        false

                                    if (
                                        purchaseContractUri ==
                                        null
                                    ) {

                                        purchaseContractPicker
                                            .launch(
                                                arrayOf(
                                                    "application/pdf"
                                                )
                                            )

                                    } else {

                                        val uri =
                                            Uri.parse(
                                                purchaseContractUri
                                            )

                                        val intent =
                                            Intent(
                                                Intent.ACTION_VIEW
                                            ).apply {

                                                setDataAndType(
                                                    uri,
                                                    "application/pdf"
                                                )

                                                addFlags(
                                                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                                                )
                                            }

                                        try {

                                            context.startActivity(
                                                intent
                                            )

                                        } catch (
                                            _: Exception
                                        ) {

                                            purchaseContractPicker
                                                .launch(
                                                    arrayOf(
                                                        "application/pdf"
                                                    )
                                                )
                                        }
                                    }
                                }
                            )

                            if (
                                purchaseContractUri !=
                                null
                            ) {

                                DropdownMenuItem(

                                    text = {

                                        Text(
                                            "Kaufvertrag ersetzen"
                                        )
                                    },

                                    leadingIcon = {

                                        Icon(
                                            Icons.Filled.Download,
                                            contentDescription =
                                                null
                                        )
                                    },

                                    onClick = {

                                        purchaseContractMenuExpanded =
                                            false

                                        purchaseContractPicker
                                            .launch(
                                                arrayOf(
                                                    "application/pdf"
                                                )
                                            )
                                    }
                                )
                            }
                        }
                    }
                }

                AnimatedVisibility(

                    visible =
                        visible,

                    enter =
                        slideInHorizontally(

                            animationSpec =
                                tween(
                                    2000
                                ),

                            initialOffsetX = {
                                fullWidth ->
                                fullWidth
                            }
                        ),

                    modifier =
                        Modifier
                            .align(
                                Alignment.TopEnd
                            )
                            .padding(
                                end = 2.dp
                            )
                ) {

                    Box(

                        modifier =
                            Modifier
                                .size(
                                    54.dp
                                )
                                .clip(
                                    CircleShape
                                )
                                .background(

                                    brush =
                                        Brush.linearGradient(
                                            listOf(
                                                Color(0xFF5A5A5A),
                                                Color(0xFFE8E8E8),
                                                Color(0xFFFFFFFF),
                                                Color(0xFF8A8A8A),
                                                Color(0xFF4A4A4A)
                                            )
                                        )
                                )
                                .padding(
                                    3.dp
                                )
                    ) {

                        Box(

                            modifier =
                                Modifier
                                    .fillMaxSize()
                                    .clip(
                                        CircleShape
                                    )
                                    .background(
                                        Color(0xFFD4AF37)
                                    )
                        ) {

                            IconButton(

                                onClick = {
                                    profileMenuExpanded =
                                        !profileMenuExpanded
                                },

                                modifier =
                                    Modifier.fillMaxSize()
                            ) {

                                Icon(

                                    imageVector =
                                        Icons.Filled.Person,

                                    contentDescription =
                                        "Profil",

                                    tint =
                                        Color.Black,

                                    modifier =
                                        Modifier.size(
                                            28.dp
                                        )
                                )
                            }
                        }

                        DropdownMenu(

                            expanded =
                                profileMenuExpanded,

                            onDismissRequest = {
                                profileMenuExpanded =
                                    false
                            }
                        ) {

                            DropdownMenuItem(

                                text = {
                                    Text(
                                        "Anmelden"
                                    )
                                },

                                leadingIcon = {

                                    Icon(
                                        Icons.Filled.Person,
                                        contentDescription =
                                            null
                                    )
                                },

                                onClick = {

                                    profileMenuExpanded =
                                        false

                                    profileDialog =
                                        ProfileDialog.LOGIN
                                }
                            )

                            DropdownMenuItem(

                                text = {
                                    Text(
                                        "Profil erstellen"
                                    )
                                },

                                leadingIcon = {

                                    Icon(
                                        Icons.Filled.Person,
                                        contentDescription =
                                            null
                                    )
                                },

                                onClick = {

                                    profileMenuExpanded =
                                        false

                                    profileDialog =
                                        ProfileDialog.REGISTER
                                }
                            )
                        }
                    }
                }
            }
        }

        item {

            Card(

                modifier =
                    Modifier
                        .fillMaxWidth(),

                shape =
                    RoundedCornerShape(
                        22.dp
                    ),

                colors =
                    CardDefaults
                        .cardColors(
                            containerColor =
                                Color.Black
                        )
            ) {

                Image(

                    painter =
                        painterResource(
                            R.drawable.bild_3
                        ),

                    contentDescription =
                        "CARVITA Fahrzeug",

                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(
                                245.dp
                            ),

                    contentScale =
                        ContentScale.Crop
                )
            }
        }

        items(
            items.chunked(
                2
            )
        ) { row ->

            Row(

                modifier =
                    Modifier
                        .fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.spacedBy(
                        10.dp
                    )
            ) {

                row.forEach { item ->

                    AnimatedVisibility(

                        visible =
                            visible,

                        modifier =
                            Modifier
                                .weight(
                                    1f
                                ),

                        enter =
                            slideInHorizontally(

                                animationSpec =
                                    tween(
                                        2000
                                    ),

                                initialOffsetX = {
                                    fullWidth ->

                                    if (
                                        row.indexOf(
                                            item
                                        ) == 0
                                    ) {
                                        -fullWidth
                                    } else {
                                        fullWidth
                                    }
                                }
                            )
                    ) {

                        MenuCard(

                            item =
                                item,

                            visited =
                                visitedStore
                                    .isVisited(
                                        item.screen
                                    ),

                            onClick = {
                                onSelect(
                                    item.screen
                                )
                            }
                        )
                    }
                }

                if (
                    row.size == 1
                ) {

                    Spacer(
                        Modifier
                            .weight(
                                1f
                            )
                    )
                }
            }
        }

        item {

            Text(

                "CARVITA – die Geschichte deiner Fahrzeuge",

                color =
                    Muted,

                fontSize =
                    12.sp,

                modifier =
                    Modifier.padding(
                        top = 2.dp
                    )
            )
        }
    }

    when (profileDialog) {

        ProfileDialog.LOGIN -> {

            LoginDialog(

                onDismiss = {
                    profileDialog =
                        null
                }
            )
        }

        ProfileDialog.REGISTER -> {

            RegisterDialog(

                onDismiss = {
                    profileDialog =
                        null
                }
            )
        }

        null -> Unit
    }
}

private enum class ProfileDialog {
    LOGIN,
    REGISTER
}

@Composable
private fun LoginDialog(
    onDismiss: () -> Unit
) {

    var email by remember {
        mutableStateOf("")
    }

    var password by remember {
        mutableStateOf("")
    }

    AlertDialog(

        onDismissRequest =
            onDismiss,

        title = {

            Text(
                "Bei CARVITA anmelden"
            )
        },

        text = {

            Column {

                Text(
                    "Melde dich später mit deinem CARVITA-Konto an, um deine persönlichen Fahrzeugdaten auf verschiedenen Geräten wieder abzurufen.",
                    fontSize = 14.sp
                )

                Spacer(
                    modifier =
                        Modifier.height(
                            16.dp
                        )
                )

                OutlinedTextField(

                    value =
                        email,

                    onValueChange = {
                        email =
                            it
                    },

                    label = {
                        Text(
                            "E-Mail-Adresse"
                        )
                    },

                    singleLine = true,

                    modifier =
                        Modifier.fillMaxWidth()
                )

                Spacer(
                    modifier =
                        Modifier.height(
                            10.dp
                        )
                )

                OutlinedTextField(

                    value =
                        password,

                    onValueChange = {
                        password =
                            it
                    },

                    label = {
                        Text(
                            "Passwort"
                        )
                    },

                    singleLine = true,

                    visualTransformation =
                        PasswordVisualTransformation(),

                    modifier =
                        Modifier.fillMaxWidth()
                )

                Spacer(
                    modifier =
                        Modifier.height(
                            12.dp
                        )
                )

                Text(
                    "Die echte Anmeldung und E-Mail-Verifizierung werden im nächsten Ausbauschritt sicher angebunden.",
                    fontSize = 12.sp,
                    color = Muted
                )
            }
        },

        dismissButton = {

            TextButton(
                onClick =
                    onDismiss
            ) {

                Text(
                    "Abbrechen"
                )
            }
        },

        confirmButton = {

            Button(
                onClick = {
                    onDismiss()
                }
            ) {

                Text(
                    "Weiter"
                )
            }
        }
    )
}

@Composable
private fun RegisterDialog(
    onDismiss: () -> Unit
) {

    var name by remember {
        mutableStateOf("")
    }

    var email by remember {
        mutableStateOf("")
    }

    var password by remember {
        mutableStateOf("")
    }

    AlertDialog(

        onDismissRequest =
            onDismiss,

        title = {

            Text(
                "CARVITA-Profil erstellen"
            )
        },

        text = {

            Column {

                Text(
                    "Dein persönliches Profil wird später mit deinen Fahrzeugdaten verknüpft. Nach der echten Registrierung wird die E-Mail-Adresse verifiziert.",
                    fontSize = 14.sp
                )

                Spacer(
                    modifier =
                        Modifier.height(
                            16.dp
                        )
                )

                OutlinedTextField(

                    value =
                        name,

                    onValueChange = {
                        name =
                            it
                    },

                    label = {
                        Text(
                            "Name"
                        )
                    },

                    singleLine = true,

                    modifier =
                        Modifier.fillMaxWidth()
                )

                Spacer(
                    modifier =
                        Modifier.height(
                            10.dp
                        )
                )

                OutlinedTextField(

                    value =
                        email,

                    onValueChange = {
                        email =
                            it
                    },

                    label = {
                        Text(
                            "E-Mail-Adresse"
                        )
                    },

                    singleLine = true,

                    modifier =
                        Modifier.fillMaxWidth()
                )

                Spacer(
                    modifier =
                        Modifier.height(
                            10.dp
                        )
                )

                OutlinedTextField(

                    value =
                        password,

                    onValueChange = {
                        password =
                            it
                    },

                    label = {
                        Text(
                            "Passwort"
                        )
                    },

                    singleLine = true,

                    visualTransformation =
                        PasswordVisualTransformation(),

                    modifier =
                        Modifier.fillMaxWidth()
                )

                Spacer(
                    modifier =
                        Modifier.height(
                            12.dp
                        )
                )

                Text(
                    "Die sichere Kontoerstellung, E-Mail-Verifizierung und Cloud-Synchronisierung werden im nächsten Ausbauschritt angeschlossen.",
                    fontSize = 12.sp,
                    color = Muted
                )
            }
        },

        dismissButton = {

            TextButton(
                onClick =
                    onDismiss
            ) {

                Text(
                    "Abbrechen"
                )
            }
        },

        confirmButton = {

            Button(
                onClick = {
                    onDismiss()
                }
            ) {

                Text(
                    "Weiter"
                )
            }
        }
    )
}
