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
import android.os.Build;
import android.os.IBinder;
import android.os.Build.VERSION;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.util.Log;

public class ProtectMeSpeakService extends Service implements  TextToSpeech.OnInitListener, TextToSpeech.OnUtteranceCompletedListener {

	private TextToSpeech mTts;
	private static final String TAG = "ProtectMeSpeakService";
	
	private AudioManager mAudioManager;

	@Override
	public void onCreate() {
		super.onCreate();
		
		mAudioManager = ((AudioManager) getSystemService(Context.AUDIO_SERVICE));
		
		try {
			mTts = new TextToSpeech(this,
					this  // TextToSpeech.OnInitListener
			);

		} catch(ActivityNotFoundException e) {
			e.printStackTrace();
			return;
		}
		
		if (VERSION.SDK_INT > Build.VERSION_CODES.DONUT) {
			mAudioManager.setParameters("noise_suppression=off");
		}
	}
	



	@Override
	public void onDestroy() {
		super.onDestroy();
		if (Config.D) Log.d(TAG, "onDestroy");
		
		if (VERSION.SDK_INT > Build.VERSION_CODES.DONUT) {
			mAudioManager.setParameters("noise_suppression=on");
		}
		
		if (mTts != null) {
			mTts.shutdown();
			mTts = null;
		}
	}


	public void speakPhrase() {

		String phrase = PreferenceManager.getDefaultSharedPreferences(
				getBaseContext()).getString("et_text_to_say_to_your_contact", "");
		boolean coords = PreferenceManager.getDefaultSharedPreferences(
				getBaseContext()).getBoolean("cb_send_coordinates", true);

		// mAudioManager.setSpeakerphoneOn(true);
		
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

		mAudioManager.setStreamVolume(
				AudioManager.STREAM_VOICE_CALL, 
				mAudioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL), 0);


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
		
		Thread t = new Thread(new Runnable() {
			public void run() {
				
				try {
					Thread.sleep(15000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				speakPhrase();
			}
		});

		t.start();

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
			
			mAudioManager.setSpeakerphoneOn(true);
			
			mTts.shutdown();
			mTts = null;
			stopSelf();
		}
		
	}

}
