package com.apps.spacewarf.projetfinal.database

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query

@Dao
interface PlaceDao : BaseDao<PlaceModel> {
    @Query(value = "SELECT * from places")
    fun getAllPlaces(): List<PlaceModel>
    @Query(value = "DELETE from places")
    fun deleteAllPlaces()
}