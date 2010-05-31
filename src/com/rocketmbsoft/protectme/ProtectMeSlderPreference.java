package com.rocketmbsoft.protectme;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;



public class ProtectMeSlderPreference extends Preference 
implements OnSeekBarChangeListener{


	public ProtectMeSlderPreference(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}


	public static int maximum    = 100;
	public static int interval   = 5;

	private float oldValue = 50;
	private TextView monitorBox;


	public ProtectMeSlderPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ProtectMeSlderPreference(Context context, AttributeSet attrs, int defStyle) {
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

		params2.width = LinearLayout.LayoutParams.FILL_PARENT;
		SeekBar bar = new SeekBar(getContext());
		bar.setMax(maximum);
		bar.setProgress((int)this.oldValue);
		bar.setLayoutParams(params2);
		bar.setOnSeekBarChangeListener(this);

		this.monitorBox = new TextView(getContext());
		this.monitorBox.setTextSize(20);
		this.monitorBox.setTypeface(Typeface.MONOSPACE, Typeface.ITALIC);
		this.monitorBox.setLayoutParams(params3);
		this.monitorBox.setPadding(2, 5, 0, 0);
		this.monitorBox.setText(bar.getProgress()+"");


		layout.addView(view);
		layout.addView(this.monitorBox);
		layout.addView(bar);
		layout.setId(android.R.id.widget_frame);


		return layout; 
	}

	public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {

		progress = Math.round(((float)progress)/interval)*interval;

		if(!callChangeListener(progress)){
			seekBar.setProgress((int)this.oldValue); 
			return; 
		}

		seekBar.setProgress(progress);
		this.oldValue = progress;
		this.monitorBox.setText(progress+"");
		updatePreference(progress);

		notifyChanged();
	}

	public void onStartTrackingTouch(SeekBar seekBar) {
	}


	public void onStopTrackingTouch(SeekBar seekBar) {
	}


	@Override 
	protected Object onGetDefaultValue(TypedArray ta,int index){

		int dValue = (int)ta.getInt(index,50);

		return validateValue(dValue);
	}


	@Override
	protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {

		int temp = restoreValue ? getPersistedInt(50) : (Integer)defaultValue;

		if(!restoreValue)
			persistInt(temp);

		this.oldValue = temp;
	}


	private int validateValue(int value){

		if(value > maximum)
			value = maximum;
		else if(value < 0)
			value = 0;
		else if(value % interval != 0)
			value = Math.round(((float)value)/interval)*interval;  


		return value;  
	}


	private void updatePreference(int newValue){

		SharedPreferences.Editor editor =  getEditor();
		editor.putInt(getKey(), newValue);
		editor.commit();
	}


}
