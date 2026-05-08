package com.orbitalsonic.locationfinder.domain.repository

import com.orbitalsonic.locationfinder.domain.model.LocationData
import com.orbitalsonic.locationfinder.domain.model.LocationFailure

interface LocationRepository {
    suspend fun getCachedLocation(): LocationData?
    suspend fun fetchFreshLocation(hasPermission: Boolean): Result<LocationData>
    suspend fun saveLocationIfMoved(previous: LocationData?, new: LocationData)
    fun mapFailure(throwable: Throwable): LocationFailure
}
