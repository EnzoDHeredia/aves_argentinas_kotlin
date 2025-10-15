package com.example.avesargentinas.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [Observation::class],
    version = 2,
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

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE observations ADD COLUMN individualCount INTEGER NOT NULL DEFAULT 1"
                )
            }
        }

        private fun buildDatabase(context: Context): ObservationDatabase {
            return Room.databaseBuilder(
                context,
                ObservationDatabase::class.java,
                "observation_db"
            ).addMigrations(MIGRATION_1_2).build()
        }
    }
}
