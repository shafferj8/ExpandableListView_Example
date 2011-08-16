package com.rocketmbsoft.protectme.advanced;
    

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;

public class ProtectMeOrientationPreferenceActivity extends Activity implements OnClickListener {
	

	static final String TAG = "ProtectMeOrientationPreference";

	DemoView demoview;
	
	int x, y, az;
	DisplayMetrics metrics;
	private int anglePreference;
	
	private OrientationEventListener mOrientationListener = null;
	Button set;
	
	int currentAngle = 55;
	private boolean mod;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mOrientationListener = new OrientationEventListener(this) {

			@Override
			public void onOrientationChanged(int orientation) {
				
				if (orientation > 180) {
					mod = true;
				} else {
					mod = false;
				}
				y = (360 - orientation);
				demoview.invalidate();
			}
		};
		
		mOrientationListener.enable();
		
		demoview = new DemoView(this);
		setContentView(demoview);
		
		set = new Button(this);
		
		set.setText("Set");
		set.setWidth(300);
		
		set.setOnClickListener(this);
		
		LinearLayout layout = new LinearLayout(this);

		LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.FILL_PARENT,
				LinearLayout.LayoutParams.FILL_PARENT);
		params1.gravity = Gravity.CENTER;
		params1.weight  = 1.0f;
		
		layout.addView(set);
		
		layout.setPadding(15, 10, 10, 10);
		
		this.addContentView(layout, params1);

	}
	
	
	@Override
	public void onStart() {
		super.onStart();
		mOrientationListener.enable();
	}
	
	@Override
	public void onPause() {
		super.onPause();
		
		mOrientationListener.disable();
	}
	
	@Override
	public void onStop() {
		super.onStop();
		
		set.setOnClickListener(null);
		mOrientationListener.disable();
		demoview = null;
		mOrientationListener  = null;
		set = null;
		
	}
	


	private class DemoView extends View {

		private static final String TAG = "DemoView";
		
		public DemoView(Context context){
			super(context);
		}

		@Override 
		protected void onDraw(Canvas canvas) {
			super.onDraw(canvas);
			
			if (Config.D) Log.d(TAG, "onDraw");
			
			Paint paint = new Paint();
			paint.setStyle(Paint.Style.FILL);

			// make the entire canvas white
			paint.setColor(Color.WHITE);
			canvas.drawPaint(paint);
			
			paint.setColor(Color.GRAY);

			// draw a thick dashed line
			DashPathEffect dashPath =
				new DashPathEffect(new float[]{20,5}, 1);
			paint.setPathEffect(dashPath);
			paint.setStrokeWidth(4);
			
			paint.setColor(Color.BLACK);
			canvas.drawLine(canvas.getWidth()/2, 0, canvas.getWidth()/2, canvas.getHeight(), paint);
			
			paint.setColor(Color.RED);
			canvas.drawLine(0, canvas.getHeight()/2, canvas.getWidth(), canvas.getHeight()/2, paint);
			
			
			paint.reset();
			RectF oval = new RectF();
			
			oval.bottom = canvas.getHeight()/2+120;
			oval.left = canvas.getWidth()/2-120;
			oval.right = canvas.getWidth()/2+120;
			oval.top = canvas.getHeight()/2-120;
			
			paint.setPathEffect(dashPath);
			paint.setStrokeWidth(4);
			
			paint.setColor(Color.GREEN);
			int arcAngle = 0;
			
			if (y >= 180) {
				arcAngle = (int) Math.abs(y - 360);
				canvas.drawArc(oval , y+90, arcAngle, true, paint);
			} else {
				arcAngle = (int) y;
				canvas.drawArc(oval , 90, arcAngle, true, paint);
			}
			
			canvas.rotate(y, (canvas.getWidth()/2), (canvas.getHeight()/2));
			
			paint.setColor(Color.RED);
			canvas.drawLine(canvas.getWidth()/2, 0, canvas.getWidth()/2, canvas.getHeight(), paint);
			
			canvas.restore();
			paint.reset();
			paint.setColor(Color.LTGRAY);
			paint.setAlpha(200);
			canvas.drawRect(0, 0, canvas.getWidth(), 120, paint);
			
			
			if (y >= 180) {
				currentAngle = y - 180;
			} else {
				currentAngle = y;
			}
			
			if (mod) {
				currentAngle = 180 - currentAngle;
			}
			
			currentAngle = Math.abs(currentAngle - 180);
			
			if (Config.D)Log.d(TAG, "Modification : "+mod); 
			
			paint.reset();
			paint.setTextSize(20);
			paint.setColor(Color.RED);
			canvas.drawText("Activation Angle : "+currentAngle, 30, 75, paint);
			// canvas.drawText("Current Activation Angle : "+anglePreference, 30, 115, paint);
		}
	}

	@Override
	public void onClick(View v) {
		anglePreference = currentAngle;
		
		Intent data = new Intent();
		
		data.putExtra(Config.PREF_I_ANGLE, anglePreference);
		setResult(Activity.RESULT_OK, data);
		
		this.finish();
	}

}