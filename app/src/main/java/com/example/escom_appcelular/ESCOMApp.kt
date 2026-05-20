package com.example.escom_appcelular

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics

class ESCOMApp : Application() {

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)

        FirebaseCrashlytics.getInstance().isCrashlyticsCollectionEnabled = true

        val crashlytics = FirebaseCrashlytics.getInstance()
        crashlytics.setCustomKey("app_version", BuildConfig.VERSION_NAME)
        crashlytics.setCustomKey("build_type", BuildConfig.BUILD_TYPE)
    }
}
