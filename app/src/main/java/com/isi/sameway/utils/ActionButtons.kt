package com.isi.sameway.utils

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.isi.sameway.theme.AppColors

/**
 * Reusable action buttons row with accept and decline options.
 */
@Composable
fun ActionButtonsRow(
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    acceptText: String = "Continue",
    declineText: String = "Cancel",
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        ActionButton(
            onClick = onDecline,
            text = declineText,
            icon = Icons.Filled.Close,
            backgroundColor = AppColors.DeclineButtonColor
        )
        ActionButton(
            onClick = onAccept,
            text = acceptText,
            icon = Icons.Filled.Done,
            backgroundColor = AppColors.AcceptButtonColor
        )
    }
}

/**
 * Individual action button with icon and text.
 */
@Composable
fun ActionButton(
    onClick: () -> Unit,
    text: String,
    icon: ImageVector,
    backgroundColor: Color,
    contentColor: Color = Color.White,
    accentColor: Color = AppColors.AccentGreen
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = contentColor
        )
    ) {
        ActionButtonContent(icon = icon, text = text, accentColor = accentColor)
    }
}

/**
 * Text-based action button (smaller footprint).
 */
@Composable
fun TextActionButton(
    onClick: () -> Unit,
    text: String,
    backgroundColor: Color,
    contentColor: Color = Color.White
) {
    TextButton(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = contentColor
        )
    ) {
        Text(text)
    }
}

/**
 * Content layout for action buttons with icon and text.
 */
@Composable
private fun ActionButtonContent(
    icon: ImageVector,
    text: String,
    accentColor: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = accentColor
        )
        Text(text = text)
    }
}

/**
 * Accept and decline button pair for client interactions.
 */
@Composable
fun ClientActionButtons(
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        TextActionButton(
            onClick = onAccept,
            text = "Accept",
            backgroundColor = AppColors.AcceptButtonColor
        )
        TextActionButton(
            onClick = onDecline,
            text = "Decline",
            backgroundColor = AppColors.DeclineButtonColor
        )
    }
}
