package com.metalpizzacat.asteroids.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Gdx.audio
import com.badlogic.gdx.Input
import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.math.Intersector
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.metalpizzacat.asteroids.objects.Asteroid
import com.metalpizzacat.asteroids.objects.Bullet
import com.metalpizzacat.asteroids.objects.Label
import com.metalpizzacat.asteroids.objects.Player
import com.metalpizzacat.asteroids.pickRandom
import ktx.app.KtxInputAdapter
import ktx.app.KtxScreen
import ktx.app.clearScreen
import ktx.assets.disposeSafely
import ktx.freetype.generateFont
import ktx.graphics.use
import ktx.math.minus
import ktx.math.vec2
import kotlin.math.max
import kotlin.random.Random

class GameScreen : KtxScreen, KtxInputAdapter {
    private val shootingSound: Sound = audio.newSound(Gdx.files.internal("sounds/shoot.wav"))
    private val backgroundMusic: Music = audio.newMusic(Gdx.files.internal("sounds/asteroid_cut.wav")).apply {
        isLooping = true
    }

    private val explosionSounds: List<Sound> = listOf(
        audio.newSound(Gdx.files.internal("sounds/explosion1.wav")),
        audio.newSound(Gdx.files.internal("sounds/explosion2.wav")),
        audio.newSound(Gdx.files.internal("sounds/explosion3.wav"))
    )

    private val font: BitmapFont = FreeTypeFontGenerator(Gdx.files.internal("fonts/Roboto-Regular.ttf")).generateFont()
    private val scoreFont: BitmapFont =
        FreeTypeFontGenerator(Gdx.files.internal("fonts/PixelifySans-VariableFont_wght.ttf")).generateFont(
            FreeTypeFontGenerator.FreeTypeFontParameter().apply {
                size = 24
            })
    private val spriteBatch: SpriteBatch by lazy { SpriteBatch() }
    private val texture: Texture by lazy { Texture("asteroids.png") }
    private val player: Player by lazy {
        Player(
            texture,
            position = screenCenter.cpy(),
            playableAreaSize = Vector2(Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat()),
            onDeath = {},
            onHealthChanged = {}
        )
    }

    private var score: Int = 0
        set(value) {
            field = value
            highScore = max(score, highScore)
        }

    private var highScore: Int = 0

    private var isPlayerDead: Boolean = false

    /**
     * Max speed with which asteroids can spawn
     */
    private val maxSpeed: Float = 60f

    /**
     * Min spin with which asteroids can spawn
     */
    private val minSpeed: Float = 15f

    private val screenCenter: Vector2
        get() = Vector2(Gdx.graphics.width.toFloat() / 2f, Gdx.graphics.height.toFloat() / 2f)

    private val gameplayAreaRect: Rectangle
        get() = Rectangle(0f, 0f, Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat())

    /**
     * All the asteroids present on the game field
     */
    private val asteroids: ArrayList<Asteroid> = ArrayList()

    /**
     * All currently spawned bullets
     */
    private val bullets: ArrayList<Bullet> = ArrayList()

    private val gameOverLabel: Label = Label("You lost!", 78, screenCenter - vec2(screenCenter.x / 2, 0f))


    /**
     * All asteroids that should be deleted by the end of the frame
     */
    private val deadAsteroids: ArrayList<Asteroid> = ArrayList()

    private val newAsteroids: ArrayList<Asteroid> = ArrayList()

    /**
     * All bullets that should be deleted by the end of the frame
     */
    private val deadBullets: ArrayList<Bullet> = ArrayList()

    private fun spawnNewAsteroid() {
        asteroids.add(
            Asteroid(
                texture,
                Vector2(),
                (screenCenter - Vector2()).nor(),
                speed = Random.nextDouble(minSpeed.toDouble(), maxSpeed.toDouble()).toFloat(),
                size = 2,
                variation = Random.nextInt(0, 3)
            )
        )
    }

    init {
        Gdx.input.inputProcessor = this
    }

    private fun update(delta: Float) {
        player.update(delta)

        asteroids.addAll(newAsteroids.toSet())
        newAsteroids.clear()
        for (asteroid in asteroids) {
            asteroid.update(delta)
            if (!Intersector.overlaps(gameplayAreaRect, asteroid.collisionRect)) {
                deadAsteroids.add(asteroid)
            }
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
            if (isPlayerDead) {
                gameOverLabel.draw(it)
            }
            scoreFont.draw(it, "Score: $score", 0f, 30f)
            scoreFont.draw(it, "HighScore: $score", 100f, 30f)
        }
    }

    private fun damagePlayer() {
        player.receiveDamage()
        isPlayerDead = true
    }

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
                    variation = Random.nextInt(0, 3)
                )
            )
        }
        score += (asteroid.size + 1) * 100
    }

    private fun updateCollision() {
        for (asteroid in asteroids) {
            if (Intersector.overlaps(asteroid.collisionRect, player.collisionRect)) {
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

    override fun keyDown(keycode: Int): Boolean {
        if (keycode == Keys.J) {
            spawnNewAsteroid()
        }
        if (keycode == Keys.SPACE) {
            shoot()
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
