package com.isi.sameway.driver

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.maps.android.PolyUtil
import com.isi.sameway.directions.NetworkRepository
import com.isi.sameway.firebase.Client
import com.isi.sameway.firebase.FirebaseDatabaseHelper
import com.isi.sameway.firebase.FirebaseDatabaseHelper.observeIncomingClients
import com.isi.sameway.firebase.FirebaseDatabaseHelper.updateAccessByDriver
import com.isi.sameway.firebase.Route
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.lighthousegames.logging.logging

class DriverScreenViewModel : ViewModel() {
    private val _state: MutableStateFlow<DriverScreenState> =
        MutableStateFlow(DriverScreenState.Initial())
    val state = _state.asStateFlow()

    private val _showCompletionOverlay = MutableStateFlow(false)
    val showCompletionOverlay = _showCompletionOverlay.asStateFlow()

    private val _completedTripSummary = MutableStateFlow<CompletedTripData?>(null)
    val completedTripSummary = _completedTripSummary.asStateFlow()

    private val log = logging()
    private val currentUserId: String?
        get() = FirebaseAuth.getInstance().currentUser?.uid

    init {
        observeConfirmedRouteClients()
        observeRouteUpdates()
    }

    // region Observers

    private fun observeConfirmedRouteClients() {
        viewModelScope.launch {
            _state.filter { it is DriverScreenState.ConfirmedRoute }
                .distinctUntilChanged()
                .collectLatest {
                    observeIncomingClients { clients ->
                        updateConfirmedRouteState { copy(incomingClients = clients) }
                    }
                }
        }
    }

    private fun observeRouteUpdates() {
        viewModelScope.launch {
            FirebaseDatabaseHelper.observeRoutes { routes ->
                val userRoute = routes.find { it.userId == currentUserId }
                userRoute?.let { route ->
                    syncRouteWithFirebase(route)
                }
            }
        }
    }

    private fun syncRouteWithFirebase(route: Route) {
        val currentState = _state.value as? DriverScreenState.ConfirmedRoute ?: return

        if (currentState.carPosition != route.carPosition ||
            currentState.roadStarted != route.roadStarted) {
            _state.update {
                currentState.copy(
                    position = route.carPosition,
                    started = route.roadStarted
                )
            }
        }
    }

    // endregion

    // region Public Actions

    /**
     * Handles all UI events using a single entry point (MVI pattern).
     */
    fun onEvent(event: DriverUiEvent) {
        when (event) {
            is DriverUiEvent.MapClicked -> handleMapClick(event.location)
            is DriverUiEvent.PostRoute -> handlePostRoute(event.startTime)
            is DriverUiEvent.DeclineRoute -> handleDeclineRoute()
            is DriverUiEvent.AnswerClient -> handleAnswerClient(event.client, event.status)
            is DriverUiEvent.StartRoad -> handleStartRoad()
            is DriverUiEvent.UpdateCarPosition -> handleUpdateCarPosition(event.position)
            is DriverUiEvent.FinishRoute -> handleFinishRoute()
            is DriverUiEvent.ClientPickedUp -> handleClientPickedUp(event.client)
            is DriverUiEvent.ClientDroppedOff -> handleClientDroppedOff(event.client)
        }
    }

    // Legacy methods for backward compatibility
    fun onMapClick(latLng: LatLng) = onEvent(DriverUiEvent.MapClicked(latLng))
    fun postRoute(startTime: Int = 1200) = onEvent(DriverUiEvent.PostRoute(startTime))
    fun declineRoute() = onEvent(DriverUiEvent.DeclineRoute)
    fun answerClient(client: Client, status: RouteStatus) =
        onEvent(DriverUiEvent.AnswerClient(client, status))
    fun updateCarPosition(position: Int) = onEvent(DriverUiEvent.UpdateCarPosition(position))
    fun startRoad() = onEvent(DriverUiEvent.StartRoad)
    fun finishRoute() = onEvent(DriverUiEvent.FinishRoute)

    // endregion

    // region Event Handlers

    private fun handleMapClick(latLng: LatLng) {
        val currentState = _state.value as? DriverScreenState.Initial ?: return

        if (currentState.road.isEmpty()) {
            _state.value = currentState.copy(start = latLng)
        } else {
            loadRoute(DriverScreenState.Loading(currentState.road.first(), latLng))
        }
    }

    private fun loadRoute(newState: DriverScreenState.Loading) {
        viewModelScope.launch {
            _state.value = newState

            try {
                val encoded = NetworkRepository.requestPath(newState.road[0], newState.road[1])
                log.d { "Route encoded: $encoded" }

                if (encoded.isEmpty()) {
                    log.e { "Encoded route is empty! Check NetworkRepository logs for details." }
                    log.e { "Start: ${newState.road[0]}, End: ${newState.road[1]}" }
                    // Revert to Initial state on failure
                    _state.value = DriverScreenState.Initial()
                    return@launch
                }

                val points = PolyUtil.decode(encoded)
                log.d { "Route decoded successfully, points: ${points.size}" }
                _state.value = DriverScreenState.Loaded(points)
            } catch (e: Exception) {
                log.e { "Failed to load route: ${e.message}" }
                e.printStackTrace()
                // Revert to Initial state on error
                _state.value = DriverScreenState.Initial()
            }
        }
    }

    private fun handlePostRoute(startTime: Int) {
        val currentState = _state.value as? DriverScreenState.Loaded ?: return

        val newRoute = Route(
            userId = currentUserId ?: return,
            coordinates = currentState.road,
            status = RouteStatus.Incoming.msg,
            startingTime = startTime,
            clients = emptyList()
        )
        FirebaseDatabaseHelper.addRoute(newRoute)

        _state.value = DriverScreenState.ConfirmedRoute(
            fetchedRoad = currentState.road,
            incomingClients = emptyList(),
            startingTime = startTime
        )
    }

    private fun handleDeclineRoute() {
        if (_state.value is DriverScreenState.Loaded) {
            _state.value = DriverScreenState.Initial()
        }
    }

    private fun handleAnswerClient(client: Client, status: RouteStatus) {
        updateAccessByDriver(client.user, status)
        updateConfirmedRouteState { copy(accepted = true) }
    }

    private fun handleUpdateCarPosition(position: Int) {
        FirebaseDatabaseHelper.updateCarPosition(position)
        updateConfirmedRouteState { copy(position = position) }
    }

    private fun handleStartRoad() {
        FirebaseDatabaseHelper.updateRoadStarted(true)
        updateConfirmedRouteState { copy(started = true) }
    }

    private fun handleFinishRoute() {
        // Store trip data before resetting state
        val currentState = _state.value as? DriverScreenState.ConfirmedRoute
        if (currentState != null) {
            _completedTripSummary.value = CompletedTripData(
                road = currentState.road,
                clients = currentState.incomingClients,
                startingTime = currentState.startingTime
            )
        }

        // Show completion overlay
        _showCompletionOverlay.value = true

        // Mark route as finished in the database (don't delete it)
        FirebaseDatabaseHelper.updateRouteStatus(RouteStatus.Finished.msg)

        // Reset map state after 5 seconds, but keep overlay visible
        viewModelScope.launch {
            kotlinx.coroutines.delay(5000)
            _state.value = DriverScreenState.Initial()
        }
    }

    /**
     * Dismisses the completion overlay and clears trip data.
     * Called when user clicks the button on the completion screen.
     */
    fun dismissCompletionOverlay() {
        _showCompletionOverlay.value = false
        _completedTripSummary.value = null
    }

    private fun handleClientPickedUp(client: Client) {
        updateAccessByDriver(client.user, RouteStatus.Picked)
    }

    private fun handleClientDroppedOff(client: Client) {
        updateAccessByDriver(client.user, RouteStatus.Finished)
    }

    // endregion

    // region Helper Functions

    private inline fun updateConfirmedRouteState(
        crossinline update: DriverScreenState.ConfirmedRoute.() -> DriverScreenState.ConfirmedRoute
    ) {
        _state.update { currentState ->
            (currentState as? DriverScreenState.ConfirmedRoute)?.update() ?: currentState
        }
    }

    // endregion
}

/**
 * Represents the status of a route in the system.
 */
enum class RouteStatus(val msg: String) {
    Incoming("Incoming"),
    Active("Active"),
    Refused("Refused"),
    Finished("Finished"),
    Picked("Picked")
}

/**
 * Sealed class representing all possible states for the Driver screen.
 */
sealed class DriverScreenState(
    val road: List<LatLng>,
    val carPosition: Int = 0,
    val roadStarted: Boolean = false,
    val isRouteFinished: Boolean = false
) {

    data class Initial(private val start: LatLng? = null) : DriverScreenState(listOfNotNull(start))

    data class Loading(private val start: LatLng, private val end: LatLng) : DriverScreenState(
        listOf(start, end)
    )

    data class Loaded(private val fetchedRoad: List<LatLng>) : DriverScreenState(fetchedRoad)

    data class ConfirmedRoute(
        private val fetchedRoad: List<LatLng>,
        val incomingClients: List<Client>,
        val startingTime: Int = 0,
        val accepted: Boolean = false,
        private val position: Int = 0,
        private val started: Boolean = false,
        private val finished: Boolean = false
    ) : DriverScreenState(fetchedRoad, position, started, finished)

    /**
     * Helper property to determine if road should be visible.
     * Road is visible in Loaded and ConfirmedRoute states (when road has more than 1 point).
     */
    val shouldShowRoad: Boolean
        get() = (this is Loaded || this is ConfirmedRoute) && road.size > 1
}

/**
 * Data class to hold completed trip information for the overlay.
 */
data class CompletedTripData(
    val road: List<LatLng>,
    val clients: List<Client>,
    val startingTime: Int
)

