package com.google.maps.android.compose.directions

import com.google.maps.android.compose.BuildConfig
import com.google.maps.android.compose.utils.ClientEntry
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json

val json = Json { ignoreUnknownKeys = true}
object NetworkRepository {
    private val dispatchers = Dispatchers
    private val scope = CoroutineScope(dispatchers.IO)
    private val client = HttpClient(CIO)
    private const val API_KEY = BuildConfig.DIRECTIONS_API_KEY

    private const val BASE_URL = "https://maps.googleapis.com/maps/api/directions/json"
}

class JsonDeserializer {
    private val json = Json
    fun deserialize(value: String): List<ClientEntry> {
        return json.decodeFromString(value)
    }
}

@kotlinx.serialization.Serializable
private data class DirectionsResponse(
    val routes: List<Route>
)

@kotlinx.serialization.Serializable
private data class Route(
    val overview_polyline: Polyline
)

@kotlinx.serialization.Serializable
private data class Polyline(
    val points: String
)