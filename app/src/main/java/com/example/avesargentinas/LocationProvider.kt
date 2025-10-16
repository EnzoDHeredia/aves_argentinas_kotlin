package com.example.avesargentinas

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.random.Random

class LocationProvider(context: Context) {

    private val fusedClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context.applicationContext)

    data class Coordinates(
        val latitude: Double,
        val longitude: Double
    )

    suspend fun getCurrentLocation(context: Context): Coordinates? {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

        if (!hasPermission) {
            return null
        }

        val coords = fetchCurrentLocation()
        
        // Si la ofuscación está habilitada, aplicar desplazamiento aleatorio
        return if (coords != null && SettingsManager.isObfuscateLocation(context)) {
            obfuscateCoordinates(coords)
        } else {
            coords
        }
    }
    
    /**
     * Ofusca las coordenadas añadiendo un desplazamiento aleatorio
     * de hasta 500 metros en cualquier dirección.
     */
    private fun obfuscateCoordinates(coords: Coordinates): Coordinates {
        // Desplazamiento máximo en metros
        val maxOffsetMeters = 500.0
        
        // Aproximación: 1 grado de latitud ≈ 111,000 metros
        // 1 grado de longitud varía con la latitud
        val metersPerDegreeLat = 111000.0
        val metersPerDegreeLon = 111000.0 * kotlin.math.cos(Math.toRadians(coords.latitude))
        
        // Generar offset aleatorio en metros
        val offsetMetersLat = Random.nextDouble(-maxOffsetMeters, maxOffsetMeters)
        val offsetMetersLon = Random.nextDouble(-maxOffsetMeters, maxOffsetMeters)
        
        // Convertir a grados
        val offsetLat = offsetMetersLat / metersPerDegreeLat
        val offsetLon = offsetMetersLon / metersPerDegreeLon
        
        return Coordinates(
            latitude = coords.latitude + offsetLat,
            longitude = coords.longitude + offsetLon
        )
    }

    @SuppressLint("MissingPermission")
    private suspend fun fetchCurrentLocation(): Coordinates? {
        return suspendCancellableCoroutine { continuation ->
            fusedClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
                .addOnSuccessListener { location: Location? ->
                    if (!continuation.isActive) return@addOnSuccessListener
                    continuation.resume(location?.let { Coordinates(it.latitude, it.longitude) })
                }
                .addOnFailureListener { error ->
                    if (continuation.isActive) continuation.resume(null)
                }
        }
    }
}
