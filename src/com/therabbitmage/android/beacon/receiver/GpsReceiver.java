package com.therabbitmage.android.beacon.receiver;

import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.util.Log;

public class GpsReceiver extends BaseBroadcastReceiver {
	
	private static final String TAG = GpsReceiver.class.getSimpleName();

	public GpsReceiver(){}
	
	private OnGpsChangeListener mListener;

	@Override
	public void onReceive(Context context, Intent intent) {
		
		if(context == null){
			Log.e(TAG, "Context in receiver was null");
			return;
		}
		
		if(intent == null){
			Log.e(TAG, "Intent was null");
			return;
		}
		
		if(mListener == null){
			Log.e(TAG, "GpsReceiver's listener was null");
			return;
		}
		
		if(intent.getAction().equals(LocationManager.PROVIDERS_CHANGED_ACTION)){
			mListener.onGpsChange();
		}
		
	}
	
	public void setOnGpsChangeListener(OnGpsChangeListener listener){
		mListener = listener;
	}

}
