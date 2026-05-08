package com.orbitalsonic.locationfinder.util

import com.orbitalsonic.locationfinder.presentation.location.CacheStatus

fun CacheStatus.asReadableText(): String {
    return when (this) {
        CacheStatus.None -> "None"
        CacheStatus.Cached -> "Loaded from cache"
        CacheStatus.Fresh -> "Fresh location"
        CacheStatus.FreshFailedUsingCache -> "Fresh fetch failed, showing cache"
    }
}
