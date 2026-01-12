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
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Place
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.isi.sameway.R
import com.isi.sameway.driver.DriverScreenState
import com.isi.sameway.driver.RouteStatus
import com.isi.sameway.firebase.Client
import com.isi.sameway.theme.AppColors
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverBottomSheetScreen(
    state: DriverScreenState,
    postRoute: (Int) -> Unit,
    declineRoute: () -> Unit,
    answerClient: (Client, RouteStatus) -> Unit,
    content: @Composable () -> Unit,
) {
    val scaffoldState = rememberBottomSheetScaffoldState()

    BottomSheetScreen(
        scaffoldState = scaffoldState,
        sheetContent = {
            DriverBottomSheetContent(
                state = state,
                postRoute = postRoute,
                declineRoute = declineRoute,
                answerClient = answerClient
            )
        }
    ) {
        content()
    }
}

@Composable
private fun DriverBottomSheetContent(
    state: DriverScreenState,
    postRoute: (Int) -> Unit,
    declineRoute: () -> Unit,
    answerClient: (Client, RouteStatus) -> Unit,
) {
    // Derive a stable key that only changes when the logical screen state changes
    // This prevents re-animation when carPosition updates
    val stateKey = state::class.simpleName ?: "unknown"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Progress indicator for current step
        DriverProgressIndicator(state = state)

        Spacer(modifier = Modifier.height(12.dp))

        AnimatedContent(
            targetState = stateKey,
            transitionSpec = {
                fadeIn() + slideInVertically { height -> height / 4 } togetherWith
                        fadeOut() + slideOutVertically { height -> -height / 4 }
            },
            label = "state_transition"
        ) { targetKey ->
            when (targetKey) {
                "Initial" -> WelcomeView()
                "Loading" -> RouteCalculatingView()
                "Loaded" -> RouteConfirmationView(
                    postRoute = { startTime -> postRoute(startTime) },
                    declineRoute = declineRoute
                )
                else -> {
                    // ConfirmedRoute state
                    val confirmedState = state as? DriverScreenState.ConfirmedRoute
                    ActiveRouteView(
                        incomingClients = confirmedState?.incomingClients ?: emptyList(),
                        startingTime = confirmedState?.startingTime ?: 0,
                        answerClient = answerClient
                    )
                }
            }
        }
    }
}

@Composable
private fun DriverProgressIndicator(state: DriverScreenState) {
    val steps = listOf("Select Route", "Confirm", "Active")
    val currentStep = when (state) {
        is DriverScreenState.Initial, is DriverScreenState.Loading -> 0
        is DriverScreenState.Loaded -> 1
        is DriverScreenState.ConfirmedRoute -> 2
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEachIndexed { index, step ->
            StepIndicator(
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
private fun StepIndicator(
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
private fun WelcomeView() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF5F5F5)
        ),
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
                    text = "Tap map to create route",
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
                InfoChip(
                    icon = Icons.Default.Place,
                    label = "Start",
                    color = AppColors.AcceptButtonColor
                )
                Text(" → ", color = Color.Gray, fontSize = 12.sp)
                InfoChip(
                    icon = Icons.Default.LocationOn,
                    label = "End",
                    color = AppColors.TimeColor
                )
            }
        }
    }
}

@Composable
private fun InfoChip(icon: ImageVector, label: String, color: Color) {
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

@Composable
private fun RouteCalculatingView() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
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
                text = "Calculating route...",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                color = Color.DarkGray
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RouteConfirmationView(
    postRoute: (Int) -> Unit,
    declineRoute: () -> Unit
) {
    var showTimePicker by remember { mutableStateOf(false) }
    val currentTime = Calendar.getInstance()

    val timePickerState = rememberTimePickerState(
        initialHour = currentTime.get(Calendar.HOUR_OF_DAY),
        initialMinute = currentTime.get(Calendar.MINUTE),
        is24Hour = true,
    )

    AnimatedContent(
        targetState = showTimePicker,
        label = "time_picker_transition"
    ) { showingTimePicker ->
        if (!showingTimePicker) {
            RouteDetailsCard(
                onContinue = { showTimePicker = true },
                onCancel = declineRoute
            )
        } else {
            DepartureTimeCard(
                timePickerState = timePickerState,
                onConfirm = {
                    val startTime = timePickerState.hour * 100 + timePickerState.minute
                    postRoute(startTime)
                },
                onBack = { showTimePicker = false }
            )
        }
    }
}

@Composable
private fun RouteDetailsCard(
    onContinue: () -> Unit,
    onCancel: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Route header - compact
            Text(
                text = "Route Ready",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )

            // Route stats - compact row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF8F8F8))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                CompactStatItem(emoji = "⏱", value = "23 min")
                CompactStatItem(emoji = "📍", value = "12.5 km")
                CompactStatItem(emoji = "👥", value = "4 seats")
            }

            // Action buttons - compact
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = AppColors.DeclineButtonColor
                    )
                ) {
                    Text("Cancel", fontSize = 13.sp)
                }

                Button(
                    onClick = onContinue,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.AcceptButtonColor
                    )
                ) {
                    Text("Set Time", fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun CompactStatItem(emoji: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(emoji, fontSize = 12.sp)
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.DarkGray
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DepartureTimeCard(
    timePickerState: androidx.compose.material3.TimePickerState,
    onConfirm: () -> Unit,
    onBack: () -> Unit
) {
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
                    onClick = onBack,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Back", fontSize = 13.sp)
                }

                Button(
                    onClick = onConfirm,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.AcceptButtonColor
                    )
                ) {
                    Text("Confirm", fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun ActiveRouteView(
    incomingClients: List<Client>,
    startingTime: Int,
    answerClient: (Client, RouteStatus) -> Unit,
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
                text = "Ride Requests",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            if (incomingClients.isNotEmpty()) {
                Surface(
                    color = AppColors.PrimaryBlue,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "${incomingClients.size} pending",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        if (incomingClients.isEmpty()) {
            WaitingForRidersCard()
        } else {
            incomingClients.forEachIndexed { index, client ->
                RiderRequestCard(
                    client = client,
                    index = index,
                    startingTime = startingTime,
                    onAccept = { answerClient(client, RouteStatus.Active) },
                    onDecline = { answerClient(client, RouteStatus.Refused) }
                )
            }
        }
    }
}

@Composable
private fun WaitingForRidersCard() {
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
                text = "Waiting for riders...",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                color = Color.DarkGray
            )
        }
    }
}

@Composable
private fun RiderRequestCard(
    client: Client,
    index: Int,
    startingTime: Int,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    val isWaiting = client.status == "Waiting"
    val clientRequestedTime = client.requestedTime
    val formattedTime = if (clientRequestedTime > 0) {
        TimeUtils.formatTime(clientRequestedTime)
    } else {
        TimeUtils.formatTime(TimeUtils.addMinutes(startingTime, (index + 1) * 5))
    }
    val driverEarning = RouteCalculations.calculateDriverPrice(client.routeSegmentSize)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isWaiting) Color.White else Color(0xFFF5F5F5)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isWaiting) 2.dp else 0.dp
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Rider info row - compact
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.driver),
                    contentDescription = "Rider Profile",
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
                            text = "Alexandru-Ioan",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        StatusBadge(status = client.status)
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("🕐 $formattedTime", fontSize = 11.sp, color = AppColors.TimeColor)
                        Text("💰 $driverEarning LEI", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = AppColors.PriceColor)
                    }
                }
            }

            // Action buttons (only show for waiting clients) - compact
            if (isWaiting) {
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
                        Text("Decline", fontSize = 13.sp)
                    }

                    Button(
                        onClick = onAccept,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColors.AcceptButtonColor
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text("Accept", fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(status: String) {
    val (backgroundColor, textColor) = when (status) {
        "Waiting" -> Color(0xFFFFF3E0) to Color(0xFFE65100)
        "Active" -> Color(0xFFE8F5E9) to Color(0xFF2E7D32)
        "Picked" -> Color(0xFFE3F2FD) to Color(0xFF1565C0)
        else -> Color(0xFFFFEBEE) to Color(0xFFC62828)
    }

    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(6.dp)
    ) {
        Text(
            text = status,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = textColor
        )
    }
}

@Composable
private fun StatChip(emoji: String, value: String, color: Color, label: String? = null) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(emoji, fontSize = 14.sp)
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = color
            )
        }
        if (label != null) {
            Text(
                text = label,
                fontSize = 10.sp,
                color = Color.Gray
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun WelcomePreview() {
    WelcomeView()
}

@Preview(showBackground = true)
@Composable
private fun RouteDetailsPreview() {
    RouteDetailsCard(onContinue = {}, onCancel = {})
}

@Preview(showBackground = true)
@Composable
private fun RiderRequestPreview() {
    RiderRequestCard(
        client = Client(status = "Waiting", routeSegmentSize = 50),
        index = 0,
        startingTime = 1200,
        onAccept = {},
        onDecline = {}
    )
}