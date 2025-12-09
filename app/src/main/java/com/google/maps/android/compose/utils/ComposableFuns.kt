package com.google.maps.android.compose.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.RoundCap
import com.google.maps.android.PolyUtil
import com.google.maps.android.compose.Polyline
import kotlinx.serialization.Serializable
import org.lighthousegames.logging.logging

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

// Sample data class for client details
@Serializable
data class ClientEntry(
    val name: String,
    val cost: String,
    val starRating: Float,
    val color: Long,
)