package com.therabbitmage.android.beacon.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.therabbitmage.android.beacon.R;
import com.therabbitmage.android.beacon.service.TwitterIntentService;

public class TwitterPinActivity extends Activity {

	public static final String ACTION_GET_AUTHORIZATION = "action_authorization";
	public static final String EXTRA_URL = "extra_url";

	private EditText mEditText;
	private WebView mWebView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.twitter_pin_activity);
		mWebView = (WebView) findViewById(R.id.auth_web);
		mEditText = (EditText) findViewById(R.id.twitter_pin);

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
	}
}
