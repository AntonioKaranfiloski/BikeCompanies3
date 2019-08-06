package com.progy.elan.bikecompanies;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;


import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ArrayList<LatLng> Points = new ArrayList<>();
    private String title="",JSONResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        JSONResult= loadJSONFromAsset();
        try {
            JSONObject json = new JSONObject(JSONResult);
            JSONArray points = json.getJSONArray("points");
            title = json.getString("title");
            for (int i =0;i<points.length();i++)
            {
                JSONObject point = points.getJSONObject(i);
                String lat = point.getString("latitude");
                String lon = point.getString("longitude");
                LatLng latLng = new LatLng(Double.parseDouble(lat),Double.parseDouble(lon));
                Points.add(latLng);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }


        LatLng StartPoint = new LatLng(Points.get(0).latitude,Points.get(0).longitude);
        LatLng EndPoint = new LatLng(Points.get(Points.size()-1).latitude,Points.get(Points.size()-1).longitude);
        mMap.addMarker(new MarkerOptions().position(StartPoint).title(title+"-Start"));
        mMap.addMarker(new MarkerOptions().position(EndPoint).title(title+"-End"));
        if (Points.size()>0)
        {
            PolylineOptions options= new PolylineOptions().addAll(Points).color(Color.BLUE).width(5);
            mMap.addPolyline(options);
            mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                @Override
                public void onMapLoaded() {
                    LatLngBounds.Builder builder = new LatLngBounds.Builder();
                    for (LatLng latLng:Points)
                    {
                        builder.include(latLng);
                    }
                    LatLngBounds bounds = builder.build();

                    int padding = 20;
                    CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                    mMap.moveCamera(cu);
                }
            });
        }



    }

    public String loadJSONFromAsset() {
        String json = null;
        try {
            InputStream is = getAssets().open("trip.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }
}