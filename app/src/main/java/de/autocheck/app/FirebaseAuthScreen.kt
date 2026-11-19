
package de.autocheck.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth

@Composable
fun FirebaseAuthScreen(
    onAuthenticated: (String) -> Unit
) {
    val auth = remember { FirebaseAuth.getInstance() }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isRegistering by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (isRegistering) {
                "CARLOCA Konto erstellen"
            } else {
                "Bei CARLOCA anmelden"
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("E-Mail-Adresse") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Passwort") },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                message = ""

                if (email.isBlank() || password.length < 6) {
                    message = "Bitte E-Mail und mindestens 6 Zeichen Passwort eingeben."
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
                            message = "Anmeldung konnte nicht abgeschlossen werden."
                        }
                    } else {
                        message = result.exception?.localizedMessage
                            ?: "Anmeldung fehlgeschlagen."
                    }
                }
            },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isLoading) {
                CircularProgressIndicator()
            } else {
                Text(
                    if (isRegistering) {
                        "Konto erstellen"
                    } else {
                        "Anmelden"
                    }
                )
            }
        }

        TextButton(
            onClick = {
                isRegistering = !isRegistering
                message = ""
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                if (isRegistering) {
                    "Ich habe bereits ein Konto"
                } else {
                    "Neues Konto erstellen"
                }
            )
        }

        if (message.isNotBlank()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(message)
        }
    }
}
