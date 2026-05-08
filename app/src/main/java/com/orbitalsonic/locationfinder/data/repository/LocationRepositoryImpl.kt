package com.orbitalsonic.locationfinder.data.repository

import android.location.Location
import com.orbitalsonic.locationfinder.data.local.LocationCacheDataStore
import com.orbitalsonic.locationfinder.data.remote.LocationException
import com.orbitalsonic.locationfinder.data.remote.LocationRemoteDataSource
import com.orbitalsonic.locationfinder.domain.model.LocationData
import com.orbitalsonic.locationfinder.domain.model.LocationFailure
import com.orbitalsonic.locationfinder.domain.repository.LocationRepository

class LocationRepositoryImpl(
    private val remoteDataSource: LocationRemoteDataSource,
    private val cacheDataStore: LocationCacheDataStore
) : LocationRepository {

    override suspend fun getCachedLocation(): LocationData? = cacheDataStore.getLocation()

    override suspend fun fetchFreshLocation(hasPermission: Boolean): Result<LocationData> {
        if (!hasPermission) return Result.failure(LocationException(LocationFailure.PermissionDenied))
        return runCatching { remoteDataSource.getCurrentLocation() }
    }

    override suspend fun saveLocationIfMoved(previous: LocationData?, new: LocationData) {
        if (previous == null || distanceInMeters(previous, new) > MIN_DISTANCE_TO_UPDATE_METERS) {
            cacheDataStore.saveLocation(new)
        }
    }

    override fun mapFailure(throwable: Throwable): LocationFailure {
        return when (throwable) {
            is LocationException -> throwable.failure
            else -> LocationFailure.Unknown(throwable.message)
        }
    }

    private fun distanceInMeters(old: LocationData, new: LocationData): Float {
        val result = FloatArray(1)
        Location.distanceBetween(
            old.latitude,
            old.longitude,
            new.latitude,
            new.longitude,
            result
        )
        return result.firstOrNull() ?: 0f
    }

    private companion object {
        const val MIN_DISTANCE_TO_UPDATE_METERS = 300f
    }
}
