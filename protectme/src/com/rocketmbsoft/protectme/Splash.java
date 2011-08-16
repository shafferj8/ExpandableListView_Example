package com.rocketmbsoft.protectme;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class Splash extends Activity {
	
	private static final int STOPSPLASH = 0;
	//time in milliseconds
	private static final long SPLASHTIME = 3000;
	
	//handler for splash screen
	private Handler splashHandler = new Handler() {
		/* (non-Javadoc)
		 * @see android.os.Handler#handleMessage(android.os.Message)
		 */
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case STOPSPLASH:
				Intent intent = new Intent();
                intent.setClass(Splash.this, ProtectMeActivity.class);
                
                startActivity(intent);
				finish();
			}
			super.handleMessage(msg);
		}
	};
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.splash);
			Message msg = new Message();
			msg.what = STOPSPLASH;
			splashHandler.sendMessageDelayed(msg, SPLASHTIME);
    }
}
