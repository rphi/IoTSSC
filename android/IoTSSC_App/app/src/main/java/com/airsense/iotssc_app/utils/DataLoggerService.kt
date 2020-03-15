package com.airsense.iotssc_app.utils

import android.app.*
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.airsense.iotssc_app.ApplicationData
import com.airsense.iotssc_app.MainActivity
import com.airsense.iotssc_app.R
import com.airsense.iotssc_app.adapter.BluetoothReceiver
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import com.google.gson.Gson
import com.harrysoft.androidbluetoothserial.BluetoothManager
import com.harrysoft.androidbluetoothserial.BluetoothSerialDevice
import com.harrysoft.androidbluetoothserial.SimpleBluetoothDeviceInterface
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.lang.Error
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.math.max

class DataLoggerService : Service() {
    private val CHANNEL_ID = "ForegroundService Kotlin"


    // Application Scope
    private lateinit var app: ApplicationData

    // Android Serial Library
    private lateinit var bluetoothManager: BluetoothManager
    private lateinit var deviceInterface: SimpleBluetoothDeviceInterface
    private lateinit var bluetoothReceiver: BluetoothReceiver
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var functions: FirebaseFunctions
    private val timer = Timer()
    private lateinit var updateTask: TimerTask
    private lateinit var reconnectTask: TimerTask

    private lateinit var firestore: FirebaseFirestore

    companion object {
        fun startService(context: Context, message: String) {
            val startIntent = Intent(context, DataLoggerService::class.java)
            startIntent.putExtra("inputExtra", message)
            ContextCompat.startForegroundService(context, startIntent)
        }
        fun stopService(context: Context) {
            val stopIntent = Intent(context, DataLoggerService::class.java)
            context.stopService(stopIntent)
        }
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        //do heavy work on a background thread
        val input = intent?.getStringExtra("inputExtra") ?: "default text"
        createNotificationChannel()
        val notification = getMyActivityNotification(input)
        startForeground(1, notification)


        initialiseConnection()

        Log.d("tac","ready")

        functions = FirebaseFunctions.getInstance()
        firestore = FirebaseFirestore.getInstance()


        //stopSelf();
        return START_NOT_STICKY
    }

    private fun initialiseConnection(){
        val ctx = applicationContext
        val prefs = ctx.getSharedPreferences(
                ctx.getString(R.string.bluetooth_settings_file), Context.MODE_PRIVATE)
        val uuid = prefs.getString("UUID", null)
        Log.i("tac", "btooth uuid=$uuid")

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        bluetoothManager = BluetoothManager.getInstance()
        val result = bluetoothManager.openSerialDevice(uuid)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ connectedDevice: BluetoothSerialDevice? ->

                    deviceInterface = connectedDevice!!.toSimpleDeviceInterface()
                    deviceInterface.setListeners(this::onMessageRecieved, this::onMessageSent, this::onError)

                    val delay: Long = 0 // delay for 0 sec.
                    val period: Long = 60_000 // repeat every 60 sec.

                    updateTask = object : TimerTask() {
                        override fun run() {
                            Log.d("tac","pre send")
                            requestUpdate()
                            Log.d("tac","post send")
                        }
                    }
                    timer.scheduleAtFixedRate(updateTask, delay, period)
                }, { error: Throwable? ->
                    run {
                        Log.e("tac", error.toString())
                        reconnectTask = object : TimerTask() {
                            override fun run() {
                                Log.d("tac","pre send")
                                requestUpdate()
                                Log.d("tac","post send")
                            }
                        }
                        timer.schedule(reconnectTask, 30_000)
                    }
                })
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(CHANNEL_ID, "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT)
            val manager = getSystemService(NotificationManager::class.java)
            manager!!.createNotificationChannel(serviceChannel)
        }
    }


    private fun getMyActivityNotification(text: String): Notification {
        // The PendingIntent to launch our activity if the user selects
        // this notification
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
                this,
                0, notificationIntent, 0
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Foreground Service Kotlin Example")
                .setContentText(text)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .build()
        /*return Notification.Builder(this)
                .setContentTitle("Example title")
                .setContentText(text)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(contentIntent).getNotification()

         */
    }

    /**
     * This is the method that can be called to update the Notification
     */
    private fun updateNotification(text: String) {
        val notification: Notification = getMyActivityNotification(text)
        val mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mNotificationManager.notify(1, notification)
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

    fun interpolateBetweenBreaks(value: Float?, g_point: Float, m_point: Float, us_point: Float, u_point: Float, vu_point: Float, h_point: Float, eh_point: Float):Float?{
        if (value == null) {
            return null
        }
        when {
            value <= g_point -> {
                return 50 * (value/g_point)
            }
            value <= m_point -> {
                return 50 + 50 * ((value-g_point)/(m_point-g_point))
            }
            value <= us_point -> {
                return 100 + 50 * ((value-m_point)/(m_point-g_point))
            }
            value <= u_point -> {
                return 150 + 50 * ((value-us_point)/(m_point-g_point))
            }
            value <= vu_point -> {
                return 200 + 100 * ((value-u_point)/(m_point-g_point))
            }
            value <= h_point -> {
                return 300 + 100 * ((value-vu_point)/(m_point-g_point))
            }
            value <= eh_point -> {
                return 400 + 100 * ((value-h_point)/(m_point-g_point))
            }
            else -> {
                return 500f
            }
        }
    }

    fun messageToAQI(message: SerialMessage): Float? {
        val co = interpolateBetweenBreaks(message.co,4.4f, 9.4f, 12.4f, 15.4f, 30.4f, 40.4f, 50.4f)
        val no2 = interpolateBetweenBreaks(message.no2, 53f,100f,360f,649f,1249f,1649f,2049f)
        return max(co!!, no2!!)
    }


    fun onMessageRecieved(message: String){
        Log.d("bluetooth received", message)
        val response = Gson().fromJson(message, SerialMessage::class.java);

        if (response.status == "reading") {
            updateNotification("updating....")
        } else if (response.status == "values") {
            val current = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("HH:mm:ss")
            val formatted = current.format(formatter)
            updateNotification("last updated: $formatted")

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
                    doc["aqi"] = messageToAQI(response)

                    firestore.collection("readings").add(doc)

                } else {
                    Log.i("tac", "got null location")
                }
            }


        } else {
            Log.d("tac", "unrecognised status")
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
        if (::deviceInterface.isInitialized) {
            deviceInterface.sendMessage("r")
            updateNotification("requested update")
        } else {
            onError(Error("device not initialized"))
        }
    }

    fun onMessageSent(message: String){
        Log.i("sent message", message)
    }

    fun onError(error: Throwable){
        Log.e("bluetooth error", error.toString())
        updateNotification("LOL no it ded")
        if (::updateTask.isInitialized) {
            updateTask.cancel()
        }
        timer.purge()
        initialiseConnection()
    }

}