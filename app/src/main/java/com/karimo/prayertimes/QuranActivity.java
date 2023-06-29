package com.karimo.prayertimes;

import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import com.karimo.prayertimes.ui.main.SectionsPagerAdapter;
import com.karimo.prayertimes.databinding.ActivityQuranBinding;

public class QuranActivity extends AppCompatActivity {
    /*
    * This is a tabbed activity, 3 tabs
    * Listview to select Surah
    * Listview to read selected Surah
    * Relative layout to listen to selected Surah
    * */
    private ActivityQuranBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /*
        TODO https://stackoverflow.com/questions/18506374/the-best-way-to-caching-json
         */
        super.onCreate(savedInstanceState);

        binding = ActivityQuranBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = binding.viewPager;
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = binding.tabs;
        tabs.setupWithViewPager(viewPager);
        FloatingActionButton fab = binding.fab;

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }
}