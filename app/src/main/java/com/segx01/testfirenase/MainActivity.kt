package com.segx01.testfirenase

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import android.view.animation.LinearInterpolator
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.firebase.geofire.GeoQueryEventListener
import com.firebase.geofire.LocationCallback
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.pigo.dinamo.user.utils.location.LocationUtils
import com.segx01.testfirenase.databinding.ActivityMainBinding
import com.segx01.testfirenase.utils.PermissionUtils
import com.segx01.testfirenase.utils.activity.showMessage
import com.segx01.testfirenase.utils.activity.updateStatusBarIcons
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

class MainActivity : AppCompatActivity(), LocationListener, OnMapReadyCallback {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var geofencingClient: GeofencingClient
    private lateinit var gMap: GoogleMap
    private lateinit var locationUtils: LocationUtils
    private lateinit var permissionUtils: PermissionUtils
    private lateinit var navController: NavController
    private lateinit var handler: Handler
    private var currentMovementIndex = 0
    private val carMarker by lazy { addMarkerCar(carMovements.first(), R.drawable.ic_car) }
    private val carMovements = mutableListOf(
        LatLng(30.047557, 31.238483),
        LatLng(30.048, 31.238),
        LatLng(30.049, 31.237),
        LatLng(30.050, 31.236),
        LatLng(30.051, 31.235),
        LatLng(30.052, 31.234),
        LatLng(30.053, 31.233),
        LatLng(30.054, 31.232),
        LatLng(30.055, 31.231),
        LatLng(30.056, 31.230)
    )
    private val markerMap: MutableMap<String, Marker> = mutableMapOf()
    private var isCarInHome1 = false
    private var isCarInHome2 = false

    // Define home locations
    private val home1 = LatLng(30.055, 31.230)
    private val home2 = LatLng(30.052, 31.235)

    // Define home radius
    private val homeRadius = 100.0

    // Define geofence names
    private val home1Geofence = "Home1"
    private val home2Geofence = "Home2"

    fun addMarkerCar(
        location: LatLng,
        @DrawableRes iconResId: Int,
        title: String? = null,
        width: Float = 24f,
        height: Float = 42f
    ): Marker? {
        val bitmapDescriptor = bitmapDescriptorFromVector(this, iconResId)
        val marker = gMap.addMarker(
            MarkerOptions().position(location).icon(bitmapDescriptor).title(title)
        )
        if (marker != null) {
            markerMap[marker.id] = marker
        }
        return marker
    }

    private fun bitmapDescriptorFromVector(context: Context, vectorResId: Int): BitmapDescriptor {
        val vectorDrawable = ContextCompat.getDrawable(context, vectorResId)
        vectorDrawable?.setBounds(
            0, 0, vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight
        )

        val bitmap = Bitmap.createBitmap(
            vectorDrawable!!.intrinsicWidth, vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888
        )

        val canvas = Canvas(bitmap)
        vectorDrawable.draw(canvas)

        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    private fun getBearing(start: LatLng, end: LatLng): Float {
        val lat1 = Math.toRadians(start.latitude)
        val lon1 = Math.toRadians(start.longitude)
        val lat2 = Math.toRadians(end.latitude)
        val lon2 = Math.toRadians(end.longitude)

        val dLon = lon2 - lon1

        val y = sin(dLon) * cos(lat2)
        val x = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(dLon)

        var brng = atan2(y, x)

        // Convert radians to degrees
        brng = Math.toDegrees(brng)

        // Normalize to range [0,360)
        brng = (brng + 360) % 360

        return brng.toFloat()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val supportMapFragment =
            supportFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        supportMapFragment?.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        geofencingClient = LocationServices.getGeofencingClient(this)
        permissionUtils = PermissionUtils.with(this)
        locationUtils = LocationUtils(this, permissionUtils, this)
        geoFence(home1, home1Geofence)
        geoFence(home2, home2Geofence)
        updateStatusBarIcons(false)


        val location = LatLng(30.056, 31.230)
        val geoFire = GeoFire(FirebaseDatabase.getInstance().getReference("path/to/geofire"))
        val geoQuery = geoFire.queryAtLocation(
            GeoLocation(30.056, 31.230),
            0.1
        ) // Set the radius to 100 meters


        binding.included.fab.setOnClickListener { view ->
            if (permissionUtils.isLocationEnabled()) {
                geoFire.setLocation("Test", GeoLocation(30.056, 31.230))

                geoFire.getLocation("Test", object : LocationCallback {
                    override fun onLocationResult(p0: String?, p1: GeoLocation?) {
                        addCircleToMap(p1?.latitude ?: 0.0, p1?.longitude ?: 0.0, Color.GREEN)
                    }

                    override fun onCancelled(p0: DatabaseError?) {
                        showMessage(p0.toString(), view)
                    }
                })

                geoQuery.addGeoQueryEventListener(object : GeoQueryEventListener {
                    override fun onKeyEntered(p0: String?, p1: GeoLocation?) {
                        showMessage("Car entered GeoFire region", view)
                        Log.e("GeoFire", "Car entered GeoFire region: Key - $p0, Location - $p1")
                        isCarInRadius = true
                    }

                    override fun onKeyExited(p0: String?) {
                        showMessage("Car exited GeoFire region", view)
                        Log.e("GeoFire", "Car exited GeoFire region: Key - $p0")
                        isCarInRadius = false
                    }

                    override fun onKeyMoved(p0: String?, p1: GeoLocation?) {
                        showMessage("Car moved within GeoFire region", view)
                        Log.e(
                            "GeoFire",
                            "Car moved within GeoFire region: Key - $p0, Location - $p1"
                        )
                        // You can add additional logic if needed when the car moves within the region
                    }

                    override fun onGeoQueryReady() {
                        showMessage("onGeoQueryReady", view)
                        showMessage(isCarInRadius.toString(), view)
                        animateCameraV(location)
                        simulateCarMovement()


                        // Schedule periodic updates every 5 seconds
                        val updateInterval = 100L // 5 seconds
                        handler.postDelayed(object : Runnable {
                            override fun run() {
                                updateDistanceToCenter()
                                handler.postDelayed(this, updateInterval)
                            }
                        }, updateInterval)
                    }

                    override fun onGeoQueryError(p0: DatabaseError?) {
                        showMessage("GeoQuery Error: ${p0?.message}", view)
                        Log.e("GeoFire", "GeoQuery Error: ${p0?.message}")
                    }
                })
            }
        }
        // Set up Navigation Component
        setupNavigation()
    }

    private fun geoFence(homeLocation: LatLng, geofenceId: String) {
        val geoFire = GeoFire(FirebaseDatabase.getInstance().getReference("path/to/geofire"))
        val geoQuery = geoFire.queryAtLocation(
            GeoLocation(homeLocation.latitude, homeLocation.longitude),
            homeRadius
        )

        geoQuery.addGeoQueryEventListener(object : GeoQueryEventListener {
            override fun onKeyEntered(p0: String?, p1: GeoLocation?) {
                showMessage("Car entered $geofenceId", binding.root)
                Log.e("GeoFire", "Car entered $geofenceId: Key - $p0, Location - $p1")
                when (geofenceId) {
                    home1Geofence -> isCarInHome1 = true
                    home2Geofence -> isCarInHome2 = true
                }
                showHint()
            }

            override fun onKeyExited(p0: String?) {
                showMessage("Car exited $geofenceId", binding.root)
                Log.e("GeoFire", "Car exited $geofenceId: Key - $p0")
                when (geofenceId) {
                    home1Geofence -> isCarInHome1 = false
                    home2Geofence -> isCarInHome2 = false
                }
                showHint()
            }

            override fun onKeyMoved(p0: String?, p1: GeoLocation?) {
                showMessage("Car moved within $geofenceId", binding.root)
                Log.e("GeoFire", "Car moved within $geofenceId: Key - $p0, Location - $p1")
            }

            override fun onGeoQueryReady() {
                showMessage("onGeoQueryReady for $geofenceId", binding.root)
            }

            override fun onGeoQueryError(p0: DatabaseError?) {
                showMessage("GeoQuery Error for $geofenceId: ${p0?.message}", binding.root)
                Log.e("GeoFire", "GeoQuery Error for $geofenceId: ${p0?.message}")
            }
        })
    }

    private fun showHint() {
        val hint = when {
            isCarInHome1 && isCarInHome2 -> "Car is in both homes"
            isCarInHome1 -> "Car is in Home 1"
            isCarInHome2 -> "Car is in Home 2"
            else -> "Car is outside both homes"
        }
        showMessage(hint, binding.root)
        Log.e("GeoFire", hint)
    }

    private var isCarInRadius = false

    private fun simulateCarMovement() {
        handler = Handler(Looper.getMainLooper())
        handler.post(object : Runnable {
            override fun run() {
                if (currentMovementIndex < carMovements.size - 1) {
                    moveCar(
                        carMovements[currentMovementIndex],
                        carMovements[currentMovementIndex + 1]
                    )
                    currentMovementIndex++
                    handler.postDelayed(this, 3000) // Adjust the delay as needed
                }
            }
        })
    }

    private fun updateDistanceToCenter() {
        val centerLocation = LatLng(30.056, 31.230) // Replace with your actual center
        val distance =
            calculateDistanceBetweenPoints(carMarker?.position ?: LatLng(0.0, 0.0), centerLocation)
        Log.d("GeoFire", "Distance to center: $distance meters")
    }

    private fun calculateDistanceBetweenPoints(start: LatLng, end: LatLng): Float {
        val startLocation = Location("start")
        startLocation.latitude = start.latitude
        startLocation.longitude = start.longitude

        val endLocation = Location("end")
        endLocation.latitude = end.latitude
        endLocation.longitude = end.longitude

        return startLocation.distanceTo(endLocation)
    }

    private fun moveCar(startLoc: LatLng, endLoc: LatLng) {
        if (carMarker != null) {
            val startPosition = carMarker!!.position
            val start = SystemClock.uptimeMillis()
            val interpolator = LinearInterpolator()

            val duration = 3000 // Adjust the duration as needed

            handler.post(object : Runnable {
                override fun run() {
                    val elapsed = SystemClock.uptimeMillis() - start
                    val t = interpolator.getInterpolation(elapsed.toFloat() / duration)
                    val lng = t * endLoc.longitude + (1 - t) * startLoc.longitude
                    val lat = t * endLoc.latitude + (1 - t) * startLoc.latitude

                    carMarker!!.position = LatLng(lat, lng)
                    carMarker!!.rotation = getBearing(startPosition, LatLng(lat, lng))

                    if (t < 1.0) {
                        // Post again 16ms later (60 fps)
                        handler.postDelayed(this, 16)
                    }
                }
            })
        }
    }

    private fun addCircleToMap(latitude: Double, longitude: Double, color: Int) {
        val circleOptions = CircleOptions().center(LatLng(latitude, longitude))
            .radius(100.0) // Set the radius as needed
            .strokeColor(color).fillColor(color and 0x55FFFFFF) // Adjust alpha for fill color
            .visible(true)

        gMap.addCircle(circleOptions)
    }

    fun animateCameraV(location: LatLng, zoomLevel: Float = 15.8f, duration: Int = 1000) {
        val cameraPosition = CameraPosition.Builder().target(location).zoom(zoomLevel).build()
        gMap.animateCamera(
            CameraUpdateFactory.newCameraPosition(cameraPosition), duration, null
        )
    }


    private fun setupNavigation() {
        navController = findNavController(R.id.container)

        val bottomNavView: BottomNavigationView = binding.included.navView
        bottomNavView.setupWithNavController(navController)
    }

    override fun onLocationChanged(location: Location) {
        showMessage(LatLng(location.latitude, location.longitude).toString(), binding.root)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        gMap = googleMap

    }
}

