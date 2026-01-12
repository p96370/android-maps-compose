package com.isi.sameway.client

import com.google.android.gms.maps.model.LatLng

/**
 * Sealed interface representing all possible UI events/intents for the Client screen.
 * This follows the MVI pattern for better state management.
 */
sealed interface ClientUiEvent {
    /** User tapped on the map at the specified location */
    data class MapClicked(val location: LatLng) : ClientUiEvent

    /** User confirmed the route details (start, end, time) */
    data class SetRouteDetails(
        val start: LatLng,
        val end: LatLng,
        val startTime: Int = 1200
    ) : ClientUiEvent

    /** User wants to reset the marker positions */
    data object RestartMarkers : ClientUiEvent

    /** User responded to a route offer (accept or decline) */
    data class AnswerRoute(val route: MatchedRoute, val accept: Boolean) : ClientUiEvent
}
