package com.airsense.iotssc_app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import android.bluetooth.BluetoothAdapter
import android.location.Location
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager

import com.airsense.iotssc_app.adapter.BluetoothReceiver
import com.harrysoft.androidbluetoothserial.BluetoothManager
import com.harrysoft.androidbluetoothserial.SimpleBluetoothDeviceInterface

import com.airsense.iotssc_app.utils.DataLogger
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import com.google.gson.Gson
import java.util.concurrent.TimeUnit
import com.airsense.iotssc_app.TrackerActivity.SerialMessage as SerialMessage1

class TrackerActivity : AppCompatActivity() {

    // Application Scope
    private lateinit var app: ApplicationData

    // Android Serial Library
    private lateinit var bluetoothManager: BluetoothManager
    private lateinit var deviceInterface: SimpleBluetoothDeviceInterface
    private lateinit var bluetoothReceiver: BluetoothReceiver
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var functions: FirebaseFunctions

    private lateinit var firestore: FirebaseFirestore


    override fun onCreate(savedInstanceState: Bundle?) {
        app = applicationContext as ApplicationData
        bluetoothManager = app.bluetoothManager
        deviceInterface = app.deviceInterface
        bluetoothReceiver = app.bluetoothReceiver
        bluetoothAdapter = app.bluetoothAdapter

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tracker)

        findViewById<Button>(R.id.button).setOnClickListener{
            Log.d("tac","button pressed")
            requestUpdate()
        }

        deviceInterface.setListeners(this::onMessageRecieved, this::onMessageSent, this::onError)

        deviceInterface.sendMessage("s")

        Log.d("tac","ready")
        val dataLoggerWorkRequest = PeriodicWorkRequestBuilder<DataLogger>(15, TimeUnit.MINUTES)
                //.setConstraints(constraints)
                .build()
        WorkManager.getInstance(applicationContext).enqueue(dataLoggerWorkRequest)

        functions = FirebaseFunctions.getInstance()
        firestore = FirebaseFirestore.getInstance()
    }

    data class SerialMessage(
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

    fun onMessageRecieved(message: String){

        val response = Gson().fromJson(message, SerialMessage::class.java);

        if (response.status == "reading") {
            runOnUiThread {
                Toast.makeText(this, "updating...", Toast.LENGTH_LONG).show()
            }
        }

        if (response.status == "values") {
            runOnUiThread {
                val text = findViewById<EditText>(R.id.editText2)
                text.setText(message)
                Toast.makeText(this, "updated", Toast.LENGTH_LONG).show()
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
                            "long" to location.longitude
                    )

                    firestore.collection("readings").add(doc)

                } else {
                    Log.i("tac", "got null location")
                }
            }


        }

        /*
        On send character
        {
            "status":"reading"
        }

        Example sensor data JSON
        {
            "dust":199.40,
            "nh3":6.88,
            "co":0.58,
            "no2":0.12,
            "c3h8":20239.26,
            "c4h10":8246.12,
            "ch4":0.36,
            "h2":0.03,
            "c2h5Oh":0.11,
            "voc":0,
            "eco2":400
         }
         */
    }

    fun requestUpdate() {
        deviceInterface.sendMessage("r")
    }

    fun onMessageSent(message: String){
        Toast.makeText(this, "Msg sent", Toast.LENGTH_LONG)
    }

    fun onError(error: Throwable){
        Toast.makeText(this, "LOL no it ded", Toast.LENGTH_LONG)

    }


}
