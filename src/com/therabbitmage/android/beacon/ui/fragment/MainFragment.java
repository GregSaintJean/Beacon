package com.therabbitmage.android.beacon.ui.fragment;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.therabbitmage.android.beacon.BeaconApp;
import com.therabbitmage.android.beacon.R;
import com.therabbitmage.android.beacon.service.BeaconService;
import com.therabbitmage.android.beacon.ui.activity.SettingsActivity;
import com.therabbitmage.android.beacon.ui.activity.SetupActivity;
import com.therabbitmage.android.beacon.utils.AndroidUtils;
import com.therabbitmage.android.beacon.utils.ChronoUtils;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class MainFragment extends Fragment {
	
	private static final String TAG = MainFragment.class.getSimpleName();
	
	private static final int ZOOM_LEVEL = 13;
	
	private static final int MESSAGE_SMS = 0;
	private static final int MESSAGE_TWITTER = 1;
	
	private static final String MESSAGE_TYPE = "message_type";
	private static final String LAT_KEY = "latitude";
	private static final String LONG_KEY = "longitude";
	private static final String STATUS_TYPE = "status";
	
	private GoogleMap mGoogleMap;
	private BroadcastReceiver mNetworkReceiver, mGpsReceiver, mBeaconKillReceiver,
	mBeaconReceiver, mCoordinateReceiver;
	private LocalBroadcastManager mLocalBMgr;
	private View mIntroContainer, mInfoContainer, mErrorContainer;
	private TextView mLocationView, mStatus, mErrorNetwork, mErrorGps;
	private EditText mInput;
	private Button mMainBtn, mAltSetupBtn, mSettingsBtn;
	private StringBuilder mStatusBuilder;
	private Spinner mMessageSpinner;
	private int mMessageType = MESSAGE_SMS;
	private double mLat = 0;
	private double mLong = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(BeaconApp.hasGpsCapability()){
			BeaconApp.setGpsOnline(AndroidUtils.isGpsOnline(getActivity()));
		}
		
		BeaconApp.setHasNetworkConnectivity(AndroidUtils.hasNetworkConnectivity(getActivity()));
		mStatusBuilder = new StringBuilder();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View v = setupUI(inflater, container);
		
		return v;
	}
	
	private View setupUI(LayoutInflater inflater, ViewGroup container){
		return null;
	}

	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// TODO Auto-generated method stub
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public void onOptionsMenuClosed(Menu menu) {
		// TODO Auto-generated method stub
		super.onOptionsMenuClosed(menu);
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		super.onPrepareOptionsMenu(menu);
	}
	
	private void shutdownBeacon(){
		
	}
	
	private void startSetupActivity(){
		startActivity(new Intent(getActivity(), SetupActivity.class));
	}
	
	private void startSettingsActivity(){
		startActivity(new Intent(getActivity(), SettingsActivity.class));
	}
	
	private void fireBeacon(){
		BeaconApp.setActive(true);
		Intent intent = new Intent(getActivity(), BeaconService.class);
		intent.setAction(BeaconService.ACTION_BEGIN);
		getActivity().startService(intent);
	}
	
	private void refreshErrorContainer(){
		
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
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}
	
	private void registerReceivers(){
		
	}
	
	private void unregisterReceivers(){
		
	}

}
