package com.google.maps.android.compose.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.maps.android.compose.R
import com.google.maps.android.compose.clients
import com.google.maps.android.compose.utils.ClientCard

@Composable
fun ProfileScreen() {
    val context = LocalContext.current
    var showLeaderboard by remember { mutableStateOf(false) }
    if (showLeaderboard) {
        LeaderboardPage(showProfile = {
            showLeaderboard = false
        })
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color.LightGray),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Box(modifier = Modifier.padding(top = 60.dp)) {
                Text(
                    "Profile",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 26.sp,
                    modifier = Modifier.align(Alignment.Center),
                )
                Button(onClick = {
                    showLeaderboard = true
                }, modifier = Modifier.align(Alignment.CenterEnd)) {
                    Text("Leaderboard")
                }
            }


            Text("Name: Alexandru-Ioan", fontWeight = FontWeight.SemiBold, fontSize = 20.sp)
            Image(
                painterResource(R.drawable.user_icon_profile_room),
                contentDescription = null,
                modifier = Modifier.size(120.dp)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Text(
                    text = "Rating: 5",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp,
                    color = Color.Blue
                )
                Icon(
                    Icons.Filled.Star,
                    contentDescription = "Star",
                    tint = Color(0xFFFFD700),
                    modifier = Modifier.size(16.dp)
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Text(
                    text = "Eco-friendly level: 2/10",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp,
                    color = Color.Blue
                )
                Icon(
                    Icons.Filled.Star,
                    contentDescription = "Star",
                    tint = Color(0xFFFFD700),
                    modifier = Modifier.size(16.dp)
                )
            }

            Text(
                "Medals",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 30.dp)
            )
            Image(
                painterResource(R.drawable.badges),
                contentDescription = null,
                modifier = Modifier.size(width = 300.dp, height = 100.dp)
            )

            Text(text = "History", fontSize = 20.sp, fontWeight = FontWeight.Bold)


            Card() {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        clients.forEach {
//                            if (it != clients[2])
                                ClientCard(it)
                        }
                    }
                    Button(onClick = {
                        Toast.makeText(context, "Notified clients.", Toast.LENGTH_LONG)
                            .show()
                    }) {
                        Text(
                            "Repeat trip every wednesday at 12:00",
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

        }
    }
}

@Preview
@Composable
private fun ProfileScreenPreview() {
    ProfileScreen()
}