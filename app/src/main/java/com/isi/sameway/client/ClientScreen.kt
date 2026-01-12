package com.isi.sameway.client

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.rememberMarkerState
import com.isi.sameway.screens.TripCompletionScreen
import com.isi.sameway.screens.TripSummary
import com.isi.sameway.theme.AppColors
import com.isi.sameway.utils.ClientBottomSheetScreen
import com.isi.sameway.utils.GoogleMapsScreen
import com.isi.sameway.utils.rememberMapIconDescriptors

@SuppressLint("UnrememberedMutableState")
@Composable
fun ClientScreen(
    viewModel: ClientScreenViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val icons = rememberMapIconDescriptors()

    var showVictoryScreen by remember { mutableStateOf(false) }
    val carState = rememberMarkerState(position = LatLng(0.0, 0.0))

    // Car animation effect for client view
    ClientCarAnimationEffect(
        state = state,
        carState = carState,
        onTripEnd = {
            Toast.makeText(context, "🎉 You've arrived at your destination!", Toast.LENGTH_LONG).show()
            showVictoryScreen = true
        }
    )

    Box(modifier = Modifier.fillMaxSize()) {
        ClientBottomSheetScreen(
            state = state,
            answerRoute = viewModel::answerRoute,
            setDetails = viewModel::setDetails,
            restartMarkers = viewModel::restartMarkers
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                GoogleMapsScreen(onMapClick = viewModel::addPoint) {
                    ClientMapContent(
                        state = state,
                        carPosition = carState.position,
                        icons = icons
                    )
                }

                // Status indicator at top
                ClientStatusBar(state = state)

                // Instruction hint
                ClientInstructionHint(state = state)
            }
        }

        // Calculate trip summary from route data
        val tripSummary = state.acceptedRoute?.let { matchedRoute ->
            TripSummary.forClient(matchedRoute.route.coordinates)
        } ?: TripSummary(
            moneyEarned = "0.00",
            totalTimeMinutes = 0,
            peopleHelped = 0,
            fuelSaved = "0.0L",
            distanceKm = "0.0 km",
            co2Saved = "0.0 kg"
        )

        TripCompletionScreen(
            visible = showVictoryScreen,
            isDriver = false,
            tripSummary = tripSummary,
            buttonText = "Rate Trip",
            onButtonClick = { showVictoryScreen = false }
        )
    }
}

/**
 * Shows status bar at the top when ride is in progress.
 */
@Composable
private fun BoxScope.ClientStatusBar(
    state: ClientScreenState,
    modifier: Modifier = Modifier
) {
    val isRideInProgress = state.roadStarted && state.acceptedByDriver == true

    AnimatedVisibility(
        visible = isRideInProgress,
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
                        text = "On Your Way!",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A)
                    )
                    Text(
                        text = "Arriving at destination soon",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

/**
 * Shows instruction hints when setting up the route.
 */
@Composable
private fun BoxScope.ClientInstructionHint(
    state: ClientScreenState,
    modifier: Modifier = Modifier
) {
    val shouldShow = state.start == null || state.end == null
    val (icon, text) = when {
        state.start == null -> Icons.Default.Place to "Tap the map to set your pickup location"
        state.end == null -> Icons.Default.LocationOn to "Tap the map to set your destination"
        else -> null to ""
    }

    AnimatedVisibility(
        visible = shouldShow && icon != null,
        enter = scaleIn() + fadeIn(),
        exit = scaleOut() + fadeOut(),
        modifier = modifier
            .align(Alignment.TopCenter)
            .padding(top = 48.dp)
            .zIndex(5f)
    ) {
        icon?.let {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                shape = RoundedCornerShape(24.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        it,
                        contentDescription = null,
                        tint = if (state.start == null) AppColors.AcceptButtonColor else AppColors.TimeColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = Color.DarkGray
                    )
                }
            }
        }
    }
}

/**
 * Handles car animation for client view (follows driver's position synced from Firebase).
 * The carPosition in state is already synced from the driver, so we just update the visual marker.
 * Car is visible from the moment driver accepts until trip is complete.
 * Shows car moving from driver's start point, not just from client's pickup.
 */
@Composable
private fun ClientCarAnimationEffect(
    state: ClientScreenState,
    carState: com.google.maps.android.compose.MarkerState,
    onTripEnd: () -> Unit
) {
    // Initialize car position when driver accepts - use fullDriverRoute for accurate position
    LaunchedEffect(state.acceptedByDriver, state.acceptedRoute, state.fullDriverRoute) {
        if (state.acceptedByDriver != true) return@LaunchedEffect

        val fullRoute = state.fullDriverRoute
        if (fullRoute.isEmpty()) return@LaunchedEffect

        // Set initial position using driver's absolute position on their full route
        val clampedPosition = state.driverCarPosition.coerceIn(0, fullRoute.size - 1)
        carState.position = fullRoute[clampedPosition]
    }

    // Update car position during trip - use driver's absolute position
    LaunchedEffect(state.driverCarPosition, state.roadStarted, state.fullDriverRoute) {
        if (!state.roadStarted) return@LaunchedEffect

        val fullRoute = state.fullDriverRoute
        if (fullRoute.isEmpty()) return@LaunchedEffect

        // Use the driver's absolute position on their full route
        val clampedPosition = state.driverCarPosition.coerceIn(0, fullRoute.size - 1)
        carState.position = fullRoute[clampedPosition]

        // Check if trip has ended (driver reached end of route)
        val matchedRoute = state.acceptedRoute ?: return@LaunchedEffect
        val clientEndIndex = matchedRoute.driverRouteStartIndex + matchedRoute.route.coordinates.size - 1

        if (state.driverCarPosition >= clientEndIndex) {
            onTripEnd()
        }
    }
}