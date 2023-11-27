package com.karimo.prayertimes;

import android.app.*;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.IBinder;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;


public class QuranPlayerService extends Service {
    private static final String CHANNEL_ID = "QuranPlayerForegroundServiceChannel";
    private static final int NOTIFICATION_ID = 5;
    // TODO need to control media player via notification
    //https://stackoverflow.com/questions/63501425/java-android-media-player-notification
    //https://developer.here.com/documentation/android-sdk-navigate/4.14.4.0/dev_guide/topics/get-locations-enable-background-updates.html
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String input = intent.getStringExtra("");
        createNotificationChannel();
        Intent qrPlayerNotifIntent = new Intent(getApplicationContext(), MainScreen.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(
                        getApplicationContext(),
                        NOTIFICATION_ID,
                        qrPlayerNotifIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Foreground Service")
                .setContentText(input)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentIntent(pendingIntent)
                .build();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                    NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MANIFEST);
        } else {
            startForeground(2, notification);
        }

        return START_STICKY;
    }

    private void createNotificationChannel() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel =
                    new NotificationChannel(
                            CHANNEL_ID,
                            "Quran Player Foreground Service Channel",
                            NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(notificationChannel);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
