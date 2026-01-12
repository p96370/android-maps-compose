package com.isi.sameway.utils

import com.google.android.gms.maps.model.LatLng
import kotlin.math.cos
import kotlin.math.sqrt

/**
 * Utility object for calculating route-related statistics.
 */
object RouteCalculations {

    // Constants for calculations
    private const val EARTH_RADIUS_KM = 6371.0
    private const val PRICE_PER_KM = 1.5 // LEI per km
    private const val BASE_FARE = 5.0 // LEI base fare
    private const val FUEL_CONSUMPTION_PER_KM = 0.08 // Liters per km (average car)
    private const val AVERAGE_SPEED_KMH = 30.0 // Average city speed
    private const val APP_FEE_PERCENTAGE = 0.20 // 20% app cut

    /**
     * Calculates the total distance of a route in kilometers.
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
     * Calculates the price for a ride based on distance.
     * Returns formatted string like "12.50"
     */
    fun calculatePrice(coordinatesCount: Int): String {
        // Rough estimate: each coordinate point ~ 50 meters
        val estimatedKm = coordinatesCount * 0.05
        val price = BASE_FARE + (estimatedKm * PRICE_PER_KM)
        return "%.2f".format(price)
    }

    /**
     * Calculates the price for a ride based on actual coordinates.
     */
    fun calculatePrice(coordinates: List<LatLng>): String {
        val distanceKm = calculateDistanceKm(coordinates)
        val price = BASE_FARE + (distanceKm * PRICE_PER_KM)
        return "%.2f".format(price)
    }

    /**
     * Calculates the base price for a ride based on coordinate count.
     * This is the raw price calculation before any fees.
     */
    private fun calculateBasePrice(coordinatesCount: Int): Double {
        val estimatedKm = coordinatesCount * 0.05
        return BASE_FARE + (estimatedKm * PRICE_PER_KM)
    }

    /**
     * Calculates the base price for a ride based on actual coordinates.
     */
    private fun calculateBasePrice(coordinates: List<LatLng>): Double {
        val distanceKm = calculateDistanceKm(coordinates)
        return BASE_FARE + (distanceKm * PRICE_PER_KM)
    }

    /**
     * Calculates the price the client pays.
     * Client pays: base_price * 1.25 (includes 20% app fee)
     * Formula: If driver should receive X, client pays X * 1.25
     * This way: driver gets X, app gets X * 0.25
     *
     * Example: base = 10 LEI -> client pays 12 LEI, driver receives 10 LEI, app gets 2 LEI
     */
    fun calculateClientPrice(coordinatesCount: Int): String {
        val basePrice = calculateBasePrice(coordinatesCount)
        // Client pays 25% more so that after 20% cut, driver gets the base price
        // If client pays P, driver gets P * 0.8 = basePrice, so P = basePrice / 0.8 = basePrice * 1.25
        val clientPrice = basePrice / (1 - APP_FEE_PERCENTAGE)
        return "%.2f".format(clientPrice)
    }

    /**
     * Calculates what the driver receives for a ride.
     * Driver receives: client_price * 0.8 (client payment minus 20% app fee)
     * This equals the base price.
     */
    fun calculateDriverPrice(coordinatesCount: Int): String {
        val basePrice = calculateBasePrice(coordinatesCount)
        return "%.2f".format(basePrice)
    }

    /**
     * Calculates the price the client pays based on actual coordinates.
     */
    fun calculateClientPrice(coordinates: List<LatLng>): String {
        val basePrice = calculateBasePrice(coordinates)
        val clientPrice = basePrice / (1 - APP_FEE_PERCENTAGE)
        return "%.2f".format(clientPrice)
    }

    /**
     * Calculates what the driver receives based on actual coordinates.
     */
    fun calculateDriverPrice(coordinates: List<LatLng>): String {
        val basePrice = calculateBasePrice(coordinates)
        return "%.2f".format(basePrice)
    }

    /**
     * Calculates estimated travel time in minutes.
     */
    fun calculateTravelTimeMinutes(coordinates: List<LatLng>): Int {
        val distanceKm = calculateDistanceKm(coordinates)
        return ((distanceKm / AVERAGE_SPEED_KMH) * 60).toInt().coerceAtLeast(1)
    }

    /**
     * Calculates estimated travel time in minutes based on coordinate count.
     */
    fun calculateTravelTimeMinutes(coordinatesCount: Int): Int {
        // Rough estimate based on coordinates
        val estimatedKm = coordinatesCount * 0.05
        return ((estimatedKm / AVERAGE_SPEED_KMH) * 60).toInt().coerceAtLeast(1)
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
     * Calculates fuel saved based on coordinates count and people count.
     */
    fun calculateFuelSaved(coordinatesCount: Int, peopleCount: Int): Double {
        if (peopleCount <= 1) return 0.0
        val estimatedKm = coordinatesCount * 0.05
        return estimatedKm * FUEL_CONSUMPTION_PER_KM * (peopleCount - 1)
    }

    /**
     * Formats fuel amount as a readable string.
     */
    fun formatFuel(liters: Double): String {
        return "%.1fL".format(liters)
    }

    /**
     * Calculates money earned by driver for giving rides.
     */
    fun calculateMoneyEarned(coordinates: List<LatLng>, clientsCount: Int): Double {
        if (clientsCount <= 0) return 0.0
        val distanceKm = calculateDistanceKm(coordinates)
        val pricePerClient = BASE_FARE + (distanceKm * PRICE_PER_KM)
        return pricePerClient * clientsCount
    }

    /**
     * Formats money as a readable string with LEI currency.
     */
    fun formatMoney(amount: Double): String {
        return "%.2f LEI".format(amount)
    }

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
}
