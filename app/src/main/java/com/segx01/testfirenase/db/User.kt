package com.segx01.testfirenase.db

import com.google.android.gms.maps.model.LatLng

data class User(
    val userId: Int,
    val userType: UserType,
    val userName: String,
    val userHomeLocation: LatLng
)