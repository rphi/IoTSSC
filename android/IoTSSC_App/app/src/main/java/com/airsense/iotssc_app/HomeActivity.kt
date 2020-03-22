package com.airsense.iotssc_app

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.airsense.iotssc_app.utils.DataLoggerService


class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        if (!isMyServiceRunning(DataLoggerService::class.java)){
            val activityIntent = Intent(this, MainActivity::class.java)
            startActivity(activityIntent)
        }
        setupButtons()
    }

    private fun setupButtons(){
        val alertButton = findViewById<Button>(R.id.alertbutton)
        // set on-click listener
        alertButton.setOnClickListener {
            val activityIntent = Intent(this, TrackerActivity::class.java)
            startActivity(activityIntent)
        }
        val webButton = findViewById<Button>(R.id.webbutton)
        // set on-click listener
        webButton.setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("http://www.google.com"))
            startActivity(browserIntent)
        }
    }

    private fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }
}
