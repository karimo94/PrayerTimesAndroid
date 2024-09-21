package com.karimo.prayertimes;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import android.R.integer;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

/* PrefsInterfaces class
 * Implemented by Karim Oumghar
 * Used to get information based from settings saved*/
@SuppressWarnings("unused")
public class PrefsInterface
{
	private SharedPreferences sPreferences;
	private SharedPreferences calcPreferences;
	private SharedPreferences asrPreferences;
	private CityObj myCity;
	private Gson gson;
	private int calcMethod = 2;
	private int asrMethod = 0;
	private boolean adhanEnabled;
	private boolean notifsEnabled;
	public PrefsInterface()
	{

	}
	public PrefsInterface(Context context)
	{
		gson = new Gson();
		sPreferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
		String json = sPreferences.getString("myJsonCity", "");
		if(!json.isEmpty())
		{
			Type t = new TypeToken<CityObj>(){}.getType();
			myCity = gson.fromJson(json, t);
			//System.out.println(myCity.city());
		}
		else
		{
			System.out.println("Error getting city from settings.");
		}
		calcPreferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
		String json2 = sPreferences.getString("myCalcMethod", "");
		if(!json2.isEmpty())
		{
			Type t = new TypeToken<Integer>(){}.getType();
			calcMethod = gson.fromJson(json2, t);
		}
		asrPreferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
		String json3 = sPreferences.getString("myAsrMethod", "");
		if(!json3.isEmpty())
		{
			Type t = new TypeToken<Integer>(){}.getType();
			asrMethod = gson.fromJson(json3, t);
		}
	}
	public CityObj getCityObj()
	{
		return myCity;
	}
	public double getLat()
	{
		return myCity.latitude(); //47.80;//retrieve from settings
	}
	public double getLon()
	{
		return myCity.longitude(); //-122.25;//retrive from settings
	}
	public String getCityName()
	{
		if(myCity == null)
		{
			return "";
		}
		return myCity.city();//"Bothell"; //retrieve from settings
	}
	public int[] getOffsets()
	{
		return new int[]{0, 0, 0, 1, 0, 1, 0};//retrive from settings
	}
	public double getTimeZone()
	{
		return myCity.timeZone();
	}
	public int getCalcMethod()
	{
		return calcMethod; //retrive from settings
	}
	public int getAsrMethod()
	{
		return asrMethod; //retrive from settings
	}
	public boolean adhanEnabled()
	{
		return adhanEnabled; //retrieve from settings
	}
	public boolean notificationsEnabled()
	{
		return notifsEnabled;
	}
}