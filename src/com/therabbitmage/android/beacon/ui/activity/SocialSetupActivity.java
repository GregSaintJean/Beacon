package com.therabbitmage.android.beacon.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.facebook.LoggingBehavior;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.Settings;
import com.facebook.widget.LoginButton;
import com.google.android.gms.common.SignInButton;
import com.therabbitmage.android.beacon.BuildConfig;
import com.therabbitmage.android.beacon.R;
import com.therabbitmage.android.beacon.service.TwitterIntentService;
import com.therabbitmage.android.beacon.utils.AndroidUtils;

public class SocialSetupActivity extends BaseActivity implements OnClickListener {
	
	private Session.StatusCallback mStatusCallback = new SessionStatusCallback();
	
	private BroadcastReceiver mTwitterReceiver;
	private LocalBroadcastManager mLBMgr;
	
	private Button mTwitterButton;
	private LoginButton mFacebookButton;
	private SignInButton mGoogleButton;
	
	private Button mNextButton;
	private Button mCancelButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if(BuildConfig.DEBUG){
			AndroidUtils.enableStrictMode();
		}
		super.onCreate(savedInstanceState);
		mLBMgr = LocalBroadcastManager.getInstance(this);
		setupUI();
		setupFacebookSDK(savedInstanceState);
		setupTwitter();
	}
	
	private void setupUI() {
		setContentView(R.layout.social_setup_activity);
		mTwitterButton = (Button)findViewById(R.id.twitter_login_btn);
		mFacebookButton = (LoginButton)findViewById(R.id.facebook_login_btn);
		mGoogleButton = (SignInButton)findViewById(R.id.google_login_btn);
		
		mNextButton = (Button)findViewById(R.id.next_btn);
		mCancelButton = (Button)findViewById(R.id.cancel_btn);
		
		mGoogleButton.setOnClickListener(this);
		
		mNextButton.setOnClickListener(this);
		mCancelButton.setOnClickListener(this);
	}
	
	private void setupFacebookSDK(Bundle savedInstanceState){
		Settings.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS);
		Session session = Session.getActiveSession();
        if (session == null) {
            if (savedInstanceState != null) {
                session = Session.restoreSession(this, null, mStatusCallback, savedInstanceState);
            }
            if (session == null) {
                session = new Session(this);
            }
            Session.setActiveSession(session);
            if (session.getState().equals(SessionState.CREATED_TOKEN_LOADED)) {
                session.openForRead(new Session.OpenRequest(this).setCallback(mStatusCallback));
            }
        }
        
        updateViewForFacebook();
	}
	
	//Facebook SDK
	private void updateViewForFacebook(){
			Session session = Session.getActiveSession();
			if(session.isOpened()){
				mFacebookButton.setText(R.string.logout);
				mFacebookButton.setOnClickListener(new OnClickListener(){

					@Override
					public void onClick(View v) {
						onFacebookClickLogout();
					}
					
				});
			} else {
				mFacebookButton.setText(R.string.login);
				mFacebookButton.setOnClickListener(new OnClickListener(){

					@Override
					public void onClick(View v) {
						onFacebookClickLogin();
					}
					
				});
			}
	}
		
	//Facebook SDK
	private void onFacebookClickLogin(){
		Session session = Session.getActiveSession();
		if(!session.isOpened() && !session.isClosed()){
			session.openForRead(new Session.OpenRequest(this).setCallback(mStatusCallback));
		} else {
			Session.openActiveSession(this, true, mStatusCallback);
		}
	}
		
	//Facebook SDK
	private void onFacebookClickLogout(){
		Session session = Session.getActiveSession();
		if(!session.isClosed()){
			session.closeAndClearTokenInformation();
		}
	}
	
	private void setupTwitter(){
		IntentFilter twitterFilter = new IntentFilter(TwitterIntentService.BROADCAST_LOGIN_SUCCESSFUL);
		twitterFilter.addAction(TwitterIntentService.BROADCAST_LOGOUT_SUCCESSFUL);
		mTwitterReceiver =  new TwitterReceiver();
		mLBMgr.registerReceiver(mTwitterReceiver, twitterFilter);
		updateViewForTwitter();
	}
	
	private void updateViewForTwitter(){
		if(mBeaconApp.hasTwitterLogin()){
			mTwitterButton.setText(R.string.logout);
			mTwitterButton.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					logoutTwitter();
				}
				
			});
		} else {
			mTwitterButton.setText(R.string.login);
			mTwitterButton.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					loginTwitter();
				}
				
			});
		}
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		//Facebook SDK
		Session.getActiveSession().addCallback(mStatusCallback);
	}

	@Override
	protected void onResume() {
		super.onResume();
		updateViewForTwitter();
	}

	@Override
	protected void onStop() {
		super.onStop();
		//Facebook SDK
		Session.getActiveSession().removeCallback(mStatusCallback);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mLBMgr.unregisterReceiver(mTwitterReceiver);
	}
	
	@Override
	public void onClick(View view) {
		
		if(view.getId() == R.id.google_login_btn){
			
		}
		
		if (view.getId() == R.id.next_btn) {
			startFinishActivity();
		}

		if (view.getId() == R.id.cancel_btn) {
			onBackPressed();
		}
		
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		//Facebook SDK
		Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		//Facebook SDK
		Session session = Session.getActiveSession();
		Session.saveSession(session, outState);
	}

	//Facebook SDK
	private class SessionStatusCallback implements Session.StatusCallback{

		@Override
		public void call(Session session, SessionState state, Exception exception) {
			updateViewForFacebook();
		}
		
	}
	
	private void loginTwitter(){
		Intent intent = new Intent(this, TwitterIntentService.class);
		intent.setAction(TwitterIntentService.ACTION_AUTH);
		startService(intent);
	}
	
	private void logoutTwitter(){
		Intent intent = new Intent(this, TwitterIntentService.class);
		intent.setAction(TwitterIntentService.ACTION_LOGOUT);
		startService(intent);
	}
	
	private void startFinishActivity(){
		Intent intent = new Intent(this, SetupFinishActivity.class);
		startActivity(intent);
	}
	
	private class TwitterReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context ctx, Intent intent) {
			
			if(intent.getAction().equals(TwitterIntentService.BROADCAST_LOGIN_SUCCESSFUL) 
					|| intent.getAction().equals(TwitterIntentService.BROADCAST_LOGOUT_SUCCESSFUL)){
				
				updateViewForTwitter();
				
			}
			
		}
		
	}

}
