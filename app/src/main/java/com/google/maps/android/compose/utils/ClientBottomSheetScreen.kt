package com.google.maps.android.compose.utils

import android.R.attr.fontWeight
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import com.google.maps.android.compose.R
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.TimePickerLayoutType
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.client.ClientScreenState
import com.google.maps.android.compose.firebase.Route
import com.google.maps.android.compose.theme.backgroundColor
import com.google.maps.android.compose.utils.RouteMetaInfoRow
import java.util.Calendar


// Constants for reusable values
private object ClientBottomSheetConstants {
    val PRIMARY_BLUE = Color(0xFF0B57D0)
    val ACCENT_GREEN = Color(0xFF17EABA)
    val ACCEPT_BUTTON_COLOR = Color(0xFF2B6777)
    val DECLINE_BUTTON_COLOR = Color(0xFF80A9A1)
    val VERTICAL_SPACING = 20.dp
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientBottomSheetScreen(
    state: ClientScreenState,
    answerRoute: (Route, Boolean) -> Unit,
    setDetails: (LatLng, LatLng) -> Unit,
    restartMarkers: () -> Unit,
    content: @Composable () -> Unit,
) {
    val scaffoldState = rememberBottomSheetScaffoldState()
    BottomSheetScreen(scaffoldState, sheetContent = {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(bottom = 60.dp, top = 56.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            when {
                state.start == null || state.end == null -> {
                    InitialStateViewClient()
                }

                state.routeDetails == null -> {
                    RouteDetailsView(
                        setDetails = { setDetails(state.start, state.end) },
                        restartMarkers = restartMarkers
                    )
                }

                state.acceptedRoute != null -> {
                    AcceptedRouteView(state.acceptedByDriver)
                }

                state.routes.isNullOrEmpty().not() -> {
                    AvailableRoutesView(state.routes, answerRoute)
                }

                else -> {
                    WaitingForDriversView()
                }
            }
        }
    }) {
        content()
    }
}

@Composable
fun InitialStateViewClient() {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Welcome!",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Click on the map to select a start and end point.",
            textAlign = TextAlign.Center,
            color = Color.Gray
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteDetailsView(setDetails: () -> Unit, restartMarkers: () -> Unit) {
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
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TimePicker(state = timePickerState)
        RouteActionButtons(
            postRoute = setDetails, declineRoute = restartMarkers
        )
    }
}

@Composable
fun AcceptedRouteView(acceptedByDriver: Boolean?) {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val message = when (acceptedByDriver) {
            true -> "Route accepted by driver! Meeting with driver at 12:05"
            null -> "Route confirmed! Waiting for driver to accept..."
            false -> "Route declined! Try again."
        }
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = if (acceptedByDriver == false) Color.Red else Color.Black
        )
    }
}

@Composable
fun AvailableRoutesView(
    routes: List<Route>,
    answerRoute: (Route, Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        routes.forEachIndexed { index, route ->
            RouteCard(route = route, onAnswer = answerRoute)

            if (index != routes.lastIndex) {
                Divider(
                    color = Color.LightGray,
                    thickness = 1.dp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
fun RouteCard(route: Route, onAnswer: (Route, Boolean) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        DriverInfoRow(route.startingTime)

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            DeclineButton(onClick = { onAnswer(route, false) })
            AcceptButton(onClick = { onAnswer(route, true) }, text = "Accept")
        }
    }
}

@Composable
private fun DriverInfoRow(startingTime: Int) {
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
                text = "Elena", // Replace with route.driverName if needed
                fontWeight = FontWeight.SemiBold,
                fontSize = 24.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            RouteMetaInfoRow(startingTime)
        }
    }
}

@Composable
private fun RouteMetaInfoRow(startingTime: Int) {
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
            text = "\uD83D\uDCB6 LEI 12.50",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF027368)
        )
        Text(
            text = "⏰ 12.05",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFE91E63)
        )
    }
}
@Composable
fun WaitingForDriversView() {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Waiting for drivers...",
            fontStyle = FontStyle.Italic,
            fontSize = 24.sp,
            color = Color.Gray
        )
        CircularProgressIndicator(modifier = Modifier.padding(top = 8.dp))
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
            containerColor = ClientBottomSheetConstants.DECLINE_BUTTON_COLOR,
            contentColor = Color.White
        ),
    ) {
        ActionButtonContent(
            icon = Icons.Filled.Close, text = "Cancel"
        )
    }
}

@Composable
private fun AcceptButton(onClick: () -> Unit, text: String = "Continue") {
    Button(
        onClick = onClick, colors = ButtonDefaults.buttonColors(
            containerColor = ClientBottomSheetConstants.ACCEPT_BUTTON_COLOR,
            contentColor = Color.White
        )
    ) {
        ActionButtonContent(
            icon = Icons.Filled.Done, text = text
        )
    }
}

@Composable
private fun ActionButtonContent(
    icon: ImageVector, text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "",
            tint = ClientBottomSheetConstants.ACCENT_GREEN
        )
        Text(text = text)
    }
}

@Preview
@Composable
private fun LoadedViewPreview() {
    Surface(modifier = Modifier.fillMaxSize()) {
        AvailableRoutesView(listOf(Route(), Route()), answerRoute = { _, _ -> })
    }
}


//@Preview
//@Composable
//private fun Preview2() {
//    Surface(modifier = Modifier.fillMaxSize()) {
//        RouteDetailsView(setDetails = {}, restartMarkers = {})
//    }
//
//}
