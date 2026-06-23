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
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // "X" in bold bright red font
            Text(
                text = "X",
                color = Color(0xFFD32F2F), // Bold textured bright red
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Serif,
                    fontSize = (64 * sizeFraction).sp
                )
            )
            // "HUB" in bold/black font
            Text(
                text = "HUB",
                color = MaterialTheme.colorScheme.onBackground, // Adapts to Dark/Light mode perfectly
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.SansSerif,
                    fontSize = (64 * sizeFraction).sp
                )
            )
        }

        Spacer(modifier = Modifier.height((2 * sizeFraction).dp))

        // Thin, clean, solid horizontal divider line
        Box(
            modifier = Modifier
                .width((200 * sizeFraction).dp)
                .height((1.5f * sizeFraction).dp)
                .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f))
        )

        Spacer(modifier = Modifier.height((6 * sizeFraction).dp))

        // Text "BY MR.UNKNOWN" in clean, elegant, spaced-out sans-serif font
        Text(
            text = "BY MR.UNKNOWN",
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif,
                letterSpacing = (3 * sizeFraction).sp,
                fontSize = (11 * sizeFraction).sp
            )
        )
    }
}
