package com.example.ecar_service_station.infra.app;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.IBinder;

import androidx.core.content.ContextCompat;

public class GpsTracker extends Service implements LocationListener {

    private static final long MIN_DISTANCE_UPDATES = 10;
    private static final long MIN_TIME_UPDATES = 1000 * 60;

    private final Context context;
    private final LocationManager locationManager;

    public GpsTracker(Context context) {
        this.context = context;
        this.locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
    }

    @SuppressLint("MissingPermission")
    public Location getLocation() {
        Location location = null;

        boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (isGpsEnabled || isNetworkEnabled) {
            int fineLocationPermission =
                    ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION);

            int coarseLocationPermission =
                    ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION);

            if (fineLocationPermission != PackageManager.PERMISSION_GRANTED
                    || coarseLocationPermission != PackageManager.PERMISSION_GRANTED) {

                return null;
            }

            if (isNetworkEnabled) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_UPDATES, MIN_DISTANCE_UPDATES, this);

                if (locationManager != null) {
                    location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                }
            }

            if (isGpsEnabled && location == null) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_UPDATES, MIN_DISTANCE_UPDATES, this);

                if (locationManager != null) {
                    location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                }
            }
        }

        return location;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onLocationChanged(Location location) {

    }
}
