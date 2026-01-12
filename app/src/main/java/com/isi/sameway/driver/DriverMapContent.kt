package com.isi.sameway.driver

import androidx.compose.runtime.Composable
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberMarkerState
import com.isi.sameway.firebase.Client
import com.isi.sameway.utils.CarMarker
import com.isi.sameway.utils.DrawPoliline
import com.isi.sameway.utils.MapIconDescriptors
import kotlin.math.max

/**
 * Renders all map content for the Driver screen based on the current state.
 */
@Composable
fun DriverMapContent(
    state: DriverScreenState,
    carPosition: LatLng,
    icons: MapIconDescriptors
) {
    // Draw the route polyline
    RoutePolyline(state)

    // Draw route endpoint markers
    RouteEndpoints(state)

    // State-specific content
    when (state) {
        is DriverScreenState.Initial,
        is DriverScreenState.Loading,
        is DriverScreenState.Loaded -> {
            // No additional content needed
        }
        is DriverScreenState.ConfirmedRoute -> {
            ConfirmedRouteMapContent(
                state = state,
                carPosition = carPosition,
                icons = icons
            )
        }
    }
}

/**
 * Renders the route polyline with proper handling for driving state.
 */
@Composable
private fun RoutePolyline(state: DriverScreenState) {
    if (!state.shouldShowRoad) return

    val roadPoints = when {
        state is DriverScreenState.ConfirmedRoute && state.roadStarted -> {
            // Show only remaining route when driving
            state.road.takeLast(max(0, state.road.size - state.carPosition))
        }
        else -> state.road
    }

    if (roadPoints.isNotEmpty()) {
        DrawPoliline(roadPoints)
    }
}

/**
 * Renders route start and end markers.
 */
@Composable
private fun RouteEndpoints(state: DriverScreenState) {
    val firstLocation = state.road.firstOrNull()
    val secondLocation = state.road.lastOrNull()?.takeIf { state.road.size > 1 }

    firstLocation?.let { position ->
        Marker(
            state = MarkerState(position = position),
            title = position.toString(),
            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)
        )
    }

    secondLocation?.let { position ->
        Marker(
            state = MarkerState(position = position),
            title = position.toString(),
            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)
        )
    }
}

/**
 * Renders map content specific to confirmed route state.
 */
@Composable
private fun ConfirmedRouteMapContent(
    state: DriverScreenState.ConfirmedRoute,
    carPosition: LatLng,
    icons: MapIconDescriptors
) {
    // Draw client markers
    ClientMarkers(
        clients = state.incomingClients,
        sourceIcon = icons.personIcon,
        destinationIcon = icons.destinationIcon
    )

    // Draw car marker when driving
    if (state.roadStarted) {
        CarMarker(
            position = carPosition,
            icon = icons.carIcon,
            isVisible = true
        )
    }
}

/**
 * Renders markers for all incoming clients.
 */
@Composable
private fun ClientMarkers(
    clients: List<Client>,
    sourceIcon: BitmapDescriptor,
    destinationIcon: BitmapDescriptor
) {
    clients.forEach { client ->
        // Show pickup marker only if client hasn't been picked up or finished
        if (client.status != "Picked" && client.status != "Finished") {
            ClientPickupMarker(client, sourceIcon)
        }

        // Always show destination marker
        ClientDestinationMarker(client, destinationIcon)
    }
}

@Composable
private fun ClientPickupMarker(client: Client, icon: BitmapDescriptor) {
    Marker(
        state = rememberMarkerState(position = client.start),
        title = "Client ${client.user}",
        snippet = "Departure",
        icon = icon,
        onClick = { false }
    )
}

@Composable
private fun ClientDestinationMarker(client: Client, icon: BitmapDescriptor) {
    Marker(
        state = rememberMarkerState(position = client.end),
        title = "Client ${client.user}",
        snippet = "Destination",
        icon = icon,
        onClick = { false }
    )
}
