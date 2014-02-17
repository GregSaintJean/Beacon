package com.therabbitmage.android.beacon.ui.activity;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.therabbitmage.android.beacon.BeaconApp;
import com.therabbitmage.android.beacon.R;

public class MainActivity extends Activity implements OnClickListener{
	private static final String TAG = MainActivity.class.getSimpleName();
	
	private Button mMainButton;
	private Button m911Button; 
	private Button mAltSetupButton;
	
	private boolean mIsSetup = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setupUI();
		
		// Add code to print out the key hash
	    try {
	        PackageInfo info = getPackageManager().getPackageInfo(
	                "com.therabbitmage.android.beacon", 
	                PackageManager.GET_SIGNATURES);
	        for (Signature signature : info.signatures) {
	            MessageDigest md = MessageDigest.getInstance("SHA");
	            md.update(signature.toByteArray());
	            Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
	            }
	    } catch (NameNotFoundException e) {
	    	Log.d(TAG + e.getStackTrace()[0].getLineNumber(), e.toString());

	    } catch (NoSuchAlgorithmException e) {
	    	Log.d(TAG + e.getStackTrace()[0].getLineNumber(), e.toString());
	    }
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
