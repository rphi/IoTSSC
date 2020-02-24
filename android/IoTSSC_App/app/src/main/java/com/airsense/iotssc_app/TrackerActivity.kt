package com.airsense.iotssc_app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import android.bluetooth.BluetoothAdapter
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
import java.util.concurrent.TimeUnit

class TrackerActivity : AppCompatActivity() {

    // Application Scope
    private lateinit var app: ApplicationData

    // Android Serial Library
    private lateinit var bluetoothManager: BluetoothManager
    private lateinit var deviceInterface: SimpleBluetoothDeviceInterface
    private lateinit var bluetoothReceiver: BluetoothReceiver
    private lateinit var bluetoothAdapter: BluetoothAdapter

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



    }

    fun onMessageRecieved(message: String){
        runOnUiThread{
            val text = findViewById<EditText>(R.id.editText2)
            text.setText(message)
            Toast.makeText(this, "Updated", Toast.LENGTH_LONG)
        }
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
