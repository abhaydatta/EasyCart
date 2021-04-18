package com.GroceerCart.sa

import android.app.Application
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger

class GroccerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
       FacebookSdk.sdkInitialize(applicationContext)
	 //  FacebookSdk.sdkInitialize(applicationContext)
       AppEventsLogger.activateApp(this)
    }
}