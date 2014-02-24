package com.therabbitmage.android.beacon.ui.fragment;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract.Contacts;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import com.therabbitmage.android.beacon.provider.Beacon;
import com.therabbitmage.android.beacon.ui.activity.PhoneSetupActivity;
import com.therabbitmage.android.beacon.ui.adapter.PhoneContactsAdapter;

public class PhoneContactListFragment extends Fragment implements AdapterView.OnItemClickListener, LoaderCallbacks<Cursor>{
	
	private PhoneSetupActivity mActivity;
	private TextView mEmptyView;
	private View mRoot;
	private PhoneContactsAdapter mAdapter;
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mActivity = (PhoneSetupActivity)activity;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mAdapter = new PhoneContactsAdapter(getActivity());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		return super.onCreateView(inflater, container, savedInstanceState);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new CursorLoader(getActivity(),
				Contacts.CONTENT_URI,
				Beacon.BeaconContacts.sProjection,
				null,
				null,
				Beacon.BeaconContacts.DEFAULT_SORT_ORDER);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		if(data != null && data.getCount() > 0){
			mAdapter.swapCursor(data);
		} else {
			//TODO You might want to handle the case where the user actually doesn't have any contacts
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mAdapter.swapCursor(null);
	}

}
