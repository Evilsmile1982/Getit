package de.autocheck.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun DetailScaffold(
    title: String,
    onBack: () -> Unit,
    content: @Composable () -> Unit
) {

    Scaffold(

        containerColor =
            Color(0xFF050608),

        topBar = {

            Box(

                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .background(
                            Color(0xFF050608)
                        )

            ) {

                IconButton(

                    modifier =
                        Modifier.align(
                            Alignment.CenterStart
                        ),

                    onClick = onBack

                ) {

                    Icon(
                        imageVector =
                            Icons.Filled.ArrowBack,
                        contentDescription =
                            "Zurück",
                        tint =
                            Color.White
                    )
                }

                Text(

                    text = title,

                    modifier =
                        Modifier.align(
                            Alignment.Center
                        ),

                    color =
                        Color.White
                )
            }
        }

    ) { padding ->

        Box(

            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)

        ) {

            content()
        }
    }
}
