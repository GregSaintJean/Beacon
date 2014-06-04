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
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
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
import android.os.ResultReceiver;
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
import com.therabbitmage.android.beacon.R;
import com.therabbitmage.android.beacon.SignalApp;
import com.therabbitmage.android.beacon.entities.beacon.BeaconSMSContact;
import com.therabbitmage.android.beacon.entities.google.urlshortener.Url;
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

public class SignalService extends BaseService implements LocationListener,
	GooglePlayServicesClient.ConnectionCallbacks,
	GooglePlayServicesClient.OnConnectionFailedListener{
	
	private static final String TAG = SignalService.class.getSimpleName();
	
	private static final String GOOGLE_MAPS_URL = "http://www.google.com/maps/@";
	private static final String GOOGLE_MAPS_DEFAULT_ZOOM = ",14z";
	
	private static final int NOTIFICATION_ID = 0;
	
	private static final int REQUEST_CODE_SMS = 0;
	private static final int REQUEST_CODE_TRANSMISSION = 1;
	private static final int REQUEST_CODE_NOTIFICATION = 2;
	private static final int REQUEST_CODE_DELAYED_TRAMISSION = 3;

	// Update frequency in milliseconds
	// public static final long UPDATE_INTERVAL = 10 * ChronoUtils.ONE_MINUTE;

	public static final long THIRTY_SECONDS = 30 * ChronoUtils.ONE_SECOND;
	public static final long UPDATE_INTERVAL = THIRTY_SECONDS;

	// A fast frequency ceiling in milliseconds
	// public static final long FASTEST_INTERVAL = 7 * ChronoUtils.ONE_MINUTE;
	public static final long FASTEST_INTERVAL = 15 * ChronoUtils.ONE_SECOND;
	
	public static final String ACTION_START = "com.therabbitmage.android.beacon.service.NewBeaconService.action.START";
	public static final String ACTION_STOP = "com.therabbitmage.android.beacon.service.NewBeaconService.action.STOP";
	public static final String ACTION_TRANSMIT = "com.therabbitmage.android.beacon.service.NewBeaconService.action.TRANSMIT";
	public static final String ACTION_SEND = "com.therabbitmage.android.beacon.service.NewBeaconService.action.SEND";
	
	public static final String ACTION_SMS_SENT = "com.therabbitmage.android.beacon.service.NewBeaconService.SMS_SENT";
	
	public static final String EXTRA_MESSAGE = "extra_message";
	public static final String EXTRA_MESSAGE_TYPE = "extra_message_type";
	
	public static final String EXTRA_TRANSMIT_TYPE = "extra_transmit_type";
	public static final String EXTRA_TRANSMIT_FIRST_TWITTER = "extra_transmit_first_twitter";
	
	public static final String BROADCAST_BEACON_MESSAGE = "broadcast_beacon_message";
	public static final String BROADCAST_COORDINATES = "broadcast_coodinates";
	public static final String BROADCAST_SERVICE_KILLED = "broadcast_service_killed";
	public static final String BROADCAST_BEACON_ERROR = "broadcast_beacon_error";
	
	public static final String EXTRA_BROADCAST_MESSAGE = "extra_broadcast_message";
	public static final String EXTRA_BROADCAST_MESSAGE_TYPE = "extra_broadcast_message_type";
	public static final String EXTRA_BROADCAST_LATITUDE = "extra_broadcast_latitude";
	public static final String EXTRA_BROADCAST_LONGITUDE = "extra_broadcast_longitude";
	
	public static final int MESSAGE_TYPE_ALL = 0;
	public static final int MESSAGE_TYPE_SMS = 1;
	public static final int MESSAGE_TYPE_TWITTER = 2;
	
	private static final int MESSAGE_START = 0;
	private static final int MESSAGE_STOP = 1;
	private static final int MESSAGE_TRANSMIT = 2;
	private static final int MESSAGE_SEND = 3;
	private static PendingIntent sRepeatingPendingIntent;
	public static PendingIntent sDelayedTwitterIntent;
	
	static
	{
		sRepeatingPendingIntent = PendingIntent.getBroadcast(SignalApp.getInstance(), REQUEST_CODE_TRANSMISSION, new Intent(ACTION_TRANSMIT), PendingIntent.FLAG_UPDATE_CURRENT);
		Intent twitterIntent = new Intent();
		twitterIntent.putExtra(EXTRA_TRANSMIT_TYPE, EXTRA_TRANSMIT_FIRST_TWITTER);
		sDelayedTwitterIntent = PendingIntent.getBroadcast(SignalApp.getInstance(), REQUEST_CODE_DELAYED_TRAMISSION, twitterIntent, PendingIntent.FLAG_UPDATE_CURRENT);
	}
	
	private static SignalService sInstance;
	
	private NotificationManager mNotificationMgr;
	private AlarmManager mAlarmMgr;
	private NotificationCompat.Builder mBuilder;
	private LocalBroadcastManager mLocalBMgr;
	private BroadcastReceiver mSMSReceiver;
	private NetworkReceiver mNetworkReceiver;
	private GpsReceiver mGpsReceiver;
	private LocationHelper mLocationHelper;
	private Geocoder mGeocoder;
	private ActivityRecognitionClient mActivityRecognitionClient;
	private Location mStartingLocation, mCurrentLocation;
	private Address mCurrentAddress;
	private Twitter mTwitter;
	private long mLastLocationUpdateTime, mLastLocationChangeTime;
	
	private volatile Looper mServiceLooper;
	private volatile ServiceHandler mServiceHandler;
	
	private List<BeaconSMSContact> mContactList;
	private boolean mHasSentFirstTransmission, mConnectedToGooglePlay, mSentFirstSMSData, mSentTwitterData, mWaitingForLocation;
	private String mMapUrl;
	private Url mShortenedUrl;
	
	private enum DeviceState{
		STILL, MOVING, MOVING_VEHICLE
	}
	
	public static SignalService getInstance(){
		return sInstance;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		sInstance = this;
		mNotificationMgr = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		mAlarmMgr = (AlarmManager)getSystemService(ALARM_SERVICE);
		
		HandlerThread thread = new HandlerThread(
				SignalService.class.getSimpleName(), 
				Process.THREAD_PRIORITY_BACKGROUND);
		thread.start();
		mServiceLooper = thread.getLooper();
		mServiceHandler = new ServiceHandler(mServiceLooper);
		
		mLocalBMgr = LocalBroadcastManager.getInstance(this);
		registerReceivers();
		
		//Paranoia check
		gpsAndNetworkCheck();
		mTwitter = TwitterBeacon.getTwitter(SignalApp.getInstance());
		initLocationServices();
		
		compileContactList();
		mLastLocationUpdateTime = mLastLocationChangeTime = -1;
		mHasSentFirstTransmission = mSentFirstSMSData = mSentTwitterData = mConnectedToGooglePlay = mWaitingForLocation = false;
		mStartingLocation = mCurrentLocation = null;
	}
	
	private void gpsAndNetworkCheck(){
		SignalApp.setHasNetworkConnectivity(AndroidUtils.hasNetworkConnectivity(this));
		SignalApp.setGpsOnline(AndroidUtils.isGpsOnline(this));
	}
	
	private void initLocationServices(){
		Log.d(TAG, "Initializing Google Location Services");
		mGeocoder = new Geocoder(this, Locale.getDefault());
		mLocationHelper = new LocationHelper(this, this, this, this);
		mActivityRecognitionClient = new ActivityRecognitionClient(this, this,
				this);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		if(intent == null){
			return START_STICKY;
		}
		
		Message msg = mServiceHandler.obtainMessage();
		msg.arg1 = startId;
		msg.arg2 = flags;
		if(intent != null){
			msg.obj = intent.getExtras();
		}
		
		if(intent.getAction() != null){
			
			if(intent.getAction().equals(ACTION_START)){
				msg.what = MESSAGE_START;
			} else if(intent.getAction().equals(ACTION_STOP)){
				msg.what = MESSAGE_STOP;
			} else if(intent.getAction().equals(ACTION_TRANSMIT)){
				msg.what = MESSAGE_TRANSMIT;
			} else if(intent.getAction().equals(ACTION_SEND)){
				msg.what = MESSAGE_SEND;
			}
			
			mServiceHandler.sendMessage(msg);
		}
		
		return START_STICKY;
		
	}
	
	private final class ServiceHandler extends Handler{
		public ServiceHandler(Looper looper){
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			Bundle arguments = (Bundle)msg.obj;
			
			switch(msg.what){
				case MESSAGE_START:
					startBeacon();
					break;
				case MESSAGE_STOP:
					shutdownBeacon();
					break;
				case MESSAGE_TRANSMIT:
					Log.d(TAG, "Coming from onStartCommand");
					transmitBeacon();
					break;
				case MESSAGE_SEND:
					prepareUserMessage(arguments);
					break;
			
			}
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	private void attemptToConnectToGooglePlayServices(){
		Log.d(TAG, "Attempting to connect to Google Play Services");
		mLocationHelper.setUpdateInterval(UPDATE_INTERVAL);
		mLocationHelper.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		mLocationHelper.setFastestInterval(FASTEST_INTERVAL);
		mLocationHelper.reset();
		
		//mActivityRecognitionClient.connect();
	}
	
	private void startBeacon(){
		SignalApp.setisBeaconOnline(true);
		if(SignalApp.hasNetworkConnectivity()){
			attemptToConnectToGooglePlayServices();
		}
	}
	
	private void compileContactList() {

		ContentResolver resolver = getContentResolver();

		Cursor c = resolver.query(BeaconMobileQuery.CONTENT_URI,
				BeaconMobileQuery.PROJECTION, null, null,
				BeaconMobileQuery.SORT_ORDER);

		mContactList = new LinkedList<BeaconSMSContact>();

		if (c.moveToFirst()) {

			do {

				BeaconSMSContact contact = new BeaconSMSContact();
				contact.setContactId(c.getInt(c
						.getColumnIndex(Beacon.BeaconMobileContactDetails.CN_CONTACT_ID)));
				contact.setDisplayName(c.getString(c
						.getColumnIndex(Beacon.BeaconMobileContactDetails.CN_DISPLAY_NAME)));
				contact.setNumber(c.getString(c
						.getColumnIndex(Beacon.BeaconMobileContactDetails.CN_NUMBER)));
				mContactList.add(contact);

			} while (c.moveToNext());

		}
		
		if(!c.isClosed()){
			c.close();
		}

	}
	
	private void registerReceivers() {
		mSMSReceiver = new SMSReceiver();
		registerReceiver(mSMSReceiver, new IntentFilter(ACTION_SMS_SENT));

		mNetworkReceiver = new NetworkReceiver();
		mNetworkReceiver.setOnNetworkChangeListener(mNetworkChangeListener);
		registerReceiver(mNetworkReceiver, new IntentFilter(
				ConnectivityManager.CONNECTIVITY_ACTION));

		mGpsReceiver = new GpsReceiver();
		mGpsReceiver.setOnGpsChangeListener(mGpsChangeListener);
		registerReceiver(mGpsReceiver, new IntentFilter(
				LocationManager.PROVIDERS_CHANGED_ACTION));
	}

	private void unregisterReceivers() {
		unregisterReceiver(mSMSReceiver);
		unregisterReceiver(mNetworkReceiver);
		unregisterReceiver(mGpsReceiver);
	}
	
	@Override
	public void onConnected(Bundle bundle) {
		final Bundle b =  bundle;
		if(IS_DEBUG){
			Log.d(TAG, getString(R.string.logcat_google_play_services_connected));
		}
		
		mServiceHandler.post(new Runnable(){

			@Override
			public void run() {
				runOnConnected(b);
			}
			
		});
		
		mConnectedToGooglePlay = true;
		
	}
	
	private void runOnConnected(Bundle bundle){
		mLocationHelper.onConnected(bundle);
		
		if(mActivityRecognitionClient != null
				&& mActivityRecognitionClient.isConnected()
				/*&& !isRequestingActivityUpdates*/){
			//TODO Activity Recognition?
		}
		if(!mHasSentFirstTransmission){
			prepareFirstMessage();
		}
	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		Log.d(TAG, getString(R.string.logcat_connection_failed_called));

		// TODO handle these
		switch (result.getErrorCode()) {
		case ConnectionResult.DATE_INVALID:
			Log.e(TAG, getString(R.string.logcat_date_invalid));
			break;
		case ConnectionResult.DEVELOPER_ERROR:
			Log.e(TAG, getString(R.string.logcat_developer_error));
			break;
		case ConnectionResult.INTERNAL_ERROR:
			break;
		case ConnectionResult.INVALID_ACCOUNT:
			break;
		case ConnectionResult.LICENSE_CHECK_FAILED:
			break;
		case ConnectionResult.NETWORK_ERROR:
			Log.e(TAG, getString(R.string.logcat_network_lost));
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
		
		if(!mHasSentFirstTransmission){
			mSentFirstSMSData = mSentTwitterData = false;
			prepareFirstMessage();
		}
	}

	@Override
	public void onDisconnected() {
		mServiceHandler.post(new Runnable(){

			@Override
			public void run() {
				runOnDisconnect();
			}
			
		});
	}
	
	private void runOnDisconnect(){
		if(IS_DEBUG){
			Log.d(TAG, getString(R.string.logcat_google_play_services_disconnected));
		}
		
		mConnectedToGooglePlay = false;
		//TODO Handle Activity Recognition
	}

	@Override
	public void onLocationChanged(Location location) {
		if(IS_DEBUG){
			Log.d(TAG, "onLocationChanged called");
		}
		
		final Location l = location;
		
		mServiceHandler.post(new Runnable(){

			@Override
			public void run() {
				runOnLocationChanged(l);
			}
			
		});
		
	}
	
	private void runOnLocationChanged(Location location){
		
		if(LocationUtils.isBetterLocation(location, mCurrentLocation)){
			mCurrentLocation = location;
			
			if(mStartingLocation == null){
				mStartingLocation = mCurrentLocation;
			}
			
			broadcastCoordinates();
			
			if(IS_DEBUG){
				Log.d(TAG, getString(R.string.logcat_location_changed));
				if(mLastLocationUpdateTime == -1){
					mLastLocationUpdateTime = Calendar.getInstance().getTimeInMillis();
					Log.d(TAG, getString(R.string.logcat_first_location_update));
				} else {
					long timeElapsed = (Calendar.getInstance().getTimeInMillis() - mLastLocationChangeTime)/1000;
					Log.d(TAG, String.format(getString(R.string.logcat_time_in_secs_passed_update), timeElapsed));
				}
			}
			
			if(mWaitingForLocation){
				mWaitingForLocation = false;
				prepareFirstMessage();
			}
		}
		
	}
	
	private void sendDelayedTwitterMessage(){
		
		
		ResultReceiver receiver = new ResultReceiver(mServiceHandler){

			@Override
			protected void onReceiveResult(int resultCode, Bundle resultData) {
				super.onReceiveResult(resultCode, resultData);
				
				if(resultCode == UrlShortenService.RESULT_CODE_SUCCESS){
					mShortenedUrl = resultData.getParcelable(UrlShortenService.EXTRA_URL);
					
					String smsMessage = IS_DEBUG ?
							String.format(
									getString(R.string.transmit_test_sms_beacon_online_location_map),
									Double.toString(mCurrentLocation.getLatitude()),
									Double.toString(mCurrentLocation.getLongitude()),
									mShortenedUrl.getId())
							:
							String.format(
									getString(R.string.transmit_sms_beacon_online_location_map),
									Double.toString(mCurrentLocation.getLatitude()),
									Double.toString(mCurrentLocation.getLongitude()),
									mShortenedUrl.getId());
					
					String twitterMessage = IS_DEBUG ?
							String.format(getString(R.string.transmit_test_twitter_beacon_online_location_map),
									Double.toString(mCurrentLocation.getLatitude()),
									Double.toString(mCurrentLocation.getLongitude()),
									mShortenedUrl.getId())
							:
							String.format(getString(R.string.transmit_twitter_beacon_online_location_map),
							Double.toString(mCurrentLocation.getLatitude()),
							Double.toString(mCurrentLocation.getLongitude()),
							mShortenedUrl.getId());
					
					sendSMSMessage(smsMessage);
					sendTwitterMessage(twitterMessage);
					
				} else {
					String smsMessage = IS_DEBUG ?
							String.format(
									getString(R.string.transmit_test_sms_beacon_online_location),
									Double.toString(mCurrentLocation.getLatitude()),
									Double.toString(mCurrentLocation.getLongitude()))
							:
							String.format(
							getString(R.string.transmit_sms_beacon_online_location),
							Double.toString(mCurrentLocation.getLatitude()),
							Double.toString(mCurrentLocation.getLongitude()));
					
					String twitterMessage = IS_DEBUG ?
							String.format(getString(R.string.transmit_test_twitter_beacon_online_location),
									Double.toString(mCurrentLocation.getLatitude()),
									Double.toString(mCurrentLocation.getLongitude()))
							:
							String.format(getString(R.string.transmit_twitter_beacon_online_location),
							Double.toString(mCurrentLocation.getLatitude()),
							Double.toString(mCurrentLocation.getLongitude()));
					
					sendSMSMessage(smsMessage);
					sendTwitterMessage(twitterMessage);
				}
			}
			
		};
		
		Intent intent = new Intent(SignalService.this,
				UrlShortenService.class);
		intent.setAction(UrlShortenService.ACTION_SHORTEN);
		intent.putExtra(UrlShortenService.EXTRA_URL, mMapUrl);
		intent.putExtra(UrlShortenService.EXTRA_RECEIVER, receiver);
		startService(intent);
	}
	
	private void prepareFirstMessage(){
		
		if(mHasSentFirstTransmission){
			return;
		}
		
		if(!SignalApp.hasSmsCapability()){
			
			if(!SignalApp.hasTwitterLogin()){
				//TODO Handle this better but right now, there is no point. Maybe bring up a dialog in activity.
				shutdownBeacon();
				return;
			}
			
			if(SignalApp.hasNetworkConnectivity()){
				sendDelayedTwitterMessage();
			} else {
				mAlarmMgr.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + THIRTY_SECONDS, sDelayedTwitterIntent);
			}
			
			return;
		}
		
		if(SignalApp.hasGpsCapability() && SignalApp.isGpsOnline()){
			
			if(mCurrentLocation == null){
				mWaitingForLocation = true;
				return;
			}
			
			if(SignalApp.hasNetworkConnectivity()){
				
				mMapUrl = GOOGLE_MAPS_URL + mCurrentLocation.getLatitude() + ","
						+ mCurrentLocation.getLongitude()
						+ GOOGLE_MAPS_DEFAULT_ZOOM;
				
				ResultReceiver receiver = new ResultReceiver(mServiceHandler){

					@Override
					protected void onReceiveResult(int resultCode, Bundle resultData) {
						super.onReceiveResult(resultCode, resultData);
						
						if(resultCode == UrlShortenService.RESULT_CODE_SUCCESS){
							mShortenedUrl = resultData.getParcelable(UrlShortenService.EXTRA_URL);
							String smsMessage = IS_DEBUG ?
									String.format(
											getString(R.string.transmit_test_sms_beacon_online_location_map),
											Double.toString(mCurrentLocation.getLatitude()),
											Double.toString(mCurrentLocation.getLongitude()),
											mShortenedUrl.getId())
									:
									String.format(
											getString(R.string.transmit_sms_beacon_online_location_map),
											Double.toString(mCurrentLocation.getLatitude()),
											Double.toString(mCurrentLocation.getLongitude()),
											mShortenedUrl.getId());
							
							String twitterMessage = IS_DEBUG ?
									String.format(getString(R.string.transmit_test_twitter_beacon_online_location_map),
											Double.toString(mCurrentLocation.getLatitude()),
											Double.toString(mCurrentLocation.getLongitude()),
											mShortenedUrl.getId())
									:
									String.format(getString(R.string.transmit_twitter_beacon_online_location_map),
									Double.toString(mCurrentLocation.getLatitude()),
									Double.toString(mCurrentLocation.getLongitude()),
									mShortenedUrl.getId());
							
							sendSMSMessage(smsMessage);
							sendTwitterMessage(twitterMessage);
							
						} else {
							
							String smsMessage = IS_DEBUG ?
									String.format(
											getString(R.string.transmit_test_sms_beacon_online_location),
											Double.toString(mCurrentLocation.getLatitude()),
											Double.toString(mCurrentLocation.getLongitude()))
									:
									String.format(
									getString(R.string.transmit_sms_beacon_online_location),
									Double.toString(mCurrentLocation.getLatitude()),
									Double.toString(mCurrentLocation.getLongitude()));
							
							String twitterMessage = IS_DEBUG ?
									String.format(getString(R.string.transmit_test_twitter_beacon_online_location),
											Double.toString(mCurrentLocation.getLatitude()),
											Double.toString(mCurrentLocation.getLongitude()))
									:
									String.format(getString(R.string.transmit_twitter_beacon_online_location),
									Double.toString(mCurrentLocation.getLatitude()),
									Double.toString(mCurrentLocation.getLongitude()));
							
							sendSMSMessage(smsMessage);
							sendTwitterMessage(twitterMessage);
							
						}
						
						
						
					}
					
				};
				
				Intent intent = new Intent(SignalService.this,
						UrlShortenService.class);
				intent.setAction(UrlShortenService.ACTION_SHORTEN);
				intent.putExtra(UrlShortenService.EXTRA_URL, mMapUrl);
				intent.putExtra(UrlShortenService.EXTRA_RECEIVER, receiver);
				startService(intent);
				
			} else {
				
				String smsMessage = IS_DEBUG ?
						String.format(
								getString(R.string.transmit_test_sms_beacon_online_location),
								Double.toString(mCurrentLocation.getLatitude()),
								Double.toString(mCurrentLocation.getLongitude()))
						:
						String.format(
						getString(R.string.transmit_sms_beacon_online_location),
						Double.toString(mCurrentLocation.getLatitude()),
						Double.toString(mCurrentLocation.getLongitude()));
				
				String twitterMessage = IS_DEBUG ?
						String.format(getString(R.string.transmit_test_twitter_beacon_online_location),
								Double.toString(mCurrentLocation.getLatitude()),
								Double.toString(mCurrentLocation.getLongitude()))
						:
						String.format(getString(R.string.transmit_twitter_beacon_online_location),
						Double.toString(mCurrentLocation.getLatitude()),
						Double.toString(mCurrentLocation.getLongitude()));
				
				sendSMSMessage(smsMessage);
				sendTwitterMessage(twitterMessage);
				
			}
			
		} else {
			
			if(SignalApp.hasGpsCapability()){
				//TODO set something up so as soon as the GPS comes online, it sends the coordinates.
			}
			
			sendSMSMessage(IS_DEBUG ? getString(R.string.transmit_test_sms_beacon_online) : getString(R.string.transmit_sms_beacon_online));
			
			if(SignalApp.hasNetworkConnectivity() && SignalApp.hasTwitterLogin()){
				
				sendTwitterMessage(IS_DEBUG ? getString(R.string.transmit_test_twitter_beacon_online) : getString(R.string.transmit_twitter_beacon_online));
				
			}
		}
		
		mHasSentFirstTransmission = true;
		scheduleNextRegularTransmission();
		
	}
	
	private void prepareUserMessage(Bundle extras){
		if(extras.containsKey(EXTRA_BROADCAST_MESSAGE_TYPE) && extras.containsKey(EXTRA_BROADCAST_MESSAGE)){
			sendUserMessage(extras.getInt(EXTRA_BROADCAST_MESSAGE_TYPE), extras.getString(EXTRA_BROADCAST_MESSAGE));
		} else {
			
			if(!extras.containsKey(EXTRA_BROADCAST_MESSAGE_TYPE)){
				Log.e(TAG, getString(R.string.logcat_action_message_type));
			}
			
			if(!extras.containsKey(EXTRA_BROADCAST_MESSAGE)){
				Log.e(TAG, getString(R.string.logcat_action_message));
			}
			
		}
	}
	
	private void sendUserMessage(int messageType, String message){
		
		String formattedMessage;
		
		if(messageType == MESSAGE_TYPE_SMS 
				|| messageType == MESSAGE_TYPE_ALL){
			formattedMessage = String.format(getString(R.string.transmit_sms_beacon_message),
					message);
			sendSMSMessage(formattedMessage);
		}
		
		if(messageType == MESSAGE_TYPE_TWITTER 
				|| messageType == MESSAGE_TYPE_ALL){
			formattedMessage = String.format(getString(R.string.transmit_twitter_beacon_message),
					message);
			sendTwitterMessage(formattedMessage);
		}
		
	}
	
	private void sendTwitterMessage(String message) {
		if(!SignalApp.hasTwitterLogin()){
			return;
		}
		
		//TODO Have it handle direct messenging

		AccessToken accessToken = new AccessToken(SignalApp.getTwitterAccessToken(),
				SignalApp.getTwitterAccessTokenSecret());
		mTwitter.setOAuthAccessToken(accessToken);

		Intent broadcast = null;

		try {
			mTwitter.updateStatus(message);
		} catch (TwitterException e) {
			Log.e(TAG, e.toString());
			broadcast = new Intent(BROADCAST_BEACON_ERROR);
			broadcast.putExtra(EXTRA_BROADCAST_MESSAGE, e.toString());
			mLocalBMgr.sendBroadcast(broadcast);
			return;
		}
		
		String broadcastMessage = getString(R.string.tweet_sent) + ": " + message;
		broadcastUpdate(broadcastMessage);
		
		sendNotification(
				getString(R.string.notification_twitter_status_updated), null,
				null, null, null, false);

	}
	
	private void sendSMSMessage(String message){
		if(!SignalApp.hasSmsCapability() && IS_DEBUG){
			Log.d(TAG, getString(R.string.logcat_device_no_sms));
			return;
		}
		
		ListIterator<BeaconSMSContact> iter = mContactList.listIterator();
		while(iter.hasNext()){
			BeaconSMSContact contact= iter.next();
			sendSMS(message, contact.getNumber());
		}
	}
	
	private void sendSMS(String message, String number){
		AndroidUtils.sendSms(this, number, message, 
				PendingIntent.getBroadcast(this, REQUEST_CODE_SMS, new Intent(ACTION_SMS_SENT), 0), null);
	}
	
	private String getCurrentAddress(){
		if (mCurrentAddress == null
				|| mCurrentAddress.getMaxAddressLineIndex() <= 0) {
			return null;
		}
		String address = new String();
		for (int i = 0; i < mCurrentAddress.getMaxAddressLineIndex(); i++) {
			address += mCurrentAddress.getAddressLine(i) + "\n";
		}
		return address;
	}
	
	private void retrieveCurrentAddress() {
		try {
			List<Address> addressList = mGeocoder.getFromLocation(
					mCurrentLocation.getLatitude(),
					mCurrentLocation.getLongitude(), 1);

			if (addressList != null && addressList.size() > 0) {
				// TODO This might need to enhanced so that every time an
				// address is retrieved, it's stored on the hard drive for
				// history taking.
				mCurrentAddress = addressList.get(0);
			} else {

				mCurrentAddress = null;
			}

		} catch (IOException e) {
			Log.e(TAG, e.toString());
		}
	}
	
	private void calculateAndChangeDistance(Location startLocation,
			Location endLocation) {
		float[] results = new float[1];

		Location.distanceBetween(startLocation.getLatitude(),
				startLocation.getLongitude(), endLocation.getLatitude(),
				endLocation.getLongitude(), results);

		if (results[0] > 50) {
			mCurrentLocation = endLocation;
			retrieveCurrentAddress();
			broadcastCoordinates();
		}
	}
	
	private void scheduleNextRegularTransmission(){
		if(IS_DEBUG){
			Log.d(TAG, "scheduleNextRegularTransmission is called");
		}
		
		long inactiveInterval = SignalApp.getInactiveTransmissionInterval();
		long nextTransmission = System.currentTimeMillis()  + inactiveInterval;
		
		mAlarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, nextTransmission, inactiveInterval, sRepeatingPendingIntent);
	}
	
	public void transmitBeacon(){
		if(IS_DEBUG){
			Log.d(TAG, getString(R.string.logcat_tranmission_intent_received));
		}
		
	}
	
	private void shutdownBeacon(){
		if(IS_DEBUG){
			Log.d(TAG, getString(R.string.logcat_terminate_beacon));
		}
		
		stopSelf();
	}
	
	private void sendNotification(String title, String message,
			Class<?> activity, String action, Bundle extras, boolean ongoing){
		mBuilder = new NotificationCompat.Builder(this);
		mBuilder.setSmallIcon(R.drawable.ic_launcher).setContentTitle(title);
		mBuilder.setOngoing(ongoing);
		
		if (message != null) {
			mBuilder.setContentText(message);
		}

		if (activity != null) {

			Intent resultIntent = new Intent(this, activity);

			if (action != null) {
				resultIntent.setAction(action);
			}

			if (extras != null) {
				resultIntent.putExtras(extras);
			}

			TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
			stackBuilder.addParentStack(activity);
			stackBuilder.addNextIntent(resultIntent);
			PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(
					REQUEST_CODE_NOTIFICATION, PendingIntent.FLAG_UPDATE_CURRENT);
			mBuilder.setContentIntent(resultPendingIntent);
		}

		mNotificationMgr.notify(NOTIFICATION_ID, mBuilder.build());
	}
	
	private void broadcastCoordinates() {
		Intent broadcastIntent = new Intent(BROADCAST_COORDINATES);
		broadcastIntent.putExtra(EXTRA_BROADCAST_LATITUDE,
				mCurrentLocation.getLatitude());
		broadcastIntent.putExtra(EXTRA_BROADCAST_LONGITUDE,
				mCurrentLocation.getLongitude());
		mLocalBMgr.sendBroadcast(broadcastIntent);
	}
	
	private void broadcastUpdate(String message){
		Intent broadcastIntent = new Intent(BROADCAST_COORDINATES);
		broadcastIntent.putExtra(EXTRA_BROADCAST_MESSAGE, message);
		mLocalBMgr.sendBroadcast(broadcastIntent);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if(IS_DEBUG){
			Log.d(TAG, "Beacon Service onDestroyed called");
		}
		SignalApp.setisBeaconOnline(false);
		
		mAlarmMgr.cancel(sRepeatingPendingIntent);
		
		unregisterReceivers();
		mLocationHelper.shutdownLocationHelper();
		mServiceLooper.quit();
		mNotificationMgr.cancelAll();
		//TODO Remove this. It may not be necessary (or at least set a preference for it.
		sendNotification(getString(R.string.notification_beacon_offline), null,
				null, null, null, false);
		Intent broadcastIntent = new Intent(BROADCAST_SERVICE_KILLED);
		mLocalBMgr.sendBroadcast(broadcastIntent);
		sInstance = null;
	}
	
	private OnNetworkChangeListener mNetworkChangeListener = new OnNetworkChangeListener() {

		@Override
		public void onNetworkChange() {
			if(IS_DEBUG){
				Log.d(TAG, SignalService.this.getString(R.string.logcat_network_change_detected));
			}
			
			SignalApp.setHasNetworkConnectivity(AndroidUtils
					.hasNetworkConnectivity(SignalService.this));
			
			if(SignalApp.isBeaconOnline() && !mConnectedToGooglePlay && SignalApp.hasNetworkConnectivity()){
				attemptToConnectToGooglePlayServices();
			}
			
		}

	};

	private OnGpsChangeListener mGpsChangeListener = new OnGpsChangeListener() {

		@Override
		public void onGpsChange() {
			if(IS_DEBUG){
				Log.d(TAG, SignalService.this.getString(R.string.logcat_gps_change_detected));
			}
			SignalApp.setGpsOnline(AndroidUtils.isGpsOnline(SignalService.this));
		}

	};
	
	private class SMSReceiver extends BroadcastReceiver {

		public SMSReceiver() {
		}

		@Override
		public void onReceive(Context context, Intent intent) {
			if (context == null) {
				Log.e(TAG, getString(R.string.logcat_context_receiver_null));
				return;
			}

			if (intent == null) {
				Log.e(TAG, getString(R.string.logcat_intent_null));
				return;
			}
			
			if(IS_DEBUG){
				Log.d(TAG, "Received SMS intent");
			}

			switch (getResultCode()) {
				case Activity.RESULT_OK:
					// TODO update UI to show message sent
					Log.v(TAG, "Message sent!");
					Intent broadcast = new Intent(BROADCAST_BEACON_MESSAGE);
					broadcast
							.putExtra(EXTRA_BROADCAST_MESSAGE, "SMS message sent!");
					mLocalBMgr.sendBroadcast(broadcast);
					break;
				// TODO I might want to handle all these the same, with a retry algo
				case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
					Log.e(TAG, getString(R.string.logcat_generic_sms_failure));
					break;
				case SmsManager.RESULT_ERROR_NO_SERVICE:
					Log.e(TAG, getString(R.string.logcat_sms_no_service));
					break;
				case SmsManager.RESULT_ERROR_NULL_PDU:
					Log.e(TAG, getString(R.string.logcat_generic_null_pdu));
					break;
				case SmsManager.RESULT_ERROR_RADIO_OFF:
					Log.e(TAG, getString(R.string.logcat_sms_radio_off));
					break;
			}
		}

	}

}
