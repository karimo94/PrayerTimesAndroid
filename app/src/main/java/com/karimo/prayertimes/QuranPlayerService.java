package com.karimo.prayertimes;

import android.content.Intent;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.session.MediaSession;
import androidx.media3.session.MediaSessionService;


public class QuranPlayerService extends MediaSessionService {

    /* EXOPLAYER CONTROLLER */
    ExoPlayer exoPlayer;
    MediaSession session;
    //-------------------------------------

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
        if (player.getPlayWhenReady()) {
            // Make sure the service is not in foreground.
            player.pause();
        }
        stopSelf();
    }
    // need to release the player and media session in onDestroy
    @Override
    public void onDestroy() {
        session.getPlayer().release();
        session.release();
        session = null;
        super.onDestroy();
    }
}
