package com.therabbitmage.android.beacon.ui.fragment;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.therabbitmage.android.beacon.BeaconApp;
import com.therabbitmage.android.beacon.R;
import com.therabbitmage.android.beacon.provider.Beacon;
import com.therabbitmage.android.beacon.ui.activity.PhoneSetupActivity;
import com.therabbitmage.android.beacon.ui.adapter.BeaconContactsAdapter;

public class PhoneSetupFragment extends Fragment implements OnClickListener, LoaderCallbacks<Cursor> {
	
	private PhoneSetupActivity mActivity;
	private View mRoot;
	private Button mAddContactButton;
	private ListView mContactList;
	private TextView mEmptyView;
	private ProgressBar mProgress;
	private BeaconContactsAdapter mAdapter;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mActivity = (PhoneSetupActivity)activity;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mAdapter = new BeaconContactsAdapter((FragmentActivity)getActivity());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mRoot = inflater.inflate(R.layout.phone_setup_fragment, container, false);
		mAddContactButton = (Button)mRoot.findViewById(R.id.add_contact_btn);
		mContactList = (ListView)mRoot.findViewById(R.id.contact_list);
		mEmptyView = (TextView)mRoot.findViewById(R.id.empty_view);
		mProgress = (ProgressBar)mRoot.findViewById(R.id.progress);
		mAddContactButton.setOnClickListener(this);
		return mRoot;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mContactList.setAdapter(mAdapter);
		getLoaderManager().initLoader(0, null, this);
	}

	@Override
	public void onClick(View view) {
		
		if(view.getId() == R.id.add_contact_btn){
			
			if(((BeaconApp)getActivity().getApplicationContext()).isTablet()){
				
			} else {
				mActivity.showPhoneContactListFragment();
			}
			
		}
		
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new CursorLoader(getActivity(),
				Beacon.BeaconMobileContactDetails.CONTENT_URI,
				Beacon.BeaconMobileContactDetails.sProjection,
				null,
				null,
				Beacon.BeaconMobileContactDetails.DEFAULT_SORT_ORDER);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		
		if(data != null && data.getCount() > 0){
			mAdapter.swapCursor(data);
			mProgress.setVisibility(View.GONE);
			mContactList.setVisibility(View.VISIBLE);
			mEmptyView.setVisibility(View.GONE);
		} else {
			mProgress.setVisibility(View.GONE);
			mEmptyView.setVisibility(View.VISIBLE);
			mContactList.setVisibility(View.GONE);
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mAdapter.swapCursor(null);
	}

}
