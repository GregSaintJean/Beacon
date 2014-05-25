package com.therabbitmage.android.beacon.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.therabbitmage.android.beacon.R;
import com.therabbitmage.android.beacon.ui.activity.AboutActivity;
import com.therabbitmage.android.beacon.ui.adapter.SettingsAdapter;

public class SettingsFragment extends Fragment implements OnItemClickListener{
	
	private ListView mList;
	
	public SettingsFragment(){
		
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View v = inflater.inflate(R.layout.settings_fragment, null, false);
		
		mList = (ListView)v.findViewById(R.id.list);
		mList.setAdapter(new SettingsAdapter(getActivity()));
		mList.setOnItemClickListener(this);
		
		return v;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		
		if(parent == null || view ==  null){
			return;
		}
		
		switch(position){
			case 0:
				openAboutActivity();
				break;
			case 1:
				break;
		}
	}
	
	public void openAboutActivity(){
		Intent intent = new Intent(getActivity(), AboutActivity.class);
		startActivity(intent);
	}

}
