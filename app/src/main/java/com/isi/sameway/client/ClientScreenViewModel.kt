package com.isi.sameway.client

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.isi.sameway.driver.RouteStatus
import com.isi.sameway.firebase.FirebaseDatabaseHelper
import com.isi.sameway.firebase.FirebaseDatabaseHelper.observeRoutes
import com.isi.sameway.firebase.Route
import com.isi.sameway.firebase.gaussianDistance
import com.isi.sameway.utils.RouteCalculations
import com.isi.sameway.utils.TimeUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.lighthousegames.logging.logging

class ClientScreenViewModel : ViewModel() {
    private val _state = MutableStateFlow(ClientScreenState())
    val state = _state.asStateFlow()

    private val _routes = MutableStateFlow<List<Route>>(emptyList())

    companion object {
        private const val DISTANCE_THRESHOLD = 10000F
        private val log = logging()
    }

    init {
        observeRouteChanges()
    }

    // region Observers

    private fun observeRouteChanges() {
        viewModelScope.launch {
            observeRoutes { routes ->
                _routes.value = routes
                handleRouteUpdates(routes)
                updateAvailableRoutes()
            }
        }
    }

    private fun handleRouteUpdates(routes: List<Route>) {
        val acceptedRoute = _state.value.acceptedRoute ?: return

        val matchingRoute = routes.find { it.userId == acceptedRoute.route.userId }

        // If the route was deleted (driver finished the trip), reset to initial state
        if (matchingRoute == null) {
            _state.update { ClientScreenState() }
            return
        }

        // If the route status is Finished, reset to initial state
        if (matchingRoute.status == RouteStatus.Finished.msg) {
            _state.update { ClientScreenState() }
            return
        }

        log.d { "acceptedRoute: $matchingRoute ${matchingRoute.status}" }

        handleDriverResponse(matchingRoute, acceptedRoute.driverRouteStartIndex)
        syncCarPositionIfAccepted(matchingRoute, acceptedRoute.driverRouteStartIndex)
    }

    private fun handleDriverResponse(route: Route, driverRouteStartIndex: Int) {
        val clientStatus = route.clients.firstOrNull()?.status ?: return

        when (clientStatus) {
            RouteStatus.Active.msg -> {
                // Calculate client-relative car position
                val clientRelativeCarPosition = maxOf(0, route.carPosition - driverRouteStartIndex)
                _state.update { state ->
                    state.copy(
                        acceptedByDriver = true,
                        driverCarPosition = route.carPosition,
                        carPosition = clientRelativeCarPosition,
                        roadStarted = route.roadStarted,
                        fullDriverRoute = route.coordinates
                    )
                }
            }
            RouteStatus.Refused.msg -> {
                _state.update { state ->
                    state.copy(
                        acceptedByDriver = false,
                        acceptedRoute = null,
                        driverCarPosition = 0,
                        carPosition = 0,
                        roadStarted = false,
                        fullDriverRoute = emptyList()
                    )
                }
            }
            RouteStatus.Finished.msg -> {
                // Trip finished - reset to initial state
                _state.update { ClientScreenState() }
            }
        }
    }

    private fun syncCarPositionIfAccepted(route: Route, driverRouteStartIndex: Int) {
        if (_state.value.acceptedByDriver == true) {
            // Calculate client-relative car position
            val clientRelativeCarPosition = maxOf(0, route.carPosition - driverRouteStartIndex)
            _state.update { state ->
                state.copy(
                    driverCarPosition = route.carPosition,
                    carPosition = clientRelativeCarPosition,
                    roadStarted = route.roadStarted,
                    fullDriverRoute = route.coordinates
                )
            }
        }
    }

    private fun updateAvailableRoutes() {
        val details = _state.value.routeDetails ?: return
        val routes = _routes.value
        val matchingRoutes = findMatchingRoutes(routes, details)

        log.d { "routes: $routes" }
        _state.update { it.copy(routes = matchingRoutes) }
    }

    // endregion

    // region Public Actions

    /**
     * Handles all UI events using a single entry point (MVI pattern).
     */
    fun onEvent(event: ClientUiEvent) {
        when (event) {
            is ClientUiEvent.MapClicked -> handleMapClick(event.location)
            is ClientUiEvent.SetRouteDetails -> handleSetDetails(event.start, event.end, event.startTime)
            is ClientUiEvent.RestartMarkers -> handleRestartMarkers()
            is ClientUiEvent.AnswerRoute -> handleAnswerRoute(event.route, event.accept)
        }
    }

    // Legacy methods for backward compatibility
    fun addPoint(latLng: LatLng) = onEvent(ClientUiEvent.MapClicked(latLng))
    fun setDetails(start: LatLng, end: LatLng, startTime: Int = 1200) =
        onEvent(ClientUiEvent.SetRouteDetails(start, end, startTime))
    fun restartMarkers() = onEvent(ClientUiEvent.RestartMarkers)
    fun answerRoute(route: MatchedRoute, accept: Boolean) =
        onEvent(ClientUiEvent.AnswerRoute(route, accept))

    // endregion

    // region Event Handlers

    private fun handleMapClick(latLng: LatLng) {
        if (_state.value.start == null) {
            _state.update { it.copy(start = latLng) }
        } else {
            _state.update { it.copy(end = latLng) }
        }
    }

    private fun handleSetDetails(start: LatLng, end: LatLng, startTime: Int) {
        _state.update { it.copy(routeDetails = ClientRouteDetails(start, end, startTime)) }
        updateAvailableRoutes()
    }

    private fun handleRestartMarkers() {
        _state.update { it.copy(start = null, end = null) }
    }

    private fun handleAnswerRoute(route: MatchedRoute, accept: Boolean) {
        if (accept) {
            val clientStartTime = _state.value.routeDetails?.startTime ?: 0
            val routeDistanceKm = RouteCalculations.calculateDistanceKm(route.route.coordinates)
            FirebaseDatabaseHelper.requestAccessFromDriver(
                route.route, route.route.coordinates.first(), route.route.coordinates.last(), clientStartTime, routeDistanceKm
            )
            _state.update { it.copy(acceptedRoute = route) }
        } else {
            _state.update { it.copy(routes = it.routes?.minus(route)) }
        }
    }

    // endregion

    // region Route Matching Logic

    private fun findMatchingRoutes(routes: List<Route>, details: ClientRouteDetails): List<MatchedRoute> {
        return routes.mapNotNull { route ->
            createMatchingRouteSegment(route, details)
        }
    }

    private fun createMatchingRouteSegment(route: Route, details: ClientRouteDetails): MatchedRoute? {
        val coordinates = route.coordinates

        // Skip routes with empty coordinates
        if (coordinates.isEmpty()) {
            log.d { "Skipping route with empty coordinates" }
            return null
        }

        // Find closest point to client's start
        val closestStart = findClosestPoint(coordinates, details.start) ?: return null
        val startIndex = coordinates.indexOf(closestStart)

        // Ensure there are coordinates after the start point
        if (startIndex >= coordinates.size - 1) {
            log.d { "No coordinates after start point" }
            return null
        }

        // Find closest point to client's end (must be after start)
        val coordsAfterStart = coordinates.slice(startIndex until coordinates.size)
        val closestEnd = findClosestPoint(coordsAfterStart, details.end) ?: return null

        // Check all criteria
        // Allow routes that started at most 30 minutes before the client's requested time
        val earliestAcceptableTime = TimeUtils.addMinutes(details.startTime, -30)
        val meetsTimeCriteria = route.startingTime >= earliestAcceptableTime
        val isIncomingRoute = route.status == RouteStatus.Incoming.msg
        val isCloseEnough = isWithinDistanceThreshold(details, closestStart, closestEnd)

        log.d { "Route criteria: onTime=$meetsTimeCriteria, isIncoming=$isIncomingRoute, isClose=$isCloseEnough" }

        if (meetsTimeCriteria && isIncomingRoute && isCloseEnough) {
            val endIndex = startIndex + coordsAfterStart.indexOf(closestEnd)
            val routeSegment = coordinates.slice(startIndex..endIndex)
            return MatchedRoute(
                route = route.copy(coordinates = routeSegment),
                driverRouteStartIndex = startIndex
            )
        }

        return null
    }

    private fun isWithinDistanceThreshold(
        details: ClientRouteDetails,
        closestStart: LatLng,
        closestEnd: LatLng
    ): Boolean {
        val startDistance = gaussianDistance(details.start, closestStart)
        val endDistance = gaussianDistance(details.end, closestEnd)
        return startDistance < DISTANCE_THRESHOLD && endDistance < DISTANCE_THRESHOLD
    }

    private fun findClosestPoint(coordinates: List<LatLng>, target: LatLng): LatLng? {
        return coordinates.minByOrNull { gaussianDistance(it, target) }
    }

    // endregion
}

/**
 * Represents the current state of the Client screen.
 */
data class ClientScreenState(
    val acceptedRoute: MatchedRoute? = null,
    val acceptedByDriver: Boolean? = null,
    val routes: List<MatchedRoute>? = emptyList(),
    val routeDetails: ClientRouteDetails? = null,
    val start: LatLng? = null,
    val end: LatLng? = null,
    /** Car position relative to the DRIVER's full route (absolute position) */
    val driverCarPosition: Int = 0,
    /** Car position relative to the CLIENT's route segment */
    val carPosition: Int = 0,
    val roadStarted: Boolean = false,
    /** Full driver route for showing car moving towards client */
    val fullDriverRoute: List<LatLng> = emptyList()
) {
    /** Whether the client is currently waiting for driver acceptance */
    val isWaitingForDriver: Boolean
        get() = acceptedRoute != null && acceptedByDriver == null

    /** Whether the trip is currently active */
    val isTripActive: Boolean
        get() = acceptedByDriver == true && roadStarted

    /** Whether markers have been set */
    val hasSelectedLocations: Boolean
        get() = start != null && end != null

    /** Whether the driver has reached the client's pickup point */
    val driverReachedPickup: Boolean
        get() = acceptedRoute != null && driverCarPosition >= acceptedRoute.driverRouteStartIndex
}

/**
 * Details about the client's desired route.
 */
data class ClientRouteDetails(
    val start: LatLng,
    val end: LatLng,
    val startTime: Int = 1200
)

/**
 * A matched route with metadata about where it starts in the driver's full route.
 * This is needed to correctly translate the driver's carPosition to the client's perspective.
 */
data class MatchedRoute(
    val route: Route,
    /** The index in the driver's original route where the client's segment starts */
    val driverRouteStartIndex: Int
)

