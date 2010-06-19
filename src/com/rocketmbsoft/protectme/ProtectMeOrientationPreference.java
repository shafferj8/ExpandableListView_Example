package com.rocketmbsoft.protectme;
    

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.RectF;
import android.preference.Preference;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;

public class ProtectMeOrientationPreference extends Preference implements OnClickListener {
	public ProtectMeOrientationPreference(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	public ProtectMeOrientationPreference(Context context, AttributeSet ats) {
		super(context, ats);
	}


	DemoView demoview;
	
	int x, y, az;
	DisplayMetrics metrics;
	private int anglePreference;
	
	private OrientationEventListener mOrientationListener = null;
	Button set;
	
	int currentAngle = 55;
	
	/** Called when the activity is first created. */
	@Override
	protected View onCreateView(ViewGroup parent){
		super.onCreateView(parent);
		
		mOrientationListener = new OrientationEventListener(getContext()) {
			
			@Override
			public void onOrientationChanged(int orientation) {
				y = (360 - orientation);
				
				demoview.invalidate();
			}
		};
		
		mOrientationListener.enable();
		
		demoview = new DemoView(getContext());
		
		set = new Button(getContext());
		
		set.setText("Set");
		set.setWidth(300);
		
		set.setOnClickListener(this);
		
		LinearLayout layout = new LinearLayout(getContext());

		LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.FILL_PARENT,
				LinearLayout.LayoutParams.FILL_PARENT);
		params1.gravity = Gravity.CENTER;
		params1.weight  = 1.0f;
		
		layout.addView(set);
		
		layout.setPadding(15, 10, 10, 10);
		
		layout.addView(demoview);
		
		return layout;
		
	}
//	
//	@Override
//	public void onStart() {
//		super.onStart();
//		mOrientationListener.enable();
//	}
//	
//	@Override
//	public void onPause() {
//		super.onPause();
//		
//		mOrientationListener.disable();
//	}
//	
//	@Override
//	public void onStop() {
//		super.onStop();
//		
//	}

	private class DemoView extends View {

		public DemoView(Context context){
			super(context);
		}

		@Override 
		protected void onDraw(Canvas canvas) {
			super.onDraw(canvas);
			
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
			
			paint.reset();
			paint.setTextSize(20);
			paint.setColor(Color.RED);
			canvas.drawText("Activation Angle : "+currentAngle, 30, 75, paint);
			canvas.drawText("Current Activation Angle : "+anglePreference, 30, 115, paint);
		}
	}

	@Override
	public void onClick(View v) {
		anglePreference = currentAngle;
		
	}
}