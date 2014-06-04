package com.therabbitmage.android.beacon.receiver;

import com.therabbitmage.android.beacon.SignalApp;
import com.therabbitmage.android.beacon.R;
import com.therabbitmage.android.beacon.service.SignalService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class TransmitReceiver extends BroadcastReceiver {
	
	private static final String TAG = TransmitReceiver.class.getSimpleName();

	@Override
	public void onReceive(Context context, Intent intent) {
		if (context == null) {
			Log.e(TAG, SignalApp.getInstance().getString(R.string.logcat_context_receiver_null));
			return;
		}

		if (intent == null) {
			Log.e(TAG, SignalApp.getInstance().getString(R.string.logcat_intent_null));
			return;
		}
		
		if(intent.getAction().equals(SignalService.ACTION_TRANSMIT)){
			Log.d(TAG, "Transmission request received");
			/*TODO Write logic that checks if the Beacon is suppose to be on and if it is turn it back on.
			*/ 
			if(SignalService.getInstance() != null){
				SignalService.getInstance().transmitBeacon();
			}
			return;
		}
	}

}
