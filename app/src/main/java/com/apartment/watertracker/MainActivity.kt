package com.apartment.watertracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.apartment.watertracker.core.navigation.WaterTrackerNavHost
import com.apartment.watertracker.core.ui.theme.WaterTrackerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            WaterTrackerTheme {
                WaterTrackerNavHost()
            }
        }
    }
}
