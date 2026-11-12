package com.estcomputer.patient

import android.app.Application
import com.estcomputer.patient.runtime.PatientRuntime
import com.estcomputer.patient.runtime.androidModules
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class PatientApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val koin = startKoin {
            androidContext(this@PatientApplication)
            modules(androidModules)
        }.koin
        koin.get<PatientRuntime>().start()
    }
}
