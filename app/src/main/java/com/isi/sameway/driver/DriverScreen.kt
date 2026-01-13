package com.isi.sameway.driver

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.rememberMarkerState
import com.isi.sameway.screens.TripCompletionScreen
import com.isi.sameway.screens.TripSummary
import com.isi.sameway.theme.AppColors
import com.isi.sameway.utils.DriverBottomSheetScreen
import com.isi.sameway.utils.GoogleMapsScreen
import com.isi.sameway.utils.rememberMapIconDescriptors
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

@SuppressLint("UnusedMaterialScaffoldPaddingParameter", "UnrememberedMutableState")
@Composable
fun DriverScreen(
    viewModel: DriverScreenViewModel = viewModel(),
    navigateToProfile: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val showCompletionOverlay by viewModel.showCompletionOverlay.collectAsState()
    val completedTripData by viewModel.completedTripSummary.collectAsState()
    val context = LocalContext.current
    val icons = rememberMapIconDescriptors()

    val carState = rememberMarkerState(position = LatLng(0.0, 0.0))

    // Car animation effect
    CarAnimationEffect(
        state = state,
        carState = carState,
        onPositionUpdate = viewModel::updateCarPosition,
        onRouteComplete = viewModel::finishRoute,
        onClientPickup = { client ->
            viewModel.answerClient(client, RouteStatus.Picked)
            Toast.makeText(context, "🎉 Client picked up!", Toast.LENGTH_SHORT).show()
        },
        onClientDropoff = { client ->
            viewModel.answerClient(client, RouteStatus.Finished)
            Toast.makeText(context, "✅ Client dropped off successfully!", Toast.LENGTH_SHORT).show()
        }
    )

    Box(modifier = Modifier.fillMaxSize()) {
        DriverBottomSheetScreen(
            state = state,
            postRoute = { startTime -> viewModel.postRoute(startTime) },
            declineRoute = viewModel::declineRoute,
            answerClient = viewModel::answerClient
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                GoogleMapsScreen(onMapClick = viewModel::onMapClick) {
                    DriverMapContent(
                        state = state,
                        carPosition = carState.position,
                        icons = icons
                    )
                }

                // Top status bar
                RouteStatusBar(state = state)

                // Start road button
                StartRoadButton(
                    state = state,
                    onStartRoad = viewModel::startRoad
                )
            }
        }

        // Calculate trip summary from completed trip data or current state
        val tripSummary = completedTripData?.let { tripData ->
            val activeClients = tripData.clients.filter {
                it.status == "Active" || it.status == "Picked" || it.status == "Finished"
            }
            TripSummary.fromRoute(
                coordinates = tripData.road,
                clientRouteDistances = activeClients.map { it.routeDistanceKm }
            )
        } ?: when (val currentState = state) {
            is DriverScreenState.ConfirmedRoute -> {
                val activeClients = currentState.incomingClients.filter {
                    it.status == "Active" || it.status == "Picked" || it.status == "Finished"
                }
                TripSummary.fromRoute(
                    coordinates = currentState.road,
                    clientRouteDistances = activeClients.map { it.routeDistanceKm }
                )
            }
            else -> TripSummary(
                moneyEarned = "0.00 LEI",
                totalTimeMinutes = 0,
                peopleHelped = 0,
                fuelSaved = "0.0L",
                distanceKm = "0.0 km",
                co2Saved = "0.0 kg"
            )
        }

        TripCompletionScreen(
            visible = showCompletionOverlay,
            isDriver = true,
            tripSummary = tripSummary,
            buttonText = "View Profile",
            onButtonClick = {
                viewModel.dismissCompletionOverlay()
                navigateToProfile()
            }
        )
    }
}

/**
 * Shows current route status at the top of the screen.
 */
@Composable
private fun BoxScope.RouteStatusBar(
    state: DriverScreenState,
    modifier: Modifier = Modifier
) {
    val isConfirmedRoute = state is DriverScreenState.ConfirmedRoute
    val clientCount = (state as? DriverScreenState.ConfirmedRoute)?.incomingClients?.size ?: 0

    AnimatedVisibility(
        visible = isConfirmedRoute && state.roadStarted,
        enter = slideInVertically { -it } + fadeIn(),
        exit = slideOutVertically { -it } + fadeOut(),
        modifier = modifier
            .align(Alignment.TopCenter)
            .padding(top = 48.dp)
            .zIndex(5f)
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = AppColors.AcceptButtonColor,
                    modifier = Modifier.size(24.dp)
                )

                Column {
                    Text(
                        text = "Trip in Progress",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A)
                    )
                    Text(
                        text = "$clientCount rider${if (clientCount != 1) "s" else ""} on board",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

/**
 * Handles car animation and client pickup/dropoff events.
 */
@Composable
private fun CarAnimationEffect(
    state: DriverScreenState,
    carState: com.google.maps.android.compose.MarkerState,
    onPositionUpdate: (Int) -> Unit,
    onRouteComplete: () -> Unit,
    onClientPickup: (com.isi.sameway.firebase.Client) -> Unit,
    onClientDropoff: (com.isi.sameway.firebase.Client) -> Unit
) {
    LaunchedEffect(state.carPosition, state.roadStarted, state.isRouteFinished) {
        if (!state.roadStarted || state.road.isEmpty() || state.isRouteFinished) return@LaunchedEffect

        // Check if we've already reached the end
        if (state.carPosition >= state.road.size - 1) {
            onRouteComplete()
            return@LaunchedEffect
        }

        delay(0.8.seconds)

        val newPosition = state.carPosition + 1
        val nextCarPosition = state.road.getOrNull(newPosition)
            ?: return@LaunchedEffect

        carState.position = nextCarPosition
        onPositionUpdate(newPosition)

        if (newPosition >= state.road.size - 1) {
            onRouteComplete()
        }

        // Handle client pickups and dropoffs
        (state as? DriverScreenState.ConfirmedRoute)?.incomingClients?.forEach { client ->
            when (carState.position) {
                client.start -> onClientPickup(client)
                client.end -> onClientDropoff(client)
            }
        }
    }
}

/**
 * Professional button to start the road.
 */
@Composable
private fun BoxScope.StartRoadButton(
    state: DriverScreenState,
    onStartRoad: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (state.roadStarted) return

    val (showButton, buttonText, buttonEnabled) = when {
        state.road.isEmpty() -> Triple(true, "Tap map to set start", false)
        state.road.size == 1 -> Triple(true, "Tap map to set destination", false)
        state is DriverScreenState.ConfirmedRoute -> Triple(true, "Start Trip", true)
        else -> Triple(false, "", false)
    }

    AnimatedVisibility(
        visible = showButton,
        enter = scaleIn() + fadeIn(),
        exit = scaleOut() + fadeOut(),
        modifier = modifier
            .align(Alignment.TopCenter)
            .padding(top = 48.dp)
            .zIndex(5f)
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (buttonEnabled) AppColors.AcceptButtonColor else Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = RoundedCornerShape(24.dp)
        ) {
            if (buttonEnabled) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onStartRoad,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent
                        ),
                        elevation = ButtonDefaults.buttonElevation(0.dp)
                    ) {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = buttonText,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Place,
                        contentDescription = null,
                        tint = AppColors.PrimaryBlue,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = buttonText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.DarkGray
                    )
                }
            }
        }
    }
}

