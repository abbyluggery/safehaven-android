package app.neurothrive.safehaven.util.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import timber.log.Timber
import kotlin.math.sqrt

/**
 * Shake Detector
 *
 * CRITICAL PANIC FEATURE:
 * Detects rapid phone shakes (3+ shakes in 2 seconds)
 * Triggers panic delete dialog
 *
 * This enables survivors to quickly wipe all evidence if
 * the abuser is nearby or demands access to the phone.
 */
class ShakeDetector(
    context: Context,
    private val onShakeDetected: () -> Unit
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private var lastShakeTime = 0L
    private var shakeCount = 0

    // Tuning parameters
    private val SHAKE_THRESHOLD = 20f  // Adjust based on testing
    private val SHAKE_COOLDOWN = 2000L  // 2 seconds between shake sequences
    private val REQUIRED_SHAKES = 3  // Number of shakes to trigger

    /**
     * Start listening for shake events
     */
    fun start() {
        sensorManager.registerListener(
            this,
            accelerometer,
            SensorManager.SENSOR_DELAY_UI
        )
        Timber.d("Shake detector started")
    }

    /**
     * Stop listening for shake events
     */
    fun stop() {
        sensorManager.unregisterListener(this)
        Timber.d("Shake detector stopped")
    }

    override fun onSensorChanged(event: SensorEvent) {
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        // Calculate acceleration magnitude (minus gravity)
        val acceleration = sqrt(x * x + y * y + z * z) - SensorManager.GRAVITY_EARTH

        if (acceleration > SHAKE_THRESHOLD) {
            val currentTime = System.currentTimeMillis()

            // Check if this is part of the same shake sequence
            if (currentTime - lastShakeTime < SHAKE_COOLDOWN) {
                shakeCount++

                if (shakeCount >= REQUIRED_SHAKES) {
                    // PANIC TRIGGERED
                    Timber.w("PANIC: Shake threshold reached ($shakeCount shakes)")
                    onShakeDetected()

                    // Reset counter
                    shakeCount = 0
                    lastShakeTime = 0L
                }
            } else {
                // New shake sequence
                shakeCount = 1
            }

            lastShakeTime = currentTime
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not needed for this use case
    }
}
