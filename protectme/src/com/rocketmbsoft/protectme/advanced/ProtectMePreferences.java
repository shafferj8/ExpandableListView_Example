

package com.rocketmbsoft.protectme.advanced;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.provider.Contacts.Phones;
import android.provider.ContactsContract;
import android.util.Log;

/**
 * Example that shows finding a preference from the hierarchy and a custom
 * preference type.
 */
public class ProtectMePreferences extends PreferenceActivity implements
Preference.OnPreferenceClickListener, Preference.OnPreferenceChangeListener {

	private static final int PICK_CONTACT = 54365;
	private static final int ORIENTATION = 54366;
	private static final int PICK_SMS_CONTACT = 54367;

	private static final String TAG = "ProtectMePreferences";

	private PreferenceScreen orientationPs = null;
	private PreferenceScreen shakePs = null;
	private SharedPreferences sharedPrefs = null;
	private ListPreference listPref = null;
	
	public Preference prefCallContact = null;
	public Preference prefSmsContact = null;
	
	public EditTextPreference prefChallenge1 = null;
	public EditTextPreference prefChallenge2 = null;;
	
	public static ProtectMePreferences instance = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		instance = this;

		sharedPrefs = getPreferenceManager().getSharedPreferences();

		// Load the XML preferences file
		addPreferencesFromResource(R.xml.preferences);

		if (Config.D)
			Log.d(TAG, "Prefernce Count : "
					+ getPreferenceScreen().getPreferenceCount());
		
		prefChallenge1 = (EditTextPreference)getPreferenceManager().findPreference("et_challenge_phrase");
		prefChallenge2 = (EditTextPreference)getPreferenceManager().findPreference("seconds_to_wait_for_response");

		listPref = (ListPreference)getPreferenceManager().findPreference("method_of_activation");
		prefCallContact = getPreferenceManager().findPreference("str_phone_contact");
		prefCallContact.setOnPreferenceClickListener(this);

		prefSmsContact = getPreferenceManager().findPreference("str_sms_contact");
		prefSmsContact.setOnPreferenceClickListener(this);

		orientationPs = (PreferenceScreen) getPreferenceScreen()
		.findPreference("orientation_preference_screen");

		shakePs = (PreferenceScreen) getPreferenceScreen().findPreference(
		"shake_preference_screen");

		int anglePref = sharedPrefs.getInt(
				Config.PREF_I_ANGLE, 90);

		orientationPs.setSummary("Activation Angle : " + anglePref);

		orientationPs.setEnabled(sharedPrefs.getString(Config.PREF_S_ACTIVATION_METHOD, "orientation").equals("orientation"));
		shakePs.setEnabled(sharedPrefs.getString(Config.PREF_S_ACTIVATION_METHOD, "orientation").equals("shaking"));
		
		listPref.setOnPreferenceChangeListener(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	public void onActivityResult(int reqCode, int resultCode, Intent data) {
		super.onActivityResult(reqCode, resultCode, data);

		switch (reqCode) {
		case (PICK_CONTACT): {
			if (resultCode == Activity.RESULT_OK) {
				Uri contactData = data.getData();

				Cursor c = null;

				if (VERSION.SDK_INT > Build.VERSION_CODES.DONUT) {
					if (Config.D)
						Log.d(TAG, "SDK is greater than DONUT");
					c = getContentResolver()
					.query(contactData,
							new String[] {
							ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
							ContactsContract.CommonDataKinds.Phone.NUMBER },
							null, null, null);
				} else {
					Log.d(TAG, "SDK is DONUT or lower");
					c = getContentResolver()
					.query(contactData,
							new String[] { Phones.DISPLAY_NAME,
							Phones.NUMBER }, null, null, null);
				}

				for (int i = 0; i < c.getColumnCount(); i++) {
					if (Config.D)
						Log.d(TAG, "Column Name : *" + c.getColumnName(i) + "*");
				}

				if (c.moveToFirst()) {

					String name = c.getString(0);
					String phone = c.getString(1);

					if (Config.D)
						Log.d(TAG, "Selected Name : " + name);
					if (Config.D)
						Log.d(TAG, "Selected Phone : " + phone);

					SharedPreferences.Editor editor = getPreferenceManager()
					.getSharedPreferences().edit();
					editor.putString(Config.PREF_STR_PHONE_CONTACT_NAME, name);
					editor.putString(Config.PREF_STR_PHONE_CONTACT_NUMBER, phone);
					editor.commit();
				}
			}
			break;
		}

		case (ORIENTATION): {
			if (Config.D)
				Log.d(TAG, "Orientation activity result");

			if (resultCode == Activity.RESULT_OK) {
				int anglePref = data.getIntExtra(Config.PREF_I_ANGLE, 90);

				if (Config.D)
					Log.d(TAG, "Received Angle Preference : " + anglePref);

				orientationPs.setSummary("Activation Angle : " + anglePref);

				SharedPreferences.Editor editor = getPreferenceManager()
				.getSharedPreferences().edit();

				editor.putInt(Config.PREF_I_ANGLE, anglePref);
				editor.commit();

			}
			break;
		}

		case (PICK_SMS_CONTACT): {
			break;
		}

		}
	}

	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
			Preference preference) {

		if (Config.D)
			Log.d(TAG, "onPreferenceTreeClick, PreferenceScreen : "
					+ preferenceScreen.getKey() + ", Preference : "
					+ preference.getKey());

		if (preference.getKey().equals("orientation_preference_screen")) {
			if (Config.D)
				Log.d(TAG, "Orientation Screen Selected");

			Intent launchPreferencesIntent = new Intent().setClass(this,
					ProtectMeOrientationPreferenceActivity.class);

			startActivityForResult(launchPreferencesIntent, ORIENTATION);
		}

		return false;
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {

		if (Config.D)
			Log.d(TAG, "Preference Selected : " + preference.getKey());

		if (preference.getKey().equals("str_phone_contact")) {
			if (Config.D)
				Log.d(TAG, "Trying to start intent");

			Intent contactintent = new Intent(Intent.ACTION_PICK);

			if (VERSION.SDK_INT > Build.VERSION_CODES.DONUT) {
				contactintent
				.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
			} else {
				contactintent.setType(Phones.CONTENT_TYPE);
			}

			startActivityForResult(contactintent, PICK_CONTACT);

		} else if (preference.getKey().equals("str_sms_contact")) {
			if (Config.D)
				Log.d(TAG, "Trying to start intent");

			Intent launchPreferencesIntent = new Intent().setClass(this,
					ExpandableContacts.class);

			startActivityForResult(launchPreferencesIntent, PICK_SMS_CONTACT);

		}

		return false;
	}

	@Override
	public boolean onPreferenceChange(Preference arg0, Object arg1) {
		orientationPs.setEnabled(((String)arg1).equals("orientation"));
		shakePs.setEnabled(((String)arg1).equals("shaking"));
		return true;
	}

}
