package com.rocketmbsoft.protectme;

import java.util.HashMap;
import java.util.List;

import android.app.Service;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioManager;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

public class ProtectMeSpeakService extends Service implements  TextToSpeech.OnInitListener {

	private TextToSpeech mTts;
	private static final boolean D = true;
	private static final String TAG = "ProtectMeSpeakService";

	private boolean offhook = false;
	private boolean idle = false;


	private PhoneStateListener mPhoneListener = new PhoneStateListener()
	{
		public void onCallStateChanged(int state, String incomingNumber)
		{
			switch (state)
			{
			case TelephonyManager.CALL_STATE_RINGING:
				if (D) Log.d(TAG, "CALL_STATE_RINGING : "+incomingNumber);
				break;
			case TelephonyManager.CALL_STATE_OFFHOOK:
				if (D) Log.d(TAG, "CALL_STATE_OFFHOOK : "+incomingNumber);
				offhook = true;

				if (offhook && idle) {
					if (D) Log.d(TAG, "Call In Progess ... ");

					speakPhrase();
				}
				break;
			case TelephonyManager.CALL_STATE_IDLE:
				if (D) Log.d(TAG, "CALL_STATE_IDLE : "+incomingNumber);
				idle = true;

				if (offhook && idle) {
					if (D) Log.d(TAG, "Call Is Disconnected ...");
				}
				break;
			default:
				if (D) Log.d(TAG, "Unknown phone state=" + state);
			}
		}
	};
	private AudioManager mAudioManager;

	@Override
	public void onCreate() {
		try {
			mTts = new TextToSpeech(this,
					this  // TextToSpeech.OnInitListener
			);

		} catch(ActivityNotFoundException e) {
			e.printStackTrace();
			return;
		}

		TelephonyManager tm = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
		tm.listen(mPhoneListener, PhoneStateListener.LISTEN_CALL_STATE);
	}


	@Override
	public void onDestroy() {

		if (D) Log.d(TAG, "onDestroy");

		if (mTts != null) {
			mTts.shutdown();
			mTts = null;
			stopSelf();
		}
	}


	public void speakPhrase() {

		String phrase = PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getString("et_text_to_say_to_your_contact", "");
		boolean coords = PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getBoolean("cb_send_coordinates", true);

		if (coords) {

			Location loc;
			LocationManager locMan;
			List<String> providers;
			float lat, lon;

			//Get the location manager from the server
			locMan = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

			providers = locMan.getProviders(true);

			//Just grab the first member of the list. It's name will be "gps"
			loc = locMan.getLastKnownLocation(providers.get(0));

			try {
				lat =  (float)loc.getLatitude();
				lon =  (float)loc.getLongitude();

				phrase += ". My coordinates are, latitude "+lat+", longitude "+lon;
			} catch (Exception e) {
				Log.e("speakPhrase","Could not get coordinates : ");
				e.printStackTrace();
			}
		}

		mAudioManager = ((AudioManager) getSystemService(Context.AUDIO_SERVICE));
		mAudioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, mAudioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL), 0);

		HashMap<String, String> ttsParams = new HashMap<String, String>();
		ttsParams.put(TextToSpeech.Engine.KEY_PARAM_STREAM,
				String.valueOf(AudioManager.STREAM_VOICE_CALL));

		for (int i = 0; i < 3; i++) {
			mTts.speak(phrase,
					TextToSpeech.QUEUE_ADD, 
					ttsParams);
		}

		mTts.shutdown();
		mTts = null;
		stopSelf();
	}



	@Override
	public void onInit(int status) {
		if (D) Log.d(TAG, "onInit");
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

}
