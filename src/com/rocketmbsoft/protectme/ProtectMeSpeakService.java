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

public class ProtectMeSpeakService extends Service implements  TextToSpeech.OnInitListener, TextToSpeech.OnUtteranceCompletedListener {

	private TextToSpeech mTts;
	private static final String TAG = "ProtectMeSpeakService";

	private boolean offhook = false;
	private boolean idle = false;

	private TelephonyManager tm = null;

	private PhoneStateListener mPhoneListener = new PhoneStateListener()
	{
		public void onCallStateChanged(int state, String incomingNumber)
		{
			switch (state)
			{
			case TelephonyManager.CALL_STATE_RINGING:
				if (Config.D) Log.d(TAG, "CALL_STATE_RINGING : "+incomingNumber);
				break;
			case TelephonyManager.CALL_STATE_OFFHOOK:
				if (Config.D) Log.d(TAG, "CALL_STATE_OFFHOOK : "+incomingNumber);
				offhook = true;

				if (offhook && idle) {
					if (Config.D) Log.d(TAG, "Call In Progess ... ");
					
					tm.listen(mPhoneListener, PhoneStateListener.LISTEN_NONE);

					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					speakPhrase();
				}
				break;
			case TelephonyManager.CALL_STATE_IDLE:
				if (Config.D) Log.d(TAG, "CALL_STATE_IDLE : "+incomingNumber);
				idle = true;

				if (offhook && idle) {
					if (Config.D) Log.d(TAG, "Call Is Disconnected ...");
				}
				break;
			default:
				if (Config.D) Log.d(TAG, "Unknown phone state=" + state);
			}
		}
	};
	private AudioManager mAudioManager;

	@Override
	public void onCreate() {
		super.onCreate();
		
		try {
			mTts = new TextToSpeech(this,
					this  // TextToSpeech.OnInitListener
			);

		} catch(ActivityNotFoundException e) {
			e.printStackTrace();
			return;
		}

		tm = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
	}
	



	@Override
	public void onDestroy() {
		super.onDestroy();
		if (Config.D) Log.d(TAG, "onDestroy");
		
		if (tm != null) {
			tm.listen(mPhoneListener, PhoneStateListener.LISTEN_NONE);
		}

		if (mTts != null) {
			mTts.shutdown();
			mTts = null;
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


		for (int i = 0; i < 3; i++) {
			HashMap<String, String> ttsParams = new HashMap<String, String>();
			ttsParams.put(TextToSpeech.Engine.KEY_PARAM_STREAM,
					String.valueOf(AudioManager.STREAM_VOICE_CALL));
			ttsParams.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID,
					TAG+i);
			
			mTts.speak(phrase,
					TextToSpeech.QUEUE_ADD, 
					ttsParams);
		}

	}



	@Override
	public void onInit(int status) {
		if (Config.D) Log.d(TAG, "onInit");
		
		mTts.setOnUtteranceCompletedListener(this);
		
		tm.listen(mPhoneListener, PhoneStateListener.LISTEN_CALL_STATE);
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}




	@Override
	public void onUtteranceCompleted(String utteranceId) {
		if (Config.D) Log.d(TAG, "onUtteranceCompleted : "+utteranceId);
		
		if (utteranceId.equals(TAG+2)) {
			if (Config.D) Log.d(TAG, "onUtteranceCompleted got last utterance, shutting down");
			
			mTts.shutdown();
			mTts = null;
			stopSelf();
		}
		
	}

}
