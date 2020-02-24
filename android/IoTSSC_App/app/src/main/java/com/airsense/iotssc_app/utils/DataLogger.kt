package com.airsense.iotssc_app.utils

import android.content.Context
import android.location.Location
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices


class DataLogger(appContext: Context, workerParams: WorkerParameters)
    : Worker(appContext, workerParams) {
    private val TAG = "DataLogger"
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var previousLocation: Location

    override fun doWork(): Result {
        // Do the work here--in this case, upload the images.


        getDataFromHardware()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(applicationContext)
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                if (previousLocation.distanceTo(location) >= 100.0) {
                    uploadToServer()
                } else {
                    Log.i(TAG, "got location too close to previous location")
                }
            }
        }


        // Indicate whether the task finished successfully with the Result
        return Result.success()
    }

    private fun getDataFromHardware() {
        /*
        val ctx = applicationContext
        val prefs = ctx.getSharedPreferences(
                ctx.getString(R.string.bluetooth_settings_file), Context.MODE_PRIVATE)
        val uuid = prefs.getString("UUID", null)
        */
    }

    private fun uploadToServer() {

    }
}


