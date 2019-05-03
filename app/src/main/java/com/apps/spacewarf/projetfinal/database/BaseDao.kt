package com.apps.spacewarf.projetfinal.database

import android.arch.persistence.room.*

/**
 * Base interface to allow CRUD requests (except the get request)
 */
@Dao
interface BaseDao<in T> {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(t: T): Long
    @Delete
    fun delete(type : T)
    @Update
    fun update(type : T)
}