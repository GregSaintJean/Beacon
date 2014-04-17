package com.therabbitmage.android.beacon.utils;

import android.content.Context;
import android.os.Bundle;

import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.therabbitmage.android.beacon.R;

//TODO Document
public class LocationHelper {
	
	private Context mCtx;
	private LocationClient mLocationClient;
	private LocationRequest mLocationRequest;
	private LocationListener mLocationListener;
	private long mUpdateInterval, mFastestInterval;
	private int mPriority;
	private boolean isRequestingLocationUpdates;
	private GooglePlayServicesClient.ConnectionCallbacks mConnectionCallbacks;
	private GooglePlayServicesClient.OnConnectionFailedListener mConnectionFailedListener;
	private boolean mWaitingForConnection;
	
	public LocationHelper(Context ctx, GooglePlayServicesClient.ConnectionCallbacks connectionCallbacks, 
			GooglePlayServicesClient.OnConnectionFailedListener connectionFailedListener,
			LocationListener locationListener){
		mCtx = ctx;
		mConnectionCallbacks = connectionCallbacks;
		mConnectionFailedListener = connectionFailedListener;
		mLocationListener = locationListener;
		mUpdateInterval = mFastestInterval = -1;
		isRequestingLocationUpdates = mWaitingForConnection = false;
	}
	
	public Context getContext(){
		return mCtx;
	}
	
	public void setUpdateInterval(long updateInterval){
		
		if(updateInterval == -1){
			throw new IllegalArgumentException(mCtx.getString(R.string.error_invalid_update_interval));
		}
		
		mUpdateInterval = updateInterval;
		if(mUpdateInterval < mFastestInterval
				|| mFastestInterval == -1){
			mFastestInterval = mUpdateInterval;
		}
		
		if(isRequestingLocationUpdates){
			reset();
		}
		
	}
	
	public long getUpdateInterval(){
		return mUpdateInterval;
	}
	
	public void setFastestInterval(long fastestInterval){
		
		if(fastestInterval == -1){
			throw new IllegalArgumentException(mCtx.getString(R.string.error_invalid_update_interval));
		}
		
		mFastestInterval = fastestInterval;
		if(mFastestInterval > mUpdateInterval
				|| mUpdateInterval == -1){
			mUpdateInterval = mFastestInterval;
		}
		
		if(isRequestingLocationUpdates){
			reset();
		}
	}
	
	public long getFastestInterval(){
		return mFastestInterval;
	}
	
	public void setPriority(int priority){
		
		switch(priority){
			case LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY:
			case LocationRequest.PRIORITY_HIGH_ACCURACY:
			case LocationRequest.PRIORITY_LOW_POWER:
			case LocationRequest.PRIORITY_NO_POWER:
				mPriority = priority;
				break;
			default:
				throw new IllegalArgumentException(mCtx.getString(R.string.error_invalid_priority));
		}
		
		if(isRequestingLocationUpdates){
			reset();
		}
	}
	
	private void updateClient(){
		mLocationClient = new LocationClient(mCtx, mConnectionCallbacks, mConnectionFailedListener);
		mLocationRequest = LocationRequest.create();
		mLocationRequest.setInterval(mUpdateInterval);
		mLocationRequest.setFastestInterval(mFastestInterval);
		mLocationRequest.setPriority(mPriority);
	}
	
	public int getPriority(){
		return mPriority;
	}
	
	public void onConnected(Bundle bundle){
		if(mLocationClient != null
				&& mLocationClient.isConnected()
				&& !isRequestingLocationUpdates){
			requestLocationUpdates();
			mWaitingForConnection = false;
		}
	}
	
	public void requestLocationUpdates(){
		
		if(mUpdateInterval == -1){
			throw new IllegalStateException(mCtx.getString(R.string.error_state_update_interval));
		} else if(mFastestInterval ==  -1 && mUpdateInterval != -1){
			mFastestInterval = mUpdateInterval;
		}
		
		mLocationClient.requestLocationUpdates(mLocationRequest, mLocationListener);
		isRequestingLocationUpdates = true;
	}
	
	public void stopRequestLocationUpdates(){
		mLocationClient.removeLocationUpdates(mLocationListener);
		isRequestingLocationUpdates = false;
	}
	
	public void reset(){
		if(mLocationClient != null){
			stopRequestLocationUpdates();
			mLocationClient.disconnect();
		}
		mWaitingForConnection = false;
		updateClient();
		mLocationClient.connect();
		mWaitingForConnection = true;
	}
	
	public void shutdownLocationHelper(){
		stopRequestLocationUpdates();
		mLocationClient.disconnect();
	}

}
