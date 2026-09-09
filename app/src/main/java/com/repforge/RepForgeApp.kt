package com.repforge

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class RepForgeApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize Timber for debug logging
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}
