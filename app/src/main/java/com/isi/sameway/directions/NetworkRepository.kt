package com.isi.sameway.directions

import com.google.android.gms.maps.model.LatLng
import com.isi.sameway.BuildConfig
import com.isi.sameway.utils.ClientEntry
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.lighthousegames.logging.logging

val json = Json { ignoreUnknownKeys = true}
object NetworkRepository {
    private val log = logging()
    private val dispatchers = Dispatchers
    private val scope = CoroutineScope(dispatchers.IO)
    private val client = HttpClient(CIO)
    private const val API_KEY = BuildConfig.MAPS_API_KEY

    private const val BASE_URL = "https://maps.googleapis.com/maps/api/directions/json"

    suspend fun requestPath(start: LatLng, end: LatLng): String = withContext(scope.coroutineContext) {

        try {
            // Construct the URL with the API key and request parameters
            val url = "$BASE_URL?origin=${start.latitude},${start.longitude}&destination=${end.latitude},${end.longitude}&key=$API_KEY"
            log.d { "Requesting directions from: ${start.latitude},${start.longitude} to ${end.latitude},${end.longitude}" }
            log.d { "API URL: ${url.replace(API_KEY, "***API_KEY***")}" }

            val response = client.get(urlString = url)

            // Check if the request was successful (status code 200)
            if (response.status == HttpStatusCode.OK) {
                val responseBody = response.bodyAsText()
                log.d { "Response received, length: ${responseBody.length}" }
                log.d { "Response body: ${responseBody.take(500)}" } // Log first 500 chars

                // Deserialize the JSON response into a DirectionsResponse object
                val directionsResponse = json.decodeFromString<DirectionsResponse>(responseBody)

                log.d { "API Status: ${directionsResponse.status}" }
                directionsResponse.error_message?.let {
                    log.e { "API Error message: $it" }
                }
                log.d { "Number of routes in response: ${directionsResponse.routes.size}" }

                // Extract the polyline points from the first route (assuming it's the shortest path)
                val polylinePoints = directionsResponse.routes.firstOrNull()?.overview_polyline?.points

                if (polylinePoints.isNullOrEmpty()) {
                    log.w { "No polyline points found in response! Routes available: ${directionsResponse.routes.size}" }
                    if (directionsResponse.routes.isEmpty()) {
                        log.e { "API returned no routes. This could be due to: invalid coordinates, no route possible, or API key issues" }
                    }
                } else {
                    log.d { "Polyline points extracted, length: ${polylinePoints.length}" }
                }

                // Return the polyline points (or an empty string if none were found)
                return@withContext polylinePoints ?: ""
            } else {
                log.e { "Request failed with status: ${response.status}" }
                throw Exception("Request failed with status: ${response.status}")
            }
        } catch (e: Exception) {
            // Handle any errors that occur during the request
            log.e { "Error during directions request: ${e.message}" }
            e.printStackTrace()
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
    val routes: List<Route>,
    val status: String? = null,
    val error_message: String? = null
)

@kotlinx.serialization.Serializable
private data class Route(
    val overview_polyline: Polyline
)

@kotlinx.serialization.Serializable
private data class Polyline(
    val points: String
)