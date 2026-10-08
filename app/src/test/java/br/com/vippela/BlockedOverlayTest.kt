package br.com.vippela

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import br.com.vippela.blocking.BlockedAppOverlay
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class BlockedOverlayTest {
    @Test fun blockScreenDisplaysCloseButtonAndDismissesWithoutStackingWindows() {
        var closed = false
        val overlay = BlockedAppOverlay(RuntimeEnvironment.getApplication()) { closed = true }
        try {
            overlay.show("com.example.app")
            val field = overlay.javaClass.getDeclaredField("view").apply { isAccessible = true }
            val root = field.get(overlay) as ViewGroup
            overlay.show("com.example.other")
            assertSame(root, field.get(overlay))
            root.measure(View.MeasureSpec.makeMeasureSpec(400, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(850, View.MeasureSpec.EXACTLY))
            root.layout(0, 0, 400, 850)
            val bitmap = Bitmap.createBitmap(400,850,Bitmap.Config.ARGB_8888)
            root.draw(Canvas(bitmap))
            java.io.File("/tmp/vippela-block-preview.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG,100,it) }
            assertTrue((535..620).any { y -> (60..340).any { x -> bitmap.getPixel(x,y) == android.graphics.Color.BLACK } })
            val button = root.getChildAt(2) as Button
            assertEquals("Fechar app", button.text.toString())
            assertTrue(button.top > 650)
            button.performClick()
            assertTrue(closed)
            assertNull(overlay.packageName)
            assertNull(field.get(overlay))
        } finally { overlay.dismiss() }
    }
}
