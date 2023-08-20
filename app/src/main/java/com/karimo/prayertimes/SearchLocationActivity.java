package com.karimo.prayertimes;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.SimpleTimeZone;
import java.util.StringTokenizer;
import java.util.TimeZone;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.SearchView.OnQueryTextListener;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
@SuppressWarnings("unused")
public class SearchLocationActivity extends Activity implements TextWatcher, OnItemClickListener
{
	/***********GLOBAL VARIABLES***********/
	private ArrayList<CityObj> cities;
	
	private SharedPreferences sharedPref; //to save the database
	private Gson gson;
	
	
	private EditText searchBox;
	private ListView listView1;

	//listview adapter
	private ArrayAdapter<CityObj> adapter;
	
	/**************************************/
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search_location);
		cities = new ArrayList<>();
		
		//try to load here...
		sharedPref = this.getPreferences(Context.MODE_PRIVATE);

		gson = new Gson();
		
		String json = null;
		try
		{
			json = sharedPref.getString("myJson","");
		}
		catch(Exception e)
		{
			System.out.println("Error retrieving data from memory.");
		}
		//ok, lets see if we're successful in loading from memory
		if(!json.isEmpty())
		{
			Type t = new TypeToken<ArrayList<CityObj>>(){}.getType();
			cities = gson.fromJson(json, t);
		}
		
		//we got nothing loaded so...
		//load the csv file to search thru
		//first you need to check if the list is empty, the go ahead and load
		//otherwise, its best to load once, and just use the memory
		if(cities.size() == 0)
		{
			try
			{
				BufferedReader reader = new BufferedReader(new InputStreamReader(getAssets().open("citydb.csv")));
				String line = reader.readLine();
				while(line != null)
				{
					StringTokenizer st = new StringTokenizer(line, ",");
					String name = st.nextToken();
					String tz = st.nextToken();
					String country = st.nextToken();
					String lat = st.nextToken();
					String lon = st.nextToken();
					st.nextToken();//skip the time zone description text
					cities.add(new CityObj(name, Double.parseDouble(tz), country, Double.parseDouble(lat), Double.parseDouble(lon)));
					line = reader.readLine();
				}
				reader.close();//close the stream once we're all done
				
				//save the arraylist to memory, saves time having to reload from disk again & again
				SharedPreferences.Editor editor = this.getPreferences(Context.MODE_PRIVATE).edit();
				gson =  new Gson();
				try
				{
					/*editor.putString("citiesList", cities.toString());
					editor.commit();*/
					String json2 = gson.toJson(cities);
					editor.putString("myJson", json2);
					editor.commit();
				}
				catch(Exception e)
				{
					System.out.println("An error has occured saving to json.");
					e.getStackTrace();
				}
			}
			catch(IOException e)
			{
				System.out.println("An error has occured reading from disk.");
				e.getMessage();
			}
		}
		//initialize the edittext
		searchBox = (EditText) findViewById(R.id.searchTextBox);
		searchBox.addTextChangedListener(this);
		//initialize the listview
		listView1 = (ListView) findViewById(R.id.listView1);
		listView1.setOnItemClickListener(this);
		//populate the listview with cities
		try
		{
			adapter = new ArrayAdapter<CityObj>(this, android.R.layout.simple_list_item_1, cities);
		}
		catch(Exception e)
		{
			System.out.println("Error creating an adapter for list view.");
		}
		
		
		
		//create a listener to listen to edit text changes (search queries)
		listView1.setAdapter(adapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.search_location, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings)
		{
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id)
	{
		
		CityObj chosenItem = (CityObj)parent.getItemAtPosition(position);

		String selectedCityString = gson.toJson(chosenItem);
		Intent intent = new Intent();
		intent.putExtra("chosenCity", selectedCityString);
		setResult(RESULT_OK, intent);
		finish();
	}
	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count)
	{
		this.adapter.getFilter().filter(s);
	}

	@Override
	public void afterTextChanged(Editable s)
	{
		// TODO Auto-generated method stub
		
	}
}
