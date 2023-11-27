package com.karimo.prayertimes;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class MidnightAlarmService extends JobIntentService {
    // today
    Calendar date = new GregorianCalendar();
    AlarmManager alarmManager;
    Intent midnightIntent;
    PendingIntent midnightPendingIntent;
    private static final int MIDNIGHT_ALARM_CODE = 64;
    private static final int JOB_ID = 9400;
    @Override
    public void onCreate() {
        super.onCreate();
        midnightIntent = new Intent(MidnightAlarmService.this, MidnightAlarmReceiver.class);
        midnightIntent.putExtra("midnightIntent", MIDNIGHT_ALARM_CODE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            midnightPendingIntent = PendingIntent.getBroadcast(MidnightAlarmService.this,
                    MIDNIGHT_ALARM_CODE, midnightIntent, PendingIntent.FLAG_IMMUTABLE);
        }
        else {
            midnightPendingIntent = PendingIntent.getBroadcast(MidnightAlarmService.this,
                    MIDNIGHT_ALARM_CODE, midnightIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
    }
    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        //set a repeating alarm at midnight
        // reset hour, minutes, seconds and millis
        date.set(Calendar.HOUR_OF_DAY, 0);
        date.set(Calendar.MINUTE, 0);
        date.set(Calendar.SECOND, 0);
        date.set(Calendar.MILLISECOND, 0);

        // next day
        date.add(Calendar.DAY_OF_MONTH, 1);

        alarmManager = (AlarmManager)this.getSystemService(ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC, date.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, midnightPendingIntent);

    }
    public static void enqueueWork(Context context, Intent work) {
        //this is a public setter that calls the actual enqueueWork from implementing JobIntentSvc
        enqueueWork(context, MidnightAlarmService.class, JOB_ID, work);
    }
}
