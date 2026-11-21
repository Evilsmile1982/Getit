
package de.autocheck.app

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth

private val CarLocaGold = Color(0xFFD9B536)
private val CarLocaPurple = Color(0xFF38216F)
private val CarLocaBackground = Color(0xFF080A0E)
private val CarLocaCard = Color(0xFF12151C)
private val CarLocaText = Color(0xFFF4F4F6)
private val CarLocaMuted = Color(0xFFB9BAC3)

@Composable
fun FirebaseAuthScreen(
    onAuthenticated: (String) -> Unit
) {
    val auth = remember {
        FirebaseAuth.getInstance()
    }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isRegistering by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF050609),
                        Color(0xFF11141B),
                        Color(0xFF080A0E)
                    )
                )
            )
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(scrollState)
            .padding(horizontal = 22.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // CARLOCA-Schriftzug und goldene Fahrzeugkontur
        CarLocaLogo()

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Dein Auto. Deine Geschichte.",
            color = CarLocaText,
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Alle Fahrzeuge, Reparaturen und Dokumente\nan einem sicheren Ort.",
            color = CarLocaMuted,
            fontSize = 15.sp,
            lineHeight = 22.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(22.dp))

        // Stilisiertes Fahrzeug ohne Markenemblem
        CarLocaCarIllustration(
            modifier = Modifier
                .fillMaxWidth()
                .height(125.dp)
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Anmeldekarte
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = CarLocaCard,
                    shape = RoundedCornerShape(25.dp)
                )
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isRegistering) {
                    "Werde Teil von CARLOCA"
                } else {
                    "Bitte melde dich an"
                },
                color = CarLocaText,
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (isRegistering) {
                    "Erstelle dein persönliches Konto und verwalte deine Fahrzeuge an einem Ort."
                } else {
                    "Damit deine persönlichen Fahrzeugdaten jederzeit verfügbar sind."
                },
                color = CarLocaMuted,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    message = ""
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("E-Mail-Adresse") },
                placeholder = { Text("Deine E-Mail-Adresse") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email
                ),
                colors = authFieldColors()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    message = ""
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Passwort") },
                placeholder = { Text("Dein Passwort") },
                singleLine = true,
                visualTransformation = if (passwordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                trailingIcon = {
                    IconButton(
                        onClick = {
                            passwordVisible = !passwordVisible
                        }
                    ) {
                        Icon(
                            imageVector = if (passwordVisible) {
                                Icons.Filled.VisibilityOff
                            } else {
                                Icons.Filled.Visibility
                            },
                            contentDescription = if (passwordVisible) {
                                "Passwort verbergen"
                            } else {
                                "Passwort anzeigen"
                            },
                            tint = CarLocaMuted
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password
                ),
                colors = authFieldColors()
            )

            if (!isRegistering) {
                TextButton(
                    onClick = {
                        message = ""

                        if (email.isBlank()) {
                            message = "Bitte zuerst deine E-Mail-Adresse eingeben."
                        } else {
                            isLoading = true

                            auth.sendPasswordResetEmail(email.trim())
                                .addOnCompleteListener { result ->
                                    isLoading = false

                                    message = if (result.isSuccessful) {
                                        "Wir haben dir eine E-Mail zum Zurücksetzen des Passworts gesendet."
                                    } else {
                                        result.exception?.localizedMessage
                                            ?: "Die E-Mail konnte nicht gesendet werden."
                                    }
                                }
                        }
                    },
                    modifier = Modifier.align(Alignment.End),
                    enabled = !isLoading
                ) {
                    Text(
                        text = "Passwort vergessen?",
                        color = CarLocaGold,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            if (message.isNotBlank()) {
                Text(
                    text = message,
                    color = if (
                        message.startsWith("Wir haben dir")
                    ) {
                        CarLocaGold
                    } else {
                        Color(0xFFFF8A80)
                    },
                    fontSize = 13.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    message = ""

                    if (email.isBlank() || password.length < 6) {
                        message =
                            "Bitte E-Mail und mindestens 6 Zeichen Passwort eingeben."
                        return@Button
                    }

                    isLoading = true

                    val task = if (isRegistering) {
                        auth.createUserWithEmailAndPassword(
                            email.trim(),
                            password
                        )
                    } else {
                        auth.signInWithEmailAndPassword(
                            email.trim(),
                            password
                        )
                    }

                    task.addOnCompleteListener { result ->
                        isLoading = false

                        if (result.isSuccessful) {
                            val userId = auth.currentUser?.uid

                            if (userId != null) {
                                onAuthenticated(userId)
                            } else {
                                message =
                                    "Anmeldung konnte nicht abgeschlossen werden."
                            }
                        } else {
                            message =
                                result.exception?.localizedMessage
                                    ?: "Anmeldung fehlgeschlagen."
                        }
                    }
                },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CarLocaGold,
                    contentColor = CarLocaPurple,
                    disabledContainerColor = CarLocaGold.copy(alpha = 0.5f)
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(23.dp),
                        color = CarLocaPurple,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = if (isRegistering) {
                            "Konto erstellen"
                        } else {
                            "Anmelden"
                        },
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "oder",
                color = CarLocaMuted,
                fontSize = 14.sp
            )

            TextButton(
                onClick = {
                    isRegistering = !isRegistering
                    message = ""
                    password = ""
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                Text(
                    text = if (isRegistering) {
                        "Ich habe bereits ein Konto"
                    } else {
                        "Neues Konto erstellen"
                    },
                    color = CarLocaGold,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "CARLOCA – Deine Fahrzeugwelt.",
            color = CarLocaMuted,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun CarLocaLogo() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth(0.78f)
                .height(42.dp)
        ) {
            val path = Path().apply {
                moveTo(size.width * 0.05f, size.height * 0.82f)
                cubicTo(
                    size.width * 0.20f, size.height * 0.10f,
                    size.width * 0.34f, size.height * 0.10f,
                    size.width * 0.48f, size.height * 0.18f
                )
                cubicTo(
                    size.width * 0.62f, size.height * 0.10f,
                    size.width * 0.78f, size.height * 0.10f,
                    size.width * 0.95f, size.height * 0.82f
                )
            }

            drawPath(
                path = path,
                brush = Brush.horizontalGradient(
                    listOf(
                        Color(0xFF9D791D),
                        Color(0xFFFFE58A),
                        CarLocaGold
                    )
                ),
                style = Stroke(
                    width = 4.dp.toPx(),
                    cap = StrokeCap.Round
                )
            )
        }

        Text(
            text = "CARLOCA",
            fontSize = 39.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 2.sp,
            color = Color.White
        )
    }
}

@Composable
private fun CarLocaCarIllustration(
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val gold = CarLocaGold

        // Dezentes Licht hinter dem Fahrzeug
        drawOval(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0x66D9B536),
                    Color.Transparent
                ),
                center = Offset(
                    size.width / 2f,
                    size.height * 0.7f
                ),
                radius = size.width * 0.48f
            ),
            topLeft = Offset(
                size.width * 0.08f,
                size.height * 0.05f
            ),
            size = Size(
                size.width * 0.84f,
                size.height * 0.95f
            )
        )

        // Karosserie-Silhouette
        val body = Path().apply {
            moveTo(size.width * 0.08f, size.height * 0.78f)
            lineTo(size.width * 0.14f, size.height * 0.53f)
            lineTo(size.width * 0.29f, size.height * 0.42f)
            lineTo(size.width * 0.39f, size.height * 0.15f)
            quadraticTo(
                size.width * 0.50f, size.height * 0.07f,
                size.width * 0.61f, size.height * 0.15f
            )
            lineTo(size.width * 0.71f, size.height * 0.42f)
            lineTo(size.width * 0.86f, size.height * 0.53f)
            lineTo(size.width * 0.92f, size.height * 0.78f)
            quadraticTo(
                size.width * 0.50f, size.height * 0.98f,
                size.width * 0.08f, size.height * 0.78f
            )
            close()
        }

        drawPath(
            path = body,
            color = Color(0xFF080A0E),
            style = Stroke(
                width = 3.dp.toPx()
            )
        )

        drawPath(
            path = body,
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF33343A),
                    Color(0xFF090A0D),
                    Color.Black
                )
            )
        )

        // Windschutzscheibe
        val windshield = Path().apply {
            moveTo(size.width * 0.34f, size.height * 0.43f)
            lineTo(size.width * 0.42f, size.height * 0.20f)
            quadraticTo(
                size.width * 0.50f, size.height * 0.15f,
                size.width * 0.58f, size.height * 0.20f
            )
            lineTo(size.width * 0.66f, size.height * 0.43f)
            close()
        }

        drawPath(
            path = windshield,
            color = Color(0xFF10141A)
        )

        drawPath(
            path = windshield,
            color = gold.copy(alpha = 0.65f),
            style = Stroke(width = 2.dp.toPx())
        )

        // Scheinwerfer links und rechts
        drawLine(
            color = Color(0xFFFFE9A0),
            start = Offset(
                size.width * 0.13f,
                size.height * 0.65f
            ),
            end = Offset(
                size.width * 0.29f,
                size.height * 0.62f
            ),
            strokeWidth = 5.dp.toPx(),
            cap = StrokeCap.Round
        )

        drawLine(
            color = Color(0xFFFFE9A0),
            start = Offset(
                size.width * 0.71f,
                size.height * 0.62f
            ),
            end = Offset(
                size.width * 0.87f,
                size.height * 0.65f
            ),
            strokeWidth = 5.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Dezente goldene Kontur
        drawLine(
            color = gold.copy(alpha = 0.8f),
            start = Offset(
                size.width * 0.08f,
                size.height * 0.79f
            ),
            end = Offset(
                size.width * 0.92f,
                size.height * 0.79f
            ),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}

@Composable
private fun authFieldColors() =
    OutlinedTextFieldDefaults.colors(
        focusedTextColor = CarLocaText,
        unfocusedTextColor = CarLocaText,
        focusedBorderColor = CarLocaGold,
        unfocusedBorderColor = Color(0xFF777982),
        focusedLabelColor = CarLocaGold,
        unfocusedLabelColor = CarLocaMuted,
        cursorColor = CarLocaGold,
        focusedPlaceholderColor = CarLocaMuted,
        unfocusedPlaceholderColor = CarLocaMuted
    )
