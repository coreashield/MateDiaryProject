package com.example.matediary

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

class SplashScreen {

}

@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    // Use a LaunchedEffect to navigate after a delay
    LaunchedEffect(Unit) {
        delay(3000) // 3초 동안 스플래시 스크린을 표시합니다.
        onTimeout()
    }

    // 스플래시 스크린 UI 구성
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(modifier = Modifier
            .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val imageModifier = Modifier
                .size(320.dp)
                .background(Color.White)
                .weight(1F, fill = false)
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "logo",
                contentScale = ContentScale.Crop,
                modifier = imageModifier
            )

            val userSpeed = 200
            LottieSwimAnimation(200, (userSpeed / 50f))

            Text("Mate Diary",
                style = MaterialTheme.typography.h6,
                modifier = Modifier
                    .padding(bottom = 16.dp)
            )
        }
    }
}

@Preview
@Composable
fun SplashScreenPreview() {
    SplashScreen(onTimeout = {})
}