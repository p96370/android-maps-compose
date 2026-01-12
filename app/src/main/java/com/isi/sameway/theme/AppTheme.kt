package com.isi.sameway.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * App-wide color constants used throughout the application.
 */
object AppColors {
    // Primary colors
    val PrimaryBlue = Color(0xFF0B57D0)
    val AccentGreen = Color(0xFF17EABA)

    // Button colors
    val AcceptButtonColor = Color(0xFF2B6777)
    val DeclineButtonColor = Color(0xFF80A9A1)

    // Rating and pricing colors
    val RatingColor = Color(0xFFE1CF6E)
    val PriceColor = Color(0xFF027368)
    val TimeColor = Color(0xFFE91E63)

    // Overlay colors
    val SemiTransparentBlack = Color(0xCC000000)
    val VictoryBackgroundColor = Color(0xFF1FA4A4)
}

/**
 * App-wide dimension constants.
 */
object AppDimensions {
    val VerticalSpacing = 20.dp
    val IconSize = 48.dp
    val DefaultIconSize = 100
    val CornerRadius = 16.dp
    val DefaultPadding = 16.dp
}
