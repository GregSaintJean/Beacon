package com.therabbitmage.android.beacon.ui.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.therabbitmage.android.beacon.BeaconApp;

public abstract class BaseActionBarActivity extends ActionBarActivity {
	
	protected BeaconApp mApp;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mApp = BeaconApp.getInstance();
	}

}
