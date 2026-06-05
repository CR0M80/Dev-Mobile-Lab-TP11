package com.example.localisationsmartphone

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.localisationsmartphone.databinding.ActivityMainBinding
import com.google.android.gms.location.*
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var requestQueue: RequestQueue
    
    // REPLACE THIS WITH YOUR ACTUAL LOCAL IP ADDRESS
    private val insertUrl = "http://192.168.100.28/localisation/createPosition.php"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestQueue = Volley.newRequestQueue(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setupLocationUpdates()
        setupListeners()
        updateImeiDisplay()

        checkPermissionsAndStart()
    }

    private fun setupListeners() {
        binding.btnForceSync.setOnClickListener {
            checkPermissionsAndStart()
            Toast.makeText(this, "Forcing sync...", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupLocationUpdates() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    updateUI(location)
                    sendLocationToServer(location)
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateUI(location: Location) {
        binding.tvCoordinates.text = String.format("%.5f, %.5f", location.latitude, location.longitude)
        binding.tvAltitude.text = "${location.altitude.toInt()} m"
        binding.tvAccuracy.text = "${location.accuracy.toInt()} m"
        
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        binding.tvLastSync.text = "Last sync: ${sdf.format(Date())}"
    }

    @SuppressLint("HardwareIds")
    private fun updateImeiDisplay() {
        // Using Android ID as it's more reliable than IMEI on modern Android versions
        val deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        binding.tvImei.text = "Device ID: $deviceId"
    }

    private fun sendLocationToServer(location: Location) {
        binding.syncProgress.visibility = View.VISIBLE
        
        val request = object : StringRequest(
            Method.POST, insertUrl,
            { response ->
                binding.syncProgress.visibility = View.GONE
                // Log or show success
            },
            { error ->
                binding.syncProgress.visibility = View.GONE
                Toast.makeText(this, "Sync Error: Check Server/IP", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)

                params["latitude"] = location.latitude.toString()
                params["longitude"] = location.longitude.toString()
                params["date_position"] = sdf.format(Date())
                params["imei"] = deviceId
                return params
            }
        }

        requestQueue.add(request)
    }

    private fun checkPermissionsAndStart() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                100
            )
        } else {
            startLocationUpdates()
        }
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 60000)
            .setWaitForAccurateLocation(false)
            .setMinUpdateIntervalMillis(30000)
            .setMaxUpdateDelayMillis(60000)
            .build()

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    override fun onPause() {
        super.onPause()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates()
        }
    }
}