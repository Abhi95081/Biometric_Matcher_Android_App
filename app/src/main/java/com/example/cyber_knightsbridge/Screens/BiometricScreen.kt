package com.example.cyber_knightsbridge.Screens

import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.example.cyber_knightsbridge.R
import java.util.concurrent.Executor

@Composable
fun BiometricLoginScreen(onAuthenticated: () -> Unit) {
    val context = LocalContext.current
    val activity = context as FragmentActivity
    val executor: Executor = ContextCompat.getMainExecutor(context)

    var authStatus by remember { mutableStateOf("Waiting for biometric authentication...") }
    val biometricManager = BiometricManager.from(context)
    val canAuthenticate = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)

    val biometricPrompt = remember {
        BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    authStatus = "Authentication Successful ✅"
                    onAuthenticated()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    authStatus = "Authentication Failed ❌"
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    authStatus = "Error: $errString"
                }
            }
        )
    }

    val promptInfo = remember {
        BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric Login")
            .setSubtitle("Use your fingerprint")
            .setNegativeButtonText("Cancel")
            .build()
    }

    LaunchedEffect(Unit) {
        if (canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS) {
            biometricPrompt.authenticate(promptInfo)
        } else {
            Toast.makeText(context, "Biometric not available on this device", Toast.LENGTH_LONG).show()
        }
    }

    // Animated shimmer background gradient
    val infiniteTransition = rememberInfiniteTransition(label = "ShimmerBackground")
    val offsetX by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "OffsetX"
    )

    val shimmerBrush = Brush.linearGradient(
        colors = listOf(Color(0xFF1F1C2C), Color(0xFF928DAB), Color(0xFF1F1C2C)),
        start = androidx.compose.ui.geometry.Offset(offsetX, 0f),
        end = androidx.compose.ui.geometry.Offset(offsetX + 500f, 1000f)
    )

    val statusColor by animateColorAsState(
        targetValue = when {
            "Successful" in authStatus -> Color(0xFF4CAF50)
            "Failed" in authStatus -> Color(0xFFFF5252)
            "Error" in authStatus -> Color(0xFFFF9800)
            else -> colorScheme.onSurfaceVariant
        },
        label = "StatusColor"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(shimmerBrush),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth()
                .shadow(16.dp, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = colorScheme.surface.copy(alpha = 0.92f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.fingerprint_icons),
                    contentDescription = "Fingerprint Icon",
                    modifier = Modifier
                        .size(100.dp)
                        .shadow(10.dp, shape = RoundedCornerShape(50))
                        .background(Color(0xFF1E88E5), shape = RoundedCornerShape(50))
                        .padding(20.dp),
                    tint = Color.White
                )

                Text(
                    text = "Secure Biometric Login",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = colorScheme.primary
                )

                Text(
                    text = authStatus,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    color = statusColor
                )

                Button(
                    onClick = {
                        if (canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS) {
                            biometricPrompt.authenticate(promptInfo)
                        } else {
                            Toast.makeText(context, "Biometric not available", Toast.LENGTH_SHORT).show()
                        }
                    },
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 10.dp)
                ) {
                    Text("Retry Authentication", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
