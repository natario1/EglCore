package com.otaliastudios.opengl.demo

import android.animation.ValueAnimator
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.RectF
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.otaliastudios.opengl.core.EglCore
import com.otaliastudios.opengl.draw.*
import com.otaliastudios.opengl.program.GlFlatProgram
import com.otaliastudios.opengl.scene.GlScene
import com.otaliastudios.opengl.surface.EglWindowSurface
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {

    private lateinit var surfaceView: SurfaceView
    private var eglCore: EglCore? = null
    private var eglSurface: EglWindowSurface? = null
    private var flatProgram: GlFlatProgram? = null

    private val scene = GlScene()
    private val roundRect = GlRoundRect()
    private val triangle = GlTriangle()
    private val circle = GlCircle()

    private val rectF = RectF()

    private val drawAnimator = ValueAnimator.ofFloat(0F, 1F).also {
        it.repeatCount = ValueAnimator.INFINITE
        it.repeatMode = ValueAnimator.REVERSE
        it.duration = 1200
        it.interpolator = FastOutSlowInInterpolator()
        it.addUpdateListener { draw() }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Using a handler just because the holder callback runs inside a try-catch.
        // We prefer to crash if there's something wrong.
        val handler = Handler()
        surfaceView = findViewById(R.id.surface_view)
        surfaceView.setZOrderOnTop(true)
        surfaceView.holder.setFormat(PixelFormat.RGBA_8888)
        surfaceView.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder?) {
                handler.post { onSurfaceCreated() }
            }

            override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
                GLES20.glViewport(0, 0, width, height)
                scene.setViewportSize(width, height)
            }

            override fun surfaceDestroyed(holder: SurfaceHolder?) {
                handler.post { onSurfaceDestroyed() }
            }
        })
    }

    private fun onSurfaceCreated() {
        eglCore = EglCore()
        eglSurface = EglWindowSurface(eglCore!!, surfaceView.holder.surface!!)
        eglSurface!!.makeCurrent()
        flatProgram = GlFlatProgram()
        drawAnimator.start()
    }

    private fun onSurfaceDestroyed() {
        drawAnimator.cancel()
        flatProgram?.release()
        eglSurface?.release()
        eglCore?.release()
        flatProgram = null
        eglSurface = null
        eglCore = null
    }

    private fun draw() {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

        // Animate the background rect
        rectF.bottom = floatValue(-0.4F, -1F)
        rectF.left = floatValue(-0.4F, -1F)
        rectF.top = floatValue(0.4F, 1F)
        rectF.right = floatValue(0.4F, 1F)
        roundRect.setRect(rectF)
        roundRect.setCornersPx(floatValue(50F, 0F))
        // Animate the color
        flatProgram!!.setColor(Color.rgb(
                intValue(0, 50),
                intValue(150, 250),
                intValue(100, 150)
        ))
        // Draw
        scene.draw(flatProgram!!, roundRect)

        // Draw the triangle.
        flatProgram!!.setColor(Color.RED)
        triangle.rotation += 3
        triangle.radius = floatValue(0.15F, 0.3F)
        scene.draw(flatProgram!!, triangle)

        // Draw the circle.
        flatProgram!!.setColor(Color.rgb(180, 30, 30))
        circle.radius = floatValue(0.15F, 0F)
        scene.draw(flatProgram!!, circle)

        // Publish.
        eglSurface!!.swapBuffers()
    }

    private fun intValue(start: Int, end: Int): Int {
        return floatValue(start.toFloat(), end.toFloat()).roundToInt()
    }

    private fun floatValue(start: Float, end: Float): Float {
        return start + drawAnimator.animatedFraction * (end - start)
    }
}
