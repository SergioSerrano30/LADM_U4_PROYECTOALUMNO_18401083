package mx.tecnm.ladm_u4_proyectoalumno_18401083.Util

import android.location.Location
import android.location.LocationListener
import mx.tecnm.ladm_u4_proyectoalumno_18401083.interfaces.OnLocationListener

import android.os.Bundle




class MyLocationListener(private var onLocationListener: OnLocationListener):LocationListener {
    override fun onLocationChanged(location: Location) {
        onLocationListener.onLocationListener(location)
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {

    }

    override fun onProviderEnabled(provider: String) {

    }

    override fun onProviderDisabled(provider: String) {

    }

}