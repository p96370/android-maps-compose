package com.isi.sameway.utils

import com.google.android.gms.maps.model.LatLng
import kotlin.math.cos
import kotlin.math.sqrt

/**
 * Utility object for calculating route-related statistics.
 * All calculations use actual coordinates and haversine distance for accuracy.
 */
object RouteCalculations {

    // Constants for calculations
    private const val EARTH_RADIUS_KM = 6371.0
    private const val PRICE_PER_KM = 1.5 // LEI per km
    private const val BASE_FARE = 5.0 // LEI base fare
    private const val FUEL_CONSUMPTION_PER_KM = 0.08 // Liters per km (average car)
    private const val AVERAGE_SPEED_KMH = 30.0 // Average city speed
    private const val APP_FEE_PERCENTAGE = 0.20 // 20% app cut

    // region Distance Calculations

    /**
     * Calculates the total distance of a route in kilometers using haversine formula.
     * This is the sum of distances between each consecutive coordinate pair.
     */
    fun calculateDistanceKm(coordinates: List<LatLng>): Double {
        if (coordinates.size < 2) return 0.0

        var totalDistance = 0.0
        for (i in 0 until coordinates.size - 1) {
            totalDistance += haversineDistance(coordinates[i], coordinates[i + 1])
        }
        return totalDistance
    }

    /**
     * Formats distance as a readable string with km unit.
     */
    fun formatDistanceKm(distanceKm: Double): String {
        return "%.1f km".format(distanceKm)
    }

    /**
     * Formats distance from coordinates as a readable string.
     */
    fun formatDistanceKm(coordinates: List<LatLng>): String {
        return formatDistanceKm(calculateDistanceKm(coordinates))
    }

    // endregion

    // region Price Calculations

    /**
     * Calculates the base price for a ride based on distance.
     * This is the raw price calculation before any fees.
     */
    private fun calculateBasePrice(distanceKm: Double): Double {
        return BASE_FARE + (distanceKm * PRICE_PER_KM)
    }

    /**
     * Calculates the price the client pays based on distance in km.
     * Client pays: base_price / 0.8 (includes 20% app fee)
     * Formula: If driver should receive X, client pays X * 1.25
     * This way: driver gets X, app gets X * 0.25
     *
     * Example: base = 10 LEI -> client pays 12.50 LEI, driver receives 10 LEI, app gets 2.50 LEI
     */
    fun calculateClientPrice(distanceKm: Double): String {
        val basePrice = calculateBasePrice(distanceKm)
        val clientPrice = basePrice / (1 - APP_FEE_PERCENTAGE)
        return "%.2f".format(clientPrice)
    }

    /**
     * Calculates the price the client pays based on actual coordinates.
     */
    fun calculateClientPrice(coordinates: List<LatLng>): String {
        val distanceKm = calculateDistanceKm(coordinates)
        return calculateClientPrice(distanceKm)
    }

    /**
     * Calculates what the driver receives for a ride based on distance in km.
     * Driver receives: base_price (client payment minus 20% app fee)
     */
    fun calculateDriverPrice(distanceKm: Double): String {
        val basePrice = calculateBasePrice(distanceKm)
        return "%.2f".format(basePrice)
    }

    /**
     * Calculates total driver earnings by summing the driver price for each client's route distance.
     */
    fun calculateTotalDriverEarnings(clientRouteDistances: List<Double>): Double {
        if (clientRouteDistances.isEmpty()) return 0.0
        return clientRouteDistances.sumOf { distanceKm ->
            calculateBasePrice(distanceKm)
        }
    }

    // endregion

    // region Time Calculations

    /**
     * Calculates estimated travel time in minutes based on coordinates.
     */
    fun calculateTravelTimeMinutes(coordinates: List<LatLng>): Int {
        val distanceKm = calculateDistanceKm(coordinates)
        return ((distanceKm / AVERAGE_SPEED_KMH) * 60).toInt().coerceAtLeast(1)
    }

    /**
     * Formats travel time as a readable string.
     */
    fun formatTravelTime(minutes: Int): String {
        return when {
            minutes < 60 -> "$minutes minute${if (minutes != 1) "s" else ""}"
            else -> {
                val hours = minutes / 60
                val mins = minutes % 60
                if (mins == 0) "$hours hour${if (hours != 1) "s" else ""}"
                else "$hours hour${if (hours != 1) "s" else ""} $mins min"
            }
        }
    }

    // endregion

    // region Fuel & Environmental Calculations

    /**
     * Calculates fuel saved by carpooling.
     * Each additional person saves the full fuel that would have been used if they drove alone.
     */
    fun calculateFuelSaved(coordinates: List<LatLng>, peopleCount: Int): Double {
        if (peopleCount <= 1) return 0.0
        val distanceKm = calculateDistanceKm(coordinates)
        // Each additional person saves the fuel they would have used
        return distanceKm * FUEL_CONSUMPTION_PER_KM * (peopleCount - 1)
    }

    /**
     * Formats fuel amount as a readable string.
     */
    fun formatFuel(liters: Double): String {
        return "%.1fL".format(liters)
    }

    // endregion

    // region Money Formatting

    /**
     * Formats money as a readable string with LEI currency.
     */
    fun formatMoney(amount: Double): String {
        return "%.2f LEI".format(amount)
    }

    // endregion

    // region Route Position Calculations

    /**
     * Finds the index of the closest point on the route to a given target position.
     * Returns the index in the route coordinates list.
     */
    fun findClosestPointIndex(route: List<LatLng>, target: LatLng): Int {
        if (route.isEmpty()) return 0

        var closestIndex = 0
        var minDistance = Double.MAX_VALUE

        route.forEachIndexed { index, point ->
            val distance = haversineDistance(point, target)
            if (distance < minDistance) {
                minDistance = distance
                closestIndex = index
            }
        }

        return closestIndex
    }

    /**
     * Checks if the car has passed a specific marker position on the route.
     * @param route The full route coordinates
     * @param carPositionIndex The current car position index on the route
     * @param markerPosition The marker's position to check
     * @param thresholdPoints Additional points buffer to ensure marker is truly passed (default 2)
     * @return true if the car has passed the marker
     */
    fun hasCarPassedMarker(
        route: List<LatLng>,
        carPositionIndex: Int,
        markerPosition: LatLng,
        thresholdPoints: Int = 2
    ): Boolean {
        if (route.isEmpty()) return false

        val markerIndex = findClosestPointIndex(route, markerPosition)
        // Car has passed if its position is beyond the marker index plus threshold
        return carPositionIndex >= markerIndex + thresholdPoints
    }

    // endregion

    // region Private Helpers

    /**
     * Haversine formula to calculate distance between two LatLng points.
     * Returns distance in kilometers.
     */
    private fun haversineDistance(point1: LatLng, point2: LatLng): Double {
        val lat1Rad = Math.toRadians(point1.latitude)
        val lat2Rad = Math.toRadians(point2.latitude)
        val deltaLatRad = Math.toRadians(point2.latitude - point1.latitude)
        val deltaLngRad = Math.toRadians(point2.longitude - point1.longitude)

        val a = Math.sin(deltaLatRad / 2) * Math.sin(deltaLatRad / 2) +
                cos(lat1Rad) * cos(lat2Rad) *
                Math.sin(deltaLngRad / 2) * Math.sin(deltaLngRad / 2)

        val c = 2 * Math.atan2(sqrt(a), sqrt(1 - a))

        return EARTH_RADIUS_KM * c
    }

    // endregion
}
