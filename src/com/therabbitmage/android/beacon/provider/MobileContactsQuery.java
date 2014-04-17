package com.therabbitmage.android.beacon.provider;

import android.annotation.TargetApi;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.CommonDataKinds.Phone;

import com.therabbitmage.android.beacon.utils.AndroidUtils;

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
