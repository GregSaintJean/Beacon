package com.therabbitmage.android.beacon.service;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.therabbitmage.android.beacon.utils.LocationUtils;

public class BeaconService extends Service implements LocationListener,
	GooglePlayServicesClient.ConnectionCallbacks,
	GooglePlayServicesClient.OnConnectionFailedListener{
	
	private static final String TAG = BeaconService.class.getSimpleName();
	
	private LocationRequest mLocationRequest;
	private LocationClient mLocationClient;
	
	private NotificationManager mNm;
	
	private volatile Looper mServiceLooper;
	private volatile ServiceHandler mServiceHandler;
	
	private final class ServiceHandler extends Handler{
		public ServiceHandler(Looper looper){
			super(looper);
		}
		
		@Override
		public void handleMessage(Message msg) {
			
			Bundle arguments = (Bundle)msg.obj;
			//TODO Complete, this is where commands will be passed.
		}
	}
	
	public class LocalBinder extends Binder{
		public BeaconService getService(){
			return BeaconService.this;
		}
	}
	
	private final IBinder mBinder = new LocalBinder();
	
	@Override
	public void onCreate(){
		
		mNm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		
		HandlerThread thread = new HandlerThread(BeaconService.class.getSimpleName(), 
				Process.THREAD_PRIORITY_BACKGROUND);
		thread.start();
		mServiceLooper = thread.getLooper();
		mServiceHandler = new ServiceHandler(mServiceLooper);
		
		mLocationRequest = LocationRequest.create();
		
		mLocationRequest.setInterval(LocationUtils.UPDATE_INTERVAL);
		
		mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		
		mLocationRequest.setFastestInterval(LocationUtils.FASTEST_INTERVAL);
		
		mLocationClient = new LocationClient(this, this, this);
		
		mLocationClient.connect();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		Message msg = mServiceHandler.obtainMessage();
		msg.arg1 = startId;
		msg.arg2 = flags;
		msg.obj = intent.getExtras();
		mServiceHandler.sendMessage(msg);
		
		return START_STICKY;
	}
	
	@Override
	public void onConnectionFailed(ConnectionResult result) {
		
		//TODO handle these
		switch(result.getErrorCode()){
			case ConnectionResult.DATE_INVALID:
				break;
			case ConnectionResult.DEVELOPER_ERROR:
				break;
			case ConnectionResult.INTERNAL_ERROR:
				break;
			case ConnectionResult.INVALID_ACCOUNT:
				break;
			case ConnectionResult.LICENSE_CHECK_FAILED:
				break;
			case ConnectionResult.NETWORK_ERROR:
				break;
			case ConnectionResult.RESOLUTION_REQUIRED:
				break;
			case ConnectionResult.SERVICE_DISABLED:
				break;
			case ConnectionResult.SERVICE_INVALID:
				break;
			case ConnectionResult.SERVICE_MISSING:
				break;
			case ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED:
				break;
			case ConnectionResult.SIGN_IN_REQUIRED:
				break;
		}
		
	}

	@Override
	public void onConnected(Bundle bundle) {
		mLocationClient.requestLocationUpdates(mLocationRequest, this);
		Log.e(TAG, "Location services connected");
	}

	@Override
	public void onDisconnected() {
		Log.e(TAG, "Location services disconnected");
	}

	@Override
	public void onLocationChanged(Location location) {
		
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mLocationClient.removeLocationUpdates(this);
		mLocationClient.disconnect();
		mServiceLooper.quit();
	}

}
