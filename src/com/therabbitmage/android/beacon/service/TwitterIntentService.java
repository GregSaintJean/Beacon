package com.therabbitmage.android.beacon.service;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.therabbitmage.android.beacon.BeaconApp;
import com.therabbitmage.android.beacon.BuildConfig;
import com.therabbitmage.android.beacon.network.TwitterBeacon;
import com.therabbitmage.android.beacon.ui.activity.TwitterPinActivity;

public class TwitterIntentService extends IntentService {
	private static final String TAG = TwitterIntentService.class
			.getSimpleName();

	public static final String ACTION_AUTH = "action_twitter_auth";
	public static final String ACTION_GET_ACCESS_TOKEN = "action_get_access_token";
	public static final String ACTION_LOGOUT = "action_logout";
	
	public static final String BROADCAST_LOGIN_SUCCESSFUL = "login_successful";
	public static final String BROADCAST_LOGOUT_SUCCESSFUL = "logout_successful";

	public static final String EXTRA_PIN = "extra_pin";

	public TwitterIntentService() {
		super(TwitterIntentService.class.getSimpleName());
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

	}
	
	private void authenticate() {

		BeaconApp app = (BeaconApp) getApplicationContext();
		Twitter twitter = TwitterBeacon.getTwitter(this);
		RequestToken requestToken = null;

		if (app.hasTwitterRequestToken()) {
			requestToken = new RequestToken(app.getTwitterRequestToken(),
					app.getTwitterRequestSecretToken());
		} else {
			try {
				requestToken = twitter.getOAuthRequestToken();
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
			Twitter twitter = TwitterBeacon.getTwitter(this);
			RequestToken requestToken = new RequestToken(
					app.getTwitterRequestToken(),
					app.getTwitterRequestSecretToken());
			AccessToken accessToken = null;
			try {
				accessToken = twitter.getOAuthAccessToken(requestToken, pin);
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

}
