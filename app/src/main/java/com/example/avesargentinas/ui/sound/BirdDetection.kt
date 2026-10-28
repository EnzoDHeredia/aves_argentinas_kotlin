package com.example.avesargentinas.ui.sound

data class BirdDetection(
    val modelIndex: Int,
    val modelScientificName: String,
    val scientificName: String,
    val commonName: String?,
    val score: Float,
    val localKey: String?,
    val distributionUri: String?
)
