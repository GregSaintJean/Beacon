package com.therabbitmage.android.beacon.ui.activity;

import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;

public abstract class BaseActivity extends ActionBarActivity {
	
	private static final String TAG =  BaseActivity.class.getSimpleName();
	
	private LocalBroadcastManager mLocalBMgr;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mLocalBMgr = LocalBroadcastManager.getInstance(this);
	}
	
	public LocalBroadcastManager getLocalBroadcastManager(){
		return mLocalBMgr;
	}

}
