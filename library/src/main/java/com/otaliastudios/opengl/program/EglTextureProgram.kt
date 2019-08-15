package com.otaliastudios.opengl.program


import android.opengl.GLES11Ext
import android.opengl.GLES20
import com.otaliastudios.opengl.core.Egl
import com.otaliastudios.opengl.draw.EglDrawable
import com.otaliastudios.opengl.extensions.floatBufferOf

/**
 * An [EglProgram] that uses a simple vertex shader and a texture fragment shader.
 */
@Suppress("unused")
open class EglTextureProgram @JvmOverloads constructor(
        private val textureUnit: Int = GLES20.GL_TEXTURE0
) : EglProgram(SIMPLE_VERTEX_SHADER, SIMPLE_FRAGMENT_SHADER) {

    private val textureTarget = GLES11Ext.GL_TEXTURE_EXTERNAL_OES

    @Suppress("JoinDeclarationAndAssignment")
    private val vertexPositionHandle: Int
    private val vertexMvpMatrixHandle: Int
    private val textureCoordsHandle: Int
    private val textureTransformHandle: Int
    init {
        vertexPositionHandle = GLES20.glGetAttribLocation(handle, "aPosition")
        Egl.checkGlProgramLocation(vertexPositionHandle, "aPosition")
        textureCoordsHandle = GLES20.glGetAttribLocation(handle, "aTextureCoord")
        Egl.checkGlProgramLocation(textureCoordsHandle, "aTextureCoord")
        vertexMvpMatrixHandle = GLES20.glGetUniformLocation(handle, "uMVPMatrix")
        Egl.checkGlProgramLocation(vertexMvpMatrixHandle, "uMVPMatrix")
        textureTransformHandle = GLES20.glGetUniformLocation(handle, "uTexMatrix")
        Egl.checkGlProgramLocation(textureTransformHandle, "uTexMatrix")
    }

    @Suppress("MemberVisibilityCanBePrivate")
    val textureId: Int
    init {
        val textures = IntArray(1)
        GLES20.glGenTextures(1, textures, 0)
        Egl.checkGlError("glGenTextures")

        textureId = textures[0]
        GLES20.glActiveTexture(textureUnit)
        GLES20.glBindTexture(textureTarget, textureId)
        Egl.checkGlError("glBindTexture $textureId")

        GLES20.glTexParameterf(textureTarget, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST.toFloat())
        GLES20.glTexParameterf(textureTarget, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR.toFloat())
        GLES20.glTexParameteri(textureTarget, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(textureTarget, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)
        Egl.checkGlError("glTexParameter")
    }

    @Suppress("MemberVisibilityCanBePrivate")
    var textureTransform: FloatArray = Egl.IDENTITY_MATRIX.clone()

    override fun onPreDraw(drawable: EglDrawable, modelViewProjectionMatrix: FloatArray) {
        super.onPreDraw(drawable, modelViewProjectionMatrix)
        GLES20.glActiveTexture(textureUnit)
        GLES20.glBindTexture(textureTarget, textureId)

        // Copy the modelViewProjectionMatrix over.
        GLES20.glUniformMatrix4fv(vertexMvpMatrixHandle, 1, false,
                modelViewProjectionMatrix, 0)
        Egl.checkGlError("glUniformMatrix4fv")

        // Copy the texture transformation matrix over.
        GLES20.glUniformMatrix4fv(textureTransformHandle, 1, false,
                textureTransform, 0)
        Egl.checkGlError("glUniformMatrix4fv")

        // Enable the "aPosition" vertex attribute.
        // Connect vertexBuffer to "aPosition".
        GLES20.glEnableVertexAttribArray(vertexPositionHandle)
        Egl.checkGlError("glEnableVertexAttribArray")
        GLES20.glVertexAttribPointer(vertexPositionHandle, drawable.coordsPerVertex, GLES20.GL_FLOAT,
                false, drawable.vertexStride, drawable.vertexArray)
        Egl.checkGlError("glVertexAttribPointer")

        // Enable the "aTextureCoord" vertex attribute.
        // Connect texBuffer to "aTextureCoord".
        GLES20.glEnableVertexAttribArray(textureCoordsHandle)
        Egl.checkGlError("glEnableVertexAttribArray")
        GLES20.glVertexAttribPointer(textureCoordsHandle, 2, GLES20.GL_FLOAT,
                false, COORDINATES_STRIDE, FULL_COORDINATES)
        Egl.checkGlError("glVertexAttribPointer")
    }

    override fun onPostDraw(drawable: EglDrawable) {
        super.onPostDraw(drawable)
        GLES20.glDisableVertexAttribArray(vertexPositionHandle)
        GLES20.glDisableVertexAttribArray(textureCoordsHandle)
        GLES20.glBindTexture(textureTarget, 0)
    }

    companion object {
        @Suppress("unused")
        internal val TAG = EglTextureProgram::class.java.simpleName

        // Texture coordinates in GL go from 0 to 1 on both axes.
        private val FULL_COORDINATES = floatBufferOf(
                0.0f, 0.0f, // bottom left
                1.0f, 0.0f, // bottom right
                0.0f, 1.0f, // top left
                1.0f, 1.0f  // top right
        )

        private const val COORDINATES_STRIDE = 2 * Egl.SIZE_OF_FLOAT

        private const val SIMPLE_VERTEX_SHADER =
                "uniform mat4 uMVPMatrix;\n" +
                        "uniform mat4 uTexMatrix;\n" +
                        "attribute vec4 aPosition;\n" +
                        "attribute vec4 aTextureCoord;\n" +
                        "varying vec2 vTextureCoord;\n" +
                        "void main() {\n" +
                        "    gl_Position = uMVPMatrix * aPosition;\n" +
                        "    vTextureCoord = (uTexMatrix * aTextureCoord).xy;\n" +
                        "}\n"

        private const val SIMPLE_FRAGMENT_SHADER =
                "#extension GL_OES_EGL_image_external : require\n" +
                        "precision mediump float;\n" +
                        "varying vec2 vTextureCoord;\n" +
                        "uniform samplerExternalOES sTexture;\n" +
                        "void main() {\n" +
                        "    gl_FragColor = texture2D(sTexture, vTextureCoord);\n" +
                        "}\n"
    }

}