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

        return fetchCurrentLocation()
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
