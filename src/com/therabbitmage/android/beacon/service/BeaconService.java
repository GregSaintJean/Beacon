package com.therabbitmage.android.beacon.service;

import java.io.IOException;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;
import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.SmsManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.therabbitmage.android.beacon.BeaconApp;
import com.therabbitmage.android.beacon.entities.Contact;
import com.therabbitmage.android.beacon.network.TwitterBeacon;
import com.therabbitmage.android.beacon.provider.Beacon;
import com.therabbitmage.android.beacon.provider.BeaconMobileQuery;
import com.therabbitmage.android.beacon.receiver.GpsReceiver;
import com.therabbitmage.android.beacon.receiver.NetworkReceiver;
import com.therabbitmage.android.beacon.receiver.OnGpsChangeListener;
import com.therabbitmage.android.beacon.receiver.OnNetworkChangeListener;
import com.therabbitmage.android.beacon.utils.AndroidUtils;
import com.therabbitmage.android.beacon.utils.LocationUtils;

public class BeaconService extends Service implements LocationListener,
	GooglePlayServicesClient.ConnectionCallbacks,
	GooglePlayServicesClient.OnConnectionFailedListener{
	
	private static final String TAG = BeaconService.class.getSimpleName();
	
	public static final String ACTION_BEGIN =  "com.therabbitmage.android.beacon.service.BeaconService.action_begin";
	public static final String ACTION_STOP = "com.therabbitmage.android.beacon.service.BeaconService.action_stop";
	public static final String ACTION_SEND_MESSAGE = "com.therabbitmage.android.beacon.service.BeaconService.actionSEND_MESSAGE";
	
	public static final String BROADCAST_BEACON_IS_RUNNING = "broadcast_beacon_is_running";
	public static final String BROADCAST_BEACON_MESSAGE = "broadcast_beacon_message";
	public static final String BROADCAST_FACEBOOK_MESSAGE = "broadcast_facebook_message";
	public static final String BROADCAST_COORDINATES = "broadcast_coodinates";
	public static final String BROADCAST_SERVICE_KILLED = "broadcast_service_killed";
	public static final String EXTRA_BROADCAST_MESSAGE = "extra_broadcast_message";
	public static final String EXTRA_BROADCAST_FACEBOOK_MESSAGE = "extra_broadcast_facebook_message";
	public static final String EXTRA_BROADCAST_LATITUDE = "extra_broadcast_latitude";
	public static final String EXTRA_BROADCAST_LONGITUDE = "extra_broadcast_longitude";
		
	private static final String ACTION_SMS_SENT = "com.therabbitmage.android.beacon.service.BeaconService.SMS_SENT";
	
	private static final int MESSAGE_STOP = 1;
	private static final int MESSAGE_TRANSMISSION = 2;
	
	private long mLastTime;
	
	private BroadcastReceiver mSmsReceiver;
	private NetworkReceiver mNetworkReceiver;
	private GpsReceiver mGpsReceiver;
	private LocalBroadcastManager mLocalBMgr;
	private SmsManager mSmsManager;
	private Geocoder mGeocoder;
	private NotificationManager mNm;
	private BeaconApp mApp;
	private ActivityRecognitionClient mActivityRecognitionClient;
	
	private LocationRequest mLocationRequest;
	private LocationClient mLocationClient;
	private Location mCurrentLocation;
	private Twitter mTwitter;
	private Address mCurrentAddress;
	
	private volatile Looper mServiceLooper;
	private volatile ServiceHandler mServiceHandler;
	
	private boolean mHasNetworkConnectivity;
	private boolean mIsGpsConnected;
	private boolean isBeaconRunning;
	private boolean isRequestingLocationUpdates;
	private boolean isRequestingActivityUpdates;
	
	private long mNextTransmissionTime;
	private long mNextInactiveTransmissionTime;
	private long mNextActiveTransmissionTime;
	private long mInactiveTransmissionTime;
	private long mActiveTransmissionTime;
	private long mLastTransmissionTime;
	private boolean isActive;
	private List<Contact> mContactList;
	
	private final class ServiceHandler extends Handler{
		public ServiceHandler(Looper looper){
			super(looper);
		}
		
		@Override
		public void handleMessage(Message msg) {
			
			Bundle arguments = (Bundle)msg.obj;
			//TODO Complete, this is where commands will be passed.
			switch(msg.what){
				case MESSAGE_STOP:
					stopBeaconTransmission();
					break;
				case MESSAGE_TRANSMISSION:
					startBeacon();
					break;
					
			}
		}
	}
	
	private void stopBeaconTransmission(){
		isBeaconRunning = false;
	}
	
	@Override
	public void onCreate(){
		
		mApp = (BeaconApp)getApplicationContext();
		mNm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		mSmsManager = SmsManager.getDefault();
		
		HandlerThread thread = new HandlerThread(BeaconService.class.getSimpleName(), 
				Process.THREAD_PRIORITY_BACKGROUND);
		thread.start();
		mServiceLooper = thread.getLooper();
		mServiceHandler = new ServiceHandler(mServiceLooper);
		
		mLocalBMgr = LocalBroadcastManager.getInstance(this);
		registerReceivers();
		
		mHasNetworkConnectivity = AndroidUtils.hasNetworkConnectivity(this);
		mIsGpsConnected = AndroidUtils.isGpsOnline(this);
		isBeaconRunning = false;
		
		mTwitter = TwitterBeacon.getTwitter(mApp);
		
		mGeocoder = new Geocoder(this, Locale.getDefault());
		
		mLocationRequest = LocationRequest.create();
		
		mLocationRequest.setInterval(LocationUtils.UPDATE_INTERVAL);
		
		mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		
		mLocationRequest.setFastestInterval(LocationUtils.FASTEST_INTERVAL);
		
		mLocationClient = new LocationClient(this, this, this);
		
		mLocationClient.connect();
		isRequestingLocationUpdates = false;
		
		mActivityRecognitionClient = new ActivityRecognitionClient(this, this, this);
		mActivityRecognitionClient.connect();
		isRequestingActivityUpdates = false;
		
		mHasNetworkConnectivity = AndroidUtils.hasNetworkConnectivity(this);
		
		mInactiveTransmissionTime = mApp.getSmsInactiveTransmissionInterval();
		mActiveTransmissionTime = mApp.getSmsActiveTransmissionInterval();
		mLastTransmissionTime = -1;
		mNextTransmissionTime = -1;
		//TODO When Activity recognition is done, this boolean will change.
		isActive = true;
		compileContactList();
	}
	
	private void compileContactList(){
		
		ContentResolver resolver = getContentResolver();
		
		Cursor c = resolver.query(BeaconMobileQuery.CONTENT_URI,
						BeaconMobileQuery.PROJECTION,
						null,
						null,
						BeaconMobileQuery.SORT_ORDER);
		
		mContactList = new LinkedList<Contact>();
		
		if(c.moveToFirst()){
			
			do{
				
				Contact contact = new Contact();
				contact.setName(c.getString(c.getColumnIndex(Beacon.BeaconMobileContactDetails.CN_DISPLAY_NAME)));
				contact.setNumber(c.getString(c.getColumnIndex(Beacon.BeaconMobileContactDetails.CN_NUMBER)));
				mContactList.add(contact);
				
			} while(c.moveToNext());
				
		}
		
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		Message msg = mServiceHandler.obtainMessage();
		msg.arg1 = startId;
		msg.arg2 = flags;
		msg.obj = intent.getExtras();
		
		if(intent.getAction() != null){
			if(intent.getAction().equals(ACTION_STOP)){
				msg.what = MESSAGE_STOP;
			}
		}
		
		mServiceHandler.sendMessage(msg);
		
		return START_STICKY;
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.i(TAG, "Beacon Service onDestroyed called");
		unregisterReceivers();
		stopRequestActivityUpdates();
		stopRequestingLocationUpdates();
		mLocationClient.disconnect();
		mActivityRecognitionClient.disconnect();
		mServiceLooper.quit();
		Intent broadcastIntent = new Intent(BROADCAST_SERVICE_KILLED);
		mLocalBMgr.sendBroadcast(broadcastIntent);
	}
	
	private void registerReceivers(){
		mSmsReceiver = new SmsReceiver();
		registerReceiver(mSmsReceiver, new IntentFilter(ACTION_SMS_SENT));
		
		mNetworkReceiver = new NetworkReceiver();
		mNetworkReceiver.setOnNetworkChangeListener(mNetworkChangeListener);
		registerReceiver(mNetworkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
		
		mGpsReceiver = new GpsReceiver();
		mGpsReceiver.setOnGpsChangeListener(mGpsChangeListener);
		registerReceiver(mGpsReceiver, 
				new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION));
	}
	
	private void unregisterReceivers(){
		unregisterReceiver(mSmsReceiver);
		unregisterReceiver(mNetworkReceiver);
		unregisterReceiver(mGpsReceiver);
	}
	
	private void transmitEndingTransmission(){
		
	}
	
	private void startBeacon(){
		
		isBeaconRunning = true;
		
		final Runnable runningBeacon = new Runnable(){

			@Override
			public void run() {
				
				if(isBeaconRunning){
					
					Calendar now = Calendar.getInstance();
					
					if(now.getTimeInMillis() >= mNextTransmissionTime){
						transmit();
						calculateNextTransmissionTimes(now);
					}
					
					mServiceHandler.post(this);
					
				} else {
					Log.d(TAG, "Terminating Beacon");
					transmitEndingTransmission();
					stopSelf();
				}
				
			}
			
		};
		
		mServiceHandler.post(runningBeacon);
		
	}
	
	private boolean isFirstTransmission(){
		return mLastTransmissionTime == -1 && mNextTransmissionTime == -1;
	}
	
	private void transmitSms(){
		//TODO Send sms
		if(mIsGpsConnected){
			
		} else {
			
		}
		ListIterator<Contact> iter = mContactList.listIterator();
		while(iter.hasNext()){
			Contact contact = iter.next();
			String number = contact.getNumber();
			String message = "Hello World\n";
			message += "Latitude = " + mCurrentLocation.getLatitude() + "\n";
			message += "Longitude = " + mCurrentLocation.getLongitude() + "\n";
			message += "End message!\n";
			List<String> messages = mSmsManager.divideMessage(message);
			for(int i = 0; i < messages.size(); i++){
				mSmsManager.sendTextMessage(number, null, messages.get(i),
						PendingIntent.getBroadcast(BeaconService.this, 0, new Intent(ACTION_SMS_SENT), 0), null);
				}
			}
	}
	
	private void transmitTwitter(){
		if(mHasNetworkConnectivity){
			//TODO Transmit twitter
			if(mIsGpsConnected){
				
			} else {
				
			}
			
		} else {
			
			if(mIsGpsConnected){
				
			} else {
				
			}
			
		}
	}
	
	private void transmit(){
		Log.d(TAG, "Transmitting");
		transmitSms();
		transmitTwitter();
	}
	
	private void calculateNextTransmissionTimes(Calendar now){
		mLastTransmissionTime = now.getTimeInMillis();
		mNextActiveTransmissionTime = mLastTransmissionTime + mActiveTransmissionTime;
		mNextInactiveTransmissionTime = mLastTransmissionTime + mInactiveTransmissionTime;
		if(isActive){
			mNextTransmissionTime = mNextActiveTransmissionTime;
		} else {
			mNextTransmissionTime = mNextInactiveTransmissionTime;
		}
	}
	
	private void getAddress(){
		try{
			List<Address> addressList = mGeocoder.getFromLocation(mCurrentLocation.getLatitude(), 
					mCurrentLocation.getLongitude(), 1);
			
			if(addressList != null && addressList.size() > 0){
				mCurrentAddress = addressList.get(0);
			} else {
				//TODO This might need to change so that it traces every single address you went to.
				mCurrentAddress = null;
			}
		}catch (IOException e){
			Log.e(TAG, e.toString());
		}
	}
	
	private void sendSMS(Cursor data){
		new SendSmsTask().execute(data);
	}
	
	private void sendTwitter(){
		new SendTwitterUpdateTask().execute();
	}
	
	@Override
	public void onConnectionFailed(ConnectionResult result) {
		
		Log.e(TAG, "Connection failed called");
		
		//TODO handle these
		switch(result.getErrorCode()){
			case ConnectionResult.DATE_INVALID:
				Log.e(TAG, "Date invalid");
				break;
			case ConnectionResult.DEVELOPER_ERROR:
				Log.e(TAG, "Developer error");
				break;
			case ConnectionResult.INTERNAL_ERROR:
				break;
			case ConnectionResult.INVALID_ACCOUNT:
				break;
			case ConnectionResult.LICENSE_CHECK_FAILED:
				break;
			case ConnectionResult.NETWORK_ERROR:
				Log.e(TAG, "Network connectivity lost!");
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
		Log.v(TAG, "Location services connected");
		
		if(mLocationClient != null 
				&& mLocationClient.isConnected() 
				&& !isRequestingLocationUpdates){
			requestLocationUpdates();
		}
		
		if(mActivityRecognitionClient != null 
				&& mActivityRecognitionClient.isConnected() 
				&& !isRequestingActivityUpdates){
			requestActivityUpdates();
		}
	}
	
	private void requestLocationUpdates(){
		mLocationClient.requestLocationUpdates(mLocationRequest, this);
		isRequestingLocationUpdates = true;
	}
	
	private void stopRequestingLocationUpdates(){
		mLocationClient.removeLocationUpdates(this);
		isRequestingLocationUpdates = false;
	}
	
	private void requestActivityUpdates(){
		//TODO Actually start requesting activity updates
		isRequestingActivityUpdates = true;
	}
	
	private void stopRequestActivityUpdates(){
		//TODO Actually stop requesting activity updates
		isRequestingActivityUpdates = false;
	}

	@Override
	public void onDisconnected() {
		Log.e(TAG, "Location services disconnected");
	}
	
	private void broadcastCoordinates(){
		Intent broadcastIntent = new Intent(BROADCAST_COORDINATES);
		broadcastIntent.putExtra(EXTRA_BROADCAST_LATITUDE, mCurrentLocation.getLatitude());
		broadcastIntent.putExtra(EXTRA_BROADCAST_LONGITUDE, mCurrentLocation.getLongitude());
		mLocalBMgr.sendBroadcast(broadcastIntent);
	}

	@Override
	public void onLocationChanged(Location location) {
		Log.d(TAG, "Location changed");
		Calendar now = Calendar.getInstance();
		
		
		if(mCurrentLocation == null){
			mLastTime = now.getTimeInMillis();
			mCurrentLocation = location;
			broadcastCoordinates();
			//getAddress();
			if(!isBeaconRunning){
				Message msg = mServiceHandler.obtainMessage();
				msg.what = MESSAGE_TRANSMISSION;
				mServiceHandler.sendMessage(msg);
			}
			return;
		}
		
		if(LocationUtils.isBetterLocation(mCurrentLocation, location)){
			mCurrentLocation = location;
			broadcastCoordinates();
			//getAddress();
			return;
		}
		
		new calculateAndChangeDistance().execute(mCurrentLocation, location);
		long timeElapsed = (now.getTimeInMillis() - mLastTime)/1000;
		mLastTime = now.getTimeInMillis();
		Log.d(TAG, "Time in seconds passed = " + timeElapsed);
		
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	private class SendSmsTask extends AsyncTask<Cursor, Void, Void>{

		@Override
		protected Void doInBackground(Cursor... args) {
			
			Cursor data = args[0];
			
			for(int i = 0; i < data.getCount(); i++){
				
				//String recipient = data.getString(data.getColumnIndex(Beacon.BeaconMobileContactDetails.CN_DISPLAY_NAME)); 
				String number = data.getString(data.getColumnIndex(Beacon.BeaconMobileContactDetails.CN_NUMBER));
				String message ="Hello World! This is a developer testing his app. If you are receving this message, " +
						"message the number that has sent this to you back and tell him to stop annoying you " +
						"with his shit.";
				List<String> messages = mSmsManager.divideMessage(message);
				
				for(int j = 0; j < messages.size(); j++){
					mSmsManager.sendTextMessage(number, 
							null, 
							messages.get(j), 
							PendingIntent.getBroadcast(BeaconService.this, 0, new Intent(ACTION_SMS_SENT), 0), null);
				}
			}
			
			data.close();
			
			return null;
		}
		
	}
	
	private class calculateAndChangeDistance extends AsyncTask<Location, Void, Void>{

		@Override
		protected Void doInBackground(Location... args) {
			
			float[] results =  new float[1];
			
			Location.distanceBetween(args[0].getLatitude(), args[0].getLongitude(), args[1].getLatitude(), args[1].getLongitude(), results);
			
			if(results[0] > 50){
				mCurrentLocation = args[1];
				//getAddress();
				broadcastCoordinates();
			}
			
			return null;
		}
		
	}
	
	private class SendTwitterUpdateTask extends AsyncTask<Void, Void, Void>{

		@Override
		protected Void doInBackground(Void... params) {
			
			AccessToken accessToken = new AccessToken(mApp.getTwitterAccessToken(), mApp.getTwitterAccessTokenSecret());
			mTwitter.setOAuthAccessToken(accessToken);
			
			try {
				mTwitter.updateStatus("This is a developer testing his app. plz ignore." +
						"If this is annoying, please tweet back kindly telling the developer to stfu.");
			} catch (TwitterException e) {
				Log.e(TAG, e.toString());
			}
			
			Intent broadcast = new Intent(BROADCAST_BEACON_MESSAGE);
			broadcast.putExtra(EXTRA_BROADCAST_MESSAGE, "Tweet sent!");
			mLocalBMgr.sendBroadcast(broadcast);
			
			return null;
		}
		
	}
	
	private OnNetworkChangeListener mNetworkChangeListener = new OnNetworkChangeListener(){

		@Override
		public void onNetworkChange() {
			Log.d(TAG, "Network Change detected");
			mHasNetworkConnectivity = AndroidUtils.hasNetworkConnectivity(BeaconService.this);
		}
		
	};
	
	private OnGpsChangeListener mGpsChangeListener = new OnGpsChangeListener(){

		@Override
		public void onGpsChange() {
			Log.d(TAG, "Gps Change detected");
			mIsGpsConnected = AndroidUtils.isGpsOnline(BeaconService.this);
		}
		
	};
	
	private class SmsReceiver extends BroadcastReceiver{
		
		public SmsReceiver(){}

		@Override
		public void onReceive(Context ctx, Intent intent) {
			
			Log.d(TAG, "Received SMS intent");
			
			switch(getResultCode()){
				case Activity.RESULT_OK:
					//TODO update UI to show message sent
					Log.v(TAG, "Message sent!");
					Intent broadcast = new Intent(BROADCAST_BEACON_MESSAGE);
					broadcast.putExtra(EXTRA_BROADCAST_MESSAGE, "SMS message sent!");
					mLocalBMgr.sendBroadcast(broadcast);
					break;
					//TODO I might want to handle all these the same, with a retry algo
				case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
					Log.e(TAG, "Generic SMS Failure");
					break;
				case SmsManager.RESULT_ERROR_NO_SERVICE:
					Log.e(TAG, "SMS No Service");
					break;
				case SmsManager.RESULT_ERROR_NULL_PDU:
					Log.e(TAG, "SMS Generic Null PDU");
					break;
				case SmsManager.RESULT_ERROR_RADIO_OFF:
					Log.e(TAG, "SMS Radio off");
					break;
			}
		}
		
	}
}
