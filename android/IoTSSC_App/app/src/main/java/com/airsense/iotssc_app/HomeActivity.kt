package com.airsense.iotssc_app

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Button
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import com.airsense.iotssc_app.utils.DataLoggerService
import java.lang.Integer.max
import java.lang.Integer.min


class HomeActivity : AppCompatActivity() {
    private val EXPOSURELIMIT = 1440 * 125 // mins in day * midway through unhealthy to sensitive group section of index

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        if (!isMyServiceRunning(DataLoggerService::class.java)){
            val activityIntent = Intent(this, MainActivity::class.java)
            startActivity(activityIntent)
        }
        setupButtons()
        findViewById<ProgressBar>(R.id.exposureBar).max =  EXPOSURELIMIT
        val handler = Handler()
        Thread(Runnable {
            while (true) {
                handler.post(Runnable {
                    updateProgressBar()
                })
                try {
                    Thread.sleep(60_000)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }

            }
        }).start()
    }

    private fun updateProgressBar(){
        val exposurePrefs = applicationContext.getSharedPreferences(
                applicationContext.getString(R.string.daily_exposure_file), Context.MODE_PRIVATE)
        val exposure = exposurePrefs.getInt("total", 0)
        Log.i("home", "updating exposure total to: $exposure")
        val progressBar =findViewById<ProgressBar>(R.id.exposureBar)
        progressBar.progress = exposure
        val red = min((exposure/EXPOSURELIMIT)*255, 255)
        val green = max(((EXPOSURELIMIT-exposure)/EXPOSURELIMIT)*255, 0)
        val states = arrayOf(intArrayOf(android.R.attr.state_enabled), intArrayOf(-android.R.attr.state_enabled), intArrayOf(-android.R.attr.state_checked), intArrayOf(android.R.attr.state_pressed))
        val colours = intArrayOf(Color.argb(255,red,green, 0))
        progressBar.backgroundTintList = ColorStateList(arrayOf(intArrayOf(android.R.attr.state_enabled)), colours)

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
