package com.isi.sameway.utils

/**
 * Utility functions for time formatting and calculations.
 */
object TimeUtils {

    /**
     * Formats a time integer (HHMM format) to a readable string (HH:MM).
     * Example: 1205 -> "12:05", 925 -> "09:25"
     */
    fun formatTime(time: Int): String {
        val hours = time / 100
        val minutes = time % 100
        return "%02d:%02d".format(hours, minutes)
    }

    /**
     * Adds minutes to a time integer and returns the new time.
     * Handles hour overflow.
     * Example: addMinutes(1255, 10) -> 1305
     */
    fun addMinutes(time: Int, minutesToAdd: Int): Int {
        val hours = time / 100
        val minutes = time % 100
        val totalMinutes = hours * 60 + minutes + minutesToAdd
        val newHours = (totalMinutes / 60) % 24
        val newMinutes = totalMinutes % 60
        return newHours * 100 + newMinutes
    }

    /**
     * Calculates the estimated arrival time based on starting time and route position.
     * Assumes approximately 1 minute per coordinate point.
     */
    fun calculateArrivalTime(startingTime: Int, positionIndex: Int, totalPositions: Int): Int {
        // Estimate time based on position in route (approximately 1 minute per point)
        val estimatedMinutes = (positionIndex.toFloat() / maxOf(totalPositions, 1) * 30).toInt()
        return addMinutes(startingTime, estimatedMinutes)
    }

    /**
     * Gets the current time as an integer in HHMM format.
     */
    fun getCurrentTimeAsInt(): Int {
        val calendar = java.util.Calendar.getInstance()
        return calendar.get(java.util.Calendar.HOUR_OF_DAY) * 100 +
               calendar.get(java.util.Calendar.MINUTE)
    }
}
