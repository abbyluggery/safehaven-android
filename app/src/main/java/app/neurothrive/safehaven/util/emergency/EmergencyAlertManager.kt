package app.neurothrive.safehaven.util.emergency

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.telephony.SmsManager
import androidx.core.content.ContextCompat
import app.neurothrive.safehaven.data.database.dao.EmergencyContactDao
import app.neurothrive.safehaven.data.database.dao.SOSSessionDao
import app.neurothrive.safehaven.data.database.entities.EmergencyContact
import app.neurothrive.safehaven.data.database.entities.SOSActivationMethod
import app.neurothrive.safehaven.data.database.entities.SOSDeactivationMethod
import app.neurothrive.safehaven.data.database.entities.SOSSession
import app.neurothrive.safehaven.data.database.entities.SOSLocationUpdate
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Emergency Alert Manager
 * Manages SOS panic button activations and emergency alerts
 *
 * Features:
 * - SOS panic button activation/deactivation
 * - SMS alerts to emergency contacts
 * - GPS location sharing
 * - Location updates every 5 minutes
 * - Escalation to secondary contacts
 * - "All clear" messaging
 *
 * Offline Capabilities:
 * - SMS works via cellular network (no internet needed)
 * - Last known GPS location used if no signal
 * - All evidence queued for sync when online
 */
@Singleton
class EmergencyAlertManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val emergencyContactDao: EmergencyContactDao,
    private val sosSessionDao: SOSSessionDao
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val _activeSession = MutableStateFlow<SOSSession?>(null)
    val activeSession: Flow<SOSSession?> = _activeSession.asStateFlow()

    private val _isSOSActive = MutableStateFlow(false)
    val isSOSActive: Flow<Boolean> = _isSOSActive.asStateFlow()

    /**
     * Activate SOS panic button
     * Sends immediate emergency alerts to all primary contacts
     *
     * @param userId User activating SOS
     * @param activationMethod How SOS was activated
     * @param includeLocation Whether to include GPS location
     * @return SOS session ID, or null if activation failed
     */
    suspend fun activateSOS(
        userId: String,
        activationMethod: SOSActivationMethod = SOSActivationMethod.LONG_PRESS,
        includeLocation: Boolean = true
    ): Long? {
        try {
            // Check if SOS is already active
            val existing = sosSessionDao.getActiveSession(userId)
            if (existing != null) {
                return existing.id // Already active
            }

            // Get current location (if permission granted and requested)
            val location = if (includeLocation && hasLocationPermission()) {
                getCurrentLocation()
            } else {
                null
            }

            // Create new SOS session
            val session = SOSSession(
                userId = userId,
                isActive = true,
                activationMethod = activationMethod.name,
                startLatitude = location?.latitude,
                startLongitude = location?.longitude,
                startAddress = location?.let { getAddress(it) }
            )

            val sessionId = sosSessionDao.insertSession(session)
            val createdSession = sosSessionDao.getSessionById(sessionId)
            _activeSession.value = createdSession
            _isSOSActive.value = true

            // Send emergency alerts to primary contacts
            sendEmergencyAlerts(userId, sessionId, location)

            // Save initial location update
            if (location != null) {
                val locationUpdate = SOSLocationUpdate(
                    sessionId = sessionId,
                    userId = userId,
                    latitude = location.latitude,
                    longitude = location.longitude,
                    address = getAddress(location),
                    accuracy = location.accuracy,
                    sentToContacts = true,
                    sentAt = System.currentTimeMillis()
                )
                sosSessionDao.insertLocationUpdate(locationUpdate)
            }

            return sessionId

        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    /**
     * Deactivate SOS panic button
     * Sends "all clear" message to emergency contacts
     *
     * @param userId User deactivating SOS
     * @param deactivationMethod How SOS was deactivated
     * @param sendAllClear Whether to send "all clear" message
     */
    suspend fun deactivateSOS(
        userId: String,
        deactivationMethod: SOSDeactivationMethod = SOSDeactivationMethod.SAFE_CODE,
        sendAllClear: Boolean = true
    ) {
        try {
            val session = sosSessionDao.getActiveSession(userId) ?: return

            val endTime = System.currentTimeMillis()
            val durationSeconds = (endTime - session.startTime) / 1000

            // End the session
            sosSessionDao.endSession(
                sessionId = session.id,
                endTime = endTime,
                durationSeconds = durationSeconds,
                deactivationMethod = deactivationMethod.name,
                deactivatedBy = "user",
                allClearSent = sendAllClear
            )

            // Send "all clear" message
            if (sendAllClear) {
                sendAllClearMessage(userId)
            }

            _activeSession.value = null
            _isSOSActive.value = false

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Send emergency alerts to primary contacts
     */
    private suspend fun sendEmergencyAlerts(
        userId: String,
        sessionId: Long,
        location: Location?
    ) {
        try {
            val contacts = emergencyContactDao.getPrimaryContacts(userId)
            if (contacts.isEmpty()) {
                return
            }

            var smsSent = 0

            for (contact in contacts) {
                val message = buildEmergencyMessage(contact, location)
                val success = sendSMS(contact.phoneNumber, message)
                if (success) {
                    smsSent++
                }
            }

            // Update session with contacts alerted
            sosSessionDao.updateContactsAlerted(
                sessionId = sessionId,
                primaryCount = contacts.size,
                secondaryCount = 0,
                escalationTriggered = false
            )

            sosSessionDao.incrementSMSCount(sessionId, smsSent)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Build emergency SMS message
     */
    private fun buildEmergencyMessage(contact: EmergencyContact, location: Location?): String {
        val customMessage = contact.customMessage

        return if (customMessage != null) {
            customMessage
        } else {
            buildString {
                append("🚨 EMERGENCY - ${contact.name} needs immediate help.\n\n")

                if (location != null) {
                    append("Location: ${getAddress(location)}\n")
                    append("GPS: ${location.latitude}, ${location.longitude}\n")
                    append("Maps: https://maps.google.com/?q=${location.latitude},${location.longitude}\n\n")
                }

                append("Time: ${getCurrentTimeFormatted()}\n\n")
                append("This is an automated alert from SafeHaven.")
            }
        }
    }

    /**
     * Send "all clear" message to contacts
     */
    private suspend fun sendAllClearMessage(userId: String) {
        try {
            val contacts = emergencyContactDao.getPrimaryContacts(userId)

            val message = "✅ ALL CLEAR - I am safe now. Emergency alert canceled.\n\nTime: ${getCurrentTimeFormatted()}\n\nThis is an automated message from SafeHaven."

            for (contact in contacts) {
                sendSMS(contact.phoneNumber, message)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Send false alarm message
     */
    suspend fun sendFalseAlarmMessage(userId: String) {
        try {
            val contacts = emergencyContactDao.getPrimaryContacts(userId)

            val message = "⚠️ FALSE ALARM - I accidentally triggered the emergency alert. I'm okay.\n\nTime: ${getCurrentTimeFormatted()}\n\nThis is an automated message from SafeHaven."

            for (contact in contacts) {
                sendSMS(contact.phoneNumber, message)
            }

            // Mark session as false alarm
            val session = sosSessionDao.getActiveSession(userId)
            session?.let {
                sosSessionDao.markAsFalseAlarm(it.id)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Send location update during active SOS
     */
    suspend fun sendLocationUpdate(userId: String) {
        try {
            val session = sosSessionDao.getActiveSession(userId) ?: return

            if (!hasLocationPermission()) {
                return
            }

            val location = getCurrentLocation() ?: return

            // Update session location
            sosSessionDao.updateLocation(
                sessionId = session.id,
                latitude = location.latitude,
                longitude = location.longitude,
                address = getAddress(location)
            )

            // Save location update
            val locationUpdate = SOSLocationUpdate(
                sessionId = session.id,
                userId = userId,
                latitude = location.latitude,
                longitude = location.longitude,
                address = getAddress(location),
                accuracy = location.accuracy,
                sentToContacts = false
            )
            val updateId = sosSessionDao.insertLocationUpdate(locationUpdate)

            // Send to contacts who want location updates
            val contacts = emergencyContactDao.getPrimaryContacts(userId)
                .filter { it.sendLocationUpdates }

            if (contacts.isNotEmpty()) {
                val message = buildString {
                    append("📍 Location Update\n\n")
                    append("${getAddress(location)}\n")
                    append("GPS: ${location.latitude}, ${location.longitude}\n")
                    append("Maps: https://maps.google.com/?q=${location.latitude},${location.longitude}\n\n")
                    append("Time: ${getCurrentTimeFormatted()}\n\n")
                    append("SafeHaven SOS is still active.")
                }

                for (contact in contacts) {
                    sendSMS(contact.phoneNumber, message)
                }

                sosSessionDao.markLocationUpdateSent(updateId, System.currentTimeMillis())
                sosSessionDao.incrementSMSCount(session.id, contacts.size)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Send SMS message
     * Works offline via cellular network
     */
    private fun sendSMS(phoneNumber: String, message: String): Boolean {
        return try {
            if (!hasSMSPermission()) {
                return false
            }

            val smsManager = context.getSystemService(SmsManager::class.java)
            val parts = smsManager.divideMessage(message)

            if (parts.size == 1) {
                smsManager.sendTextMessage(phoneNumber, null, message, null, null)
            } else {
                smsManager.sendMultipartTextMessage(phoneNumber, null, parts, null, null)
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Get current GPS location
     */
    private suspend fun getCurrentLocation(): Location? {
        return try {
            if (!hasLocationPermission()) {
                return null
            }

            fusedLocationClient.lastLocation.await()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Convert GPS coordinates to human-readable address
     */
    private fun getAddress(location: Location): String {
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)

            if (addresses?.isNotEmpty() == true) {
                val address = addresses[0]
                buildString {
                    if (address.featureName != null) append("${address.featureName}, ")
                    if (address.thoroughfare != null) append("${address.thoroughfare}, ")
                    if (address.locality != null) append("${address.locality}, ")
                    if (address.adminArea != null) append("${address.adminArea} ")
                    if (address.postalCode != null) append("${address.postalCode}")
                }.trim().removeSuffix(",")
            } else {
                "${location.latitude}, ${location.longitude}"
            }
        } catch (e: Exception) {
            "${location.latitude}, ${location.longitude}"
        }
    }

    /**
     * Get current time formatted
     */
    private fun getCurrentTimeFormatted(): String {
        val sdf = java.text.SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault())
        return sdf.format(java.util.Date())
    }

    /**
     * Check if app has SMS permission
     */
    private fun hasSMSPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.SEND_SMS
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Check if app has location permission
     */
    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Load active session for user (call on app start)
     */
    suspend fun loadActiveSession(userId: String) {
        scope.launch {
            val session = sosSessionDao.getActiveSession(userId)
            _activeSession.value = session
            _isSOSActive.value = session != null
        }
    }
}
