package com.orbitalsonic.locationfinder.domain.model

data class LocationData(
    val latitude: Double,
    val longitude: Double,
    val fullAddress: String,
    val shortAddress: String,
    val city: String,
    val country: String
)
