package com.isi.sameway.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberMarkerState
import kotlin.math.min

/**
 * A marker that represents the car on the map.
 */
@Composable
fun CarMarker(
    position: LatLng,
    icon: BitmapDescriptor,
    isVisible: Boolean = true
) {
    if (isVisible) {
        val markerState = rememberMarkerState(position = position)

        LaunchedEffect(position) {
            markerState.position = position
        }

        Marker(
            state = markerState,
            title = "Car Position",
            snippet = "Current position of the car",
            icon = icon,
            onClick = { false }
        )
    }
}

/**
 * Start/End location markers with consistent styling.
 */
@Composable
fun LocationMarker(
    position: LatLng?,
    title: String = "",
    snippet: String = "",
    icon: BitmapDescriptor? = null
) {
    if (position != null && position != LatLng(0.0, 0.0)) {
        Marker(
            state = rememberMarkerState(position = position),
            title = title.ifEmpty { position.toString() },
            snippet = snippet,
            icon = icon,
            onClick = { false }
        )
    }
}

/**
 * Composable for rendering route start and end markers.
 */
@Composable
fun RouteEndpointMarkers(
    startPosition: LatLng?,
    endPosition: LatLng?,
    startIcon: BitmapDescriptor? = null,
    endIcon: BitmapDescriptor? = null
) {
    LocationMarker(
        position = startPosition,
        title = "Start",
        snippet = "Starting Point",
        icon = startIcon
    )

    if (endPosition != null && endPosition != startPosition) {
        LocationMarker(
            position = endPosition,
            title = "End",
            snippet = "Destination Point",
            icon = endIcon
        )
    }
}

/**
 * Calculates the current car position on the road.
 */
fun getCarPositionOnRoad(road: List<LatLng>, positionIndex: Int): LatLng? {
    return road.getOrNull(min(positionIndex, road.size - 1))
}
