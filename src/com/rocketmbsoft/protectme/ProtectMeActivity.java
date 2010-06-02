package com.rocketmbsoft.protectme;



import com.rocketmbsoft.protectme.R;

import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class ProtectMeActivity extends Activity implements OnClickListener {
	private ImageButton btnMainContinuous;
	private ImageButton btnMainPreferences;
	private ImageButton btnStopServices;
	private Bitmap startDisabledBitmap;
	private Bitmap stopDisabledBitmap;
	private Bitmap startEnabledBitmap;
	private Bitmap stopEnabledBitmap;
	private ImageView tvTitle;
	private TextView tvStatus;
	AlertDialog.Builder builder;
	AlertDialog alert;
	boolean shakeServiceIsRunning = false;
	boolean orientationServiceIsRunning = false;
	private static final int REQUEST_CODE_PREFERENCES = 0;
	private ActivityManager mActivityManager;
	
	private static final boolean D = false;
	private static final String TAG = "ProtectMeActivity";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		if (D) Log.d(TAG, "onCreate Entered");

		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
		
		builder = new AlertDialog.Builder(this);
		builder.setMessage("Are you sure you want to exit?")
		.setCancelable(false)
		.setNeutralButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		});
		
		alert = builder.create();

		mActivityManager = (ActivityManager)getSystemService(ACTIVITY_SERVICE);

		btnMainContinuous = (ImageButton) findViewById(R.id.ibtn_start);
		tvStatus   = (TextView) findViewById(R.id.TextView01);
		btnStopServices   = (ImageButton) findViewById(R.id.ibtn_stop);

		tvTitle           = (ImageView) findViewById(R.id.iv_title);
		startDisabledBitmap  = (Bitmap) BitmapFactory.decodeResource(
				getResources(), R.drawable.startdisabled);

		stopDisabledBitmap  = (Bitmap) BitmapFactory.decodeResource(
				getResources(), R.drawable.stopdisabled);

		startEnabledBitmap  = (Bitmap) BitmapFactory.decodeResource(
				getResources(), R.drawable.start);

		stopEnabledBitmap  = (Bitmap) BitmapFactory.decodeResource(
				getResources(), R.drawable.stop);

		btnMainContinuous.setOnClickListener(new OnClickListener() {
			public void onClick(View v)
			{

				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
				
				if (prefs.getString("contact_phone", "").equals("")) {
					alert.setMessage("Please set your contact within the setup, then start ProtectMe");
					alert.show();
					
					return;
				}

				if (prefs.getString("et_challenge_phrase", "").equals("")) {
					alert.setMessage("Please set your challenge word within the setup, then start ProtectMe");
					alert.show();
			
					return;
				}

				// Make sure the service is started.  It will continue running
				// until someone calls stopService().  The Intent we use to find
				// the service explicitly specifies our service component, because
				// we want it running in our own process and don't want other
				// applications to replace it.
				startService(new Intent(ProtectMeActivity.this,
						ProtectMeShakeService.class));

				checkServices();
			}
		});

		btnStopServices.setOnClickListener(new OnClickListener() {
			public void onClick(View v)
			{

				// Cancel a previous call to startService().  Note that the
				// service will not actually stop at this point if there are
				// still bound clients.
				stopService(new Intent(ProtectMeActivity.this,
						ProtectMeShakeService.class));

				checkServices();
			}
		});

		btnMainPreferences = (ImageButton) findViewById(R.id.ibtn_setup);

		btnMainPreferences.setOnClickListener(this);

		checkServices();

	}

	void checkServices() {

		shakeServiceIsRunning = false;

		List<RunningServiceInfo> runningServices = mActivityManager.getRunningServices(100);

		for (int i = 0; i < runningServices.size(); i++) {
			RunningServiceInfo ri = runningServices.get(i);

			if (ri.service.getClassName().equals(ProtectMeShakeService.class.getName()) ||
					ri.service.getClassName().equals(ProtectMeOrientationService.class.getName())) {
				shakeServiceIsRunning = true;
				break;
			}
		}

		if (shakeServiceIsRunning) {
			btnMainContinuous.setEnabled(false);
			btnMainContinuous.setImageBitmap(startDisabledBitmap);

			btnStopServices.setEnabled(true);
			btnStopServices.setImageBitmap(stopEnabledBitmap);

			tvStatus.setText("Protect Me Service is Running");
			//tvStatus.setTextColor(R.color.solid_green);
		} else {
			btnMainContinuous.setEnabled(true);
			btnMainContinuous.setImageBitmap(startEnabledBitmap);

			btnStopServices.setEnabled(false);
			btnStopServices.setImageBitmap(stopDisabledBitmap);

			tvStatus.setText("Protect Me Service is Stopped");
			// tvStatus.setTextColor(R.color.solid_red);
		}
	}


	@Override
	public void onClick(View v) {

		// When the button is clicked, launch an activity through this intent
		Intent launchPreferencesIntent = new Intent().setClass(this, ProtectMePreferences.class);

		// Make it a subactivity so we know when it returns
		startActivityForResult(launchPreferencesIntent, REQUEST_CODE_PREFERENCES);
	}

	public void onResmue() {
		if (D) Log.d("onResmue","******** Calling onStart");
		super.onResume();
		checkServices();
	}

	public void onRestart() {
		if (D) Log.d("onRestart","******** Calling onStart");
		super.onRestart();
		checkServices();
	}


	public void onRestoreInstanceState(Bundle savedInstanceState) {
		if (D) Log.d("onRestoreInstanceState","******** Calling onStart");
		super.onRestoreInstanceState(savedInstanceState);
		checkServices();
	}

	public void onStart() {
		if (D) Log.d("ProtectMeActivity::onStart","Entered");
		super.onStart();
		// checkServices();
	}
	
	/* Creates the menu items */
	public boolean onCreateOptionsMenu(Menu menu) {
	    menu.add(0, 1234, 0, "Protect Me Users Manual");
	    return true;
	}

	/* Handles item selections */
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    case 1234:
			Intent launchPreferencesIntent = new Intent().setClass(this, ProtectMeSumActivity.class);

			// Make it a subactivity so we know when it returns
			startActivity(launchPreferencesIntent);
	    	
	        return true;
	    }
	    return false;
	}
	
	public void onDestroy() {
		if (D) Log.d("ProtectMeActivity::onDestroy","Entered");
		super.onDestroy();
		
		btnMainContinuous = null;
		btnMainPreferences = null;
		btnStopServices = null;
		startDisabledBitmap = null;
		stopDisabledBitmap = null;
		startEnabledBitmap = null;
		stopEnabledBitmap = null;
		tvTitle = null;
		tvStatus = null;
		builder = null;
		alert = null;
		mActivityManager = null;
	}
}

