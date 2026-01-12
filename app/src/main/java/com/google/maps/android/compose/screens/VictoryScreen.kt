package com.google.maps.android.compose.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun VictoryScreen(visible: Boolean, onNavigateToProfile: () -> Unit) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + scaleIn(),
        exit = fadeOut(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xCC000000)) // Semi-transparent background
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .background(Color(0xFF1FA4A4), RoundedCornerShape(16.dp))
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("🎉 Congratz! 🎉", fontSize = 32.sp, color = Color.Black)
                Spacer(modifier = Modifier.height(16.dp))

                // Display money earned
                Text("Money earned:", fontSize = 20.sp, color = Color.Black)
                Text("19 LEI", fontSize = 28.sp, color = Color.Green)

                Spacer(modifier = Modifier.height(8.dp))

                // Display time spent
                Text("Total time:", fontSize = 20.sp, color = Color.Black)
                Text("25 minute", fontSize = 28.sp, color = Color.Cyan)

                Spacer(modifier = Modifier.height(8.dp))

                // Display time spent
                Text("People helped:", fontSize = 20.sp, color = Color.Black)
                Text("2", fontSize = 28.sp, color = Color.Cyan)
                Spacer(modifier = Modifier.height(8.dp))

                // Display time spent
                Text("Passenger fuel saved:", fontSize = 20.sp, color = Color.Black)
                Text("3.6L", fontSize = 28.sp, color = Color.Cyan)
                Text("10L more until next level!", fontSize = 20.sp, color = Color.Black)

                Spacer(modifier = Modifier.height(24.dp))

                // Dismiss button
                Button(onClick = onNavigateToProfile) {
                    Text("Profile", color = Color.White)
                }
            }
        }
    }
}

@Preview
@Composable
private fun VictoryScreenPreview() {
    VictoryScreen(true) { }
}