package com.therabbitmage.android.beacon.ui.activity;

import com.therabbitmage.android.beacon.BuildConfig;
import com.therabbitmage.android.beacon.utils.AndroidUtils;

import android.app.Activity;
import android.os.Bundle;

public class HistoryActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if(BuildConfig.DEBUG){
			AndroidUtils.enableStrictMode();
		}
		super.onCreate(savedInstanceState);
	}

}
