package com.karimo.prayertimes;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class QuranReaderActivity extends Activity implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    private ArrayList<QuranObject.Verse> verses;
    private ListView versesListView;
    private VersesListArrayAdapter adapter;
    private String surahArabicName;
    private int selectedSurah = 1;
    private int verseSelected = 1;
    private String selectedSurahName;
    private String surahNameTranslit;
    // for scroll position 
    // https://stackoverflow.com/questions/3014089/maintain-save-restore-scroll-position-when-returning-to-a-listview/5688490#5688490
    BookmarkDb bookmarkDb;
    SQLiteDatabase sqLiteDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quran_reader);
        //get extras from the intent launched
        Intent intent = getIntent();
        selectedSurah = intent.getIntExtra("selectedSurah", 1);
        if(intent.hasExtra("selectedVerse")) {
            verseSelected = intent.getIntExtra("selectedVerse", 1);
        }

        versesListView = findViewById(R.id.versesReaderListView);
        //first load the testData JSON
        //and convert to list
        //then grab the surah from the id passed in
        //then load the listview adapter
        String surahsJson = loadSurahsJson();
        convertToList(surahsJson, selectedSurah);
        populateListView();
        versesListView.setSelection(verseSelected - 1);

        bookmarkDb = new BookmarkDb(this);
        sqLiteDatabase = bookmarkDb.getWritableDatabase();

        versesListView.setOnItemClickListener(this);
        versesListView.setOnItemLongClickListener(this);
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
        surahArabicName = temp.name;
        surahNameTranslit = temp.transliteration;
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
        versesListView.setSelection(position);
    }
    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        /**
         * when you long click here, a modal should show to add the verse to bookmarks db
         */
        QuranObject.Verse current = (QuranObject.Verse) parent.getItemAtPosition(position);
        Toast.makeText(this, "Add verse to bookmarks", Toast.LENGTH_SHORT).show();
        AlertDialog.Builder aboutWindow = new AlertDialog.Builder(this);
        final TextView tx = new TextView(this);
        tx.setAutoLinkMask(RESULT_OK);
        tx.setTextColor(Color.WHITE);
        tx.setTextSize(15);
        tx.setMovementMethod(LinkMovementMethod.getInstance());
        aboutWindow.setIcon(R.drawable.bookmark);
        aboutWindow.setTitle("Bookmark?");
        aboutWindow.setMessage(current.id + ": " + current.text);
        aboutWindow.setView(tx);
        aboutWindow.setPositiveButton("OK", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                boolean dbOperation = commitToDb(selectedSurah,current.id,surahArabicName,surahNameTranslit);
                if(dbOperation) {
                    Toast.makeText(getApplicationContext(), "Bookmark saved successfully", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(getApplicationContext(), "Could not add to bookmarks", Toast.LENGTH_SHORT).show();
                }
            }
        });
        aboutWindow.show();
        return true;
    }
    private boolean commitToDb(int surahId, int verseId, String surahArName, String surahNameTranslit) {
        // Insert the new row, returning the primary key value of the new row
        long newRowId = bookmarkDb.insertBookmark(surahId, verseId, surahArName, surahNameTranslit);
        Log.d("QURAN READER", "DB Commit: " + newRowId);
        return (newRowId != -1);
    }
}