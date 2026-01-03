package com.stack.player

import android.app.Application
import com.stack.core.crash.CrashCapture
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class StackApplication : Application() {

    @Inject
    lateinit var crashCapture: CrashCapture

    override fun onCreate() {
        super.onCreate()

        // Initialize crash capture for in-app crash logging (SSOT 13.1)
        crashCapture.initialize()
    }
}
