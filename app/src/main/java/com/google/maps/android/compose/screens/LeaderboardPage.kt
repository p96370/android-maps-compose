package com.google.maps.android.compose.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.maps.android.compose.clients
import com.google.maps.android.compose.utils.ClientCard

@Composable
fun LeaderboardPage(showProfile: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.LightGray),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {

        Text(
            "Leaderboard",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 26.sp,
            modifier = Modifier.padding(top = 60.dp),
        )
        Card(modifier = Modifier.fillMaxWidth(0.8f)) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                ClientCard(
                    client = clients[0].copy(
                        name = "1. Tudor (Top Score) - 560 orders", starRating = 5.0f
                    )
                )
                ClientCard(client = clients[0].copy(name = "2. Alex - 200 orders"))
                ClientCard(
                    client = clients[1].copy(
                        name = "3. Andrei - 100 orders", starRating = 4.7f
                    )
                )
                ClientCard(
                    client = clients[1].copy(
                        name = "4. Ioan - 5 orders", starRating = 4.3f
                    )
                )
                ClientCard(
                    client = clients[1].copy(
                        name = "5. Matei - 2 orders", starRating = 4.2f
                    )
                )
            }
        }
        Button(onClick = {
            showProfile()
        }, modifier = Modifier.padding(top = 30.dp)) {
            Text("Profil")
        }

    }
}