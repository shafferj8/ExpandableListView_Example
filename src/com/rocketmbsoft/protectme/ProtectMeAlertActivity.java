package com.rocketmbsoft.protectme;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;

public class ProtectMeAlertActivity extends Activity implements TextToSpeech.OnInitListener {

	private AudioManager mAudioManager; 
	private TextToSpeech mTts;
	KeyguardManager km;
	KeyguardLock lock;
	public Timer timer;
	RemindTask task = new RemindTask();
	boolean keyboardWasLocked = false;
	private int mSystemVol;
	private int mVoiceVol;
	SharedPreferences prefs;
	private static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;
	String TAG = "ProtectMeAlertActivity";
	PowerManager.WakeLock wl;
	Thread t, t1;
	private boolean stop;
	private int milliseconds_to_wait_for_response;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d("ProtectMeAlertActivity::onCreate","Entered");

		setContentView(R.layout.alert);

		// Check to see if the keyboard guard is on
		//unlock phone
		km = (KeyguardManager) getSystemService
		(Context.KEYGUARD_SERVICE);

		if (km.inKeyguardRestrictedInputMode()) {

			t1 = new Thread(new Runnable() {
				public void run() {
					lock = km.newKeyguardLock("Guardian");

					lock.disableKeyguard();

					keyboardWasLocked = true;

					wl = ((PowerManager) getSystemService(Context.POWER_SERVICE)).
					newWakeLock(PowerManager.FULL_WAKE_LOCK, "My Tag");
					wl.acquire();

					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});

			t1.start();
		}

		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE); 

		mSystemVol = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		mVoiceVol = mAudioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL);

		timer = new Timer();

		prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		
		try {
			milliseconds_to_wait_for_response = (1000 * Integer.parseInt(prefs.getString("seconds_to_wait_for_response", "20")));
		} catch (Exception e) {
			Log.d("ProtectMeAlertActivity::onCreate","Exception : "+e.getMessage());
			Log.d("ProtectMeAlertActivity::onCreate","Exception receiving preference, setting :milliseconds_to_wait_for_response, setting to 20");
			milliseconds_to_wait_for_response = 20000;
		}
		
		try {
			mTts = new TextToSpeech(this,
					this  // TextToSpeech.OnInitListener
			);

		} catch(ActivityNotFoundException e) {
			e.printStackTrace();
			return;
		}

	}

	@Override
	public void onInit(int status) {
		if (status == TextToSpeech.SUCCESS) {
			// Set preferred language to US english.
			// Note that a language may not be available, and the result will indicate this.
			mTts.setLanguage(Locale.US);
		}

		if (t1 != null) {
			try {
				t1.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		t = new Thread(new Runnable() {
			public void run() {
				startVoiceRecognizer();
			}
		});

		t.start();
	}

	public void speak() {
		mTts.speak("Are you OK?",
				TextToSpeech.QUEUE_ADD,  // Drop all pending entries in the playback queue.
				null);
		while (mTts.isSpeaking()) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}



	public void startVoiceRecognizer() {

		setMaxVolume();

		try {
			speak();
		} catch (Exception e) {

		}
		
		if (stop) {
			return;
		}

		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
				RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Please Speak Your Challenge Word");

		timer.schedule(task, milliseconds_to_wait_for_response);

		startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
	}

	public void resetVolume() {
		mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mSystemVol, 0);
		mAudioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, mVoiceVol, 0);
	}

	public void setMaxVolume() {
		mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mAudioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM), 0);
		mAudioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, mAudioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL), 0);
	}

	private void callContact() {
		// TODO 1. call the contact
		//      2. Turn on Speakerphone
		//      3. Speak the phrase the user entered
		//      4. Send coordinates if requested

		mTts.speak("I'm calling the police", 
				TextToSpeech.QUEUE_ADD,
				null);

		while (mTts.isSpeaking()) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try {
			Intent intent = new Intent(Intent.ACTION_CALL);
			String tel = prefs.getString("contact_phone", "");

			intent.setData(Uri.parse("tel:"+tel));

			mAudioManager.setSpeakerphoneOn(true);  
			mAudioManager.setRouting(AudioManager.MODE_CURRENT, AudioManager.ROUTE_SPEAKER, 1); 

			Intent launchPreferencesIntent = new Intent().setClass(this, ProtectMeSpeakService.class);

			startService(launchPreferencesIntent);

			if (keyboardWasLocked) {
				lock.reenableKeyguard();
				keyboardWasLocked = false;
			}

			// Start the Phone activity
			startActivity(intent);

			setMaxVolume();

		} catch (Exception e) {
			Log.e("SampleApp", "Failed to invoke call", e);
		}
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		Log.d("ProtectMeAlertActivity:onActivityResult", "Called");

		boolean found = false;

		if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) {
			// Fill the list view with the strings the recognizer thought it could have heard

			timer.cancel();

			ArrayList<String> matches = data.getStringArrayListExtra(
					RecognizerIntent.EXTRA_RESULTS);

			String phrase = prefs.getString("et_challenge_phrase", "");

			Log.d("ProtectMeAlertActivity:onActivityResult", "Challenge Word is : "+phrase);

			for (int i = 0; i < matches.size(); i++) {
				Log.d("ProtectMeAlertActivity:onActivityResult", "----Value returned from the Voice recognizer : "+matches.get(i));
				if (matches.get(i).contains(phrase) || phrase.contains(matches.get(i))) {
					// false alarm
					resetVolume();

					startService(new Intent(this,
							ProtectMeService.class));

					if (keyboardWasLocked) {
						lock.reenableKeyguard();
						keyboardWasLocked = false;
					}

					finish();
					found = true;
				}
			}

			if (!found) {
				callContact();
			} 
		}
	}


	@ Override
	public void onDestroy() {
		super.onDestroy();
		Log.d(TAG,"onDestroy");

		if (wl != null && wl.isHeld()) {
			wl.release();
			wl = null;
		}

		if (keyboardWasLocked) {
			lock.reenableKeyguard();
			keyboardWasLocked = false;
			lock = null;
		}

		timer.cancel();
		timer.purge();
		timer = null;

		if (mTts != null) {
			mTts.shutdown();
			mTts = null;
		}

		try {
			if (t1 != null) {
				t1.join();
				t1 = null;
			}

			if (t != null) {
				stop = true;
				t.join();
				t = null;
			}
		} catch (Exception e) {

		}
	}

	@ Override
	public void onPause() {
		super.onPause();
		Log.d(TAG,"onPause");
	}

	@ Override
	public void onStart() {
		super.onStart();
		Log.d(TAG,"onStart");
	}

	@ Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}



	class RemindTask extends TimerTask {
		public void run() {

			Intent map = new Intent();

			ArrayList<String> ret = new ArrayList<String>();

			ret.add("uuuuuuuuuu");

			map.putStringArrayListExtra(
					RecognizerIntent.EXTRA_RESULTS, ret);

			onActivityResult(VOICE_RECOGNITION_REQUEST_CODE, RESULT_OK, map);
		}
	}
}
