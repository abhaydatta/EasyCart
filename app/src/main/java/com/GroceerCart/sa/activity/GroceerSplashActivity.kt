package com.GroceerCart.sa.activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.GroceerCart.sa.R

class GroceerSplashActivity : AppCompatActivity() {
    // This is the loading time of the splash screen
    private val sharedPrefFile = "groceerpreference"

    private val splashTimeOut:Long = 3000 // 10 sec
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_groceer_splash)
        Handler().postDelayed({
            // This method will be executed once the timer is over
            // Start your app main activity
            val sharedPreferences: SharedPreferences = this.getSharedPreferences(sharedPrefFile,
                Context.MODE_PRIVATE)
            var vendorAvailable = sharedPreferences.getBoolean("VendorFlag",false)
           startActivity(Intent(this, GroceerPinActivity::class.java))

             if (vendorAvailable){
                 startActivity(Intent(this, GroceerHomeActivity::class.java))
             }else{
                 startActivity(Intent(this, GroceerPinActivity::class.java))
             }
            // close this activityG
            finish()
        }, splashTimeOut)
    }
}