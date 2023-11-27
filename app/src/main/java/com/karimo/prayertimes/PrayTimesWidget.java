package com.karimo.prayertimes;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.widget.RemoteViews;
import android.widget.TextView;

@SuppressWarnings("unused")
public class PrayTimesWidget extends AppWidgetProvider
{
	PrayTimeInterface pti;
	private Date fajrTime;
	private Date dhuhrTime;
	private Date asrTime;
	private Date maghribTime;
	private Date ishaTime;
	private Date nextPrayerTime;
	private String prayerName;
	private SimpleDateFormat dtFormat = new SimpleDateFormat("hh:mm a");
	private PendingIntent pendingIntent = null;
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
	{
		pti = new PrayTimeInterface(context);
		schedule(context);
		for(int i = 0; i < appWidgetIds.length; i++)
		{
			int appWidgetId = appWidgetIds[i];
			
			
			RemoteViews rvs  = new RemoteViews(context.getPackageName(), R.layout.widget1);
			SimpleDateFormat sdFormat = new SimpleDateFormat("dd-MMM-yyyy");
			String dateString = sdFormat.format(Calendar.getInstance().getTime());
			
			//to update a label
			rvs.setTextViewText(R.id.widgetDateDesc, dateString + " - " + HijriCalendar.writeIslamicDate());
			rvs.setTextViewText(R.id.wFajrTime, pti.getFajr());
			rvs.setTextViewText(R.id.wDhuhrTime, pti.getDhuhr());
			rvs.setTextViewText(R.id.wAsrTime, pti.getAsr());
			rvs.setTextViewText(R.id.wMaghribTime, pti.getMaghrib());
			rvs.setTextViewText(R.id.wIshaTime, pti.getIsha());
			
			Intent intent = new Intent(context, MainScreen.class);
	        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

	        rvs.setOnClickPendingIntent(R.id.mainWidgetLinLayout, pendingIntent);
			// Tell the AppWidgetManager to perform an update on the current app
		      // widget
		    appWidgetManager.updateAppWidget(appWidgetId, rvs);
		}
		super.onUpdate(context, appWidgetManager, appWidgetIds);
	}
	public void schedule(Context context)
	{
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.HOUR, 0);
		calendar.set(Calendar.AM_PM, Calendar.AM);
		calendar.add(Calendar.DAY_OF_MONTH, 1);
		AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		final Intent i = new Intent(context.getApplicationContext(), UpdateWidgetReceiver.class);
		pendingIntent = PendingIntent.getService(context, 0, i, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE);
		alarmManager.setRepeating(AlarmManager.RTC, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
	}
}
