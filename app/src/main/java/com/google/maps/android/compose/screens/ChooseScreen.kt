package com.google.maps.android.compose.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.maps.android.compose.R
import com.google.maps.android.compose.theme.backgroundColor

@Composable
fun ChooseScreen(
    navigateToDriver: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().background(color = backgroundColor),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "SameWay",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 26.sp,
            modifier = Modifier.padding(top = 60.dp)
        )
        Text("Driver", modifier = Modifier.padding(20.dp).padding(top = 20.dp), fontSize = 22.sp)
        Image(
            painterResource(R.drawable.driver), contentDescription = null,
            modifier = Modifier.weight(1f).clickable { navigateToDriver() })
    }
}