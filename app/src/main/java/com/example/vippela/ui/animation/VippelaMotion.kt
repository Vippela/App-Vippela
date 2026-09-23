package com.example.vippela.ui.animation

import android.animation.ValueAnimator
import android.os.Build
import android.provider.Settings
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/** Constantes e helpers compartilhados das animações do Vippela. */
object VippelaMotion {
    /** Pausa do splash antes de entrar no onboarding. */
    const val SplashDelayMillis = 1_350L

    // Transição entre telas do onboarding
    const val OnboardingSlideMillis = 430
    const val OnboardingFadeInMillis = 260
    const val OnboardingFadeOutMillis = 220

    // Entrada de cada tela
    const val EntranceScaleFrom = .965f
    const val EntranceAlphaMillis = 360
    const val EntranceDampingRatio = Spring.DampingRatioMediumBouncy
    const val EntranceStiffness = Spring.StiffnessMediumLow

    // Movimento ambiente (loops infinitos)
    const val BobMillis = 1_550
    const val BobAmplitudeDp = 5f
    const val BreatheMillis = 1_900
    const val BreatheMin = .985f
    const val BreatheMax = 1.025f

    // Abelha da tela READY
    const val BeeTranslationXDp = 8f
    const val BeeTranslationYDp = 6f
    const val BeeRotationDegrees = 4f

    // Navegação por abas do app familiar
    const val FamiliarSlideInMillis = 360
    const val FamiliarSlideOutMillis = 300
    const val FamiliarFadeInMillis = 220
    const val FamiliarFadeOutMillis = 170

    /** Zera uma duração quando o sistema tem animações desativadas. */
    fun duration(enabled: Boolean, millis: Int): Int = if (enabled) millis else 0

    /** Direção horizontal (+1 avanço / -1 volta) do onboarding. */
    fun onboardingDirection(initial: Int, target: Int): Int = if (target > initial) 1 else -1

    /** Direção horizontal (+1 avanço / -1 volta) da navegação familiar. */
    fun familiarDirection(initial: Int, target: Int): Int = if (target >= initial) 1 else -1
}

/** Spec de transição entre as telas do onboarding (slide + fade direcionais). */
fun AnimatedContentTransitionScope<Int>.onboardingTransition(
    slideMillis: Int,
    fadeInMillis: Int,
    fadeOutMillis: Int,
): ContentTransform {
    val direction = VippelaMotion.onboardingDirection(initialState, targetState)
    return (slideInHorizontally(tween(slideMillis)) { it * direction } + fadeIn(tween(fadeInMillis))) togetherWith
        (slideOutHorizontally(tween(slideMillis)) { -it * direction / 3 } + fadeOut(tween(fadeOutMillis)))
}

/** Spec de transição entre abas/telas do app familiar. */
fun AnimatedContentTransitionScope<Int>.familiarTransition(
    slideInMillis: Int,
    slideOutMillis: Int,
    fadeInMillis: Int,
    fadeOutMillis: Int,
): ContentTransform {
    val direction = VippelaMotion.familiarDirection(initialState, targetState)
    return (slideInHorizontally(tween(slideInMillis)) { it * direction / 2 } + fadeIn(tween(fadeInMillis))) togetherWith
        (slideOutHorizontally(tween(slideOutMillis)) { -it * direction / 4 } + fadeOut(tween(fadeOutMillis)))
}

/**
 * `true` enquanto o usuário não desativou animações no sistema
 * (Acessibilidade → Remover animações / escala de animador em 0).
 */
@Composable
fun rememberAnimationsEnabled(): Boolean {
    val context = LocalContext.current
    return remember(context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ValueAnimator.areAnimatorsEnabled()
        } else {
            Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1f,
            ) != 0f
        }
    }
}
