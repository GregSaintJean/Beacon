package com.therabbitmage.android.beacon.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.therabbitmage.android.beacon.R;
import com.therabbitmage.android.beacon.ui.fragment.PhoneSetupFragment;

public class PhoneSetupActivity extends FragmentActivity implements OnClickListener {

	private Button mNextButton;
	private Button mCancelButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setupUI();
	}

	public void setupUI() {
		setContentView(R.layout.setup_activity);
		mNextButton = (Button) findViewById(R.id.next_btn);
		mCancelButton = (Button) findViewById(R.id.cancel_btn);

		mNextButton.setOnClickListener(this);
		mCancelButton.setOnClickListener(this);

		PhoneSetupFragment fragment = new PhoneSetupFragment();
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		transaction.add(R.id.content_container, fragment);
		transaction.commit();
	}

	@Override
	public void onClick(View view) {

		if (view.getId() == R.id.next_btn) {

		}

		if (view.getId() == R.id.cancel_btn) {
			startMainActivity();
		}

	}

	private void startMainActivity() {
		Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);
	}

}
