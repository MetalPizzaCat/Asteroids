package com.metalpizzacat.asteroids.objects

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import ktx.math.plus
import ktx.math.times

class Bullet(texture: Texture, position: Vector2, val direction: Vector2, val speed: Float = 300f) :
    GameObject(position) {
    private val sprite: Sprite by lazy { Sprite(texture, 0, 0, 8, 8) }

    override fun draw(spriteBatch: SpriteBatch) {
        sprite.draw(spriteBatch)
    }

    override fun update(delta: Float) {
        position += direction * speed * delta
    }

    override val collisionRect: Rectangle
        get() = Rectangle(position.x, position.y, 8f, 8f)


    override var position: Vector2
        get() = super.position
        set(value) {
            super.position = value
            sprite.setPosition(value.x, value.y)
        }
}
