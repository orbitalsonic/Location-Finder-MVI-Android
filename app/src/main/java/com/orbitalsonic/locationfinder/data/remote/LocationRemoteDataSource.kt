package com.orbitalsonic.locationfinder.data.remote

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.orbitalsonic.locationfinder.domain.model.LocationData
import com.orbitalsonic.locationfinder.domain.model.LocationFailure
import java.io.IOException
import java.util.Locale
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

class LocationRemoteDataSource(private val context: Context) {

    private val fusedLocationProviderClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    suspend fun getCurrentLocation(): LocationData {
        if (!hasLocationPermission()) {
            throw LocationException(LocationFailure.PermissionDenied)
        }
        if (!isGpsEnabled()) {
            throw LocationException(LocationFailure.GpsDisabled)
        }
        if (!Geocoder.isPresent()) {
            throw LocationException(LocationFailure.GeocoderUnavailable)
        }

        val location = requestCurrentLocation()
            ?: throw LocationException(LocationFailure.NullLocation)

        val addresses = getAddresses(location.latitude, location.longitude)
        val address = addresses.firstOrNull()
        val fullAddress = address?.getAddressLine(0).orEmpty()
        val shortAddress = listOfNotNull(
            address?.thoroughfare,
            address?.subLocality,
            address?.locality
        ).joinToString(", ").ifBlank { fullAddress }

        return LocationData(
            latitude = location.latitude,
            longitude = location.longitude,
            fullAddress = fullAddress,
            shortAddress = shortAddress,
            city = address?.locality.orEmpty(),
            country = address?.countryName.orEmpty()
        )
    }

    @SuppressLint("MissingPermission")
    private suspend fun requestCurrentLocation(): android.location.Location? {
        return suspendCancellableCoroutine { continuation ->
            val cancellationTokenSource = CancellationTokenSource()
            continuation.invokeOnCancellation { cancellationTokenSource.cancel() }
            val request = CurrentLocationRequest.Builder()
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .build()
            fusedLocationProviderClient.getCurrentLocation(request, cancellationTokenSource.token)
                .addOnSuccessListener { continuation.resume(it) }
                .addOnFailureListener {
                    continuation.cancel(LocationException(LocationFailure.Unknown(it.message)))
                }
        }
    }

    private suspend fun getAddresses(latitude: Double, longitude: Double): List<Address> {
        val geocoder = Geocoder(context, Locale.getDefault())
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            suspendCancellableCoroutine { continuation ->
                geocoder.getFromLocation(latitude, longitude, 1) { list ->
                    continuation.resume(list ?: emptyList())
                }
            }
        } else {
            try {
                @Suppress("DEPRECATION")
                geocoder.getFromLocation(latitude, longitude, 1) ?: emptyList()
            } catch (ioe: IOException) {
                throw LocationException(LocationFailure.Unknown(ioe.message))
            }
        }
    }

    private fun hasLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
        return fine == PackageManager.PERMISSION_GRANTED || coarse == PackageManager.PERMISSION_GRANTED
    }

    private fun isGpsEnabled(): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
            locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }
}

class LocationException(val failure: LocationFailure) : Exception()
