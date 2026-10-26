
package de.autocheck.app

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
import androidx.compose.material.icons.filled.TireRepair
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val PremiumGold = Color(0xFFD4AF37)

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

    var profileMenuExpanded by remember {
        mutableStateOf(false)
    }

    var profileDialog by remember {
        mutableStateOf<ProfileDialog?>(null)
    }

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
                        contentDescription = "CARVITA",
                        modifier = Modifier.fillMaxWidth(),
                        contentScale = ContentScale.Fit
                    )
                }

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
                                text = if (introEnabled) "INTRO AN" else "INTRO AUS",
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
                    Box(
                        modifier = Modifier
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
                                .background(PremiumGold)
                        ) {
                            IconButton(
                                onClick = {
                                    profileMenuExpanded = !profileMenuExpanded
                                },
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Person,
                                    contentDescription = "Profil",
                                    tint = Color.Black,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = profileMenuExpanded,
                            onDismissRequest = {
                                profileMenuExpanded = false
                            }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Anmelden") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Filled.Person,
                                        contentDescription = null
                                    )
                                },
                                onClick = {
                                    profileMenuExpanded = false
                                    profileDialog = ProfileDialog.LOGIN
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("Profil erstellen") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Filled.Person,
                                        contentDescription = null
                                    )
                                },
                                onClick = {
                                    profileMenuExpanded = false
                                    profileDialog = ProfileDialog.REGISTER
                                }
                            )
                        }
                    }
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
                    contentDescription = "CARVITA Fahrzeug",
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

    when (profileDialog) {
        ProfileDialog.LOGIN -> {
            LoginDialog(
                onDismiss = {
                    profileDialog = null
                }
            )
        }

        ProfileDialog.REGISTER -> {
            RegisterDialog(
                onDismiss = {
                    profileDialog = null
                }
            )
        }

        null -> Unit
    }
}

@Composable
private fun PremiumHomeButton(
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(15.dp)

    val goldBorder = Brush.linearGradient(
        colors = listOf(
            Color(0xFF805B16),
            Color(0xFFFFE9A0),
            Color(0xFFD4AF37),
            Color(0xFFFFF2B0),
            Color(0xFF805B16)
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
            containerColor = Color(0xFF090909)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF17130B),
                            Color(0xFF050505),
                            Color(0xFF17130B)
                        )
                    )
                )
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(38.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "♛",
                    color = Color(0xFFFFE28A),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "PREMIUM",
                    color = Color(0xFFFFD76A),
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.5.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )

                Text(
                    text = "JETZT EXKLUSIVE FUNKTIONEN ENTDECKEN",
                    color = Color(0xFFE7D4A0),
                    fontSize = 7.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.2.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }

            Text(
                text = "❯",
                color = Color(0xFFFFD76A),
                fontSize = 23.sp,
                fontWeight = FontWeight.Black
            )
        }
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
        onDismissRequest = onDismiss,
        title = {
            Text("Bei CARVITA anmelden")
        },
        text = {
            Column {
                Text(
                    "Melde dich später mit deinem CARVITA-Konto an, " +
                        "um deine persönlichen Fahrzeugdaten auf " +
                        "verschiedenen Geräten wieder abzurufen.",
                    fontSize = 14.sp
                )

                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("E-Mail-Adresse") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(10.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Passwort") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    "Die echte Anmeldung und E-Mail-Verifizierung " +
                        "werden im nächsten Ausbauschritt sicher angebunden.",
                    fontSize = 12.sp,
                    color = Muted
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Abbrechen")
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Weiter")
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
        onDismissRequest = onDismiss,
        title = {
            Text("CARVITA-Profil erstellen")
        },
        text = {
            Column {
                Text(
                    "Dein persönliches Profil wird später mit deinen " +
                        "Fahrzeugdaten verknüpft. Nach der echten " +
                        "Registrierung wird die E-Mail-Adresse verifiziert.",
                    fontSize = 14.sp
                )

                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(10.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("E-Mail-Adresse") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(10.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Passwort") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    "Die sichere Kontoerstellung, E-Mail-Verifizierung " +
                        "und Cloud-Synchronisierung werden im nächsten " +
                        "Ausbauschritt angeschlossen.",
                    fontSize = 12.sp,
                    color = Muted
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Abbrechen")
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Weiter")
            }
        }
    )
}
