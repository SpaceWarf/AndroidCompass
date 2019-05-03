package com.apps.spacewarf.projetfinal

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.arch.lifecycle.Observer
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.view.animation.Animation.RELATIVE_TO_SELF
import android.view.animation.RotateAnimation
import android.widget.ImageView
import android.widget.TextView
import com.apps.spacewarf.projetfinal.managers.LocationManager
import com.apps.spacewarf.projetfinal.managers.PlacesManager
import com.apps.spacewarf.projetfinal.utilities.Utilities
import com.google.android.gms.maps.model.LatLng
import java.text.DecimalFormat

private const val ARRIVAL_DISTANCE = 0.02

class CompassActivity : AppCompatActivity(), SensorEventListener {
    private lateinit var destinationTitle: TextView
    private lateinit var distanceView: TextView
    private lateinit var compassView: ImageView
    private lateinit var sensorManager: SensorManager

    private var accelerationSensor: Sensor? = null
    private var magneticSensor: Sensor? = null

    private var accelerationMatrix = FloatArray(3)
    private var magneticMatrix = FloatArray(3)
    private var currentDegree = 0f
    private var arrived = false

    private lateinit var currentLocation: Location

    /**
     * Bind UI, subscribe to location updates and handle sensor manager instantiation
     */
    @SuppressLint("ServiceCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_compass)

        destinationTitle = findViewById(R.id.destination_title)
        distanceView = findViewById(R.id.distance_text)
        compassView = findViewById(R.id.compass_image)

        setDestinationText()
        LocationManager.getInstance(this).mLocation.observe(this, Observer {
            it?.let { location ->
                currentLocation = location
                PlacesManager.selectedLatLng?.let { latLng ->
                    val locationLatLng = LatLng(location.latitude, location.longitude)
                    val distance = Utilities.calculateHaversineDistance(locationLatLng, latLng)
                    setDistanceText(distance)
                    if (!arrived) {
                        testArrival(distance)
                    }
                }
            }
        })
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        magneticSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
    }

    /**
     * Activate the sensor updates when the activity is in focus.
     */
    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(this, accelerationSensor, SensorManager.SENSOR_DELAY_GAME)
        sensorManager.registerListener(this, magneticSensor, SensorManager.SENSOR_DELAY_GAME)
    }

    /**
     * Disables the sensor updates when the activity is not on focus. This spares battery life.
     */
    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this, accelerationSensor)
        sensorManager.unregisterListener(this, magneticSensor)
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

    /**
     * Handles changes in registered sensor listeners, which are the magnetometer and the accelerometer
     */
    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            val rotationMatrix = FloatArray(9)
            val orientation = FloatArray(3)

            when (it.sensor.type) {
                Sensor.TYPE_MAGNETIC_FIELD -> Utilities.lowPassFilter(it.values, magneticMatrix)
                Sensor.TYPE_ACCELEROMETER -> Utilities.lowPassFilter(it.values, accelerationMatrix)
            }

            if (SensorManager.getRotationMatrix(rotationMatrix, null, accelerationMatrix, magneticMatrix)) {
                SensorManager.getOrientation(rotationMatrix, orientation)
                rotateCompass(orientation[0])
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    /**
     * Translates the azimuth value into degrees and rotates the compass image with an animation
     */
    private fun rotateCompass(azimuth: Float) {
        var degree = Math.toDegrees(azimuth.toDouble()).toFloat()
        val bearing = currentLocation.bearingTo(PlacesManager.getSelectedPlaceLocation())
        degree -= bearing

        val rotateAnimation = RotateAnimation(
            currentDegree,
            -degree,
            RELATIVE_TO_SELF, 0.5f,
            RELATIVE_TO_SELF, 0.5f
        )
        rotateAnimation.duration = 500
        rotateAnimation.repeatCount = 0
        rotateAnimation.fillAfter = true

        compassView.startAnimation(rotateAnimation)
        currentDegree = -degree
    }

    /**
     * Detects if the user has arrived at its destination and display a dialog alert allowing
     * them to stay in this activity or return to the map activity
     */
    private fun testArrival(distance: Double) {
        if (distance < ARRIVAL_DISTANCE) {
            arrived = true
            AlertDialog.Builder(this)
                .setTitle("You have arrived at your destination")
                .setMessage("Do you want to stay or go back to the map?")
                .setPositiveButton("Go back") { _, _ ->
                    finish()
                }
                .setNegativeButton("Stay", null)
                .show()
        }
    }

    /**
     * Finish this activity with corresponding result data
     */
    override fun finish() {
        val data = Intent()
        data.putExtra("ClearPlace", arrived)
        setResult(Activity.RESULT_OK, data)
        super.finish()
    }

    private fun setDestinationText() {
        destinationTitle.text = String.format(getString(R.string.destination_title), PlacesManager.selectedName)
    }

    private fun setDistanceText(distance: Double) {
        if (distance < 1) {
            distanceView.text = String.format(getString(R.string.distance_m_text), Math.round(distance * 1000))
        } else {
            val formatter = DecimalFormat("0.0")
            distanceView.text = String.format(getString(R.string.distance_km_text), formatter.format(distance))
        }
    }
}