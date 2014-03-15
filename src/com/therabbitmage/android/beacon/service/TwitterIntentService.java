package com.therabbitmage.android.beacon.service;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.therabbitmage.android.beacon.BeaconApp;
import com.therabbitmage.android.beacon.BuildConfig;
import com.therabbitmage.android.beacon.R;
import com.therabbitmage.android.beacon.network.TwitterBeacon;
import com.therabbitmage.android.beacon.ui.activity.TwitterPinActivity;

public class TwitterIntentService extends IntentService {
	private static final String TAG = TwitterIntentService.class
			.getSimpleName();

	public static final String ACTION_AUTH = "action_twitter_auth";
	public static final String ACTION_GET_ACCESS_TOKEN = "action_get_access_token";
	public static final String ACTION_LOGOUT = "action_logout";
	public static final String ACTION_UPDATE_STATUS = "action_update_status";
	
	public static final String EXTRA_MESSAGE = "extra_message";
	
	public static final String BROADCAST_LOGIN_SUCCESSFUL = "login_successful";
	public static final String BROADCAST_LOGOUT_SUCCESSFUL = "logout_successful";
	public static final String BROADCAST_TWITTER_LOG_MESSAGE = "broadcast_twitter_log_message";
	public static final String BROADCAST_TWITTER_SERVICE_ERROR = "broadcast_error";

	public static final String EXTRA_PIN = "extra_pin";
	
	private LocalBroadcastManager mLocalBMgr;
	
	private BeaconApp mBeaconApp;
	private Twitter mTwitter;

	public TwitterIntentService() {
		super(TwitterIntentService.class.getSimpleName());
		mBeaconApp = (BeaconApp)getApplicationContext();
		mTwitter = TwitterBeacon.getTwitter(mBeaconApp);
		mLocalBMgr = LocalBroadcastManager.getInstance(this);
	}

	@Override
	protected void onHandleIntent(Intent intent) {

		if (intent.getAction().equals(ACTION_AUTH)) {
			authenticate();
		}

		if (intent.getAction().equals(ACTION_GET_ACCESS_TOKEN)) {
			String pin = intent.getStringExtra(EXTRA_PIN);
			assert (pin != null);
			getAccessToken(pin);
		}
		
		if(intent.getAction().equals(ACTION_LOGOUT)){
			logout();
		}
		
		if(intent.getAction().equals(ACTION_UPDATE_STATUS)){
			
			Bundle args = intent.getExtras();
			
			if(!args.containsKey(EXTRA_MESSAGE)){
				//TODO Broadcast error message
			}
			
			String message = args.getString(EXTRA_MESSAGE);
			updateStatus(message);
			
		}

	}
	
	private void authenticate() {

		BeaconApp app = (BeaconApp) getApplicationContext();
		RequestToken requestToken = null;

		if (app.hasTwitterRequestToken()) {
			requestToken = new RequestToken(app.getTwitterRequestToken(),
					app.getTwitterRequestSecretToken());
		} else {
			try {
				requestToken = mTwitter.getOAuthRequestToken();
			} catch (TwitterException e) {
				Log.e(TAG + "[" + e.getStackTrace()[0].getLineNumber() + "]",
						e.toString());
				return;
			}
		}

		
		if(BuildConfig.DEBUG){
			Log.d(TAG, "Request Token = " + requestToken.getToken());
			Log.d(TAG, "Request Token Secret = " + requestToken.getTokenSecret());
			
		}
		app.setTwitterRequestToken(requestToken.getToken());
		app.setTwitterRequestSecretToken(requestToken.getTokenSecret());

		String url = requestToken.getAuthorizationURL();
		if(BuildConfig.DEBUG){
			Log.d(TAG, "AuthorizationUrl = " + url);
		}

		Intent intent = new Intent(this, TwitterPinActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra(TwitterPinActivity.EXTRA_URL, url);
		startActivity(intent);
	}

	private void getAccessToken(String pin) {
		BeaconApp app = (BeaconApp) getApplicationContext();
		if (app.hasTwitterRequestToken()) {
			RequestToken requestToken = new RequestToken(
					app.getTwitterRequestToken(),
					app.getTwitterRequestSecretToken());
			AccessToken accessToken = null;
			try {
				accessToken = mTwitter.getOAuthAccessToken(requestToken, pin);
			} catch (TwitterException e) {
				Log.e(TAG + "[" + e.getStackTrace()[0].getLineNumber() + "]",
						e.toString());
				return;
			}

			if(BuildConfig.DEBUG){
				Log.d(TAG, "Access Token = " + accessToken.getToken());
				Log.d(TAG, "Access Token Secret = " + accessToken.getTokenSecret());
				Log.d(TAG, "Screen Name = " + accessToken.getScreenName());
				Log.d(TAG, "User ID = " + accessToken.getUserId());
			}
			
			app.setTwitterAccessToken(accessToken.getToken());
			app.setTwitterAccessTokenSecret(accessToken.getTokenSecret());
			app.setTwitterUserId((int)accessToken.getUserId());
			app.setTwitterScreenName(accessToken.getScreenName());
			
			LocalBroadcastManager mMgr = LocalBroadcastManager.getInstance(this);
			Intent intent = new Intent();
			intent.setAction(BROADCAST_LOGIN_SUCCESSFUL);
			mMgr.sendBroadcast(intent);
		} else {
			authenticate();
		}
	}
	
	private void logout(){
		BeaconApp app = (BeaconApp)getApplicationContext();
		app.clearTwitterAccessTokenAndSecret();
		app.clearTwitterRequestTokenAndSecret();
		TwitterBeacon.clearTwitter();
		LocalBroadcastManager mMgr = LocalBroadcastManager.getInstance(this);
		Intent intent = new Intent();
		intent.setAction(BROADCAST_LOGOUT_SUCCESSFUL);
		mMgr.sendBroadcast(intent);
	}
	
	private void updateStatus(String message){
		
		if(!mBeaconApp.hasTwitterAccessToken() || !mBeaconApp.hasTwitterAccessTokenSecret()){
			
			Intent broadcastIntent = new Intent(BROADCAST_TWITTER_SERVICE_ERROR);
			broadcastIntent.putExtra(EXTRA_MESSAGE, getString(R.string.error_no_access_token));
			mLocalBMgr.sendBroadcast(broadcastIntent);
			return;
			
		}
		AccessToken accessToken = new AccessToken(mBeaconApp.getTwitterAccessToken(), mBeaconApp.getTwitterAccessTokenSecret());
		mTwitter.setOAuthAccessToken(accessToken);
		
		try {
			mTwitter.updateStatus("This is a developer testing his app. plz ignore." +
					"If this is annoying, please tweet back kindly telling the developer to stfu.");
		} catch (TwitterException e) {
			Log.e(TAG, e.toString());
			//TODO Broadcast error message
		}
		
		Intent broadcast = new Intent(BROADCAST_TWITTER_LOG_MESSAGE);
		broadcast.putExtra(BROADCAST_TWITTER_LOG_MESSAGE, getString(R.string.tweet_sent) + "Message: " + message);
		mLocalBMgr.sendBroadcast(broadcast);
	}

}
