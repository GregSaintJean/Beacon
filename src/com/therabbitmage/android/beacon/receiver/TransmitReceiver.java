package com.therabbitmage.android.beacon.receiver;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.therabbitmage.android.beacon.R;
import com.therabbitmage.android.beacon.BeaconApp;
import com.therabbitmage.android.beacon.service.SignalService;

public class TransmitReceiver extends BaseBroadcastReceiver {
	
	private static final String TAG = TransmitReceiver.class.getSimpleName();

	@Override
	public void onReceive(Context context, Intent intent) {
		
		if (context == null) {
			Log.e(TAG, BeaconApp.getInstance().getString(R.string.logcat_context_receiver_null));
			return;
		}

		if (intent == null) {
			Log.e(TAG, BeaconApp.getInstance().getString(R.string.logcat_intent_null));
			return;
		}
		
		if(intent.getAction().equals(SignalService.ACTION_TRANSMIT)){
			if(IS_DEBUG){
				Log.d(TAG, context.getString(R.string.logcat_transmission_request_received));
			}
			//TODO Write logic that checks if the Beacon is suppose to be on and if it is turn it back on. 
			if(SignalService.getInstance() != null){
				SignalService.getInstance().transmitBeacon();
			}
			return;
		}
		
		if(intent.getAction().equals(SignalService.ACTION_DELAY_SMS_TRANSMIT)){
			if(IS_DEBUG){
				Log.d(TAG, context.getString(R.string.logcat_transmission_delay_sms_received));
			}
			//TODO Implement
			return;
		}
		
		if(intent.getAction().equals(SignalService.ACTION_DELAY_TWITTER_TRANSMIT)){
			if(IS_DEBUG){
				Log.d(TAG, context.getString(R.string.logcat_transmission_delay_twitter_received));
			}
			//TODO Implement
			return;
		}
		
		if(intent.getAction().equals(SignalService.ACTION_DELAY_SMS_TRANSMIT_GPS)){
			if(IS_DEBUG){
				Log.d(TAG, context.getString(R.string.logcat_transmission_delay_sms_received_gps));
			}
			//TODO Implement
			return;
		}
		
		if(intent.getAction().equals(SignalService.ACTION_DELAY_TWITTER_TRANSMIT_GPS)){
			if(IS_DEBUG){
				Log.d(TAG, context.getString(R.string.logcat_transmission_delay_twitter_received_gps));
			}
			//TODO Implement
			return;
		}
		
		if(intent.getAction().equals(SignalService.ACTION_DELAY_FIRST_MESSAGE_TWITTER)){
			
			if(intent.getExtras().getString(SignalService.EXTRA_TRANSMIT_TYPE).equals(SignalService.EXTRA_TRANSMIT_FIRST_TWITTER)){
				
				
				
			} else if (intent.getExtras().getString(SignalService.EXTRA_TRANSMIT_TYPE).equals(SignalService.EXTRA_TRANSMIT_FIRST_TWITTER_GPS)){
				
				
				
			}
			
			return;
		}

	}

}
