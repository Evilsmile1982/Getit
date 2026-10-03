package de.autocheck.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
                        .statusBarsPadding()
                        .height(64.dp)
                        .background(
                            Color(0xFF050608)
                        )
            ) {

                /*
                 * Zurück-Button
                 *
                 * Der komplette Bereich ist 48 x 48 dp groß.
                 * Dadurch ist der Button gut mit dem Finger
                 * erreichbar und sitzt sauber in der Toolbar.
                 */
                IconButton(

                    modifier =
                        Modifier
                            .align(
                                Alignment.CenterStart
                            )
                            .padding(
                                start = 12.dp
                            )
                            .size(48.dp),

                    onClick = onBack

                ) {

                    Icon(

                        imageVector =
                            Icons.Filled.ArrowBack,

                        contentDescription =
                            "Zurück zum Hauptmenü",

                        tint =
                            Color.White,

                        modifier =
                            Modifier.size(30.dp)
                    )
                }

                /*
                 * Der Titel wird unabhängig vom Zurück-Button
                 * exakt in der gesamten Bildschirmbreite
                 * zentriert.
                 */
                Text(

                    text = title,

                    modifier =
                        Modifier.align(
                            Alignment.Center
                        ),

                    color =
                        Color.White,

                    fontSize =
                        20.sp,

                    fontWeight =
                        FontWeight.SemiBold
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
