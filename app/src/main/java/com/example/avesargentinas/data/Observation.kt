package com.example.avesargentinas.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "observations")
data class Observation(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val imageUri: String,
    val displayName: String,
    val scientificName: String,
    val confidence: Float,
    val regionalName: String?,
    val latitude: Double?,
    val longitude: Double?,
    val capturedAt: Long,
    val notes: String?
)
