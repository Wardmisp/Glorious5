package com.g5

import android.app.Application
import com.g5.di.coreModule
import com.g5.di.networkModule
import com.g5.di.databaseModule
import com.g5.di.repositoryModule
import com.g5.di.useCaseModule
import com.g5.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class G5Application : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@G5Application)
            modules(coreModule, networkModule, databaseModule, repositoryModule, useCaseModule, viewModelModule)
        }
    }
}
