package com.metalpizzacat.asteroids.objects

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2

/**
 * Base class for all objects that should exist in the game world
 */
abstract class GameObject(open var position: Vector2) {
    abstract fun draw(spriteBatch: SpriteBatch)
    abstract fun update(delta : Float)

    abstract val collisionRect : Rectangle

    var visible : Boolean = true

}
