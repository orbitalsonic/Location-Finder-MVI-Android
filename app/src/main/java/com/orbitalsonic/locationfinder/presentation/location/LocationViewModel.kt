package com.orbitalsonic.locationfinder.presentation.location

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.orbitalsonic.locationfinder.domain.model.LocationFailure
import com.orbitalsonic.locationfinder.domain.repository.LocationRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class LocationViewModel(
    private val repository: LocationRepository
) : ViewModel() {

    private val _state = MutableStateFlow(LocationState())
    val state: StateFlow<LocationState> = _state.asStateFlow()

    private val effectChannel = Channel<LocationEffect>(Channel.BUFFERED)
    val effect = effectChannel.receiveAsFlow()

    fun onIntent(intent: LocationIntent) {
        when (intent) {
            is LocationIntent.Initialize -> initialize(intent.hasPermission)
            is LocationIntent.Refresh -> refresh(intent.hasPermission)
            LocationIntent.PermissionRationaleRequired -> emitEffect(LocationEffect.ShowPermissionRationaleDialog)
            LocationIntent.PermissionPermanentlyDenied -> emitEffect(LocationEffect.ShowPermissionPermanentlyDeniedDialog)
            LocationIntent.OpenAppSettingsRequested -> emitEffect(LocationEffect.OpenAppSettings)
            LocationIntent.OpenGpsSettingsRequested -> emitEffect(LocationEffect.OpenGpsSettings)
        }
    }

    private fun initialize(hasPermission: Boolean) {
        viewModelScope.launch {
            val cached = repository.getCachedLocation()
            if (cached != null) {
                _state.value = _state.value.copy(
                    location = cached,
                    cacheStatus = CacheStatus.Cached,
                    errorMessage = null
                )
            }
            refresh(hasPermission)
        }
    }

    private fun refresh(hasPermission: Boolean) {
        if (!hasPermission) {
            emitEffect(LocationEffect.RequestPermission)
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            val previous = _state.value.location
            val result = repository.fetchFreshLocation(hasPermission = true)
            result.onSuccess { fresh ->
                repository.saveLocationIfMoved(previous = previous, new = fresh)
                _state.value = _state.value.copy(
                    isLoading = false,
                    location = fresh,
                    cacheStatus = CacheStatus.Fresh,
                    errorMessage = null
                )
            }.onFailure { throwable ->
                handleFailure(repository.mapFailure(throwable))
            }
        }
    }

    private fun handleFailure(failure: LocationFailure) {
        val currentState = _state.value
        val message = when (failure) {
            LocationFailure.PermissionDenied -> {
                emitEffect(LocationEffect.RequestPermission)
                "Location permission denied."
            }
            LocationFailure.PermissionPermanentlyDenied -> {
                emitEffect(LocationEffect.ShowPermissionPermanentlyDeniedDialog)
                "Location permission permanently denied."
            }
            LocationFailure.GpsDisabled -> {
                emitEffect(LocationEffect.ShowEnableGpsDialog)
                "GPS is disabled."
            }
            LocationFailure.GeocoderUnavailable -> "Geocoder is unavailable on this device."
            LocationFailure.NullLocation -> "Could not fetch your current location."
            is LocationFailure.Unknown -> failure.message ?: "Unexpected location error."
        }

        _state.value = currentState.copy(
            isLoading = false,
            cacheStatus = if (currentState.location != null) {
                CacheStatus.FreshFailedUsingCache
            } else {
                currentState.cacheStatus
            },
            errorMessage = message
        )
        emitEffect(LocationEffect.ShowMessage(message))
    }

    private fun emitEffect(effect: LocationEffect) {
        viewModelScope.launch { effectChannel.send(effect) }
    }

    class Factory(private val repository: LocationRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return LocationViewModel(repository) as T
        }
    }
}
