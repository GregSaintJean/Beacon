package com.therabbitmage.android.beacon.ui.adapter;

import com.therabbitmage.android.beacon.R;
import com.therabbitmage.android.beacon.provider.Beacon;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class BeaconContactsAdapter extends CursorAdapter {
	
	private LayoutInflater mInflater;

	public BeaconContactsAdapter(Context context) {
		super(context, null, 0);
		mInflater = LayoutInflater.from(context);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		final ViewHolder holder = (ViewHolder)view.getTag();
		holder.text.setText(cursor.getString(cursor.getColumnIndex(Beacon.BeaconContacts.CN_FIRST_NAME))
				+ " " + cursor.getString(cursor.getColumnIndex(Beacon.BeaconContacts.CN_LAST_NAME)));
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
		final View itemLayout = mInflater.inflate(R.layout.beacon_contact_list_adapter, viewGroup, false);
		
		final ViewHolder holder = new ViewHolder();
		holder.text = (TextView)itemLayout;
		itemLayout.setTag(holder);
		return itemLayout;
	}
	
	class ViewHolder{
		TextView text;
	}

}
