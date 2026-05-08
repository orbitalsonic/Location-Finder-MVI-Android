package com.orbitalsonic.locationfinder

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.snackbar.Snackbar
import com.orbitalsonic.locationfinder.data.local.LocationCacheDataStore
import com.orbitalsonic.locationfinder.data.remote.LocationRemoteDataSource
import com.orbitalsonic.locationfinder.data.repository.LocationRepositoryImpl
import com.orbitalsonic.locationfinder.databinding.ActivityMainBinding
import com.orbitalsonic.locationfinder.presentation.location.LocationEffect
import com.orbitalsonic.locationfinder.presentation.location.LocationIntent
import com.orbitalsonic.locationfinder.presentation.location.LocationState
import com.orbitalsonic.locationfinder.presentation.location.LocationViewModel
import com.orbitalsonic.locationfinder.ui.dialog.DialogUtils
import com.orbitalsonic.locationfinder.util.asReadableText
import com.orbitalsonic.locationfinder.util.hasLocationPermission
import com.orbitalsonic.locationfinder.util.shouldShowLocationPermissionRationale
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val viewModel: LocationViewModel by viewModels {
        val repository = LocationRepositoryImpl(
            remoteDataSource = LocationRemoteDataSource(this),
            cacheDataStore = LocationCacheDataStore(this)
        )
        LocationViewModel.Factory(repository)
    }

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val granted = result[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            result[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (granted) {
            viewModel.onIntent(LocationIntent.Refresh(hasPermission = true))
            return@registerForActivityResult
        }

        if (shouldShowLocationPermissionRationale()) {
            viewModel.onIntent(LocationIntent.PermissionRationaleRequired)
        } else {
            viewModel.onIntent(LocationIntent.PermissionPermanentlyDenied)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applyInsets()
        setupClicks()
        collectState()
        collectEffects()
        viewModel.onIntent(LocationIntent.Initialize(hasPermission = hasLocationPermission()))
    }

    private fun applyInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupClicks() {
        binding.btnRefresh.setOnClickListener {
            if (hasLocationPermission()) {
                viewModel.onIntent(LocationIntent.Refresh(hasPermission = true))
            } else if (shouldShowLocationPermissionRationale()) {
                viewModel.onIntent(LocationIntent.PermissionRationaleRequired)
            } else {
                viewModel.onIntent(LocationIntent.Refresh(hasPermission = false))
            }
        }
    }

    private fun collectState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect(::renderState)
            }
        }
    }

    private fun collectEffects() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.effect.collect(::handleEffect)
            }
        }
    }

    private fun renderState(state: LocationState) {
        binding.progress.visibility = if (state.isLoading) View.VISIBLE else View.GONE
        val location = state.location
        binding.tvCity.text = "${getString(R.string.label_city)}: ${location?.city.orUnknown()}"
        binding.tvShortAddress.text =
            "${getString(R.string.label_short_address)}: ${location?.shortAddress.orUnknown()}"
        binding.tvFullAddress.text =
            "${getString(R.string.label_full_address)}: ${location?.fullAddress.orUnknown()}"
        binding.tvLatitude.text =
            "${getString(R.string.label_latitude)}: ${location?.latitude?.toString().orUnknown()}"
        binding.tvLongitude.text =
            "${getString(R.string.label_longitude)}: ${location?.longitude?.toString().orUnknown()}"
        binding.tvCacheStatus.text =
            "${getString(R.string.label_cache_status)}: ${state.cacheStatus.asReadableText()}"
    }

    private fun handleEffect(effect: LocationEffect) {
        when (effect) {
            LocationEffect.RequestPermission -> requestLocationPermission()
            LocationEffect.ShowPermissionRationaleDialog -> showPermissionRationaleDialog()
            LocationEffect.ShowPermissionPermanentlyDeniedDialog -> showPermissionDeniedDialog()
            LocationEffect.OpenAppSettings -> openAppSettings()
            LocationEffect.ShowEnableGpsDialog -> showEnableGpsDialog()
            LocationEffect.OpenGpsSettings -> openLocationSettings()
            is LocationEffect.ShowMessage -> showMessage(effect.message)
        }
    }

    private fun requestLocationPermission() {
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    private fun showPermissionRationaleDialog() {
        DialogUtils.showPermissionRationaleDialog(this) { requestLocationPermission() }
    }

    private fun showPermissionDeniedDialog() {
        DialogUtils.showPermissionDeniedDialog(this) {
            viewModel.onIntent(LocationIntent.OpenAppSettingsRequested)
        }
    }

    private fun showEnableGpsDialog() {
        DialogUtils.showGpsDisabledDialog(this) {
            viewModel.onIntent(LocationIntent.OpenGpsSettingsRequested)
        }
    }

    private fun openAppSettings() {
        val uri = Uri.fromParts("package", packageName, null)
        startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, uri))
    }

    private fun openLocationSettings() {
        startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
    }

    private fun showMessage(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    private fun String?.orUnknown(): String = if (this.isNullOrBlank()) getString(R.string.value_unknown) else this
}