package com.apps.spacewarf.projetfinal.managers

import android.app.Activity
import android.arch.lifecycle.MutableLiveData
import android.location.Location
import com.google.android.gms.location.*

class LocationManager(activity: Activity) {
    private val fusedLocationClient =
        LocationServices.getFusedLocationProviderClient(activity.applicationContext)
    var mLocation = MutableLiveData<Location>()
        private set

    /**
     * Initializes the location callback which updates the current user location.
     * The updates are only triggered every 2 seconds to spare battery life.
     */
    init {
        if (PermissionsManager.checkLocationPermission(activity)) {
            val locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult?) {
                    locationResult ?: return
                    for (location in locationResult.locations) {
                        mLocation.value = location
                    }
                }
            }

            val request = LocationRequest()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(2000)

            fusedLocationClient.requestLocationUpdates(request, locationCallback, null)
        }
    }

    companion object {
        private var INSTANCE: LocationManager? = null

        /**
         * Creates an instance of the location manager if one does not exists. If one exists, return it.
         * This implements the Singleton pattern.
         */
        fun getInstance(activity: Activity) : LocationManager {
            INSTANCE?.let {
                return it
            }
            val newInstance = LocationManager(activity)
            INSTANCE = newInstance
            return newInstance
        }
    }
}