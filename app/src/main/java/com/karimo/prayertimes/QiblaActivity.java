package com.karimo.prayertimes;

import java.util.List;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import androidx.core.app.ActivityCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


public class QiblaActivity extends Activity implements SensorEventListener, ActivityCompat.OnRequestPermissionsResultCallback {
	//define the display assembly compass picture
	private ImageView compass;

	//define the qibla arrow picture
	private ImageView qiblaPointer;

	//record the compass picture angle turned
	private float currentDegree = 0f;

	//keep track of the qibla bearing
	private double bearing;

	//device sensor manager
	private SensorManager sensorManager;
	private Sensor magnetometer;
	private Sensor accelerometer;
	float azimut;
	float degree;
	float[] mGravity;
	float[] mGeomagnetic;
	static final float ALPHA = 0.25f; // if ALPHA = 1 OR 0, no filter applies.
	TextView compHeadingText;

	private final double MAKKAH_LAT = 21.422510;
	private final double MAKKAH_LON = 39.826168;
	Location makkah;
	Location bestPosition;
	private static final int REQ_LOCATION_PERMISSION = 100;
	String[] perms = {Manifest.permission.ACCESS_FINE_LOCATION,
			Manifest.permission.ACCESS_COARSE_LOCATION};
	@SuppressLint("SourceLockedOrientationActivity")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_qibla);
		//first thing, ask for location permission (if not available or set)
		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
				ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			ActivityCompat.requestPermissions(this, perms, REQ_LOCATION_PERMISSION);
		}
		makkah = new Location("Makkah");
		makkah.setLatitude(MAKKAH_LAT);
		makkah.setLongitude(MAKKAH_LON);

		bearing = getBearing();
		//require portrait view only
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		compass = (ImageView) findViewById(R.id.imgViewCompass);
		compass.setRotation(0);

		qiblaPointer = (ImageView) findViewById(R.id.qibla_arrow);
		//set the arrow to point to the bearing heading
		qiblaPointer.setRotation((float) bearing);

		compHeadingText = (TextView) findViewById(R.id.comp_heading);

		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
	}
	@Override
	protected void onResume() {
		super.onResume();
		//for the system's orientation sensor registered listeners
		sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
		sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_GAME);
	}

	@Override
	protected void onPause() {
		super.onPause();
		//to stop the listener & save battery
		sensorManager.unregisterListener(this, accelerometer);
		sensorManager.unregisterListener(this, magnetometer);
	}
	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
			mGravity = lowPass(event.values.clone(), mGravity);
		if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
			mGeomagnetic = lowPass(event.values.clone(), mGeomagnetic);
		if (mGravity != null && mGeomagnetic != null) {
			float R[] = new float[9];
			float I[] = new float[9];
			boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
			if (success) {
				float orientation[] = new float[3];
				SensorManager.getOrientation(R, orientation);
				azimut = orientation[0]; // orientation contains: azimut, pitch and roll

				//convert azimut radians to degrees
				degree = Math.round((float)Math.toDegrees(azimut));
				if (degree < 0.0f) {
					degree += 360f;
				}
				compHeadingText.setText("Heading: " + Float.toString(degree) + " deg" + " | " + "Qibla: " + Float.toString((float) Math.floor(bearing)) + " deg");

				RotateAnimation ra = new RotateAnimation(currentDegree, -degree, Animation.RELATIVE_TO_SELF,
						0.5f, Animation.RELATIVE_TO_SELF, 0.5f);

				//how long the animation will take
				ra.setDuration(210);

				//set the animation after the end of the reservation status
				ra.setFillAfter(true);

				//start the animation
				compass.startAnimation(ra);
				qiblaPointer.startAnimation(ra);
				currentDegree = -degree;
			}
		}

	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) { }
	private float[] lowPass(float[] input, float[] output) {
		if ( output == null ) return input;

		for ( int i=0; i<input.length; i++ ) {
			output[i] = output[i] + ALPHA * (input[i] - output[i]);
		}
		return output;
	}
	private double getBearing() {
		//later on, just grab our current location from settings, reduce work
		LocationManager localizer = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		String provider = LocationManager.GPS_PROVIDER;
		List<String> providers = localizer.getProviders(true);
		String[] perms = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

		//if we need location services enabled in settings (or no internet)
		if (!localizer.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
			Toast.makeText(getApplicationContext(), "Enable network services!", Toast.LENGTH_LONG).show();
			startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), 10);
		}
		//for Oreo and up
		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
				ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			ActivityCompat.requestPermissions(this, perms, REQ_LOCATION_PERMISSION);
		}


		bestPosition = localizer.getLastKnownLocation(provider);
		if(bestPosition == null) {
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
		}
		return bestPosition.bearingTo(makkah);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.qibla, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		/*int id = item.getItemId();
		if (id == R.id.action_settings)
		{
			return true;
		}
		return super.onOptionsItemSelected(item);*/
		return true;
	}
}
