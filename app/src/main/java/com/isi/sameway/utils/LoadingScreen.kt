package com.isi.sameway.utils

import AnimatedProgressBar
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.isi.sameway.loadingPeriod
import kotlinx.coroutines.delay
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds

// Define the gradient colors
private val GradientColors = listOf(
    Color.Red,
    Color.Yellow,
    Color.Green,
    Color.Cyan,
    Color.Blue,
    Color.Magenta
)
@Composable
fun LoadingScreen() {
    var progress by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(Unit) {
        var timeElapsed = 0.seconds
        while (timeElapsed < loadingPeriod) {
            delay(1.seconds)
            timeElapsed += 1.seconds
            progress = minOf(1f, progress + Random.nextFloat() / 2f)
        }

    }


    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize().background(color = Color.LightGray),
    ) {
        Text("Loading routes...", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        AnimatedProgressBar(
            progress = progress,
            modifier = Modifier.fillMaxWidth(0.8f).padding(top = 40.dp),
            strokeWidth = 20.dp,
            colors = GradientColors
        )
    }
}