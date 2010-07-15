/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.rocketmbsoft.protectme;

import com.rocketmbsoft.protectme.R;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.provider.Contacts.Phones;
import android.provider.ContactsContract;
import android.util.Log;

/**
 * Example that shows finding a preference from the hierarchy and a custom preference type.
 */
public class ProtectMePreferences extends PreferenceActivity implements Preference.OnPreferenceClickListener {


	private static final int PICK_CONTACT = 54365;
	private static final int ORIENTATION = 54366;

	private static final String TAG = "ProtectMePreferences";

	private CheckBoxPreference shakeCb = null;
	private PreferenceScreen orientationPs = null;
	private PreferenceScreen shakePs = null;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Load the XML preferences file
		addPreferencesFromResource(R.xml.preferences);

		if (Config.D) Log.d(TAG,"Prefernce Count : "+getPreferenceScreen().getPreferenceCount());

		getPreferenceManager().findPreference("str_contact").setOnPreferenceClickListener(this);

		shakeCb = (CheckBoxPreference)getPreferenceScreen().findPreference(
		"shake_check_box");

		orientationPs = (PreferenceScreen)getPreferenceScreen().findPreference(
		"orientation_preference_screen");

		shakePs = (PreferenceScreen)getPreferenceScreen().findPreference(
		"shake_preference_screen");

		// shakeCb.setOnPreferenceClickListener(this);
		shakeCb.setEnabled(false);
		shakeCb.setChecked(true);

		int anglePref = getPreferenceManager().getSharedPreferences().getInt(Config.ANGLE_PREFERENCE, 90);

		orientationPs.setSummary("Activation Angle : "+anglePref);

		updateTriggerMethod();
	}

	@Override
	protected void onResume() {
		super.onResume();

		// Set up a listener whenever a key changes
		// getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	protected void onPause() {
		super.onPause();

		// Unregister the listener whenever a key changes
		// getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);

	}

	@Override
	public void onActivityResult(int reqCode, int resultCode, Intent data) {
		super.onActivityResult(reqCode, resultCode, data);

		switch (reqCode) {
		case (PICK_CONTACT) :
		{
			if (resultCode == Activity.RESULT_OK) {
				Uri contactData = data.getData();

				Cursor c = null;
				
				if (VERSION.SDK_INT > Build.VERSION_CODES.DONUT) {
					if (Config.D) Log.d(TAG, "SDK is greater than DONUT");
					c = getContentResolver().query(contactData,
							new String[]{ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, 
							ContactsContract.CommonDataKinds.Phone.NUMBER}, null, null, null);
				} else {
					Log.d(TAG, "SDK is DONUT or lower");
					c = getContentResolver().query(contactData,
							new String[]{Phones.DISPLAY_NAME, 
							Phones.NUMBER}, null, null, null);
				}

				for (int i = 0; i < c.getColumnCount(); i++) {
					if (Config.D) Log.d(TAG,"Column Name : *"+c.getColumnName(i)+"*");
				}

				if (c.moveToFirst()) {

					String name = c.getString(0);
					String phone = c.getString(1);

					if (Config.D) Log.d(TAG,"Selected Name : "+name);
					if (Config.D) Log.d(TAG,"Selected Phone : "+phone);

					SharedPreferences.Editor editor = getPreferenceManager().getSharedPreferences().edit();
					editor.putString("contact_name", name);
					editor.putString("contact_phone", phone);
					editor.commit();
				}
			}
			break;
		}

		case (ORIENTATION) :
		{
			if (Config.D) Log.d(TAG,"Orientation activity result");

			if (resultCode == Activity.RESULT_OK) {
				int anglePref = data.getIntExtra(Config.ANGLE_PREFERENCE, 90);

				if (Config.D) Log.d(TAG,"Received Angle Preference : "+anglePref);

				orientationPs.setSummary("Activation Angle : "+anglePref);
				
				SharedPreferences.Editor editor = getPreferenceManager().getSharedPreferences().edit();
				
				editor.putInt(Config.ANGLE_PREFERENCE, anglePref);
				editor.commit();

			}
			break;
		}

		}
	}

	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {

		if (Config.D) Log.d(TAG,"onPreferenceTreeClick, PreferenceScreen : "+preferenceScreen.getKey()+
				", Preference : "+preference.getKey());

		if (preference.getKey().equals("orientation_preference_screen")) {
			if (Config.D) Log.d(TAG,"Orientation Screen Selected");

			Intent launchPreferencesIntent = new Intent().setClass(this, ProtectMeOrientationPreferenceActivity.class);

			startActivityForResult(launchPreferencesIntent, ORIENTATION);
		}

		return false;
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {

		if (Config.D) Log.d(TAG,"Preference Selected : "+preference.getKey());

		if (preference.getKey().equals("str_contact")) {
			if (Config.D) Log.d(TAG,"Trying to start intent");

			Intent contactintent = new Intent (Intent.ACTION_PICK);
			
			if (VERSION.SDK_INT > Build.VERSION_CODES.DONUT) {
				contactintent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
			} else {
				contactintent.setType(Phones.CONTENT_TYPE);
			}

			startActivityForResult(contactintent, PICK_CONTACT);

		} else if (preference.getKey().equals("shake_check_box")) {
			updateTriggerMethod();
		}

		return false;
	}

	public void updateTriggerMethod() {

		SharedPreferences.Editor editor = getPreferenceManager().getSharedPreferences().edit();
		
		if (shakeCb.isChecked()) {
			shakeCb.setTitle(R.string.shake_cb_enabled);
			shakePs.setEnabled(true);
			orientationPs.setEnabled(false);
			
			editor.putBoolean(Config.SHAKE_IS_CHECKED, true);
		} else {
			shakeCb.setTitle(R.string.shake_cb_disabled);
			shakePs.setEnabled(false);
			orientationPs.setEnabled(true);
			
			editor.putBoolean(Config.SHAKE_IS_CHECKED, false);
		}
		
		editor.commit();
	}

}
