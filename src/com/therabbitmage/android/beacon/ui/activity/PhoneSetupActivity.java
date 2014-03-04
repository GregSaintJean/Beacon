package com.therabbitmage.android.beacon.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.therabbitmage.android.beacon.BuildConfig;
import com.therabbitmage.android.beacon.R;
import com.therabbitmage.android.beacon.ui.fragment.PhoneContactListFragment;
import com.therabbitmage.android.beacon.ui.fragment.PhoneSetupFragment;
import com.therabbitmage.android.beacon.utils.AndroidUtils;

public class PhoneSetupActivity extends FragmentActivity implements OnClickListener {

	private Button mNextButton;
	private Button mCancelButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if(BuildConfig.DEBUG){
			AndroidUtils.enableStrictMode();
		}
		super.onCreate(savedInstanceState);
		setupUI();
	}

	public void setupUI() {
		setContentView(R.layout.setup_activity);
		mNextButton = (Button) findViewById(R.id.next_btn);
		mCancelButton = (Button) findViewById(R.id.cancel_btn);

		mNextButton.setOnClickListener(this);
		mCancelButton.setOnClickListener(this);

		//TODO Support Two Pane setup
		PhoneSetupFragment fragment = new PhoneSetupFragment();
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		transaction.add(R.id.content_container, fragment);
		transaction.commit();
	}

	@Override
	public void onClick(View view) {

		if (view.getId() == R.id.next_btn) {
			startSocialSetupActivity();
		}

		if (view.getId() == R.id.cancel_btn) {
			onBackPressed();
		}

	}
	
	public void showPhoneContactListFragment(){
		PhoneContactListFragment fragment = new PhoneContactListFragment();
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		transaction.replace(R.id.content_container, fragment);
		transaction.addToBackStack(fragment.getClass().getSimpleName());
		transaction.commit();
	}
	
	private void startSocialSetupActivity(){
		Intent intent = new Intent(this, SocialSetupActivity.class);
		startActivity(intent);
	}

}
