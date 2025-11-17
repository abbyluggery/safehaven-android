package app.neurothrive.safehaven

import android.app.Application
import app.neurothrive.safehaven.util.crypto.SafeHavenCrypto
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

/**
 * SafeHaven Application Class
 *
 * CRITICAL: Initializes encryption key on first launch
 */
@HiltAndroidApp
class SafeHavenApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize Timber logging
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        // CRITICAL: Initialize encryption key
        try {
            SafeHavenCrypto.initializeKey()
            Timber.d("SafeHaven encryption initialized")
        } catch (e: Exception) {
            Timber.e(e, "CRITICAL: Encryption initialization failed")
        }

        Timber.d("SafeHaven Application started")
    }
}
