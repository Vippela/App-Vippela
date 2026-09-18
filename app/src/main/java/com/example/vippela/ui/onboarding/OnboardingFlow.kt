package com.example.vippela.ui.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

@Composable
fun OnboardingFlow(onFinish: () -> Unit = {}) {
    var step by rememberSaveable { mutableIntStateOf(0) }

    when (step) {
        0 -> OnboardingWelcomeScreen(onNext = { step = 1 })
        1 -> OnboardingValueScreen(onNext = { step = 2 })
        2 -> ProblemaScreen(onProximo = { step = 3 })
        3 -> SolucaoScreen(onNext = { step = 4 })
        else -> ComecarScreen(onStart = { step = 0 })
    }
}