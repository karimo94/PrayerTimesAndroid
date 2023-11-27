package com.karimo.prayertimes;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import android.content.Context;

/* PrayTimesInterface class
 * Implemented by Karim Oumghar
 * Used for interfacing between the main screen controls and the PrayTime class
 * As well as the preferences settings class
 * 
 * We will also be listening in this class for when to sound Adhan sound
 * For each salat time*/

public class PrayTimeInterface
{
	PrayTime p;
	PrefsInterface ps;
	ArrayList<String> prayerTimes;
	public PrayTimeInterface(Context ctx)//default
	{
		p = new PrayTime();
		ps = new PrefsInterface(ctx);
		Date now = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(now);
        int[] offsets = {0, 0, 0, 1, 0, 1, 0}; // {Fajr,Sunrise,Dhuhr,Asr,Sunset,Maghrib,Isha}
        p.tune(offsets);//takes an int[] array, again get these offsets from settings
        p.setTimeFormat(p.getTime12());
        p.setCalcMethod(p.getISNA());
        p.setAsrJuristic(p.getAsrJuristic());
        p.setAdjustHighLats(p.getAdjustHighLats());
        //grab the salat times
        //latitude, then longitude!
        if(ps.getCityObj() != null)
        {
        	prayerTimes = p.getPrayerTimes(cal,
        			getLat(),getLon(), getTimeZone());
        }
	}
	public CityObj getCityObj()
	{
		return ps.getCityObj();
	}
	public String getFajr()
	{
		if(ps.getCityObj() == null)
		{
			return "--:--";
		}
		return prayerTimes.get(0);
	}
	public String getSunrise()
	{
		if(ps.getCityObj() == null)
		{
			return "--:--";
		}
		return prayerTimes.get(1);
	}
	public String getDhuhr()
	{
		if(ps.getCityObj() == null)
		{
			return "--:--";
		}
		return prayerTimes.get(2);
	}
	public String getAsr()
	{
		if(ps.getCityObj() == null)
		{
			return "--:--";
		}
		return prayerTimes.get(3);
	}
	public String getMaghrib()
	{
		if(ps.getCityObj() == null)
		{
			return "--:--";
		}
		return prayerTimes.get(5);
	}
	public String getIsha()
	{
		if(ps.getCityObj() == null)
		{
			return "--:--";
		}
		return prayerTimes.get(6);
	}
	public String getCity()
	{
		if(ps.getCityObj() == null)
		{
			return "--:--";
		}
		return ps.getCityName();
	}
	public double getLat()
	{
		return ps.getLat();
	}
	public double getLon()
	{
		return ps.getLon();
	}
	public double getTimeZone()
	{
		return ps.getTimeZone();
	}
}
