package com.therabbitmage.android.beacon.ui.adapter;

import com.therabbitmage.android.beacon.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class SetupAdapter extends ArrayAdapter<String> {

	private LayoutInflater mInflater;

	public SetupAdapter(Context context) {
		super(context, 0);
		mInflater = LayoutInflater.from(context);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		View v;
		
		if(convertView == null){
			v = mInflater.inflate(R.layout.setup_list_item, null, false);
		} else {
			v = convertView;
		}
		
		TextView title = (TextView)v.findViewById(R.id.title);
		TextView subtext = (TextView)v.findViewById(R.id.subtext);
		
		ImageView icon = (ImageView)v.findViewById(R.id.icon);
		
		Context ctx = getContext();
		title.setText(ctx.getResources().getStringArray(R.array.setup_array)[position]);
		subtext.setText(ctx.getResources().getStringArray(R.array.setup_array_desc)[position]);
		
		return v;
	}

	@Override
	public int getCount() {
		return 2;
	}

}
