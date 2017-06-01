package com.example.arasua6707.mygooglemap;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private LocationManager locationManager;
    private boolean isGPSenabled = false;
    private boolean isNetworkEnabled = false;
    private boolean canGetLocation = false;
    private static final long MIN_TIME_BW_UPDATES = 1000 * 15;
    private static final float MIN_DISTANCE_BW_UPDATES = 5.0f;
    private Location myLocation;
    private static final float MY_LOC_ZOOM_FACTOR = 17.0f;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng dearborn = new LatLng(42.3001807, -83.2437341);
        mMap.addMarker(new MarkerOptions().position(dearborn).title("Birthplace"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(dearborn));

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);


    }

    public void switchView(View v) {
        if (mMap.getMapType() == GoogleMap.MAP_TYPE_SATELLITE) {
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        } else if (mMap.getMapType() == GoogleMap.MAP_TYPE_NORMAL) {
            mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        }
    }

    public void getLocation() {
        try {
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            //get GPS status
            isGPSenabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if (isGPSenabled) Log.d("MyMaps", "getLocation: GPS is enabled");

            //get network status
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if (isNetworkEnabled) Log.d("MyMaps", "getLocation: Network is enabled");

            if (!isGPSenabled && !isNetworkEnabled) {
                Log.d("MyMaps", "getLocation: No provider is enabled");
            } else {
                this.canGetLocation = true;
                if (isGPSenabled) {
                    Log.d("MyMaps", "getLocation: GPS enabled -- requesting location updates");
                    if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_BW_UPDATES, (android.location.LocationListener) locationListenerGps);
                    Log.d("MyMaps", "getLocation:GPS update request succesful");
                    Toast.makeText(this, "Using GPS", Toast.LENGTH_SHORT);
                }
                if (isNetworkEnabled) {
                    Log.d("MyMaps", "getLocation: Network enabled -- requesting location updates");
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_BW_UPDATES, locationListenerNetwork);
                    Log.d("MyMaps", "getLocation: Network update request succesful");
                    Toast.makeText(this, "Using Network", Toast.LENGTH_SHORT);
                }
            }
        } catch (Exception e) {
            Log.d("MyMaps", "getLocation: Caught an exception in getLocation");
            e.printStackTrace();
        }
    }

    android.location.LocationListener locationListenerGps = new android.location.LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            //output message in Log.d and Toast
            //drop a marker on the map (create a method called dropAmarker)
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            //setup a switch statement on status
            //case: LocationProvider.AVAILABLE --> output message
            //case: LocationProvider.OUT_OF_SERVICE --> request updates from NETWORK_PROVIDER
            //case: Location.Provider.TEMPORARILY_UNAVAILABLE --> request updates from NETWORK_PROVIDER
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }
    };

    android.location.LocationListener locationListenerNetwork = new android.location.LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            //output message in Log.d and Toast
            //drop a marker on the map (create a method called dropAmarker)
            //relaunch request for network location updates (requestLocationUpdates(NETWORK_PROVIDER)
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            //output Log.d and Toast
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }
    };

    public void dropAmarker(String provider) {

        LatLng userLocation = null;

        if (locationManager != null) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            myLocation = locationManager.getLastKnownLocation(provider);
        }
        if(myLocation == null){
            //display message in Log.d and Toast
        } else {
            userLocation = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());

            //display message in Log.d and Toast
            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(userLocation, MY_LOC_ZOOM_FACTOR);

            //Add a shape for your marke
            Circle circle = mMap.addCircle(new CircleOptions()
                    .center(userLocation)
                    .radius(1)
                    .strokeColor(Color.RED)
                    .strokeWidth(2)
                    .fillColor(Color.RED));

            mMap.animateCamera(update);
        }
    }

}

