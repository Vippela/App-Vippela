package com.example.vippela

import com.example.vippela.ui.animation.VippelaMotion
import com.example.vippela.ui.onboarding.CONNECT
import com.example.vippela.ui.onboarding.CONNECTED
import com.example.vippela.ui.onboarding.PROBLEM
import com.example.vippela.ui.onboarding.READY
import com.example.vippela.ui.onboarding.SOLUTION
import com.example.vippela.ui.onboarding.SPLASH
import com.example.vippela.ui.onboarding.VALUE
import com.example.vippela.ui.onboarding.WELCOME
import com.example.vippela.ui.onboarding.hasAmbientArtwork
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AnimationSpecTest {
    @Test
    fun onboardingDirection_advancesForwardAndBack() {
        assertEquals(1, VippelaMotion.onboardingDirection(WELCOME, VALUE))
        assertEquals(-1, VippelaMotion.onboardingDirection(VALUE, WELCOME))
        assertEquals(1, VippelaMotion.onboardingDirection(SPLASH, WELCOME))
    }

    @Test
    fun familiarDirection_treatsEqualAsForward() {
        assertEquals(1, VippelaMotion.familiarDirection(0, 0))
        assertEquals(1, VippelaMotion.familiarDirection(1, 2))
        assertEquals(-1, VippelaMotion.familiarDirection(2, 1))
    }

    @Test
    fun ambientArtwork_skippedOnlyOnProblemAndSolution() {
        assertFalse(hasAmbientArtwork(PROBLEM))
        assertFalse(hasAmbientArtwork(SOLUTION))
        assertTrue(hasAmbientArtwork(SPLASH))
        assertTrue(hasAmbientArtwork(WELCOME))
        assertTrue(hasAmbientArtwork(VALUE))
        assertTrue(hasAmbientArtwork(READY))
        assertTrue(hasAmbientArtwork(CONNECT))
        assertTrue(hasAmbientArtwork(CONNECTED))
    }

    @Test
    fun duration_zeroedWhenAnimationsDisabled() {
        assertEquals(430, VippelaMotion.duration(enabled = true, millis = 430))
        assertEquals(0, VippelaMotion.duration(enabled = false, millis = 430))
    }
}
