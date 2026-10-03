package com.octomid.trisbraind

import android.app.Application
import com.octomid.trisbraind.di.AppContainer

class TrisApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
