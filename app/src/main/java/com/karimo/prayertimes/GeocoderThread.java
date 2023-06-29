package com.karimo.prayertimes;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;

public class GeocoderThread implements Runnable {
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
    @Override
    public void run() {
        try
        {
            DecimalFormat decimalFormat = new DecimalFormat("#.00");
            latitude = Double.parseDouble(decimalFormat.format(latitude));
            longitude = Double.parseDouble(decimalFormat.format(longitude));
            address = geo.getFromLocation(latitude, longitude, 2);
            if(address.size() > 0)
            {
                city = address.get(0).getLocality();
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
    }
    public String getCity() {
        return city;
    }
}
