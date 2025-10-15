package com.example.avesargentinas.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class ObservationRepository private constructor(
    private val dao: ObservationDao
) {

    fun observeAll(): Flow<List<Observation>> = dao.observeAll()

    suspend fun insert(observation: Observation): Long = withContext(Dispatchers.IO) {
        dao.insert(observation)
    }

    suspend fun delete(observation: Observation) = withContext(Dispatchers.IO) {
        dao.delete(observation)
    }

    suspend fun getById(id: Long): Observation? = withContext(Dispatchers.IO) {
        dao.getById(id)
    }

    companion object {
        @Volatile
        private var instance: ObservationRepository? = null

        fun getInstance(context: Context): ObservationRepository {
            return instance ?: synchronized(this) {
                val database = ObservationDatabase.getInstance(context)
                ObservationRepository(database.observationDao()).also { instance = it }
            }
        }
    }
}
