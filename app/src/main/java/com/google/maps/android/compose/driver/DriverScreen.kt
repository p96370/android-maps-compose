package com.google.maps.android.compose.driver

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberMarkerState
import com.google.maps.android.compose.theme.backgroundColor
import com.google.maps.android.compose.utils.DrawPoliline
import com.google.maps.android.compose.utils.DriverBottomSheetScreen
import com.google.maps.android.compose.utils.GoogleMapsScreen
import kotlinx.coroutines.delay
import com.google.maps.android.compose.R
import org.lighthousegames.logging.logging
import kotlin.math.max
import kotlin.time.Duration.Companion.seconds
import androidx.core.graphics.createBitmap
import androidx.core.graphics.scale
import androidx.core.graphics.set
import com.google.maps.android.compose.clients
import com.google.maps.android.compose.screens.VictoryScreen
import kotlin.math.min

private val log = logging()

@SuppressLint("UnusedMaterialScaffoldPaddingParameter", "UnrememberedMutableState")
@Composable
fun DriverScreen(
    viewModel: DriverScreenViewModel = viewModel(),
) {
    val state = viewModel.state.collectAsState().value

    var roadStarted by remember{ mutableStateOf(false) }

    var showVictoryScreen by remember { mutableStateOf(false) }

    val context = LocalContext.current

    var carPosition by remember { mutableIntStateOf(0) }

    val carState = rememberMarkerState(position = LatLng(0.0, 0.0))
    LaunchedEffect(carPosition, roadStarted) {
        if (roadStarted) {
            delay(0.8.seconds)
            carState.position = state.road[min(carPosition + 1, state.road.size - 1)]
            carPosition++
            if (carPosition == state.road.size - 1) {
                showVictoryScreen = true
            }
            (state as? DriverScreenState.ConfirmedRoute)?.let {
                it.incomingClients.forEach { client ->
                    if (client.start == carState.position) {
                        viewModel.answerClient(client, RouteStatus.Picked)
                        Toast.makeText(context, "Picked client", Toast.LENGTH_SHORT)
                            .show()
                    }
                    if (client.end == carState.position) {
                        viewModel.answerClient(client, RouteStatus.Finished)
                        Toast.makeText(context, "Dropped client at finish.", Toast.LENGTH_SHORT)
                            .show()
                    }
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

    val sourceDescriptor: BitmapDescriptor = remember {
        // You can load the bitmap from resources:
        val drawable = context.getDrawable(R.drawable.person) // Your car icon drawable
        val bitmap = drawable?.toBitmap() ?: createBitmap(1, 1)

        val smallBitmap = bitmap.scale(100, 100)
        BitmapDescriptorFactory.fromBitmap(smallBitmap)
    }

    val destDescriptor: BitmapDescriptor = remember {
        // You can load the bitmap from resources:
        val drawable = context.getDrawable(R.drawable.user_icon_2) // Your car icon drawable
        val bitmap = drawable?.toBitmap() ?: createBitmap(1, 1)

        val smallBitmap = bitmap.scale(100, 100)
        BitmapDescriptorFactory.fromBitmap(smallBitmap)
    }

    val firstLocation by remember(state.road) {
        derivedStateOf { state.road.firstOrNull() }
    }
    val secondLocation by remember(state.road) {
        derivedStateOf { state.road.lastOrNull()?.takeIf { state.road.size > 1 } }
    }
    val markerFirst = MarkerState(position = firstLocation ?: LatLng(0.0, 0.0))
    val markerSecond = MarkerState(position = secondLocation ?: LatLng(0.0, 0.0))

    Box(modifier = Modifier.fillMaxSize()) {
        DriverBottomSheetScreen(
            state = state,
            postRoute = viewModel::postRoute,
            declineRoute = viewModel::declineRoute,
            answerClient = viewModel::answerClient
        ) {
            GoogleMapsScreen(
                onMapClick = viewModel::onMapClick
            ) {
                when (state) {
                    is DriverScreenState.Initial -> {}
                    is DriverScreenState.Loading -> {}

                    is DriverScreenState.Loaded -> {
                        DrawPoliline(state.road)
                    }

                    is DriverScreenState.ConfirmedRoute -> {
                        DrawPoliline(state.road.takeLast(max(0, state.road.size - carPosition)))
                        state.incomingClients.forEach { client ->
                            if (client.status != "Picked" && client.status != "Finished") {
                                Marker(
                                    state = rememberMarkerState(position = client.start),
                                    title = "Client ${client.user}",
                                    snippet = "Departure",
                                    onClick = {
                                        false
                                    },
                                    icon = sourceDescriptor,
                                )
                            }
                            Marker(
                                state = rememberMarkerState(position = client.end),
                                title = "Client ${client.user}",
                                snippet = "Destination",
                                onClick = {
                                    false
                                },
                                icon = destDescriptor,
                            )
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
            }


            if (!roadStarted) {
                Button(
                    onClick = {
                        roadStarted = true
                    },
                    modifier = Modifier
                        .align(Alignment.TopCenter) // Align button to the top center
                        .padding(top = 100.dp)       // Add padding from the top
                        .zIndex(5f),               // Ensure it's displayed above other elements
                    colors = ButtonDefaults.buttonColors(
                        containerColor = backgroundColor,   // Set button background color
                        contentColor = Color.Black     // Set text color
                    )
                ) {
                    Text(
                        text = when {
                            state.road.isEmpty() -> "Enter Start Location"
                            state.road.size == 1 -> "Enter Destination"
                            else -> "Route Ready. Press to start"
                        },
                        fontSize = 16.sp,          // Set an appropriate font size
                        textDecoration = TextDecoration.None // Remove underline for cleaner design
                    )
                }
            }

        }

        VictoryScreen(visible = showVictoryScreen)
    }
}

