package com.rocketmbsoft.protectme;

import java.util.ArrayList;
import java.util.HashMap;
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

public class ProtectMeAlertActivity extends Activity implements 
TextToSpeech.OnInitListener, TextToSpeech.OnUtteranceCompletedListener {

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
	private static final String TAG = "ProtectMeAlertActivity";
	PowerManager.WakeLock wl;
	Thread t, t1;
	private boolean stop;
	private int milliseconds_to_wait_for_response;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (Config.D) Log.d(TAG,"onCreate  Entered");

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
			milliseconds_to_wait_for_response = (1000 * Integer.parseInt(
					prefs.getString("seconds_to_wait_for_response", "20")));
		} catch (Exception e) {
			Log.e("ProtectMeAlertActivity::onCreate","Exception : "+e.getMessage());
			Log.e("ProtectMeAlertActivity::onCreate","Exception receiving preference,"+
					" setting :milliseconds_to_wait_for_response, setting to 20");
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

		mTts.setOnUtteranceCompletedListener(this);

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
		HashMap<String, String> ttsParams = new HashMap<String, String>();
		ttsParams.put(TextToSpeech.Engine.KEY_PARAM_STREAM,
				String.valueOf(AudioManager.STREAM_MUSIC));
		ttsParams.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID,
		"Are you OK?");

		mTts.speak("Are you OK?",
				TextToSpeech.QUEUE_ADD, 
				ttsParams);

	}



	public void startVoiceRecognizer() {

		setMaxVolume();

		speak();

	}

	public void resetVolume() {
		mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mSystemVol, 0);
		mAudioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, mVoiceVol, 0);
	}

	public void setMaxVolume() {
		mAudioManager.setStreamVolume(
				AudioManager.STREAM_MUSIC, mAudioManager.getStreamMaxVolume(
						AudioManager.STREAM_SYSTEM), 0);
		mAudioManager.setStreamVolume(
				AudioManager.STREAM_VOICE_CALL, mAudioManager.getStreamMaxVolume(
						AudioManager.STREAM_VOICE_CALL), 0);
	}

	private void callContact() {
		HashMap<String, String> ttsParams = new HashMap<String, String>();
		ttsParams.put(TextToSpeech.Engine.KEY_PARAM_STREAM,
				String.valueOf(AudioManager.STREAM_VOICE_CALL));
		ttsParams.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID,
		"I'm calling the police");

		mTts.speak("I'm calling the police", 
				TextToSpeech.QUEUE_ADD,
				ttsParams);

	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (Config.D) Log.d(TAG,"onActivityResult  Entered");

		boolean found = false;

		if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) {
			// Fill the list view with the strings the recognizer thought it could have heard

			timer.cancel();

			ArrayList<String> matches = data.getStringArrayListExtra(
					RecognizerIntent.EXTRA_RESULTS);

			String phrase = prefs.getString("et_challenge_phrase", "");

			if (Config.D) Log.d(TAG,"Challenge Word is : "+phrase);

			for (int i = 0; i < matches.size(); i++) {
				if (Config.D) Log.d(TAG,"onActivityResult ----Value returned from the Voice recognizer : "+matches.get(i));
				if (matches.get(i).contains(phrase) || phrase.contains(matches.get(i))) {
					// false alarm
					resetVolume();

					if (prefs.getBoolean(Config.SHAKE_IS_CHECKED, true)) {
						startService(new Intent(this,
								ProtectMeShakeService.class));
					} else {
						startService(new Intent(this,
								ProtectMeOrientationService.class));						
					}

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
		if (Config.D) Log.d(TAG,"onDestroy");

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
		if (Config.D) Log.d(TAG,"onStart");
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



	@Override
	public void onUtteranceCompleted(String utteranceId) {
		if (utteranceId.equals("Are you OK?")) {
			if (stop) {
				return;
			}

			Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
			intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
					RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
			intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Please Speak Your Challenge Word");

			timer.schedule(task, milliseconds_to_wait_for_response);

			startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
		} else if (utteranceId.equals("I'm calling the police")) {
			try {
				Intent intent = new Intent(Intent.ACTION_CALL);

				String tel = prefs.getString("contact_phone", "");

				intent.setData(Uri.parse("tel:"+tel));

				Intent launchPreferencesIntent = new Intent().setClass(this, ProtectMeSpeakService.class);

				startService(launchPreferencesIntent);

				if (keyboardWasLocked) {
					lock.reenableKeyguard();
					keyboardWasLocked = false;
				}

				// Start the Phone activity
				startActivity(intent);


			} catch (Exception e) {
				Log.e("SampleApp", "Failed to invoke call", e);
			}
		}
	}
}
