package com.karimo.prayertimes;

import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class DhikrActivity extends Activity {

    //private TextView dhikrTextView;
    RecyclerView dhikrListView;
    ArrayList<String> dhikrItems;
    DhikrAdapter dhikrAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dhikr);
        //dhikrTextView = (TextView) findViewById(R.id.dhikrTextView);
        dhikrListView = findViewById(R.id.dhikr_recyclerview);
        dhikrListView.setLayoutManager(new LinearLayoutManager(this));
        dhikrItems = new ArrayList<>();
        loadDhikr();
        dhikrAdapter = new DhikrAdapter(dhikrItems);
        dhikrListView.setAdapter(dhikrAdapter);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        // getMenuInflater().inflate(R.menu.info, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return true;
    }
    private void loadDhikr() {
        StringBuilder sb = new StringBuilder();
        try
        {
            BufferedReader rd = new BufferedReader(new InputStreamReader(
                    this.getAssets().open("dhikr.txt")));

            String lineString = rd.readLine();
            while (lineString != null)
            {
                sb.append(lineString);
                sb.append(System.getProperty("line.separator"));
                lineString = rd.readLine();
            }
            //dhikrTextView.setText(sb);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        //split using -----------------------
        String[] dhikrTokens = sb.toString().split("-----------------------");
        for(int i = 0; i < dhikrTokens.length; i++) {
            dhikrItems.add(dhikrTokens[i]);
        }
    }
}