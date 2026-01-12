package com.isi.sameway.driver

import com.google.android.gms.maps.model.LatLng
import com.isi.sameway.firebase.Client

/**
 * Sealed interface representing all possible UI events/intents for the Driver screen.
 * This follows the MVI pattern for better state management.
 */
sealed interface DriverUiEvent {
    /** User tapped on the map at the specified location */
    data class MapClicked(val location: LatLng) : DriverUiEvent

    /** User confirmed and posted the route with optional start time */
    data class PostRoute(val startTime: Int = 1200) : DriverUiEvent

    /** User declined the current route */
    data object DeclineRoute : DriverUiEvent

    /** User responded to a client's ride request */
    data class AnswerClient(val client: Client, val status: RouteStatus) : DriverUiEvent

    /** User started driving the route */
    data object StartRoad : DriverUiEvent

    /** Internal event: Car position needs to be updated */
    data class UpdateCarPosition(val position: Int) : DriverUiEvent

    /** Internal event: Route has been completed */
    data object FinishRoute : DriverUiEvent

    /** Internal event: Client was picked up at position */
    data class ClientPickedUp(val client: Client) : DriverUiEvent

    /** Internal event: Client was dropped off */
    data class ClientDroppedOff(val client: Client) : DriverUiEvent
}
