package com.metalpizzacat.asteroids

import kotlin.math.abs
import kotlin.math.sign
import kotlin.random.Random

fun moveTowards(from: Float, to: Float, delta: Float): Float = if (abs(to - from) < delta) {
    to
} else {
    from + sign(to - from) * delta
}

/**
 * Picks a random element from the list
 */
fun <T> List<T>.pickRandom(): T? = if (this.isEmpty()) {
    null
} else {
    this[Random.nextInt(0, this.size - 1)]
}


/**
 * A simple helper function to get a random float value in a given range
 */
fun getRandomFloatInRange(min: Float, max: Float) = Random.nextDouble(min.toDouble(), max.toDouble()).toFloat()
