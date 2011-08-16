package com.rocketmbsoft.protectme;

import java.util.Vector;

import android.graphics.Bitmap;

public class ContactRow {
	public Bitmap bitmap = null;
	public String name = null;
	public Vector<ContactSubRow> numbers = new Vector<ContactSubRow>();
	
	public class ContactSubRow {
		public String type = null;
		public String number = null;
		public boolean selected = false;
	}
}
