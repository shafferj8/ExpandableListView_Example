package com.rocketmbsoft.protectme.advanced;

import java.util.StringTokenizer;
import java.util.Vector;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class MultiEntryPreference {
	
	private SharedPreferences prefs;
	private String key;
	private Vector<String> val = new Vector<String>();

	public MultiEntryPreference(SharedPreferences pref, String key) {
		
		prefs = pref;
		this.key = key;
		
		String s = prefs.getString(key, "");
		
		StringTokenizer st = new StringTokenizer(s, ";");
		
		while (st.hasMoreTokens()) {
			String j = st.nextToken();
			
			if (! j.equals("")) {
				val.add(j);
			}
		}
	}

	public boolean contains(String number) {
		return val.contains(number);
	}
	
	public void add(String s) {
		String p = new String();
		
		if (val.contains(s)) {
			return;
		}
		
		val.add(s);
		
		Editor e = prefs.edit();
		
		for (String j : val) {
			p = p + ";" + j;
		}
		
		e.putString(key, p);
		
		e.commit();
	}
	
	public void remove(String s) {
		String p = new String();
		
		if (! val.contains(s)) {
			return;
		}
		
		val.remove(s);
		
		Editor e = prefs.edit();
		
		for (String j : val) {
			p = p + ";" + j;
		}
		
		e.putString(key, p);
		
		e.commit();
	}
	
	public Vector<String> getValues() {
		return val;
	}
}
