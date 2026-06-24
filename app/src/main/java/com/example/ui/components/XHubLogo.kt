package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun XHubCleanLogo(
    modifier: Modifier = Modifier,
    sizeFraction: Float = 1.0f
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // "X" in bold bright red font
            Text(
                text = "X",
                color = Color(0xFFD32F2F), // Textured/bright security red
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.SansSerif,
                    fontSize = (54 * sizeFraction).sp
                )
            )
            // "HUB" in bold/black font adapting to system theme
            Text(
                text = "HUB",
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.SansSerif,
                    fontSize = (54 * sizeFraction).sp
                )
            )
        }

        Spacer(modifier = Modifier.height((2 * sizeFraction).dp))

        // Divider line
        Box(
            modifier = Modifier
                .width((140 * sizeFraction).dp)
                .height((2 * sizeFraction).dp)
                .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
        )

        Spacer(modifier = Modifier.height((4 * sizeFraction).dp))

        // "BY MR.UNKNOWN" in capital spaced-out letters
        Text(
            text = "BY MR.UNKNOWN",
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = (2.5 * sizeFraction).sp,
                fontSize = (10 * sizeFraction).sp
            )
        )
    }
}

