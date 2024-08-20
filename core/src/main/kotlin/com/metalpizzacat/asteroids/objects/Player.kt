package com.metalpizzacat.asteroids.objects

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Gdx.audio
import com.badlogic.gdx.Gdx.files
import com.badlogic.gdx.Input
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.metalpizzacat.asteroids.moveTowards
import ktx.math.minus
import ktx.math.plus
import ktx.math.times
import kotlin.math.cos
import kotlin.math.sin

class Player(
    texture: Texture,
    position: Vector2,
    private val playableAreaSize: Vector2,
    val onDeath: () -> Unit,
    private val speed: Float = 10f
) : GameObject(position) {
    private val hitSound: Sound = audio.newSound(files.internal("sounds/playerdead.wav"))

    /**
     * Current speed at which player is moving
     */
    private var velocity: Float = 0f

    var paused: Boolean = false

    var health: Int = 3
        set(value) {
            field = value
            if (field <= 0) {
                onDeath()
            }
        }

    /**
     * Current speed at which player *should* be moving
     */
    private var targetVelocity: Float = 0f

    private val sprite: Sprite by lazy { Sprite(texture, 16, 16, 32, 32) }

    /**
     * Offset from the center of the sprite to the position
     */
    private val centerOffset: Vector2 = Vector2(16f, 16f)

    /**
     * Rectangle used for intersection testing
     */
    override val collisionRect: Rectangle =
        Rectangle(position.x - centerOffset.x, position.y - centerOffset.y, 32f, 32f)


    override var position: Vector2
        get() = super.position
        set(value) {
            super.position = value
            sprite.setPosition(super.position.x - centerOffset.x, super.position.y - centerOffset.x)
            collisionRect.setPosition(super.position - centerOffset)
        }

    /**
     * Current rotation of the player ship. Doesn't change the collision shape but determines where player will move next
     */
    private var rotation: Float = 0f
        set(value) {
            field = value % 360
            sprite.rotation = field
        }

    val forwardVector: Vector2
        get() = Vector2(
            cos(Math.toRadians(rotation.toDouble()).toFloat()),
            sin(Math.toRadians(rotation.toDouble()).toFloat())
        )


    override fun draw(spriteBatch: SpriteBatch) {
        if (visible) {
            sprite.draw(spriteBatch)
        }
    }

    override fun update(delta: Float) {
        if (paused) {
            return
        }
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            rotation += (speed / 3f)
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            rotation -= (speed / 3f)
        }
        targetVelocity = if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            speed
        } else {
            0f
        }


        velocity = moveTowards(velocity, targetVelocity, delta * speed)

        position += forwardVector * velocity

        if (position.x > playableAreaSize.x) {
            position.x = 0f
        }
        if (position.y > playableAreaSize.y) {
            position.y = 0f
        }
        if (position.x < 0) {
            position.x = playableAreaSize.x
        }
        if (position.y < 0) {
            position.y = playableAreaSize.y
        }
    }

    fun receiveDamage() {
        hitSound.play()
        health -= 1
        if (health < 0) {
            onDeath()
        }
    }

}
