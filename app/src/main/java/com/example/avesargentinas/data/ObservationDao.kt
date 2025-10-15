package com.example.avesargentinas.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ObservationDao {
    @Query("SELECT * FROM observations ORDER BY capturedAt DESC")
    fun observeAll(): Flow<List<Observation>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(observation: Observation): Long

    @Delete
    suspend fun delete(observation: Observation)

    @Query("SELECT * FROM observations WHERE id = :id")
    suspend fun getById(id: Long): Observation?
}
