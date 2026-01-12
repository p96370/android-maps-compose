package com.isi.sameway.utils

import android.util.Log
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.ComposeMapColorScheme
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.rememberCameraPositionState
import org.lighthousegames.logging.logging

private val log = logging()
@Composable
fun GoogleMapsScreen(
    onMapClick: (LatLng) -> Unit,
    content: @Composable () -> Unit,
) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(44.447298, 26.096316), 14f)
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        googleMapOptionsFactory = {
            GoogleMapOptions().mapId("DEMO_MAP_ID")
        },
        cameraPositionState = cameraPositionState,
        properties = MapProperties(mapType = MapType.NORMAL, isMyLocationEnabled = true),
        onPOIClick = {
            Log.d("car pooling", "POI clicked: ${it.name}")
        },
        mapColorScheme = ComposeMapColorScheme.DARK,
        onMapClick = { latLng ->
            onMapClick(latLng)
        },
        contentPadding = PaddingValues(
            start = 20.dp, top = 20.dp, end = 20.dp, bottom = 40.dp
        )
    ) {
        content()
    }


}