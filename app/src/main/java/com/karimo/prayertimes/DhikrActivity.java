package com.karimo.prayertimes;

import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class DhikrActivity extends Activity {

    private TextView dhikrTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dhikr);
        dhikrTextView = (TextView) findViewById(R.id.dhikrTextView);
        displayDhikr();
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
    private void displayDhikr() {
        try
        {
            BufferedReader rd = new BufferedReader(new InputStreamReader(
                    this.getAssets().open("dhikr.txt")));
            StringBuilder sb = new StringBuilder();
            String lineString = rd.readLine();
            while (lineString != null)
            {
                sb.append(lineString);
                sb.append(System.getProperty("line.separator"));
                lineString = rd.readLine();
            }
            dhikrTextView.setText(sb);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}