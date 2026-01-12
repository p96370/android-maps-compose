package com.google.maps.android.compose.firebase

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
    val clients: List<Client> = emptyList()
)

@IgnoreExtraProperties
data class Client(
    val user: String = "",
    val start: LatLng = LatLng(0.0, 0.0),
    val end: LatLng = LatLng(0.0, 0.0),
    val status: String = "Waiting"
) {
    constructor() : this("", LatLng(0.0, 0.0), LatLng(0.0, 0.0), "Waiting")
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
    val status: String = "Waiting"
) {
    constructor() : this("", CustomLatLng(), CustomLatLng(), "Waiting")
}

