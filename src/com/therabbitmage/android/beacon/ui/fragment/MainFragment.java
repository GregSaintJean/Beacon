package com.therabbitmage.android.beacon.ui.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
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
import com.therabbitmage.android.beacon.service.BeaconService;
import com.therabbitmage.android.beacon.ui.activity.SetupActivity;
import com.therabbitmage.android.beacon.utils.AndroidUtils;
import com.therabbitmage.android.beacon.utils.ChronoUtils;

public class MainFragment extends Fragment implements OnClickListener, OnEditorActionListener, OnItemSelectedListener{
	
	private static final String TAG = MainFragment.class.getSimpleName();
	
	private static final int ZOOM_LEVEL = 13;
	
	private static final int MESSAGE_SMS = 0;
	private static final int MESSAGE_TWITTER = 1;
	
	private static final String MESSAGE_TYPE = "message_type";
	private static final String LAT_KEY = "latitude";
	private static final String LONG_KEY = "longitude";
	private static final String STATUS_TYPE = "status";
	
	private GoogleMap mGoogleMap;
	private View mIntroContainer, mInfoContainer, mErrorContainer;
	private TextView mLocationView, mStatus, mErrorNetwork, mErrorGps;
	private EditText mInput;
	private Button mBtnMain, mBtnSend;
	private StringBuilder mStatusBuilder;
	private Spinner mMessageSpinner;
	private int mMessageType = MESSAGE_SMS;
	private double mLat = 0;
	private double mLong = 0;
	
	public MainFragment(){
		
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mStatusBuilder = new StringBuilder();
		setHasOptionsMenu(true);
	}
	
	@Override
	public void onStart() {
		super.onStart();
		if(!BeaconApp.isActive()){
			showIntro();
		} else {
			showInfo();
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View v = setupUI(inflater, container);
		
		if(savedInstanceState != null){
			if(savedInstanceState.containsKey(MESSAGE_TYPE)){
				mMessageType = savedInstanceState.getInt(MESSAGE_TYPE);
				mMessageSpinner.setSelection(mMessageType);
			}
			
			if(savedInstanceState.containsKey(LAT_KEY) && savedInstanceState.containsKey(LONG_KEY)){
				mLat =  savedInstanceState.getDouble(LAT_KEY);
				mLong =  savedInstanceState.getDouble(LONG_KEY);
				setLocationOnLocationView(mLat, mLong);
			}
			
			if(savedInstanceState.containsKey(STATUS_TYPE)){
				mStatusBuilder.append(savedInstanceState.getString(STATUS_TYPE));
				mStatus.setText(mStatusBuilder.toString());
			}
		}
		
		return v;
	}
	
	private View setupUI(LayoutInflater inflater, ViewGroup container){
		View v = inflater.inflate(R.layout.main_fragment, null, false);
		
		if (mGoogleMap == null) {
			mGoogleMap = ((SupportMapFragment) getActivity().getSupportFragmentManager()
					.findFragmentById(R.id.map)).getMap();
		}
		
		mInfoContainer = v.findViewById(R.id.info_container);
		mIntroContainer = v.findViewById(R.id.intro_container);
		mErrorContainer = v.findViewById(R.id.error_container);
		mLocationView = (TextView)v.findViewById(R.id.location_view);
		mStatus = (TextView)v.findViewById(R.id.status);
		mErrorNetwork = (TextView)v.findViewById(R.id.error_network_tv);
		mErrorGps = (TextView)v.findViewById(R.id.error_gps_tv);
		mBtnMain = (Button)v.findViewById(R.id.mainbtn);
		mInput = (EditText)v.findViewById(R.id.input_edittxt);
		mMessageSpinner = (Spinner)v.findViewById(R.id.message_spinner);
		ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter
				.createFromResource(getActivity(), R.array.message_array, R.layout.beacon_spinner_item);
		spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mMessageSpinner.setAdapter(spinnerAdapter);
		
		mMessageSpinner.setOnItemSelectedListener(this);
		mInput.setOnEditorActionListener(this);
		mBtnMain.setOnClickListener(this);
		mBtnSend.setOnClickListener(this);
		
		if(!AndroidUtils.isGoogleServicesAvailable(getActivity())){
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setTitle(R.string.google_play_required)
			.setMessage(R.string.google_play_must_be_installed)
			.setNeutralButton(android.R.string.ok, new android.content.DialogInterface.OnClickListener(){

				@Override
				public void onClick(DialogInterface dialog, int which) {
					try{
						dialog.dismiss();
					} finally{
						getActivity().finish();
					}
				}
				
			});
			builder.create().show();	
		}
		
		return v;
	}
	
	private void showInfo(){
		mIntroContainer.setVisibility(View.GONE);
		mInfoContainer.setVisibility(View.VISIBLE);
		mLocationView.setVisibility(View.VISIBLE);
		mErrorContainer.setVisibility(View.GONE);
		getActivity().invalidateOptionsMenu();
		refreshErrorContainer();
		mGoogleMap.getUiSettings().setAllGesturesEnabled(true);
		mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);
		mGoogleMap.getUiSettings().setZoomControlsEnabled(true);
		mGoogleMap.setMyLocationEnabled(true);
	}
	
	private void showIntro(){
		mIntroContainer.setVisibility(View.VISIBLE);
		mInfoContainer.setVisibility(View.GONE);
		mLocationView.setVisibility(View.GONE);
		mGoogleMap.getUiSettings().setAllGesturesEnabled(false);
		mGoogleMap.getUiSettings().setMyLocationButtonEnabled(false);
		mGoogleMap.getUiSettings().setZoomControlsEnabled(false);
		mGoogleMap.setMyLocationEnabled(false);
		
		if(BeaconApp.isSetupDone()){
			mBtnMain.setText(R.string.start);
		} else {
			mBtnMain.setText(R.string.setup);
		}
		
		getActivity().invalidateOptionsMenu();
		refreshErrorContainer();
	}
	
	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		boolean handled = false;
		if(actionId == EditorInfo.IME_ACTION_SEND){
			if(TextUtils.isEmpty(v.getText())){
				handled = false;
			} else {
				handled = true;
				
				CharSequence message = v.getText().toString();
				
				if (mMessageType == MESSAGE_SMS){
					sendSMSRequest(message);
				} else if(mMessageType == MESSAGE_TWITTER){
					sendTwitterRequest(message);
				}
				
				v.setText("");
				
				
			}
		}
		return handled;
	}
	
	private void sendSMSRequest(CharSequence message){
		Intent messageIntent = new Intent(getActivity(), BeaconService.class);
		messageIntent.setAction(BeaconService.ACTION_SEND_SMS_MESSAGE);
		messageIntent.putExtra(BeaconService.EXTRA_MESSAGE, message);
		getActivity().startService(messageIntent);
		Log.d(TAG, "Sending SMS Request");
	}
	
	private void sendTwitterRequest(CharSequence message){
		Intent messageIntent = new Intent(getActivity(), BeaconService.class);
		messageIntent.setAction(BeaconService.ACTION_SEND_TWITTER_MESSAGE);
		messageIntent.putExtra(BeaconService.EXTRA_MESSAGE, message);
		getActivity().startService(messageIntent);
		Log.d(TAG, "Sending Twitter Request");
	}
	
	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		
		switch(position){
		case 0:
			mMessageType = MESSAGE_SMS;
			break;
		case 1:
			mMessageType = MESSAGE_TWITTER;
			break;
		}
		
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putInt(MESSAGE_TYPE, mMessageType);
		outState.putDouble(LAT_KEY, mLat);
		outState.putDouble(LONG_KEY, mLong);
		outState.putString(STATUS_TYPE, mStatusBuilder.toString());
		super.onSaveInstanceState(outState);
	}
	
	@Override
	public void onClick(View v) {
		if(mBtnMain != null && v.getId() == mBtnMain.getId()){
			
			if(BeaconApp.isSetupDone()){
				fireBeacon();
				showInfo();
			} else {
				startSetupActivity();
			}
		}
		
		if(mBtnSend != null && v.getId() == mBtnSend.getId()){
			
			CharSequence message = mInput.getText().toString();
			if (mMessageType == MESSAGE_SMS){
				sendSMSRequest(message);
			} else if(mMessageType == MESSAGE_TWITTER){
				sendTwitterRequest(message);
			}
			
			mInput.setText("");
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.main, menu);
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		MenuItem shutdownItem = menu.findItem(R.id.shutdown);
		shutdownItem.setVisible(BeaconApp.isActive());
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		switch(item.getItemId()){
			case R.id.shutdown:
				shutdownBeacon();
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	private void shutdownBeacon(){
		try{
			Intent shutdownIntent = new Intent(getActivity(), BeaconService.class);
			shutdownIntent.setAction(BeaconService.ACTION_STOP);
			getActivity().startService(shutdownIntent);
		} finally{
			getActivity().finish();
		}
	}
	
	private void startSetupActivity(){
		startActivity(new Intent(getActivity(), SetupActivity.class));
	}
	
	private void fireBeacon(){
		BeaconApp.setActive(true);
		Intent intent = new Intent(getActivity(), BeaconService.class);
		intent.setAction(BeaconService.ACTION_BEGIN);
		getActivity().startService(intent);
	}
	
	public void refreshErrorContainer(){
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
	
	public void setLat(double lat){
		mLat = lat;
	}
	
	public void setLong(double lng){
		mLong = lng;
	}
	
	public void setLocationOnMap(double lat, double lng){
		
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
	
	public void setLocationOnLocationView(double lat, double lng){
		//TODO Change text so that it puts down the address as well
		mLocationView.setText(String.format(getString(R.string.location_view_text), 
				Double.toString(lat),
				Double.toString(lng)));
	}
	
	public void updateStatusView(String update){
		mStatusBuilder.append(ChronoUtils.getCurrentTime() + ": " + update + "\n");
		mStatus.setText(mStatusBuilder.toString());
	}



}
