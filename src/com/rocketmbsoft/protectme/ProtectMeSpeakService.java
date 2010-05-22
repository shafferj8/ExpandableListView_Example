package com.rocketmbsoft.protectme;

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
import android.util.Log;

public class ProtectMeSpeakService extends Service implements  TextToSpeech.OnInitListener {

	private TextToSpeech mTts;
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

				phrase += "My coordinates are, latitude "+lat+", longitude "+lon;
			} catch (Exception e) {
				Log.d("speakPhrase","Could not get coordinates : ");
				e.printStackTrace();
			}
		}

		mAudioManager = ((AudioManager) getSystemService(Context.AUDIO_SERVICE));
		mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);

		for (int i = 0; i < 3; i++) {
			mTts.speak(phrase,
					TextToSpeech.QUEUE_ADD, 
					null);
		}
		
		mTts.shutdown();
		mTts = null;
		stopSelf();
	}
	
	@Override
	public void onInit(int status) {

		try {
			Thread.sleep(4000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		speakPhrase();
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

}
