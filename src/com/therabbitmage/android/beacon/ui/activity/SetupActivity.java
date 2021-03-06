package com.therabbitmage.android.beacon.ui.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.content.LocalBroadcastManager;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.therabbitmage.android.beacon.BeaconApp;
import com.therabbitmage.android.beacon.R;
import com.therabbitmage.android.beacon.service.TwitterIntentService;

public class SetupActivity extends Activity implements OnClickListener{
	
	private BroadcastReceiver mTwitterReceiver;
	private LocalBroadcastManager mLBMgr;
	private Button mPhoneContactsBtn, mTwitterAccountBtn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mLBMgr = LocalBroadcastManager.getInstance(this);
		if(!BeaconApp.isSetupDone()){
			BeaconApp.setIsSetupDone(true);
		}
		setupUI();
		setupTwitter();
	}
	
	private void setupUI(){
		setContentView(R.layout.setup_activity);
		mPhoneContactsBtn =  (Button)findViewById(R.id.setup_contacts_btn);
		mTwitterAccountBtn = (Button)findViewById(R.id.setup_twitter_btn);
		mPhoneContactsBtn.setOnClickListener(this);
		updateViewForTwitter();
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}
	
	private void setupTwitter(){
		IntentFilter twitterFilter = new IntentFilter(TwitterIntentService.BROADCAST_LOGIN_SUCCESSFUL);
		twitterFilter.addAction(TwitterIntentService.BROADCAST_LOGOUT_SUCCESSFUL);
		mTwitterReceiver =  new TwitterReceiver();
		mLBMgr.registerReceiver(mTwitterReceiver, twitterFilter);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mLBMgr.unregisterReceiver(mTwitterReceiver);
	}

	@Override
	public void onClick(View v) {
		
		if(mPhoneContactsBtn != null && v.getId() == mPhoneContactsBtn.getId()){
			openSMSSetupActivity();
		}
		
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
			case android.R.id.home:
				NavUtils.navigateUpFromSameTask(this);
				return true;
			default:
				return super.onOptionsItemSelected(item);
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
	
	private void openSMSSetupActivity(){
		Intent intent = new Intent(this, PhoneSetupSMSContactsActivity.class);
		startActivity(intent);
	}
	
	private void updateViewForTwitter(){
		if(mTwitterAccountBtn != null){
			if(BeaconApp.hasTwitterLogin()){
				mTwitterAccountBtn.setText(R.string.logout);
				mTwitterAccountBtn.setOnClickListener(new OnClickListener(){

					@Override
					public void onClick(View v) {
						logoutTwitter();
					}
					
				});
			} else {
				mTwitterAccountBtn.setText(R.string.login);
				mTwitterAccountBtn.setOnClickListener(new OnClickListener(){

					@Override
					public void onClick(View v) {
						loginTwitter();
					}
					
				});
			}
		}
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
