package com.pigo.dinamo.user.utils.location

import android.Manifest
import android.content.pm.PackageManager
import android.location.LocationListener
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.segx01.testfirenase.MainActivity
import com.segx01.testfirenase.utils.PermissionUtils

class LocationUtils(
    private val activity: MainActivity,
    private val permissionUtils: PermissionUtils,
    private val locationListener: LocationListener
) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(activity)

    private val locationRequest: LocationRequest = LocationRequest.create().apply {
        interval = 5000 // 5 seconds
        fastestInterval = 2000 // 2 seconds
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        // Set the smallest displacement to 0 to get continuous updates
        smallestDisplacement = 0f
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            // Handle location updates
            val location = locationResult.lastLocation
            location?.apply {
                locationListener.onLocationChanged(this)
            }
        }
    }


    fun startLocationUpdates() {
        if (permissionUtils.arePermissionsGranted()) {
            if (activity.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                fusedLocationClient.requestLocationUpdates(
                    locationRequest, locationCallback, Looper.getMainLooper()
                )
            } else {
                // The permission is not granted despite the check in arePermissionsGranted
                // This can happen on some devices or specific scenarios, handle accordingly
                // You might want to inform the user or take appropriate actions
                if (!permissionUtils.isLocationEnabled()) permissionUtils.showLocationSettings()
                // Request permissions using PermissionUtils
                permissionUtils.requestPermissions()
            }
        } else {
            if (!permissionUtils.isLocationEnabled()) permissionUtils.showLocationSettings()
            // Request permissions using PermissionUtils
            permissionUtils.requestPermissions()
        }
    }


    fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    fun getCurrentLocation(onLocationResult: (LatLng) -> Unit) {

        if (permissionUtils.arePermissionsGranted()) {
            if (activity.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    location?.let {
                        val latLng = LatLng(it.latitude, it.longitude)
                        onLocationResult.invoke(latLng)
                    }
                }
            } else {
                // The permission is not granted despite the check in arePermissionsGranted
                // This can happen on some devices or specific scenarios, handle accordingly
                // You might want to inform the user or take appropriate actions
                if (!permissionUtils.isLocationEnabled()) permissionUtils.showLocationSettings()
                permissionUtils.requestPermissions()

            }
        } else {
            if (!permissionUtils.isLocationEnabled()) permissionUtils.showLocationSettings()
            // Request permissions using PermissionUtils
            permissionUtils.requestPermissions()
        }

    }
}
