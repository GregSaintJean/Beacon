package com.therabbitmage.android.beacon.ui.activity;

import java.util.ArrayList;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.therabbitmage.android.beacon.R;
import com.therabbitmage.android.beacon.entities.beacon.BeaconSMSContact;
import com.therabbitmage.android.beacon.entities.beacon.PhoneContact;
import com.therabbitmage.android.beacon.provider.BeaconMobileQuery;
import com.therabbitmage.android.beacon.provider.MobileContactsQuery;
import com.therabbitmage.android.beacon.ui.adapter.SMSContactAdapter;
import com.therabbitmage.android.beacon.utils.ContactHelper;

public class AddSMSContactsActivity extends ActionBarActivity 
implements AdapterView.OnItemClickListener, LoaderCallbacks<Cursor> {
	
	private static final String TAG = AddSMSContactsActivity.class.getSimpleName();
	private TextView mEmpty;
	private ListView mList;
	private ProgressBar mProgress;
	private SMSContactAdapter mAdapter;
	private boolean mHasStarted = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setupUI();
		mAdapter = new SMSContactAdapter(this);
		mList.setAdapter(mAdapter);
		getSupportLoaderManager().initLoader(MobileContactsQuery.MOBILE_QUERY_ID, null, this);
		getSupportLoaderManager().initLoader(BeaconMobileQuery.BEACON_QUERY_ID, null, this);
		//mHasStarted = true;
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		/*if(mHasStarted){
			getSupportLoaderManager().restartLoader(MobileContactsQuery.MOBILE_QUERY_ID, null, this);
			getSupportLoaderManager().restartLoader(BeaconMobileQuery.BEACON_QUERY_ID, null, this);
		}*/
	}

	private void setupUI(){
		setContentView(R.layout.add_sms_contacts_activity);
		mProgress = (ProgressBar)findViewById(R.id.progress);
		mList = (ListView)findViewById(R.id.list);
		mEmpty = (TextView)findViewById(R.id.empty);
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
			case android.R.id.home:
				NavUtils.navigateUpFromSameTask(this);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		
	}
	
	private void showProgressBar(){
		mProgress.setVisibility(View.VISIBLE);
		mEmpty.setVisibility(View.GONE);
		mList.setVisibility(View.GONE);
	}
	
	private void showList(){
		mProgress.setVisibility(View.GONE);
		mEmpty.setVisibility(View.GONE);
		mList.setVisibility(View.VISIBLE);
	}
	
	private void showEmptyView(int resId){
		mProgress.setVisibility(View.GONE);
		mEmpty.setVisibility(View.VISIBLE);
		mList.setVisibility(View.GONE);
		mEmpty.setText(resId);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		
		Loader<Cursor> l = null;
		switch(id){
			case MobileContactsQuery.MOBILE_QUERY_ID:
				l = new CursorLoader(this,
						MobileContactsQuery.CONTENT_URI,
						MobileContactsQuery.PROJECTION,
						MobileContactsQuery.SELECTION,
						null,
						MobileContactsQuery.SORT_ORDER);
				return l;
			case BeaconMobileQuery.BEACON_QUERY_ID:
				l = new CursorLoader(this,
					BeaconMobileQuery.CONTENT_URI,
					BeaconMobileQuery.PROJECTION,
					null,
					null,
					BeaconMobileQuery.SORT_ORDER);
				return l;
		}
		return null;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		
		switch(loader.getId()){
			case MobileContactsQuery.MOBILE_QUERY_ID:
				if(data != null && data.getCount() > 0){
					ArrayList<PhoneContact> phoneContacts = ContactHelper.convertCursortoPhoneContacts(data);
					mAdapter.setPhoneContacts(phoneContacts);
				} else if(data.getCount() == 0){
					showEmptyView(R.string.no_contacts_found);
					return;
				}
				
				break;
			case BeaconMobileQuery.BEACON_QUERY_ID:
				if(data != null){
					ArrayList<BeaconSMSContact> beaconContacts = ContactHelper.convertCursorToBeaconContacts(data);
					mAdapter.setBeaconContacts(beaconContacts);
				}
				
				break;
		}
		
		if(mAdapter.getCount() > 0){
			showList();
		}
		
	}

	@Override
	public void onLoaderReset(Loader<Cursor> data) {
		mAdapter.setBeaconContacts(null);
		mAdapter.setPhoneContacts(null);
	}

}
