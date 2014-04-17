package com.therabbitmage.android.beacon.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.therabbitmage.android.beacon.BuildConfig;
import com.therabbitmage.android.beacon.R;
import com.therabbitmage.android.beacon.network.TwitterBeacon;
import com.therabbitmage.android.beacon.service.TwitterIntentService;
import com.therabbitmage.android.beacon.utils.AndroidUtils;

public class TwitterPinActivity extends BaseActivity {
	
	private LocalBroadcastManager mBMgr;
	private BroadcastReceiver mTwitterReceiver;

	public static final String ACTION_GET_AUTHORIZATION = "action_authorization";
	public static final String EXTRA_URL = "extra_url";

	private EditText mEditText;
	private WebView mWebView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if(BuildConfig.DEBUG){
			AndroidUtils.enableStrictMode();
		}
		super.onCreate(savedInstanceState);
		setContentView(R.layout.twitter_pin_activity);
		mWebView = (WebView) findViewById(R.id.auth_web);
		mEditText = (EditText) findViewById(R.id.twitter_pin);

		mWebView.clearCache(true);
		mWebView.loadUrl(getIntent().getStringExtra(EXTRA_URL));
		mEditText.setOnEditorActionListener(new OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView view, int actionId,
					KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE) {

					Intent intent = new Intent(TwitterPinActivity.this,
							TwitterIntentService.class);
					intent.setAction(TwitterIntentService.ACTION_GET_ACCESS_TOKEN);
					intent.putExtra(TwitterIntentService.EXTRA_PIN, view
							.getText().toString());
					startService(intent);
					return true;
				}
				return false;
			}

		});
		
		mBMgr = LocalBroadcastManager.getInstance(this);
		IntentFilter twitterFilter = new IntentFilter(TwitterIntentService.BROADCAST_LOGIN_SUCCESSFUL);
		mTwitterReceiver = new TwitterReceiver();
		mBMgr.registerReceiver(mTwitterReceiver, twitterFilter);
	}

	@Override
	public void onBackPressed() {
		mApp.clearTwitterAccessTokenAndSecret();
		mApp.clearTwitterRequestTokenAndSecret();
		TwitterBeacon.clearTwitter();
		super.onBackPressed();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mBMgr.unregisterReceiver(mTwitterReceiver);
	}
	
	private class TwitterReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			
			if(intent.getAction().equals(TwitterIntentService.BROADCAST_LOGIN_SUCCESSFUL)){
				finish();
			}
			
		}
		
	}
}
