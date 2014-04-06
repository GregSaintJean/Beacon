package com.therabbitmage.android.beacon.ui.activity;

import android.app.Activity;
import android.os.Bundle;

import com.therabbitmage.android.beacon.BeaconApp;

public abstract class BaseActivity extends Activity {
	
	protected BeaconApp mApp;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mApp = BeaconApp.getInstance();
	}

}
