package com.karimo.prayertimes;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.*;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.RingtonePreference;

import android.provider.Settings;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import android.text.TextUtils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
@SuppressWarnings({"deprecation", "unused"})
public class SettingsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener, LocationListener {
	/**
	 * Determines whether to always show the simplified settings UI, where
	 * settings are presented in a single list. When false, settings are shown
	 * as a master/detail two-pane view on tablets. When true, a single pane is
	 * shown on tablets.
	 */
	private static final boolean ALWAYS_SIMPLE_PREFS = false;
	public static final int MANUAL_SEARCH_SELECTED = 1;
	private static final int REQ_LOCATION_PERMISSION = 100;
	/*************************GLOBALS**************************/
	private Preference manSel;
	private Preference autoLoc;
	private CityObj myCity; //our city object, regardless if chosen manually/automatically
	private SharedPreferences sPreferences;
	private SharedPreferences radioSelectionPreferences;
	private static Gson gson;

	EditTextPreference latText;
	EditTextPreference lonText;
	EditTextPreference cityName;
	EditTextPreference tzText;

	CheckBoxPreference dstOpt;
	ListPreference calcSelect;
	ListPreference asrSelect;
	CheckBoxPreference notifsOpt;
	CheckBoxPreference adhanOpt;
	CheckBoxPreference fajrOpt;
	CheckBoxPreference dhuhrOpt;
	CheckBoxPreference asrOpt;
	CheckBoxPreference maghribOpt;
	CheckBoxPreference ishaOpt;

	public int calcMethod;
	public int asrMethod;

	private ArrayList<String> fragmentsList = new ArrayList<String>();

	LocationManager localizer;
	Location bestPosition;
	String[] perms = {Manifest.permission.ACCESS_FINE_LOCATION,
			Manifest.permission.ACCESS_COARSE_LOCATION};
	/**********************************************************/
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

		setupSimplePreferencesScreen();

		//load the labels for the location panels
		latText = (EditTextPreference) findPreference("lat");
		lonText = (EditTextPreference) findPreference("lon");
		cityName = (EditTextPreference) findPreference("cityname");
		tzText = (EditTextPreference) findPreference("timezone");

		radioSelectionPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		//load myCity
		sPreferences = PreferenceManager.getDefaultSharedPreferences(this);

		gson = new Gson();
		String json = null;
		try {
			json = sPreferences.getString("myJsonCity", "");
		} catch (Exception e) {
			System.out.println("Error retrieving data from memory.");
		}
		//ok, lets see if we're successful in loading from memory
		if (!json.isEmpty()) {
			Type t = new TypeToken<CityObj>() {
			}.getType();
			myCity = gson.fromJson(json, t);
			if (myCity != null) {
				latText.setSummary(Double.toString(myCity.latitude()));
				lonText.setSummary(Double.toString(myCity.longitude()));
				cityName.setSummary(myCity.city());
				tzText.setSummary(Double.toString(myCity.timeZone()));
			}
		}

		//get the list preferences

		calcSelect = (ListPreference) findPreference("calc_method_pref");
		asrSelect = (ListPreference) findPreference("asr_method_pref");

		//get dst check box preferences
		dstOpt = (CheckBoxPreference) findPreference("use_dst_option");

		//get notification check box preferences
		notifsOpt = (CheckBoxPreference) findPreference("enable_notifs");

		//get adhan check box preferences
		adhanOpt = (CheckBoxPreference) findPreference("enable_adhan");
		//get the rest of the check-box prefs
		fajrOpt = (CheckBoxPreference) findPreference("enable_fajrAdhan");
		dhuhrOpt = (CheckBoxPreference) findPreference("enable_dhuhrAdhan");
		asrOpt = (CheckBoxPreference) findPreference("enable_asrAdhan");
		maghribOpt = (CheckBoxPreference) findPreference("enable_maghribAdhan");
		ishaOpt = (CheckBoxPreference) findPreference("enable_ishaAdhan");
		//register the shared preference change listener
		radioSelectionPreferences.registerOnSharedPreferenceChangeListener(this);

		//ask for location permission
		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
				ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			ActivityCompat.requestPermissions(this, perms, REQ_LOCATION_PERMISSION);
		}
	}

	/**
	 * Shows the simplified settings UI if the device configuration if the
	 * device configuration dictates that a simplified, single-pane UI should be
	 * shown.
	 */
	private void setupSimplePreferencesScreen() {
		if (!isSimplePreferences(this)) {
			return;
		}


		// Add 'location' preferences.
		addPreferencesFromResource(R.xml.pref_location);

		manSel = (Preference) findPreference("manual_select");

		manSel.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
				Intent manSearch = new Intent(SettingsActivity.this, SearchLocationActivity.class);

				startActivityForResult(manSearch, 1); //start the activity but listen for a result (code = 1)

				return true;
			}

		});

		autoLoc = (Preference) findPreference("auto_detect");
		autoLoc.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
				getLocAuto();
				return false;
			}

		});


		// Add calc method preferences

		addPreferencesFromResource(R.xml.pref_calc_method);

		addPreferencesFromResource(R.xml.pref_notification);

		//addPreferencesFromResource(R.xml.pref_misc);

	}

	@Override
	protected void onResume() {
		super.onResume();

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == MANUAL_SEARCH_SELECTED)//we just got back from manual search (code = 1)
		{
			if (resultCode == RESULT_OK) {
				String selectedCity = data.getStringExtra("chosenCity");
				Type t = new TypeToken<CityObj>() {
				}.getType();
				myCity = gson.fromJson(selectedCity, t);
				//go check if dst is used
				updateSelection("use_dst_option");
				//set the labels and text for the location panels
				latText.setSummary(Double.toString(myCity.latitude()));
				lonText.setSummary(Double.toString(myCity.longitude()));
				cityName.setSummary(myCity.city());
				tzText.setSummary(Double.toString(myCity.timeZone()));
				latText.setText(Double.toString(myCity.latitude()));
				lonText.setText(Double.toString(myCity.longitude()));
				cityName.setText(myCity.city());
				tzText.setText(Double.toString(myCity.timeZone()));


				//save the new myCity
				//finally, save our city object for retrieval later on
				SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
				gson = new Gson();
				try {
					String json2 = gson.toJson(myCity);
					editor.putString("myJsonCity", json2);
					editor.commit();
				} catch (Exception e) {
					System.out.println("An error has occured saving to json.");
					e.getStackTrace();
				}
			}
		}
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences spf, String key) {
		updateSelection(key);
	}

	private void updateSelection(String key) {
		if (key != null) {
			/*if(key.contentEquals("wall_select_option"))
			{
				//change the wallpaper of MainScreen
				updateWallpaper();
			}*/
			/*if(key.contentEquals("lang_select_option"))
			{
				//the language
				updateLanguage();
			}*/
			if (key.contentEquals("calc_method_pref")) {
				//the calculation method
				updateCalcMethod();
			}
			if (key.contentEquals("asr_method_pref")) {
				//the asr method of calculation
				updateAsrMethod();
			}
			if (key.contentEquals("use_dst_option")) {
				updateDst();
			}
			if (key.contentEquals("enable_notifs")) {
				updateNotifs();
			}
			if (key.contentEquals("enable_adhan")) {
				updateAdhan();
			}
			if (key.contentEquals("enable_fajrAdhan")) {
				updateFajrAdhan();
			}
			if (key.contentEquals("enable_dhuhrAdhan")) {
				updateDhuhrAdhan();
			}
			if (key.contentEquals("enable_asrAdhan")) {
				updateAsrAdhan();
			}
			if (key.contentEquals("enable_maghribAdhan")) {
				updateMaghribAdhan();
			}
			if (key.contentEquals("enable_ishaAdhan")) {
				updateIshaAdhan();
			}
		}
	}

	private void updateDst() {
		boolean dstCheck = dstOpt.isChecked();
		if (dstCheck) {
			Calendar cal = Calendar.getInstance();

			if (cal.get(Calendar.DST_OFFSET) > 0) {
				myCity.setTimeZone(myCity.timeZone() + 1);
				//should save/overwrite?
			}
		}
		SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
		try {
			editor.putBoolean("myDstOpt", dstCheck);
			editor.commit();
		} catch (Exception e) {
			System.out.println("An error has occured saving to json.");
			e.getStackTrace();
		}
	}

	private void getLocAuto() {
		/*setup the location manager and providers first, get needed permissions
		then scan for most accurate location and set it to bestPosition
		initially we will use getLastKnownLocation() but in the case it returns null
		this SettingsActivity implements LocationListener so if bestPosition is null
		we call requestSingleUpdate
		otherwise, we have a location, call assignCity()*/
		localizer = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		String provider = LocationManager.GPS_PROVIDER;
		List<String> providers = localizer.getProviders(true);
		bestPosition = null;

		//if we need location services enabled in settings (or no internet)
		if (!localizer.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
			Toast.makeText(getApplicationContext(), "Enable network services!", Toast.LENGTH_LONG).show();
			startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), 10);
		}

		//if needing location services switched on
		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
				ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			ActivityCompat.requestPermissions(this, perms, REQ_LOCATION_PERMISSION);
		}
		//scan for most accurate location
		Location current = null;
		for(String s : providers) {
			//try this first
			current = localizer.getLastKnownLocation(s);
			if(bestPosition == null && current != null ||
					(bestPosition != null && current != null &&
							bestPosition.getAccuracy() < current.getAccuracy())) {
				bestPosition = current;
			}
		}

		if(bestPosition == null) {
			//prompt OS to request a location update
			localizer.requestSingleUpdate(provider, this, null);
		}
		else {
			assignCity();
		}
	}
	private void assignCity() {
		//if we have a non-null Location
		//use the Runnable class to use Geocoder on its own thread
		//to get the city from the provided lat/lon
		//then bundle everything into a City object and save to SharedPrefs
		if(bestPosition != null) {
			//get the lat/long
			double latitude = bestPosition.getLatitude();
			double longitude = bestPosition.getLongitude();
			/*****************************************************************************************/
			//get the city, moved to a separate class
			StringBuilder builder = new StringBuilder();
			String city = null;

			GeocoderThread geocoderThread = new GeocoderThread(latitude, longitude, this);

			try {
				city = geocoderThread.execute().get();
			} catch (ExecutionException e) {
				throw new RuntimeException(e);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
			/****************************************************************************************/
			//get the timezone
			TimeZone tz = Calendar.getInstance().getTimeZone();
			double offset = (tz.getRawOffset() / 60000) / 60;
			Calendar cal = Calendar.getInstance();
			if(cal.get(Calendar.DST_OFFSET) > 0)
			{
				offset = offset + 1;
			}
			/****************************************************************************************/
			//create a new city from our location obtained
			myCity = new CityObj(city, offset, city, latitude, longitude);

			//update the setting fields
			latText.setSummary(Double.toString(myCity.latitude()));
			latText.setText(Double.toString(myCity.latitude()));

			lonText.setSummary(Double.toString(myCity.longitude()));
			lonText.setText(Double.toString(myCity.longitude()));

			cityName.setSummary(myCity.city());
			cityName.setText(myCity.city());

			tzText.setSummary(Double.toString(myCity.timeZone()));
			tzText.setText(Double.toString(myCity.timeZone()));
		}

		//finally, save our city object for retrieval later on
		SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
		gson =  new Gson();
		try
		{
			String json2 = gson.toJson(myCity);
			editor.putString("myJsonCity", json2);
			editor.commit();
		}
		catch(Exception e)
		{
			System.out.println("An error has occured saving to json.");
			e.getStackTrace();
		}
	}
	private void updateLanguage() 
	{
		/*String selectedOption = langSelect.getValue();
		int index = langSelect.findIndexOfValue(selectedOption);
		switch(index)
		{
		case 0:
			localeString = "en_GB";
			break;
		case 1:
			localeString = "ar_DZ";
			break;
		case 2:
			localeString = "fr_FR";
			break;
		case 3:
			localeString = "tr_TR";
			break;
		case 4:
			localeString = "es_ES";
			break;
		default:
			localeString = "en_GB";
		}*/
	}
	
	private void updateCalcMethod()
	{
		String selectedOption = calcSelect.getValue();
		int index = calcSelect.findIndexOfValue(selectedOption);
		switch(index)
		{
		case 0:
			calcMethod = 0;
			break;
		case 1:
			calcMethod = 1;
			break;
		case 3:
			calcMethod = 3;
			break;
		case 4:
			calcMethod = 4;
			break;
		case 5:
			calcMethod = 5;
			break;
		case 6:
			calcMethod = 6;
			break;
		default:
			calcMethod = 2;
			break;
		}
		//save it
		SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
		gson =  new Gson();
		try
		{
			String json2 = gson.toJson(calcMethod);
			editor.putString("myCalcMethod", json2);
			editor.commit();
		}
		catch(Exception e)
		{
			System.out.println("An error has occured saving calc method to json.");
			e.getStackTrace();
		}
		return;
	}
	private void updateAsrMethod()
	{
		String selectedOption = asrSelect.getValue();
		int index = asrSelect.findIndexOfValue(selectedOption);
		switch(index)
		{
		case 1:
			asrMethod = 1;
			break;
		default:
			asrMethod = 0;
			break;
		}
		//save it
		SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
		gson =  new Gson();
		try
		{
			String json2 = gson.toJson(asrMethod);
			editor.putString("myAsrMethod", json2);
			editor.commit();
		}
		catch(Exception e)
		{
			System.out.println("An error has occured saving asr method to json.");
			e.getStackTrace();
		}
		return;
	}
	private void updateNotifs()
	{
		boolean notifsCheck = notifsOpt.isChecked();

		SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
		try
		{
			editor.putBoolean("myNotifsOpt", notifsCheck);
			editor.commit();
		}
		catch(Exception e)
		{
			System.out.println("An error has occured saving notifs option.");
			e.getStackTrace();
		}
	}
	private void updateAdhan()
	{
		boolean adhanCheck = adhanOpt.isChecked();

		SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
		try
		{
			editor.putBoolean("myAdhanOpt", adhanCheck);
			editor.commit();
		}
		catch(Exception e)
		{
			System.out.println("An error has occured saving adhan option");
			e.getStackTrace();
		}
	}
	private void updateFajrAdhan()
	{
		boolean fajrAdhanCheck = fajrOpt.isChecked();

		SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
		try
		{
			editor.putBoolean("myFajrAdhanOpt", fajrAdhanCheck);
			editor.commit();
		}
		catch(Exception e)
		{
			System.out.println("An error has occured saving adhan option");
			e.getStackTrace();
		}
	}
	private void updateDhuhrAdhan()
	{
		boolean dhuhrAdhanCheck = dhuhrOpt.isChecked();

		SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
		try
		{
			editor.putBoolean("myDhuhrAdhanOpt", dhuhrAdhanCheck);
			editor.commit();
		}
		catch(Exception e)
		{
			System.out.println("An error has occured saving adhan option");
			e.getStackTrace();
		}
	}
	private void updateAsrAdhan()
	{
		boolean asrAdhanCheck = asrOpt.isChecked();

		SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
		try
		{
			editor.putBoolean("myAsrAdhanOpt", asrAdhanCheck);
			editor.commit();
		}
		catch(Exception e)
		{
			System.out.println("An error has occured saving adhan option");
			e.getStackTrace();
		}
	}
	private void updateMaghribAdhan()
	{
		boolean maghribAdhanCheck = maghribOpt.isChecked();

		SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
		try
		{
			editor.putBoolean("myMaghribAdhanOpt", maghribAdhanCheck);
			editor.commit();
		}
		catch(Exception e)
		{
			System.out.println("An error has occured saving adhan option");
			e.getStackTrace();
		}
	}
	private void updateIshaAdhan()
	{
		boolean ishaAdhanCheck = ishaOpt.isChecked();

		SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
		try
		{
			editor.putBoolean("myIshaAdhanOpt", ishaAdhanCheck);
			editor.commit();
		}
		catch(Exception e)
		{
			System.out.println("An error has occured saving adhan option");
			e.getStackTrace();
		}
	}
	/** {@inheritDoc} */
	@Override
	public boolean onIsMultiPane()
	{
		return isXLargeTablet(this) && !isSimplePreferences(this);
	}
	@Override
	protected boolean isValidFragment(String fragmentName) //for tablets 10 inches and above
	{
		return fragmentsList.contains(fragmentName);
	}
	/**
	 * Helper method to determine if the device has an extra-large screen. For
	 * example, 10" tablets are extra-large.
	 */
	private static boolean isXLargeTablet(Context context)
	{
		return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
	}

	/**
	 * Determines whether the simplified settings UI should be shown. This is
	 * true if this is forced via {@link #ALWAYS_SIMPLE_PREFS}, or the device
	 * doesn't have newer APIs like {@link PreferenceFragment}, or the device
	 * doesn't have an extra-large screen. In these cases, a single-pane
	 * "simplified" settings UI should be shown.
	 */
	private static boolean isSimplePreferences(Context context)
	{
		return ALWAYS_SIMPLE_PREFS
				|| Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB
				|| !isXLargeTablet(context);
	}

	/** {@inheritDoc} */
	@Override
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void onBuildHeaders(List<Header> target)
	{
		if (!isSimplePreferences(this))
		{
			loadHeadersFromResource(R.xml.pref_headers, target);
		}
		for(Header h:target)
		{
			fragmentsList.add(h.fragment);
		}
	}

	/**
	 * A preference value change listener that updates the preference's summary
	 * to reflect its new value.
	 */
	private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
		@Override
		public boolean onPreferenceChange(Preference preference, Object value)
		{
			String stringValue = value.toString();

			if (preference instanceof ListPreference)
			{
				// For list preferences, look up the correct display value in
				// the preference's 'entries' list.
				ListPreference listPreference = (ListPreference) preference;
				int index = listPreference.findIndexOfValue(stringValue);

				// Set the summary to reflect the new value.
				preference
						.setSummary(index >= 0 ? listPreference.getEntries()[index]
								: null);

			} 
			else if (preference instanceof RingtonePreference)
			{
				// For ringtone preferences, look up the correct display value
				// using RingtoneManager.
				if (TextUtils.isEmpty(stringValue))
				{
					// Empty values correspond to 'silent' (no ringtone).
					preference.setSummary(R.string.pref_ringtone_silent);

				} 
				else
				{
					Ringtone ringtone = RingtoneManager.getRingtone(
							preference.getContext(), Uri.parse(stringValue));

					if (ringtone == null)
					{
						// Clear the summary if there was a lookup error.
						preference.setSummary(null);
					} else
					{
						// Set the summary to reflect the new ringtone display
						// name.
						String name = ringtone
								.getTitle(preference.getContext());
						preference.setSummary(name);
					}
				}

			} 
			else
			{
				// For all other preferences, set the summary to the value's
				// simple string representation.
				preference.setSummary(stringValue);
			}
			return true;
		}
	};

	/**
	 * Binds a preference's summary to its value. More specifically, when the
	 * preference's value is changed, its summary (line of text below the
	 * preference title) is updated to reflect the value. The summary is also
	 * immediately updated upon calling this method. The exact display format is
	 * dependent on the type of preference.
	 * 
	 * @see #sBindPreferenceSummaryToValueListener
	 */
	private static void bindPreferenceSummaryToValue(Preference preference)
	{
		// Set the listener to watch for value changes.
		preference
				.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

		// Trigger the listener immediately with the preference's
		// current value.
		sBindPreferenceSummaryToValueListener.onPreferenceChange(
				preference,
				PreferenceManager.getDefaultSharedPreferences(
						preference.getContext()).getString(preference.getKey(),
						""));
	}

	@Override
	public void onLocationChanged(@NonNull Location location) {
		bestPosition = location;
		assignCity();
	}


	/**
	 * This fragment shows general preferences only. It is used when the
	 * activity is showing a two-pane settings UI.
	 */
	@TargetApi(Build.VERSION_CODES.KITKAT)
	public static class GeneralPreferenceFragment extends PreferenceFragment
	{
		@Override
		public void onCreate(Bundle savedInstanceState)
		{
			super.onCreate(savedInstanceState);
		}
	}
}
/*Settings
 * GROUP: LOCATION
 * auto-detect - 3 textboxes, 1 drop down, 1 button
 * choose city from list - give its own activity, text box, listview
 * 
 * GROUP: CALCULATION METHOD
 * change calc method - list of radio buttons - 
 * asr calc - list of radio buttons (shafii, hanbali, maliki)(hanifi)
 * prayer times adjustments - 5 drop-downs, default entry is 0, have negative and positive up to 10 minutes
 * 
 * GROUP: NOTIFICATIONS
 * enable notifications (play notification sound @ prayer time) - checkbox
 * enable vibration - checkbox
 * show notification icon - checkbox
 * enable azan - radio buttons group
 * enable silence after azan
 * 
 * GROUP: MISC
 * language - radio buttons (2) english/arabic
 */