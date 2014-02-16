package com.therabbitmage.android.beacon;

import android.app.Application;

import com.therabbitmage.android.beacon.utils.AndroidUtils;

public class BeaconApp extends Application {
	
	private boolean mHasNetworkLocationProvider;
	private boolean mHasGPSLocationProvider;
	private boolean isConnected;
	
	private static final String US_EMERGENCY_NUMBER = "911";

	@Override
	public void onCreate() {
		super.onCreate();
	}
	
	public final void sms911(String number, String message){
		AndroidUtils.sendSMS(this, US_EMERGENCY_NUMBER, message);
	}
	
	public final void dial911(){
		AndroidUtils.dialNumber(this, US_EMERGENCY_NUMBER);
	}
}
