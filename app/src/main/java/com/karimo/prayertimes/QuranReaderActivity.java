package com.karimo.prayertimes;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import androidx.viewpager.widget.ViewPager;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class QuranReaderActivity extends Activity implements AdapterView.OnItemClickListener {

    private ArrayList<QuranObject.Verse> verses;
    private ListView versesListView;
    private VersesListArrayAdapter adapter;
    private int selectedSurah = 1;
    // for scroll position 
    // https://stackoverflow.com/questions/3014089/maintain-save-restore-scroll-position-when-returning-to-a-listview/5688490#5688490

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quran_reader);
        //get extras from the intent launched
        Intent intent = getIntent();
        selectedSurah = intent.getIntExtra("selectedSurah", 1);
        versesListView = findViewById(R.id.versesReaderListView);
        //first load the testData JSON
        //and convert to list
        //then grab the surah from the id passed in
        //then load the listview adapter
        String surahsJson = loadSurahsJson();
        convertToList(surahsJson, selectedSurah);
        populateListView();

    }

    private String loadSurahsJson() {
        String qrJson = null;
        //read the json file from assets
        try {
            InputStream is = this.getAssets().open("chapters/" + selectedSurah + ".json");
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
    private void convertToList(String jsonInput, int selectedSurah) {
        Gson jsonTool = new Gson();
        QuranObject temp = jsonTool.fromJson(jsonInput, QuranObject.class);
        getActionBar().setTitle(temp.toStringSimple());
        verses = temp.verses;
    }
    private void populateListView() {
        //https://javapapers.com/android/android-listview-custom-layout-tutorial/
        try
        {
            adapter = new VersesListArrayAdapter(this, R.layout.verses_list_view_item);
        }
        catch(Exception e)
        {
            System.out.println("Error creating an adapter for list view: " + e.getMessage());
        }

        //create a listener to listen to edit text changes (search queries)
        adapter.addAll(verses);
        versesListView.setAdapter(adapter);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    /*
        when you long click here, a modal should show to add the verse to bookmarks json (cache)
     */
    }
}