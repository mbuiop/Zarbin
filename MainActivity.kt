package com.space.shooter.game

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.space.shooter.game.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var gameView: GameView
    private lateinit var gameManager: GameManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Fullscreen immersive mode
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            or View.SYSTEM_UI_FLAG_FULLSCREEN
            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        )
        
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        initializeGame()
    }
    
    private fun initializeGame() {
        gameManager = GameManager(this)
        gameView = GameView(this, gameManager)
        binding.gameContainer.addView(gameView)
        
        setupControls()
        loadGameData()
    }
    
    private fun setupControls() {
        binding.joystickView.setOnMoveListener { angle, strength ->
            gameManager.updateSpaceshipMovement(angle, strength)
        }
    }
    
    private fun loadGameData() {
        // Load saved game state
        gameManager.loadGameState()
    }
    
    override fun onPause() {
        super.onPause()
        gameView.pause()
        gameManager.saveGameState()
    }
    
    override fun onResume() {
        super.onResume()
        gameView.resume()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        gameManager.saveGameState()
    }
}
