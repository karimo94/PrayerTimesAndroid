package com.karimo.prayertimes;

import android.content.Intent;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.session.MediaSession;
import androidx.media3.session.MediaSessionService;


public class QuranPlayerService extends MediaSessionService {
    /* NOTIFICATION CONTROLS */
    private static final String CHANNEL_ID = "QuranPlayerForegroundServiceChannel";
    private static final int NOTIFICATION_ID = 5;
    //-------------------------------------
    /* EXOPLAYER CONTROLLER */
    ExoPlayer exoPlayer;
    MediaSession session;
    //-------------------------------------
    final String ENCODING = ".mp3";
    String audioUrl;

    // override the onGetSession method to give other clients
    // access to your media session that was built when the service was created
    @Nullable
    @Override
    public MediaSession onGetSession(MediaSession.ControllerInfo controllerInfo) {
        return session;
    }
    // Create your exoPlayer and mediaSession in the oncreate lifecycle event
    @Override
    public void onCreate() {
        super.onCreate();
        exoPlayer = new ExoPlayer.Builder(this).build();
        session = new MediaSession.Builder(this, exoPlayer).build();
    }
    // the user dismissed the app from the recent tasks
    @Override
    public void onTaskRemoved(@Nullable Intent rootIntent) {
        Player player = session.getPlayer();
        if(!player.getPlayWhenReady() ||
            player.getMediaItemCount() == 0 ||
            player.getPlaybackState() == Player.STATE_ENDED) {
            //stop the service if not playing, continue playing in the background otherwise
            stopSelf();
        }
    }
    // need to release the player and media session in onDestroy
    @Override
    public void onDestroy() {
        if(exoPlayer != null && !exoPlayer.isPlaying()) {
            Log.d("QURAN PLAYER", "will clear media player");
            //clearMediaPlayer();
        }
        session.getPlayer().release();
        session.release();
        session = null;
        super.onDestroy();
    }
}
