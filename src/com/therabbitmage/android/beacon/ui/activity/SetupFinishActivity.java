package com.therabbitmage.android.beacon.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.therabbitmage.android.beacon.R;

public class SetupFinishActivity extends BaseActivity {
	
	private Button mFinishButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setup_finish_activity);
		
		mBeaconApp.setIsSetupDone(true);
		
		mFinishButton = (Button)findViewById(R.id.finish_btn);
		mFinishButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				headBackToMainActivity();
			}
			
		});
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		headBackToMainActivity();
	}

	private void headBackToMainActivity(){
		Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);
	}

}
