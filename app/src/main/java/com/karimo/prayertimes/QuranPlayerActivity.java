package com.karimo.prayertimes;

import android.app.Activity;
import android.content.ComponentName;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.*;
import android.os.Bundle;
import androidx.annotation.OptIn;
import androidx.core.content.ContextCompat;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.session.MediaController;
import androidx.media3.session.SessionToken;
import androidx.media3.ui.TimeBar;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.gson.Gson;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Timer;
import java.util.concurrent.ExecutionException;

import static androidx.media3.common.Player.STATE_READY;

public class QuranPlayerActivity extends Activity implements AdapterView.OnItemClickListener, Player.Listener {
    final String ENCODING = ".mp3";
    /* UI CONTROLS */
    ListView surahsListView;
    TimeBar seekBar;
    TextView currentPlayingSurahName;
    TextView trackPosition;
    TextView trackLength;
    ImageButton playPauseBtn;


    /* LIST VIEW, ARRAY ADAPTER & TRACK VARIABLES */
    int currentSelectedIndex;
    String audioUrl;
    ChaptersListArrayAdapter surahsListArrayAdapter;
    ArrayList<ChaptersObject.Chapter> surahs;

    /* HANDLER TO RUN THE SEEKBAR */
    Handler handler = new Handler();
    /************************/

    /************************/

    //Your UI uses the media controller to send commands from UI to player within session
    //To create a MediaController, start by creating a SessionToken for the corresponding MediaSession
    SessionToken sessionToken;

    //Using this sessiontoken to then build a MediaController connects the controller to the given session
    //this takes place async, so listen for the result and use when available
    MediaController myMediaController;
    ListenableFuture<MediaController> controllerFuture;

    private final Runnable updateProgressAction = new Runnable() {
        @Override
        public void run() {
            updateSeekbar();
        }
    };
    @OptIn(markerClass = UnstableApi.class) @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quran_player);
        /* INIT UI COMPONENTS */
        surahs = new ArrayList<>();
        surahsListView = findViewById(R.id.selectSurahToPlayListView);
        surahsListView.setOnItemClickListener(this);

        currentPlayingSurahName = findViewById(R.id.surahPlayingText);
        trackPosition = findViewById(R.id.trackPosition);
        trackLength = findViewById(R.id.trackLength);

        seekBar = findViewById(R.id.seekBar);
        playPauseBtn = findViewById(R.id.togglePlayBtn);
        /************************/

        /* INIT SELECTED INDEX, AUDIO URL */
        audioUrl = getApplicationContext().getString(R.string.quran_audio_url1);
        currentSelectedIndex = 1;
        /************************/

        /* INIT SESSION TOKEN, MEDIA CONTROLLER */
        sessionToken = new SessionToken(this, new ComponentName(this, QuranPlayerService.class));
        controllerFuture = new MediaController.Builder(this, sessionToken).buildAsync();
        /************************/

        /* INIT SURAHS LIST VIEW */
        String surahsJson = loadSurahsJson();
        convertToList(surahsJson);
        populateListView();
        /************************/

        /* INIT LISTENERS FOR SEEKBAR AND MEDIA CONTROLLER */
        controllerFuture.addListener(() -> {
            //MediaController is available here with controllerFuture.get();
            try {
                myMediaController = controllerFuture.get();
                myMediaController.addListener(this);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }, ContextCompat.getMainExecutor(this));

        seekBar.addListener(new TimeBar.OnScrubListener() {
            @Override
            public void onScrubStart(TimeBar timeBar, long position) {

            }

            @Override
            public void onScrubMove(TimeBar timeBar, long position) {
                if(myMediaController != null) {
                    myMediaController.seekTo(position);
                    trackPosition.setText(String.format("%02d:%02d", position / 60000, (position / 1000) % 60));
                }
            }

            @Override
            public void onScrubStop(TimeBar timeBar, long position, boolean canceled) {

            }
        });
    }
    private String loadSurahsJson() {
        String qrJson = null;
        //read the json file from assets
        try {
            InputStream is = this.getAssets().open("index.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            qrJson = new String(buffer,"UTF-8");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return qrJson;
    }
    private void convertToList(String jsonInput) {
        Gson jsonTool = new Gson();
        ChaptersObject temp = jsonTool.fromJson(jsonInput, ChaptersObject.class);
        surahs = temp.chapters;
    }
    private void populateListView() {
        try {
            surahsListArrayAdapter = new ChaptersListArrayAdapter(this, R.layout.surahs_list_view_item);
        }
        catch(Exception e) {
            System.out.println("Error creating an adapter for list view: " + e.getMessage());
        }

        //create a listener to listen to edit text changes (search queries)
        surahsListArrayAdapter.addAll(surahs);
        surahsListView.setAdapter(surahsListArrayAdapter);
    }
    public void togglePlay(View v) {
        Log.d("QURAN PLAYER", "togglePlay press");
        if(myMediaController != null && myMediaController.isPlaying()) {
            myMediaController.setPlayWhenReady(false);
            playPauseBtn.setImageResource(android.R.drawable.ic_media_play);
        }
        else {
            if(myMediaController.getMediaItemCount() > 0) {
                myMediaController.setPlayWhenReady(true);
                playPauseBtn.setImageResource(android.R.drawable.ic_media_pause);
            }
        }
    }
    public void goToNextSurah(View v) {
        myMediaController.stop();
        //advance the index
        if(currentSelectedIndex + 1 > 114) {
            currentSelectedIndex = 1;
        }
        else {
            currentSelectedIndex += 1;
        }
        autoPlaySurah(currentSelectedIndex);
    }
    public void goToPreviousSurah(View v) {
        myMediaController.stop();
        //decrement the index
        if(currentSelectedIndex - 1 < 0) {
            currentSelectedIndex = 114;
        }
        else {
            currentSelectedIndex -= 1;
        }
        autoPlaySurah(currentSelectedIndex);
    }
    private void displayCurrentTrackText(ArrayList<ChaptersObject.Chapter> tracks, int index) {

        currentPlayingSurahName.setText(tracks.get(index).transliteration + " | " +
                tracks.get(index).name + " | " +
                tracks.get(index).translation);
    }
    @OptIn(markerClass = UnstableApi.class)
    private void autoPlaySurah(int id) {
        String requestedAudioUrl = audioUrl + String.format("%03d", id) + ENCODING;
        Log.d("QURAN PLAYER","will prepare");
        myMediaController.stop();
        myMediaController.setMediaItem(MediaItem.fromUri(requestedAudioUrl));
        myMediaController.prepare();
        //---
        //play the track immediately after prepare, no need to call this in state changed
        myMediaController.play();
        updateProgressAction.run();
        //---
        surahsListView.invalidate();
        surahsListView.setSelection(id - 1);
        surahsListView.setItemChecked(id - 1, true);
    }
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //when you click on an item, grab the index and set the media player source
        //to the url and start autoplay
        ChaptersObject.Chapter selected = (ChaptersObject.Chapter) parent.getItemAtPosition(position);
        currentSelectedIndex = selected.id;
        autoPlaySurah(currentSelectedIndex);
    }

    @OptIn(markerClass = UnstableApi.class)
    private void updateSeekbar() {
        if (myMediaController != null && myMediaController.isPlaying() && myMediaController.getCurrentPosition() < myMediaController.getDuration()) {
            int currentPosition = (int) myMediaController.getCurrentPosition();
            trackPosition.setText(String.format("%02d:%02d", currentPosition / 60000, (currentPosition / 1000) % 60));
            seekBar.setPosition(currentPosition);
        }
        handler.postDelayed(updateProgressAction, 1000);
    }
    @OptIn(markerClass = UnstableApi.class)
    private void clearMediaPlayer() {
        seekBar.setPosition(0);
        myMediaController.stop();
        myMediaController.release();
        trackPosition.setText("--:--");
        trackLength.setText("--:--");
        myMediaController = null;
    }

    @Override
    @OptIn(markerClass = UnstableApi.class)
    public void onPlaybackStateChanged(@Player.State int playbackState ) {
        Log.d("QURAN PLAYER", "state: " + playbackState);

        if(myMediaController.getPlaybackState() == MediaController.STATE_READY) {
            int duration = (int) myMediaController.getDuration();
            seekBar.setDuration(duration);
            trackLength.setText(String.format("%02d:%02d", duration / 60000, (duration / 1000) % 60));
            displayCurrentTrackText(surahs, currentSelectedIndex - 1);
            playPauseBtn.setImageResource(android.R.drawable.ic_media_pause);
        }

        if (myMediaController.getPlaybackState() == MediaController.STATE_ENDED) {
            currentSelectedIndex += 1;
            autoPlaySurah(currentSelectedIndex);
        }
    }

    @Override
    public void onStop() {
        /*
        when your activity is no longer visible to the user
        release any resources not needed, if the player is
        not playing anything, clearMediaPlayer()
         */
        if(myMediaController != null && !myMediaController.isPlaying()) {
            Log.d("QURAN PLAYER", "will clear media player");
            clearMediaPlayer();
        }
        super.onStop();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
    }
    @Override
    protected void onPause() {
        //what you want to do is set a timeout
        //and if you happen to resume the activity within limit
        //no need to call clearMediaPlayer()
        Log.d("QURAN PLAYER", "pause hit");
        super.onPause();
    }
    @Override
    protected void onResume() {
        Log.d("QURAN PLAYER", "resume hit");
        super.onResume();
    }
}