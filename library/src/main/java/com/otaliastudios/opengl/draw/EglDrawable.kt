package com.otaliastudios.opengl.draw


import android.os.Build
import androidx.annotation.RequiresApi
import com.otaliastudios.opengl.core.Egl
import com.otaliastudios.opengl.core.floatBufferOf
import com.otaliastudios.opengl.core.toBuffer
import com.otaliastudios.opengl.program.EglFlatProgram
import com.otaliastudios.opengl.program.EglTextureProgram
import java.nio.FloatBuffer

abstract class EglDrawable {

    /**
     * The model matrix for this object. Defaults to the
     * identity matrix, but can be accessed and modified.
     */
    val modelMatrix = Egl.IDENTITY_MATRIX.clone()

    /**
     * Returns the array of vertices.
     * To avoid allocations, this returns internal state.  The caller must not modify it.
     */
    abstract val vertexArray: FloatBuffer

    /**
     * Returns the number of vertices stored in the vertex array.
     */
    abstract val vertexCount: Int

    /**
     * Returns the number of position coordinates per vertex.  This will be 2 or 3.
     */
    abstract val coordsPerVertex: Int

    /**
     * Returns the width, in bytes, of the data for each vertex.
     */
    open val vertexStride: Int
        get() = coordsPerVertex * SIZE_OF_FLOAT

    /**
     * Texture drawing: returns the array of texture coordinates.
     * Defaults to [FULL_TEXTURE_COORDS].
     */
    open val texCoordArray: FloatBuffer = FULL_TEXTURE_COORDS

    /**
     * Texture drawing: Returns the width, in bytes, of the data for each texture coordinate.
     * Defaults to 2 * [SIZE_OF_FLOAT].
     */
    open val texCoordStride: Int = 2 * SIZE_OF_FLOAT

    companion object {
        const val SIZE_OF_FLOAT = 4

        // Texture coordinates in GL go from 0 to 1 on both axes
        val FULL_TEXTURE_COORDS = floatBufferOf(
                0.0f, 0.0f, // 0 bottom left
                1.0f, 0.0f, // 1 bottom right
                0.0f, 1.0f, // 2 top left
                1.0f, 1.0f // 3 top right
        )
    }

}