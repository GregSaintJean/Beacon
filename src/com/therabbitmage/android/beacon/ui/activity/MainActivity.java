package com.therabbitmage.android.beacon.ui.activity;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

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
import com.therabbitmage.android.beacon.utils.AndroidUtils;
import com.therabbitmage.android.beacon.utils.ChronoUtils;

public class MainActivity extends BaseActionBarActivity implements OnClickListener{
	
	private static final String TAG = MainActivity.class.getSimpleName();
	
	private static final int ZOOM_LEVEL = 13;
	
	private GoogleMap mGoogleMap;
	private BroadcastReceiver mNetworkReceiver, mGpsReceiver, mBeaconKillReceiver,
	mBeaconReceiver, mCoordinateReceiver;
	private LocalBroadcastManager mLocalBMgr;
	private View mIntroContainer, mInfoContainer, mErrorContainer;
	private TextView mLocationView, mStatus, mErrorNetwork, mErrorGps;
	private EditText mInput;
	private Button mMainBtn, mAltSetupBtn, mSettingsBtn;
	private StringBuilder mStatusBuilder;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(mApp.hasGpsCapability())
		{
			mApp.setGpsOnline(AndroidUtils.isGpsOnline(this));
		}
		mApp.setHasNetworkConnectivity(AndroidUtils.hasNetworkConnectivity(this));
		setupUI();
		registerReceivers();
		mStatusBuilder = new StringBuilder();
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		if(!BeaconApp.isActive())
			showIntro();
		else
			showInfo();
	}

	private void setupUI(){
		
		setContentView(R.layout.activity_main);
		
		if (mGoogleMap == null) {
			mGoogleMap = ((SupportMapFragment) getSupportFragmentManager()
					.findFragmentById(R.id.map)).getMap();
			mGoogleMap.getUiSettings().setAllGesturesEnabled(false);
			mGoogleMap.getUiSettings().setMyLocationButtonEnabled(false);
			mGoogleMap.getUiSettings().setZoomControlsEnabled(false);
		}
		
		mInfoContainer = findViewById(R.id.info_container);
		mIntroContainer = findViewById(R.id.intro_container);
		mErrorContainer = findViewById(R.id.error_container);
		mLocationView = (TextView)findViewById(R.id.location_view);
		mStatus = (TextView)findViewById(R.id.status);
		mErrorNetwork = (TextView)findViewById(R.id.error_network_tv);
		mErrorGps = (TextView)findViewById(R.id.error_gps_tv);
		mMainBtn = (Button)findViewById(R.id.mainbtn);
		mAltSetupBtn = (Button)findViewById(R.id.alt_setup_btn);
		mSettingsBtn = (Button)findViewById(R.id.settings_btn);
		mInput = (EditText)findViewById(R.id.input_edittxt);
		mInput.setOnEditorActionListener(new OnEditorActionListener(){

			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				boolean handled = false;
				if(actionId == EditorInfo.IME_ACTION_SEND){
					if(TextUtils.isEmpty(v.getText())){
						handled = false;
					} else {
						handled = true;
						Intent messageIntent = new Intent(MainActivity.this, BeaconService.class);
						//messageIntent.setAction(BeaconService.ACTION_SEND_SMS_MESSAGE);
						messageIntent.setAction(BeaconService.ACTION_SEND_TWITTER_MESSAGE);
						messageIntent.putExtra(BeaconService.EXTRA_MESSAGE, v.getText().toString());
						startService(messageIntent);
					}
				}
				return handled;
			}
			
		});
		
		mMainBtn.setOnClickListener(this);
		mAltSetupBtn.setOnClickListener(this);
		mSettingsBtn.setOnClickListener(this);
		
		if(!AndroidUtils.isGoogleServicesAvailable(this)){
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.google_play_required)
			.setMessage(R.string.google_play_must_be_installed)
			.setNeutralButton(android.R.string.ok, new android.content.DialogInterface.OnClickListener(){

				@Override
				public void onClick(DialogInterface dialog, int which) {
					try{
						dialog.dismiss();
					} finally{
						MainActivity.this.finish();
					}
				}
				
			});
			builder.create().show();	
		}
	}

	private void showInfo(){
		getActionBar().show();
		mIntroContainer.setVisibility(View.GONE);
		mInfoContainer.setVisibility(View.VISIBLE);
		mLocationView.setVisibility(View.VISIBLE);
		mErrorContainer.setVisibility(View.GONE);
		invalidateOptionsMenu();
		refreshErrorContainer();
		mGoogleMap.setMyLocationEnabled(true);
	}
	
	private void showIntro(){
		getActionBar().hide();
		mIntroContainer.setVisibility(View.VISIBLE);
		mInfoContainer.setVisibility(View.GONE);
		mLocationView.setVisibility(View.GONE);
		
		if(mApp.isSetupDone()){
			mMainBtn.setText(R.string.start);
			mAltSetupBtn.setVisibility(View.VISIBLE);
			mSettingsBtn.setVisibility(View.VISIBLE);
		} else {
			mMainBtn.setText(R.string.setup);
			mAltSetupBtn.setVisibility(View.GONE);
			mSettingsBtn.setVisibility(View.GONE);
		}
		
		invalidateOptionsMenu();
		refreshErrorContainer();
		mGoogleMap.setMyLocationEnabled(false);
	}
	
	@Override
	public void onClick(View v) {
		
		if(mMainBtn != null && v.getId() == mMainBtn.getId()){
			
			if(mApp.isSetupDone()){
				fireBeacon();
				showInfo();
			} else {
				startSetupActivity();
			}
			
		}
		
		if(mAltSetupBtn != null && v.getId() == mAltSetupBtn.getId()){
			if(mApp.isSetupDone()){
				startSetupActivity();
			}
		}
		
		if(mSettingsBtn != null && v.getId() ==  mSettingsBtn.getId()){
			startSettingsActivity();
		}
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		
		MenuItem shutdownItem = menu.findItem(R.id.shutdown);
		shutdownItem.setVisible(BeaconApp.isActive());
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		switch(item.getItemId()){
			case R.id.shutdown:
				shutdownBeacon();
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
		startActivity(new Intent(this, SetupActivity.class));
	}
	
	private void startSettingsActivity(){
		startActivity(new Intent(this, SettingsActivity.class));
	}
	
	private void fireBeacon(){
		BeaconApp.setActive(true);
		Intent intent = new Intent(this, BeaconService.class);
		intent.setAction(BeaconService.ACTION_BEGIN);
		startService(intent);
	}
	
	private void refreshErrorContainer(){
		
		if(BeaconApp.isActive()){
			mErrorContainer.setVisibility(View.GONE);
			mErrorNetwork.setVisibility(View.GONE);
			mErrorGps.setVisibility(View.GONE);
			return;
		}
		
		if(BeaconApp.hasNetworkConnectivity() && BeaconApp.isGpsOnline()){
			mErrorContainer.setVisibility(View.GONE);
			mErrorNetwork.setVisibility(View.GONE);
			mErrorGps.setVisibility(View.GONE);
			return;
		}
		
		mErrorContainer.setVisibility(View.VISIBLE);
		
		if(BeaconApp.hasNetworkConnectivity() && !BeaconApp.isGpsOnline()){
			mErrorNetwork.setVisibility(View.GONE);
			mErrorGps.setVisibility(View.VISIBLE);
			return;
		}
		
		if(!BeaconApp.hasNetworkConnectivity() && BeaconApp.isGpsOnline()){
			mErrorNetwork.setVisibility(View.VISIBLE);
			mErrorGps.setVisibility(View.GONE);
			return;
		}
		
		if(!BeaconApp.hasNetworkConnectivity() && !BeaconApp.isGpsOnline()){
			mErrorNetwork.setVisibility(View.VISIBLE);
			mErrorGps.setVisibility(View.VISIBLE);
			return;
		}
		
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
		//TODO Change text so that it puts down the address as well
		mLocationView.setText(String.format(getString(R.string.location_view_text), 
				Double.toString(lat),
				Double.toString(lng)));
	}
	
	private void updateStatusView(String update){
		mStatusBuilder.append(ChronoUtils.getCurrentTime() + ": " + update + "\n");
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
			if(MainActivity.this == null){
				return;
			}
			mApp.setHasNetworkConnectivity(AndroidUtils.hasNetworkConnectivity(MainActivity.this));
			refreshErrorContainer();
		}
		
	};
	
	private OnGpsChangeListener mGpsChangeListener = new OnGpsChangeListener(){

		@Override
		public void onGpsChange() {
			if(MainActivity.this == null){
				return;
			}
			Log.d(TAG, "GPS Provider change detected.");
			mApp.setGpsOnline(AndroidUtils.isGpsOnline(MainActivity.this));
			refreshErrorContainer();
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
				Log.e(TAG, "Context in receiver was null");
				return;
			}
			
			if(intent == null){
				Log.e(TAG, "Intent was null");
				return;
			}
			
			if(intent.getAction().equals(BeaconService.BROADCAST_SERVICE_KILLED)){
				MainActivity.this.finish();
			}
			
		}
		
	}
	
	private class BeaconReceiver extends BroadcastReceiver{
		
		public BeaconReceiver(){}

		@Override
		public void onReceive(Context context, Intent intent) {
			
			if(context == null){
				Log.e(TAG, "Context in receiver was null");
				return;
			}
			
			if(intent == null){
				Log.e(TAG, "Intent was null");
				return;
			}
			
			if(intent.getAction().equals(BeaconService.BROADCAST_BEACON_MESSAGE)){
				MainActivity.this.updateStatusView(intent.getStringExtra(BeaconService.EXTRA_BROADCAST_MESSAGE));
			}
			
		}
		
	}
	
	private class CoordinateReceiver extends BroadcastReceiver{
		
		public CoordinateReceiver(){}

		@Override
		public void onReceive(Context context, Intent intent) {
			
			if(context == null){
				Log.e(TAG, "Context in receiver was null");
				return;
			}
			
			if(intent == null){
				Log.e(TAG, "Intent was null");
				return;
			}
			
			double lat = intent.getDoubleExtra(BeaconService.EXTRA_BROADCAST_LATITUDE, 0);
			double lng = intent.getDoubleExtra(BeaconService.EXTRA_BROADCAST_LONGITUDE, 0);
			
			MainActivity.this.setLocationOnMap(lat, lng);
			MainActivity.this.setLocationOnLocationView(lat, lng);
			
		}
		
	}

}
