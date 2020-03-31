package com.airsense.iotssc_app

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.iid.FirebaseInstanceId
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap.OnMapClickListener
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions


class TrackerActivity : AppCompatActivity() {

    private var mapView: MapView? = null;

    // Application Scope
    private lateinit var app: ApplicationData
    private lateinit var functions: FirebaseFunctions

    private lateinit var firestore: FirebaseFirestore
    private lateinit var token: String
    private lateinit var symbolManager: SymbolManager
    private var user: FirebaseUser? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Mapbox.getInstance(this, "pk.eyJ1Ijoic2F0eTkiLCJhIjoiY2s3ZzRkM2J5MDh2YTNkbWZodDhmejJ4YSJ9.WvyMWNMes8c4dzchxhsF5A")
        setContentView(R.layout.activity_tracker)
        mapView = findViewById<MapView>(R.id.mapView)
        mapView!!.onCreate(savedInstanceState)

        functions = FirebaseFunctions.getInstance()
        firestore = FirebaseFirestore.getInstance()
        user = FirebaseAuth.getInstance().currentUser

        mapView!!.getMapAsync(OnMapReadyCallback { mapboxMap ->
            mapboxMap.setStyle(Style.Builder().fromUri(Style.MAPBOX_STREETS)) {
                // Custom map style has been loaded and map is now ready
                // create symbol manager object
                symbolManager = SymbolManager(mapView!!, mapboxMap, it)

                // add click listeners if desired
                symbolManager.addClickListener { symbol ->
                    Log.w("TrackerActivity", "click $symbol")
                }

                symbolManager.addLongClickListener { symbol ->
                    Log.w("TrackerActivity", "long click $symbol")
                }

                symbolManager.iconAllowOverlap = true
                symbolManager.textAllowOverlap = true
                FirebaseInstanceId.getInstance().instanceId
                        .addOnCompleteListener(OnCompleteListener { task ->
                            if (!task.isSuccessful) {
                                Log.w("TrackerActivity", "getInstanceId failed", task.exception)
                                return@OnCompleteListener
                            }

                            // Get new Instance ID token
                            token = task.result?.token!!
                            firestore.collection("notificationSubscribers")
                                    .whereEqualTo("d.auth", user?.uid)
                                    .get()
                                    .addOnSuccessListener { documents ->
                                        for (document in documents) {
                                            Log.d("TrackerActivity", "${document.id} => ${document.data}")
                                            val p: GeoPoint = document.getGeoPoint("l")!!

                                            displayPoint(p.latitude, p.longitude)
                                            // update reg token if device has changed
                                            if (document.getString("d.registrationToken") != token) {
                                                document.reference.update("d.registrationToken", token)
                                            }
                                        }
                                    }
                                    .addOnFailureListener { exception ->
                                        Log.w("TrackerActivity", "Error getting documents: ", exception)
                                    }

                        })

            }
            mapboxMap.addOnMapClickListener(OnMapClickListener { point ->
                startPointSubscription(point)
                true
            })
        })

        val alertButton = findViewById<Button>(R.id.clearAlerts)
        // set on-click listener
        alertButton.setOnClickListener {
            firestore.collection("notificationSubscribers")
                    .whereEqualTo("d.auth", user!!.uid)
                    .get()
                    .addOnSuccessListener { documents ->
                        for (document in documents) {
                            document.reference.delete().addOnCompleteListener { _ ->
                                Log.i("TrackerActivity", "deleted subscription")
                            }
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.w("TrackerActivity", "Error getting documents fore deletion: ", exception)
                    }
            symbolManager.deleteAll()
        }
    }

    private fun startPointSubscription(point: LatLng){
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("Title for this air purity alert")

        // Set up the input
        val input = EditText(this)
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.inputType = InputType.TYPE_CLASS_TEXT
        builder.setView(input)

        // Set up the buttons
        builder.setPositiveButton("OK", DialogInterface.OnClickListener { dialog, which -> stage2PointSubscription(point,input.text.toString()) })
        builder.setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, which -> dialog.cancel() })

        builder.show()
    }

    private fun stage2PointSubscription(point: LatLng, name: String){
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("Level you want to be alerted at?")

        // Set up the input
        val input = EditText(this)
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.inputType = InputType.TYPE_CLASS_NUMBER
        builder.setView(input)

        // Set up the buttons
        builder.setPositiveButton("OK", DialogInterface.OnClickListener { dialog, which -> subscribeToPoint(point,name, input.text.toString().toInt()) })
        builder.setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, which -> dialog.cancel() })

        builder.show()
    }

    private fun displayPoint(lat: Double, long: Double){
        val symbol = symbolManager.create(SymbolOptions()
                .withLatLng(LatLng(lat, long))
                .withIconImage("harbor-15")
                .withIconSize(2.0f)
                .withDraggable(false))
    }

    override fun onStart() {
        super.onStart()
        mapView?.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView?.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView?.onDestroy()
    }

    fun subscribeToPoint(point: LatLng, name: String, limit: Int){
        val subscription = hashMapOf(
                "name" to name,
                "registrationToken" to token,
                "limit" to limit,
                "lat" to point.latitude,
                "long" to point.longitude,
                "auth" to user?.uid
        )

        functions.getHttpsCallable("addMessage")
                .call(subscription)

        displayPoint(point.latitude, point.longitude)
    }


}
