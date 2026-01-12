package com.isi.sameway.utils

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.isi.sameway.R
import com.isi.sameway.theme.AppColors

/**
 * Data class for route meta information display.
 */
data class RouteMetaInfo(
    val rating: String = "5 / 5",
    val price: String = "12.50",
    val time: String = "12.05"
)

/**
 * Row displaying route meta information (rating, price, time).
 */
@Composable
fun RouteMetaInfoRow(
    info: RouteMetaInfo,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        MetaItem(
            emoji = "⭐",
            value = info.rating,
            color = AppColors.RatingColor
        )
        MetaItem(
            emoji = "\uD83D\uDCB6",
            value = "LEI ${info.price}",
            color = AppColors.PriceColor
        )
        MetaItem(
            emoji = "⏰",
            value = info.time,
            color = AppColors.TimeColor
        )
    }
}

@Composable
private fun MetaItem(
    emoji: String,
    value: String,
    color: Color
) {
    Text(
        text = "$emoji $value",
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = color
    )
}

/**
 * User profile info section with avatar and name.
 */
@Composable
fun UserProfileSection(
    name: String,
    subtitle: String? = null,
    avatarRes: Int = R.drawable.driver,
    metaInfo: RouteMetaInfo? = null,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
        modifier = modifier.fillMaxWidth()
    ) {
        ProfileAvatar(avatarRes)

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                fontWeight = FontWeight.SemiBold,
                fontSize = 24.sp
            )

            subtitle?.let {
                Text(
                    text = it,
                    color = Color.LightGray,
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.Bold
                )
            }

            metaInfo?.let {
                Spacer(modifier = Modifier.height(8.dp))
                RouteMetaInfoRow(info = it)
            }
        }
    }
}

@Composable
private fun ProfileAvatar(avatarRes: Int) {
    Image(
        painter = painterResource(id = avatarRes),
        contentDescription = "Profile",
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(Color.LightGray)
    )
}
