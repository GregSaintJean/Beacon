package com.therabbitmage.android.beacon.ui.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.therabbitmage.android.beacon.BuildConfig;
import com.therabbitmage.android.beacon.utils.AndroidUtils;

public class HistoryActivity extends ActionBarActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if(BuildConfig.DEBUG){
			AndroidUtils.enableStrictMode();
		}
		super.onCreate(savedInstanceState);
	}

}
