package com.metalpizzacat.asteroids.screens

import com.badlogic.gdx.Gdx.*
import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.math.Intersector
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.metalpizzacat.asteroids.objects.*
import com.metalpizzacat.asteroids.pickRandom
import ktx.app.KtxInputAdapter
import ktx.app.KtxScreen
import ktx.app.clearScreen
import ktx.assets.disposeSafely
import ktx.graphics.use
import ktx.math.minus
import ktx.math.times
import ktx.math.vec2
import kotlin.math.max
import kotlin.random.Random

class GameScreen : KtxScreen, KtxInputAdapter {
    private val shootingSound: Sound = audio.newSound(files.internal("sounds/shoot.wav"))
    private val backgroundMusic: Music = audio.newMusic(files.internal("sounds/space.ogg")).apply {
        isLooping = true
        play()
    }

    /**
     * Texture used by every object
     */
    private val texture: Texture by lazy { Texture("asteroids.png") }

    /**
     * Icons used to display player health
     */
    private val healthIcons: List<Sprite> =
        listOf(
            Sprite(texture, 16, 16, 32, 32),
            Sprite(texture, 16, 16, 32, 32),
            Sprite(texture, 16, 16, 32, 32)
        )

    /**
     * Invincibility on respawn timer
     */
    private val invincibilityTimer = Timer(2f, true, autoStart = false) {
        playerIsSpawning = false
        player.visible = true
    }

    private val explosionSounds: List<Sound> = listOf(
        audio.newSound(files.internal("sounds/explosion1.wav")),
        audio.newSound(files.internal("sounds/explosion2.wav")),
        audio.newSound(files.internal("sounds/explosion3.wav"))
    )

    /**
     * Font used to draw score and high score values
     */
    private val scoreFont: BitmapFont =
        FreeTypeFontGenerator(files.internal("fonts/PixelifySans-VariableFont_wght.ttf")).generateFont(
            FreeTypeFontGenerator.FreeTypeFontParameter().apply {
                size = 24
            })

    private val spriteBatch: SpriteBatch by lazy { SpriteBatch() }

    private val player: Player by lazy {
        Player(
            texture,
            position = screenCenter.cpy(),
            playableAreaSize = Vector2(graphics.width.toFloat(), graphics.height.toFloat()),
            onDeath = {
                isPlayerDead = true
                backgroundMusic.stop()
                saveProgress()
            },
        )
    }


    private val scoreLabel: Label = Label("Score 0", position = Vector2(0f, 30f), font = scoreFont)
    private val highScoreLabel: Label =
        Label("HighScore 0", position = Vector2(scoreLabel.width, 30f), font = scoreFont)


    /**
     * Whether player is in invincible spawning state or not
     */
    private var playerIsSpawning: Boolean = false
        set(value) {
            field = value
            player.paused = value
        }

    private var score: Int = 0
        set(value) {
            field = value
            highScore = max(score, highScore)
            scoreLabel.text = "Score: $value"
            scoreLabel.position.y = scoreLabel.height + 5f
        }

    private var highScore: Int = 0
        private set(value) {
            field = value
            highScoreLabel.text = "HighScore: $value"
            highScoreLabel.position = vec2(scoreLabel.width + 10f, highScoreLabel.height + 5f)
        }

    private var isPlayerDead: Boolean = false
        set(value) {
            field = value
            gameOverLabel.visible = value
            gameOverHelperLabel.visible = value
        }

    /**
     * Max speed with which asteroids can spawn
     */
    private val maxSpeed: Float = 60f

    /**
     * Min spin with which asteroids can spawn
     */
    private val minSpeed: Float = 15f

    private val screenCenter: Vector2
        get() = Vector2(graphics.width.toFloat() / 2f, graphics.height.toFloat() / 2f)

    private val gameplayAreaRect: Rectangle
        get() = Rectangle(0f, 0f, graphics.width.toFloat(), graphics.height.toFloat())

    /**
     * All the asteroids present on the game field
     */
    private val asteroids: ArrayList<Asteroid> = ArrayList()

    /**
     * All currently spawned bullets
     */
    private val bullets: ArrayList<Bullet> = ArrayList()

    private val gameOverLabel: Label =
        Label("You lost!", 78, screenCenter - vec2(screenCenter.x / 2, 0f)).apply { visible = false }
    private val gameOverHelperLabel: Label =
        Label(
            "Press 's' to try again",
            48,
            screenCenter - vec2((screenCenter.x - screenCenter.x / 4f), 60f)
        ).apply { visible = false }


    /**
     * All asteroids that should be deleted by the end of the frame
     */
    private val deadAsteroids: ArrayList<Asteroid> = ArrayList()

    /**
     * Newly added asteroids that don't yet participate in main game logic. Used to avoid changing asteroids array when iterating over it
     */
    private val newAsteroids: ArrayList<Asteroid> = ArrayList()

    /**
     * All bullets that should be deleted by the end of the frame
     */
    private val deadBullets: ArrayList<Bullet> = ArrayList()

    private val gameObjects: List<GameObject> = listOf(
        invincibilityTimer,
        /**
         * Invincibility blinking timer
         */
        Timer(0.1f, oneTime = false, autoStart = true) {
            if (playerIsSpawning) {
                player.visible = !player.visible
            }
        },
        /**
         * Timer used to spawn new asteroids
         */
        RandomTimer(0.5f, 3f, oneTime = false, autoStart = true) {
            spawnNewAsteroid()
        },
        scoreLabel,
        highScoreLabel,
        gameOverLabel,
        gameOverHelperLabel
    )

    /**
     * Spawns a randomly sized asteroid in one of the edges of the screen
     */
    private fun spawnNewAsteroid() {
        val pos = Vector2(
            Random.nextDouble(-1.0, 1.0).toFloat(),
            Random.nextDouble(-1.0, 1.0).toFloat()
        ).nor() * Vector2(graphics.width.toFloat(), graphics.height.toFloat())
        newAsteroids.add(
            Asteroid(
                texture,
                pos,
                (screenCenter - pos).nor(),
                speed = Random.nextDouble(minSpeed.toDouble(), maxSpeed.toDouble()).toFloat(),
                size = Random.nextInt(3),
                variation = Random.nextInt(0, 3),
                gameplayArea = gameplayAreaRect
            )
        )
    }

    /**
     * Save progress to the save file
     */
    private fun saveProgress() {
        val fileHandle = files.local("record.bin")
        fileHandle.file().bufferedWriter().use { out ->
            out.write(highScore)
        }
    }

    /**
     * Loads highscore from the savefile or if no savefile is found sets highscore to 0
     */
    private fun loadProgress() {
        highScore = if (files.local("record.bin").exists()) {
            files.local("record.bin").file().bufferedReader().use {
                it.read()
            }
        } else {
            0
        }
    }

    init {
        input.inputProcessor = this
        scoreLabel.position.y = scoreLabel.height + 5f
        highScoreLabel.position = vec2(scoreLabel.width + 10f, highScoreLabel.height + 5f)
        loadProgress()
        for (i in 1..3) {
            healthIcons[i - 1].x = graphics.width - i * 32f
            healthIcons[i - 1].rotation = 90f
        }
    }

    /**
     * Run main logic related to updating player and asteroid positions
     * @param delta Time since last frame
     */
    private fun update(delta: Float) {

        player.update(delta)
        gameObjects.forEach { it.update(delta) }
        asteroids.addAll(newAsteroids.toSet())
        newAsteroids.clear()
        for (asteroid in asteroids) {
            asteroid.update(delta)
        }
        for (bullet in bullets) {
            bullet.update(delta)
            if ((!Intersector.overlaps(gameplayAreaRect, bullet.collisionRect))) {
                deadBullets.add(bullet)
            }
        }
        // clean up asteroids and bullets that have left gameplay area
        asteroids.removeAll(deadAsteroids.toSet())
        bullets.removeAll(deadBullets.toSet())
        deadAsteroids.clear()
        deadBullets.clear()
        updateCollision()
    }

    override fun render(delta: Float) {
        super.render(delta)
        if (!isPlayerDead) {
            update(delta)
        }

        clearScreen(0f, 0f, 0f)
        spriteBatch.use {
            player.draw(spriteBatch)
            asteroids.forEach { ast -> ast.draw(it) }
            bullets.forEach { bul -> bul.draw(it) }
            gameObjects.forEach { g -> g.draw(it) }
            healthIcons.forEachIndexed { i, sprite -> if (i < player.health) sprite.draw(spriteBatch) }
        }
    }

    private fun damagePlayer() {
        player.receiveDamage()
        playerIsSpawning = true
        player.position = screenCenter.cpy()
        invincibilityTimer.start()
    }

    /**
     * Explode an asteroid and play appropriate sounds. If asteroid had babies those babies explode into the world like baby spiders.
     * Also adds value to the score
     */
    private fun destroyAsteroid(asteroid: Asteroid) {
        explosionSounds.pickRandom()?.play()
        deadAsteroids.add(asteroid)
        for (i in 0..<asteroid.size) {
            newAsteroids.add(
                Asteroid(
                    texture,
                    asteroid.position,
                    Vector2(Random.nextDouble(-1.0, 1.0).toFloat(), Random.nextDouble(-1.0, 1.0).toFloat()).nor(),
                    size = asteroid.size - 1,
                    speed = asteroid.speed * 0.8f,
                    rotationSpeed = Random.nextFloat() * minSpeed,
                    variation = Random.nextInt(0, 3),
                    gameplayArea = gameplayAreaRect
                )
            )
        }
        score += (asteroid.size + 1) * 100
    }

    /**
     * Check if objects are overlapping and execute logic if they do
     */
    private fun updateCollision() {
        for (asteroid in asteroids) {
            if (Intersector.overlaps(asteroid.collisionRect, player.collisionRect) && !playerIsSpawning) {
                damagePlayer()
                return
            }
            for (bullet in bullets) {
                if (Intersector.overlaps(asteroid.collisionRect, bullet.collisionRect)) {
                    deadBullets.add(bullet)
                    destroyAsteroid(asteroid)
                    break
                }
            }
        }
    }

    /**
     * Reset the game state to allow player to play again
     */
    private fun restart() {
        asteroids.clear()
        deadBullets.clear()
        bullets.clear()
        isPlayerDead = false
        score = 0
        backgroundMusic.play()
        player.position = screenCenter.cpy()
        player.health = 3
    }

    override fun keyDown(keycode: Int): Boolean {
        if (!isPlayerDead) {

            if (keycode == Keys.J) {
                spawnNewAsteroid()
            }
            if (keycode == Keys.SPACE) {
                shoot()
            }
        } else {
            if (keycode == Keys.S) {
                restart()
            }
        }
        return super.keyDown(keycode)

    }

    private fun shoot() {
        shootingSound.play()
        bullets.add(Bullet(texture, player.position, player.forwardVector))
    }

    override fun dispose() {
        super.dispose()
        texture.disposeSafely()
        spriteBatch.disposeSafely()
    }
}
