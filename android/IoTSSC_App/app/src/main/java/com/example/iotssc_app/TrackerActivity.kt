package com.example.iotssc_app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager

import com.example.iotssc_app.adapter.BluetoothReceiver
import com.example.iotssc_app.adapter.DiscoveredBluetoothDevice
import com.example.iotssc_app.adapter.ScannerDevicesAdapter
import com.example.iotssc_app.utils.Utils
import com.harrysoft.androidbluetoothserial.BluetoothManager
import com.harrysoft.androidbluetoothserial.BluetoothSerialDevice
import com.harrysoft.androidbluetoothserial.SimpleBluetoothDeviceInterface

import java.util.ArrayList

import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.example.iotssc_app.utils.DataLogger
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
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
        deviceInterface.sendMessage("s")
    }

    fun onMessageSent(message: String){
        Toast.makeText(this, "Msg sent", Toast.LENGTH_LONG)
    }

    fun onError(error: Throwable){
        Toast.makeText(this, "LOL no it ded", Toast.LENGTH_LONG)

    }


}
