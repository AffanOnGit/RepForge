package com.repforge

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.repforge.core.ui.theme.RepForgeTheme
import com.repforge.navigation.RepForgeNavHost
import com.repforge.navigation.BottomNavBar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val startupVm: AppStartupViewModel by viewModels()

        // Keep splash screen up until start destination is resolved
        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition { startupVm.startDestination.value == null }

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            RepForgeTheme {
                val startDestination by startupVm.startDestination.collectAsState()

                // Don't compose the UI until the destination is resolved
                if (startDestination != null) {
                    val navController = rememberNavController()

                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        bottomBar = {
                            BottomNavBar(navController = navController)
                        }
                    ) { innerPadding ->
                        RepForgeNavHost(
                            navController = navController,
                            innerPadding = innerPadding,
                            startDestination = startDestination!!
                        )
                    }
                }
            }
        }
    }
}
