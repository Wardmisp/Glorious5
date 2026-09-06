package com.g5

import android.app.Application
import com.g5.di.appKoinModules
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class G5Application : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@G5Application)
            modules(appKoinModules())
        }
    }
}
