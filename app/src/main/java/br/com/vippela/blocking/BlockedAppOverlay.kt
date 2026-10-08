package br.com.vippela.blocking

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.RectF
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import br.com.vippela.R

internal class BlockedAppOverlay(private val context: Context, private val closeApp: () -> Unit) {
    private val windows = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var view: View? = null
    var packageName: String? = null
        private set

    fun show(pkg: String) {
        if (view != null) return
        val root = object : FrameLayout(context) {
            override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
                super.onMeasure(widthMeasureSpec, heightMeasureSpec)
                val w = measuredWidth
                val h = measuredHeight
                fun measure(index: Int, width: Int, height: Int) {
                    getChildAt(index)?.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY))
                }
                measure(0, w, (h * .61f).toInt() - (h * .31f).toInt())
                measure(1, w * 11 / 12 - w / 12, (h * .735f).toInt() - (h * .625f).toInt())
                measure(2, w - 2 * (w * .035f).toInt(), maxOf((h * .05f).toInt(), (48 * resources.displayMetrics.density).toInt()))
            }
            override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
                val w = right - left
                val h = bottom - top
                getChildAt(0)?.layout(0, (h * .31f).toInt(), w, (h * .61f).toInt())
                getChildAt(1)?.layout(w / 12, (h * .625f).toInt(), w * 11 / 12, (h * .735f).toInt())
                val margin = (w * .035f).toInt()
                val edge = h - (h * .067f).toInt()
                getChildAt(2)?.layout(margin, edge - maxOf((h * .05f).toInt(), (48 * resources.displayMetrics.density).toInt()), w - margin, edge)
            }
            override fun dispatchKeyEvent(event: KeyEvent): Boolean {
                if (event.keyCode == KeyEvent.KEYCODE_BACK) {
                    if (event.action == KeyEvent.ACTION_UP) close()
                    return true
                }
                return super.dispatchKeyEvent(event)
            }
        }
        root.setBackgroundColor(Color.rgb(255, 196, 0))
        root.isFocusableInTouchMode = true
        val art = BlockIllustration(context)
        root.addView(art)
        val message = TextView(context).apply {
            text = "Com mais cuidado,\npequena abelha..."
            textSize = 24f
            typeface = context.resources.getFont(R.font.poppins_regular)
            setTextColor(Color.BLACK)
            gravity = Gravity.CENTER
            accessibilityLiveRegion = View.ACCESSIBILITY_LIVE_REGION_POLITE
        }
        root.addView(message)
        val button = Button(context).apply {
            text = "Fechar app"
            textSize = 23f
            isAllCaps = false
            typeface = context.resources.getFont(R.font.poppins_regular)
            setTextColor(Color.BLACK)
            background = GradientDrawable().apply {
                setColor(Color.rgb(249, 127, 25))
                cornerRadius = 10 * resources.displayMetrics.density
            }
            setPadding(0, 0, 0, 0)
            setOnClickListener { close() }
        }
        root.addView(button)
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.OPAQUE,
        ).apply { gravity = Gravity.TOP or Gravity.START; title = "Proteção familiar Vippela" }
        try {
            windows.addView(root, params)
            view = root
            packageName = pkg
            root.requestFocus()
            root.post {
                root.translationY = root.height.toFloat()
                root.animate().translationY(0f).setDuration(320).setInterpolator(DecelerateInterpolator()).start()
                message.announceForAccessibility(message.text)
            }
        } catch (_: RuntimeException) {
            // O retorno à tela inicial já protegeu o aplicativo.
            dismiss()
        }
    }
    private fun close() { closeApp(); dismiss() }
    fun dismiss() {
        view?.let { it.animate().cancel(); runCatching { windows.removeViewImmediate(it) } }
        view = null
        packageName = null
    }
}

internal class BlockIllustration(context: Context) : View(context) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val scale = minOf(width / 400f, height / 250f)
        canvas.save()
        canvas.translate((width - 400 * scale) / 2, (height - 250 * scale) / 2)
        canvas.scale(scale, scale)
        fun petal(cx: Float, cy: Float, rx: Float, ry: Float, angle: Float, color: Int) {
            canvas.save(); canvas.rotate(angle, cx, cy)
            paint.style = Paint.Style.FILL; paint.color = color
            canvas.drawOval(RectF(cx-rx, cy-ry, cx+rx, cy+ry), paint)
            canvas.restore()
        }
        petal(201f, 142f, 51f, 96f, -17f, Color.rgb(219,217,217))
        petal(160f, 176f, 90f, 49f, 32f, Color.rgb(69,71,108))
        petal(247f, 172f, 94f, 55f, -45f, Color.rgb(250,176,110))
        paint.color = Color.BLACK; paint.style = Paint.Style.STROKE; paint.strokeWidth = 3f
        paint.strokeCap = Paint.Cap.ROUND
        canvas.drawPath(Path().apply { moveTo(20f,38f); cubicTo(51f,17f,73f,17f,104f,39f) }, paint)
        canvas.save(); canvas.rotate(-25f,130f,54f)
        canvas.drawOval(RectF(111f,37f,130f,53f),paint)
        canvas.drawOval(RectF(128f,35f,144f,63f),paint)
        paint.style = Paint.Style.FILL
        canvas.drawOval(RectF(124f,55f,141f,71f),paint)
        paint.style = Paint.Style.STROKE
        canvas.drawOval(RectF(108f,52f,132f,68f),paint)
        canvas.drawPath(Path().apply { moveTo(126f,40f); quadTo(112f,27f,111f,49f); moveTo(137f,68f); quadTo(145f,83f,138f,87f); moveTo(140f,63f); lineTo(155f,66f) },paint)
        canvas.restore(); canvas.restore()
    }
}
