package com.karimo.prayertimes;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.JobIntentService;

public class TimeUpdaterService extends JobIntentService
{
	PrayTimeInterface pi;
	Date fajrTime;
	Date dhuhrTime;
	Date asrTime;
	Date maghribTime;
	Date ishaTime;
	Date nextPrayerTime;
	SimpleDateFormat dtFormat = new SimpleDateFormat("hh:mm a", Locale.US);
	Calendar tOne = Calendar.getInstance();
	Calendar tTwo = Calendar.getInstance();
	Calendar tThree = Calendar.getInstance();
	Calendar tFour = Calendar.getInstance();
	Calendar tFive = Calendar.getInstance();
	Date[] timings;
	Intent alarmServiceIntent;
	Calendar[] alarmTimesArray = new Calendar[5];
	public static final int JOB_ID = 9000;
	@Override
	public void onCreate()
	{
		super.onCreate();
		alarmServiceIntent = new Intent(TimeUpdaterService.this, AlarmService.class);

	}

	@Override
	protected void onHandleWork(@NonNull Intent intent) {
		//from implementing JobIntentService, if this doesn't work, consider deleting
		//simply carry from onStartCommand
		setTimes();
		setDailyAlarms();
	}
	public static void enqueueWork(Context context, Intent work) {
		//this is a public setter that calls the actual enqueueWork from implementing JobIntentSvc
		enqueueWork(context, TimeUpdaterService.class, JOB_ID, work);
	}

	@SuppressWarnings("deprecation")
	private void setTimes()
	{
		pi = new PrayTimeInterface(getApplicationContext());
		Date cd = Calendar.getInstance().getTime();
		if(pi.getCityObj() == null)
		{
			return;
		}
		try
		{
			fajrTime = dtFormat.parse(pi.getFajr());
			fajrTime.setYear(cd.getYear());
			fajrTime.setMonth(cd.getMonth());
			fajrTime.setDate(cd.getDate());
			dhuhrTime = dtFormat.parse(pi.getDhuhr());
			dhuhrTime.setYear(cd.getYear());
			dhuhrTime.setMonth(cd.getMonth());
			dhuhrTime.setDate(cd.getDate());
			asrTime = dtFormat.parse(pi.getAsr());
			asrTime.setYear(cd.getYear());
			asrTime.setMonth(cd.getMonth());
			asrTime.setDate(cd.getDate());
			maghribTime = dtFormat.parse(pi.getMaghrib());
			maghribTime.setYear(cd.getYear());
			maghribTime.setMonth(cd.getMonth());
			maghribTime.setDate(cd.getDate());
			ishaTime = dtFormat.parse(pi.getIsha());
			ishaTime.setYear(cd.getYear());
			ishaTime.setMonth(cd.getMonth());
			ishaTime.setDate(cd.getDate());
			
			//finally set the calendar times
			tOne.setTime(fajrTime);
			tTwo.setTime(dhuhrTime);
			tThree.setTime(asrTime);
			tFour.setTime(maghribTime);
			tFive.setTime(ishaTime);
		} 
		catch (ParseException e)
		{
			e.printStackTrace();
		}
		timings = new Date[]{fajrTime, dhuhrTime, asrTime,maghribTime,ishaTime};
		return;
	}
	private void setDailyAlarms()
	{
		
		alarmTimesArray[0] = tOne;
		alarmTimesArray[1] = tTwo;
		alarmTimesArray[2] = tThree;
		alarmTimesArray[3] = tFour;
		alarmTimesArray[4] = tFive;
		/*Date testerDate, testerDate2, testerDate3, testerDate4, testerDate5, cd = Calendar.getInstance().getTime();
		Calendar testOne = Calendar.getInstance(), testTwo = Calendar.getInstance(), 
				testThree = Calendar.getInstance(), testFour = Calendar.getInstance(), testFive = Calendar.getInstance();*/
		try
		{
			/*testerDate = dtFormat.parse("10:28 pm");
			testerDate.setYear(cd.getYear());
			testerDate.setMonth(cd.getMonth());
			testerDate.setDate(cd.getDate());
			testOne.setTime(testerDate);
			testerDate2 = dtFormat.parse("10:30 pm");
			testerDate2.setYear(cd.getYear());
			testerDate2.setMonth(cd.getMonth());
			testerDate2.setDate(cd.getDate());
			testTwo.setTime(testerDate2);
			testerDate3 = dtFormat.parse("10:37 pm");
			testerDate3.setYear(cd.getYear());
			testerDate3.setMonth(cd.getMonth());
			testerDate3.setDate(cd.getDate());
			testThree.setTime(testerDate3);
			
			testerDate4 = dtFormat.parse("10:40 pm");
			testerDate4.setYear(cd.getYear());
			testerDate4.setMonth(cd.getMonth());
			testerDate4.setDate(cd.getDate());
			testFour.setTime(testerDate4);
			testerDate5 = dtFormat.parse("10:45 pm");
			testerDate5.setYear(cd.getYear());
			testerDate5.setMonth(cd.getMonth());
			testerDate5.setDate(cd.getDate());
			testFive.setTime(testerDate5);
			alarmTimesArray[0] = testOne;
			alarmTimesArray[1] = testTwo;
			alarmTimesArray[2] = testThree;
			alarmTimesArray[3] = testFour;
			alarmTimesArray[4] = testFive;*/
			
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		//start a service!
		
		//start the alarm service, pass in the salat times
		
		alarmServiceIntent.putExtra("alarmTimesArray", alarmTimesArray);
		AlarmService.enqueueWork(getApplicationContext(), alarmServiceIntent);//startService(alarmServiceIntent);
	}
	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		super.onStartCommand(intent, flags, startId);
		setTimes();
		setDailyAlarms();

		return START_STICKY;
	}


}
