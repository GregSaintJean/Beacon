package com.therabbitmage.android.beacon.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class SocialSetupActivity extends FragmentActivity implements OnClickListener {
	
	private Button mNextButton;
	private Button mCancelButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setupUI();
	}
	
	private void setupUI() {
		
	}
	
	@Override
	public void onClick(View view) {
		
		if (view == mNextButton) {

		}

		if (view == mCancelButton) {
			startPhoneSetupActivity();
		}
		
	}
	
	private void startPhoneSetupActivity() {
		Intent intent = new Intent(this, PhoneSetupActivity.class);
		startActivity(intent);
	}

}
