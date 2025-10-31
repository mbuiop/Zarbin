package com.space.shooter.game

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import kotlin.math.cos
import kotlin.math.sin

class GameView(context: Context, attrs: AttributeSet? = null) : SurfaceView(context, attrs), SurfaceHolder.Callback {
    
    private lateinit var gameThread: GameThread
    private val gameManager: GameManager
    private val paint = Paint()
    private val backgroundPaint = Paint()
    private val starfieldPaint = Paint()
    
    // Background elements
    private val stars = mutableListOf<Star>()
    private val planets = mutableListOf<Planet>()
    private val nebulae = mutableListOf<Nebula>()
    
    init {
        holder.addCallback(this)
        gameManager = (context as MainActivity).gameManager
        initializeBackground()
    }
    
    private fun initializeBackground() {
        // Create stars for starfield
        for (i in 0 until 200) {
            stars.add(Star())
        }
        
        // Create planets
        for (i in 0 until 10) {
            planets.add(Planet())
        }
        
        // Create nebulae
        for (i in 0 until 5) {
            nebulae.add(Nebula())
        }
    }
    
    override fun surfaceCreated(holder: SurfaceHolder) {
        gameThread = GameThread(holder, this)
        gameThread.running = true
        gameThread.start()
    }
    
    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        // Handle surface changes
    }
    
    override fun surfaceDestroyed(holder: SurfaceHolder) {
        var retry = true
        gameThread.running = false
        while (retry) {
            try {
                gameThread.join()
                retry = false
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
    }
    
    fun update() {
        gameManager.update()
        updateBackground()
    }
    
    private fun updateBackground() {
        // Update star positions for parallax effect
        stars.forEach { it.update() }
        planets.forEach { it.update() }
        nebulae.forEach { it.update() }
    }
    
    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        
        if (canvas != null) {
            drawBackground(canvas)
            drawGameElements(canvas)
            drawUI(canvas)
        }
    }
    
    private fun drawBackground(canvas: Canvas) {
        // Draw deep space background
        canvas.drawColor(Color.BLACK)
        
        // Draw stars
        stars.forEach { star ->
            paint.color = star.color
            paint.alpha = star.alpha
            canvas.drawCircle(star.x, star.y, star.radius, paint)
        }
        
        // Draw nebulae
        nebulae.forEach { nebula ->
            paint.color = nebula.color
            paint.alpha = nebula.alpha
            canvas.drawCircle(nebula.x, nebula.y, nebula.radius, paint)
        }
        
        // Draw planets
        planets.forEach { planet ->
            paint.color = planet.color
            canvas.drawCircle(planet.x, planet.y, planet.radius, paint)
        }
    }
    
    private fun drawGameElements(canvas: Canvas) {
        // Draw spaceship
        gameManager.spaceship?.let { spaceship ->
            paint.color = Color.WHITE
            canvas.drawBitmap(spaceship.bitmap, spaceship.x, spaceship.y, paint)
        }
        
        // Draw planets (targets)
        gameManager.planets.forEach { planet ->
            paint.color = planet.color
            canvas.drawCircle(planet.x, planet.y, planet.radius, paint)
            
            // Draw planet value
            paint.color = Color.WHITE
            paint.textSize = 30f
            canvas.drawText(planet.value.toString(), planet.x, planet.y, paint)
        }
        
        // Draw enemies
        gameManager.enemies.forEach { enemy ->
            paint.color = Color.RED
            canvas.drawBitmap(enemy.bitmap, enemy.x, enemy.y, paint)
        }
        
        // Draw explosions
        gameManager.explosions.forEach { explosion ->
            paint.color = Color.YELLOW
            paint.alpha = (explosion.life * 255).toInt()
            canvas.drawCircle(explosion.x, explosion.y, explosion.radius, paint)
        }
    }
    
    private fun drawUI(canvas: Canvas) {
        // Draw score
        paint.color = Color.WHITE
        paint.textSize = 50f
        canvas.drawText("Score: ${gameManager.score}", 50f, 100f, paint)
        
        // Draw level
        canvas.drawText("Level: ${gameManager.currentLevel}", 50f, 170f, paint)
        
        // Draw coins
        canvas.drawText("Coins: ${gameManager.coins}", 50f, 240f, paint)
    }
    
    fun pause() {
        gameThread.running = false
    }
    
    fun resume() {
        if (::gameThread.isInitialized) {
            gameThread.running = true
        }
    }
    
    // Background element classes
    private inner class Star {
        var x = (0..width).random().toFloat()
        var y = (0..height).random().toFloat()
        var radius = (1..3).random().toFloat()
        var speed = (1..5).random().toFloat() / 10
        var color = Color.argb(255, 255, 255, 255)
        var alpha = (100..255).random()
        
        fun update() {
            x -= speed
            if (x < -radius) {
                x = width + radius.toFloat()
                y = (0..height).random().toFloat()
            }
        }
    }
    
    private inner class Planet {
        var x = (0..width).random().toFloat()
        var y = (0..height).random().toFloat()
        var radius = (20..50).random().toFloat()
        var speed = (1..3).random().toFloat() / 20
        var color = Color.argb(255, 
            (50..200).random(),
            (50..200).random(),
            (50..200).random()
        )
        
        fun update() {
            x -= speed
            if (x < -radius) {
                x = width + radius.toFloat()
                y = (0..height).random().toFloat()
            }
        }
    }
    
    private inner class Nebula {
        var x = (0..width).random().toFloat()
        var y = (0..height).random().toFloat()
        var radius = (100..300).random().toFloat()
        var speed = (1..2).random().toFloat() / 50
        var color = Color.argb(
            (30..80).random(),
            (100..200).random(),
            (50..150).random(),
            (150..255).random()
        )
        var alpha = (30..80).random()
        
        fun update() {
            x -= speed
            if (x < -radius) {
                x = width + radius.toFloat()
                y = (0..height).random().toFloat()
            }
        }
    }
}

class GameThread(private val holder: SurfaceHolder, private val gameView: GameView) : Thread() {
    var running = false
    
    override fun run() {
        var canvas: Canvas?
        
        while (running) {
            canvas = null
            
            try {
                canvas = holder.lockCanvas()
                synchronized(holder) {
                    gameView.update()
                    gameView.draw(canvas)
                }
            } finally {
                if (canvas != null) {
                    holder.unlockCanvasAndPost(canvas)
                }
            }
            
            try {
                sleep(16) // ~60 FPS
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
    }
}
