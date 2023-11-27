package com.karimo.prayertimes;

import android.app.Activity;
import android.app.NotificationChannel;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import android.os.Bundle;
import androidx.media3.common.C;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.TimeBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.android.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Timer;

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
    /* EXOPLAYER CONTROLLER */
    ExoPlayer exoPlayer;
    /* LIST VIEW, ARRAY ADAPTER & TRACK VARIABLES */
    ArrayList<ChaptersObject.Chapter> surahs;
    int currentSelectedIndex;
    ChaptersListArrayAdapter surahsListArrayAdapter;
    String audioUrl;
    /* HANDLER TO RUN THE SEEKBAR */
    Handler handler = new Handler();
    private final Runnable updateProgressAction = new Runnable() {
        @Override
        public void run() {
            updateSeekbar();
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quran_player);
        surahs = new ArrayList<>();
        surahsListView = findViewById(R.id.selectSurahToPlayListView);
        surahsListView.setOnItemClickListener(this);

        currentPlayingSurahName = findViewById(R.id.surahPlayingText);
        trackPosition = findViewById(R.id.trackPosition);
        trackLength = findViewById(R.id.trackLength);

        seekBar = findViewById(R.id.seekBar);

        audioUrl = getApplicationContext().getString(R.string.quran_audio_url1);

        exoPlayer = new ExoPlayer.Builder(this).build();
        exoPlayer.addListener(this);

        String surahsJson = loadSurahsJson();
        convertToList(surahsJson);
        populateListView();

        currentSelectedIndex = 1;
        playPauseBtn = findViewById(R.id.togglePlayBtn);
        seekBar.addListener(new TimeBar.OnScrubListener() {
            @Override
            public void onScrubStart(TimeBar timeBar, long position) {

            }

            @Override
            public void onScrubMove(TimeBar timeBar, long position) {
                if(exoPlayer != null) {
                    exoPlayer.seekTo(position);
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
        try
        {
            surahsListArrayAdapter = new ChaptersListArrayAdapter(this, R.layout.surahs_list_view_item);
        }
        catch(Exception e)
        {
            System.out.println("Error creating an adapter for list view: " + e.getMessage());
        }

        //create a listener to listen to edit text changes (search queries)
        surahsListArrayAdapter.addAll(surahs);
        surahsListView.setAdapter(surahsListArrayAdapter);
    }
    public void togglePlay(View v) {
        Log.d("QURAN PLAYER", "togglePlay press");
        if(exoPlayer.isPlaying()) {
            exoPlayer.setPlayWhenReady(false);
            playPauseBtn.setImageResource(android.R.drawable.ic_media_play);
        }
        else {
            if(exoPlayer.getMediaItemCount() > 0) {
                exoPlayer.setPlayWhenReady(true);
                playPauseBtn.setImageResource(android.R.drawable.ic_media_pause);
            }
        }
    }
    public void goToNextSurah(View v) {
        exoPlayer.stop();
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
        exoPlayer.stop();
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
    private void autoPlaySurah(int id) {
        String requestedAudioUrl = audioUrl + String.format("%03d", id) + ENCODING;
        Log.d("QURAN PLAYER","will prepare");
        exoPlayer.stop();
        exoPlayer.setMediaItem(MediaItem.fromUri(requestedAudioUrl));
        exoPlayer.prepare();
        //---
        //play the track immediately after prepare, no need to call this in state changed
        exoPlayer.play();
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

    private void updateSeekbar() {
        if (exoPlayer != null && exoPlayer.isPlaying() && exoPlayer.getCurrentPosition() < exoPlayer.getDuration()) {
            int currentPosition = (int) exoPlayer.getCurrentPosition();
            trackPosition.setText(String.format("%02d:%02d", currentPosition / 60000, (currentPosition / 1000) % 60));
            seekBar.setPosition(currentPosition);
        }
        handler.postDelayed(updateProgressAction, 1000);
    }
    private void clearMediaPlayer() {
        seekBar.setPosition(0);
        exoPlayer.stop();
        exoPlayer.release();
        trackPosition.setText("--:--");
        trackLength.setText("--:--");
        exoPlayer = null;
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        Log.d("QURAN PLAYER", "state: " + playbackState);
        if(exoPlayer.getPlaybackState() == ExoPlayer.STATE_READY) {
            int duration = (int) exoPlayer.getDuration();
            seekBar.setDuration(duration);
            trackLength.setText(String.format("%02d:%02d", duration / 60000, (duration / 1000) % 60));
            displayCurrentTrackText(surahs, currentSelectedIndex - 1);
            playPauseBtn.setImageResource(android.R.drawable.ic_media_pause);
        }

        if (exoPlayer.getPlaybackState() == ExoPlayer.STATE_ENDED){
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
        if(exoPlayer != null && !exoPlayer.isPlaying()) {
            Log.d("QURAN PLAYER", "will clear media player");
            clearMediaPlayer();
        }
        super.onStop();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}