package com.metalpizzacat.asteroids

import com.metalpizzacat.asteroids.screens.GameScreen
import ktx.app.KtxGame
import ktx.app.KtxScreen
import ktx.async.KtxAsync

class AsteroidGame : KtxGame<KtxScreen>() {
    override fun create() {
        super.create()
        KtxAsync.initiate()

        addScreen(GameScreen())
        setScreen<GameScreen>()
    }
}

