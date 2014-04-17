package com.therabbitmage.android.beacon.service;

import android.os.Handler;
import android.os.Looper;
import android.os.ResultReceiver;

public class MainReceiver extends ResultReceiver {
	
	public MainReceiver(){
		super(new Handler(Looper.getMainLooper()));
	}

}
