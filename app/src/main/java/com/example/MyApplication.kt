package com.example

import android.app.Application
import android.util.Log
import com.example.data.remote.FirebaseConfig
import com.example.data.ads.AdMobManager
import com.example.data.remote.NetworkMonitor
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Log.d("MyApplication", "Application onCreate - initializing SDKs")
        
        // 1. Initialize Network Monitor
        try {
            NetworkMonitor.initialize(this)
        } catch (e: Exception) {
            Log.e("MyApplication", "Failed to initialize NetworkMonitor", e)
        }

        // 2. Initialize AdMob Manager
        try {
            AdMobManager.initialize(this)
        } catch (e: Exception) {
            Log.e("MyApplication", "Failed to initialize AdMob", e)
        }

        // 3. Programmatically initialize FirebaseApp
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                val options = FirebaseOptions.Builder()
                    .setApiKey(FirebaseConfig.API_KEY)
                    .setApplicationId(FirebaseConfig.APP_ID)
                    .setProjectId(FirebaseConfig.PROJECT_ID)
                    .setGcmSenderId(FirebaseConfig.MESSAGING_SENDER_ID)
                    .build()
                FirebaseApp.initializeApp(this, options)
                Log.d("MyApplication", "Firebase App initialized programmatically successfully")
            }
        } catch (e: Exception) {
            Log.e("MyApplication", "Failed to initialize FirebaseApp programmatically", e)
        }
    }
}
