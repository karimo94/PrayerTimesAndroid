package com.karimo.prayertimes;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

public class BookmarkActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookmarked_verses);
        //TODO sqlite based storage of verses, its a listview activity
    }
}