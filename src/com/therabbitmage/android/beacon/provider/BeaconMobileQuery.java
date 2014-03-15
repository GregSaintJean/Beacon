package com.therabbitmage.android.beacon.provider;

import android.net.Uri;

import com.therabbitmage.android.beacon.provider.Beacon.BeaconMobileContactDetails;

public interface BeaconMobileQuery {
	
	final static int BEACON_QUERY_ID = 0;
	
	final static Uri CONTENT_URI = BeaconMobileContactDetails.CONTENT_URI;
	
	final static String[] PROJECTION = BeaconMobileContactDetails.sProjection;
	
	final static String SORT_ORDER = BeaconMobileContactDetails.DEFAULT_SORT_ORDER;

}
