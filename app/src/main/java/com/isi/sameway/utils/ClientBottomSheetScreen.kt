package com.isi.sameway.utils

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.model.LatLng
import com.isi.sameway.R
import com.isi.sameway.client.ClientScreenState
import com.isi.sameway.client.MatchedRoute
import com.isi.sameway.firebase.Route
import com.isi.sameway.theme.AppColors
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientBottomSheetScreen(
    state: ClientScreenState,
    answerRoute: (MatchedRoute, Boolean) -> Unit,
    setDetails: (LatLng, LatLng, Int) -> Unit,
    restartMarkers: () -> Unit,
    content: @Composable () -> Unit,
) {
    val scaffoldState = rememberBottomSheetScaffoldState()

    BottomSheetScreen(
        scaffoldState = scaffoldState,
        sheetContent = {
            ClientBottomSheetContent(
                state = state,
                answerRoute = answerRoute,
                setDetails = setDetails,
                restartMarkers = restartMarkers
            )
        }
    ) {
        content()
    }
}

@Composable
private fun ClientBottomSheetContent(
    state: ClientScreenState,
    answerRoute: (MatchedRoute, Boolean) -> Unit,
    setDetails: (LatLng, LatLng, Int) -> Unit,
    restartMarkers: () -> Unit
) {
    // Derive a stable key that only changes when the logical screen state changes
    // This prevents re-animation when carPosition updates
    val stateKey = when {
        state.start == null || state.end == null -> "welcome"
        state.routeDetails == null -> "set_time"
        state.acceptedRoute != null -> "ride_status_${state.acceptedByDriver}"
        !state.routes.isNullOrEmpty() -> "available_drivers"
        else -> "searching"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Progress indicator
        ClientProgressIndicator(state = state)

        Spacer(modifier = Modifier.height(12.dp))

        AnimatedContent(
            targetState = stateKey,
            transitionSpec = {
                fadeIn() + slideInVertically { height -> height / 4 } togetherWith
                        fadeOut() + slideOutVertically { height -> -height / 4 }
            },
            label = "client_state_transition"
        ) { targetKey ->
            when (targetKey) {
                "welcome" -> {
                    ClientWelcomeView()
                }
                "set_time" -> {
                    SetDepartureTimeView(
                        start = state.start!!,
                        end = state.end!!,
                        setDetails = setDetails,
                        restartMarkers = restartMarkers
                    )
                }
                "searching" -> {
                    SearchingDriversView()
                }
                "available_drivers" -> {
                    AvailableDriversView(
                        routes = state.routes ?: emptyList(),
                        clientRequestedTime = state.routeDetails?.startTime ?: 0,
                        answerRoute = answerRoute
                    )
                }
                else -> {
                    // Ride status states
                    val matchedRoute = state.acceptedRoute
                    val driverStartTime = matchedRoute?.route?.startingTime ?: 0
                    val clientPickupTime = state.routeDetails?.startTime ?: 0
                    val clientRoute = matchedRoute?.route?.coordinates ?: emptyList()
                    val driverCarPosition = state.carPosition
                    val driverRouteStartIndex = matchedRoute?.driverRouteStartIndex ?: 0
                    // Get the driver route coordinates up to the client pickup point
                    val driverRouteToPickup = matchedRoute?.route?.coordinates?.take(driverRouteStartIndex + 1) ?: emptyList()

                    RideStatusView(
                        acceptedByDriver = state.acceptedByDriver,
                        clientPickupTime = clientPickupTime,
                        driverStartTime = driverStartTime,
                        driverRouteCoordinates = driverRouteToPickup,
                        clientRouteCoordinates = clientRoute,
                        currentCarPosition = driverCarPosition,
                        roadStarted = state.roadStarted
                    )
                }
            }
        }
    }
}

@Composable
private fun ClientProgressIndicator(state: ClientScreenState) {
    val steps = listOf("Set Route", "Find Ride", "Ride")
    val currentStep = when {
        state.start == null || state.end == null -> 0
        state.acceptedRoute == null -> 1
        else -> 2
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEachIndexed { index, step ->
            ClientStepIndicator(
                stepNumber = index + 1,
                label = step,
                isActive = index == currentStep,
                isCompleted = index < currentStep
            )
            if (index < steps.lastIndex) {
                Box(
                    modifier = Modifier
                        .width(24.dp)
                        .height(2.dp)
                        .background(
                            if (index < currentStep) AppColors.AcceptButtonColor
                            else Color.LightGray
                        )
                )
            }
        }
    }
}

@Composable
private fun ClientStepIndicator(
    stepNumber: Int,
    label: String,
    isActive: Boolean,
    isCompleted: Boolean
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(
                    when {
                        isCompleted -> AppColors.AcceptButtonColor
                        isActive -> AppColors.PrimaryBlue
                        else -> Color.LightGray
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isCompleted) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(12.dp)
                )
            } else {
                Text(
                    text = stepNumber.toString(),
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 9.sp,
            color = if (isActive) AppColors.PrimaryBlue else Color.Gray,
            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
private fun ClientWelcomeView() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = AppColors.PrimaryBlue
                )
                Text(
                    text = "Tap map to set route",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1A1A1A)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                InfoChipClient(
                    icon = Icons.Default.Place,
                    label = "Pickup",
                    color = AppColors.AcceptButtonColor
                )
                Text(" → ", color = Color.Gray, fontSize = 12.sp)
                InfoChipClient(
                    icon = Icons.Default.LocationOn,
                    label = "Drop-off",
                    color = AppColors.TimeColor
                )
            }
        }
    }
}

@Composable
private fun InfoChipClient(icon: ImageVector, label: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = color
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = Color.DarkGray,
            fontWeight = FontWeight.Medium
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SetDepartureTimeView(
    start: LatLng,
    end: LatLng,
    setDetails: (LatLng, LatLng, Int) -> Unit,
    restartMarkers: () -> Unit
) {
    val currentTime = Calendar.getInstance()

    val timePickerState = rememberTimePickerState(
        initialHour = currentTime.get(Calendar.HOUR_OF_DAY),
        initialMinute = currentTime.get(Calendar.MINUTE),
        is24Hour = true,
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    Icons.Default.DateRange,
                    contentDescription = null,
                    tint = AppColors.PrimaryBlue,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Departure Time",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }

            TimePicker(state = timePickerState)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = restartMarkers,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = AppColors.DeclineButtonColor
                    )
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reset", fontSize = 13.sp)
                }

                Button(
                    onClick = {
                        val startTime = timePickerState.hour * 100 + timePickerState.minute
                        setDetails(start, end, startTime)
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.AcceptButtonColor
                    )
                ) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Find Rides", fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun SearchingDriversView() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F7FF)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = AppColors.PrimaryBlue,
                strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Searching for rides...",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                color = Color.DarkGray
            )
        }
    }
}

@Composable
private fun AvailableDriversView(
    routes: List<MatchedRoute>,
    clientRequestedTime: Int,
    answerRoute: (MatchedRoute, Boolean) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Compact Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Available Rides",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Surface(
                color = AppColors.AcceptButtonColor,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "${routes.size} found",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        routes.forEach { matchedRoute ->
            DriverOfferCard(
                matchedRoute = matchedRoute,
                clientRequestedTime = clientRequestedTime,
                onAccept = { answerRoute(matchedRoute, true) },
                onDecline = { answerRoute(matchedRoute, false) }
            )
        }
    }
}

@Composable
private fun DriverOfferCard(
    matchedRoute: MatchedRoute,
    clientRequestedTime: Int,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    // Client price includes the 20% app fee
    val clientPrice = RouteCalculations.calculateClientPrice(matchedRoute.route.coordinates)
    val driverStartTime = matchedRoute.route.startingTime
    val clientPickupTime = clientRequestedTime

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Driver info row - compact
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.driver),
                    contentDescription = "Driver Profile",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray)
                )

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Elena",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text("⭐ 5.0", fontSize = 11.sp, color = AppColors.RatingColor)
                    }
                    Text(
                        text = "Departs ${TimeUtils.formatTime(driverStartTime)}",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }

                // Price badge
                Surface(
                    color = Color(0xFFE8F5E9),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "$clientPrice LEI",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.PriceColor
                    )
                }
            }

            // Pickup time row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF8F8F8))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🕐 Pickup at ", fontSize = 12.sp, color = Color.Gray)
                Text(
                    text = TimeUtils.formatTime(clientPickupTime),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.TimeColor
                )
            }

            // Action buttons - compact
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onDecline,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFFD32F2F)
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text("Skip", fontSize = 13.sp)
                }

                Button(
                    onClick = onAccept,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.AcceptButtonColor
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text("Request", fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun RideStatusView(
    acceptedByDriver: Boolean?,
    clientPickupTime: Int,
    driverStartTime: Int,
    driverRouteCoordinates: List<LatLng>,
    clientRouteCoordinates: List<LatLng>,
    currentCarPosition: Int,
    roadStarted: Boolean
) {
    val formattedClientTime = TimeUtils.formatTime(clientPickupTime)
    val clientPrice = RouteCalculations.calculateClientPrice(clientRouteCoordinates)

    // Calculate dynamic time to arrival based on remaining driver route coordinates
    val minutesAway = if (roadStarted && driverRouteCoordinates.isNotEmpty()) {
        val remainingCoordinates = driverRouteCoordinates.drop(currentCarPosition)
        RouteCalculations.calculateTravelTimeMinutes(remainingCoordinates)
    } else if (acceptedByDriver == true && driverRouteCoordinates.isNotEmpty()) {
        RouteCalculations.calculateTravelTimeMinutes(driverRouteCoordinates)
    } else {
        0
    }

    val (icon, iconTint, backgroundColor, title, message) = when (acceptedByDriver) {
        true -> RideStatusInfo(
            icon = Icons.Default.Done,
            iconTint = AppColors.AcceptButtonColor,
            backgroundColor = Color(0xFFE8F5E9),
            title = "Ride Confirmed! 🎉",
            message = "Pickup at $formattedClientTime"
        )
        null -> RideStatusInfo(
            icon = Icons.Default.Info,
            iconTint = AppColors.PrimaryBlue,
            backgroundColor = Color(0xFFF0F7FF),
            title = "Waiting for Driver",
            message = "Request sent, awaiting response..."
        )
        false -> RideStatusInfo(
            icon = Icons.Default.Warning,
            iconTint = Color(0xFFD32F2F),
            backgroundColor = Color(0xFFFFEBEE),
            title = "Request Declined",
            message = "Please try another ride"
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = iconTint
                )
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A)
                    )
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
                if (acceptedByDriver == null) {
                    Spacer(modifier = Modifier.weight(1f))
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = AppColors.PrimaryBlue,
                        strokeWidth = 2.dp
                    )
                }
            }

            if (acceptedByDriver == true) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.6f))
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Text("🚗 Elena", fontSize = 12.sp, color = Color.DarkGray)
                    Text("💰 $clientPrice LEI", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = AppColors.PriceColor)
                }
            }
        }
    }
}

private data class RideStatusInfo(
    val icon: ImageVector,
    val iconTint: Color,
    val backgroundColor: Color,
    val title: String,
    val message: String
)

@Preview(showBackground = true)
@Composable
private fun ClientWelcomePreview() {
    ClientWelcomeView()
}

@Preview(showBackground = true)
@Composable
private fun SearchingDriversPreview() {
    SearchingDriversView()
}

@Preview(showBackground = true)
@Composable
private fun DriverOfferPreview() {
    DriverOfferCard(
        matchedRoute = MatchedRoute(Route(), 0),
        clientRequestedTime = 1430,
        onAccept = {},
        onDecline = {}
    )
}

@Preview(showBackground = true)
@Composable
private fun RideConfirmedPreview() {
    RideStatusView(
        acceptedByDriver = true,
        clientPickupTime = 1430,
        driverStartTime = 1400,
        driverRouteCoordinates = listOf(
            LatLng(44.4268, 26.1025),
            LatLng(44.4368, 26.1125)
        ),
        clientRouteCoordinates = listOf(
            LatLng(44.4368, 26.1125),
            LatLng(44.4468, 26.1225)
        ),
        currentCarPosition = 0,
        roadStarted = false
    )
}
