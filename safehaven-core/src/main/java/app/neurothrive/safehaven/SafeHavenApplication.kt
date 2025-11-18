package app.neurothrive.safehaven

import android.app.Application
import app.neurothrive.safehaven.util.crypto.SafeHavenCrypto
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SafeHavenApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // CRITICAL: Initialize encryption key on first launch
        SafeHavenCrypto.initializeKey()
    }
}
