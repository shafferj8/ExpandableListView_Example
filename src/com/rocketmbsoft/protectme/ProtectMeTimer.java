package com.rocketmbsoft.protectme;

import java.util.Timer;
import java.util.TimerTask;

import android.util.Log;



public final class ProtectMeTimer {
	private static int hitCount = 0;
	private static Timer timer;
	private static RemindTask task;
	static int count = 0;
	static boolean started = false;
	private static int secondsWithHit = 0;
	private static boolean timerIsScheduled = false;
	
	private static final boolean D = false;


	/**
	 * Called notifying this class that the threshold was passed for shaking the phone.
	 * 
	 * @return True if the maximum amount of hits occurred within the specified time.
	 * 		False if the max hits did not yet occur.
	 */
	public static synchronized boolean maxHits() {
		boolean ret = false;
		hitCount++;

		if (! timerIsScheduled ) {
			if (D) Log.d("ProtectMeTimer::maxHits","Scheduling Timer");
			timerIsScheduled = true;
			timer = new Timer();
			task = new RemindTask();
			timer.scheduleAtFixedRate(task, 0, 1000);
		} 
		
		if(secondsWithHit >= ProtectMeShakeService.shakes ) {
			if (D) Log.d("ProtectMeTimer::maxHits","Max Hits Reached");
			hitCount = 0;
			secondsWithHit = 0;
			timer.cancel();
			timer.purge();
			timer = null;
			timerIsScheduled = false;
			ret = true;
		}

		return ret;
	}

	static class RemindTask extends TimerTask {
		public void run() {

			if (hitCount > 0) {
				if (D) Log.d("RemindTask","Had a hit within this second. Seconds with a hit : "+secondsWithHit);
				secondsWithHit++;
				hitCount = 0;
			} else {
				if (D) Log.d("RemindTask","No hits within this second, Canceling timer : "+secondsWithHit);
				hitCount = 0;
				secondsWithHit = 0;
				timer.cancel();
				timer.purge();
				timer = null;
				timerIsScheduled = false;
			}
		}
	}


	public void onDestroy() {

		if (D) Log.d("ProtectMeTimmer::onDestroy","called");
		
		started = false;

		if (timer != null) {
			timer.cancel();
			timer.purge();
			timer = null;
		}
		
		if (D) Log.d("ProtectMeTimmer::onDestroy","finished");
	}
}
