package com.apps.spacewarf.projetfinal.database

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.google.android.gms.maps.model.LatLng


/**
 * This class corresponds to the schema of the table "devices".
 */
@Entity(tableName = "places")
data class PlaceModel (
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "place_id")
    val id: Int = 0,
    @ColumnInfo(name = "place_latitude")
    val latitude: Double,
    @ColumnInfo(name = "place_longitude")
    val longitude: Double,
    @ColumnInfo(name = "place_name")
    val name: String
)