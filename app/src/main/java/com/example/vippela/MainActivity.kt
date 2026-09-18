package com.example.vippela

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.vippela.ui.onboarding.OnboardingFlow
import com.example.vippela.ui.theme.VippelaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VippelaTheme {
                OnboardingFlow()
            }
        }
    }
}