package com.apps.spacewarf.projetfinal.managers

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.widget.ArrayAdapter
import com.apps.spacewarf.projetfinal.MapsActivity
import com.apps.spacewarf.projetfinal.bluetooth.BluetoothClient
import com.apps.spacewarf.projetfinal.bluetooth.BluetoothServerController
import java.util.*

val uuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

class BluetoothManager {
    companion object {
        private val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        const val delimiter = "/@/"
        var devices = ArrayList<BluetoothDevice>()
        var mArrayAdapter: ArrayAdapter<String>? = null

        /**
         * The Bluetooth API does not allow us to query connected devices, only bonded ones.
         * We will have to handle unconnected devices when the user selects the device in the device list.
         */
        fun initialize(activity: MapsActivity) {
                devices = ArrayList()
                mArrayAdapter = ArrayAdapter(activity, android.R.layout.simple_list_item_1)

                for (device in mBluetoothAdapter.bondedDevices) {
                    devices.add(device)
                    mArrayAdapter!!.add((if (device.name != null) device.name else "Unknown") + "\n" + device.address + "\n")
                }
        }

        /**
         * Starts the Bluetooth server controller which will pass client requests to servers
         */
        fun startServer(activity: MapsActivity) {
            BluetoothServerController(activity, uuid).start()
        }

        /**
         * Start a Bluetooth client which will communicate with a Bluetooth server
         */
        fun startClient(activity: MapsActivity, deviceIndex: Int) {
            PlacesManager.selectedLatLng?.let {
                BluetoothClient(activity, PlacesManager.selectedName, it, devices[deviceIndex], uuid).start()
            }
        }

        /**
         * Returns true if there is a Bluetooth adapter and if the Bluetooth service is enabled
         */
        fun isBluetoothActive() : Boolean {
            return mBluetoothAdapter != null && mBluetoothAdapter.isEnabled
        }
    }
}