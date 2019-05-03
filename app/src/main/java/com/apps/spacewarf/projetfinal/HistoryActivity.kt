package com.apps.spacewarf.projetfinal

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.widget.ListView
import com.apps.spacewarf.projetfinal.adapters.PlacesAdapter
import com.apps.spacewarf.projetfinal.managers.PlacesManager
import com.google.android.gms.maps.model.LatLng

class HistoryActivity : AppCompatActivity() {
    private lateinit var recentPlacesList: ListView

    /**
     * Bind UI, fetch places to render and create OnClick event for list items
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        recentPlacesList = findViewById(R.id.places_list)

        PlacesManager.loadRecentPlaces(this)
        val placesList = PlacesManager.getUniqueRecentPlaces()
        val adapter = PlacesAdapter(this, placesList)
        recentPlacesList.adapter = adapter
        recentPlacesList.setOnItemClickListener { _, _, position, _ ->
            val place = placesList[position]
            PlacesManager.selectedLatLng = LatLng(place.latitude, place.longitude)
            PlacesManager.selectedName = place.name
            finish(true)
        }
    }

    private fun finish(goTo: Boolean) {
        val data = Intent()
        data.putExtra("GoToPlace", goTo)
        setResult(Activity.RESULT_OK, data)
        finish()
    }

    /**
     * Handles the topbar back arrow button. We do not want a new instance of the Maps activity
     * to be created when the button is pressed. Only the current activity to finish.
     */
    override fun onOptionsItemSelected(item : MenuItem) : Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish(false)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}