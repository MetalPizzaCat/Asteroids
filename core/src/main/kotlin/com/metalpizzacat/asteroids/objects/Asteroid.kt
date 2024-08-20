package com.metalpizzacat.asteroids.objects

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import ktx.math.plus
import ktx.math.times

class Asteroid(
    texture: Texture,
    position: Vector2,
    var direction: Vector2,
    var gameplayArea: Rectangle,
    /**
     * How many hits are required to destroy this asteroid
     */
    var health: Int = 1,
    val speed: Float = 15f,
    val rotationSpeed: Float = 1f,
    /**
     * Current size type of the asteroid. Minimum is 0 which means it can't be split into more asteroids
     */
    val size: Int = 0,
    variation: Int = 0
) : GameObject(position) {

    private val sprite: Sprite by lazy {
        when (size) {
            1 -> {
                Sprite(texture, variation * 32 + 128, 0, 32, 32)
            }

            2 -> {
                Sprite(texture, variation * 64 + 256, 0, 64, 64)
            }

            else -> {
                Sprite(texture, variation * 16 + 64, 0, 16, 16)
            }
        }
    }

    /**
     * Rectangle used for intersection testing
     */
    override val collisionRect: Rectangle = Rectangle(position.x, position.y, (size + 1) * 16f, (size + 1) * 16f)

    /**
     * Current position of the asteroid in the screen space
     */
    override var position: Vector2
        get() = super.position
        set(value) {
            super.position = value
            sprite.setPosition(value.x, value.y)
            collisionRect.setPosition(super.position)
        }

    override fun draw(spriteBatch: SpriteBatch) {
        sprite.draw(spriteBatch)
    }

    override fun update(delta: Float) {
        position = position + direction * speed * delta
        sprite.rotation += rotationSpeed
        if (position.x > gameplayArea.x + gameplayArea.width) {
            position.x = 0f
        }
        if (position.y > gameplayArea.y + gameplayArea.height) {
            position.y = 0f
        }
        if (position.x < gameplayArea.x) {
            position.x = gameplayArea.x + gameplayArea.width
        }
        if (position.y < gameplayArea.y) {
            position.y = gameplayArea.y + gameplayArea.height
        }
    }
}
