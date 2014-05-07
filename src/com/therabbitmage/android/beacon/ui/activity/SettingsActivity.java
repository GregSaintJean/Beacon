package com.therabbitmage.android.beacon.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;

import com.therabbitmage.android.beacon.R;

public class SettingsActivity extends PreferenceActivity {

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
	}

	@Override
	@Deprecated
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
			Preference preference) {
		
		if(preference.getKey().equals("pref_about_beacon")){
			Intent intent = new Intent(this, AboutActivity.class);
			startActivity(intent);
			return true;
		}
		
		return super.onPreferenceTreeClick(preferenceScreen, preference);
	}

}
