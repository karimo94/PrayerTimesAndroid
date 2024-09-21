package com.karimo.prayertimes;

import java.text.SimpleDateFormat;
import java.util.Calendar;


import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.*;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.widget.RemoteViews;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import static androidx.core.content.ContextCompat.getSystemService;

public class AlarmReceiver extends BroadcastReceiver
{
	private Intent adhanServiceIntent;
	private SimpleDateFormat sdfDateFormat = new SimpleDateFormat("hh:mm a");
	private String[] prayerNames = {"Fajr","Dhuhr","Asr","Maghrib","Isha"};
	SharedPreferences spAdhan;
	SharedPreferences spNotifs;
	SharedPreferences sp1;
	SharedPreferences sp2;
	SharedPreferences sp3;
	SharedPreferences sp4;
	SharedPreferences sp5;
	boolean[] adhanChecks = new boolean[5];
	boolean isNotifsEnabled = false;
	boolean isAdhanEnabled = false;
	String adhanResource;

	public AlarmReceiver() {
		
	}
	@RequiresApi(api = Build.VERSION_CODES.O)
	@Override
	public void onReceive(Context context, Intent intent)
	{
		setChecks(context);
		adhanServiceIntent = new Intent(context, AdhanService.class);
		int requestCode = intent.getExtras().getInt("salatIntent");
		if(isNotifsEnabled && !adhanChecks[requestCode]) //later change to just the first clause
		{
			addNotification(context, prayerNames[requestCode]);
		}
		if(isNotifsEnabled && adhanChecks[requestCode]) {
			/*Intent adhanIntent = new Intent(context.getApplicationContext(), AdhanService.class);
			AdhanService.enqueueWork(context.getApplicationContext(), adhanIntent);*/
			createSoundNotification(context, prayerNames[requestCode]);
		}
		if(!isNotifsEnabled && adhanChecks[requestCode]) {
			Intent adhanIntent = new Intent(context.getApplicationContext(), AdhanService.class);
			AdhanService.enqueueWork(context.getApplicationContext(), adhanIntent);
		}

		//update the widget to highlight the current prayer on the widget
		RemoteViews rvs = new RemoteViews(context.getPackageName(), R.xml.widget1_info);
		ComponentName thisWidget = new ComponentName(context, PrayTimesWidget.class );
		AppWidgetManager.getInstance(context).updateAppWidget(thisWidget, rvs);
	}
	@RequiresApi(api = Build.VERSION_CODES.O)
	public void addNotification(Context context, String prayerName)
	{
		/* notification stuff */
		NotificationChannel notificationChannel = null;
		// make the channel. The method has been discussed before.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			notificationChannel = makeNotificationChannel("CHANNEL_1",
					"Prayer notification channel",
					NotificationManager.IMPORTANCE_DEFAULT,
					context);
		}
		NotificationCompat.Builder notification =
				new NotificationCompat.Builder(context, notificationChannel.getId());

		String timestamp = sdfDateFormat.format(Calendar.getInstance().getTime());
		notification
		  .setSmallIcon(R.drawable.masjid_icon)
		  .setContentTitle("Salat")
		  .setContentText(prayerName + " " + timestamp);


		notification.setAutoCancel(true);
		Intent notificationIntent = new Intent(context, MainScreen.class);
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		notification.setContentIntent(contentIntent);

		// Add as notification
		NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		manager.notify(1, notification.build());
	}
	@RequiresApi(api = Build.VERSION_CODES.O)
	private NotificationChannel makeNotificationChannel(String id, String name, int importance, Context context)
	{
		NotificationChannel channel = new NotificationChannel(id, name, importance);
		channel.setShowBadge(true); // set false to disable badges, Oreo exclusive

		NotificationManager notificationManager =
				(NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

		assert notificationManager != null;

		channel.setSound(null, null);

		notificationManager.createNotificationChannel(channel);

		return channel;
	}
	@RequiresApi(api = Build.VERSION_CODES.O)
	private void createSoundNotification(Context context, String prayerName) {
		Intent intent = new Intent(context, MainScreen.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

		//set your adhan sound resource here from shared preferences
		Uri soundUri = Uri.parse(adhanResource);


		NotificationManager mNotificationManager =
				(NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);


		// Creating an Audio Attribute
		AudioAttributes audioAttributes = new AudioAttributes.Builder()
				.setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
				.setUsage(AudioAttributes.USAGE_ALARM)
				.build();

		// Creating Channel
		NotificationChannel notificationChannel =
				new NotificationChannel("CHANNEL_SOUND_2","Adhan Notification Channel",
						NotificationManager.IMPORTANCE_HIGH);
		notificationChannel.setSound(soundUri,audioAttributes);
		mNotificationManager.createNotificationChannel(notificationChannel);


		String timestamp = sdfDateFormat.format(Calendar.getInstance().getTime());
		NotificationCompat.Builder notificationBuilder =
				new NotificationCompat.Builder(context, "CHANNEL_SOUND_2")
						.setSmallIcon(R.drawable.masjid_icon)
						.setContentTitle("Salat")
						.setContentText(prayerName + " " + timestamp)
						.setAutoCancel(false)
						.setSound(soundUri);
		mNotificationManager.notify(0, notificationBuilder.build());
	}
	public void setChecks(Context context)
	{
		sp1 = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
		adhanChecks[0] = sp1.getBoolean("myFajrAdhanOpt", false);
		sp2 = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
		adhanChecks[1] = sp2.getBoolean("myDhuhrAdhanOpt", false);
		sp3 = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
		adhanChecks[2] = sp3.getBoolean("myAsrAdhanOpt", false);
		sp4 = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
		adhanChecks[3] = sp4.getBoolean("myMaghribAdhanOpt", false);
		sp5 = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
		adhanChecks[4] = sp5.getBoolean("myIshaAdhanOpt", false);
		
		spNotifs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
		isNotifsEnabled = spNotifs.getBoolean("myNotifsOpt", false);
		spAdhan = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
		isAdhanEnabled = spAdhan.getBoolean("myAdhanOpt", false);

		adhanResource = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext())
				.getString("myAdhanSoundPref", context.getString(R.string.default_adhan_resource));
	}
}
