package com.airsense.iotssc_app.utils

import android.content.Context
import android.hardware.Sensor
import android.location.Location
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.airsense.iotssc_app.R
import com.airsense.iotssc_app.TrackerActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.harrysoft.androidbluetoothserial.BluetoothManager
import com.harrysoft.androidbluetoothserial.BluetoothSerialDevice
import com.harrysoft.androidbluetoothserial.SimpleBluetoothDeviceInterface
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import java.util.*


class DataLogger(appContext: Context, workerParams: WorkerParameters)
    : Worker(appContext, workerParams) {
    private val TAG = "DataLogger"
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var deviceInterface: SimpleBluetoothDeviceInterface? = null
    private lateinit var bluetoothManager: BluetoothManager

    private var firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun doWork(): Result {
        // Do the work here--in this case, upload the images.

        getDataFromHardware()

        // Indicate whether the task finished successfully with the Result

        Thread.sleep(1000*120)

        return Result.success()
    }

    private fun getDataFromHardware() {
        val ctx = applicationContext
        val prefs = ctx.getSharedPreferences(
                ctx.getString(R.string.bluetooth_settings_file), Context.MODE_PRIVATE)
        val uuid = prefs.getString("UUID", null)
        Log.i(TAG, "btooth uuid=$uuid")
        bluetoothManager = BluetoothManager.getInstance()
        val result = bluetoothManager.openSerialDevice(uuid)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ connectedDevice: BluetoothSerialDevice? ->
                    deviceInterface = connectedDevice!!.toSimpleDeviceInterface()
                    deviceInterface!!.sendMessage("s")
                    deviceInterface!!.setMessageReceivedListener(this::serial_handler)
                }, { error: Throwable? -> Log.e(TAG, error.toString()) })

    }

    data class SensorValues(
            val status: String = "none",
            val dust: Float? = null,
            val nh3: Float? = null,
            val co: Float? = null,
            val no2: Float? = null,
            val c3h8: Float? = null,
            val c4h10: Float? = null,
            val ch4: Float? = null,
            val h2: Float? = null,
            val c2h5oh: Float? = null,
            val voc: Float? = null,
            val eco2: Float? = null
    )

    private fun serial_handler(data: String){
        val response = Gson().fromJson(data, SensorValues::class.java);

        if (response.status != "values") {
            return
        }


        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(applicationContext)
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->

            if (location != null) {
                val doc = hashMapOf(
                        "dust" to response.dust,
                        "nh3" to response.nh3,
                        "co" to response.co,
                        "no2" to response.no2,
                        "c3h8" to response.c3h8,
                        "ch4h10" to response.c4h10,
                        "ch4" to response.ch4,
                        "h2" to response.h2,
                        "c2h5oh" to response.c2h5oh,
                        "voc" to response.voc,
                        "eco2" to response.eco2,
                        "lat" to location.latitude,
                        "long" to location.longitude,
                        "timestamp" to Date()
                )

                firestore.collection("readings").add(doc)

            } else {
                Log.i("tac", "got null location")
            }

        bluetoothManager.closeDevice(
                applicationContext.getSharedPreferences(
                applicationContext.getString(R.string.bluetooth_settings_file),
                        Context.MODE_PRIVATE).getString("UUID", null)
        )
        }
    }
}


