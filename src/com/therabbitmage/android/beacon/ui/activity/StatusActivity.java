package com.therabbitmage.android.beacon.ui.activity;

import android.app.Activity;
import android.os.Bundle;

import com.therabbitmage.android.beacon.BuildConfig;
import com.therabbitmage.android.beacon.R;
import com.therabbitmage.android.beacon.utils.AndroidUtils;

public class StatusActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if(BuildConfig.DEBUG){
			AndroidUtils.enableStrictMode();
		}
		super.onCreate(savedInstanceState);
		setContentView(R.layout.status_activity);
	}

}
