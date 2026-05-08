package com.orbitalsonic.locationfinder.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.orbitalsonic.locationfinder.domain.model.LocationData
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.locationDataStore: DataStore<Preferences> by preferencesDataStore(name = "location_cache")

class LocationCacheDataStore(private val context: Context) {

    private object Keys {
        val Latitude = doublePreferencesKey("latitude")
        val Longitude = doublePreferencesKey("longitude")
        val FullAddress = stringPreferencesKey("full_address")
        val ShortAddress = stringPreferencesKey("short_address")
        val City = stringPreferencesKey("city")
        val Country = stringPreferencesKey("country")
    }

    suspend fun getLocation(): LocationData? {
        val preferences = context.locationDataStore.data
            .map { pref ->
                pref[Keys.Latitude]?.let { latitude ->
                    val longitude = pref[Keys.Longitude] ?: return@map null
                    LocationData(
                        latitude = latitude,
                        longitude = longitude,
                        fullAddress = pref[Keys.FullAddress].orEmpty(),
                        shortAddress = pref[Keys.ShortAddress].orEmpty(),
                        city = pref[Keys.City].orEmpty(),
                        country = pref[Keys.Country].orEmpty()
                    )
                }
            }
            .first()
        return preferences
    }

    suspend fun saveLocation(location: LocationData) {
        context.locationDataStore.edit { pref ->
            pref[Keys.Latitude] = location.latitude
            pref[Keys.Longitude] = location.longitude
            pref[Keys.FullAddress] = location.fullAddress
            pref[Keys.ShortAddress] = location.shortAddress
            pref[Keys.City] = location.city
            pref[Keys.Country] = location.country
        }
    }
}
