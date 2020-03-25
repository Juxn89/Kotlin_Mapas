package com.example.maps

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.util.jar.Manifest

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

    private val permisoFineLocation = android.Manifest.permission.ACCESS_FINE_LOCATION
    private val permisoCoarseLocation = android.Manifest.permission.ACCESS_COARSE_LOCATION
    private val CODIGO_SOLICITUD_PERMISO = 100

    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var locationRequest: LocationRequest? = null
    private var callback: LocationCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = FusedLocationProviderClient(this)
        inicializarLocationRequest()

        callback = object:LocationCallback(){
            override fun onLocationResult(locationResult: LocationResult?) {
                super.onLocationResult(locationResult)

                if (mMap != null) {
                    for (ubicacion in locationResult?.locations!!) {

                        mMap.isMyLocationEnabled = true
                        mMap.uiSettings.isMyLocationButtonEnabled = true

                        //Toast.makeText(applicationContext, "${ubicacion.latitude}, ${ubicacion.longitude}", Toast.LENGTH_SHORT).show()
                        val sydney = LatLng(ubicacion.latitude, ubicacion.longitude)
                        mMap.addMarker(MarkerOptions().position(sydney).title("Aquí estoy"))
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
                    }
                }
            }
        }
    }

    fun inicializarLocationRequest(){
        locationRequest = LocationRequest()
        locationRequest?.interval = 10000
        locationRequest?.fastestInterval = 5000
        locationRequest?.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
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
        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL

        if (validarPersimisosUbicacion()) {
            obtenerUbicacion()
        }
        else {
            pedirPermisos()
        }
    }

    override fun onStart() {
        super.onStart()

        if (validarPersimisosUbicacion()) {
            obtenerUbicacion()
        }
        else {
            pedirPermisos()
        }
    }

    override fun onPause() {
        super.onPause()

        detenerActualizacionUbicacion()
    }

    private fun validarPersimisosUbicacion():Boolean {
        val hayUbicacionPremisa = ActivityCompat.checkSelfPermission(this, permisoFineLocation) == PackageManager.PERMISSION_GRANTED
        val hayUbicacionOrdinaria = ActivityCompat.checkSelfPermission(this, permisoCoarseLocation) == PackageManager.PERMISSION_GRANTED

        return hayUbicacionPremisa && hayUbicacionOrdinaria
    }

    @SuppressLint("MissingPermission")
    private fun obtenerUbicacion() {
        /*fusedLocationClient?.lastLocation?.addOnSuccessListener(this, object: OnSuccessListener<Location>{
            override fun onSuccess(p0: Location?) {
                if (p0 != null){
                    Toast.makeText(applicationContext, "${p0?.latitude} - ${p0?.longitude}", Toast.LENGTH_SHORT).show()
                }
                else {
                    Toast.makeText(applicationContext, "No hay última ubicación", Toast.LENGTH_SHORT).show()
                }
            }
        })*/

        /*callback = object:LocationCallback(){
            override fun onLocationResult(locationResult: LocationResult?) {
                super.onLocationResult(locationResult)

                for (ubicacion in locationResult?.locations!!) {
                    Toast.makeText(applicationContext, "${ubicacion.latitude}, ${ubicacion.longitude}", Toast.LENGTH_SHORT).show()
                }
            }
        }*/

        fusedLocationClient?.requestLocationUpdates(locationRequest, callback, null)
    }

    private  fun pedirPermisos() {
        val deboProveerContexto = ActivityCompat.shouldShowRequestPermissionRationale(this, permisoFineLocation)

        if (deboProveerContexto) {
            solicitudPermiso()
        } else {
            solicitudPermiso()
        }
    }

    private fun solicitudPermiso() {
        requestPermissions(arrayOf(permisoFineLocation, permisoCoarseLocation), CODIGO_SOLICITUD_PERMISO)
    }

    private fun detenerActualizacionUbicacion() {
        fusedLocationClient?.removeLocationUpdates(callback)
    }
}
