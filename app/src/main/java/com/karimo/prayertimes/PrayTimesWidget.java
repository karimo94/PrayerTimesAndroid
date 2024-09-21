package com.karimo.prayertimes;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.widget.RemoteViews;

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
	private Typeface tf;
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

			rvs.setTextViewText(R.id.widgetDateDesc, dateString + " — " + pti.getCity());
			rvs.setTextViewText(R.id.wFajrTime, pti.getFajr());
			rvs.setTextViewText(R.id.wDhuhrTime, pti.getDhuhr());
			rvs.setTextViewText(R.id.wAsrTime, pti.getAsr());
			rvs.setTextViewText(R.id.wMaghribTime, pti.getMaghrib());
			rvs.setTextViewText(R.id.wIshaTime, pti.getIsha());

			//to highlight a label, you could scan the times & see which one needs text color change holo_green_light
			//one more thing you need to do, is in the AlarmReceiver, trigger the widget to update
			highlightCurrentPrayer(pti, rvs);

			Intent intent = new Intent(context, MainScreen.class);
	        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

	        rvs.setOnClickPendingIntent(R.id.mainWidgetLinLayout, pendingIntent);
			// Tell the AppWidgetManager to perform an update on the current app widget
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
	private void highlightCurrentPrayer(PrayTimeInterface pti, RemoteViews rvs) {
		//go through the prayer times
		//figure out from current() which prayer time to color on the widget
		DateFormat dtf = new SimpleDateFormat("hh:mm a");
		Date current = null;

		for(int i = 0; i < pti.prayerTimes.size(); i++) {
			Date pt = null;

			try {
                pt = dtf.parse(pti.prayerTimes.get(i));
				current = dtf.parse(dtf.format(Calendar.getInstance().getTime()));
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }

			//you need to just compare the hh:mm, not the dt
			if(current.equals(pt) || current.after(pt)) {
				switch(i) {
					case 0:
						rvs.setTextColor(R.id.wFajrTime, Color.BLUE);
						rvs.setTextColor(R.id.wDhuhrTime, Color.BLACK);
						rvs.setTextColor(R.id.wAsrTime, Color.BLACK);
						rvs.setTextColor(R.id.wMaghribTime, Color.BLACK);
						rvs.setTextColor(R.id.wIshaTime, Color.BLACK);
						break;
					case 2:
						rvs.setTextColor(R.id.wFajrTime, Color.BLACK);
						rvs.setTextColor(R.id.wDhuhrTime, Color.BLUE);
						rvs.setTextColor(R.id.wAsrTime, Color.BLACK);
						rvs.setTextColor(R.id.wMaghribTime, Color.BLACK);
						rvs.setTextColor(R.id.wIshaTime, Color.BLACK);
						break;
					case 3:
						rvs.setTextColor(R.id.wFajrTime, Color.BLACK);
						rvs.setTextColor(R.id.wDhuhrTime, Color.BLACK);
						rvs.setTextColor(R.id.wAsrTime, Color.BLUE);
						rvs.setTextColor(R.id.wMaghribTime, Color.BLACK);
						rvs.setTextColor(R.id.wIshaTime, Color.BLACK);
						break;
					case 5:
						rvs.setTextColor(R.id.wFajrTime, Color.BLACK);
						rvs.setTextColor(R.id.wDhuhrTime, Color.BLACK);
						rvs.setTextColor(R.id.wAsrTime, Color.BLACK);
						rvs.setTextColor(R.id.wMaghribTime, Color.BLUE);
						rvs.setTextColor(R.id.wIshaTime, Color.BLACK);
						break;
					case 6:
						rvs.setTextColor(R.id.wFajrTime, Color.BLACK);
						rvs.setTextColor(R.id.wDhuhrTime, Color.BLACK);
						rvs.setTextColor(R.id.wAsrTime, Color.BLACK);
						rvs.setTextColor(R.id.wMaghribTime, Color.BLACK);
						rvs.setTextColor(R.id.wIshaTime, Color.BLUE);
						break;
					default:
						break;
				}
			}
        }
	}
}