package com.isi.sameway.client

import androidx.compose.runtime.Composable
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberMarkerState
import com.isi.sameway.firebase.Route
import com.isi.sameway.utils.CarMarker
import com.isi.sameway.utils.DrawPoliline
import com.isi.sameway.utils.MapIconDescriptors
import com.isi.sameway.utils.RouteCalculations
import kotlin.math.max

/**
 * Renders all map content for the Client screen based on the current state.
 */
@Composable
fun ClientMapContent(
    state: ClientScreenState,
    carPosition: LatLng,
    icons: MapIconDescriptors
) {
    // Draw user-selected start/end markers (hide after driver passes them when ride is active)
    UserSelectedMarkers(
        startPosition = state.start,
        endPosition = state.end,
        fullDriverRoute = if (state.acceptedByDriver == true) state.fullDriverRoute else emptyList(),
        driverCarPosition = state.driverCarPosition
    )

    // Draw available routes and their endpoints
    RoutesContent(
        routes = state.routes,
        carPosition = state.carPosition,
        icons = icons
    )

    // When driver accepts, show the route from driver's current position to client's destination
    if (state.acceptedByDriver == true && state.fullDriverRoute.isNotEmpty()) {
        val driverRouteStartIndex = state.acceptedRoute?.driverRouteStartIndex ?: 0

        // Draw route from driver's current position to client's pickup (before pickup)
        if (state.driverCarPosition < driverRouteStartIndex) {
            val routeToPickup = state.fullDriverRoute.subList(
                state.driverCarPosition.coerceAtMost(state.fullDriverRoute.size - 1),
                (driverRouteStartIndex + 1).coerceAtMost(state.fullDriverRoute.size)
            )
            if (routeToPickup.isNotEmpty()) {
                // Green color for the route to pickup
                DrawPoliline(routeToPickup, color = 0xFF4CAF50)
            }
        }

        // Draw the client's route (from client's start to end)
        val clientRouteStart = driverRouteStartIndex.coerceAtMost(state.fullDriverRoute.size - 1)
        val remainingClientRoute = if (state.driverReachedPickup) {
            state.fullDriverRoute.subList(
                state.driverCarPosition.coerceAtMost(state.fullDriverRoute.size - 1),
                state.fullDriverRoute.size
            )
        } else {
            state.fullDriverRoute.subList(clientRouteStart, state.fullDriverRoute.size)
        }
        if (remainingClientRoute.isNotEmpty()) {
            DrawPoliline(remainingClientRoute)
        }

        // Show car marker - always visible when driver has accepted
        CarMarker(
            position = carPosition,
            icon = icons.carIcon,
            isVisible = true
        )

        // Show driver's start point marker when driver is still heading to client
        if (state.driverCarPosition < driverRouteStartIndex && state.fullDriverRoute.isNotEmpty()) {
            Marker(
                state = rememberMarkerState(position = state.fullDriverRoute.first()),
                title = "Driver Start",
                snippet = "Driver's starting point",
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
            )
        }
    }
}

/**
 * Renders the user-selected start and end location markers.
 * When fullDriverRoute is provided, markers are hidden after the driver passes them.
 */
@Composable
private fun UserSelectedMarkers(
    startPosition: LatLng?,
    endPosition: LatLng?,
    fullDriverRoute: List<LatLng> = emptyList(),
    driverCarPosition: Int = 0
) {
    startPosition?.let { position ->
        // Hide start marker if driver has passed it
        val hasPassedStart = if (fullDriverRoute.isNotEmpty()) {
            RouteCalculations.hasCarPassedMarker(
                route = fullDriverRoute,
                carPositionIndex = driverCarPosition,
                markerPosition = position
            )
        } else false

        if (!hasPassedStart) {
            Marker(
                state = MarkerState(position = position),
                title = position.toString(),
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)
            )
        }
    }

    endPosition?.let { position ->
        // Hide end marker if driver has passed it
        val hasPassedEnd = if (fullDriverRoute.isNotEmpty()) {
            RouteCalculations.hasCarPassedMarker(
                route = fullDriverRoute,
                carPositionIndex = driverCarPosition,
                markerPosition = position
            )
        } else false

        if (!hasPassedEnd) {
            Marker(
                state = MarkerState(position = position),
                title = position.toString(),
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)
            )
        }
    }
}

/**
 * Renders all available routes with their polylines and markers.
 */
@Composable
private fun RoutesContent(
    routes: List<MatchedRoute>?,
    carPosition: Int,
    icons: MapIconDescriptors
) {
    routes?.forEach { matchedRoute ->
        RouteContent(
            route = matchedRoute.route,
            carPosition = carPosition,
            carIcon = icons.carIcon
        )
    }
}

/**
 * Renders a single route's polyline and endpoint markers.
 */
@Composable
private fun RouteContent(
    route: Route,
    carPosition: Int,
    carIcon: com.google.android.gms.maps.model.BitmapDescriptor
) {
    val coordinates = route.coordinates
    if (coordinates.isEmpty()) return

    // Draw the route polyline (remaining portion)
    val remainingRoute = coordinates.takeLast(max(0, coordinates.size - carPosition))
    if (remainingRoute.isNotEmpty()) {
        DrawPoliline(remainingRoute)
    }

    // Draw start marker
    RouteStartMarker(coordinates.first(), carIcon)

    // Draw end marker
    RouteEndMarker(coordinates.last(), carIcon)
}

@Composable
private fun RouteStartMarker(
    position: LatLng,
    icon: com.google.android.gms.maps.model.BitmapDescriptor
) {
    Marker(
        state = rememberMarkerState(position = position),
        title = "Start",
        snippet = "Starting Point",
        icon = icon
    )
}

@Composable
private fun RouteEndMarker(
    position: LatLng,
    icon: com.google.android.gms.maps.model.BitmapDescriptor
) {
    Marker(
        state = rememberMarkerState(position = position),
        title = "End",
        snippet = "Destination Point",
        icon = icon
    )
}
