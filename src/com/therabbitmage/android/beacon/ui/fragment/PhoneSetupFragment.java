package com.therabbitmage.android.beacon.ui.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import com.therabbitmage.android.beacon.R;

public class PhoneSetupFragment extends Fragment implements OnClickListener {
	
	private View mRoot;
	private Button mAddContactButton;
	private ListView mContactList;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mRoot = inflater.inflate(R.layout.phone_setup_fragment, container, false);
		mAddContactButton = (Button)mRoot.findViewById(R.id.add_contact_btn);
		mContactList = (ListView)mRoot.findViewById(R.id.contact_list);
		mAddContactButton.setOnClickListener(this);
		return mRoot;
	}

	@Override
	public void onClick(View view) {
		
		if(view.getId() == R.id.add_contact_btn){
			
		}
		
	}

}
