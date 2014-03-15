package com.therabbitmage.android.beacon.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.therabbitmage.android.beacon.BeaconApp;
import com.therabbitmage.android.beacon.R;
import com.therabbitmage.android.beacon.service.BeaconService;
import com.therabbitmage.android.beacon.utils.TimeUtils;

public class EmergencyActivity extends FragmentActivity implements OnClickListener {

	protected static final String TAG = EmergencyActivity.class.getSimpleName();
	
	private Session.StatusCallback mStatusCallback = new SessionStatusCallback();

	private GoogleMap mGoogleMap;
	
	private UiLifecycleHelper uiHelper;
	private BroadcastReceiver mBeaconReceiver;
	private BroadcastReceiver mFacebookReceiver;
	private BroadcastReceiver mCoordinateReceiver;
	private BroadcastReceiver mBeaconKillReceiver;
	private LocalBroadcastManager mLocalBMgr;
	private TextView mStatus;
	
	private StringBuilder mStatusBuilder;
	
	private BeaconApp mBeaconApp;
	
	private Button m911Button;
	private Button mShutdownButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		/*if(BuildConfig.DEBUG){
			AndroidUtils.enableStrictMode();
		}*/
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.emergency_activity);
		
		mBeaconApp = (BeaconApp)getApplicationContext();

		if (mGoogleMap == null) {
			mGoogleMap = ((SupportMapFragment) getSupportFragmentManager()
					.findFragmentById(R.id.map)).getMap();
			mGoogleMap.setMyLocationEnabled(true);
			mGoogleMap.getUiSettings().setAllGesturesEnabled(false);
			mGoogleMap.getUiSettings().setMyLocationButtonEnabled(false);
			mGoogleMap.getUiSettings().setZoomControlsEnabled(false);
		}
		
		mStatus = (TextView)findViewById(R.id.status);
		m911Button = (Button)findViewById(R.id.call911_btn);
		mShutdownButton = (Button)findViewById(R.id.shutdown_btn);
		m911Button.setOnClickListener(this);
		mShutdownButton.setOnClickListener(this);
		mStatusBuilder = new StringBuilder();
		mLocalBMgr = LocalBroadcastManager.getInstance(this);
		registerReceivers();
		startBeaconService();
		uiHelper = new UiLifecycleHelper(this, mStatusCallback);
	    uiHelper.onCreate(savedInstanceState);
		
	}
	
	private void registerReceivers(){
		mBeaconReceiver = new BeaconReceiver();
		mFacebookReceiver = new FacebookReceiver();
		mCoordinateReceiver = new CoordinateReceiver();
		mBeaconKillReceiver = new BeaconKillReceiver();
		
		IntentFilter filter = new IntentFilter(BeaconService.BROADCAST_BEACON_MESSAGE);
		mLocalBMgr.registerReceiver(mBeaconReceiver, filter);
		
		filter = new IntentFilter(BeaconService.BROADCAST_FACEBOOK_MESSAGE);
		mLocalBMgr.registerReceiver(mFacebookReceiver, filter);
		
		filter = new IntentFilter(BeaconService.BROADCAST_COORDINATES);
		mLocalBMgr.registerReceiver(mCoordinateReceiver, filter);
		
		filter = new IntentFilter(BeaconService.BROADCAST_SERVICE_KILLED);
		mLocalBMgr.registerReceiver(mBeaconKillReceiver, filter);
	}
	
	private void unregisterReceivers(){
		mLocalBMgr.unregisterReceiver(mBeaconReceiver);
		mLocalBMgr.unregisterReceiver(mFacebookReceiver);
		mLocalBMgr.unregisterReceiver(mCoordinateReceiver);
		mLocalBMgr.unregisterReceiver(mBeaconKillReceiver);
	}
	
	private void startBeaconService(){
		startService(new Intent(this, BeaconService.class));
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		uiHelper.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		uiHelper.onResume();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceivers();
	}
	
	@Override
	public void onClick(View v) {
		
		if(v.getId() == R.id.call911_btn){
			mBeaconApp.dial911();
		}
		
		if(v.getId() == R.id.shutdown_btn){
			Intent shutdownIntent = new Intent(this, BeaconService.class);
			shutdownIntent.setAction(BeaconService.ACTION_STOP);
			startService(shutdownIntent);
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		//Facebook SDK
		uiHelper.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		uiHelper.onSaveInstanceState(outState);
	}
	
	//Facebook SDK
	private class SessionStatusCallback implements Session.StatusCallback{

		@Override
		public void call(Session session, SessionState state, Exception exception) {
			onSessionStateChange(session, state, exception);
		}
			
	}
	
	private void onSessionStateChange(Session session, SessionState state, Exception exception) {
	    if (state.isOpened()) {
	        Log.i(TAG, "Logged in...");
	    } else if (state.isClosed()) {
	        Log.i(TAG, "Logged out...");
	    }
	}
	
	private class BeaconReceiver extends BroadcastReceiver{
		
		public BeaconReceiver(){}

		@Override
		public void onReceive(Context context, Intent intent) {
			
			if(intent.getAction().equals(BeaconService.BROADCAST_BEACON_MESSAGE)){
				
				String currentTime = TimeUtils.getCurrentTime();
				
				String message = intent.getStringExtra(BeaconService.EXTRA_BROADCAST_MESSAGE);
				mStatusBuilder.append(currentTime + ": " + message + "\n");
				mStatus.setText(mStatusBuilder.toString());
			}
			
		}
		
	}
	
	private class FacebookReceiver extends BroadcastReceiver{
		
		public FacebookReceiver(){}

		@Override
		public void onReceive(Context context, Intent intent) {
			
			if(intent.getAction().equals(BeaconService.BROADCAST_FACEBOOK_MESSAGE)){
				//TODO attempt to post to Facebook
			}
			
		}
		
	}
	
	private class CoordinateReceiver extends BroadcastReceiver{
		
		public CoordinateReceiver(){}

		@Override
		public void onReceive(Context context, Intent intent) {
			
			double lat = intent.getDoubleExtra(BeaconService.EXTRA_BROADCAST_LATITUDE, 0);
			double lng = intent.getDoubleExtra(BeaconService.EXTRA_BROADCAST_LONGITUDE, 0);
			LatLng coord = new LatLng(lat, lng);
			
			CameraPosition.Builder builder = new CameraPosition.Builder();
			builder.target(coord);
			builder.zoom(13);
			CameraUpdate update = CameraUpdateFactory.newCameraPosition(builder.build());
			mGoogleMap.animateCamera(update);
			
		}
		
	}
	
	private class BeaconKillReceiver extends BroadcastReceiver{
		
		public void onReceive(Context context, Intent intent){
			
			if(intent.getAction().equals(BeaconService.BROADCAST_SERVICE_KILLED)){
				EmergencyActivity.this.finish();
			}
			
		}
		
	}

}
