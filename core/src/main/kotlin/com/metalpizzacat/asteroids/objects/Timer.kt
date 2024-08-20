package com.metalpizzacat.asteroids.objects

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2

/**
 * A simple timer that will invoke timeout function when time runs out
 */
open class Timer(var time: Float, var oneTime: Boolean, val autoStart : Boolean, var onTimeout: () -> Unit) : GameObject(Vector2()) {

    var remainingTime: Float = time
        protected set

    var paused: Boolean = !autoStart

    var finished: Boolean = false
        protected set

    override fun draw(spriteBatch: SpriteBatch) {
        // it's a timer, we have nothing to draw
    }

    open fun start(){
        paused = false
        remainingTime = time
        finished = false
    }


    protected open fun timeout() {
        onTimeout()
        if (!oneTime) {
           start()
        } else {
            finished = true
        }
    }

    override fun update(delta: Float) {
        if (paused || finished) {
            return
        }
        remainingTime -= delta
        if (remainingTime < 0f) {
            timeout()
        }
    }

    override val collisionRect: Rectangle
        get() = Rectangle()

}
