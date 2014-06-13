package com.therabbitmage.android.beacon.receiver;

import android.content.BroadcastReceiver;

import com.therabbitmage.android.beacon.SignalApp;

public abstract class BaseBroadcastReceiver extends BroadcastReceiver{
	protected static final boolean IS_DEBUG = SignalApp.isDebug();
}
