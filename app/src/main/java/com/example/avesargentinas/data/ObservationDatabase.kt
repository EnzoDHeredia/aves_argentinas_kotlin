package com.example.avesargentinas.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [Observation::class],
    version = 1,
    exportSchema = false
)
abstract class ObservationDatabase : RoomDatabase() {

    abstract fun observationDao(): ObservationDao

    companion object {
        @Volatile
        private var instance: ObservationDatabase? = null

        fun getInstance(context: Context): ObservationDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context.applicationContext).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context): ObservationDatabase {
            return Room.databaseBuilder(
                context,
                ObservationDatabase::class.java,
                "observation_db"
            ).build()
        }
    }
}
