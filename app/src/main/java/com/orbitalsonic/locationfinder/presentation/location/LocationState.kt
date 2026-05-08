package com.orbitalsonic.locationfinder.presentation.location

import com.orbitalsonic.locationfinder.domain.model.LocationData

data class LocationState(
    val isLoading: Boolean = false,
    val location: LocationData? = null,
    val cacheStatus: CacheStatus = CacheStatus.None,
    val errorMessage: String? = null
)

enum class CacheStatus {
    None,
    Cached,
    Fresh,
    FreshFailedUsingCache
}
