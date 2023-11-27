package com.karimo.prayertimes;



import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import static androidx.core.app.JobIntentService.enqueueWork;

public class DeviceBootReceiver extends BroadcastReceiver //WakefulBroadcastReceiver
{

	@Override
	public void onReceive(Context context, Intent intent)
	{
		String action = intent.getAction();
		if(Intent.ACTION_BOOT_COMPLETED.equals(action)) {
			/* Setting the alarm here */
			Intent alarmIntent = new Intent(context.getApplicationContext(), TimeUpdaterService.class);

			//context.startService(alarmIntent);
			TimeUpdaterService.enqueueWork(context.getApplicationContext(), alarmIntent);

			//also set midnight alarm intent here
			Intent midnightAlarmIntent = new Intent(context.getApplicationContext(), MidnightAlarmService.class);
			MidnightAlarmService.enqueueWork(context.getApplicationContext(), midnightAlarmIntent);
		}
	}
}
