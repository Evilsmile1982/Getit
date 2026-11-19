
package de.autocheck.app

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.fragment.app.FragmentActivity
import com.google.firebase.auth.FirebaseAuth
import de.autocheck.app.ui.AutoCheckTheme
import de.autocheck.app.utils.scheduleWinterTireReminders

class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createNotificationChannel()

        // Jährliche Winterreifen-Erinnerungen einplanen.
        scheduleWinterTireReminders(applicationContext)

        if (Build.VERSION.SDK_INT >= 33) {
            requestPermissions(
                arrayOf(
                    "android.permission.POST_NOTIFICATIONS"
                ),
                5001
            )
        }

        setContent {
            AutoCheckTheme {
                val firebaseAuth = remember {
                    FirebaseAuth.getInstance()
                }

                var isAuthenticated by remember {
                    mutableStateOf(
                        firebaseAuth.currentUser != null
                    )
                }

                if (isAuthenticated) {
                    AutoCheckApp()
                } else {
                    FirebaseAuthScreen(
                        onAuthenticated = {
                            isAuthenticated = true
                        }
                    )
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
