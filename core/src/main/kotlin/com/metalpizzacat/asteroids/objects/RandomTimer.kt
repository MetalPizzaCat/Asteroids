package com.metalpizzacat.asteroids.objects

import com.metalpizzacat.asteroids.getRandomFloatInRange

/**
 * A version of timer that changes target time to one in the range every time it finishes
 */
class RandomTimer(val minTime: Float, val maxTime: Float, oneTime: Boolean, autoStart: Boolean, onTimeout: () -> Unit) :
    Timer(getRandomFloatInRange(minTime, maxTime), oneTime, autoStart, onTimeout) {

    override fun start() {
        paused = false
        remainingTime = getRandomFloatInRange(minTime, maxTime)
        time = remainingTime
    }
}
