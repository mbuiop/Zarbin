package com.space.shooter.game

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.*

class JoystickView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    
    interface OnMoveListener {
        fun onMove(angle: Double, strength: Double)
    }
    
    private var outerCircle = Paint()
    private var innerCircle = Paint()
    private var horizontalLine = Paint()
    private var verticalLine = Paint()
    
    private var centerX = 0f
    private var centerY = 0f
    private var innerCircleRadius = 0f
    private var outerCircleRadius = 0f
    
    private var joystickX = 0f
    private var joystickY = 0f
    
    private var listener: OnMoveListener? = null
    private var isMoving = false
    
    init {
        setupPaints()
    }
    
    private fun setupPaints() {
        // Outer circle (background)
        outerCircle.color = Color.argb(100, 50, 50, 50)
        outerCircle.style = Paint.Style.FILL
        
        // Inner circle (joystick)
        innerCircle.color = Color.argb(150, 255, 255, 255)
        innerCircle.style = Paint.Style.FILL
        
        // Cross lines
        horizontalLine.color = Color.argb(100, 255, 255, 255)
        horizontalLine.strokeWidth = 3f
        horizontalLine.style = Paint.Style.STROKE
        
        verticalLine.color = Color.argb(100, 255, 255, 255)
        verticalLine.strokeWidth = 3f
        verticalLine.style = Paint.Style.STROKE
    }
    
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        
        centerX = (w / 2).toFloat()
        centerY = (h / 2).toFloat()
        outerCircleRadius = (min(w, h) / 2 * 0.8).toFloat()
        innerCircleRadius = outerCircleRadius * 0.4f
        
        joystickX = centerX
        joystickY = centerY
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        // Draw outer circle
        canvas.drawCircle(centerX, centerY, outerCircleRadius, outerCircle)
        
        // Draw cross lines
        canvas.drawLine(
            centerX - outerCircleRadius, centerY,
            centerX + outerCircleRadius, centerY,
            horizontalLine
        )
        canvas.drawLine(
            centerX, centerY - outerCircleRadius,
            centerX, centerY + outerCircleRadius,
            verticalLine
        )
        
        // Draw inner circle (joystick)
        canvas.drawCircle(joystickX, joystickY, innerCircleRadius, innerCircle)
    }
    
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                return handleActionDown(event)
            }
            MotionEvent.ACTION_MOVE -> {
                return handleActionMove(event)
            }
            MotionEvent.ACTION_UP -> {
                return handleActionUp()
            }
        }
        return true
    }
    
    private fun handleActionDown(event: MotionEvent): Boolean {
        isMoving = true
        updateJoystickPosition(event.x, event.y)
        return true
    }
    
    private fun handleActionMove(event: MotionEvent): Boolean {
        if (isMoving) {
            updateJoystickPosition(event.x, event.y)
        }
        return true
    }
    
    private fun handleActionUp(): Boolean {
        isMoving = false
        resetJoystick()
        listener?.onMove(0.0, 0.0)
        return true
    }
    
    private fun updateJoystickPosition(touchX: Float, touchY: Float) {
        val distance = sqrt(
            (touchX - centerX).toDouble().pow(2.0) + 
            (touchY - centerY).toDouble().pow(2.0)
        )
        
        if (distance <= outerCircleRadius) {
            joystickX = touchX
            joystickY = touchY
        } else {
            val angle = atan2((touchY - centerY).toDouble(), (touchX - centerX).toDouble())
            joystickX = (centerX + cos(angle) * outerCircleRadius).toFloat()
            joystickY = (centerY + sin(angle) * outerCircleRadius).toFloat()
        }
        
        invalidate()
        
        // Calculate angle and strength
        val angle = atan2(
            (joystickY - centerY).toDouble(),
            (joystickX - centerX).toDouble()
        )
        val strength = min(distance / outerCircleRadius, 1.0)
        
        listener?.onMove(angle, strength)
    }
    
    private fun resetJoystick() {
        joystickX = centerX
        joystickY = centerY
        invalidate()
    }
    
    fun setOnMoveListener(listener: OnMoveListener) {
        this.listener = listener
    }
}
