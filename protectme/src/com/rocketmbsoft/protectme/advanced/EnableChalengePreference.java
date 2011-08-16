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



public class EnableChalengePreference extends Preference
implements OnClickListener {

	public CheckBox cbChallenge = null;

	public EnableChalengePreference(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}


	public EnableChalengePreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public EnableChalengePreference(Context context, AttributeSet attrs, int defStyle) {
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

		cbChallenge = new CheckBox(getContext());

		cbChallenge.setText("Challenge Word");

		cbChallenge.setOnClickListener(this);

		cbChallenge.setChecked(getSharedPreferences().getBoolean(Config.PREF_B_CHALLENGE_ENABLED, false));

		ProtectMePreferences.instance.prefChallenge1.setEnabled(getSharedPreferences().getBoolean(Config.PREF_B_CHALLENGE_ENABLED, false));
		ProtectMePreferences.instance.prefChallenge2.setEnabled(getSharedPreferences().getBoolean(Config.PREF_B_CHALLENGE_ENABLED, false));
		
		layout.addView(view);

		layout.setId(android.R.id.widget_frame);
		
		layout.addView(cbChallenge);

		return layout; 
	}

	@Override
	public void onClick(View arg0) {
		CheckBox cb = null;

		if (arg0 instanceof CheckBox) {
			cb = (CheckBox)arg0;

			ProtectMePreferences.instance.prefChallenge1.setEnabled(cb.isChecked());
			ProtectMePreferences.instance.prefChallenge2.setEnabled(cb.isChecked());

			Editor e = getSharedPreferences().edit();

			e.putBoolean(Config.PREF_B_CHALLENGE_ENABLED, cb.isChecked());
			e.commit();
		}

	}


}

