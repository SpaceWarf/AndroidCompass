package com.apps.spacewarf.projetfinal.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothServerSocket
import android.util.Log
import com.apps.spacewarf.projetfinal.MapsActivity
import java.util.*

class BluetoothServerController(private val activity: MapsActivity, uuid: UUID) : Thread() {
    private val serverSocket: BluetoothServerSocket?

    init {
        val btAdapter = BluetoothAdapter.getDefaultAdapter()
        if (btAdapter != null) {
            this.serverSocket = btAdapter.listenUsingRfcommWithServiceRecord("BTSocket", uuid)
        } else {
            this.serverSocket = null
        }

    }

    /**
     * Listens to Bluetooth communication and forwards incoming sockets to a bluetooth server
     */
    override fun run() {
        while(true) {
            try {
                serverSocket!!.accept().let {
                    BluetoothServer(this.activity, it).start()
                }
            } catch(e: Exception) {
                Log.e("client", "Server socket error", e)
                break
            }
        }
    }
}