package br.com.vippela

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import br.com.vippela.data.DemoState
import br.com.vippela.ui.navigation.VippelaApp
import java.io.File
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w393dp-h873dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class MotionTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    private fun frame(name: String): Bitmap {
        compose.waitForIdle()
        lateinit var bitmap: Bitmap
        compose.runOnUiThread {
            val view = compose.activity.window.decorView
            bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
            view.draw(Canvas(bitmap))
            val dir = File("build/screenshots/motion").apply { mkdirs() }
            File(dir, "$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        }
        return bitmap
    }
    private fun topPixels(bitmap: Bitmap): IntArray = IntArray(bitmap.width * 244).also {
        bitmap.getPixels(it, 0, bitmap.width, 0, 0, bitmap.width, 244)
    }

    @Test fun beeKeepsMovingAndOnboardingSlidesBetweenPages() {
        compose.mainClock.autoAdvance = false
        compose.setContent { VippelaApp(DemoState()) }
        compose.mainClock.advanceTimeBy(600)
        val first = topPixels(frame("bee-0600"))
        compose.mainClock.advanceTimeBy(800)
        val second = topPixels(frame("bee-1400"))
        assertFalse("A abelha deve percorrer o trajeto", first.contentEquals(second))
        compose.mainClock.advanceTimeBy(5000)
        val later = topPixels(frame("bee-6400"))
        compose.mainClock.advanceTimeBy(600)
        assertFalse("A abelha deve continuar após o primeiro voo", later.contentEquals(topPixels(frame("bee-7000"))))
        compose.onNodeWithText("Próximo  ›").performClick()
        compose.mainClock.advanceTimeBy(96)
        val during = compose.onNodeWithText("Nosso\nvalor").fetchSemanticsNode().boundsInRoot.left
        frame("onboarding-during")
        compose.mainClock.advanceTimeBy(500)
        val after = compose.onNodeWithText("Nosso\nvalor").fetchSemanticsNode().boundsInRoot.left
        frame("onboarding-after")
        assertTrue("A página deve deslizar até a posição final", during > after + 1f)
        compose.onNodeWithText("Seja\nbem-vindo!").assertDoesNotExist()
    }

    @Test fun navigationSlidesForwardAndBack() {
        compose.setContent { VippelaApp(DemoState()) }
        repeat(4) { compose.onNodeWithText("Próximo  ›").performClick() }
        compose.onNodeWithText("Começar  ›").performClick()
        compose.onNodeWithText("Entrar na Vippela").assertIsDisplayed()
        compose.mainClock.autoAdvance = false
        compose.onNodeWithText("Continuar com email").performClick()
        compose.mainClock.advanceTimeBy(96)
        val during = compose.onNodeWithText("Bem-vindo de volta!").fetchSemanticsNode().boundsInRoot.left
        frame("navigation-during")
        compose.mainClock.advanceTimeBy(500)
        val after = compose.onNodeWithText("Bem-vindo de volta!").fetchSemanticsNode().boundsInRoot.left
        frame("navigation-after")
        assertTrue("Navegação deve deslizar, sem corte seco", during > after + 1f)
        compose.runOnUiThread { compose.activity.onBackPressedDispatcher.onBackPressed() }
        compose.mainClock.advanceTimeBy(96)
        val backDuring = compose.onNodeWithText("Entrar na Vippela").fetchSemanticsNode().boundsInRoot.left
        frame("back-during")
        compose.mainClock.advanceTimeBy(500)
        val backAfter = compose.onNodeWithText("Entrar na Vippela").fetchSemanticsNode().boundsInRoot.left
        frame("back-after")
        assertTrue("Voltar deve animar no sentido inverso", backDuring < backAfter - 1f)
    }
}
