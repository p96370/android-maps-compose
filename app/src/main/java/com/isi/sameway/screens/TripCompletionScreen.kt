package com.isi.sameway.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.model.LatLng
import com.isi.sameway.theme.AppColors
import com.isi.sameway.utils.RouteCalculations
import kotlinx.coroutines.delay

/**
 * Data class representing the trip summary with calculated values.
 */
data class TripSummary(
    val moneyEarned: String,
    val totalTimeMinutes: Int,
    val peopleHelped: Int,
    val fuelSaved: String,
    val distanceKm: String,
    val co2Saved: String
) {
    companion object {
        /**
         * Creates a TripSummary from route data with calculated values.
         */
        fun fromRoute(
            coordinates: List<LatLng>,
            clientsCount: Int
        ): TripSummary {
            val distanceKm = RouteCalculations.calculateDistanceKm(coordinates)
            val moneyEarned = RouteCalculations.calculateMoneyEarned(coordinates, clientsCount)
            val travelTime = RouteCalculations.calculateTravelTimeMinutes(coordinates)
            val fuelSaved = RouteCalculations.calculateFuelSaved(coordinates, clientsCount + 1)
            // CO2 saved: ~2.3 kg per liter of fuel
            val co2Saved = fuelSaved * 2.3

            return TripSummary(
                moneyEarned = RouteCalculations.formatMoney(moneyEarned),
                totalTimeMinutes = travelTime,
                peopleHelped = clientsCount,
                fuelSaved = RouteCalculations.formatFuel(fuelSaved),
                distanceKm = "%.1f km".format(distanceKm),
                co2Saved = "%.1f kg".format(co2Saved)
            )
        }

        /**
         * Creates a default TripSummary for client rides.
         */
        fun forClient(coordinates: List<LatLng>): TripSummary {
            val distanceKm = RouteCalculations.calculateDistanceKm(coordinates)
            val travelTime = RouteCalculations.calculateTravelTimeMinutes(coordinates)
            val fuelSaved = RouteCalculations.calculateFuelSaved(coordinates, 2) // Driver + client
            val co2Saved = fuelSaved * 2.3

            return TripSummary(
                moneyEarned = RouteCalculations.calculateClientPrice(coordinates),
                totalTimeMinutes = travelTime,
                peopleHelped = 1,
                fuelSaved = RouteCalculations.formatFuel(fuelSaved),
                distanceKm = "%.1f km".format(distanceKm),
                co2Saved = "%.1f kg".format(co2Saved)
            )
        }
    }
}

/**
 * A beautiful, modern trip completion screen that displays trip statistics.
 * Renamed from VictoryOverlay to TripCompletionScreen.
 */
@Composable
fun TripCompletionScreen(
    visible: Boolean,
    isDriver: Boolean = true,
    tripSummary: TripSummary,
    buttonText: String = "Continue",
    onButtonClick: () -> Unit
) {
    var showContent by remember { mutableStateOf(false) }

    LaunchedEffect(visible) {
        if (visible) {
            delay(200)
            showContent = true
        } else {
            showContent = false
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(300)),
        exit = fadeOut(animationSpec = tween(200)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xE6000000),
                            Color(0xCC1A1A1A)
                        )
                    )
                )
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            AnimatedVisibility(
                visible = showContent,
                enter = scaleIn(animationSpec = tween(400)) + slideInVertically(
                    animationSpec = tween(400),
                    initialOffsetY = { it / 4 }
                )
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Success Icon
                        SuccessIcon()

                        Spacer(modifier = Modifier.height(16.dp))

                        // Title
                        Text(
                            text = if (isDriver) "Trip Complete!" else "You've Arrived!",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1A1A)
                        )

                        Text(
                            text = if (isDriver)
                                "Great job! Here's your trip summary"
                            else
                                "Thanks for riding with us!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 4.dp)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Stats Grid
                        if (isDriver) {
                            DriverStatsGrid(tripSummary)
                        } else {
                            ClientStatsGrid(tripSummary)
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Eco Impact Section
                        EcoImpactSection(tripSummary)

                        Spacer(modifier = Modifier.height(24.dp))

                        // Action Button
                        Button(
                            onClick = onButtonClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AppColors.AcceptButtonColor
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = buttonText,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SuccessIcon() {
    Box(
        modifier = Modifier
            .size(80.dp)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        AppColors.AcceptButtonColor,
                        Color(0xFF00C853)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = Color.White
        )
    }
}

@Composable
private fun DriverStatsGrid(tripSummary: TripSummary) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                modifier = Modifier.weight(1f),
                emoji = "💰",
                label = "Earnings",
                value = tripSummary.moneyEarned,
                backgroundColor = Color(0xFFE8F5E9),
                valueColor = Color(0xFF2E7D32)
            )
            StatCard(
                modifier = Modifier.weight(1f),
                emoji = "⏱️",
                label = "Duration",
                value = RouteCalculations.formatTravelTime(tripSummary.totalTimeMinutes),
                backgroundColor = Color(0xFFE3F2FD),
                valueColor = Color(0xFF1565C0)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                modifier = Modifier.weight(1f),
                emoji = "👥",
                label = "People Helped",
                value = tripSummary.peopleHelped.toString(),
                backgroundColor = Color(0xFFFFF3E0),
                valueColor = Color(0xFFE65100)
            )
            StatCard(
                modifier = Modifier.weight(1f),
                emoji = "📍",
                label = "Distance",
                value = tripSummary.distanceKm,
                backgroundColor = Color(0xFFF3E5F5),
                valueColor = Color(0xFF7B1FA2)
            )
        }
    }
}

@Composable
private fun ClientStatsGrid(tripSummary: TripSummary) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            modifier = Modifier.weight(1f),
            emoji = "💳",
            label = "Trip Cost",
            value = tripSummary.moneyEarned + " LEI",
            backgroundColor = Color(0xFFE3F2FD),
            valueColor = Color(0xFF1565C0)
        )
        StatCard(
            modifier = Modifier.weight(1f),
            emoji = "⏱️",
            label = "Trip Time",
            value = RouteCalculations.formatTravelTime(tripSummary.totalTimeMinutes),
            backgroundColor = Color(0xFFFFF3E0),
            valueColor = Color(0xFFE65100)
        )
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    emoji: String,
    label: String,
    value: String,
    backgroundColor: Color,
    valueColor: Color
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(emoji, fontSize = 24.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = valueColor,
                textAlign = TextAlign.Center
            )
            Text(
                text = label,
                fontSize = 12.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun EcoImpactSection(tripSummary: TripSummary) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFE8F5E9)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("🌱", fontSize = 20.sp)
                Text(
                    text = "Eco Impact",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                EcoStat(
                    icon = "⛽",
                    value = tripSummary.fuelSaved,
                    label = "Fuel Saved"
                )
                EcoStat(
                    icon = "🌍",
                    value = tripSummary.co2Saved,
                    label = "CO₂ Reduced"
                )
            }
        }
    }
}

@Composable
private fun EcoStat(
    icon: String,
    value: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(icon, fontSize = 20.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2E7D32)
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = Color(0xFF388E3C)
        )
    }
}
