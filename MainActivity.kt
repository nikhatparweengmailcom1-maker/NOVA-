package com.nova.assistant

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import dagger.hilt.android.AndroidEntryPoint
import com.nova.assistant.presentation.ui.navigation.NovaNavHost
import com.nova.assistant.presentation.ui.theme.NovaTheme
import com.nova.assistant.service.NovaForegroundService
import com.nova.assistant.util.PermissionManager
import javax.inject.Inject

/**
 * NOVA's single activity — hosts the entire Compose navigation graph.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var permissionManager: PermissionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Start NOVA foreground service
        NovaForegroundService.start(this)

        setContent {
            NovaApp()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Service keeps running even when activity is destroyed
    }

    @Composable
    private fun NovaApp() {
        NovaTheme {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = androidx.compose.ui.graphics.Color.Transparent
            ) {
                NovaNavHost()
            }
        }
    }
}
