
package de.autocheck.app

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
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

        CarLocaLogo()

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Dein Auto. Deine Geschichte.",
            color = CarLocaText,
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Alle Fahrzeuge, Reparaturen und Dokumente\nan einem sicheren Ort.",
            color = CarLocaMuted,
            fontSize = 15.sp,
            lineHeight = 22.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Neues CARLOCA-Fahrzeugbild
        Image(
            painter = painterResource(
                id = R.drawable.carloca_login_car
            ),
            contentDescription = "Schwarzes CARLOCA-Fahrzeug mit goldener Beleuchtung",
            modifier = Modifier
                .fillMaxWidth()
                .height(165.dp),
            contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.height(12.dp))

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
                textAlign = TextAlign.Center
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
                textAlign = TextAlign.Center
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
                            message =
                                "Bitte zuerst deine E-Mail-Adresse eingeben."
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
                    color = if (message.startsWith("Wir haben dir")) {
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

        Spacer(modifier = Modifier.height(24.dp))
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
