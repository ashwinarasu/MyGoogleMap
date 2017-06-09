package com.example.arasua6707.mygooglemap;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
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

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private LocationManager locationManager;
    private boolean isGpsEnabled = false;
    private boolean isNetworkEnabled = false;
    private boolean canGetLocation = false;
    private boolean isTrackable = true;
    private static final long MIN_TIME_BW_UPDATES = 1000 * 15;
    private static final float MIN_DISTANCE_BW_UPDATES = 5.0f;
    private Location myLocation;
    private static final float MY_LOC_ZOOM_FACTOR = 17.0f;
    private boolean isTracked = false;
    private boolean dotColor = false;


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


    public void poiSearch(View v) {
        EditText locationSearch = (EditText) findViewById(R.id.searchBox);
        String location = locationSearch.getText().toString();
        List<android.location.Address> addressList = null;

        if (location != null || !location.equals("")) {
            Geocoder geocoder = new Geocoder(this);
            try {
                addressList = geocoder.getFromLocationName(location, 1);

            } catch (IOException e) {
                e.printStackTrace();
            }
            android.location.Address address = addressList.get(0);
            LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
            mMap.addMarker(new MarkerOptions().position(latLng).title("Search Result"));
            mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
        }
    }

    //calculates distance
    public static double distFrom(double lat1, double lng1, double lat2, double lng2) {
        double earthRadius = 3958.75; // miles (or 6371.0 kilometers)
        double dLat = Math.toRadians(lat2-lat1);
        double dLng = Math.toRadians(lng2-lng1);
        double sindLat = Math.sin(dLat / 2);
        double sindLng = Math.sin(dLng / 2);
        double a = Math.pow(sindLat, 2) + Math.pow(sindLng, 2)
                * Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2));
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double dist = earthRadius * c;
        return dist;
    }

    public void getLocation() {
        isTrackable = false;

        try {
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

            //get gps status
            isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if (isGpsEnabled == true) {
                Log.d("MyMaps", "getLocation: GPS is enabled");
            }

            //get network status
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if (isNetworkEnabled == true) {
                Log.d("MyMaps", "getLocation: Network is enabled");
            }

            if (!isGpsEnabled && !isNetworkEnabled) {
                Log.d("MyMaps", "getLocation: No Provider is enabled");
            } else {
                canGetLocation = true;
                if (isGpsEnabled == true) {
                    Log.d("MyMaps", "getLocation: GPS enabled & requesting location updates");
                    if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    Log.d("MyMaps", "Permissions granted");
                 /*   if (isTrackable = false) {
                        locationManager.removeUpdates(locationListenerGPS);
                        locationManager.removeUpdates(locationListenerNetwork);
                        return;
                    } */
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_BW_UPDATES, locationListenerGps);
                    // isTrackable = false;
                    Log.d("MyMaps", "getLocation: GPS update request is happening");
                    Toast.makeText(this, "Currently Using GPS", Toast.LENGTH_SHORT).show();
                    /*if (dotColor == true) {
                        dotColor = false;
                    } */
                }
                if (isNetworkEnabled == true) {
                    Log.d("MyMaps", "getLocation: Network enabled & requesting location updates");
                  /*  if (isTrackable = false) {
                        locationManager.removeUpdates(locationListenerGPS);
                        locationManager.removeUpdates(locationListenerNetwork);
                        return;
                    } */
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_BW_UPDATES, locationListenerNetwork);
                    //   isTrackable = false;
                    Log.d("MyMaps", "getLocation: Network update request in process");
                    Toast.makeText(this, "Currently Using Network", Toast.LENGTH_SHORT).show();
                    //dotColor = true;
                }
            }
        } catch (Exception e) {
            Log.d("MyMaps", "Caught an exception in getLocation");
            e.printStackTrace();
        }

    }

    android.location.LocationListener locationListenerGps = new android.location.LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            //output message in Log.d and Toast
            Log.d("My Maps", "GPS Location changed");
            Toast.makeText(MapsActivity.this, "GPS Location Changed", Toast.LENGTH_SHORT).show();
            //drop a marker on the map (create a method called dropAmarker)
            dropAmarker(LocationManager.GPS_PROVIDER);

            if (ActivityCompat.checkSelfPermission(MapsActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapsActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            locationManager.removeUpdates(locationListenerNetwork);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            //setup a switch statement on status
            //case: LocationProvider.AVAILABLE --> output message
            //case: LocationProvider.OUT_OF_SERVICE --> request updates from NETWORK_PROVIDER
            //case: Location.Provider.TEMPORARILY_UNAVAILABLE --> request updates from NETWORK_PROVIDER
            switch (status) {
                case LocationProvider.AVAILABLE:
                    Log.d("MyMaps", "location provider available");
                    break;
                case LocationProvider.OUT_OF_SERVICE:
                    if (ActivityCompat.checkSelfPermission(MapsActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapsActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_BW_UPDATES, locationListenerNetwork);

                    break;
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_BW_UPDATES, locationListenerNetwork);
                    break;
                default:
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_BW_UPDATES, locationListenerNetwork);
                    break;
            }
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }
    };


    public void trackMe(View view) {

        if (isTrackable == true) {
            Toast.makeText(MapsActivity.this, "getting location", Toast.LENGTH_SHORT).show();
            getLocation();
        } else if (isTrackable == false) {
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
            locationManager.removeUpdates(locationListenerNetwork);
            locationManager.removeUpdates(locationListenerGps);
            Toast.makeText(MapsActivity.this, "Stopped Tracking", Toast.LENGTH_SHORT).show();
            isTrackable = true;
        }
    }

    android.location.LocationListener locationListenerNetwork = new android.location.LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            //output message in Log.d and Toast
            Log.d("MyMaps", "Network location changed");
            Toast.makeText(MapsActivity.this, "Network location changed", Toast.LENGTH_SHORT).show();
            //drop a marker
            dropAmarker(LocationManager.NETWORK_PROVIDER);
            //relaunch request for network location updates (requestLocationUpdates(NETWORK_PROVIDER)
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            //output Log.d and Toast
            Log.d("MyMaps", "Network onStatusChanged called");
            Toast.makeText(MapsActivity.this, "Network onStatusChanged has been called", Toast.LENGTH_SHORT).show();


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

        if (myLocation == null) {
            //display a log d message and/or toast
            Log.d("MyMaps", "dropMarker: location is null");

        } else {
            userLocation = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());

            //display log d message and/or toast of coordinates
            Toast.makeText(MapsActivity.this, "" + myLocation.getLatitude() + ", " + myLocation.getLongitude(), Toast.LENGTH_SHORT).show();

            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(userLocation, MY_LOC_ZOOM_FACTOR);

            Circle myCircle;

            //add a shape for a marker (don't use standard teardrop marker)
            //dotColor determines whether device is using network or GPS and changes the color accordingly

            if (dotColor == true) {
                myCircle = mMap.addCircle(new CircleOptions().center(userLocation).radius(1).strokeColor(Color.BLACK).strokeWidth(2).fillColor(Color.BLACK));
                Log.d("MyMaps", "black dot laid for GPS");
            } else if (dotColor == false) {
                myCircle = mMap.addCircle(new CircleOptions().center(userLocation).radius(1).strokeColor(Color.YELLOW).strokeWidth(2).fillColor(Color.YELLOW));
                Log.d("MyMaps", "yellow dot laid for network");
            }

            mMap.animateCamera(update);
        }
    }
    public void clearAllMarkers(View v){
        mMap.clear();
    }

}

