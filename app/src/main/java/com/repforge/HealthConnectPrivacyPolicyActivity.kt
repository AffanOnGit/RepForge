package com.repforge

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.repforge.core.ui.components.ForgeButton
import com.repforge.core.ui.theme.CarbonSlate
import com.repforge.core.ui.theme.ForgeAmber
import com.repforge.core.ui.theme.RepForgeTheme
import com.repforge.core.ui.theme.TextPrimary
import com.repforge.core.ui.theme.TextSecondary

/**
 * Privacy Policy and Health Connect rationale screen required by Android Health Connect.
 */
class HealthConnectPrivacyPolicyActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            RepForgeTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = CarbonSlate
                ) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .padding(24.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = "RepForge Privacy Policy",
                            style = MaterialTheme.typography.headlineMedium,
                            color = ForgeAmber
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Health Connect Integration",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "RepForge requests access to Health Connect to provide a seamless strength training experience:\n\n" +
                                    "• Read Weight & Heart Rate: Automatically recalibrates your metabolic BMR and scientific calorie expenditure calculations.\n\n" +
                                    "• Write Workout Sessions & Calories: Exports completed strength training sessions (exercises, duration, tonnage, and estimated energy expenditure) back to your chosen health platform.\n\n" +
                                    "RepForge is local-first. Your personal health and fitness data stays on your device and is never sold to third parties.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        ForgeButton(
                            text = "Close",
                            onClick = { finish() }
                        )
                    }
                }
            }
        }
    }
}
