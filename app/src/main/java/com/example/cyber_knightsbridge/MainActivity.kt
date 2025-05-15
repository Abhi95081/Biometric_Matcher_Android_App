package com.example.cyber_knightsbridge

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.fragment.app.FragmentActivity
import androidx.navigation.compose.rememberNavController
import com.example.cyber_knightsbridge.Screens.BiometricLoginScreen
import com.example.cyber_knightsbridge.Screens.MainAppScreen
import com.example.cyber_knightsbridge.Screens.SplashScreen
import com.example.cyber_knightsbridge.ui.theme.Cyber_KnightsbridgeTheme
import kotlinx.coroutines.delay

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Cyber_KnightsbridgeTheme {
                var showSplash by remember { mutableStateOf(true) }
                var isAuthenticated by remember { mutableStateOf(false) }

                LaunchedEffect(Unit) {
                    delay(2500)
                    showSplash = false
                }

                Crossfade(targetState = showSplash to isAuthenticated) { (isSplash, auth) ->
                    when {
                        isSplash -> SplashScreen()
                        !auth -> BiometricLoginScreen(onAuthenticated = {
                            isAuthenticated = true
                        })
                        else -> MainAppScreen()

                    }
                }
            }
        }
    }
}

