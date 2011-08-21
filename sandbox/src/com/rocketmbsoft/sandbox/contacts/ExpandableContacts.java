package com.rocketmbsoft.sandbox.contacts;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Hashtable;

import com.rocketmbsoft.sandbox.contacts.ContactRow.ContactSubRow;

import android.app.ExpandableListActivity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.view.View;
import android.widget.CheckedTextView;
import android.widget.ExpandableListView;

public class ExpandableContacts extends ExpandableListActivity {
	private MultiNumberContactAdapter contactAdapter = null;
	private ArrayList<ContactRow> contactRows = null;
	Hashtable<String, ContactRow> h = new Hashtable<String, ContactRow>();
	
	private static final String TAG = ExpandableContacts.class.getSimpleName();;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
        setContentView(R.layout.main);
        
        fillContacts();
        
        contactRows = new ArrayList<ContactRow>();
        
        contactAdapter = new MultiNumberContactAdapter(this, R.layout.row, contactRows);
        this.setListAdapter(contactAdapter);
        
        for ( ContactRow c : h.values() ) {
        	contactAdapter.addItem(c);
        }
	}
	
	public void fillContacts() {

		ContentResolver cr = getContentResolver();
		ContactRow cr1 = null;

		Cursor emailCur = cr.query(
				ContactsContract.Contacts.CONTENT_URI,
				new String[] { 
						ContactsContract.Contacts.DISPLAY_NAME,
						ContactsContract.Contacts._ID}, null,
						null, null);
		
		while (emailCur.moveToNext()) {
			cr1 = new ContactRow();
			ContactSubRow csr = cr1.new ContactSubRow();
			
			// This is making a call to the phone table and using the CONTACT_ID
			// foreign key to find the phone number and type we are interested in.
			Cursor phoneCur = cr.query(
					ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
					new String[] { 
							ContactsContract.CommonDataKinds.Phone.TYPE,
							ContactsContract.CommonDataKinds.Phone.NUMBER},
							ContactsContract.CommonDataKinds.Phone.CONTACT_ID+"="+emailCur.getLong(1),
							null, null);
			
			// Move the cursor to the first. If this fails then that means there was
			// no phone information in the table for the contact.
			if (phoneCur.moveToFirst()) {
				int t = phoneCur
				.getInt(phoneCur
						.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
				
				csr.type = getResources().getString(
						ContactsContract.CommonDataKinds.Phone.getTypeLabelResource(t));
				
				csr.number = phoneCur.getString(phoneCur
						.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
			} else {
				// There was no phone information for this contact so bail and start
				// again from the top.
				continue;
			}

			String displayName = emailCur
			.getString(emailCur
					.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
			
			Bitmap photo = null;

            photo = BitmapFactory.decodeStream(openPhoto(emailCur.getLong(1)));
            
            if (photo != null) {
            	photo = Bitmap.createScaledBitmap(photo, 40, 40, false);
            }
			
			if (h.contains(displayName)) {
				h.get(displayName).numbers.add(csr);
			} else {
				
				cr1.bitmap = photo;
				cr1.name = displayName;
				cr1.numbers.add(csr);
				
				h.put(displayName, cr1);
			}
		}
		emailCur.close();
	}
	
	public InputStream openPhoto(long contactId) {
	     Uri contactUri = ContentUris.withAppendedId(Contacts.CONTENT_URI, contactId);
	     Uri photoUri = Uri.withAppendedPath(contactUri, Contacts.Photo.CONTENT_DIRECTORY);
	     Cursor cursor = getContentResolver().query(photoUri,
	          new String[] {ContactsContract.CommonDataKinds.Photo.PHOTO}, null, null, null);
	     if (cursor == null) {
	         return null;
	     }
	     try {
	         if (cursor.moveToFirst()) {
	             byte[] data = cursor.getBlob(0);
	             if (data != null) {
	                 return new ByteArrayInputStream(data);
	             }
	         }
	     } finally {
	         cursor.close();
	     }
	     return null;
	 }
	
	@Override
	public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
		boolean ret = false;
		
		CheckedTextView cb = (CheckedTextView) v.findViewById(R.id.contact_number);
		
		if (cb != null) {
			cb.toggle();
		}
		
		contactRows.get(groupPosition).numbers.get(childPosition).selected = cb.isChecked();
		
		return ret;
	}

}