@file:JvmName("Lwjgl3Launcher")

package com.metalpizzacat.asteroids.lwjgl3

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration
import com.metalpizzacat.asteroids.AsteroidGame

/** Launches the desktop (LWJGL3) application. */
fun main() {
    // This handles macOS support and helps on Windows.
    if (StartupHelper.startNewJvmIfRequired())
      return
    Lwjgl3Application(AsteroidGame(), Lwjgl3ApplicationConfiguration().apply {
        setTitle("Asteroids")
        setWindowedMode(800, 640)
        setResizable(false)
        setWindowIcon(*(arrayOf(128, 64, 32, 16).map { "logo$it.png" }.toTypedArray()))
    })
}
