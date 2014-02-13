package com.therabbitmage.android.beacon.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.therabbitmage.android.beacon.R;
import com.therabbitmage.android.beacon.ui.fragment.SocialSetupFragment;

public class SetupActivity extends FragmentActivity implements OnClickListener{
	
	private Button mNextButton;
	private Button mCancelButton;
	private FragmentManager mFragManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setupUI();
	}
	
	public void setupUI(){
		setContentView(R.layout.setup_activity);
		mFragManager = getSupportFragmentManager();
		mNextButton = (Button)findViewById(R.id.next_btn);
		mCancelButton = (Button)findViewById(R.id.cancel_btn);
		
		mNextButton.setOnClickListener(this);
		mCancelButton.setOnClickListener(this);
		
		SocialSetupFragment fragment = new SocialSetupFragment();
		FragmentTransaction transaction = mFragManager.beginTransaction();
		transaction.add(R.id.content_container, fragment);
		transaction.commit();
	}

	@Override
	public void onClick(View v) {
		
		if(v == mNextButton){
			
		}
		
		if(v == mCancelButton){
			startMainActivity();
		}
		
	}
	
	private void replaceFragment(Fragment fragment){
		FragmentTransaction transaction = mFragManager.beginTransaction();
		transaction.replace(R.id.content_container, fragment);
		transaction.commit();
	}
	
	private void startMainActivity(){
		Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);
	}

}
