package com.orbitalsonic.locationfinder.presentation.location

sealed class LocationEffect {
    data object RequestPermission : LocationEffect()
    data object ShowPermissionRationaleDialog : LocationEffect()
    data object ShowPermissionPermanentlyDeniedDialog : LocationEffect()
    data object OpenAppSettings : LocationEffect()
    data object ShowEnableGpsDialog : LocationEffect()
    data object OpenGpsSettings : LocationEffect()
    data class ShowMessage(val message: String) : LocationEffect()
}
