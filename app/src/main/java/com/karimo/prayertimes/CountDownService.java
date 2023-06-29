package com.karimo.prayertimes;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.JobIntentService;

public class CountDownService extends JobIntentService
{
	CountDownTime ctx;
	Date fajrTime;
	Date dhuhrTime;
	Date asrTime;
	Date maghribTime;
	Date ishaTime;
	Date nextPrayerTime;
	
	SimpleDateFormat sdfDateFormat = new SimpleDateFormat("hh:mm a");
	
	String prayerName;
	
	Date[] times = null;
	Intent nextTextIntent;
	SharedPreferences spManager;

	private static final int JOB_ID = 9300;

	public void setTimes(Date[] arr)
	{
		fajrTime = arr[0];
		dhuhrTime = arr[1];
		asrTime = arr[2];
		maghribTime = arr[3];
		ishaTime = arr[4];
	}
	protected void onHandleIntent(Intent intent)
	{
		Object[] tempObjects = null;
		try
		{
			tempObjects = (Object[]) intent.getSerializableExtra("timingsArray");
			times = Arrays.copyOf(tempObjects, 5, Date[].class);
		} 
		catch (Exception e)
		{
			e.printStackTrace();
			return;
		}

		setTimes(times);
	}
	@Override
	public void onCreate()
	{
		super.onCreate();
	}
	@Override
	public void onDestroy()
	{
		super.onDestroy();
	}

	@Override
	protected void onHandleWork(@NonNull Intent intent) {
		//this is from implementing JobIntentService
		onHandleIntent(intent);

		Handler handler = new Handler(Looper.getMainLooper());
		handler.post(new Runnable() {

			@Override
			public void run() {
				getNextPrayer();
			}
		});

	}
	public static void enqueueWork(Context context, Intent work) {
		enqueueWork(context, CountDownService.class, JOB_ID, work);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		super.onStartCommand(intent, flags, startId);
		
		onHandleIntent(intent);
		getNextPrayer();
		
		return START_STICKY;
	}

	public void startTicking()
	{
		Date nowDate = Calendar.getInstance().getTime();
		long difference = 0;
	    if(nextPrayerTime != null)
	    {
	    	difference = nextPrayerTime.getTime() - nowDate.getTime();
	    }
	    if(nextPrayerTime == null)
	    {
	    	onDestroy();
	    }
	    if(difference > 0)
	    {
	    	ctx = new CountDownTime(difference, 1000);
	    	ctx.start();
	    }
	}
	public void getNextPrayer()
	{
		/*if(fajrTime == null || dhuhrTime == null || asrTime == null || maghribTime == null || ishaTime == null)
		{
			return;
		}*/
		Date nowDate = Calendar.getInstance().getTime();
		
		if(nowDate.before(fajrTime))
	    {
	    	nextPrayerTime = fajrTime;
	    	prayerName = "Fajr";
	    }
		if(nowDate.before(dhuhrTime) && nowDate.after(fajrTime))
	    {
	    	nextPrayerTime = dhuhrTime;
	    	prayerName = "Dhuhr";
	    }
	    if(nowDate.before(asrTime) && nowDate.after(dhuhrTime))
	    {
	    	nextPrayerTime = asrTime;
	    	prayerName = "Asr";
	    }
	    if(nowDate.before(maghribTime)&& nowDate.after(asrTime))
	    {
	    	nextPrayerTime = maghribTime;
	    	prayerName = "Maghrib";
	    }
	    if(nowDate.before(ishaTime) && nowDate.after(maghribTime))
	    {
	    	nextPrayerTime = ishaTime;
	    	prayerName = "Isha";
	    }
	    if(nowDate.after(ishaTime))
	    {
	    	prayerName = "";
	    	nextPrayerTime = null;
	    	stopSelf();
	    }

		startTicking(); //use a handler for this
	}
	public class CountDownTime extends CountDownTimer
	{

		public CountDownTime(long millisInFuture, long countDownInterval)
		{
			super(millisInFuture, countDownInterval);
		}

		@Override
		public void onTick(long millisUntilFinished)
		{
			String textString = ("Next: " + prayerName + " in " + String.format("%02d:%02d:%02d", (millisUntilFinished / (1000 * 60 * 60)),
	                ((millisUntilFinished / (1000 * 60)) % 60), ((millisUntilFinished / 1000) % 60)));
			nextTextIntent = new Intent("update_next_text");
			nextTextIntent.putExtra("countdownTime", textString);
			getApplicationContext().sendBroadcast(nextTextIntent);//LocalBroadcastManager.getInstance(CountDownService.this).
		}

		@Override
		public void onFinish()
		{
			getNextPrayer();
		}
	}
}
