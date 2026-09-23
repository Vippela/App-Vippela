package com.example.vippela

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import com.example.vippela.ui.familiar.FamiliarApp
import com.example.vippela.ui.onboarding.VippelaOnboarding
import com.example.vippela.ui.onboarding.OnboardingAudience
import com.example.vippela.ui.responsavel.ResponsibleApp
import com.example.vippela.ui.theme.VippelaTheme

private const val PREFS = "vippela_prefs"
private const val KEY_DARK_MODE = "dark_mode"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.getInsetsController(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        val prefs = getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val savedDarkMode = prefs.getBoolean(KEY_DARK_MODE, false)
        setContent {
            var darkMode by rememberSaveable { mutableStateOf(savedDarkMode) }
            var onboardingFinished by rememberSaveable { mutableStateOf(false) }

            fun persistDarkMode(value: Boolean) {
                darkMode = value
                prefs.edit().putBoolean(KEY_DARK_MODE, value).apply()
            }

            fun finishOnboarding() {
                onboardingFinished = true
            }

            VippelaTheme(darkTheme = darkMode) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val isResponsible = BuildConfig.APP_VARIANT == "responsavel"
                    if (!onboardingFinished) {
                        VippelaOnboarding(
                            audience = if (isResponsible) OnboardingAudience.RESPONSAVEL else OnboardingAudience.FAMILIAR,
                            onFinished = ::finishOnboarding,
                        )
                    } else if (isResponsible) {
                        ResponsibleApp()
                    } else {
                        FamiliarApp(darkMode = darkMode, onDarkModeChange = ::persistDarkMode)
                    }
                }
            }
        }
    }
}
