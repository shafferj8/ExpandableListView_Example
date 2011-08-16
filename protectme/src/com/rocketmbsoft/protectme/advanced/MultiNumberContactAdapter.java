package com.rocketmbsoft.protectme.advanced;

import java.util.ArrayList;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.TextView;


public class MultiNumberContactAdapter extends BaseExpandableListAdapter {

	private ArrayList<ContactRow> items;
	private Context context;

	public MultiNumberContactAdapter(Context context, int textViewResourceId, ArrayList<ContactRow> items) {
		this.items = items;
		this.context = context;
	}


	public void addItem(ContactRow r) {
		items.add(r);

	}

	@Override
	public boolean areAllItemsEnabled() {
		return true;
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {

		return items.get(groupPosition).numbers.get(childPosition);
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		View v = convertView;
		if (v == null) {
			LayoutInflater vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(R.layout.subrow, null);
		}
		ContactRow cr = items.get(groupPosition);
		if (cr != null) {
			CheckedTextView contactNumber = (CheckedTextView) v.findViewById(R.id.contact_number);
			TextView contactType = (TextView) v.findViewById(R.id.contact_type);

			contactNumber.setFocusable(false);
			contactNumber.setFocusableInTouchMode(false);

			contactType.setFocusable(false);
			contactType.setFocusableInTouchMode(false);

			if (contactNumber != null) {
				contactNumber.setText(cr.numbers.get(childPosition).number);
			}

			if (contactType != null) {
				contactType.setText(cr.numbers.get(childPosition).type);
			}
			
			contactNumber.setChecked(cr.numbers.get(childPosition).selected);
			
			
		}
		
		v.bringToFront();
		
		return v;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		
		return items.get(groupPosition).numbers.size();
	}

	@Override
	public long getCombinedChildId(long groupId, long childId) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getCombinedGroupId(long groupId) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Object getGroup(int groupPosition) {
		
		return items.get(groupPosition);
	}

	@Override
	public int getGroupCount() {
		
		return items.size();
	}

	@Override
	public long getGroupId(int groupPosition) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded, View convertView,
			ViewGroup parent) {
		View v = convertView;
		if (v == null) {
			LayoutInflater vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(R.layout.row, null);
		}
		ContactRow cr = items.get(groupPosition);
		if (cr != null) {
			ImageView contactImage = (ImageView) v.findViewById(R.id.contact_image);
			TextView contactName = (TextView) v.findViewById(R.id.contact_name);

			if (contactImage != null) {
				if (cr.bitmap != null) {
					contactImage.setImageBitmap(cr.bitmap);
				} else {
					contactImage.setImageResource(R.drawable.icon);
				}
			}

			if (contactName != null) {
				contactName.setText(cr.name);
			}
		}

		return v;
	}

	@Override
	public boolean hasStableIds() {

		return false;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		
		return true;
	}

	@Override
	public boolean isEmpty() {
		
		return (items.size() == 0);
	}

	@Override
	public void onGroupCollapsed(int groupPosition) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onGroupExpanded(int groupPosition) {
		// TODO Auto-generated method stub

	}

	@Override
	public void registerDataSetObserver(DataSetObserver observer) {
		// TODO Auto-generated method stub

	}

	@Override
	public void unregisterDataSetObserver(DataSetObserver observer) {
		// TODO Auto-generated method stub

	}
}

