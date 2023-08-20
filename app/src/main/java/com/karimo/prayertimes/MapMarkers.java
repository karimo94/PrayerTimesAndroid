package com.karimo.prayertimes;

import java.util.ArrayList;

class Geometry{
    class Location{
        public double lat;
        public double lng;
    }
    public Location location;
    public Viewport viewport;
}

class Northeast{
    public double lat;
    public double lng;
}

class OpeningHours{
    public boolean open_now;
}

class Photo{
    public int height;
    public ArrayList<String> html_attributions;
    public String photo_reference;
    public int width;
}

class PlusCode{
    public String compound_code;
    public String global_code;
}

class Result{
    public String business_status;
    public Geometry geometry;
    public String icon;
    public String icon_background_color;
    public String icon_mask_base_uri;
    public String name;
    public OpeningHours opening_hours;
    public ArrayList<Photo> photos;
    public String place_id;
    public PlusCode plus_code;
    public int price_level;
    public double rating;
    public String reference;
    public String scope;
    public ArrayList<String> types;
    public int user_ratings_total;
    public String vicinity;
}

public class MapMarkers{
    public ArrayList<Object> html_attributions;
    public ArrayList<Result> results;
    public String status;
}

class Southwest{
    public double lat;
    public double lng;
}

class Viewport{
    public Northeast northeast;
    public Southwest southwest;
}


