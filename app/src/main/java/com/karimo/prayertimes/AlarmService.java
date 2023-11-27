package com.karimo.prayertimes;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Calendar;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.JobIntentService;

public class AlarmService extends JobIntentService
{
	AlarmManager almgr1;
	AlarmManager almgr2;
	AlarmManager almgr3;
	AlarmManager almgr4;
	AlarmManager almgr5;
	AlarmReceiver acvr;
	Intent alarmIntent;
	Intent alarmIntent2;
	Intent alarmIntent3;
	Intent alarmIntent4;
	Intent alarmIntent5;
	PendingIntent pendingIntent;
	PendingIntent pendingIntent2;
	PendingIntent pendingIntent3;
	PendingIntent pendingIntent4;
	PendingIntent pendingIntent5;
	Calendar []alarmTimings = new Calendar[5];
	Gson gson;
	private static final int JOB_ID = 9100;


	@Override
	public void onCreate() {
		super.onCreate();
		alarmIntent = new Intent(AlarmService.this, AlarmReceiver.class);
		alarmIntent.putExtra("salatIntent", 0);
		alarmIntent2 = new Intent(AlarmService.this, AlarmReceiver.class);
		alarmIntent2.putExtra("salatIntent", 1);
		alarmIntent3 = new Intent(AlarmService.this, AlarmReceiver.class);
		alarmIntent3.putExtra("salatIntent", 2);
		alarmIntent4 = new Intent(AlarmService.this, AlarmReceiver.class);
		alarmIntent4.putExtra("salatIntent", 3);
		alarmIntent5 = new Intent(AlarmService.this, AlarmReceiver.class);
		alarmIntent5.putExtra("salatIntent", 4);
	}
	public static void enqueueWork(Context context, Intent work) {
		//this is a public setter that calls the actual enqueueWork from implementing JobIntentSvc
		enqueueWork(context, AlarmService.class, JOB_ID, work);
	}
	protected void onHandleIntent(Intent intent)
	{
		Calendar []timings = new Calendar[5];
		Object[] tempObjects = new Object[5];
		try
		{
			if(intent != null)
			{
				tempObjects = (Calendar[]) intent.getSerializableExtra("alarmTimesArray");
				timings = Arrays.copyOf(tempObjects, 5,Calendar[].class);
				saveTimings(timings);
			}
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
		if(intent == null)
		{
			SharedPreferences spEditor = PreferenceManager.getDefaultSharedPreferences(this);
			Gson gson = new Gson();
			try
			{
				String json = spEditor.getString("mySavedTimingsArr", "");
				if(!json.isEmpty())
				{
					Type t = new TypeToken<Calendar[]>(){}.getType();
					timings = gson.fromJson(json, t);
				}
				else
				{
					System.out.println("Error getting calendar timings array from disk.");
				}
			} 
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		//initialize the alarm manager, alarm receiver and pending intent and alarmIntent
		acvr = new AlarmReceiver();
		almgr1 = (AlarmManager)this.getSystemService(ALARM_SERVICE);
		almgr2 = (AlarmManager)this.getSystemService(ALARM_SERVICE);
		almgr3 = (AlarmManager)this.getSystemService(ALARM_SERVICE);
		almgr4 = (AlarmManager)this.getSystemService(ALARM_SERVICE);
		almgr5 = (AlarmManager)this.getSystemService(ALARM_SERVICE);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			pendingIntent = PendingIntent.getBroadcast(AlarmService.this, 0, alarmIntent, PendingIntent.FLAG_IMMUTABLE);
			pendingIntent2 = PendingIntent.getBroadcast(AlarmService.this, 1, alarmIntent2, PendingIntent.FLAG_IMMUTABLE);
			pendingIntent3 = PendingIntent.getBroadcast(AlarmService.this, 2, alarmIntent3, PendingIntent.FLAG_IMMUTABLE);
			pendingIntent4 = PendingIntent.getBroadcast(AlarmService.this, 3, alarmIntent4, PendingIntent.FLAG_IMMUTABLE);
			pendingIntent5 = PendingIntent.getBroadcast(AlarmService.this, 4, alarmIntent5, PendingIntent.FLAG_IMMUTABLE);
		}
		else {
			pendingIntent = PendingIntent.getBroadcast(AlarmService.this, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
			pendingIntent2 = PendingIntent.getBroadcast(AlarmService.this, 1, alarmIntent2, PendingIntent.FLAG_UPDATE_CURRENT);
			pendingIntent3 = PendingIntent.getBroadcast(AlarmService.this, 2, alarmIntent3, PendingIntent.FLAG_UPDATE_CURRENT);
			pendingIntent4 = PendingIntent.getBroadcast(AlarmService.this, 3, alarmIntent4, PendingIntent.FLAG_UPDATE_CURRENT);
			pendingIntent5 = PendingIntent.getBroadcast(AlarmService.this, 4, alarmIntent5, PendingIntent.FLAG_UPDATE_CURRENT);
		}
		//set the alarmTimings array
		setTimes(timings);
	}
	private void setTimes(Calendar[] times)
	{
		for(int i = 0; i < 5; i++)
		{
			alarmTimings[i] = times[i];
		}
	}
	private void saveTimings(Calendar [] arr)
	{
		SharedPreferences.Editor sp = PreferenceManager.getDefaultSharedPreferences(this).edit();
		Gson gson = new Gson();
		try
		{
			String arrString = gson.toJson(arr);
			sp.putString("mySavedTimingsArr", arrString);
			sp.commit();
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}


	@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		super.onStartCommand(intent, flags, startId);
		//grab the array passed in
		
		onHandleIntent(intent);
		setAlarms();
		
		return START_STICKY;
	}

	@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
	@Override
	protected void onHandleWork(@NonNull Intent intent) {
		//for implementing JobIntentService
		onHandleIntent(intent);
		setAlarms();
	}

	@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
	private void setAlarms()
	{
		//set the alarms here
		if(alarmTimings[0] != null && alarmTimings[0].getTimeInMillis() >= Calendar.getInstance().getTimeInMillis())
		{
			almgr1.setExact(AlarmManager.RTC_WAKEUP,alarmTimings[0].getTimeInMillis(), pendingIntent);
			//almgr1.setRepeating(AlarmManager.RTC_WAKEUP, alarmTimings[0].getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
		}
		if(alarmTimings[1] != null && alarmTimings[1].getTimeInMillis() >= Calendar.getInstance().getTimeInMillis())
		{
			almgr2.setExact(AlarmManager.RTC_WAKEUP, alarmTimings[1].getTimeInMillis(), pendingIntent2);
			//almgr2.setRepeating(AlarmManager.RTC_WAKEUP, alarmTimings[1].getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent2);
		}
		if(alarmTimings[2] != null && alarmTimings[2].getTimeInMillis() >= Calendar.getInstance().getTimeInMillis())
		{
			almgr3.setExact(AlarmManager.RTC_WAKEUP, alarmTimings[2].getTimeInMillis(), pendingIntent3);
			//almgr3.setRepeating(AlarmManager.RTC_WAKEUP, alarmTimings[2].getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent3);
		}
		if(alarmTimings[3] != null && alarmTimings[3].getTimeInMillis() >= Calendar.getInstance().getTimeInMillis())
		{
			almgr4.setExact(AlarmManager.RTC_WAKEUP, alarmTimings[3].getTimeInMillis(), pendingIntent4);
			//almgr4.setRepeating(AlarmManager.RTC_WAKEUP, alarmTimings[3].getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent4);
		}
		if(alarmTimings[4] != null && alarmTimings[4].getTimeInMillis() >= Calendar.getInstance().getTimeInMillis())
		{
			almgr5.setExact(AlarmManager.RTC_WAKEUP, alarmTimings[4].getTimeInMillis(), pendingIntent5);
			//almgr5.setRepeating(AlarmManager.RTC_WAKEUP, alarmTimings[4].getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent5);
		}
	}
}
