
package de.autocheck.app

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import com.google.firebase.auth.FirebaseAuth
import de.autocheck.app.ui.AutoCheckTheme
import de.autocheck.app.utils.scheduleWinterTireReminders

class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createNotificationChannel()
        scheduleWinterTireReminders(applicationContext)

        if (Build.VERSION.SDK_INT >= 33) {
            requestPermissions(
                arrayOf("android.permission.POST_NOTIFICATIONS"),
                5001
            )
        }

        setContent {
            AutoCheckTheme {
                val firebaseAuth = remember {
                    FirebaseAuth.getInstance()
                }

                val backup = remember {
                    SupabaseBackup(applicationContext)
                }

                var isAuthenticated by remember {
                    mutableStateOf(firebaseAuth.currentUser != null)
                }

                var backupReady by remember {
                    mutableStateOf(false)
                }

                var backupError by remember {
                    mutableStateOf<String?>(null)
                }

                LaunchedEffect(isAuthenticated) {
                    backupReady = false
                    backupError = null

                    if (isAuthenticated) {
                        backup.start(
                            onReady = {
                                backupReady = true
                            },
                            onError = { error ->
                                backupError =
                                    error.localizedMessage
                                        ?: "Supabase-Sicherung fehlgeschlagen"
                            }
                        )
                    }
                }

                when {
                    !isAuthenticated -> {
                        FirebaseAuthScreen(
                            onAuthenticated = {
                                isAuthenticated = true
                            }
                        )
                    }

                    backupReady -> {
                        AutoCheckApp()
                    }

                    backupError != null -> {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text("Cloud-Sicherung fehlgeschlagen:")
                            Text(backupError ?: "")
                        }
                    }

                    else -> {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator()
                            Text("CARLOCA-Daten werden geladen …")
                        }
                    }
                }
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "pickerl_reminders",
                "Pickerl Erinnerungen",
                NotificationManager.IMPORTANCE_DEFAULT
            )

            getSystemService(
                NotificationManager::class.java
            ).createNotificationChannel(channel)
        }
    }
}
