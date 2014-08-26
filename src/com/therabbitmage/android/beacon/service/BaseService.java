package com.therabbitmage.android.beacon.service;

import android.app.Service;

import com.therabbitmage.android.beacon.BeaconApp;

public abstract class BaseService extends Service {
	protected static final boolean IS_DEBUG = BeaconApp.isDebug();
}
