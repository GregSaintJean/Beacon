package com.therabbitmage.android.beacon;

import org.apache.http.protocol.HTTP;

import android.app.Application;
import android.content.Intent;
import android.net.Uri;

public class BeaconApp extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
	}
	
	public final void sms911(String number, String message){
		if(number == null){
			throw new NullPointerException(getString(R.string.error_phone_number_required));
		}
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setData(Uri.parse("sms:" + number));
		intent.setType(HTTP.PLAIN_TEXT_TYPE);
		intent.putExtra("sms_body", message);
		if(intent.resolveActivity(getPackageManager()) != null){
			startActivity(intent);
		}
		
	}

}
