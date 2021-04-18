package com.GroceerCart.sa.activity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.IntentSender.SendIntentException
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.GroceerCart.sa.R
import com.GroceerCart.sa.listener.GroceerBaseActivity
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.*
import kotlinx.android.synthetic.main.activity_groceer_pin.*
import java.util.*


class GroceerPinActivity : GroceerBaseActivity() {
    private val sharedPrefFile = "groceerpreference"
    private lateinit var  sharedPreferences: SharedPreferences
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private  var requestingLocationUpdates : Boolean = false
    private var REQUEST_LOCATION_CODE = 101
   // private var mGoogleApiClient: GoogleApiClient? = null
    private var mLocation: Location? = null
    private var mLocationRequest: LocationRequest? = null
    private val UPDATE_INTERVAL = (2 * 1000).toLong()  /* 10 secs */
    private val FASTEST_INTERVAL: Long = 2000 /* 2 sec */
    private  var latitude:Double = 0.0
    private var longitude:Double = 0.0
    private  lateinit var  progressBar:ProgressBar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_groceer_pin)
        progressBar = findViewById(R.id.progressBar)
        sharedPreferences = this.getSharedPreferences(sharedPrefFile,Context.MODE_PRIVATE)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for (location in locationResult.locations){
                   //Toast.makeText(applicationContext,"Current Location Found ",Toast.LENGTH_LONG).show()
                    requestingLocationUpdates = false
                    setPinCode(location)
                    stopLocationUpdates()

                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onStop() {
        super.onStop()
    }


    private fun intiLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    1
                )
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    1
                )
            }
        }else{
            startLocationUpdates()
        }
    }


    fun startGpsLocation(){
        val googleApiClient =
            GoogleApiClient.Builder(this).addApi(LocationServices.API).build()
        googleApiClient.connect()

        val locationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 1000
        locationRequest.fastestInterval = 1000 / 2.toLong()

        val builder1 =
            LocationSettingsRequest.Builder().addLocationRequest(locationRequest)

        val result =
            LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder1.build())
        result.setResultCallback { locationSettingsResult ->
            val status =
                locationSettingsResult.status
            when (status.statusCode) {
                LocationSettingsStatusCodes.SUCCESS -> {
                    Log.i("GroceerPinActivity", "All location settings are satisfied.")
                    intiLocationPermission()
                }
                LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                    Log.i(
                        "GroceerPinActivity",
                        "Location settings are not satisfied. Show the user a dialog to upgrade location settings "
                    )
                    try {
                        // Show the dialog by calling startResolutionForResult(), and check the result
                        // in onActivityResult().
                        status.startResolutionForResult(this, 1)
                    } catch (e: SendIntentException) {
                        Log.i(
                            "GroceerPinActivity",
                            "PendingIntent unable to execute request."
                        )
                    }
                }
                LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> Log.i(
                   "GroceerPinActivity",
                    "Location settings are inadequate, and cannot be fixed here. Dialog not created."
                )
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Toast.makeText(this, "GPS Permission Granted", Toast.LENGTH_SHORT).show()
        intiLocationPermission()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        when (requestCode) {
            1 -> {
                if (grantResults.isNotEmpty() && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED) {
                    if ((ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION) ===
                                PackageManager.PERMISSION_GRANTED)) {
                        startLocationUpdates()
                    }
                } else {
                    //Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
                }
                return
            }
        }
    }

     private fun startLocationUpdates(){
         requestingLocationUpdates = true
         mLocationRequest = LocationRequest.create().setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY).setInterval(UPDATE_INTERVAL).setFastestInterval(FASTEST_INTERVAL)
        // Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()

         if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
         ) {
             return
         }else{
             fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                 location?.let { it: Location ->
                   //  Toast.makeText(this, "Location Found", Toast.LENGTH_LONG).show()
                 } ?: kotlin.run {
                     // Handle Null case or Request periodic location update https://developer.android.com/training/location/receive-location-updates
                 }

                 fusedLocationClient.requestLocationUpdates(mLocationRequest,
                     locationCallback,
                     Looper.getMainLooper())
             }
         }

    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    fun getPinLocation(view: View){
        progressBar.visibility = View.VISIBLE
        requestingLocationUpdates = true
        startGpsLocation()

    }

    private fun setPinCode(location: Location) {
        Log.e("PinActivity",getConnectionStatus().toString())
        if (getConnectionStatus()){
            if (location.latitude > 0 && location.longitude >0) {
                val addresses: List<Address>
                val geocoder: Geocoder = Geocoder(this, Locale.getDefault())
                var lat: Double = location.latitude
                var lng: Double = location.longitude
                addresses = geocoder.getFromLocation(
                    lat,
                    lng,
                    1
                ) // Here 1 represent max location result to returned, by documents it recommended 1 to 5

                val address: String = addresses[0]
                    .getAddressLine(0) // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                val postalCode: String = addresses[0].getPostalCode()
                progressBar.visibility = View.GONE
                edtPinCode.setText(postalCode)
            }
        }else{
            progressBar.visibility = View.GONE
            Toast.makeText(this,"Network Connection Error! , Please check your Internet Connection and Try Again",Toast.LENGTH_LONG).show()
        }

    }

    fun getVendorList(view: View) {
        if(edtPinCode.text.length>0){
            val editor:SharedPreferences.Editor =  sharedPreferences.edit()
            editor.putString("pinCode",edtPinCode.text.toString())
            editor.commit()
            editor.apply()
            startActivity(Intent(this, GroceerVendorListActivity::class.java).putExtra("pinCode",edtPinCode.text.toString()))
        }else{
            Toast.makeText(this,"Please Enter Pin code and Try Again!",Toast.LENGTH_LONG).show()

        }
    }
}