package com.google.maps.android.compose.utils

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.maps.android.compose.R
import com.google.maps.android.compose.driver.DriverScreenState
import com.google.maps.android.compose.driver.RouteStatus
import com.google.maps.android.compose.firebase.Client
import com.google.maps.android.compose.utils.RouteMetaInfoRow
import java.util.Calendar

// Constants for reusable values
private object DriverBottomSheetConstants {
    val PRIMARY_BLUE = Color(0xFF0B57D0)
    val ACCENT_GREEN = Color(0xFF17EABA)
    val ACCEPT_BUTTON_COLOR = Color(0xFF2B6777)
    val DECLINE_BUTTON_COLOR = Color(0xFF80A9A1)
    val VERTICAL_SPACING = 20.dp
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverBottomSheetScreen(
    state: DriverScreenState,
    postRoute: () -> Unit,
    declineRoute: () -> Unit,
    answerClient: (Client, RouteStatus) -> Unit,
    content: @Composable () -> Unit,
) {
    val scaffoldState = rememberBottomSheetScaffoldState()

    BottomSheetScreen(
        scaffoldState = scaffoldState, sheetContent = {
            DriverBottomSheetContent(
                state = state,
                postRoute = postRoute,
                declineRoute = declineRoute,
                answerClient = answerClient
            )
        }) {
        content()
    }
}

@Composable
private fun DriverBottomSheetContent(
    state: DriverScreenState,
    postRoute: () -> Unit,
    declineRoute: () -> Unit,
    answerClient: (Client, RouteStatus) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                bottom = 60.dp, top = 56.dp
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(DriverBottomSheetConstants.VERTICAL_SPACING)
    ) {
        when (state) {
            is DriverScreenState.Initial -> InitialStateView()
            is DriverScreenState.Loading -> LoadingStateView()
            is DriverScreenState.Loaded -> LoadedStateView(
                postRoute = postRoute, declineRoute = declineRoute
            )

            is DriverScreenState.ConfirmedRoute -> ConfirmedRouteStateView(
                incomingClients = state.incomingClients, answerClient = answerClient
            )
        }
    }
}

@Composable
private fun ConfirmedRouteStateView(
    incomingClients: List<Client>,
    answerClient: (Client, RouteStatus) -> Unit,
) {
    if (incomingClients.isNotEmpty()) {
        Column(
            modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            incomingClients.forEachIndexed { index, client ->
                ClientCard(
                    client = client,
                    onAccept = { answerClient(client, RouteStatus.Active) },
                    onDecline = { answerClient(client, RouteStatus.Refused) },
                    index = index,
                )
            }
        }
    } else {
        WaitingForClientsView()
    }
}

@Composable
private fun WaitingForClientsView() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Waiting for clients",
            fontStyle = FontStyle.Italic,
            fontSize = 24.sp,
            color = Color.Gray
        )
        CircularProgressIndicator()
    }
}

@Composable
private fun InitialStateView() {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Welcome!",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Click on the map to select a start and end point.",
            textAlign = TextAlign.Center,
            color = Color.Gray
        )
    }
}

@Composable
private fun LoadingStateView() {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(modifier = Modifier.padding(bottom = 8.dp))
        Text(
            text = "Calculating route...",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LoadedStateView(
    postRoute: () -> Unit, declineRoute: () -> Unit
) {
    var continueClicked by remember { mutableStateOf(false) }

    val currentTime = Calendar.getInstance()

    val timePickerState = rememberTimePickerState(
        initialHour = currentTime.get(Calendar.HOUR_OF_DAY),
        initialMinute = currentTime.get(Calendar.MINUTE),
        is24Hour = true,
    )

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.Start
    ) {

        if (!continueClicked) {
            Text(
                "Universitatea din București", fontWeight = FontWeight.Medium, fontSize = 24.sp
            )
            RouteDurationInfo()
            RouteActionButtons(postRoute = { continueClicked = true }, declineRoute = declineRoute)
        } else {
            Box(modifier = Modifier.fillMaxWidth()) {
                TimePicker(
                    state = timePickerState, modifier = Modifier.align(Alignment.Center)
                )
            }

            RouteActionButtons(
                postRoute = postRoute,
                declineRoute = { continueClicked = false },
            )
        }
    }
}

@Composable
private fun RouteDurationInfo() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painter = painterResource(R.drawable.car),
            contentDescription = "Location icon",
            tint = DriverBottomSheetConstants.PRIMARY_BLUE,
            modifier = Modifier.size(48.dp)
        )
        Text(
            text = "23 min",
            modifier = Modifier.padding(start = 8.dp),
            fontSize = 22.sp,
            color = DriverBottomSheetConstants.PRIMARY_BLUE
        )
    }
}

@Composable
private fun RouteActionButtons(
    postRoute: () -> Unit, declineRoute: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
    ) {
        DeclineButton(onClick = declineRoute)
        AcceptButton(onClick = postRoute)
    }
}

@Composable
private fun DeclineButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = DriverBottomSheetConstants.DECLINE_BUTTON_COLOR,
            contentColor = Color.White
        ),
    ) {
        ActionButtonContent(
            icon = Icons.Filled.Close, text = "Cancel"
        )
    }
}

@Composable
private fun AcceptButton(onClick: () -> Unit) {
    Button(
        onClick = onClick, colors = ButtonDefaults.buttonColors(
            containerColor = DriverBottomSheetConstants.ACCEPT_BUTTON_COLOR,
            contentColor = Color.White
        )
    ) {
        ActionButtonContent(
            icon = Icons.Filled.Done, text = "Continue"
        )
    }
}

@Composable
private fun ActionButtonContent(
    icon: androidx.compose.ui.graphics.vector.ImageVector, text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "",
            tint = DriverBottomSheetConstants.ACCENT_GREEN
        )
        Text(text = text)
    }
}

@Composable
private fun ClientCard(
    client: Client, onAccept: () -> Unit, onDecline: () -> Unit, index: Int,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ClientInfoSection(client, index)
        if (client.status == "Waiting") {
            ClientActionButtons(
                onAccept = onAccept, onDecline = onDecline
            )
        }
    }
}

@Composable
private fun ClientInfoSection(client: Client, index: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
        modifier = Modifier.fillMaxWidth()
    ) {
        Image(
            painter = painterResource(id = R.drawable.driver),
            contentDescription = "Driver Profile",
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color.LightGray)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Elena",
                fontWeight = FontWeight.SemiBold,
                fontSize = 24.sp
            )
            Text("Client ${client.status}", color = Color.LightGray, fontStyle = FontStyle.Italic, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(8.dp))

            RouteMetaInfoRow(index)
        }
    }
}

@Composable
private fun RouteMetaInfoRow(index: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "⭐ 5 / 5",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFE1CF6E)
        )
        Text(
            text = "\uD83D\uDCB6 LEI ${if (index == 0) "10.5" else "8.5"}",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF027368)
        )
        Text(
            text = "⏰ ${if (index == 0) "12.05" else "12.11"}",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFE91E63)
        )
    }
}

@Composable
private fun ClientActionButtons(
    onAccept: () -> Unit, onDecline: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
    ) {
        AcceptClientButton(onClick = onAccept)
        DeclineClientButton(onClick = onDecline)
    }
}

@Composable
private fun AcceptClientButton(onClick: () -> Unit) {
    TextButton(
        onClick = onClick, colors = ButtonDefaults.buttonColors(
            containerColor = DriverBottomSheetConstants.ACCEPT_BUTTON_COLOR,
            contentColor = Color.White
        )
    ) {
        Text("Accept")
    }
}

@Composable
private fun DeclineClientButton(onClick: () -> Unit) {
    TextButton(
        onClick = onClick, colors = ButtonDefaults.buttonColors(
            containerColor = DriverBottomSheetConstants.DECLINE_BUTTON_COLOR,
            contentColor = Color.White
        )
    ) {
        Text("Decline")
    }
}

@Preview
@Composable
private fun LoadedViewPreview() {
    LoadedStateView(postRoute = {}, declineRoute = {})
}

@Preview
@Composable
private fun LoadedViewPreview2() {
    Surface(modifier = Modifier.fillMaxSize()) {
        ClientInfoSection(client = Client(), 0)
    }
}