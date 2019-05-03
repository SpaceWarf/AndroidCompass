package com.apps.spacewarf.projetfinal.bluetooth

import android.bluetooth.BluetoothSocket
import android.util.Log
import android.widget.Toast
import com.apps.spacewarf.projetfinal.MapsActivity
import com.apps.spacewarf.projetfinal.managers.BluetoothManager
import com.google.android.gms.maps.model.LatLng
import java.util.*

class BluetoothServer(private val activity: MapsActivity, private val socket: BluetoothSocket): Thread() {
    private val inputStream = socket.inputStream

    /**
     * Reads data from a received socket and forwards the data to our MapsActivity
     */
    override fun run() {
        try {
            val scanner = Scanner(inputStream).useDelimiter("\\A")
            val text = if (scanner.hasNext()) scanner.next() else ""
            val data = text.split(BluetoothManager.delimiter)
            Log.i("server", "Message received: $text")
            activity.runOnUiThread {
                if (data.size == 3) {
                    activity.onReceiveBluetoothData(data[0], LatLng(data[1].toDouble(), data[2].toDouble()))
                }
            }
        } catch (e: Exception) {
            Log.e("client", "Cannot read data", e)
        } finally {
            sleep(1000)
            inputStream.close()
            socket.close()
        }
    }
}