package com.apps.spacewarf.projetfinal.database

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context

/**
 * The database of our application which contains the table "places".
 */
@Database(entities = [PlaceModel::class], version = 1)
abstract class PlaceRoomDatabase : RoomDatabase() {
    abstract fun placeDao(): PlaceDao

    /**
     * Singleton of the database. We need a singleton to avoid creating new instances of the database
     * because it's a costly operation.
     */
    companion object {
        @Volatile
        private var INSTANCE: PlaceRoomDatabase? = null

        fun getDatabase(context: Context): PlaceRoomDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PlaceRoomDatabase::class.java,
                    "database"
                ).allowMainThreadQueries().build()
                INSTANCE = instance
                return instance
            }
        }
    }
}