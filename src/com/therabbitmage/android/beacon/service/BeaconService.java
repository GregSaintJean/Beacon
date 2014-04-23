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
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.SmsManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.therabbitmage.android.beacon.BeaconApp;
import com.therabbitmage.android.beacon.R;
import com.therabbitmage.android.beacon.entities.beacon.BeaconSMSContact;
import com.therabbitmage.android.beacon.network.TwitterBeacon;
import com.therabbitmage.android.beacon.provider.Beacon;
import com.therabbitmage.android.beacon.provider.BeaconMobileQuery;
import com.therabbitmage.android.beacon.receiver.GpsReceiver;
import com.therabbitmage.android.beacon.receiver.NetworkReceiver;
import com.therabbitmage.android.beacon.receiver.OnGpsChangeListener;
import com.therabbitmage.android.beacon.receiver.OnNetworkChangeListener;
import com.therabbitmage.android.beacon.utils.AndroidUtils;
import com.therabbitmage.android.beacon.utils.ChronoUtils;
import com.therabbitmage.android.beacon.utils.LocationHelper;
import com.therabbitmage.android.beacon.utils.LocationUtils;

public class BeaconService extends Service implements LocationListener,
	GooglePlayServicesClient.ConnectionCallbacks,
	GooglePlayServicesClient.OnConnectionFailedListener{
	
	private static final String TAG = BeaconService.class.getSimpleName();
	
	public static final String ACTION_BEGIN =  "com.therabbitmage.android.beacon.service.BeaconService.action_begin";
	public static final String ACTION_STOP = "com.therabbitmage.android.beacon.service.BeaconService.action_stop";
	public static final String ACTION_SEND_SMS_MESSAGE = "com.therabbitmage.android.beacon.service.BeaconService.action.SEND_SMS_MESSAGE";
	public static final String ACTION_SEND_TWITTER_MESSAGE = "com.therabbitmage.android.beacon.service.BeaconService.action.SEND_TWITTER_MESSAGE";
	public static final String ACTION_SEND_MESSAGE = "com.therabbitmage.android.beacon.service.BeaconService.action.SEND_MESSAGE";
	
	public static final String BROADCAST_BEACON_IS_RUNNING = "broadcast_beacon_is_running";
	public static final String BROADCAST_BEACON_MESSAGE = "broadcast_beacon_message";
	public static final String BROADCAST_FACEBOOK_MESSAGE = "broadcast_facebook_message";
	public static final String BROADCAST_COORDINATES = "broadcast_coodinates";
	public static final String BROADCAST_SERVICE_KILLED = "broadcast_service_killed";
	public static final String BROADCAST_BEACON_ERROR = "broadcast_beacon_error";
	public static final String EXTRA_BROADCAST_MESSAGE = "extra_broadcast_message";
	public static final String EXTRA_BROADCAST_FACEBOOK_MESSAGE = "extra_broadcast_facebook_message";
	public static final String EXTRA_BROADCAST_LATITUDE = "extra_broadcast_latitude";
	public static final String EXTRA_BROADCAST_LONGITUDE = "extra_broadcast_longitude";
	public static final String EXTRA_MESSAGE = "extra_message";
	public static final String EXTRA_RESULT_RECEIVER = "extra_result_receiver";
		
	private static final String ACTION_SMS_SENT = "com.therabbitmage.android.beacon.service.BeaconService.SMS_SENT";
	
	private static final int MESSAGE_STOP = 1;
	private static final int MESSAGE_START_BEACON = 2;
	private static final int MESSAGE_SMS_SEND = 3;
	private static final int MESSAGE_TWITTER_SEND = 4;
	private static final int MESSAGE_SEND_ALL = 5;
	
	private static final int NOTIFICATION_ID = 1;
	
	// Update frequency in milliseconds
	//public static final long UPDATE_INTERVAL = 10 * ChronoUtils.ONE_MINUTE;
	
	public static final long UPDATE_INTERVAL = 30 * ChronoUtils.ONE_SECOND;
	
	// A fast frequency ceiling in milliseconds
	//public static final long FASTEST_INTERVAL = 7 * ChronoUtils.ONE_MINUTE;
	public static final long FASTEST_INTERVAL = 15 * ChronoUtils.ONE_SECOND;
	
	private long mLastTime;
	
	private BroadcastReceiver mSmsReceiver;
	private NetworkReceiver mNetworkReceiver;
	private GpsReceiver mGpsReceiver;
	private LocalBroadcastManager mLocalBMgr;
	private Geocoder mGeocoder;
	private NotificationManager mNm;
	private NotificationCompat.Builder mBuilder;
	private BeaconApp mApp;
	private ActivityRecognitionClient mActivityRecognitionClient;
	
	private LocationHelper mLocationHelper;
	private Location mCurrentLocation;
	private Twitter mTwitter;
	private Address mCurrentAddress;
	
	private volatile Looper mServiceLooper;
	private volatile ServiceHandler mServiceHandler;
	
	private boolean isRequestingActivityUpdates;
	
	private long mNextTransmissionTime;
	private long mNextInactiveTransmissionTime;
	private long mNextActiveTransmissionTime;
	private long mInactiveTransmissionTime;
	private long mActiveTransmissionTime;
	private long mLastTransmissionTime;
	private List<BeaconSMSContact> mContactList;
	
	private enum DeviceState{
		STILL,
		MOVING,
		MOVING_VEHICLE
	}
	
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
					Log.d(TAG, "Message to stop beacon received");
					mApp.setBeaconStatus(false);
					break;
				case MESSAGE_START_BEACON:
					Log.d(TAG, "Message to start beacon received");
					startBeacon();
					break;
				case MESSAGE_SEND_ALL:
					break;
				case MESSAGE_SMS_SEND:
					break;
				case MESSAGE_TWITTER_SEND:
					break;
					
			}
		}
	}
	
	@Override
	public void onCreate(){
		
		mApp = BeaconApp.getInstance();
		mNm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		
		HandlerThread thread = new HandlerThread(BeaconService.class.getSimpleName(), 
				Process.THREAD_PRIORITY_BACKGROUND);
		thread.start();
		mServiceLooper = thread.getLooper();
		mServiceHandler = new ServiceHandler(mServiceLooper);
		
		mLocalBMgr = LocalBroadcastManager.getInstance(this);
		registerReceivers();
		
		mApp.setHasNetworkConnectivity(AndroidUtils.hasNetworkConnectivity(this));
		mApp.setGpsOnline(AndroidUtils.isGpsOnline(this));
		
		mTwitter = TwitterBeacon.getTwitter(mApp);
		
		mGeocoder = new Geocoder(this, Locale.getDefault());
		
		mLocationHelper = new LocationHelper(this, this, this, this);
		mLocationHelper.setUpdateInterval(UPDATE_INTERVAL);
		mLocationHelper.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		mLocationHelper.setFastestInterval(FASTEST_INTERVAL);
		mLocationHelper.reset();
		
		mActivityRecognitionClient = new ActivityRecognitionClient(this, this, this);
		mActivityRecognitionClient.connect();
		isRequestingActivityUpdates = false;
		
		mInactiveTransmissionTime = mApp.getInactiveTransmissionInterval();
		mActiveTransmissionTime = mApp.getActiveTransmissionInterval();
		mLastTransmissionTime = -1;
		mNextTransmissionTime = -1;
		//TODO When Activity recognition is done, this boolean will change.
		mApp.setActive(true);
		compileContactList();
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		Message msg = mServiceHandler.obtainMessage();
		msg.arg1 = startId;
		msg.arg2 = flags;
		if(intent != null){
			msg.obj = intent.getExtras();
		}
		
		if(intent.getAction() != null){
			if(intent.getAction().equals(ACTION_STOP)){
				msg.what = MESSAGE_STOP;
			} else if (intent.getAction().equals(ACTION_SEND_MESSAGE)){
				msg.what = MESSAGE_SEND_ALL;
			} else if (intent.getAction().equals(ACTION_SEND_SMS_MESSAGE)){
				msg.what = MESSAGE_SMS_SEND;
			} else if (intent.getAction().equals(ACTION_SEND_TWITTER_MESSAGE)){
				msg.what = MESSAGE_TWITTER_SEND;
			} else if(intent.getAction().equals(ACTION_BEGIN)){
				msg.what = MESSAGE_START_BEACON;
			}
		} 
		
		mServiceHandler.sendMessage(msg);
		
		return START_STICKY;
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.i(TAG, "Beacon Service onDestroyed called");
		TwitterBeacon.clearTwitter();
		unregisterReceivers();
		stopRequestActivityUpdates();
		mLocationHelper.shutdownLocationHelper();
		mActivityRecognitionClient.disconnect();
		mServiceLooper.quit();
		mNm.cancelAll();
		sendNotification(getString(R.string.notification_beacon_offline), null, null, null, null, false);
		Intent broadcastIntent = new Intent(BROADCAST_SERVICE_KILLED);
		mLocalBMgr.sendBroadcast(broadcastIntent);
		mApp.setActive(false);
		mApp.setBeaconStatus(false);
	}
	
	private void sendNotification(String title, String message, Class<?> activity, String action, Bundle extras, boolean ongoing){
		mBuilder = new NotificationCompat.Builder(this)
		.setSmallIcon(R.drawable.ic_launcher)
		.setContentTitle(title)
		.setOngoing(ongoing);
		
		if(message != null){
			mBuilder.setContentText(message);
		}
		
		if(activity != null){
			
			Intent resultIntent = new Intent(this, activity);
			
			if(action != null){
				resultIntent.setAction(action);
			}
			
			if(extras != null){
				resultIntent.putExtras(extras);
			}
			
			TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
			stackBuilder.addParentStack(activity);
			stackBuilder.addNextIntent(resultIntent);
			PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
			mBuilder.setContentIntent(resultPendingIntent);
		}
		
		mNm.notify(NOTIFICATION_ID, mBuilder.build());
	}
	
	private void compileContactList(){
		
		ContentResolver resolver = getContentResolver();
		
		Cursor c = resolver.query(BeaconMobileQuery.CONTENT_URI,
						BeaconMobileQuery.PROJECTION,
						null,
						null,
						BeaconMobileQuery.SORT_ORDER);
		
		mContactList = new LinkedList<BeaconSMSContact>();
		
		if(c.moveToFirst()){
			
			do{
				
				BeaconSMSContact contact = new BeaconSMSContact();
				contact.setContactId(c.getInt(c.getColumnIndex(Beacon.BeaconMobileContactDetails.CN_CONTACT_ID)));
				contact.setDisplayName(c.getString(c.getColumnIndex(Beacon.BeaconMobileContactDetails.CN_DISPLAY_NAME)));
				contact.setNumber(c.getString(c.getColumnIndex(Beacon.BeaconMobileContactDetails.CN_NUMBER)));
				mContactList.add(contact);
				
			} while(c.moveToNext());
				
		}
		
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
	
	private void startBeacon(){
		mApp.setBeaconStatus(true);
		mServiceHandler.post(mBeacon);
	}
	
	private void transmit(){
		Log.d(TAG, "Transmitting");
		
		if(AndroidUtils.checkPhoneAndSmsCapability(this)){
			mServiceHandler.post(mSendSms);
		}
		
		if(AndroidUtils.hasNetworkConnectivity(this) && mApp.hasTwitterLogin()){
			mServiceHandler.post(mUpdateTwitter);
		}
		
	}
	
	private Runnable mBeacon = new Runnable(){

		@Override
		public void run() {
			if(BeaconApp.isBeaconOnline()){
				
				Calendar now = Calendar.getInstance();
				
				if(now.getTimeInMillis() >= mNextTransmissionTime){
					//transmit();
					calculateNextTransmissionTimes(now);
				}
				
				mServiceHandler.post(this);
				
			} else {
				Log.d(TAG, "Terminating Beacon");
				stopSelf();
			}
			
		}
		
	};
	
	private Runnable mSendSms = new Runnable(){

		@Override
		public void run() {
			String message = "Hello World\n";
			
			if(BeaconApp.isGpsOnline()){
				message += "Latitude = " + mCurrentLocation.getLatitude() + "\n";
				message += "Longitude = " + mCurrentLocation.getLongitude() + "\n";
				
				if(mCurrentAddress != null){
					
					for(int i = 0; i < mCurrentAddress.getMaxAddressLineIndex(); i++){
						message += mCurrentAddress.getAddressLine(i) + "\n";
					}
					
				}
			}
			
			message += "Hello World! This is a developer testing his app. If you are receving this message, " +
					"message the number that has sent this to you back and tell him to stop annoying you " +
					"with his shit.\n";
			message += "End message!\n";
			
			ListIterator<BeaconSMSContact> iter = mContactList.listIterator();
			while(iter.hasNext()){
				BeaconSMSContact contact = iter.next();
				AndroidUtils.sendSms(BeaconService.this, contact.getNumber(), message,
						PendingIntent.getBroadcast(BeaconService.this, 0, new Intent(ACTION_SMS_SENT), 0),
						null);
			}
			
			sendNotification(getString(R.string.sms_message_sent), "Message sent: " + message, null, null, null, true);
			
		}
		
	};
	
	private void sendSms(String message, String number){
		AndroidUtils.sendSms(BeaconService.this, number, message, 
				PendingIntent.getBroadcast(BeaconService.this, 0, new Intent(ACTION_SMS_SENT), 0), null);
		
		sendNotification(getString(R.string.sms_message_sent), "Message sent: " + message, null, null, null, true);
	}
	
	private void sendTwitterTweet(String message){
		
		AccessToken accessToken = new AccessToken(mApp.getTwitterAccessToken(), mApp.getTwitterAccessTokenSecret());
		mTwitter.setOAuthAccessToken(accessToken);
		
		Intent broadcast = null;
		
		try{
			mTwitter.updateStatus(message);
		} catch (TwitterException e){
			Log.e(TAG, e.toString());
			broadcast = new Intent(BROADCAST_BEACON_ERROR);
			broadcast.putExtra(EXTRA_BROADCAST_MESSAGE, e.toString());
			mLocalBMgr.sendBroadcast(broadcast);
			return;
		}
		
		broadcast = new Intent(BROADCAST_BEACON_MESSAGE);
		broadcast.putExtra(EXTRA_BROADCAST_MESSAGE, getString(R.string.tweet_sent));
		mLocalBMgr.sendBroadcast(broadcast);
		sendNotification(getString(R.string.notification_twitter_status_updated), null, null, null, null, true);
		
	}
	
	private Runnable mUpdateTwitter = new Runnable(){

		@Override
		public void run() {
			if(BeaconApp.hasNetworkConnectivity()){
				
				String message = "Testing 1 2 3 4 5";
				
				if(BeaconApp.isGpsOnline()){
					message += "Latitude = " + mCurrentLocation.getLatitude() + "\n";
					message += "Longitude = " + mCurrentLocation.getLongitude() + "\n";
					
					if(mCurrentAddress != null){
						
						for(int i = 0; i < mCurrentAddress.getMaxAddressLineIndex(); i++){
							message += mCurrentAddress.getAddressLine(i) + "\n";
						}
						
					}
				}
				
				AccessToken accessToken = new AccessToken(mApp.getTwitterAccessToken(), mApp.getTwitterAccessTokenSecret());
				mTwitter.setOAuthAccessToken(accessToken);
				
				try {
					mTwitter.updateStatus(message);
				} catch (TwitterException e) {
					Log.e(TAG, e.toString());
				}
				
				Intent broadcast = new Intent(BROADCAST_BEACON_MESSAGE);
				broadcast.putExtra(EXTRA_BROADCAST_MESSAGE, getString(R.string.tweet_sent));
				mLocalBMgr.sendBroadcast(broadcast);
				//TODO Enhance so that the user can send direct messages
				
				sendNotification(getString(R.string.notification_twitter_status_updated), null, null, null, null, true);
				
			} else {
				//TODO set a time to transmit again.
			}
		}
		
	};
	
	private void getAddress(){
		try{
			List<Address> addressList = mGeocoder.getFromLocation(mCurrentLocation.getLatitude(), 
					mCurrentLocation.getLongitude(), 1);
			
			if(addressList != null && addressList.size() > 0){
				//TODO This might need to enchanced so that every time an address is retrieved, it's stored on the hard drive for history taking.
				mCurrentAddress = addressList.get(0);
			} else {
				
				mCurrentAddress = null;
			}
			
		} catch (IOException e){
			Log.e(TAG, e.toString());
		}
	}
	
	private void calculateAndChangeDistance(Location startLocation, Location endLocation){
		float[] results =  new float[1];
		
		Location.distanceBetween(startLocation.getLatitude(), startLocation.getLongitude(),
				endLocation.getLatitude(), endLocation.getLongitude(), results);
		
		if(results[0] > 50){
			mCurrentLocation = endLocation;
			//getAddress();
			broadcastCoordinates();
		}
	}
	
	private boolean isFirstTransmission(){
		return mLastTransmissionTime == -1 && mNextTransmissionTime == -1;
	}
	
	private void calculateNextTransmissionTimes(Calendar now){
		mLastTransmissionTime = now.getTimeInMillis();
		mNextActiveTransmissionTime = mLastTransmissionTime + mActiveTransmissionTime;
		mNextInactiveTransmissionTime = mLastTransmissionTime + mInactiveTransmissionTime;
		if(BeaconApp.isActive()){
			mNextTransmissionTime = mNextActiveTransmissionTime;
		} else {
			mNextTransmissionTime = mNextInactiveTransmissionTime;
		}
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
		
		mLocationHelper.onConnected(bundle);
		
		if(mActivityRecognitionClient != null 
				&& mActivityRecognitionClient.isConnected() 
				&& !isRequestingActivityUpdates){
			requestActivityUpdates();
		}
	}
	
	@Override
	public void onDisconnected() {
		Log.e(TAG, "Location services disconnected");
	}
	
	@Override
	public void onLocationChanged(Location location) {
		Log.d(TAG, "Location changed");
		Calendar now = Calendar.getInstance();
		
		if(mCurrentLocation == null){
			mLastTime = now.getTimeInMillis();
			mCurrentLocation = location;
			mServiceHandler.post(new Runnable(){

				@Override
				public void run() {
					getAddress();
				}
				
			});
			broadcastCoordinates();
			if(!BeaconApp.isBeaconOnline()){
				Message msg = mServiceHandler.obtainMessage();
				msg.what = MESSAGE_START_BEACON;
				mServiceHandler.sendMessage(msg);
			}
			return;
		}
		
		if(LocationUtils.isBetterLocation(mCurrentLocation, location)){
			mCurrentLocation = location;
			mServiceHandler.post(new Runnable(){

				@Override
				public void run() {
					getAddress();
				}
				
			});
			broadcastCoordinates();
			return;
		}
		
		final Location l = location;
		mServiceHandler.post(new Runnable(){

			@Override
			public void run() {
				calculateAndChangeDistance(mCurrentLocation, l);
			}
			
		});
		long timeElapsed = (now.getTimeInMillis() - mLastTime)/1000;
		mLastTime = now.getTimeInMillis();
		Log.d(TAG, "Time in seconds passed = " + timeElapsed);
		
	}
	
	private void requestActivityUpdates(){
		//TODO Actually start requesting activity updates
		isRequestingActivityUpdates = true;
	}
	
	private void stopRequestActivityUpdates(){
		//TODO Actually stop requesting activity updates
		isRequestingActivityUpdates = false;
	}
	
	private void broadcastCoordinates(){
		Intent broadcastIntent = new Intent(BROADCAST_COORDINATES);
		broadcastIntent.putExtra(EXTRA_BROADCAST_LATITUDE, mCurrentLocation.getLatitude());
		broadcastIntent.putExtra(EXTRA_BROADCAST_LONGITUDE, mCurrentLocation.getLongitude());
		mLocalBMgr.sendBroadcast(broadcastIntent);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	private OnNetworkChangeListener mNetworkChangeListener = new OnNetworkChangeListener(){

		@Override
		public void onNetworkChange() {
			Log.d(TAG, "Network Change detected");
			mApp.setHasNetworkConnectivity(AndroidUtils.hasNetworkConnectivity(BeaconService.this));
		}
		
	};
	
	private OnGpsChangeListener mGpsChangeListener = new OnGpsChangeListener(){

		@Override
		public void onGpsChange() {
			Log.d(TAG, "Gps Change detected");
			mApp.setGpsOnline(AndroidUtils.isGpsOnline(BeaconService.this));
		}
		
	};
	
	private class SmsReceiver extends BroadcastReceiver{
		
		public SmsReceiver(){}

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
