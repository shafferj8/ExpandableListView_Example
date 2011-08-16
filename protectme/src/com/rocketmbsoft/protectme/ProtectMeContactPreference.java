package com.rocketmbsoft.protectme;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;


public class ProtectMeContactPreference extends Preference implements OnSharedPreferenceChangeListener {

	public static int maximum    = 100;
	public static int interval   = 5;

	private TextView monitorBox;

	public ProtectMeContactPreference(Context context) {
		super(context);
	}

	public ProtectMeContactPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		
	}

	public ProtectMeContactPreference(Context context, AttributeSet attrs, int defStyle) {
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
				80,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		params2.gravity = Gravity.RIGHT;


		LinearLayout.LayoutParams params3 = new LinearLayout.LayoutParams(
				300,
				LinearLayout.LayoutParams.FILL_PARENT);
		params3.gravity = Gravity.CENTER;


		layout.setPadding(15, 5, 10, 5);
		layout.setOrientation(LinearLayout.VERTICAL);

		TextView view = new TextView(getContext());
		view.setText(getTitle());
		view.setTextSize(20);
		view.setTypeface(Typeface.SANS_SERIF, Typeface.BOLD);
		view.setGravity(Gravity.LEFT);
		view.setLayoutParams(params1);

		this.monitorBox = new TextView(getContext());
		this.monitorBox.setTextSize(15);
		this.monitorBox.setTypeface(Typeface.MONOSPACE, Typeface.ITALIC);
		this.monitorBox.setLayoutParams(params3);
		this.monitorBox.setPadding(2, 5, 0, 0);
		this.monitorBox.setText("Contact : "+getSharedPreferences().getString("contact_name", ""));


		layout.addView(view);
		layout.addView(this.monitorBox);
		layout.setId(android.R.id.widget_frame);
		
		this.getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

		return layout; 
	}


	@Override 
	protected Object onGetDefaultValue(TypedArray ta,int index){

		String dValue = ta.getString(index);

		return dValue;
	}


	@Override
	protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {

		String temp = restoreValue ? getPersistedString("") : (String)defaultValue;

		if(!restoreValue)
			persistString(temp);
	}



	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		
		if (key.equals("contact_name")) {
			this.monitorBox.setText("Contact : "+getSharedPreferences().getString(key, ""));
		}
		
	}

}

