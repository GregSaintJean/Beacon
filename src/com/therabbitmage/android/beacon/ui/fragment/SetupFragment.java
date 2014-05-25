package com.therabbitmage.android.beacon.ui.fragment;

import com.therabbitmage.android.beacon.R;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.therabbitmage.android.beacon.ui.activity.PhoneSetupSMSContactsActivity;
import com.therabbitmage.android.beacon.ui.adapter.SetupAdapter;

public class SetupFragment extends Fragment implements OnItemClickListener{
	
	private ListView mList;
	
	public SetupFragment(){
		
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View v = inflater.inflate(R.layout.setup_fragment, null, false);
		
		mList = (ListView)v.findViewById(R.id.list);
		mList.setAdapter(new SetupAdapter(getActivity()));
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
				openSMSSetupActivity();
				break;
			case 1:
				break;
		}
		
	}
	
	private void openSMSSetupActivity(){
		Intent intent = new Intent(getActivity(), PhoneSetupSMSContactsActivity.class);
		startActivity(intent);
	}

}
