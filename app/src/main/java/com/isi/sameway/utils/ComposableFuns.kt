package com.isi.sameway.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.RoundCap
import com.google.maps.android.PolyUtil
import com.google.maps.android.compose.Polyline
import kotlinx.serialization.Serializable
import org.lighthousegames.logging.logging


@Composable
fun DrawEncodedPolyline(polylinePoints: String) {
    val decodedPolyline = PolyUtil.decode(polylinePoints)
    val log = logging()

    DrawPoliline(decodedPolyline, color = 0x80E6E6FA, visible = true)
}

@Composable
fun DrawPoliline(
    points: List<LatLng>, color: Long = 0xFF00BFFF, visible: Boolean = true, zIndex: Float = 1f
) {
    Polyline(
        points = points,
        color = Color(color).copy(alpha = 200f),
        width = 14f,
        visible = visible,
        geodesic = true,
        zIndex = zIndex,
        startCap = RoundCap(),
        endCap = RoundCap(),
        onClick = {
        }
    )

}

@Composable
fun DrawPoliline(
    startLocation: LatLng, endLocation: LatLng, color: Long = 0xFF00BFFF, visible: Boolean = true, zIndex: Float = 1f
) {
    DrawPoliline(listOf(startLocation, endLocation), color, visible, zIndex)
}

//@OptIn(ExperimentalMaterial3Api::class)
//@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
//@Composable
//fun IncomingClientsBottomSheet(
//    content: @Composable () -> Unit,
//) {
//    val scope = rememberCoroutineScope()
//    var showBottomSheet by remember { mutableStateOf(false) }
//    val sheetState = rememberModalBottomSheetState()
//
//    var showClient0 by remember { mutableStateOf(true)}
//    var showClient1 by remember { mutableStateOf(true)}
//    var showClient2 by remember { mutableStateOf(true)}
//
//    Scaffold(
//        floatingActionButton = {
//            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
//                ExtendedFloatingActionButton(text = { Text(if (event >= 1) "Clienti" else "Incepe") },
//                    containerColor = Color.LightGray,
//                    icon = { Icon(Icons.Filled.Add, contentDescription = null) },
//                    onClick = { showBottomSheet = true })
//            }
//        },
//        modifier = Modifier
//            .fillMaxSize()
//            .systemBarsPadding(),
//    ) {
//        content()
//        if (showBottomSheet) {
//            ModalBottomSheet(sheetState = sheetState, shape = MaterialTheme.shapes.large.copy(
//                topStart = CornerSize(16.dp),
//                topEnd = CornerSize(16.dp),
//            ), containerColor = Color.White, onDismissRequest = { showBottomSheet = false }) {
//                Column(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(16.dp)
//                        .background(Color(0xFFF9FAFB)) // Light gray background
//                ) {
//                    if (showClient0 && event >= 2) {
//                        ClientItem(clientEntry = clients[0], onShowRoad = {
//                            showBottomSheet = false
//                            showRoad0()
//                        }, onAccept = {
//                            showBottomSheet = false
//                            showClient0 = false
//                            showRoad0()
//                        })
//                    }
//
//                    if (showClient1 && event >= 3) {
//                        ClientItem(clientEntry = clients[1], onShowRoad = {
//                            showBottomSheet = false
//                            showRoad1()
//                        }, onAccept = {
//                            showBottomSheet = false
//                            showClient1 = false
//                            showRoad1()
//                        })
//                    }
//                    if (showClient2 && event >= 3) {
//                        ClientItem(clientEntry = clients[2], onShowRoad = {
//                            showBottomSheet = false
//                            showRoad2()
//                        }, onAccept = {
//                        }, onDecline = {
//                            showBottomSheet = false
//                            showClient2 = false
//                            declineClient2()
//                        }, dangerSign = true)
//                    }
//
//
//                    Spacer(modifier = Modifier.height(16.dp))
//
//                    // Close Button
//                    Button(
//                        onClick = {
//                            scope.launch { sheetState.hide() }.invokeOnCompletion {
//                                if (!sheetState.isVisible) {
//                                    showBottomSheet = false
//                                }
//                            }
//                        },
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .height(48.dp)
//                            .align(Alignment.CenterHorizontally),
//                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF4285F4))
//                    ) {
//                        Text(
//                            text = "Ascunde", color = Color.White, fontSize = 16.sp
//                        )
//                    }
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun ClientItem(
//    clientEntry: ClientEntry, onShowRoad: () -> Unit, onAccept: () -> Unit, onDecline: () -> Unit = {}, dangerSign: Boolean = false,
//) {
//    Card(
//        shape = RoundedCornerShape(8.dp), modifier = Modifier
//            .fillMaxWidth()
//            .clickable {
//                onShowRoad()
//            }, elevation = CardDefaults.cardElevation(4.dp), colors = CardDefaults.cardColors(
//            containerColor = Color(clientEntry.color),
//        )
//    ) {
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(12.dp),
//            verticalAlignment = Alignment.CenterVertically,
//        ) {
//            // User Icon
//            Image(
//                painter = painterResource(id = R.drawable.user_icon_2), // Replace with your image resource
//                contentDescription = "User Icon",
//                modifier = Modifier
//                    .size(48.dp)
//                    .background(Color.Gray, shape = CircleShape)
//                    .padding(4.dp),
//                contentScale = ContentScale.Crop
//            )
//
//            Spacer(modifier = Modifier.width(12.dp))
//
//            Column(modifier = Modifier.weight(1f)) {
//                Text(text = clientEntry.name, fontSize = 18.sp, color = Color.Black)
//                Row(
//                    verticalAlignment = Alignment.CenterVertically,
//                    horizontalArrangement = Arrangement.spacedBy(3.dp),
//                    modifier = Modifier.padding(top = 4.dp)
//                ) {
//                    Text(text = clientEntry.starRating.toString())
//                    Icon(
//                        Icons.Filled.Star,
//                        contentDescription = "Star",
//                        tint = Color(0xFFFFD700),
//                        modifier = Modifier.size(16.dp)
//                    )
//                    if (dangerSign) {
//                        Text("Posibil sa intarzie", color = Color.Red, fontSize = 12.sp, fontStyle = FontStyle.Italic)
//                    }
//                }
//            }
//
//            Spacer(modifier = Modifier.width(8.dp))
//
//            // Accept/Decline buttons
//            Row(horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
//                Text("+${clientEntry.cost}$", color = Color(0xFF006400) , modifier = Modifier.padding(horizontal = 6.dp))
//                IconButton(onAccept) {
//                    Image(painterResource(R.drawable.accept), contentDescription = null)
//                }
//                IconButton(onDecline) {
//                    Image(painterResource(R.drawable.cancel), contentDescription = null)
//                }
//            }
//        }
//    }
//}



// Sample data class for client details
@Serializable
data class ClientEntry(
    val name: String,
    val cost: String,
    val starRating: Float,
    val color: Long,
)