package com.otaliastudios.opengl.draw

import android.os.Build
import androidx.annotation.RequiresApi
import com.otaliastudios.opengl.core.Egl
import com.otaliastudios.opengl.core.floatBufferOf
import com.otaliastudios.opengl.core.toBuffer
import com.otaliastudios.opengl.program.EglFlatProgram
import com.otaliastudios.opengl.program.EglTextureProgram
import java.nio.FloatBuffer


/**
 * Includes stuff from grafika's Drawable2d FULL_RECTANGLE.
 */
open class EglRect: EglDrawable() {

    companion object {

        // A full square, extending from -1 to +1 in both dimensions.
        // When the model/view/projection matrix is identity, this will exactly cover the viewport.
        private val FULL_RECTANGLE_COORDS = floatBufferOf(
                -1.0f, -1.0f, // 0 bottom left
                1.0f, -1.0f, // 1 bottom right
                -1.0f, 1.0f, // 2 top left
                1.0f, 1.0f) // 3 top right

        private const val COORDS_PER_VERTEX = 2
    }

    fun setVertexCoords(array: FloatArray) {
        vertexCoords = array.toBuffer()
    }

    private var vertexCoords = FULL_RECTANGLE_COORDS

    override val vertexArray: FloatBuffer
        get() = vertexCoords

    override val vertexCount: Int
        get() = 8 /* floats in FULL_RECTANGLE_COORDS */ / coordsPerVertex

    override val coordsPerVertex = COORDS_PER_VERTEX
}