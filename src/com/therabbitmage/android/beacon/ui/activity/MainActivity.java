package com.therabbitmage.android.beacon.ui.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;

import com.therabbitmage.android.beacon.BeaconApp;
import com.therabbitmage.android.beacon.R;
import com.therabbitmage.android.beacon.receiver.GpsReceiver;
import com.therabbitmage.android.beacon.receiver.NetworkReceiver;
import com.therabbitmage.android.beacon.receiver.OnGpsChangeListener;
import com.therabbitmage.android.beacon.receiver.OnNetworkChangeListener;
import com.therabbitmage.android.beacon.service.BeaconService;
import com.therabbitmage.android.beacon.utils.AndroidUtils;

public class MainActivity extends Activity implements OnClickListener {

	private static final String TAG = MainActivity.class.getSimpleName();
	
	private NetworkReceiver mNetworkReceiver;
	private GpsReceiver mGpsReceiver;
	private BroadcastReceiver mBeaconKillReceiver;
	
	private Button mMainButton;
	private Button mAltSetupButton;
	private Button mNetworkIndicator;
	private Button mGpsIndicator;
	private BeaconApp mApp;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		//AndroidUtils.logBundleContents(this, savedInstanceState, TAG);
		/*if(BuildConfig.DEBUG){
			AndroidUtils.enableStrictMode();
		}*/
		super.onCreate(savedInstanceState);
		
		mApp = BeaconApp.getInstance();
		if(mApp.isFirstRun()){
			Log.d(TAG, "first run executed");
			mApp.firstRunExecuted();
		}
		setupUI();
		registerReceivers();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceivers();
	}
	
	private void registerReceivers(){
		mNetworkReceiver = new NetworkReceiver();
		mNetworkReceiver.setOnNetworkChangeListener(mNetworkChangeListener);
		registerReceiver(mNetworkReceiver, 
				new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
		mGpsReceiver = new GpsReceiver();
		mGpsReceiver.setOnGpsChangeListener(mGpsChangeListener);
		registerReceiver(mGpsReceiver, 
				new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION));
		mBeaconKillReceiver = new BeaconKillReceiver();
		registerReceiver(mBeaconKillReceiver,
				new IntentFilter(BeaconService.BROADCAST_SERVICE_KILLED));
		
	}
	
	private void unregisterReceivers(){
		unregisterReceiver(mNetworkReceiver);
		unregisterReceiver(mGpsReceiver);
		unregisterReceiver(mBeaconKillReceiver);
	}

	private void setupUI() {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		mMainButton = (Button) findViewById(R.id.mainbtn);
		mAltSetupButton = (Button) findViewById(R.id.alt_setup_btn);
		mNetworkIndicator = (Button)findViewById(R.id.network_indicator);
		mGpsIndicator = (Button)findViewById(R.id.gps_indicator);
		
		if (mApp.isSetupDone()) {
			mMainButton.setText(R.string.fire);
			mAltSetupButton.setVisibility(View.VISIBLE);
		} else {
			mMainButton.setText(R.string.setup);
		}

		mMainButton.setOnClickListener(this);
		mAltSetupButton.setOnClickListener(this);
		mNetworkIndicator.setOnClickListener(this);
		mGpsIndicator.setOnClickListener(this);
		
		mNetworkIndicator.setEnabled(!AndroidUtils.hasNetworkConnectivity(this));
		mGpsIndicator.setEnabled(!AndroidUtils.isGpsOnline(this));
	}

	@Override
	public void onClick(View v) {

		if (v.getId() == R.id.mainbtn) {

			if (mApp.isSetupDone()) {
				
				try{
					startActivity(new Intent(this, EmergencyActivity.class));
				} finally{
					finish();
				}
				
			} else {
				startSetupActivity();
			}

		}

		if (v.getId() == R.id.alt_setup_btn) {
			startSetupActivity();
		}
		
		if(v.getId() == R.id.network_indicator){
			startNetworkSettingsActivity();
		}
		
		if(v.getId() == R.id.gps_indicator){
			startGpsSettingsActivity();
		}

	}

	private void startSetupActivity() {
		startActivity(new Intent(this, PhoneSetupActivity.class));
	}
	
	private void startNetworkSettingsActivity(){
		startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
	}
	
	private void startGpsSettingsActivity(){
		startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
	}
	
	private OnNetworkChangeListener mNetworkChangeListener = new OnNetworkChangeListener(){

		@Override
		public void onNetworkChange() {
			Log.d(TAG, "Network connectivity changed detected");
			if(mNetworkIndicator == null || MainActivity.this == null){
				return;
			}
			mNetworkIndicator.setEnabled(!AndroidUtils.hasNetworkConnectivity(MainActivity.this));
		}
		
	};
	
	private OnGpsChangeListener mGpsChangeListener = new OnGpsChangeListener(){

		@Override
		public void onGpsChange() {
			Log.d(TAG, "GPS Provider change detected.");
			mGpsIndicator.setEnabled(!AndroidUtils.isGpsOnline(MainActivity.this));
		}
		
	};
	
	private class BeaconKillReceiver extends BroadcastReceiver{
		
		public void onReceive(Context context, Intent intent){
			
			if(intent.getAction().equals(BeaconService.BROADCAST_SERVICE_KILLED)){
				MainActivity.this.finish();
			}
			
		}
		
	}

}
