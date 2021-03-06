package com.therabbitmage.android.beacon.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.therabbitmage.android.beacon.R;
import com.therabbitmage.android.beacon.ui.fragment.SettingsFragment;

public class NewSettingsActivity extends NavDrawerActivity {
	
	private static final String TAG = NewSettingsActivity.class.getSimpleName();
	
	private SettingsFragment mSettingsFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.new_settings_activity);
		setDrawerLayout((DrawerLayout) findViewById(R.id.drawer_layout));
		setDrawerList((ListView)findViewById(R.id.left_drawer));
		
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);
		
		ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
				this,
				getDrawerLayout(),
				R.drawable.ic_drawer,
				R.string.drawer_open, R.string.drawer_close){

					@Override
					public void onDrawerClosed(View drawerView) {
						invalidateOptionsMenu();
					}

					@Override
					public void onDrawerOpened(View drawerView) {
						invalidateOptionsMenu();
					}
			
		};;
		
		setDrawerToggle(toggle);
		
		getDrawerLayout().setDrawerListener(getDrawerToggle());
		
		ArrayAdapter<CharSequence> drawerAdapter = ArrayAdapter.createFromResource(this, R.array.settings_nav_array, R.layout.drawer_list_item);
		
		getDrawerList().setAdapter(drawerAdapter);
		getDrawerList().setOnItemClickListener(new DrawerItemClickListener());
		
		mSettingsFragment = (SettingsFragment)getSupportFragmentManager().findFragmentById(R.id.content_frame);
	}
	
	private class DrawerItemClickListener implements ListView.OnItemClickListener{

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			
			Log.d(TAG, "Detecting selection");
			Log.d(TAG, "Position = " + position);
			
			if(parent == null || view == null){
				Log.d(TAG, "Returning from null checks");
				return;
			}
			
			if(position < 0 || id < 0){
				Log.d(TAG, "Returning from number checks");
				return;
			}
			
			switch(position){
				case 0:
					startMainActivity();
					break;
				case 1:
					startSetupActivity();
					break;
			}
			
		}
	}
	
	public void startMainActivity(){
		Intent intent = new Intent(this, NewMainActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION| Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}
	
	public void startSetupActivity(){
		Intent intent = new Intent(this, NewSetupActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION| Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}
}
