package de.autocheck.app

import android.net.Uri
import android.widget.VideoView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun IntroVideoScreen(
    onFinished: () -> Unit
) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { viewContext ->
                VideoView(viewContext).apply {
                    setVideoURI(
                        Uri.parse(
                            "android.resource://${viewContext.packageName}/${R.raw.carvita_intro}"
                        )
                    )

                    setOnPreparedListener { player ->
                        player.isLooping = false
                        start()
                    }

                    setOnCompletionListener {
                        onFinished()
                    }

                    setOnErrorListener { _, _, _ ->
                        onFinished()
                        true
                    }
                }
            }
        )
    }
}
