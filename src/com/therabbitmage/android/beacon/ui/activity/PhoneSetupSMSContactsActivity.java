package com.therabbitmage.android.beacon.ui.activity;

import java.util.ArrayList;

import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.therabbitmage.android.beacon.R;
import com.therabbitmage.android.beacon.entities.beacon.BeaconSMSContact;
import com.therabbitmage.android.beacon.provider.Beacon;
import com.therabbitmage.android.beacon.provider.BeaconManager;
import com.therabbitmage.android.beacon.ui.adapter.ContactsAdapter;
import com.therabbitmage.android.beacon.utils.ContactHelper;

public class PhoneSetupSMSContactsActivity extends BaseActionBarActivity implements LoaderCallbacks<Cursor> {
	private ListView mContactsList;
	private TextView mEmptyView;
	private ProgressBar mProgress;
	private ContactsAdapter mAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setupUI();
		mAdapter = new ContactsAdapter(this);
		mContactsList.setAdapter(mAdapter);
		getSupportLoaderManager().initLoader(0, null, this);
	}
	
	private void setupUI(){
		setContentView(R.layout.phone_setup_sms_contacts);
		mContactsList = (ListView)findViewById(R.id.list);
		mEmptyView = (TextView)findViewById(R.id.empty);
		mProgress = (ProgressBar)findViewById(R.id.progress);
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.add_sms_contacts_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
			case android.R.id.home:
				NavUtils.navigateUpFromSameTask(this);
				return true;
			case R.id.pull_contacts:
				startAddSMSContactsActivity();
				return true;
			case R.id.purge:
				new PurgeContactListTask().execute();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	
	private void startAddSMSContactsActivity(){
		Intent intent = new Intent(this, AddSMSContactsActivity.class);
		startActivity(intent);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new CursorLoader(this,
				Beacon.BeaconMobileContactDetails.CONTENT_URI,
				Beacon.BeaconMobileContactDetails.sProjection,
				null,
				null,
				Beacon.BeaconMobileContactDetails.DEFAULT_SORT_ORDER);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		
		if(data != null && data.getCount() > 0){
			ArrayList<BeaconSMSContact> list = ContactHelper.convertCursorToBeaconContacts(data);
			mAdapter.setData(list);
			showList();
		} else if(data.getCount() == 0){
			showEmptyView(R.string.no_contacts_found);
		}
		
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		showProgressBar();
		mAdapter.setData(null);
	}
	
	private void showProgressBar(){
		mProgress.setVisibility(View.VISIBLE);
		mEmptyView.setVisibility(View.GONE);
		mContactsList.setVisibility(View.GONE);
	}
	
	private void showList(){
		mProgress.setVisibility(View.GONE);
		mEmptyView.setVisibility(View.GONE);
		mContactsList.setVisibility(View.VISIBLE);
	}
	
	private void showEmptyView(int resId){
		mProgress.setVisibility(View.GONE);
		mEmptyView.setVisibility(View.VISIBLE);
		mContactsList.setVisibility(View.GONE);
		
		mEmptyView.setText(resId);
	}
	
	private class PurgeContactListTask extends AsyncTask<Void, Void, Void>{

		@Override
		protected Void doInBackground(Void... params) {
			for(int i = 0; i < mAdapter.getData().size(); i++){
				BeaconManager mgr = new BeaconManager(PhoneSetupSMSContactsActivity.this);
				mgr.removePhoneContactByContactId(mAdapter.getData().get(i).getContactId());
			}
			
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			mAdapter.notifyDataSetChanged();
		}
		
	}

}
