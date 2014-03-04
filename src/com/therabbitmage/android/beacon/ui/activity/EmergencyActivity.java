package com.therabbitmage.android.beacon.ui.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.therabbitmage.android.beacon.BuildConfig;
import com.therabbitmage.android.beacon.R;
import com.therabbitmage.android.beacon.service.BeaconService;
import com.therabbitmage.android.beacon.utils.AndroidUtils;

public class EmergencyActivity extends FragmentActivity {

	protected static final String TAG = EmergencyActivity.class.getSimpleName();

	private GoogleMap mGoogleMap;
	
	private BeaconService mBeaconService;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if(BuildConfig.DEBUG){
			AndroidUtils.enableStrictMode();
		}
		super.onCreate(savedInstanceState);
		setContentView(R.layout.emergency_activity);

		if (mGoogleMap == null) {
			mGoogleMap = ((SupportMapFragment) getSupportFragmentManager()
					.findFragmentById(R.id.map)).getMap();
			mGoogleMap.setMyLocationEnabled(true);
		}
		
		startBeaconService();
		
	}
	
	private void startBeaconService(){
		//TODO need to also do startService
		
		if(mBeaconService == null){
			bindService(new Intent(this, BeaconService.class), mConnection, Context.BIND_AUTO_CREATE);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(mBeaconService != null){
			unbindService(mConnection);
		}
	}
	
	private ServiceConnection mConnection = new ServiceConnection(){

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mBeaconService = ((BeaconService.LocalBinder) service).getService();
			
			if(BuildConfig.DEBUG){
				Log.d(TAG, "Beacon Service donnected");
			}
			
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			
			mBeaconService = null;
			
			if(BuildConfig.DEBUG){
				Log.d(TAG, "Beacon Service disconnected");
			}
			
		}
		
	};

}
