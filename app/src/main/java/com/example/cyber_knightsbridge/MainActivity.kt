package com.example.cyber_knightsbridge

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.runtime.*
import androidx.fragment.app.FragmentActivity
import com.example.cyber_knightsbridge.Screens.BiometricLoginScreen
import com.example.cyber_knightsbridge.Screens.MainAppScreen
import com.example.cyber_knightsbridge.Screens.SplashScreen
import com.example.cyber_knightsbridge.ui.theme.Cyber_KnightsbridgeTheme

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            Cyber_KnightsbridgeTheme {
                var showSplash by remember { mutableStateOf(true) }
                var isAuthenticated by remember { mutableStateOf(false) }

                Crossfade(targetState = showSplash to isAuthenticated, label = "ScreenTransition") { (isSplash, auth) ->
                    when {
                        isSplash -> SplashScreen(onTimeout = { showSplash = false })
                        !auth -> BiometricLoginScreen(onAuthenticated = { isAuthenticated = true })
                        else -> MainAppScreen()
                    }
                }
            }
        }
    }
}
