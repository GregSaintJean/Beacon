package com.therabbitmage.android.beacon.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.therabbitmage.android.beacon.BeaconApp;
import com.therabbitmage.android.beacon.BuildConfig;
import com.therabbitmage.android.beacon.R;
import com.therabbitmage.android.beacon.utils.AndroidUtils;

public class MainActivity extends Activity implements OnClickListener {

	private static final String TAG = MainActivity.class.getSimpleName();
	private Button mMainButton;
	private Button m911Button;
	private Button mAltSetupButton;
	private BeaconApp mBeaconApp;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if(BuildConfig.DEBUG){
			AndroidUtils.enableStrictMode();
		}
		super.onCreate(savedInstanceState);
		mBeaconApp = ((BeaconApp) getApplicationContext());
		if(mBeaconApp.isFirstRun()){
			Log.d(TAG, "first run executed");
			mBeaconApp.firstRunExecuted();
		}
		setupUI();
	}

	private void setupUI() {
		setContentView(R.layout.activity_main);
		mMainButton = (Button) findViewById(R.id.mainbtn);
		m911Button = (Button) findViewById(R.id.nine_one_one_btn);
		mAltSetupButton = (Button) findViewById(R.id.alt_setup_btn);

		if (mBeaconApp.isSetupDone()) {
			mMainButton.setText(R.string.fire);
			mAltSetupButton.setVisibility(View.VISIBLE);
		} else {
			mMainButton.setText(R.string.setup);
		}

		mMainButton.setOnClickListener(this);
		m911Button.setOnClickListener(this);
		mAltSetupButton.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {

		if (v == mMainButton) {

			if (mBeaconApp.isSetupDone()) {
				Intent intent = new Intent(this, EmergencyActivity.class);
				startActivity(intent);
			} else {
				startSetupActivity();
			}

		}

		if (v == m911Button) {
			mBeaconApp.dial911();
		}

		if (v == mAltSetupButton) {
			startSetupActivity();
		}

	}

	private void startSetupActivity() {
		Intent intent = new Intent(this, PhoneSetupActivity.class);
		startActivity(intent);
	}

}
