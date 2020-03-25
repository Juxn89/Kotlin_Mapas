package com.example.maps

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
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
import com.google.android.gms.maps.model.*
import java.util.jar.Manifest

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener, GoogleMap.OnMarkerDragListener {

    private lateinit var mMap: GoogleMap

    private val permisoFineLocation = android.Manifest.permission.ACCESS_FINE_LOCATION
    private val permisoCoarseLocation = android.Manifest.permission.ACCESS_COARSE_LOCATION
    private val CODIGO_SOLICITUD_PERMISO = 100

    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var locationRequest: LocationRequest? = null
    private var callback: LocationCallback? = null

    // Marcadores del mapa
    private var listaMarcadores:ArrayList<Marker>? = null

    private var marcadorGolden:Marker? = null
    private var marcadorPiramides:Marker? = null
    private var marcadorTorre:Marker? = null

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
        //mMap.mapType = GoogleMap.MAP_TYPE_NORMAL

        val exitoCambioMapa = mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.estilo_mapa))

        if (!exitoCambioMapa) {
            // Mencionar que hubo un problema al cambio de mapa
        }

        val GOLDEN_GATE = LatLng(37.8199286, -122.4782551)
        val PIRAMIDES = LatLng(29.9772962, 31.1324955)
        val TORRE_PISA = LatLng(43.722952, 10.396597)

        marcadorGolden = mMap.addMarker(MarkerOptions().position(GOLDEN_GATE).title("Golden Gate").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)).alpha(0.3f))
        marcadorGolden?.tag = 0

        marcadorPiramides = mMap.addMarker(MarkerOptions().position(PIRAMIDES).title("Pirámides").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE)).alpha(0.6f))
        marcadorPiramides?.tag = 0

        marcadorTorre = mMap.addMarker(MarkerOptions()
            .position(TORRE_PISA)
            .title("Torre de Pisa")
            //.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
            .icon(BitmapDescriptorFactory.fromResource(R.drawable.tren_1))
            .snippet("Metro de Torre de Pisa")
            .alpha(0.9f))
        marcadorTorre?.tag = 0

        mMap.setOnMarkerClickListener(this)
        mMap.setOnMarkerDragListener(this)

        prepararMarcadores()

        if (validarPersimisosUbicacion()) {
            obtenerUbicacion()
        }
        else {
            pedirPermisos()
        }
    }

    private fun prepararMarcadores() {
        listaMarcadores = ArrayList()
        mMap.setOnMapLongClickListener {
            location: LatLng? ->
            listaMarcadores?.add(mMap.addMarker(MarkerOptions().position(location!!).title("Golden Gate").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_tren)).alpha(0.3f)))
            listaMarcadores?.last()!!.isDraggable = true
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

    override fun onMarkerClick(marcador: Marker?): Boolean {
        var numeroClicks = marcador?.tag as? Int

        if(numeroClicks != null){
            numeroClicks++
            marcador?.tag = numeroClicks

            Toast.makeText(this, "Se han dado ${numeroClicks} clicks", Toast.LENGTH_SHORT).show()
        }

        return false
    }

    override fun onMarkerDragEnd(marcador: Marker?) {
        Toast.makeText(this, "Terminó de mover el marcador", Toast.LENGTH_SHORT).show()
        Log.d("MARCADOR_FINAL", marcador?.position?.latitude.toString())
    }

    override fun onMarkerDragStart(marcador: Marker?) {
        Toast.makeText(this, "Empenzando a mover el marcador", Toast.LENGTH_SHORT).show()
        Log.d("MARCADOR_INICIAL", marcador?.position?.latitude.toString())

        val index = listaMarcadores?.indexOf(marcador!!)
        Log.d("MARCADOR_INICIAL", listaMarcadores?.get(index!!)!!.position?.latitude.toString())
    }

    override fun onMarkerDrag(marcador: Marker?) {
        title = "${marcador?.position?.latitude} - ${marcador?.position?.longitude}"
    }
}
