package com.orbitalsonic.locationfinder.presentation.location

sealed class LocationIntent {
    data class Initialize(val hasPermission: Boolean) : LocationIntent()
    data class Refresh(val hasPermission: Boolean) : LocationIntent()
    data object PermissionRationaleRequired : LocationIntent()
    data object PermissionPermanentlyDenied : LocationIntent()
    data object OpenAppSettingsRequested : LocationIntent()
    data object OpenGpsSettingsRequested : LocationIntent()
}
