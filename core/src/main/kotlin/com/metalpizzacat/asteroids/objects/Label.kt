package com.metalpizzacat.asteroids.objects

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2

class Label(text: String, fontSize: Int = 16, position: Vector2) : GameObject(position) {
    private val font: BitmapFont = FreeTypeFontGenerator(Gdx.files.internal("fonts/Roboto-Regular.ttf")).generateFont(
        FreeTypeFontGenerator.FreeTypeFontParameter().apply {
            size = fontSize
        })

    private val layout : GlyphLayout = GlyphLayout()

    var text : String = text
        set(value){
            field = value
            layout.setText(font, value)
            width = layout.width
            height = layout.height
        }

    var width : Float = 0f
        private set
    var height : Float = 0f
        private set

    override fun draw(spriteBatch: SpriteBatch) {
        font.draw(spriteBatch, text, position.x, position.y)
    }

    override fun update(delta: Float) {

    }

    override val collisionRect: Rectangle
        get() = Rectangle(position.x, position.y, 1f, 1f)
}
