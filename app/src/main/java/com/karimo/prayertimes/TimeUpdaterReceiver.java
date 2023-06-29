package com.karimo.prayertimes;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;


public class TimeUpdaterReceiver extends BroadcastReceiver
{
	Intent timeUpdaterServiceIntent;

	@Override
	public void onReceive(Context context, Intent intent)
	{

		timeUpdaterServiceIntent = new Intent(context.getApplicationContext(), TimeUpdaterService.class);
		if(MainScreen.isMainScreenActive) //if the main screen is currently on display
		{
			//redraw the main screen at midnight
			Intent redrawIntent = new Intent(context.getApplicationContext(), MainScreen.class);
			context.startActivity(redrawIntent);
		}

		//restart the time updater service, get new daily adhan/prayer times
		TimeUpdaterService.enqueueWork(context.getApplicationContext(), timeUpdaterServiceIntent);//context.startService(timeUpdaterServiceIntent);
	}
	@SuppressWarnings("deprecation")
	private boolean isServiceRunning(Class<?> serviceClass, Context context)
	{
		ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
	    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) 
	    {
	        if (serviceClass.getName().equals(service.service.getClassName())) 
	        {
	            return true;
	        }
	    }
	    return false;
	}
}