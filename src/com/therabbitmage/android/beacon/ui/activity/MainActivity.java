package com.therabbitmage.android.beacon.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.therabbitmage.android.beacon.BeaconApp;
import com.therabbitmage.android.beacon.R;

public class MainActivity extends Activity implements OnClickListener{
	
	private Button mMainButton;
	private Button m911Button; 
	private Button mAltSetupButton;
	
	private boolean mIsSetup = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setupUI();
	}
	
	private void setupUI(){
		setContentView(R.layout.activity_main);
		mMainButton = (Button)findViewById(R.id.mainbtn);
		m911Button = (Button)findViewById(R.id.nine_one_one_btn);
		mAltSetupButton = (Button)findViewById(R.id.alt_setup_btn);
		
		if(mIsSetup){
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
		
		if(v == mMainButton){
			
			if(mIsSetup){
				//TODO Fire!!!
			} else {
				startSetupActivity();
			}
			
		}
		
		if(v ==  m911Button){
			((BeaconApp)getApplicationContext()).dial911();
		}
		
		if(v == mAltSetupButton){
			startSetupActivity();
		}
		
	}
	
	private void startSetupActivity(){
		Intent intent = new Intent(this, SetupActivity.class);
		startActivity(intent);
	}

}
