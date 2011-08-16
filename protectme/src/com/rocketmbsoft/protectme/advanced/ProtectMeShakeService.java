

package com.rocketmbsoft.protectme.advanced;


import java.util.concurrent.Semaphore;

import com.rocketmbsoft.protectme.advanced.R;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;


/**
 * This is an example of implementing an application service that runs locally
 * in the same process as the application.  The {@link LocalServiceController}
 * and {@link LocalServiceBinding} classes show how to interact with the
 * service.
 *
 * <p>Notice the use of the {@link NotificationManager} when interesting things
 * happen in the service.  This is generally how background services should
 * interact with the user, rather than doing something more disruptive such as
 * calling startActivity().
 */
public class ProtectMeShakeService extends Service  {
	private NotificationManager mNM;

	private SensorManager mSensorManager;  

	private static double MAX_SHAKE;
	private SharedPreferences sharedPref;
	private int sensitivity;
	public static int shakes;

	final Semaphore lock = new Semaphore(1);
	int count = 0;
	private static boolean startedAlert = false;
	private static MediaPlayer mMediaPlayer;



	private SensorEventListener mSensorListener = new SensorEventListener() { 

		@Override
		public void onSensorChanged(SensorEvent event) {
			
			double forceThreshHold = 0; 
			double totalForce = 0; 

			totalForce += Math.pow(event.values[SensorManager.DATA_X]/SensorManager.GRAVITY_EARTH, 2.0); 
			totalForce += Math.pow(event.values[SensorManager.DATA_Y]/SensorManager.GRAVITY_EARTH, 2.0); 
			totalForce += Math.pow(event.values[SensorManager.DATA_Z]/SensorManager.GRAVITY_EARTH, 2.0); 
			totalForce = Math.sqrt(totalForce) - 1.0; 

			forceThreshHold = (double) (((100 - sensitivity) * 0.01) * MAX_SHAKE) + 0.0001;

			if (totalForce > forceThreshHold) {

				mMediaPlayer.start();
				
				if (! ProtectMeTimer.maxHits()) {

					return;
				}
				
				if (lock.tryAcquire()) {
					
					if (! startedAlert) {

						if (Config.D) Log.d("SensorShake Algorithm", "**************Sensor value : "+totalForce+ ", Force Threshold : "+forceThreshHold);

						mSensorManager.unregisterListener(this);

						// When the button is clicked, launch an activity through this intent
						Intent launchPreferencesIntent = new Intent().setClass(ProtectMeShakeService.this, ProtectMeAlertActivity.class);

						launchPreferencesIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

						startedAlert = true;
						
						mMediaPlayer.start();

						startActivity(launchPreferencesIntent);

						ProtectMeShakeService.this.stopSelf();
					}
				}
			}
		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			// TODO Auto-generated method stub

		} 
	};

	/**
	 * Class for clients to access.  Because we know this service always
	 * runs in the same process as its clients, we don't need to deal with
	 * IPC.
	 */
	public class LocalBinder extends Binder {
		ProtectMeShakeService getService() {
			return ProtectMeShakeService.this;
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();
		
		if (Config.D) Log.d("ProtectMeShakeService::onCreate","Entered");
		
		mMediaPlayer = MediaPlayer.create(this, R.raw.beep);
		mMediaPlayer.setLooping(false);
		
		mMediaPlayer.start();

		// PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

		sharedPref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

		sensitivity = sharedPref.getInt("int_sensitivity", 0);
		shakes = getShakes();

		try {
			MAX_SHAKE = Float.parseFloat(sharedPref.getString("et_force_threshold", "4.000000f"));
		} catch (Exception e) {
			Log.e("Caught Exception","Parsing threshold preferrence : "+e.getMessage());
			e.printStackTrace();
			MAX_SHAKE = 4.000000f;
		}

		mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE); 

		mSensorManager.registerListener(mSensorListener, 
				mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), 
				SensorManager.SENSOR_DELAY_FASTEST);

		// Display a notification about us starting.  We put an icon in the status bar.
		showNotification();
	}

	private int getShakes() {
		String s = sharedPref.getString("seconds_of_shake", "four");
		int ret = 4;
		
		if (s.equalsIgnoreCase("one")) {
			ret = 1;
		} else if (s.equalsIgnoreCase("two")) {
			ret = 2;
		} else if (s.equalsIgnoreCase("three")) {
			ret = 3;
		} else if (s.equalsIgnoreCase("four")) {
			ret = 4;
		} else if (s.equalsIgnoreCase("five")) {
			ret = 5;
		} else if (s.equalsIgnoreCase("six")) {
			ret = 6;
		} else if (s.equalsIgnoreCase("seven")) {
			ret = 7;
		} else if (s.equalsIgnoreCase("eight")) {
			ret = 8;
		} else if (s.equalsIgnoreCase("nine")) {
			ret = 9;
		} else if (s.equalsIgnoreCase("ten")) {
			ret = 10;
		}
		
		return ret;
	}

	protected boolean inThreatZone() {
		// TODO Auto-generated method stub
		return true;
	}

	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i("LocalService", "Received start id " + startId + ": " + intent);
		Toast.makeText(this, R.string.local_shake_service_started, Toast.LENGTH_SHORT).show();
		// We want this service to continue running until it is explicitly
		// stopped, so return sticky.
		return 1;
	}



	@Override
	public void onDestroy() {
		
		if (Config.D) Log.d("ProtectMeShakeService::onDestroy","Entered");
		
		super.onDestroy();
		
		// Cancel the persistent notification.
		mNM.cancel(R.string.local_shake_service_started);
		//
		mSensorManager.unregisterListener(mSensorListener);

		lock.release();

		startedAlert = false;
		
		mMediaPlayer.stop();
		mMediaPlayer.release();
		mMediaPlayer = null;
		
		// Tell the user we stopped.
		Toast.makeText(this, R.string.local_shake_service_stopped, Toast.LENGTH_SHORT).show();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	// This is the object that receives interactions from clients.  See
	// RemoteService for a more complete example.
	private final IBinder mBinder = new LocalBinder();

	/**
	 * Show a notification while this service is running.
	 */
	private void showNotification() {
		// In this sample, we'll use the same text for the ticker and the expanded notification
		CharSequence text = getText(R.string.local_shake_service_started);

		// Set the icon, scrolling text and timestamp
		Notification notification = new Notification(R.drawable.protectme, text,
				System.currentTimeMillis());

		// The PendingIntent to launch our activity if the user selects this notification
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				new Intent(this, ProtectMeActivity.class), 0);

		// Set the info for the views that show in the notification panel.
		notification.setLatestEventInfo(this, getText(R.string.local_service_label),
				text, contentIntent);

		// Send the notification.
		// We use a layout id because it is a unique number.  We use it later to cancel.
		mNM.notify(R.string.local_shake_service_started, notification);
	}
}

