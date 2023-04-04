package com.hybridApp.sample

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class HybridAppApp : Application() {

    override fun onCreate() {
        super.onCreate()
    }

}