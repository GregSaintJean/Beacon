package com.therabbitmage.android.beacon.ui.activity;

import com.therabbitmage.android.beacon.BeaconApp;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

public abstract class BaseFragmentActivity extends FragmentActivity {
	
	protected BeaconApp mApp;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mApp = BeaconApp.getInstance();
	}

}
