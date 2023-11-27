package com.karimo.prayertimes;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import androidx.annotation.NonNull;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;

public class GeocoderThread extends AsyncTask<Void, Void, String> {

    Geocoder geo;
    double latitude;
    double longitude;
    String city;
    List<Address> address;
    public GeocoderThread(double lat, double lon, Context ctx) {
        latitude = lat;
        longitude = lon;
        geo = new Geocoder(ctx, Locale.getDefault());
    }

    public String getCity() {
        return city;
    }

    @Override
    protected String doInBackground(Void... voids) {
        try
        {
            DecimalFormat decimalFormat = new DecimalFormat("#.00");
            latitude = Double.parseDouble(decimalFormat.format(latitude));
            longitude = Double.parseDouble(decimalFormat.format(longitude));
            if(android.os.Build.VERSION.SDK_INT >= 33) {
               geo.getFromLocation(latitude, longitude, 4, addresses -> {
                   address = addresses;
               });
            }
            else {
                address = geo.getFromLocation(latitude, longitude, 3);
            }
            if(address.size() > 0)
            {
                for(int i = 0; i < address.size(); i++) {
                    Address addr = address.get(i);
                    if(addr.getLocality() == null && addr.getSubAdminArea() == null) {
                        city = addr.getAdminArea();
                    }
                    else if(addr.getLocality() == null && addr.getSubAdminArea() != null) {
                        city = addr.getSubAdminArea();
                    }
                    else {
                        city = addr.getLocality();
                    }
                }
            }
        }
        catch (IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        catch (NullPointerException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return city;
    }
}