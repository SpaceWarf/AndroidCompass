package com.apps.spacewarf.projetfinal.utilities

import com.google.android.gms.maps.model.LatLng
import kotlin.math.pow

private const val EARTH_RADIUS = 6371

class Utilities {
    companion object {
        /**
         * Transforms degree values to radians
         */
        private fun deg2rad(degrees: Double) : Double {
            return degrees * (Math.PI / 180)
        }

        /**
         * Uses the Haversine formula to calculate the distance between two points on a sphere
         * Reference: https://en.wikipedia.org/wiki/Haversine_formula
         */
        fun calculateHaversineDistance(point1: LatLng, point2: LatLng) : Double {
            val deltaLat = deg2rad(point2.latitude - point1.latitude)
            val deltaLng = deg2rad(point2.longitude - point1.longitude)
            val a =
                Math.sin(deltaLat / 2).pow(2) +
                Math.cos(deg2rad(point1.latitude)) * Math.cos(deg2rad(point2.latitude)) *
                Math.sin(deltaLng / 2).pow(2)
            val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
            return EARTH_RADIUS * c
        }

        /**
         * This is a low pass filter which attenuates data variations to make them smoother
         * Concretely, this means that big changes in the magnetic and acceleration data are attenuated
         * so the compass needle doesn't bounce around erratically on the screen
         */
        fun lowPassFilter(input: FloatArray, output: FloatArray) {
            val alpha = 0.02f

            for (i in input.indices) {
                output[i] = output[i] + alpha * (input[i] - output[i])
            }
        }
    }
}