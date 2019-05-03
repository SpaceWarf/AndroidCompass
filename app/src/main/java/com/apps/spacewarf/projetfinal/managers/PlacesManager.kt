package com.apps.spacewarf.projetfinal.managers

import android.content.Context
import android.location.Location
import com.apps.spacewarf.projetfinal.database.PlaceModel
import com.apps.spacewarf.projetfinal.database.PlaceRoomDatabase
import com.google.android.gms.maps.model.LatLng

class PlacesManager {
    companion object {
        var selectedLatLng: LatLng? = null
        var selectedName: String = ""
        val selectedLongitude: Double
            get() = selectedLatLng?.longitude ?: 0.0
        val selectedLatitude: Double
            get() = selectedLatLng?.latitude ?: 0.0

        var recentPlaces: List<PlaceModel> = listOf()

        fun clearSelectedPlace() {
            selectedLatLng = null
            selectedName = ""
        }

        fun loadRecentPlaces(context: Context) {
            recentPlaces = PlaceRoomDatabase.getDatabase(context).placeDao().getAllPlaces()
        }

        fun getUniqueRecentPlaces(): List<PlaceModel> {
            return recentPlaces.reversed().distinctBy { Pair(it.latitude, it.longitude) }
        }

        fun getSelectedPlaceLocation(): Location {
            val location = Location("service provider")
            location.latitude = selectedLatitude
            location.longitude = selectedLongitude
            return location
        }
    }
}