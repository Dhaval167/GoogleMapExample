package com.techcoder.googlemapexample.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.*
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.gson.Gson
import com.techcoder.googlemapexample.R
import com.techcoder.googlemapexample.model.GoogleMapDTO
import kotlinx.android.synthetic.main.activity_maps.*
import okhttp3.OkHttpClient
import okhttp3.Request


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    lateinit var locationManager: LocationManager
    var permission = arrayOf(
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.ACCESS_COARSE_LOCATION
    )

    var markerPoints = ArrayList<LatLng>()

    var mGPs = false
    var mNetwork = false

    var locationGps: Location? = null
    var locationNetwork: Location? = null

    companion object {
        const val PERMISSION_REQUEST = 10
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        btn_find_you.setOnClickListener {

            mMap.clear()

            // Add a marker in currentLocations and move the camera
            val currentLocation = if (mNetwork) {
                LatLng(locationNetwork!!.latitude, locationNetwork!!.longitude)
            } else {
                LatLng(locationGps!!.latitude, locationGps!!.longitude)
            }
            mMap.addMarker(
                MarkerOptions().position(currentLocation).title("You").icon(
                    BitmapDescriptorFactory.fromResource(
                        R.drawable.marker
                    )
                )
            )
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 20f))

        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (chackPermissons(permission)) {
                enableView()
            } else {
                requestPermissions(permission, PERMISSION_REQUEST)
            }
        } else {
            enableView()
        }
    }


    private fun enableView() {
        getLocation()
        Toast.makeText(this, "Done", Toast.LENGTH_SHORT).show()
    }

    @SuppressLint("MissingPermission")
    fun getLocation() {

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        mGPs = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        mNetwork = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)


        if (mGPs || mNetwork) {

            if (mGPs) {
                Log.d("MapActivity", "Gps")


                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0F,
                    object : LocationListener {
                        override fun onLocationChanged(location: Location?) {
                            if (location != null) {
                                locationGps = location
                                Log.d("MapActivity", "GPS latitude : ${locationGps!!.latitude}")
                                Log.d(
                                    "MapActivity",
                                    "GPS longitude : ${locationGps!!.longitude}"
                                )

                            }
                        }

                        override fun onStatusChanged(
                            provider: String?,
                            status: Int,
                            extras: Bundle?
                        ) {

                        }

                        override fun onProviderEnabled(provider: String?) {

                        }

                        override fun onProviderDisabled(provider: String?) {

                        }
                    })

                val localGpsLocation =
                    locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                if (localGpsLocation != null) {
                    locationGps = localGpsLocation
                }
            }


            if (mNetwork) {
                Log.d("MapActivity", "Network")

                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 0F,
                    object : LocationListener {
                        override fun onLocationChanged(location: Location?) {
                            if (location != null) {
                                locationNetwork = location

                                Log.d(
                                    "MapActivity",
                                    "Network latitude : ${locationNetwork!!.latitude}"
                                )
                                Log.d(
                                    "MapActivity",
                                    "Network longitude : ${locationNetwork!!.longitude}"
                                )
                            }
                        }

                        override fun onStatusChanged(
                            provider: String?,
                            status: Int,
                            extras: Bundle?
                        ) {

                        }

                        override fun onProviderEnabled(provider: String?) {

                        }

                        override fun onProviderDisabled(provider: String?) {

                        }
                    })

                val localNetworkLocation =
                    locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                if (localNetworkLocation != null) {
                    locationNetwork = localNetworkLocation
                }

                if (locationGps != null && locationNetwork != null) {
                    if (locationGps!!.accuracy > locationNetwork!!.accuracy) {
                        Log.d("MapActivity", "Network latitude : ${locationNetwork!!.latitude}")
                        Log.d("MapActivity", "Network longitude : ${locationNetwork!!.longitude}")
                    } else {

                        Log.d("MapActivity", "GPS latitude : ${locationGps!!.latitude}")
                        Log.d("MapActivity", "GPS longitude : ${locationGps!!.longitude}")

                    }
                }
            }


        } else {
            startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        }


    }

    private fun chackPermissons(permissionArray: Array<String>): Boolean {
        var allSuccess = true
        for (i in permissionArray.indices) {
            if (this.checkCallingOrSelfPermission(permissionArray[i]) == PackageManager.PERMISSION_DENIED) {
                allSuccess = false
            }
        }
        return allSuccess
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST) {
            var allSuccess = true
            for (i in permissions.indices) {
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    allSuccess = false
                }
                val requestAgain =
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && shouldShowRequestPermissionRationale(
                        permissions[i]
                    )
                if (requestAgain) {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(
                        this,
                        "Go to settings and enable the permission",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            if (allSuccess) enableView()
        }
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.setOnMapClickListener {

            if (markerPoints.size > 1) {
                markerPoints.clear()
                mMap.clear()
            }
            markerPoints.add(it)

            val markerOptions = MarkerOptions()
            markerOptions.position(it)
            if (markerPoints.size == 1) {
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
            } else if (markerPoints.size == 2) {
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
            }
            markerOptions.title("${it.latitude} : ${it.longitude}")
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(it, 20f))
            mMap.addMarker(markerOptions)

            // Checks, whether start and end locations are captured
            if (markerPoints.size >= 2) {

                val origin = markerPoints[0]
                val dest = markerPoints[1]

                Log.d("MapDraw", "Before URL")
                // Getting URL to the Google Directions API
                val url = getDirectionsUrl(origin, dest)
                Log.d("MapDraw", url)
                GetDirection(url).execute()

            }

        }
    }

    private fun getDirectionsUrl(origin: LatLng, dest: LatLng): String {

        // Origin of route
        val str_origin = "origin=${origin.latitude},${origin.longitude}"

        // Destination of route
        val str_dest = "destination=${dest.latitude},${dest.longitude}"

        // Sensor enabled
        val sensor = "sensor=false"
        val mode = "mode=driving"

        // Building the parameters to the web service
        val parameters = "$str_origin&$str_dest&$sensor&$mode"

        // Output format
        val output = "json"

        // Building the url to the web service
        val url = "https://maps.googleapis.com/maps/api/directions/$output?$parameters"

        return url
    }

    private inner class GetDirection(val url: String) :
        AsyncTask<Void, Void, List<List<LatLng>>>() {
        override fun doInBackground(vararg params: Void?): List<List<LatLng>> {
            val client = OkHttpClient()
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val data = response.body!!.string()
            Log.d("GoogleMap", " data : $data")
            val result = ArrayList<List<LatLng>>()
            try {
                val respObj = Gson().fromJson(data, GoogleMapDTO::class.java)

                val path = ArrayList<LatLng>()

                for (i in 0 until respObj.routes[0].legs[0].steps.size) {
//                    val startLatLng = LatLng(respObj.routes[0].legs[0].steps[i].start_location.lat.toDouble()
//                            ,respObj.routes[0].legs[0].steps[i].start_location.lng.toDouble())
//                    path.add(startLatLng)
//                    val endLatLng = LatLng(respObj.routes[0].legs[0].steps[i].end_location.lat.toDouble()
//                            ,respObj.routes[0].legs[0].steps[i].end_location.lng.toDouble())
                    path.addAll(decodePolyline(respObj.routes[0].legs[0].steps[i].polyline.points))
                }
                result.add(path)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return result
            d
        }

        override fun onPostExecute(result: List<List<LatLng>>) {
            val lineoption = PolylineOptions()
            for (i in result.indices) {
                lineoption.addAll(result[i])
                lineoption.width(10f)
                lineoption.color(Color.BLUE)
                lineoption.geodesic(true)
            }
            mMap.addPolyline(lineoption)
        }
    }

     fun decodePolyline(encoded: String): List<LatLng> {

        val poly = ArrayList<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat

            shift = 0
            result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng

            val latLng = LatLng((lat.toDouble() / 1E5), (lng.toDouble() / 1E5))
            poly.add(latLng)
        }

        return poly
    }
}