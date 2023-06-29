package com.karimo.prayertimes;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MidnightAlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        //enqueueWork for TimeUpdaterService (just like you would do in mainscreen call)
        //which in turn calls upon AlarmService which sets the daily alarms
        Intent timeUpdaterServiceIntent = new Intent(context, TimeUpdaterService.class);
        TimeUpdaterService.enqueueWork(context, timeUpdaterServiceIntent);
    }
}
