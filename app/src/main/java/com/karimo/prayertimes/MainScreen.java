package com.karimo.prayertimes;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;


@SuppressWarnings("unused")
public class MainScreen extends Activity
{

	//GLOBAL VARIABLES********************************************
	public PrayTimeInterface pi;
	//textviews***************************************************
	TextView city;
	TextView fajr;
	TextView sunrise;
	TextView dhuhr;
	TextView asr;
	TextView maghrib;
	TextView isha;
	
	TextView dateDesc;
	TextView fajrDesc;
	TextView sunriseDesc;
	TextView dhuhrDesc;
	TextView asrDesc;
	TextView maghribDesc;
	TextView ishaDesc;
	TextView nextTextView;
	//************************************************************
	
	//Dates, Calendars, Preferences, Alarms, Intents, Arrays, booleans*******
	Typeface tf; 
	SimpleDateFormat dtFormat;
	Date fajrTime;
	Date dhuhrTime;
	Date asrTime;
	Date maghribTime;
	Date ishaTime;
	Date nextPrayerTime;
	Date testerDate;
	AlertDialog.Builder aboutWindow;
	SharedPreferences sp, sp2, sp3;
	public static Context appContext;
	Intent countdownServiceIntent;
	
	Intent timeUpdaterServiceIntent;
	Intent updateAlarmIntent;
	Intent midnightAlarmIntent;
	PendingIntent pendingIntent;
	Date[] timings = new Date[5];
	
	AlarmManager am;
	
	boolean adhanEnabled;
	boolean notifsEnabled;
	public static boolean isMainScreenActive = false;
	Calendar tOne = Calendar.getInstance();
	Calendar tTwo = Calendar.getInstance();
	Calendar tThree = Calendar.getInstance();
	Calendar tFour = Calendar.getInstance();
	Calendar tFive = Calendar.getInstance();
	//************************************************************
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_main_screen);
		
		//initialize a new prayer times object
		pi = new PrayTimeInterface(this.getApplicationContext());
		
		//initialize all the text views
		initTextViews();
		
		//define the timing format
		dtFormat = new SimpleDateFormat("hh:mm a", Locale.US);
		
		sp = getSharedPreferences("com.karimo.prayertimes", MODE_PRIVATE);
		sp2 = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		sp3 = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		
		
		//update ui alarm intent
		updateAlarmIntent = new Intent(MainScreen.this, TimeUpdaterReceiver.class);
		//countdown service intent
		countdownServiceIntent = new Intent(MainScreen.this, CountDownService.class);

		//set typeface
		tf = Typeface.createFromAsset(getAssets(), "SFUI.otf");
		setTypefaces(); //set the typefaces for all the text
		
		//display hijri calendar date
		TextView hdt = (TextView) findViewById(R.id.digitalClock1);
		hdt.setText(HijriCalendar.writeIslamicDate());
		hdt.setTypeface(tf);
		
		//display gregorian date
		displayDate();

        //set each prayer time to the label
        setTextToTimes();
        
		//set the daily prayer times to objects
		setTimes();
		
		//register a receiver to update the "next prayer text"
		this.registerReceiver(bcsr, new IntentFilter("update_next_text"));
		
		/*start the countdown timer service, should we have our timings in order, 
		 * and there is something to countdown to*/
		if(pi.getCityObj() != null && Calendar.getInstance().getTimeInMillis() < tFive.getTimeInMillis())
		{
			//start the notifications/adhan service, pass the prayer times
			countdownServiceIntent.putExtra("timingsArray", timings);
			CountDownService.enqueueWork(this.getApplicationContext(), countdownServiceIntent);//startService(countdownServiceIntent);
		}
		
		//get if adhan and notifications are enabled, and if the alarm service is running
		adhanEnabled = sp2.getBoolean("myAdhanOpt", false);
		notifsEnabled = sp3.getBoolean("myNotifsOpt", false);

		//check if there's anything needed to sound the adhan for
		boolean doneForTheDay = false;
		if(pi.getCityObj() != null)//making sure to account for first run
		{
			doneForTheDay = Calendar.getInstance().getTimeInMillis() >= tFive.getTimeInMillis();
		}
		
		/*if the service isnt running already, 
		and either notifications or adhan are enabled, 
		and we're not done for the day, launch the service*/
		if((adhanEnabled || notifsEnabled && !doneForTheDay))
		{	
			timeUpdaterServiceIntent = new Intent(MainScreen.this, TimeUpdaterService.class);
			TimeUpdaterService.enqueueWork(this.getApplicationContext(), timeUpdaterServiceIntent);//startService(timeUpdaterServiceIntent);

			//also set midnight alarm intent here
			midnightAlarmIntent = new Intent(MainScreen.this, MidnightAlarmService.class);
			MidnightAlarmService.enqueueWork(this.getApplicationContext(), midnightAlarmIntent);
		}
		//initialize the alarm manager for update ui for a new day
		am = (AlarmManager)getSystemService(ALARM_SERVICE);
		//schedule an alarm to update the application data at midnight
		if(pi.getCityObj() != null)
		{
			scheduleUpdate();
		}
		AppRater.app_launched(this);
		isMainScreenActive = true;
	}
	@Override
	public void onStart()
	{
		super.onStart();
		appContext = this.getApplicationContext();
	}
	static public Context getMainContext()
	{
		return appContext;
	}
	@Override
	public void onStop()
	{
		super.onStop();
		unregisterReceiver(bcsr);
		isMainScreenActive = false;
	}
	@Override
	protected void onResume()
	{
		super.onResume();
		
		if(sp.getBoolean("firstrun", true))
		{
			//make an alert dialog telling the user to set the city in settings
			AlertDialog.Builder firstRunDialog = new AlertDialog.Builder(this);
			firstRunDialog.setTitle("Before you proceed...");
			TextView alertTextView = new TextView(this);
			alertTextView.setText("Please set the city configuration in settings\nfor correct salat timing display.");
			firstRunDialog.setView(alertTextView);
			firstRunDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					Intent settingsScreenIntent = new Intent(MainScreen.this, SettingsActivity.class);
					startActivity(settingsScreenIntent);
				}
			});
			firstRunDialog.show();
			sp.edit().putBoolean("firstrun", false).commit();
		}
		
		//should we get back from settings, redraw the main screen 
		this.onCreate(null);
		
		//re-register the bcsr receiver
		LocalBroadcastManager.getInstance(this).registerReceiver(bcsr,
				new IntentFilter("com.android.INTENT_ACTION_TO_CLOSE_ACTIVITY"));
	}
	@Override
	public void onDestroy()
	{
		super.onDestroy();
		if(pi.getCityObj() != null)
		{
			stopService(countdownServiceIntent);
		}
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main_screen, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			Intent settingsScreen = new Intent(MainScreen.this, SettingsActivity.class);
			startActivity(settingsScreen);
			return true;
		}
		if(id == R.id.action_infohelp) {
			Intent infoScreenIntent = new Intent(MainScreen.this, InfoActivity.class);
			startActivity(infoScreenIntent);
		}
		if(id == R.id.about) {
			showAboutDialog();
			return true;
		}
		
		return super.onOptionsItemSelected(item);
	}
	public void goToSettings(View v)
	{
		Intent settingsScreen = new Intent(MainScreen.this, SettingsActivity.class);
		startActivity(settingsScreen);
	}
	public void goToQibla(View v)
	{
		Intent qiblaScreen = new Intent(MainScreen.this, QiblaActivity.class);
		startActivity(qiblaScreen);
	}
	public void goToQuranPlayer(View v)
	{
		Intent quranPlayerIntent = new Intent(MainScreen.this, QuranPlayerActivity.class);
		startActivity(quranPlayerIntent);
	}
	public void goToDhikr(View v) {
		Intent dhikrScreenIntent = new Intent(MainScreen.this, DhikrActivity.class);
		startActivity(dhikrScreenIntent);
	}
	public void goToNearestMap(View v) {
		Intent nearestMasjid = new Intent(MainScreen.this, NearestMasjid.class);
		startActivity(nearestMasjid);
	}
	public void goToQuran(View v) {
		Intent quranActivity = new Intent(MainScreen.this, QuranActivity.class);
		startActivity(quranActivity);
	}
	public void showAboutDialog()
	{
		//the code below is for the 'about' modal dialog
        aboutWindow = new AlertDialog.Builder(this);
        final String website = " simpledevcode.wordpress.com";
        final String AboutDialogMessage = " Prayer Times\n Version 2.4\n By Karim Oumghar\n\n Website for contact:\n";
        final TextView tx = new TextView(this);
        tx.setText(AboutDialogMessage + website);
        tx.setAutoLinkMask(RESULT_OK);
        tx.setTextColor(Color.WHITE);
        tx.setTextSize(15);
        tx.setMovementMethod(LinkMovementMethod.getInstance());
        Linkify.addLinks(tx, Linkify.WEB_URLS);
        
        aboutWindow.setIcon(R.drawable.ic_launcher);
        aboutWindow.setTitle("About");
    	aboutWindow.setView(tx);
    	
    	aboutWindow.setPositiveButton("OK", new DialogInterface.OnClickListener() 
    	{
			
			@Override
			public void onClick(DialogInterface dialog, int which) 
			{
				dialog.dismiss();
			}
		    	});
    	aboutWindow.show();
	}
	public void displayDate()
	{
		Calendar c = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");
		String dateString = sdf.format(c.getTime());
		dateDesc.setText(dateString);
	}
	public void initTextViews()
	{
		//initialize the textviews
		city = (TextView)findViewById(R.id.CityText);
		fajr = (TextView)findViewById(R.id.fajrTime);
		sunrise = (TextView)findViewById(R.id.sunriseTime);
		dhuhr = (TextView)findViewById(R.id.dhuhrTime);
		asr = (TextView)findViewById(R.id.asrTime);
		maghrib = (TextView)findViewById(R.id.maghribTime);
		isha = (TextView)findViewById(R.id.ishaTime);
		
		nextTextView = (TextView)findViewById(R.id.NextText);
		
		dateDesc = (TextView)findViewById(R.id.dateTextView); 
		fajrDesc = (TextView)findViewById(R.id.FajrText);
		sunriseDesc = (TextView)findViewById(R.id.SunriseText);
		dhuhrDesc = (TextView)findViewById(R.id.DhuhrText);
		asrDesc = (TextView)findViewById(R.id.AsrText);
		maghribDesc = (TextView)findViewById(R.id.MaghribText);
		ishaDesc = (TextView)findViewById(R.id.IshaText);
	}
	public void setTextToTimes()
	{
		//set each prayer time to the label
        city.setText(pi.getCity());
        fajr.setText(pi.getFajr());
        sunrise.setText(pi.getSunrise());
        dhuhr.setText(pi.getDhuhr());
        asr.setText(pi.getAsr());
        maghrib.setText(pi.getMaghrib());
        isha.setText(pi.getIsha());
	}
	private void setTypefaces()
	{
		city.setTypeface(tf);
		fajr.setTypeface(tf);
		sunrise.setTypeface(tf);
		dhuhr.setTypeface(tf);
		asr.setTypeface(tf);
		maghrib.setTypeface(tf);
		isha.setTypeface(tf);
		
		dateDesc.setTypeface(tf);
		fajrDesc.setTypeface(tf);
		sunriseDesc.setTypeface(tf);
		dhuhrDesc.setTypeface(tf);
		asrDesc.setTypeface(tf);
		maghribDesc.setTypeface(tf);
		ishaDesc.setTypeface(tf);
	}
	private void scheduleUpdate()
	{
		//changed from FLAG_UPDATE_CURRENT to support new firmware
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			pendingIntent = PendingIntent.getBroadcast(getApplicationContext(),
					7,
					updateAlarmIntent,
					PendingIntent.FLAG_IMMUTABLE);
		}
		else {
			pendingIntent = PendingIntent.getBroadcast(getApplicationContext(),
					7,
					updateAlarmIntent,
					PendingIntent.FLAG_UPDATE_CURRENT);
		}
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.HOUR, 0);
		calendar.set(Calendar.AM_PM, Calendar.AM);
		calendar.add(Calendar.DAY_OF_MONTH, 1);
		am.setRepeating(AlarmManager.RTC, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
	}
	private void setTimes()
	{
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
		//timings = new Date[]{fajrTime, dhuhrTime, asrTime,maghribTime,ishaTime};
		timings[0] = fajrTime;
		timings[1] = dhuhrTime;
		timings[2] = asrTime;
		timings[3] = maghribTime;
		timings[4] = ishaTime;
		
	}
	private void checkIfArabicLocale()
	{
		//if arabic locale is checked, make the labels for the prayer times arabic text
		//save this for later
	}

	private boolean isServiceRunning(Class<?> serviceClass)
	{
		ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
	    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) 
	    {
	        if (serviceClass.getName().equals(service.service.getClassName())) 
	        {
	            return true;
	        }
	    }
	    return false;
	}
	private BroadcastReceiver bcsr = new BroadcastReceiver()
	{

		@Override
		public void onReceive(Context context, Intent intent)
		{
			nextTextView.setText(intent.getExtras().getString("countdownTime"));
		}
		
	};
}
