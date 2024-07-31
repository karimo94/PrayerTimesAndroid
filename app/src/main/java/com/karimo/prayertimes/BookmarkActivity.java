package com.karimo.prayertimes;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;


public class BookmarkActivity extends Activity implements BookmarksAdapter.RecyclerItemClickListener{
    BookmarkDb bookmarkDb;
    SQLiteDatabase sqLiteDatabase;
    RecyclerView bookmarksListView;
    ArrayList<BookmarkBean> allBookmarks;
    BookmarksAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookmarked_verses);
        //all you're doing here is loading all rows from the database
        //when you click on a list item, it should take you to quran reader activity
        allBookmarks = new ArrayList<>();
        bookmarksListView = findViewById(R.id.bookmarks_recyclerview);
        bookmarksListView.setLayoutManager(new LinearLayoutManager(this));
        bookmarkDb = new BookmarkDb(this);
        sqLiteDatabase = bookmarkDb.getWritableDatabase();

        allBookmarks = bookmarkDb.loadAllBookmarks();
        adapter = new BookmarksAdapter(this, allBookmarks);
        adapter.setRecyclerClickListener(this);
        bookmarksListView.setAdapter(adapter);
    }


    @Override
    public void onItemClick(View view, int position) {
        //should open quran reader activity
        //pass in the verse id so the activity can
        //scroll to that verse

        BookmarkBean temp = allBookmarks.get(position);
        Intent openSurahIntent = new Intent(this, QuranReaderActivity.class);
        openSurahIntent.putExtra("selectedSurah", temp.surahId);
        openSurahIntent.putExtra("selectedVerse",temp.verseId);
        startActivity(openSurahIntent);
    }
}