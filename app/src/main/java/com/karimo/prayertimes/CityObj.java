package com.karimo.prayertimes;

public class CityObj implements Comparable<CityObj>
{
	private String city;
	private String country;
	private double timeZone;
	private double latitude;
	private double longitude;
	public CityObj(String city, double timeZone, String country, double latitude, double longitude)
	{
		this.city = city;
		this.country = country;
		this.timeZone = timeZone;
		this.latitude = latitude;
		this.longitude = longitude;
	}
	public String city()
	{
		return city;
	}
	public String country()
	{
		return country;
	}
	public double timeZone()
	{
		return timeZone;
	}
	public void setTimeZone(double timeZone)
	{
		this.timeZone = timeZone;
	}
	public double latitude()
	{
		return latitude;
	}
	public double longitude()
	{
		return longitude;
	}
	@Override
	public int compareTo(CityObj another)
	{
		return city.compareTo(another.city());
	}
	@Override
	public String toString()
	{
		if(city.equals("") || city == null)
		{
			return "Current Location";
		}
		else
		{
			return city;
		}
	}
}
