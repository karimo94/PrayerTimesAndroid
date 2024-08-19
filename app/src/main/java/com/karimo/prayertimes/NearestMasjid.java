package com.karimo.prayertimes;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.provider.Settings;
import android.view.*;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class NearestMasjid extends AppCompatActivity implements OnMapReadyCallback {

    SupportMapFragment mapFragment;
    private GoogleMap mMap;
    private Location myLocation;
    private LocationManager locationManager;
    private static final int REQ_LOCATION_PERMISSION = 100;
    private List<LatLng> masjids;
    private MapMarkers markers;
    String[] perms = {Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION};
    LatLng sydney = new LatLng(-34, 151);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearest_masjid);
        //ask for location permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, perms, REQ_LOCATION_PERMISSION);
        }

        myLocation = getLoc(); //needs null check
        getNearestMasjids();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.myMap);
        mapFragment.getMapAsync(this);

    }
    @SuppressLint("MissingPermission")
    private Location getLoc() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        String provider = LocationManager.GPS_PROVIDER;
        List<String> providers = locationManager.getProviders(true);
        myLocation = null;

        //if we need location services enabled in settings (or no internet)
        if (!locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            Toast.makeText(getApplicationContext(), "Enable network services!", Toast.LENGTH_LONG).show();
            startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), 10);
        }

        //if needing location services switched on
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, perms, REQ_LOCATION_PERMISSION);
        }
        //scan for most accurate location
        Location current = null;
        for(String s : providers) {
            //try this first
            current = locationManager.getLastKnownLocation(s);
            if(myLocation == null && current != null ||
                    (myLocation != null && current != null &&
                            myLocation.getAccuracy() < current.getAccuracy())) {
                myLocation = current;
            }
        }
        if(myLocation == null) {
            Toast.makeText(this,
                    "No recent location found, please check Settings.",
                    Toast.LENGTH_SHORT).show();
        }
        return myLocation;
    }
    /*
    * This is an API callout to Google Maps API to retrieve endpoints to display on the map
     */
    private void getNearestMasjids() {
        masjids = new ArrayList<>();
        MapsApiCallout callout =
                new MapsApiCallout(myLocation.getLatitude(), myLocation.getLongitude(), this);

        try {
            markers = callout.execute().get();
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        for(int i = 0; i < markers.results.size(); i++) {
            Result myResult = markers.results.get(i);
            LatLng temp = new LatLng(myResult.geometry.location.lat, myResult.geometry.location.lng);
            masjids.add(temp);
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
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney (as default) and move the camera
        LatLng actual = myLocation == null ?
                sydney : new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
        mMap.addMarker(
                new MarkerOptions()
                        .position(actual)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                        .title("Marker in your location")
        );
        for(int i = 0; i < masjids.size(); i++) {
            LatLng coords = masjids.get(i);
            mMap.addMarker(
                    new MarkerOptions()
                            .position(coords)
                            .title(markers.results.get(i).name)
            );
        }
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(actual, 10));
    }
}