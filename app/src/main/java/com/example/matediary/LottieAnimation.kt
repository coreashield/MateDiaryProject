package com.example.matediary

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieClipSpec
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieAnimatable
import com.airbnb.lottie.compose.rememberLottieComposition

@Composable
// animationSize : 전체크기, speed : 애니메이션 속도조절 가능
fun LottieSwimAnimation(animationSize: Int, speed: Float = 10f) {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.animationplant)
    )
    val lottieAnimatable = rememberLottieAnimatable()

    LaunchedEffect(composition) {
        lottieAnimatable.animate(
            composition = composition,
            clipSpec = LottieClipSpec.Frame(0, 1200),
            iterations = LottieConstants.IterateForever,  // 무한 반복
            initialProgress = 0f,
            speed = 0.1f * speed
        )
    }
    Box {
        LottieAnimation(
            composition = composition,
            progress = { lottieAnimatable.progress },
            modifier = Modifier.size(animationSize.dp)
        )
    }
}