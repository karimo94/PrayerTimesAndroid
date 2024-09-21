package com.karimo.prayertimes;

import android.content.Context;
import android.os.AsyncTask;
import com.google.gson.Gson;
import okhttp3.*;

import java.io.IOException;

public class MapsApiCallout extends AsyncTask<Void, Void, MapMarkers> {

    private double latitude;
    private double longitude;
    private final String API_KEY_PT1;
    private final String API_KEY_PT2;
    private final String API_KEY_PT3;
    private final String URL = "https://maps.googleapis.com";
    private final String ENDPOINT = "/maps/api/place/nearbysearch/json?";
    private String args;
    private Gson jsonResponse;
    private MapMarkers markers;
    public MapsApiCallout(double lat, double lon, Context ctx) {
        API_KEY_PT1 = ctx.getResources().getString(R.string.gcpKeyPt1);
        API_KEY_PT2 = ctx.getResources().getString(R.string.gcpKeyPt2);
        API_KEY_PT3 = ctx.getResources().getString(R.string.gcpKeyPt3);
        String API_KEY = API_KEY_PT1 + API_KEY_PT2 + API_KEY_PT3;
        latitude = lat;
        longitude = lon;
        jsonResponse = new Gson();
        args = "location=" + latitude + "%2C" + longitude + "&radius=15500&type=mosque&keyword=masjid&key=" + API_KEY;
    }


    @Override
    protected MapMarkers doInBackground(Void... voids) {
        String myUrl = URL + ENDPOINT + args;
        OkHttpClient myClient = new OkHttpClient();
        Request myRequest = new Request.Builder().url(myUrl).method("GET", null).build();
        ResponseBody myResponse;
        try {
            myResponse = myClient.newCall(myRequest).execute().body();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            markers = jsonResponse.fromJson(myResponse.string(), MapMarkers.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return markers;
    }
}