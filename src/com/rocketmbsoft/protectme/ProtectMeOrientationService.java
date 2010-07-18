

package com.rocketmbsoft.protectme;


import com.rocketmbsoft.protectme.R;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.OrientationEventListener;
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
public class ProtectMeOrientationService extends Service  {
	private NotificationManager mNM;

	private SharedPreferences sharedPref;

	int y, currentAngle;

	OrientationEventListener mOrientationListener = null;

	private int anglePref;	

	private static final String TAG = "ProtectMeOrientationService";

	/**
	 * Class for clients to access.  Because we know this service always
	 * runs in the same process as its clients, we don't need to deal with
	 * IPC.
	 */
	public class LocalBinder extends Binder {
		ProtectMeOrientationService getService() {
			return ProtectMeOrientationService.this;
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();

		if (Config.D) Log.d(TAG ,"onCreate - Entered");

		sharedPref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

		mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

		anglePref = sharedPref.getInt(Config.ANGLE_PREFERENCE, 90);

		
		mOrientationListener = new OrientationEventListener(this) {

			@Override
			public void onOrientationChanged(int orientation) {
				
				if (Config.D) Log.d(TAG, "Orientation changed : "+orientation);
				
				y = (360 - orientation);

				if (y >= 180) {
					currentAngle = y - 180;
				} else {
					currentAngle = y;
				}
				
				if (orientation > 180) {
					currentAngle = 180 - currentAngle;
				}
				
				currentAngle = Math.abs(currentAngle - 180);

				if (currentAngle > anglePref) {
					if (Config.D)Log.d(TAG, "Acitvated by orientation angle"); 
					
					mOrientationListener.disable();

					// When the button is clicked, launch an activity through this intent
					Intent launchPreferencesIntent = new Intent().setClass(
							ProtectMeOrientationService.this, ProtectMeAlertActivity.class);

					launchPreferencesIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

					startActivity(launchPreferencesIntent);

					ProtectMeOrientationService.this.stopSelf();
				}
			}
		};
		
		mOrientationListener.enable();

		// Display a notification about us starting.  We put an icon in the status bar.
		showNotification();
	}


	protected boolean inThreatZone() {
		// TODO Auto-generated method stub
		return true;
	}

	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i(TAG, "Received start id " + startId + ": " + intent);
		Toast.makeText(this, R.string.local_orientation_service_started, Toast.LENGTH_SHORT).show();
		// We want this service to continue running until it is explicitly
		// stopped, so return sticky.
		return 1;
	}



	@Override
	public void onDestroy() {

		if (Config.D) Log.d(TAG,"onDestroy - Entered");

		super.onDestroy();

		// Cancel the persistent notification.
		mNM.cancel(R.string.local_orientation_service_started);

		mOrientationListener.disable();
		mOrientationListener  = null;

		// Tell the user we stopped.
		Toast.makeText(this, R.string.local_orientation_service_stopped, Toast.LENGTH_SHORT).show();
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
		CharSequence text = getText(R.string.local_orientation_service_started);

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
		mNM.notify(R.string.local_orientation_service_started, notification);
	}
}

