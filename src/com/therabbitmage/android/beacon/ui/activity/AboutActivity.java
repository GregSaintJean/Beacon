package com.therabbitmage.android.beacon.ui.activity;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.therabbitmage.android.beacon.R;

public class AboutActivity extends ActionBarActivity {
	
	private static final String TAG = AboutActivity.class.getSimpleName();
	
	private TextView mAppInfo;
	private TextView mOpenSource;
	private TextView mTwitter4JLink;
	private TextView mGson;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about_activity);
		mAppInfo = (TextView)findViewById(R.id.app_info);
		mOpenSource = (TextView)findViewById(R.id.open_source_names);
		mAppInfo.setVisibility(View.VISIBLE);
		mTwitter4JLink = (TextView)findViewById(R.id.twitter4j_link);
		mTwitter4JLink.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				Uri uri = Uri.parse("http://twitter4j.org/");
				Intent intent = new Intent(Intent.ACTION_VIEW, uri);
				 if (intent.resolveActivity(getPackageManager()) != null) {
					 startActivity(intent);
				 }
				
			}
			
		});
		mGson = (TextView)findViewById(R.id.gson_link);
		mGson.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				Uri uri = Uri.parse("https://code.google.com/p/google-gson/");
				Intent intent = new Intent(Intent.ACTION_VIEW, uri);
				if (intent.resolveActivity(getPackageManager()) != null) {
					startActivity(intent);
				}
			}
			
		});
		
		PackageManager pMgr = this.getPackageManager();
		PackageInfo info;
		try {
			info = pMgr.getPackageInfo(this.getPackageName(), 0);
			String versionCode = Integer.toString(info.versionCode);
			String versionName = info.versionName;
			
			mAppInfo.setText(String.format(getString(R.string.version), versionName, versionCode));
		} catch (NameNotFoundException e) {
			Log.e(TAG, e.toString());
			mAppInfo.setVisibility(View.GONE);
		}
		
	}

}
