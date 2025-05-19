package com.example.cyber_knightsbridge.Screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shadow
import com.airbnb.lottie.compose.*
import com.example.cyber_knightsbridge.R
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onTimeout: () -> Unit = {}
) {
    val alpha = remember { Animatable(0f) }
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.fingerprint_animation))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever,
        isPlaying = true
    )

    LaunchedEffect(Unit) {
        alpha.animateTo(1f, animationSpec = tween(2500))
        delay(3000)
        onTimeout()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF6A11CB), // purple
                        Color(0xFF2575FC)  // blue
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            LottieAnimation(
                composition = composition,
                progress = { progress },
                modifier = Modifier.size(180.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Fingerprint Detector",
                style = TextStyle(
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    shadow = Shadow(
                        color = Color.Cyan.copy(alpha = 0.9f),
                        offset = Offset(4f, 4f),
                        blurRadius = 16f
                    )
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(alpha.value)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Secure. Fast. Reliable.",
                style = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Magenta.copy(alpha = 0.8f),
                    letterSpacing = 1.2.sp,
                    shadow = Shadow(
                        color = Color.Magenta.copy(alpha = 0.7f),
                        offset = Offset(2f, 2f),
                        blurRadius = 8f
                    )
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(alpha.value)
            )
        }
    }
}
