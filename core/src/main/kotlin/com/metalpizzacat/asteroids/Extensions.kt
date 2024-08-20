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
