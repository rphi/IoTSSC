package com.airsense.iotssc_app.utils

import android.content.Context
import android.location.Location
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.airsense.iotssc_app.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.harrysoft.androidbluetoothserial.BluetoothManager
import com.harrysoft.androidbluetoothserial.BluetoothSerialDevice
import com.harrysoft.androidbluetoothserial.SimpleBluetoothDeviceInterface
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers


class DataLogger(appContext: Context, workerParams: WorkerParameters)
    : Worker(appContext, workerParams) {
    private val TAG = "DataLogger"
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var deviceInterface: SimpleBluetoothDeviceInterface? = null

    override fun doWork(): Result {
        // Do the work here--in this case, upload the images.


        getDataFromHardware()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(applicationContext)
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->

            if (location != null) {
                val satellites = location.extras.getInt("satellites")
                Log.i("ACCURACY", location.accuracy.toString())
                Log.i("PROVIDER", location.provider.toString())
                Log.i("SATELLITES", satellites.toString())
                if (satellites>0 || location.accuracy <= 15) {
                    Log.i(TAG, "uploading to server")
                    uploadToServer()
                } else {
                    Log.i(TAG, "INSIDE")
                }
            } else {
                Log.i(TAG, "got null location")
            }
        }


        // Indicate whether the task finished successfully with the Result
        return Result.success()
    }

    private fun getDataFromHardware() {
        val ctx = applicationContext
        val prefs = ctx.getSharedPreferences(
                ctx.getString(R.string.bluetooth_settings_file), Context.MODE_PRIVATE)
        val uuid = prefs.getString("UUID", null)
        Log.i(TAG, "btooth uuid=$uuid")
        val bluetoothManager = BluetoothManager.getInstance()
        val result = bluetoothManager.openSerialDevice(uuid)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ connectedDevice: BluetoothSerialDevice? ->
                    deviceInterface = connectedDevice!!.toSimpleDeviceInterface()
                    deviceInterface!!.sendMessage("s")
                    bluetoothManager.closeDevice(uuid)
                }, { error: Throwable? -> Log.e(TAG, error.toString()) })

    }




    private fun uploadToServer() {

    }
}


