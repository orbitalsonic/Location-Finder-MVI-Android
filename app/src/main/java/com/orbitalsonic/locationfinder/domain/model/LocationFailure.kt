package com.orbitalsonic.locationfinder.domain.model

sealed class LocationFailure {
    data object PermissionDenied : LocationFailure()
    data object PermissionPermanentlyDenied : LocationFailure()
    data object GpsDisabled : LocationFailure()
    data object GeocoderUnavailable : LocationFailure()
    data object NullLocation : LocationFailure()
    data class Unknown(val message: String?) : LocationFailure()
}
