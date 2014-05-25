package com.therabbitmage.android.beacon.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.therabbitmage.android.beacon.BeaconApp;
import com.therabbitmage.android.beacon.R;
import com.therabbitmage.android.beacon.receiver.GpsReceiver;
import com.therabbitmage.android.beacon.receiver.NetworkReceiver;
import com.therabbitmage.android.beacon.receiver.OnGpsChangeListener;
import com.therabbitmage.android.beacon.receiver.OnNetworkChangeListener;
import com.therabbitmage.android.beacon.service.BeaconService;
import com.therabbitmage.android.beacon.ui.fragment.MainFragment;
import com.therabbitmage.android.beacon.utils.AndroidUtils;

public class NewMainActivity extends NavDrawerActivity{
	
	private static final String TAG = NewMainActivity.class.getSimpleName();
	
	private BroadcastReceiver mNetworkReceiver, mGpsReceiver, mBeaconKillReceiver,
	mBeaconReceiver, mCoordinateReceiver;
	
	private MainFragment mMainFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.new_main_activity);
		registerReceivers();
		setDrawerLayout((DrawerLayout) findViewById(R.id.drawer_layout));
		setDrawerList((ListView)findViewById(R.id.left_drawer));
		
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);
		
		ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
				this,
				getDrawerLayout(),
				R.drawable.ic_drawer,
				R.string.drawer_open, R.string.drawer_close){

					@Override
					public void onDrawerClosed(View drawerView) {
						invalidateOptionsMenu();
					}

					@Override
					public void onDrawerOpened(View drawerView) {
						invalidateOptionsMenu();
					}
			
		};;
		
		setDrawerToggle(toggle);
		
		getDrawerLayout().setDrawerListener(getDrawerToggle());
		
		ArrayAdapter<CharSequence> drawerAdapter;
		
		if(BeaconApp.isSetupDone()){
			drawerAdapter = ArrayAdapter.createFromResource(this, R.array.main_nav_array_1, R.layout.drawer_list_item);
		} else {
			drawerAdapter = ArrayAdapter.createFromResource(this, R.array.main_nav_array_2, R.layout.drawer_list_item);
		}
		
		getDrawerList().setAdapter(drawerAdapter);
		getDrawerList().setOnItemClickListener(new DrawerItemClickListener());
		
		mMainFragment = (MainFragment)getSupportFragmentManager().findFragmentById(R.id.content_frame);
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		switchDrawerItems(BeaconApp.isSetupDone());
	}
	
	private class DrawerItemClickListener implements ListView.OnItemClickListener{

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			
			Log.d(TAG, "Detecting selection");
			Log.d(TAG, "Position = " + position);
			
			if(parent == null || view == null){
				Log.d(TAG, "Returning from null checks");
				return;
			}
			
			if(position < 0 || id < 0){
				Log.d(TAG, "Returning from number checks");
				return;
			}
			
			if(BeaconApp.isSetupDone()){
				
				switch(position){
					case 0:
						startSetupActivity();
						break;
					case 1:
						startSettingsActivity();
						break;
				}
				
			} else {
				
				if(position == 0)
					startSettingsActivity();
				
			}
			
		}
		
	}
	
	//TODO Implement
	public void switchDrawerItems(boolean s){
		
		if(s){
			
		} else{
			
		}
		
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceivers();
	}

	private void startSetupActivity(){
		try{
			Intent intent = new Intent(this, NewSetupActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
			startActivity(intent);
		} finally{
			finish();
		}
		
	}
	
	private void startSettingsActivity(){
		try{
			Intent intent = new Intent(this, NewSettingsActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
			startActivity(intent);
		} finally{
			finish();
		}
		
	}
	
	private void registerReceivers(){
		mBeaconReceiver = new BeaconReceiver();
		mCoordinateReceiver = new CoordinateReceiver();
		mBeaconKillReceiver = new BeaconKillReceiver();
		
		IntentFilter filter = new IntentFilter(BeaconService.BROADCAST_BEACON_MESSAGE);
		getLocalBroadcastManager().registerReceiver(mBeaconReceiver, filter);
		
		filter = new IntentFilter(BeaconService.BROADCAST_COORDINATES);
		getLocalBroadcastManager().registerReceiver(mCoordinateReceiver, filter);
		
		filter = new IntentFilter(BeaconService.BROADCAST_SERVICE_KILLED);
		getLocalBroadcastManager().registerReceiver(mBeaconKillReceiver, filter);
		
		mNetworkReceiver = new NetworkReceiver();
		((NetworkReceiver)mNetworkReceiver).setOnNetworkChangeListener(mNetworkChangeListener);
		registerReceiver(mNetworkReceiver, 
				new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
		mGpsReceiver = new GpsReceiver();
		((GpsReceiver)mGpsReceiver).setOnGpsChangeListener(mGpsChangeListener);
		registerReceiver(mGpsReceiver, 
				new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION));
	}
	
	private void unregisterReceivers(){
		getLocalBroadcastManager().unregisterReceiver(mBeaconReceiver);
		getLocalBroadcastManager().unregisterReceiver(mCoordinateReceiver);
		getLocalBroadcastManager().unregisterReceiver(mBeaconKillReceiver);
		unregisterReceiver(mNetworkReceiver);
		unregisterReceiver(mGpsReceiver);
	}
	
	private OnNetworkChangeListener mNetworkChangeListener = new OnNetworkChangeListener(){

		@Override
		public void onNetworkChange() {
			Log.d(TAG, "Network connectivity changed detected");
			if(NewMainActivity.this == null){
				return;
			}
			BeaconApp.setHasNetworkConnectivity(AndroidUtils.hasNetworkConnectivity(NewMainActivity.this));
			mMainFragment.refreshErrorContainer();
		}
		
	};
	
	private OnGpsChangeListener mGpsChangeListener = new OnGpsChangeListener(){

		@Override
		public void onGpsChange() {
			if(NewMainActivity.this == null){
				return;
			}
			Log.d(TAG, "GPS Provider change detected.");
			BeaconApp.setGpsOnline(AndroidUtils.isGpsOnline(NewMainActivity.this));
			mMainFragment.refreshErrorContainer();
		}
		
	};
	
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
				NewMainActivity.this.finish();
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
				mMainFragment.updateStatusView(intent.getStringExtra(BeaconService.EXTRA_BROADCAST_MESSAGE));
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
			
			if(lat == 0 || lng == 0){
				return;
			}
			
			mMainFragment.setLat(lat);
			mMainFragment.setLong(lng);
			
			mMainFragment.setLocationOnMap(lat, lng);
			mMainFragment.setLocationOnLocationView(lat, lng);
			
		}
		
	}

}
