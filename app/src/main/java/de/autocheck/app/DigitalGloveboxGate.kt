
package de.autocheck.app

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricPrompt.PromptInfo
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

private val GateGold = Color(0xFFD4AF37)
private val GateDark = Color(0xFF101010)

private val AllowedAuthenticators =
    BiometricManager.Authenticators.BIOMETRIC_WEAK or
        BiometricManager.Authenticators.DEVICE_CREDENTIAL

@Composable
fun DigitalGloveboxGate(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity

    var unlocked by remember {
        mutableStateOf(false)
    }

    var authenticating by remember {
        mutableStateOf(false)
    }

    var message by remember {
        mutableStateOf(
            "Bitte authentifizieren, um fortzufahren."
        )
    }

    if (activity == null) {
        GateMessage(
            message = "Die Sicherheitsabfrage konnte nicht gestartet werden.",
            onBack = onBack
        )
        return
    }

    val biometricManager = remember(context) {
        BiometricManager.from(context)
    }

    val promptInfo = remember {
        PromptInfo.Builder()
            .setTitle("CARLOCA – Digitales Handschuhfach")
            .setSubtitle(
                "Mit Fingerabdruck oder Geräte-PIN entsperren"
            )
            .setAllowedAuthenticators(
                AllowedAuthenticators
            )
            .build()
    }

    val prompt = remember(activity) {
        BiometricPrompt(
            activity,
            ContextCompat.getMainExecutor(activity),
            object : BiometricPrompt.AuthenticationCallback() {

                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    super.onAuthenticationSucceeded(result)
                    authenticating = false
                    unlocked = true
                    message = "Erfolgreich entsperrt."
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    message =
                        "Nicht erkannt. Bitte erneut versuchen."
                }

                override fun onAuthenticationError(
                    errorCode: Int,
                    errString: CharSequence
                ) {
                    super.onAuthenticationError(
                        errorCode,
                        errString
                    )

                    authenticating = false
                    message = errString.toString()
                }
            }
        )
    }

    fun authenticate() {
        val result = biometricManager.canAuthenticate(
            AllowedAuthenticators
        )

        if (
            result == BiometricManager.BIOMETRIC_SUCCESS
        ) {
            try {
                authenticating = true
                message = "Sicherheitsabfrage läuft ..."
                prompt.authenticate(promptInfo)
            } catch (_: Exception) {
                authenticating = false
                message =
                    "Die Sicherheitsabfrage konnte nicht geöffnet werden."
            }
        } else {
            authenticating = false

            message = when (result) {
                BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED ->
                    "Bitte richte zuerst einen Fingerabdruck oder eine " +
                        "PIN, ein Muster oder Passwort in deinen " +
                        "Android-Einstellungen ein."

                BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->
                    "Keine geeignete Biometrie-Hardware verfügbar. " +
                        "Bitte prüfe deine Gerätesperre."

                BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->
                    "Die Sicherheits-Hardware ist momentan nicht verfügbar."

                BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED ->
                    "Dein Gerät benötigt ein Sicherheitsupdate."

                else ->
                    "Die Gerätesperre ist nicht verfügbar. " +
                        "Bitte prüfe deine Android-Sicherheitseinstellungen."
            }
        }
    }

    DisposableEffect(activity) {
        val observer = LifecycleEventObserver { _, event ->
            if (
                event == Lifecycle.Event.ON_STOP &&
                unlocked &&
                !authenticating
            ) {
                unlocked = false
                message =
                    "Das Handschuhfach wurde wieder gesperrt."
            }
        }

        activity.lifecycle.addObserver(observer)

        onDispose {
            activity.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(prompt) {
        authenticate()
    }

    if (unlocked) {
        DigitalGloveboxScreen(
            onBack = {
                unlocked = false
                onBack()
            }
        )
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(GateDark)
                .padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "CARLOCA",
                color = GateGold,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(18.dp))

            Text(
                text = "DIGITALES HANDSCHUHFACH",
                color = GateGold,
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(28.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF242424)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🔒",
                        fontSize = 54.sp
                    )

                    Spacer(Modifier.height(16.dp))

                    Text(
                        text = "Geschützter Bereich",
                        color = GateGold,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = "Entsperre das Digitale Handschuhfach " +
                            "mit deinem Fingerabdruck oder ersatzweise " +
                            "mit der PIN, dem Muster oder Passwort " +
                            "deines Smartphones.",
                        color = Color.White,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(18.dp))

                    Text(
                        text = message,
                        color = GateGold,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(18.dp))

                    Button(
                        onClick = {
                            authenticate()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !authenticating
                    ) {
                        Text(
                            if (authenticating) {
                                "Bitte warten ..."
                            } else {
                                "ENTSPERREN"
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("ZURÜCK")
            }
        }
    }
}

@Composable
private fun GateMessage(
    message: String,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GateDark)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(20.dp))

        Button(onClick = onBack) {
            Text("Zurück")
        }
    }
}
