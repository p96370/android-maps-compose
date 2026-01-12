package com.google.maps.android.compose.client

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.scale
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.R
import com.google.maps.android.compose.rememberMarkerState
import com.google.maps.android.compose.screens.VictoryScreen
import com.google.maps.android.compose.theme.backgroundColor
import com.google.maps.android.compose.utils.ClientBottomSheetScreen
import com.google.maps.android.compose.utils.DrawPoliline
import com.google.maps.android.compose.utils.GoogleMapsScreen
import kotlinx.coroutines.delay
import kotlin.math.max
import kotlin.math.min
import kotlin.time.Duration.Companion.seconds

@SuppressLint("UnrememberedMutableState")
@Composable
fun ClientScreen(
    viewModel: ClientScreenViewModel = viewModel(),
) {
    val state = viewModel.state.collectAsState().value
    val firstLocation by remember(state.start) {
        derivedStateOf { state.start }
    }
    val secondLocation by remember(state.end) {
        derivedStateOf { state.end }
    }
    val markerFirst = MarkerState(position = firstLocation ?: LatLng(0.0, 0.0))
    val markerSecond = MarkerState(position = secondLocation ?: LatLng(0.0, 0.0))

    val context = LocalContext.current
    var roadStarted by remember{ mutableStateOf(false) }

    var carPosition by remember { mutableIntStateOf(0) }
    val carState = rememberMarkerState(position = LatLng(0.0, 0.0))

    var showVictoryScreen by remember { mutableStateOf(false)}

    LaunchedEffect(carPosition, roadStarted) {
        if (roadStarted) {
            delay(0.8.seconds)
            state.routes?.firstOrNull()?.let { road ->
                carState.position = road.coordinates[min(carPosition + 1, road.coordinates.size - 1)]
                carPosition++
                if (carPosition == road.coordinates.size - 1) {
                    Toast.makeText(context, "Trip ended.", Toast.LENGTH_SHORT)
                        .show()
                    showVictoryScreen = true
                }
            }

        }
    }

    val carIconDescriptor: BitmapDescriptor = remember {
        // You can load the bitmap from resources:
        val drawable = context.getDrawable(R.drawable.driver) // Your car icon drawable
        val bitmap = drawable?.toBitmap() ?: createBitmap(1, 1)

        val smallBitmap = bitmap.scale(100, 100)
        BitmapDescriptorFactory.fromBitmap(smallBitmap)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        ClientBottomSheetScreen(
            state = state,
            answerRoute = viewModel::answerRoute,
            setDetails = viewModel::setDetails,
            restartMarkers = viewModel::restartMarkers
        ) {
            GoogleMapsScreen(
                onMapClick = viewModel::addPoint
            ) {
                Marker(
                    state = markerFirst,
                    title = "${markerFirst.position}",
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN),
                )
                Marker(
                    state = markerSecond,
                    title = "${markerFirst.position}",
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN),

                    )
                state.routes?.forEach { route ->
                    DrawPoliline(route.coordinates.takeLast(max(0, route.coordinates.size - carPosition)))

                    route.coordinates.firstOrNull()?.let { startPosition ->
                        Marker(
                            state = rememberMarkerState(position = startPosition),
                            title = "Start",
                            icon = carIconDescriptor,
                            snippet = "Starting Point"
                        )
                    }

                    route.coordinates.lastOrNull()?.let { endPosition ->
                        Marker(
                            state = rememberMarkerState(position = endPosition),
                            title = "End",
                            icon = carIconDescriptor,
                            snippet = "Destination Point"
                        )
                    }
                }
                if (roadStarted) {
                    Marker(
                        state = carState,
                        title = "Car Position",
                        snippet = "Current position of the car",
                        icon = carIconDescriptor,
                        onClick = {
                            false
                        }
                    )
                }

            }
        }
        if (!roadStarted) {
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .align(Alignment.TopCenter)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onLongPress = {
                                roadStarted = true
                            }
                        )
                    }
            )
        }

        VictoryScreenClient(visible = showVictoryScreen) { }
    }
}

@Composable
private fun VictoryScreenClient(visible: Boolean, onNavigateToProfile: () -> Unit) {
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
                Text("Trip ended", fontSize = 20.sp, color = Color.Black)

                // Dismiss button
                Button(onClick = onNavigateToProfile) {
                    Text("Profile", color = Color.White)
                }
            }
        }
    }
}