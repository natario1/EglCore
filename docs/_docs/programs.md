---
layout: page
title: "Programs"
description: "Egloo programs draw inside the drawable shape"
order: 3
disqus: 1
---

After you know what to draw through drawables, Egloo needs a `GlProgram` implementation to control
**how to draw** them. In Android terms, you can think of this as the `Paint` object that's used to
draw on canvas.

In GLES terms, a `GlProgram` is exactly an OpenGL program. It accepts input shaders in the constructor
and manages the program itself.

A `GlProgram` can be used to draw one or more drawables using the draw method:

```kotlin
program.draw(glDrawable1)
program.draw(glDrawable2, mvpMatrix) // Optional
```

If not present, the model-view-projection matrix will be considered equal to the drawable's model
matrix. As with most other components, after usage, all programs should be released:

```kotlin
program.release()
```

Egloo offers two base program implementations.

### Flat program

The simplest program is one that draws the `GlDrawable` with a flat color. This program is called
`GlFlatProgram`:

```kotlin
val program = GlFlatProgram()
program.setColor(Color.RED)
program.draw(drawable1)
program.setColor(Color.GREEN)
program.draw(drawable2)
```

### Texture program

The `GlTextureProgram` program can be used to render textures. To use it, you will need to create 
a `GlTexture` first and call `program.texture = texture`: this will make sure that texture is 
correctly bound before rendering. See [textures](textures) to learn about this object.
 
The texture program has built-in support for: 
- Adapting the texture to the `GlDrawable` it is being drawn into. This means that the drawable and the texture should have the same aspect ratio to avoid distortion. 
- Apply a matrix transformation to the texture by modifying `GlTextureProgram.textureTransform`

See the sample below:

```kotlin
val texture = GlTexture()
val program = GlTextureProgram()
program.texture = texture
val surfaceTexture = SurfaceTexture(texture.id)

// Pass this surfaceTexture to Camera, for example
val camera: android.hardware.Camera = openCamera()
camera.setPreviewTexture(surfaceTexture)

// Now the program texture receives the camera frames
// And we can render them using the program
val rect = GlRect() // Draw the full frame
surfaceTexture.getTransformMatrix(program.textureTransform)
program.draw(rect)
```

If, for some reason, you do not want to call `program.texture = texture` (which gives the program
the ownership of the texture), you can still call `texture.bind()` and `texture.unbind()` manually:

```kotlin
// Option 1
texture.bind()
program.draw(drawable)
texture.unbind()

// Option 2
texture.use {
    program.draw(drawable)
}

// Option 3
program.texture = texture
program.draw(drawable)
```

These options are equivalent. Note, however, that when passing the texture to `GlTextureProgram`,
the texture will automatically be released when you call `program.release()`.