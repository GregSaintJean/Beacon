package com.therabbitmage.android.beacon.ui.fragment;

import android.annotation.TargetApi;
import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Data;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.util.ArrayMap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.therabbitmage.android.beacon.R;
import com.therabbitmage.android.beacon.provider.Beacon.BeaconMobileContactDetails;
import com.therabbitmage.android.beacon.ui.activity.PhoneSetupActivity;
import com.therabbitmage.android.beacon.ui.adapter.PhoneContactsAdapter;
import com.therabbitmage.android.beacon.utils.AndroidUtils;

public class PhoneContactListFragment extends Fragment implements AdapterView.OnItemClickListener, LoaderCallbacks<Cursor>{
	
	private PhoneSetupActivity mActivity;
	private TextView mEmptyView;
	private View mRoot;
	private ProgressBar mProgressBar;
	private ListView mList;
	private PhoneContactsAdapter mAdapter;
	private ArrayMap<Integer, Boolean> mAddedItems;
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mActivity = (PhoneSetupActivity)activity;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mAdapter = new PhoneContactsAdapter(getActivity());
		mAddedItems = new ArrayMap<Integer,Boolean>();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mRoot = inflater.inflate(R.layout.phone_contact_list_fragment, container, false);
		mEmptyView = (TextView)mRoot.findViewById(R.id.empty_view);
		mProgressBar = (ProgressBar)mRoot.findViewById(R.id.progress_bar);
		mList = (ListView)mRoot.findViewById(R.id.contact_list);
		mList.setAdapter(mAdapter);
		return mRoot;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		getLoaderManager().initLoader(MobileContactsQuery.MOBILE_QUERY_ID, null, this);
		getLoaderManager().initLoader(BeaconMobileQuery.BEACON_QUERY_ID, null, this);
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		
		switch(id){
			case MobileContactsQuery.MOBILE_QUERY_ID:
				return new CursorLoader(getActivity(),
						MobileContactsQuery.CONTENT_URI,
						MobileContactsQuery.PROJECTION,
						MobileContactsQuery.SELECTION,
						null,
						MobileContactsQuery.SORT_ORDER);
			case BeaconMobileQuery.BEACON_QUERY_ID:
				return new CursorLoader(getActivity(),
						BeaconMobileQuery.CONTENT_URI,
						BeaconMobileQuery.PROJECTION,
						null,
						null,
						BeaconMobileQuery.SORT_ORDER);
		}
		
		return null;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		
		switch(loader.getId()){
			case MobileContactsQuery.MOBILE_QUERY_ID:
				if(data != null && data.getCount() > 0){
					mAdapter.swapCursor(data);
					mProgressBar.setVisibility(View.GONE);
					mList.setVisibility(View.VISIBLE);
					if(mAddedItems != null){
						mAdapter.setSelectedNumbers(mAddedItems);
					}
				} else {
					mEmptyView.setVisibility(View.VISIBLE);
					mProgressBar.setVisibility(View.GONE);
					//TODO You might want to handle the case where the user actually doesn't have any contacts
				}
				break;
			case BeaconMobileQuery.BEACON_QUERY_ID:
				if(data != null && data.getCount() > 0){
					mAddedItems.clear();
					
					if(data.moveToFirst()){
						do{
							mAddedItems.put(
									data.getInt(
									data.getColumnIndex(
											BeaconMobileContactDetails.CN_CONTACT_ID)), 
											true);
						} while(data.moveToNext());
					}
					
					data.close();
					
					if(mAdapter != null){
						mAdapter.setSelectedNumbers(mAddedItems);
					}
					
				}
				
				break;
		}
		
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mAdapter.swapCursor(null);
	}
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public interface MobileContactsQuery{
		
		final static int MOBILE_QUERY_ID = 1;
		
		final static Uri CONTENT_URI = Data.CONTENT_URI;
		
		final static String[] PROJECTION = {
			Data._ID,
			AndroidUtils.honeycombOrBetter() ? Data.DISPLAY_NAME_PRIMARY : Data.DISPLAY_NAME,
			Phone.NUMBER,
			Phone.TYPE,
			Phone.LABEL
		};
		
		final static String SELECTION = Phone.TYPE + "='" + Phone.TYPE_MOBILE + "' AND " + Data.MIMETYPE + "='" + Phone.CONTENT_ITEM_TYPE + "'";
		
		final static String SORT_ORDER = AndroidUtils.honeycombOrBetter() ? Phone.DISPLAY_NAME_PRIMARY : Phone.DISPLAY_NAME;
		
	}
	
	public interface BeaconMobileQuery{
		
		final static int BEACON_QUERY_ID = 0;
		
		final static Uri CONTENT_URI = BeaconMobileContactDetails.CONTENT_URI;
		
		final static String[] PROJECTION = BeaconMobileContactDetails.sProjection;
		
		final static String SORT_ORDER = BeaconMobileContactDetails.DEFAULT_SORT_ORDER;
	}
	
	

}
