package com.apps.spacewarf.projetfinal.managers

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log

private const val LOCATION_REQUEST_CODE = 101
private const val BLUETOOTH_REQUEST_CODE = 102

class PermissionsManager {
    companion object {
        /**
         * Verify that the user does not have the required permissions granted
         * then ask the user to grant them
         */
        fun initialize(activity: Activity) {
            if (!checkLocationPermission(activity)) {
                requestLocationPermission(activity)
            }
            if (!checkBluetoothPermission(activity)) {
                requestBluetoothPermission(activity)
            }
        }

        /**
         * Verify that the user has granted the fine location permission
         */
        fun checkLocationPermission(activity: Activity): Boolean {
            return ContextCompat.checkSelfPermission(
                activity.applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        }

        /**
         * Verify that the user has granted the Bluetooth permission
         */
        fun checkBluetoothPermission(activity: Activity): Boolean {
            return ContextCompat.checkSelfPermission(
                activity.applicationContext,
                Manifest.permission.BLUETOOTH
            ) == PackageManager.PERMISSION_GRANTED
        }

        /**
         * Requests the location permissions to the user.
         */
        private fun requestLocationPermission(activity: Activity) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_REQUEST_CODE
            )
        }

        /**
         * Requests the Bluetooth permissions to the user
         */
        private fun requestBluetoothPermission(activity: Activity) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.BLUETOOTH),
                BLUETOOTH_REQUEST_CODE
            )
        }
    }
}