package com.therabbitmage.android.beacon.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.util.Log;

public class NetworkReceiver extends BroadcastReceiver {
	
	private static final String TAG = NetworkReceiver.class.getSimpleName();

	public NetworkReceiver(){}
	
	private OnNetworkChangeListener mListener;

	@Override
	public void onReceive(Context context, Intent intent) {
		
		if(context == null){
			Log.e(TAG, "Context in receiver was null");
			return;
		}
		
		if(mListener == null){
			Log.e(TAG, "NetworkReceiver's listener was null");
			return;
		}
		
		if(intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)){
			mListener.onNetworkChange();
		}
	}
	
	public void setOnNetworkChangeListener(OnNetworkChangeListener listener){
		mListener = listener;
	}

}
