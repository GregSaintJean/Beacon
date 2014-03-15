package com.therabbitmage.android.beacon.provider;

import android.net.Uri;
import android.provider.BaseColumns;

public final class Beacon {

	public final static String AUTHORITY = "com.therabbitmage.android.beacon.provider.BeaconProvider";

	public static class BeaconMobileContactDetails implements BaseColumns {

		public static final String TABLE_NAME = BeaconMobileContactDetails.class
				.getSimpleName();

		public final static Uri CONTENT_URI = Uri.parse("content://"
				+ AUTHORITY + "/" + TABLE_NAME);

		public static final String CN_CONTACT_ID = "contact_id";
		
		public static final String CN_DISPLAY_NAME = "display_name";
		
		public static final String CN_DELETE_FLAG = "delete_flag";

		public static final String CN_NUMBER = "number";

		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.therabbitmage.android.beacon.contact";

		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.therabbitmage.android.beacon.detail";

		public final static String DEFAULT_SORT_ORDER = _ID + " ASC";

		public static final String[] sProjection = { _ID, CN_CONTACT_ID, CN_DISPLAY_NAME, CN_DELETE_FLAG, CN_NUMBER };

		public final static String SQL_CREATE_TABLE_STRUCTURE = "(" 
				+ _ID + " INTEGER" + " PRIMARY KEY" + ", "
				+ CN_CONTACT_ID + " INTEGER" + ", "
				+ CN_DISPLAY_NAME + " TEXT" + ", "
				+ CN_NUMBER + " TEXT" + ", "
				+ CN_DELETE_FLAG + " INTEGER"
				+ ")";

		public final static String SQL_CREATE_TABLE = "CREATE TABLE "
				+ TABLE_NAME + SQL_CREATE_TABLE_STRUCTURE;

		public final static String SQL_DROP_TABLE = "DROP TABLE IF EXISTS "
				+ TABLE_NAME;

		private BeaconMobileContactDetails() {
		}
	}

	private Beacon() {
	}

}
