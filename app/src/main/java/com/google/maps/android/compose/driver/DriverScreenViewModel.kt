package com.google.maps.android.compose.driver

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.maps.android.PolyUtil
import com.google.maps.android.compose.firebase.Client
import com.google.maps.android.compose.firebase.FirebaseDatabaseHelper
import com.google.maps.android.compose.firebase.FirebaseDatabaseHelper.observeIncomingClients
import com.google.maps.android.compose.firebase.FirebaseDatabaseHelper.updateAccessByDriver
import com.google.maps.android.compose.firebase.Route
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
    private val log = logging()

    init {
        viewModelScope.launch {
            _state.filter {
                it is DriverScreenState.ConfirmedRoute
            }.distinctUntilChanged().collectLatest {
                observeIncomingClients { clients ->
                    _state.update { (it as DriverScreenState.ConfirmedRoute).copy(incomingClients = clients) }
                }
            }
        }
    }

    fun onMapClick(latLng: LatLng) {
        (_state.value as? DriverScreenState.Initial)?.let { curState ->
            if (curState.road.isEmpty()) {
                _state.value = curState.copy(start = latLng)
            } else {
                loadRoute(DriverScreenState.Loading(curState.road.first(), latLng))
            }
        }
    }

    /**
     * For given [start] and [end] points, updates the state to contain the shortest route as a list of points
     * */
    private fun loadRoute(newState: DriverScreenState.Loading) {
        viewModelScope.launch {
            _state.value = newState

//            val encoded = NetworkRepository.requestPath(newState.road[0], newState.road[1])

            val encoded = "gyenGgr~}CVVHL@h@@lA@fCDnKrBAJO^Aj@@GaREeJKc[Ow^G}@SiAuAcGUkAQsBGoBQyA[yA]iAeDyI{@kCaBgGOk@{@_Di@uAWqAAWBS|@}BtHsQ|A{DwIuNiAoBiGaKuDiGCI?o@@kAI}IMeF\\}LDwDDuARqDAiB]aIMgDC}DIoAsB_LIUR]x@oAl@q@`@[HWBYGk@eBcHa@sEEe@rHuCrHcDlKiEJDJ@JCLKFWAe@O[IE]_AE[c@yD}@{HCgBKiAKe@M@YBD|@Fj@\\jAH`@ZvC\\bDV`CQDED"
            log.d { "Route encoded: $encoded" }

            // other: "a`inGich~Cc@cHlEc@zEe@zEe@fAIzAAQhIzDClAAAl@Er@Ib@ODOLKPGVAX@ZFR@BMZkAvCaExJkB|E_A~BPXHXH^hAxDhBvGzEuClBkA|@e@nAm@hBaA`@YXU]cAAK?EFQCQDQFG"
            // university: gyenGgr~}CVVHL@h@@lA@fCDnKrBAJO^Aj@@GaREeJKc[Ow^G}@SiAuAcGUkAQsBGoBQyA[yA]iAeDyI{@kCaBgGOk@{@_Di@uAWqAAWBS|@}BtHsQ|A{DwIuNiAoBiGaKuDiGCI?o@@kAI}IMeF\}LDwDDuARqDAiB]aIMgDC}DIoAsB_LIUR]x@oAl@q@`@[HWBYGk@eBcHa@sEEe@rHuCrHcDlKiEJDJ@JCLKFWAe@O[IE]_AE[c@yD}@{HCgBKiAKe@M@YBD|@Fj@\jAH`@ZvC\bDV`CQDED
            val pointsUnfiltered = PolyUtil.decode(encoded)

            _state.value = DriverScreenState.Loaded(pointsUnfiltered)
        }
    }

    fun postRoute(startTime: Int = 1200) {
        (_state.value as? DriverScreenState.Loaded)?.let {
            val newRoute = Route(
                userId = FirebaseAuth.getInstance().currentUser!!.uid,
                coordinates = it.road,
                status = RouteStatus.Incoming.msg,
                startTime,
                clients = emptyList()
            )
            FirebaseDatabaseHelper.addRoute(newRoute)

            _state.value = DriverScreenState.ConfirmedRoute(it.road, emptyList())
        }
    }

    fun declineRoute() {
        (_state.value as? DriverScreenState.Loaded)?.let {

            _state.value = DriverScreenState.Initial()
        }
    }

    fun answerClient(client: Client, status: RouteStatus) {
        updateAccessByDriver(client.user, status)
        _state.update {
            (it as DriverScreenState.ConfirmedRoute).copy(accepted = true)
        }
    }
}

enum class RouteStatus(val msg: String) {

    Incoming("Incoming"), Active("Active"), Refused("Refused"), Finished("Finished"), Picked("Picked")
}

sealed class DriverScreenState(val road: List<LatLng>) {

    data class Initial(private val start: LatLng? = null) : DriverScreenState(listOfNotNull(start))

    data class Loading(private val start: LatLng, private val end: LatLng) : DriverScreenState(
        listOf(start, end)
    )

    data class Loaded(private val fetchedRoad: List<LatLng>) : DriverScreenState(fetchedRoad)

    data class ConfirmedRoute(
        private val fetchedRoad: List<LatLng>,
        val incomingClients: List<Client>,
        val accepted: Boolean = false,
    ) : DriverScreenState(fetchedRoad)
}

