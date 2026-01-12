package com.google.maps.android.compose.directions

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.BuildConfig
import com.google.maps.android.compose.utils.ClientEntry
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

val json = Json { ignoreUnknownKeys = true}
object NetworkRepository {
    private val dispatchers = Dispatchers
    private val scope = CoroutineScope(dispatchers.IO)
    private val client = HttpClient(CIO)
    private const val API_KEY = BuildConfig.DIRECTIONS_API_KEY

    private const val BASE_URL = "https://maps.googleapis.com/maps/api/directions/json"

    suspend fun requestPath(start: LatLng, end: LatLng): String = withContext(scope.coroutineContext) {


        try {
            // Construct the URL with the API key and request parameters
            val url = "$BASE_URL?origin=${start.latitude},${start.longitude}&destination=${end.latitude},${end.longitude}&key=$API_KEY"
            val response = client.get(urlString = url)

            // Check if the request was successful (status code 200)
            if (response.status == HttpStatusCode.OK) {
                val responseBody = response.bodyAsText()

                // Deserialize the JSON response into a DirectionsResponse object
                val directionsResponse = json.decodeFromString<DirectionsResponse>(responseBody)

                // Extract the polyline points from the first route (assuming it's the shortest path)
                val polylinePoints = directionsResponse.routes.firstOrNull()?.overview_polyline?.points

                // Return the polyline points (or an empty string if none were found)
                return@withContext polylinePoints ?: ""
            } else {
                throw Exception("Request failed with status: ${response.status}")
            }
        } catch (e: Exception) {
            // Handle any errors that occur during the request
            throw Exception("Error during request: ${e.message}")
        }

    }
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