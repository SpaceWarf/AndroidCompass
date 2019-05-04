package com.apps.spacewarf.projetfinal

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.Toolbar

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import android.util.Log
import android.view.View
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageButton
import com.apps.spacewarf.projetfinal.database.PlaceModel
import com.apps.spacewarf.projetfinal.database.PlaceRoomDatabase
import com.apps.spacewarf.projetfinal.managers.LocationManager
import com.apps.spacewarf.projetfinal.managers.PlacesManager
import com.google.android.gms.common.api.Status
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import java.util.*
import com.apps.spacewarf.projetfinal.managers.PermissionsManager
import android.arch.lifecycle.Observer
import android.content.DialogInterface
import android.content.IntentFilter
import android.widget.ArrayAdapter
import android.widget.Toast
import com.apps.spacewarf.projetfinal.bluetooth.BluetoothClient
import com.apps.spacewarf.projetfinal.managers.BluetoothManager
import android.bluetooth.BluetoothDevice as BluetoothDevice1

private const val LOCATION_REQUEST_CODE = 101
private const val BLUETOOTH_REQUEST_CODE = 102

private const val CLEAR_PLACE_RESULT_CODE = 1
private const val GOTO_PLACE_RESULT_CODE = 2
private const val ZOOM = 12f

class MapsActivity : AppCompatActivity(),
    OnMapReadyCallback,
    GoogleMap.OnMapClickListener,
    PlaceSelectionListener {

    private lateinit var mMap: GoogleMap
    private lateinit var placesClient :PlacesClient
    private lateinit var placeAutoComplete: AutocompleteSupportFragment
    private lateinit var toolbar: Toolbar
    private lateinit var navigateBtn: ImageButton
    private lateinit var bluetoothBtn: ImageButton
    private lateinit var clearBtn: ImageButton
    private lateinit var database: PlaceRoomDatabase

    private var initialLocation: Location? = null

    /**
     * This method set the initial state of the application in this order:
     *    - Bind UI elements
     *    - Update UI visibility
     *    - Setup toolbar
     *    - Setup database connection
     *    - Setup battery status
     *    - Setup Places API
     *    - Setup permissions
     *    - Setup map fragment
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        navigateBtn = findViewById(R.id.navigate_btn)
        bluetoothBtn = findViewById(R.id.bluetooth_btn)
        clearBtn = findViewById(R.id.clear_btn)
        updateUIVisibility()

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        database = PlaceRoomDatabase.getDatabase(this)

        setupPlacesAPI()

        PermissionsManager.initialize(this)

        if (PermissionsManager.checkBluetoothPermission(this) && BluetoothManager.isBluetoothActive()) {
            Log.i("server", "Launching server")
            BluetoothManager.startServer(this)
        }

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

    }

    /**
     * Inflates the menu in the toolbar
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    /**
     * Handles the custom menu bar button presses. We delegate most actions
     * to the parent class, but we handle the manage and history button ourselves
     */
    override fun onOptionsItemSelected(item : MenuItem) : Boolean {
        return when (item.itemId) {
            R.id.action_manage -> {
                onManagePressed()
                true
            }
            R.id.action_history -> {
                onHistoryPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * Call back of the permission query which handles behavior depending on
     * if the user granted or denied the requested permissions
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        when (requestCode) {
            LOCATION_REQUEST_CODE -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED)
                    finish()
                else
                    enableMapLocation()
            }
            BLUETOOTH_REQUEST_CODE -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED)
                    finish()
            }
        }
    }

    /**
     * This callback is triggered when the map is ready to be used
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isMapToolbarEnabled = false
        mMap.setOnMapClickListener(this)
        enableMapLocation()
    }

    /**
     * Handles user clicks on the map to allow custom locations to be selected
     */
    override fun onMapClick(position: LatLng?) {
        position?.let {
            clearPlace()
            PlacesManager.selectedLatLng = it
            PlacesManager.selectedName = "Custom Location"
            renderPlace()
        }
    }

    /**
     * Enables location on the map fragment and place the view to the user's current location
     */
    private fun enableMapLocation() {
        if (PermissionsManager.checkLocationPermission(this)) {
            mMap.isMyLocationEnabled = true
            LocationManager.getInstance(this)
                .mLocation.observe(this, Observer {
                    if (initialLocation == null && it != null) {
                        initialLocation = it
                        goToUserLocation()
                    }
            })
        }
    }

    /**
     * Setup the Places API for the maps search bar
     */
    private fun setupPlacesAPI() {
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, "GOOGLE_API_KEY_HERE")
            placesClient = Places.createClient(this)
        }

        placeAutoComplete = supportFragmentManager.findFragmentById(R.id.place_autocomplete) as AutocompleteSupportFragment
        placeAutoComplete.setPlaceFields(Arrays.asList(Place.Field.NAME, Place.Field.LAT_LNG))
        placeAutoComplete.setOnPlaceSelectedListener(this)
    }

    /**
     * Handles success states when a user input a Place in the search bar
     */
    override fun onPlaceSelected(place: Place) {
        place.latLng?.let {
            PlacesManager.selectedLatLng = place.latLng
            PlacesManager.selectedName = place.name ?: ""
            renderPlace()
        }
    }

    /**
     * Handles error states when a user input a Place in the search bar
     */
    override fun onError(status: Status) {
        Log.d("Maps", "An error occurred: $status")
    }

    /**
     * Places the map view at the user location
     */
    private fun goToUserLocation() {
        LocationManager.getInstance(this)
            .mLocation.value?.let {
                goTo(LatLng(it.latitude, it.longitude), ZOOM)
            }
    }

    /**
     * Places the map view at the selected place or the user location if no place is selected
     */
    private fun goToSelectedPlace() {
        if (PlacesManager.selectedLatLng != null) {
            placeAutoComplete.setText("")
            renderPlace(ZOOM)
        } else {
            goToUserLocation()
        }
    }

    /**
     * Places the map view at the requested position with requested zoom
     */
    private fun goTo(location: LatLng, zoom: Float) {
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, zoom))
    }

    /**
     * Places the map view at the requested position
     */
    private fun goTo(location: LatLng) {
        mMap.animateCamera(CameraUpdateFactory.newLatLng(location))
    }

    /**
     * Add a marker on the map with the requested information as parameters
     */
    private fun addMarker(position: LatLng, title: String) {
        val marker = MarkerOptions()
            .position(position)
            .title(title)
        mMap.addMarker(marker)
    }

    /**
     * Makes the navigate button invisible when no selectedPlace is selected
     */
    private fun updateUIVisibility() {
        PlacesManager.selectedLatLng?.let {
            navigateBtn.visibility = View.VISIBLE
            bluetoothBtn.visibility = View.VISIBLE
            clearBtn.visibility = View.VISIBLE
            return
        }
        navigateBtn.visibility = View.INVISIBLE
        bluetoothBtn.visibility = View.INVISIBLE
        clearBtn.visibility = View.INVISIBLE
    }

    /**
     * Set the map position to the selected place at the desired zoom and add a marker
     */
    private fun renderPlace(zoom: Float) {
        PlacesManager.selectedLatLng?.let {
            mMap.clear()
            goTo(it, zoom)
            addMarker(it, PlacesManager.selectedName)
            updateUIVisibility()
        }
    }

    /**
     * Set the map position to the selected place and add a marker
     */
    private fun renderPlace() {
        PlacesManager.selectedLatLng?.let {
            mMap.clear()
            goTo(it)
            addMarker(it, PlacesManager.selectedName)
            updateUIVisibility()
        }
    }

    /**
     * Unselects the currently selected Place
     */
    private fun clearPlace() {
        mMap.clear()
        PlacesManager.clearSelectedPlace()
        placeAutoComplete.setText("")
        updateUIVisibility()
    }

    /**
     * Handle results when a child activity finishes and returns data to this activity
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        placeAutoComplete.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                CLEAR_PLACE_RESULT_CODE -> {
                    if (data?.extras?.getBoolean("ClearPlace") == true) {
                        clearPlace()
                    }
                }
                GOTO_PLACE_RESULT_CODE -> {
                    if (data?.extras?.getBoolean("GoToPlace") == true) {
                        goToSelectedPlace()
                    }
                }
            }
        }
    }

    /**
     * Handles the reception of Bluetooth data and prompts the user to go to render
     * this location on the map or not
     */
    fun onReceiveBluetoothData(name: String, location: LatLng) {
        AlertDialog.Builder(this)
            .setTitle("You have received a location: $name")
            .setMessage("Do you want to go to this location?")
            .setPositiveButton("Go") { _, _ ->
                PlacesManager.selectedLatLng = location
                PlacesManager.selectedName = name
                placeAutoComplete.setText("")
                renderPlace()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    fun onNavigatePressed(view: View) {
        val intent = Intent(this, CompassActivity::class.java)
        PlacesManager.selectedLatLng?.let {
            database.placeDao().insert(PlaceModel(0, it.latitude, it.longitude, PlacesManager.selectedName))
            startActivityForResult(intent, CLEAR_PLACE_RESULT_CODE)
        }
    }

    fun onClearPressed(view: View) {
        clearPlace()
    }

    fun onBluetoothPressed(view: View) {
        if (!BluetoothManager.isBluetoothActive()) {
            Toast.makeText(this.applicationContext, "Bluetooth is disabled", Toast.LENGTH_SHORT).show()
            return
        }
        BluetoothManager.initialize(this)
        val builder = AlertDialog.Builder(this)
        builder
            .setTitle("Select a paired device")
            .setAdapter(BluetoothManager.mArrayAdapter) { _, which ->
                BluetoothManager.startClient(this, which)
            }
            .setNegativeButton("Cancel", null)
        builder.create().show()
    }

    private fun onManagePressed() {
        val intent = Intent(this, ManageActivity::class.java)
        startActivity(intent)
    }

    private fun onHistoryPressed() {
        val intent = Intent(this, HistoryActivity::class.java)
        startActivityForResult(intent, GOTO_PLACE_RESULT_CODE)
    }
}
