package com.rocketmbsoft.protectme.advanced;

import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.graphics.Typeface;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;



public class ActionSelectorPreference extends Preference
	implements OnClickListener {

	public CheckBox cbCallContact = null;
	public CheckBox cbSmsContact = null;
	public CheckBox cbPlaySiren = null;
	
	private static final int CB_SMS_ID = 0x12345;
	private static final int CB_CALL_ID = 0x12346;
	private static final int CB_PLAY_SIREN_ID = 0x12347;

	public ActionSelectorPreference(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}


	public ActionSelectorPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ActionSelectorPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	protected View onCreateView(ViewGroup parent){

		LinearLayout layout = new LinearLayout(getContext());

		LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		params1.gravity = Gravity.LEFT;
		params1.weight  = 1.0f;


		LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(
				220, // 80,
				LinearLayout.LayoutParams.FILL_PARENT);
		params2.gravity = Gravity.CENTER;


		LinearLayout.LayoutParams params3 = new LinearLayout.LayoutParams(
				40,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		params3.gravity = Gravity.CENTER;

		layout.setPadding(15, 5, 10, 5);
		layout.setOrientation(LinearLayout.VERTICAL);

		TextView view = new TextView(getContext());
		view.setText(getTitle());
		view.setTextSize(20);
		view.setTypeface(Typeface.SANS_SERIF, Typeface.BOLD);
		view.setGravity(Gravity.CENTER);
		view.setLayoutParams(params1);

		cbCallContact = new CheckBox(getContext());
		cbSmsContact = new CheckBox(getContext());
		cbPlaySiren = new CheckBox(getContext());
		
		cbSmsContact.setText("SMS a Contact");
		cbCallContact.setText("Call a Contact");
		cbPlaySiren.setText("Turn on Speaker and Play Siren");

		cbSmsContact.setOnClickListener(this);
		cbSmsContact.setId(CB_SMS_ID);
		cbCallContact.setOnClickListener(this);
		cbCallContact.setId(CB_CALL_ID);
		cbPlaySiren.setOnClickListener(this);
		cbPlaySiren.setId(CB_PLAY_SIREN_ID);
		
		cbCallContact.setChecked(getSharedPreferences().getBoolean(Config.PREF_B_CALL_CONTACT_ENABLED, false));
		cbSmsContact.setChecked(getSharedPreferences().getBoolean(Config.PREF_B_SMS_CONTACT_ENABLED, false));
		cbPlaySiren.setChecked(getSharedPreferences().getBoolean(Config.PREF_B_PLAY_SIREN_ENABLED, false));
		
		ProtectMePreferences.instance.prefSmsContact.setEnabled(getSharedPreferences().getBoolean(Config.PREF_B_SMS_CONTACT_ENABLED, false));
		ProtectMePreferences.instance.prefCallContact.setEnabled(getSharedPreferences().getBoolean(Config.PREF_B_CALL_CONTACT_ENABLED, false));

		layout.addView(view);
		layout.addView(cbCallContact);
		layout.addView(cbSmsContact);
		layout.addView(cbPlaySiren);
		
		layout.setId(android.R.id.widget_frame);


		return layout; 
	}

	@Override
	public void onClick(View arg0) {
		CheckBox cb = null;
		
		if (arg0 instanceof CheckBox) {
			cb = (CheckBox)arg0;
			
			if (cb.getId() == CB_CALL_ID) {
				
				ProtectMePreferences.instance.prefCallContact.setEnabled(cb.isChecked());

				Editor e = getSharedPreferences().edit();
				
				e.putBoolean(Config.PREF_B_CALL_CONTACT_ENABLED, cb.isChecked());
				e.commit();
			} else if (cb.getId() == CB_SMS_ID) {
				
				ProtectMePreferences.instance.prefSmsContact.setEnabled(cb.isChecked());
				
				Editor e = getSharedPreferences().edit();
				
				e.putBoolean(Config.PREF_B_SMS_CONTACT_ENABLED, cb.isChecked());
				e.commit();
			} else if (cb.getId() == CB_PLAY_SIREN_ID) {
				
				Editor e = getSharedPreferences().edit();
				
				e.putBoolean(Config.PREF_B_PLAY_SIREN_ENABLED, cb.isChecked());
				e.commit();
			}
		}
		
	}


}

