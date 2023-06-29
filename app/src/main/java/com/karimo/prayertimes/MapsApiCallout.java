package com.karimo.prayertimes;

import android.content.Context;
import android.os.AsyncTask;
import com.google.gson.Gson;
import okhttp3.*;

import java.io.IOException;

public class MapsApiCallout extends AsyncTask<Void, Void, MapMarkers> {

    private double latitude;
    private double longitude;
    private final String API_KEY;
    private final String URL = "https://maps.googleapis.com";
    private final String ENDPOINT = "/maps/api/place/nearbysearch/json?";
    private String args;
    private Gson jsonResponse;
    private MapMarkers markers;
    public MapsApiCallout(double lat, double lon, Context ctx) {
        API_KEY = ctx.getResources().getString(R.string.apiKey);
        latitude = lat;
        longitude = lon;
        jsonResponse = new Gson();
        args = "location=" + latitude + "%2C" + longitude + "&radius=7500&type=mosuq&keyword=masjid&key=" + API_KEY;
    }


    @Override
    protected MapMarkers doInBackground(Void... voids) {
        String myUrl = URL + ENDPOINT + args;
        OkHttpClient myClient = new OkHttpClient();
        MediaType mediaType = MediaType.parse("text/plain");
        RequestBody requestBody = RequestBody.create(mediaType, "");
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
//https://developers.google.com/maps/documentation/places/web-service/search-nearby?hl=en#maps_http_places_nearbysearch-java
