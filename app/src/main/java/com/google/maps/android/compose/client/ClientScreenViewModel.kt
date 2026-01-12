package com.google.maps.android.compose.client

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.driver.RouteStatus
import com.google.maps.android.compose.firebase.FirebaseDatabaseHelper
import com.google.maps.android.compose.firebase.FirebaseDatabaseHelper.observeRoutes
import com.google.maps.android.compose.firebase.Route
import com.google.maps.android.compose.firebase.gaussianDistance
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
        viewModelScope.launch {
            observeRoutes { routes ->
                _routes.value = routes
                _state.value.acceptedRoute?.let { stateAcceptedRoute ->
                    routes.find { route ->
                        route.userId == stateAcceptedRoute.userId
                    }?.let { acceptedRoute ->
                        log.d { "acceptedRoute: $acceptedRoute ${acceptedRoute.status}" }
                        acceptedRoute.clients.firstOrNull()?.status?.let { status ->
                            if (status == RouteStatus.Active.msg) {
                                _state.update { state -> state.copy(acceptedByDriver = true) }
                            } else if (status == RouteStatus.Refused.msg) {
                                _state.update { state ->
                                    state.copy(
                                        acceptedByDriver = false, acceptedRoute = null
                                    )
                                }
                            }
                        }
                    }


                }

                log.d { "routes: $routes" }
                updateState()
            }
        }
    }

    private fun updateState() {
        val details = _state.value.routeDetails ?: return
        val routes = _routes.value
        val resultRoutes = mutableListOf<Route>()

        routes.forEach { route ->
            with(route) {
                val closestStart = closestPoint(coordinates, details.start)
                val coordsAfterStart =
                    coordinates.slice(coordinates.indexOf(closestStart) until coordinates.size)
                val closestEnd = closestPoint(coordsAfterStart, details.end)
                val onTime = startingTime >= details.startTime
                val isIncoming = status == RouteStatus.Incoming.msg

                val isClose = gaussianDistance(
                    details.start, closestStart
                ) < DISTANCE_THRESHOLD && gaussianDistance(
                    details.end, closestEnd
                ) < DISTANCE_THRESHOLD

                log.d { "Route critterias: $onTime, $isIncoming, $isClose" }
                if (onTime && isIncoming && isClose) {
                    val intervalCoords = coordinates.slice(
                        coordinates.indexOf(closestStart)..coordinates.indexOf(closestEnd)
                    )
                    resultRoutes.add(route.copy(coordinates = intervalCoords))
                }
            }
        }
        _state.update { it.copy(routes = resultRoutes) }
    }

    fun addPoint(latLng: LatLng) {
        if (state.value.start == null) {
            _state.update { it.copy(start = latLng) }
        } else {
            _state.update { it.copy(end = latLng) }
        }
    }


    fun setDetails(start: LatLng, end: LatLng, startTime: Int = 1200) {
        _state.update { it.copy(routeDetails = ClientRouteDetails(start, end, startTime)) }
        updateState()
    }

    fun restartMarkers() {
        _state.update { it.copy(start = null, end = null) }
    }

    fun answerRoute(route: Route, accept: Boolean) {
        if (accept) {
            FirebaseDatabaseHelper.requestAccessFromDriver(
                route, route.coordinates.first(), route.coordinates.last()
            )
            _state.update { it.copy(acceptedRoute = route) }
        } else {
            _state.update { it.copy(routes = it.routes?.minus(route)) }
        }
    }

    private fun closestPoint(coordinates: List<LatLng>, point: LatLng): LatLng {
        return coordinates.minBy { gaussianDistance(it, point) }
    }
}


data class ClientScreenState(
    val acceptedRoute: Route? = null,
    val acceptedByDriver: Boolean? = null,
    val routes: List<Route>? = emptyList(),
    val routeDetails: ClientRouteDetails? = null,
    val start: LatLng? = null,
    val end: LatLng? = null,
)


data class ClientRouteDetails(
    val start: LatLng,
    val end: LatLng,
    val startTime: Int = 1200,
)