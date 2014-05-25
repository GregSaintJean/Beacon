package com.therabbitmage.android.beacon.ui.adapter;

import com.therabbitmage.android.beacon.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class SettingsAdapter extends ArrayAdapter<String> {
	
	private LayoutInflater mInflater;

	public SettingsAdapter(Context context) {
		super(context, 0);
		mInflater = LayoutInflater.from(context);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		View v;
		
		if(convertView == null){
			v = mInflater.inflate(R.layout.settings_list_item, null, false);
		} else {
			v = convertView;
		}
		
		TextView title = (TextView)v.findViewById(R.id.title);
		title.setText(R.string.about);
		
		return v;
	}

	@Override
	public int getCount() {
		return 1;
	}

}
