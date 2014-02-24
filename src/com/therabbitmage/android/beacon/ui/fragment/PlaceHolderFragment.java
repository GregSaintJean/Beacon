package com.therabbitmage.android.beacon.ui.fragment;

import com.therabbitmage.android.beacon.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

//Might use it for tablet
public class PlaceHolderFragment extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.placeholder_fragment, container, false);
	}

}
