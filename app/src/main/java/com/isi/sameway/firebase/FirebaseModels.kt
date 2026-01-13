package com.isi.sameway.firebase

import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class User(
    val id: String = "",
    val name: String = "",
    val rating: Float = 0.0f
)

@IgnoreExtraProperties
data class Route(
    val userId: String = "",
    val coordinates: List<LatLng> = emptyList(),
    val status: String = "Incoming",
    val startingTime: Int = 1200,
    val clients: List<Client> = emptyList(),
    val carPosition: Int = 0,
    val roadStarted: Boolean = false
)

@IgnoreExtraProperties
data class Client(
    val user: String = "",
    val start: LatLng = LatLng(0.0, 0.0),
    val end: LatLng = LatLng(0.0, 0.0),
    val status: String = "Waiting",
    /** The client's requested pickup time in HHMM format (e.g., 1430 = 14:30) */
    val requestedTime: Int = 0,
    /** The distance of the client's route segment in kilometers for price calculation */
    val routeDistanceKm: Double = 0.0
) {
    constructor() : this("", LatLng(0.0, 0.0), LatLng(0.0, 0.0), "Waiting", 0, 0.0)
}

@IgnoreExtraProperties
data class CustomLatLng(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
) {
    constructor() : this(0.0, 0.0)
}

fun CustomLatLng.toLatLng() = LatLng(latitude, longitude)

data class FirebaseClient(
    val user: String = "",
    val start: CustomLatLng = CustomLatLng(),
    val end: CustomLatLng = CustomLatLng(),
    val status: String = "Waiting",
    val requestedTime: Int = 0,
    val routeDistanceKm: Double = 0.0
) {
    constructor() : this("", CustomLatLng(), CustomLatLng(), "Waiting", 0, 0.0)
}

