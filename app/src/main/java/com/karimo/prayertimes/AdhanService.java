package com.karimo.prayertimes;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.JobIntentService;
import androidx.core.app.NotificationCompat;

public class AdhanService extends JobIntentService implements MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener
{
	private MediaPlayer mediaPlayer;
	private static final int JOB_ID = 9200;

	@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
	@Override
	public void onCreate()
	{
		super.onCreate();
		if(mediaPlayer == null) {
			mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.adhan);
		}
	}
	@Override
	public void onCompletion(MediaPlayer mp)
	{
		stopSelf();
	}
	@Override
	public void onPrepared(MediaPlayer mp)
	{
		mp.start();
	}
	public void playAdhan()
	{
		mediaPlayer.setVolume(1.0f, 1.0f);
		mediaPlayer.start();
		//set the listener for oncompletion
		mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				if(mediaPlayer != null) {
					if (mediaPlayer.isPlaying())
					{
						mediaPlayer.stop();
					}
					mediaPlayer.reset();
					mediaPlayer.release();
					mediaPlayer = null;
				}
			}
		});
	}
	@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		super.onStartCommand(intent, flags, startId);

		//FOR THE OLD StartService() implementation
		playAdhan();


		return START_STICKY;
	}
	public void onDestroy()
	{ }
	@Override
	protected void onHandleWork(@NonNull Intent intent) {
		//from implementing JobIntentService
		playAdhan();
	}
	public static void enqueueWork(Context context, Intent work) {
		//from implementing JobIntentService, this is needed
		enqueueWork(context, AdhanService.class, JOB_ID, work);
	}
}
