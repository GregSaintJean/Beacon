package com.therabbitmage.android.beacon.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.therabbitmage.android.beacon.BeaconApp;
import com.therabbitmage.android.beacon.R;
import com.therabbitmage.android.beacon.receiver.GpsReceiver;
import com.therabbitmage.android.beacon.receiver.NetworkReceiver;
import com.therabbitmage.android.beacon.receiver.OnGpsChangeListener;
import com.therabbitmage.android.beacon.receiver.OnNetworkChangeListener;
import com.therabbitmage.android.beacon.service.BeaconService;
import com.therabbitmage.android.beacon.utils.TimeUtils;

public class NewMainActivity extends BaseFragmentActivity implements OnClickListener{
	
	private static final String TAG = NewMainActivity.class.getSimpleName();
	
	private static final int ZOOM_LEVEL = 13;
	
	private GoogleMap mGoogleMap;
	private BroadcastReceiver mNetworkReceiver, mGpsReceiver, mBeaconKillReceiver,
	mBeaconReceiver, mCoordinateReceiver;
	private LocalBroadcastManager mLocalBMgr;
	private View mInfoContainer, mIntroContainer;
	private TextView mLocationView, mStatus;
	private EditText mInput;
	private Button mMainButton, mAltSetupButton;
	private StringBuilder mStatusBuilder;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setupUI();
		registerReceivers();
		mStatusBuilder = new StringBuilder();
	}
	
	private void setupUI(){
		
		setContentView(R.layout.new_main_activity);
		
		if (mGoogleMap == null) {
			mGoogleMap = ((SupportMapFragment) getSupportFragmentManager()
					.findFragmentById(R.id.map)).getMap();
			mGoogleMap.setMyLocationEnabled(true);
			mGoogleMap.getUiSettings().setAllGesturesEnabled(false);
			mGoogleMap.getUiSettings().setMyLocationButtonEnabled(false);
			mGoogleMap.getUiSettings().setZoomControlsEnabled(false);
		}
		
		mInfoContainer = findViewById(R.id.info_container);
		mIntroContainer = findViewById(R.id.intro_container);
		mLocationView = (TextView)findViewById(R.id.location_view);
		mStatus = (TextView)findViewById(R.id.status);
		mMainButton = (Button)findViewById(R.id.mainbtn);
		mAltSetupButton = (Button)findViewById(R.id.alt_setup_btn);
		mInput = (EditText)findViewById(R.id.input_edittxt);
		//TODO Setup logic for when the user is typing in their message
		
		mMainButton.setOnClickListener(this);
		mAltSetupButton.setOnClickListener(this);
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		
		if(!BeaconApp.isBeaconOnline())
			showIntro();
		else
			showInfo();
	}

	private void showInfo(){
		getActionBar().show();
		mIntroContainer.setVisibility(View.GONE);
		mInfoContainer.setVisibility(View.VISIBLE);
		mLocationView.setVisibility(View.VISIBLE);
	}
	
	private void showIntro(){
		getActionBar().hide();
		mIntroContainer.setVisibility(View.VISIBLE);
		mInfoContainer.setVisibility(View.GONE);
		mLocationView.setVisibility(View.GONE);
		
		if(mApp.isSetupDone()){
			mMainButton.setText(R.string.start);
			mAltSetupButton.setVisibility(View.VISIBLE);
		} else {
			mMainButton.setText(R.string.setup);
			mAltSetupButton.setVisibility(View.GONE);
		}
	}
	
	@Override
	public void onClick(View v) {
		
		if(mMainButton != null && v.getId() == mMainButton.getId()){
			
			if(mApp.isSetupDone()){
				fireBeacon();
				showInfo();
			} else {
				startSetupActivity();
			}
			
		}
		
		if(mAltSetupButton != null && v.getId() == mAltSetupButton.getId()){
			if(mApp.isSetupDone()){
				startSetupActivity();
			}
		}
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		MenuItem settingsItem = menu.findItem(R.id.action_settings);
		MenuItem shutdownItem = menu.findItem(R.id.shutdown);
		MenuItem contactItem = menu.findItem(R.id.contact);
		
		if(BeaconApp.isBeaconOnline() || mApp.isSetupDone())
			settingsItem.setVisible(true);
		else 
			settingsItem.setVisible(false);
		
		if(BeaconApp.isBeaconOnline()){
			shutdownItem.setVisible(true);
			contactItem.setVisible(true);
		} else {
			shutdownItem.setVisible(false);
			contactItem.setVisible(false);
		}
		
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		switch(item.getItemId()){
			case R.id.action_settings:
				startSetupActivity();
				return true;
			case R.id.shutdown:
				shutdownBeacon();
				return true;
			case R.id.contact:
				mApp.dial911();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	
	private void shutdownBeacon(){
		try{
			Intent shutdownIntent = new Intent(this, BeaconService.class);
			shutdownIntent.setAction(BeaconService.ACTION_STOP);
			startService(shutdownIntent);
		} finally{
			finish();
		}
	}
	
	private void startSetupActivity() {
		startActivity(new Intent(this, PhoneSetupActivity.class));
	}
	
	private void fireBeacon(){
		startService(new Intent(this, BeaconService.class));
	}
	
	private void setLocationOnMap(double lat, double lng){
		
		if(mGoogleMap == null){
			throw new IllegalStateException(getString(R.string.error_google_map_initialized));
		}
		
		LatLng coord = new LatLng(lat, lng);
		
		CameraPosition.Builder builder = new CameraPosition.Builder();
		builder.target(coord);
		builder.zoom(ZOOM_LEVEL);
		CameraUpdate update = CameraUpdateFactory.newCameraPosition(builder.build());
		mGoogleMap.animateCamera(update);
	}
	
	private void setLocationOnLocationView(double lat, double lng){
		//TODO Change text so that it puts down the address as well and make it an Android string.
		mLocationView.setText("Location: Latitude: " + lat +" Longtitude: " + lng);
	}
	
	private void updateStatusView(String update){
		mStatusBuilder.append(TimeUtils.getCurrentTime() + ": " + update + "\n");
		mStatus.setText(mStatusBuilder.toString());
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceivers();
	}
	
	private void registerReceivers(){
		
		mLocalBMgr = LocalBroadcastManager.getInstance(this);
		mBeaconReceiver = new BeaconReceiver();
		mCoordinateReceiver = new CoordinateReceiver();
		mBeaconKillReceiver = new BeaconKillReceiver();
		
		IntentFilter filter = new IntentFilter(BeaconService.BROADCAST_BEACON_MESSAGE);
		mLocalBMgr.registerReceiver(mBeaconReceiver, filter);
		filter = new IntentFilter(BeaconService.BROADCAST_COORDINATES);
		mLocalBMgr.registerReceiver(mCoordinateReceiver, filter);
		filter = new IntentFilter(BeaconService.BROADCAST_SERVICE_KILLED);
		mLocalBMgr.registerReceiver(mBeaconKillReceiver, filter);
		
		mNetworkReceiver = new NetworkReceiver();
		((NetworkReceiver)mNetworkReceiver).setOnNetworkChangeListener(mNetworkChangeListener);
		registerReceiver(mNetworkReceiver, 
				new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
		mGpsReceiver = new GpsReceiver();
		((GpsReceiver)mGpsReceiver).setOnGpsChangeListener(mGpsChangeListener);
		registerReceiver(mGpsReceiver, 
				new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION));
	}
	
	private OnNetworkChangeListener mNetworkChangeListener = new OnNetworkChangeListener(){

		@Override
		public void onNetworkChange() {
			Log.d(TAG, "Network connectivity changed detected");
			if(NewMainActivity.this == null){
				return;
			}
			//TODO Handle Network Offline
		}
		
	};
	
	private OnGpsChangeListener mGpsChangeListener = new OnGpsChangeListener(){

		@Override
		public void onGpsChange() {
			if(NewMainActivity.this == null){
				return;
			}
			Log.d(TAG, "GPS Provider change detected.");
			//TODO Handle GPS offline
		}
		
	};
	
	private void unregisterReceivers(){
		mLocalBMgr.unregisterReceiver(mBeaconReceiver);
		mLocalBMgr.unregisterReceiver(mCoordinateReceiver);
		mLocalBMgr.unregisterReceiver(mBeaconKillReceiver);
		unregisterReceiver(mNetworkReceiver);
		unregisterReceiver(mGpsReceiver);
	}
	
	private class BeaconKillReceiver extends BroadcastReceiver{
		
		public void onReceive(Context context, Intent intent){
			
			if(context == null){
				return;
			}
			
			if(intent.getAction().equals(BeaconService.BROADCAST_SERVICE_KILLED)){
				
				if(context == null || NewMainActivity.this == null){
					return;
				}
				
				NewMainActivity.this.finish();
			}
			
		}
		
	}
	
	private class BeaconReceiver extends BroadcastReceiver{
		
		public BeaconReceiver(){}

		@Override
		public void onReceive(Context context, Intent intent) {
			
			if(context == null){
				return;
			}
			
			if(intent.getAction().equals(BeaconService.BROADCAST_BEACON_MESSAGE)){
				
				NewMainActivity activity = (NewMainActivity)context;
				activity.updateStatusView(intent.getStringExtra(BeaconService.EXTRA_BROADCAST_MESSAGE));
			}
			
		}
		
	}
	
	private class CoordinateReceiver extends BroadcastReceiver{
		
		public CoordinateReceiver(){}

		@Override
		public void onReceive(Context context, Intent intent) {
			
			if(context == null){
				return;
			}
			
			double lat = intent.getDoubleExtra(BeaconService.EXTRA_BROADCAST_LATITUDE, 0);
			double lng = intent.getDoubleExtra(BeaconService.EXTRA_BROADCAST_LONGITUDE, 0);
			
			NewMainActivity activity = (NewMainActivity)context;
			activity.setLocationOnMap(lat, lng);
			activity.setLocationOnLocationView(lat, lng);
			
		}
		
	}

}
