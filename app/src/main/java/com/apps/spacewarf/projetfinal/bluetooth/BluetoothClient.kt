package com.apps.spacewarf.projetfinal.bluetooth

import android.bluetooth.BluetoothDevice
import android.util.Log
import android.widget.Toast
import com.apps.spacewarf.projetfinal.MapsActivity
import com.apps.spacewarf.projetfinal.managers.BluetoothManager
import com.google.android.gms.maps.model.LatLng
import java.util.*

class BluetoothClient(
    private val activity: MapsActivity,
    locationName: String,
    location: LatLng,
    device: BluetoothDevice,
    uuid: UUID
): Thread() {
    private val socket = device.createRfcommSocketToServiceRecord(uuid)
    private val data = "$locationName${BluetoothManager.delimiter}${location.latitude}${BluetoothManager.delimiter}${location.longitude}"

    /**
     * Creates a socket which communicates with another device then writes data to it.
     */
    override fun run() {
        var isConnected = true
        try {
            socket.connect()
        } catch(e: Exception) {
            Log.e("client", "Could not connect to device")
            activity.runOnUiThread {
                Toast.makeText(activity, "Selected device is not connected.", Toast.LENGTH_LONG).show()
            }
            isConnected = false
        }

        if (isConnected) {
            val outputStream = socket.outputStream
            try {
                outputStream.write(data.toByteArray())
                outputStream.flush()

            } catch(e: Exception) {
                Log.e("client", "Cannot send", e)
            } finally {
                sleep(1000)
                outputStream.close()
                socket.close()
            }
        }
    }
}