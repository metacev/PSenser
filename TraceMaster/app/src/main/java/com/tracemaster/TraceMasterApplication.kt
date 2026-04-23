package com.tracemaster

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class TraceMasterApplication : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}
