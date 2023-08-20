package com.karimo.prayertimes;

import android.app.Activity;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class QuranActivity extends Activity implements AdapterView.OnItemClickListener, TextWatcher {

    private ListView surahsListView;
    private EditText searchSurahsInput;
    private SurahsListArrayAdapter adapter;
    private ArrayList<QuranObject.Surah> surahs;
    /* TODO https://stackoverflow.com/questions/18506374/the-best-way-to-caching-json cache 3 most recent surahs */
    /* https://stackoverflow.com/questions/19945411/how-can-i-parse-a-local-json-file-from-assets-folder-into-a-listview */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quran);

        surahs = new ArrayList<>();
        surahsListView = findViewById(R.id.surahsListView);
        surahsListView.setOnItemClickListener(this);
        searchSurahsInput = findViewById(R.id.searchSurahText);

        //TODO ok, need to cache this, but later on
        String surahsJson = loadSurahsJson();
        convertToList(surahsJson);
        populateListView();
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
        //https://javapapers.com/android/android-listview-custom-layout-tutorial/
        try
        {
            adapter = new SurahsListArrayAdapter(this, R.layout.surahs_list_view_item);
        }
        catch(Exception e)
        {
            System.out.println("Error creating an adapter for list view: " + e.getMessage());
        }

        //create a listener to listen to edit text changes (search queries)
        adapter.addAll(surahs);
        surahsListView.setAdapter(adapter);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //when the user clicks on an item, they are taken to the QuranReader activity
        //pass the ID of the surah as an intent extra
        //in the reader, we can load the json and retrieve the associate surah of the list
        QuranObject.Surah surahSelected = (QuranObject.Surah) parent.getItemAtPosition(position);
        Toast.makeText(this, "You selected: " + surahSelected.name, Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, QuranReaderActivity.class);
        intent.putExtra("selectedSurah", surahSelected.id);
        startActivity(intent);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        this.adapter.getFilter().filter(s);
    }

    @Override
    public void afterTextChanged(Editable s) {

    }
}