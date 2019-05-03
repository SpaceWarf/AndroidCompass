package com.apps.spacewarf.projetfinal

import android.content.BroadcastReceiver
import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.widget.TextView
import android.content.Intent
import android.content.IntentFilter
import android.net.TrafficStats
import android.os.BatteryManager
import android.view.View
import android.widget.ImageView
import com.apps.spacewarf.projetfinal.database.PlaceRoomDatabase


class ManageActivity: AppCompatActivity() {

    private lateinit var infoBatteryStatus: TextView
    private lateinit var icBatteryStatus: ImageView
    private lateinit var infoBatteryConso: TextView
    private lateinit var infoDownload: TextView
    private lateinit var infoUpload: TextView

    private val batteryInfoReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctxt: Context, intent: Intent) {
            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)
            val batteryStatus: Int = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            infoBatteryStatus.text = "Battery level $level%"
            if (batteryStatus == BatteryManager.BATTERY_STATUS_CHARGING) {
                icBatteryStatus.setImageResource(R.drawable.ic_battery_charging)
            } else {
                var iconToSet = R.drawable.ic_battery_unknown
                when (level) {
                    in 0..5 -> iconToSet = R.drawable.ic_battery_alert
                    in 5..10 -> iconToSet = R.drawable.ic_battery_0
                    in 11..20 -> iconToSet = R.drawable.ic_battery_10
                    in 21..30 -> iconToSet = R.drawable.ic_battery_20
                    in 31..40 -> iconToSet = R.drawable.ic_battery_30
                    in 41..50 -> iconToSet = R.drawable.ic_battery_40
                    in 51..60 -> iconToSet = R.drawable.ic_battery_50
                    in 61..70 -> iconToSet = R.drawable.ic_battery_60
                    in 71..80 -> iconToSet = R.drawable.ic_battery_70
                    in 81..90 -> iconToSet = R.drawable.ic_battery_80
                    in 91..95 -> iconToSet = R.drawable.ic_battery_90
                    in 96..100 -> iconToSet = R.drawable.ic_battery_100
                }
                icBatteryStatus.setImageResource(iconToSet)
            }
            val batteryManager = applicationContext.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
            val energyConsumption = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)/1000
            infoBatteryConso.text = "Battery consumption ${energyConsumption} mA"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage)
        infoBatteryStatus = findViewById(R.id.info_battery_status)
        icBatteryStatus = findViewById(R.id.ic_battery_status)
        infoBatteryConso = findViewById(R.id.info_battery_conso)
        infoDownload = findViewById(R.id.info_download)
        infoUpload = findViewById(R.id.info_upload)
        displayDataUsage()

    }

    override fun onResume() {
        super.onResume()
        registerReceiver(batteryInfoReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    }
    private fun displayDataUsage() {
        infoDownload.text = "${TrafficStats.getUidRxBytes(android.os.Process.myUid())} bytes received"
        infoUpload.text = "${TrafficStats.getUidTxBytes(android.os.Process.myUid())} bytes transmitted"
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(batteryInfoReceiver)
    }
    /**
     * Handles the topbar back arrow button. We do not want a new instance of the Maps activity
     * to be created when the button is pressed. Only the current activity to finish.
     */
    override fun onOptionsItemSelected(item : MenuItem) : Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun onClearPressed(view: View) {
        PlaceRoomDatabase.getDatabase(this.applicationContext).placeDao().deleteAllPlaces()
    }
}