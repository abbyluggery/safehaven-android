package app.neurothrive.safehaven.util.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.sqrt

/**
 * ShakeDetector - Detect shake gesture for panic delete
 * CRITICAL: 3 rapid shakes triggers emergency data deletion
 */
class ShakeDetector(
    context: Context,
    private val onShakeDetected: () -> Unit
) : SensorEventListener {
    
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    
    private var lastShakeTime = 0L
    private var shakeCount = 0
    private val SHAKE_THRESHOLD = 20f  // Adjust based on testing
    private val SHAKE_COOLDOWN = 500L  // 500ms between shakes
    private val SHAKE_RESET_TIME = 2000L  // Reset count after 2 seconds
    private var firstShakeTime = 0L
    
    fun start() {
        sensorManager.registerListener(
            this,
            accelerometer,
            SensorManager.SENSOR_DELAY_UI
        )
    }
    
    fun stop() {
        sensorManager.unregisterListener(this)
    }
    
    override fun onSensorChanged(event: SensorEvent) {
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]
        
        val acceleration = sqrt(x*x + y*y + z*z) - SensorManager.GRAVITY_EARTH
        
        if (acceleration > SHAKE_THRESHOLD) {
            val currentTime = System.currentTimeMillis()
            
            // Check if within cooldown period
            if (currentTime - lastShakeTime > SHAKE_COOLDOWN) {
                
                // Reset count if too much time has passed
                if (currentTime - firstShakeTime > SHAKE_RESET_TIME) {
                    shakeCount = 0
                    firstShakeTime = currentTime
                }
                
                shakeCount++
                lastShakeTime = currentTime
                
                // Trigger panic delete after 3 shakes
                if (shakeCount >= 3) {
                    shakeCount = 0
                    onShakeDetected()
                }
            }
        }
    }
    
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not needed
    }
}
