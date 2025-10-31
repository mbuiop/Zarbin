package com.space.shooter.game

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import kotlin.math.*

class GameManager(private val context: Context) {
    
    // Game State
    var score = 0
    var coins = 1_000_000 // Start with 1 million coins
    var currentLevel = 1
    var isGameOver = false
    
    // Game Objects
    var spaceship: Spaceship? = null
    val planets = mutableListOf<Planet>()
    val enemies = mutableListOf<Enemy>()
    val explosions = mutableListOf<Explosion>()
    
    // Game Configuration
    private val totalPlanetsPerLevel = 20
    private val planetsPerWave = 10
    private val totalEnemies = 10
    
    init {
        initializeGame()
    }
    
    private fun initializeGame() {
        createSpaceship()
        generatePlanets()
        generateEnemies()
    }
    
    private fun createSpaceship() {
        spaceship = Spaceship().apply {
            x = 500f
            y = 500f
            speed = 8f
        }
    }
    
    private fun generatePlanets() {
        planets.clear()
        
        // Generate 20 planets for current level
        for (i in 0 until totalPlanetsPerLevel) {
            planets.add(Planet().apply {
                x = (100..1800).random().toFloat()
                y = (100..800).random().toFloat()
                value = when {
                    currentLevel <= 3 -> (1000..5000).random()
                    currentLevel <= 6 -> (5000..20000).random()
                    else -> (20000..100000).random()
                }
                radius = (30..60).random().toFloat()
                color = Color.argb(255,
                    (100..255).random(),
                    (100..255).random(),
                    (100..255).random()
                )
            })
        }
    }
    
    private fun generateEnemies() {
        enemies.clear()
        
        for (i in 0 until totalEnemies) {
            enemies.add(Enemy().apply {
                x = when ((0..3).random()) {
                    0 -> -100f // Left
                    1 -> 2000f // Right
                    2 -> (0..2000).random().toFloat() // Top
                    else -> (0..2000).random().toFloat() // Bottom
                }
                y = when ((0..3).random()) {
                    0 -> (0..1000).random().toFloat()
                    1 -> (0..1000).random().toFloat()
                    2 -> -100f
                    else -> 1100f
                }
                speed = (2f + currentLevel * 0.5f).coerceAtMost(8f)
                damage = currentLevel * 10
            })
        }
    }
    
    fun update() {
        if (isGameOver) return
        
        updateSpaceship()
        updatePlanets()
        updateEnemies()
        updateExplosions()
        checkCollisions()
        checkLevelCompletion()
    }
    
    private fun updateSpaceship() {
        spaceship?.update()
    }
    
    private fun updatePlanets() {
        // Planets are static for now
    }
    
    private fun updateEnemies() {
        enemies.forEach { enemy ->
            enemy.update(spaceship?.x ?: 0f, spaceship?.y ?: 0f)
        }
    }
    
    private fun updateExplosions() {
        explosions.removeAll { explosion ->
            explosion.update()
            explosion.life <= 0
        }
    }
    
    private fun checkCollisions() {
        checkSpaceshipPlanetCollisions()
        checkSpaceshipEnemyCollisions()
    }
    
    private fun checkSpaceshipPlanetCollisions() {
        val spaceship = spaceship ?: return
        
        planets.removeAll { planet ->
            val distance = sqrt(
                (spaceship.x - planet.x).pow(2) + 
                (spaceship.y - planet.y).pow(2)
            )
            
            if (distance < spaceship.radius + planet.radius) {
                // Collision detected
                score += planet.value
                coins += planet.value
                createExplosion(planet.x, planet.y)
                true
            } else {
                false
            }
        }
    }
    
    private fun checkSpaceshipEnemyCollisions() {
        val spaceship = spaceship ?: return
        
        enemies.removeAll { enemy ->
            val distance = sqrt(
                (spaceship.x - enemy.x).pow(2) + 
                (spaceship.y - enemy.y).pow(2)
            )
            
            if (distance < spaceship.radius + enemy.radius) {
                // Game Over - spaceship destroyed
                createExplosion(spaceship.x, spaceship.y)
                isGameOver = true
                true
            } else {
                false
            }
        }
    }
    
    private fun checkLevelCompletion() {
        if (planets.isEmpty()) {
            currentLevel++
            coins += currentLevel * 1000000 // Add 1 million coins per level
            generatePlanets()
            generateEnemies()
        }
    }
    
    private fun createExplosion(x: Float, y: Float) {
        explosions.add(Explosion().apply {
            this.x = x
            this.y = y
            radius = 50f
            life = 1f
        })
    }
    
    fun updateSpaceshipMovement(angle: Double, strength: Double) {
        spaceship?.let {
            it.moving = strength > 0.1
            if (it.moving) {
                it.dx = (cos(angle) * strength * it.speed).toFloat()
                it.dy = (sin(angle) * strength * it.speed).toFloat()
            } else {
                it.dx = 0f
                it.dy = 0f
            }
        }
    }
    
    fun saveGameState() {
        val prefs = context.getSharedPreferences("GameState", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putInt("score", score)
            putInt("coins", coins)
            putInt("level", currentLevel)
            apply()
        }
    }
    
    fun loadGameState() {
        val prefs = context.getSharedPreferences("GameState", Context.MODE_PRIVATE)
        score = prefs.getInt("score", 0)
        coins = prefs.getInt("coins", 1_000_000)
        currentLevel = prefs.getInt("level", 1)
    }
}

// Game Object Classes
open class GameObject {
    var x = 0f
    var y = 0f
    var dx = 0f
    var dy = 0f
    var speed = 0f
    var radius = 50f
    var moving = false
    lateinit var bitmap: Bitmap
}

class Spaceship : GameObject() {
    init {
        speed = 8f
        radius = 40f
    }
    
    fun update() {
        if (moving) {
            x += dx
            y += dy
            
            // Keep within screen bounds
            x = x.coerceIn(radius, 1920f - radius)
            y = y.coerceIn(radius, 1080f - radius)
        }
    }
}

class Planet {
    var x = 0f
    var y = 0f
    var radius = 50f
    var value = 1000
    var color = Color.WHITE
}

class Enemy : GameObject() {
    var damage = 10
    
    init {
        speed = 3f
        radius = 35f
    }
    
    fun update(targetX: Float, targetY: Float) {
        val angle = atan2(targetY - y, targetX - x)
        dx = cos(angle).toFloat() * speed
        dy = sin(angle).toFloat() * speed
        
        x += dx
        y += dy
    }
}

class Explosion {
    var x = 0f
    var y = 0f
    var radius = 0f
    var life = 1f // 1.0 to 0.0
    
    fun update() {
        life -= 0.05f
        radius += 2f
    }
}
