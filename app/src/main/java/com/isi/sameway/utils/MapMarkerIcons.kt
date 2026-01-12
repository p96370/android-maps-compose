package com.isi.sameway.utils

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.scale
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.isi.sameway.R

/**
 * Centralized marker icon management for map markers.
 * Provides consistent icon sizing and caching.
 */
object MapMarkerIcons {
    private const val DEFAULT_ICON_SIZE = 100

    fun createCarIcon(context: Context, size: Int = DEFAULT_ICON_SIZE): BitmapDescriptor {
        return createIconFromDrawable(context, R.drawable.driver, size)
    }

    fun createPersonIcon(context: Context, size: Int = DEFAULT_ICON_SIZE): BitmapDescriptor {
        return createIconFromDrawable(context, R.drawable.person, size)
    }

    fun createDestinationIcon(context: Context, size: Int = DEFAULT_ICON_SIZE): BitmapDescriptor {
        return createIconFromDrawable(context, R.drawable.user_icon_2, size)
    }

    private fun createIconFromDrawable(
        context: Context,
        drawableRes: Int,
        size: Int
    ): BitmapDescriptor {
        val drawable = context.getDrawable(drawableRes)
        val bitmap = drawable?.toBitmap() ?: createBitmap(1, 1)
        val scaledBitmap = bitmap.scale(size, size)
        return BitmapDescriptorFactory.fromBitmap(scaledBitmap)
    }
}

/**
 * Data class holding all marker icons used in map screens.
 */
data class MapIconDescriptors(
    val carIcon: BitmapDescriptor,
    val personIcon: BitmapDescriptor,
    val destinationIcon: BitmapDescriptor
)

/**
 * Composable function to remember marker icons across recompositions.
 */
@Composable
fun rememberMapIconDescriptors(): MapIconDescriptors {
    val context = LocalContext.current
    return remember {
        MapIconDescriptors(
            carIcon = MapMarkerIcons.createCarIcon(context),
            personIcon = MapMarkerIcons.createPersonIcon(context),
            destinationIcon = MapMarkerIcons.createDestinationIcon(context)
        )
    }
}
