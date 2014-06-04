package com.therabbitmage.android.beacon.ui.activity;

import com.google.analytics.tracking.android.EasyTracker;
import com.therabbitmage.android.beacon.SignalApp;

import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;

public abstract class BaseActivity extends ActionBarActivity {

	private static final String TAG =  BaseActivity.class.getSimpleName();
	protected static final boolean IS_DEBUG = SignalApp.isDebug();
	
	private LocalBroadcastManager mLocalBMgr;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mLocalBMgr = LocalBroadcastManager.getInstance(this);
	}
	
	@Override
	protected void onStart(){
		super.onStart();
		EasyTracker.getInstance(this).activityStart(this);
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		EasyTracker.getInstance(this).activityStop(this);
	}
	
	public LocalBroadcastManager getLocalBroadcastManager(){
		return mLocalBMgr;
	}

}
