package com.therabbitmage.android.beacon.ui.activity;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;
import android.widget.BaseAdapter;
import android.widget.ListView;


public abstract class NavDrawerActivity extends BaseActivity {
	
	private static final String TAG = NavDrawerActivity.class.getSimpleName();
	
	private ActionBarDrawerToggle mDrawerToggle;
	private BaseAdapter mDrawerAdapter;
	private ListView mDrawerList;
	private DrawerLayout mDrawerLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	public BaseAdapter getDrawerAdapter(){
		return mDrawerAdapter;
	}
	
	public DrawerLayout getDrawerLayout(){
		return mDrawerLayout;
	}
	
	public void setDrawerLayout(DrawerLayout dl){
		mDrawerLayout = dl;
	}
	
	public ActionBarDrawerToggle getDrawerToggle(){
		return mDrawerToggle;
	}
	
	public ListView getDrawerList(){
		return mDrawerList;
	}
	
	public void setDrawerList(ListView lv){
		mDrawerList = lv;
	}
	
	public void setDrawerToggle(ActionBarDrawerToggle toggle){
		mDrawerToggle = toggle;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
		
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		if(mDrawerToggle != null){
			mDrawerToggle.syncState();
		}
		
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		if(mDrawerToggle != null){
			mDrawerToggle.onConfigurationChanged(newConfig);
		}
	}
	
}
