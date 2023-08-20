package com.karimo.prayertimes;

import android.app.Activity;
import android.media.MediaPlayer;
import android.view.View;
import android.widget.*;
import android.os.Bundle;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class QuranPlayerActivity extends Activity implements AdapterView.OnItemClickListener, Runnable
{
    final String ENCODING = ".mp3";
    ListView surahsListView;

    SeekBar seekBar;
    TextView currentPlayingSurahName;
    MediaPlayer mediaPlayer;
    ArrayList<QuranObject.Surah> surahs;

    int currentSelectedIndex;
    SurahsListArrayAdapter surahsListArrayAdapter;
    String audioUrl;
    /* todo for v2.5 register reciter as a setting
    * https://www.digitalocean.com/community/tutorials/android-media-player-song-with-seekbar
    * https://stackoverflow.com/questions/35210169/how-to-change-background-color-of-only-the-clicked-item-in-listview-in-android-s
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quran_player);
        surahs = new ArrayList<>();
        surahsListView = findViewById(R.id.selectSurahToPlayListView);
        surahsListView.setOnItemClickListener(this);
        currentPlayingSurahName = findViewById(R.id.surahPlayingText);
        seekBar = findViewById(R.id.seekBar);
        mediaPlayer = new MediaPlayer();
        audioUrl = getApplicationContext().getString(R.string.quran_audio_url1);
        String surahsJson = loadSurahsJson();
        convertToList(surahsJson);
        populateListView();
        currentSelectedIndex = 1;

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int x = (int) Math.ceil(progress / 1000f);

                if (x == 0 && mediaPlayer != null && !mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                    mediaPlayer.release();
                    mediaPlayer = null;
                    QuranPlayerActivity.this.seekBar.setProgress(0);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }
    private String loadSurahsJson() {
        String qrJson = null;
        //read the json file from assets
        try {
            InputStream is = this.getAssets().open("testData.json");
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
        QuranObject temp = jsonTool.fromJson(jsonInput, QuranObject.class);
        surahs = temp.surahs;
    }
    private void populateListView() {
        try
        {
            surahsListArrayAdapter = new SurahsListArrayAdapter(this, R.layout.surahs_list_view_item);
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
        if(mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
        else {
            mediaPlayer.start();
        }
    }
    public void goToNextSurah(View v) {
        mediaPlayer.stop();
        //advance the index
        if(currentSelectedIndex == 113) {
            currentSelectedIndex = 1;
        }
        else {
            currentSelectedIndex += 1;
        }
        autoPlaySurah(currentSelectedIndex);
    }
    public void goToPreviousSurah(View v) {
        mediaPlayer.stop();
        //decrement the index
        if(currentSelectedIndex == 1) {
            currentSelectedIndex = 113;
        }
        else {
            currentSelectedIndex -= 1;
        }
        autoPlaySurah(currentSelectedIndex);
    }
    private void autoPlaySurah(int id) {
        String requestedAudioUrl = audioUrl + String.format("%03d", id) + ENCODING;
        if(mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        try {
            mediaPlayer.setDataSource(requestedAudioUrl);
            mediaPlayer.prepare();
            seekBar.setMax(mediaPlayer.getDuration());
            mediaPlayer.start();
        }
        catch(IOException ex) {
            ex.printStackTrace();
        }
        currentPlayingSurahName.setText(surahs.get(currentSelectedIndex).transliteration + " | " +
                surahs.get(currentSelectedIndex).name + " | " +
                surahs.get(currentSelectedIndex).translation);
        new Thread(this).start();
    }
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //when you click on an item, grab the index and set the media player source
        //to the url and start auto-play

        //todo for v2.5 color the selected list item

        QuranObject.Surah selected = (QuranObject.Surah) parent.getItemAtPosition(position);

        autoPlaySurah(selected.id);
        currentPlayingSurahName.setText(selected.transliteration + " | " +
                selected.name + " | " +
                selected.translation);
    }

    @Override
    public void run() {
        int currentPosition = mediaPlayer.getCurrentPosition();
        int total = mediaPlayer.getDuration();

        while (mediaPlayer != null && mediaPlayer.isPlaying() && currentPosition < total) {
            try {
                Thread.sleep(1000);
                currentPosition = mediaPlayer.getCurrentPosition();
            } catch (InterruptedException e) {
                return;
            } catch (Exception e) {
                return;
            }
            seekBar.setProgress(currentPosition);
        }
    }
}