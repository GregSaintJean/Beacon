package com.therabbitmage.android.beacon.provider;

import android.net.Uri;
import android.provider.BaseColumns;

public final class Beacon {

	public final static String AUTHORITY = "com.therabbitmage.android.beacon.provider";

	public static class BeaconContacts implements BaseColumns {

		public static final String TABLE_NAME = BeaconContacts.class
				.getSimpleName();

		public final static Uri CONTENT_URI = Uri.parse("content://"
				+ AUTHORITY + "/" + TABLE_NAME);

		public static final String CN_FIRST_NAME = "first_name";
		public static final String CN_LAST_NAME = "last_name";

		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.therabbitmage.android.beacon.list";

		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.therabbitmage.android.beacon.contact";

		public static final String DEFAULT_SORT_ORDER = CN_FIRST_NAME + " ASC";

		public static final String[] sProjection = { _ID, CN_FIRST_NAME, CN_LAST_NAME };

		public final static String SQL_CREATE_TABLE_STRUCTURE = "("
				+ _ID + " INTEGER" + " PRIMARY KEY" + ", "
				+ CN_FIRST_NAME + " TEXT"
				+ CN_LAST_NAME + " TEXT"
				+ ")";

		public final static String SQL_CREATE_TABLE = "CREATE TABLE "
				+ TABLE_NAME + SQL_CREATE_TABLE_STRUCTURE;

		public final static String SQL_DROP_TABLE = "DROP TABLE IF EXISTS "
				+ TABLE_NAME;

		private BeaconContacts() {
		}
	}

	public static class BeaconContactDetails implements BaseColumns {

		public static final String TABLE_NAME = BeaconContactDetails.class
				.getSimpleName();

		public final static Uri CONTENT_URI = Uri.parse("content://"
				+ AUTHORITY + "/" + TABLE_NAME);

		public static final String CN_CONTACT_ID = "contact_id";

		public static final String CN_TYPE = "type";

		public static final String CN_NUMBER = "number";

		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.therabbitmage.android.beacon.contact";

		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.therabbitmage.android.beacon.detail";

		public final static String DEFAULT_SORT_ORDER = CN_TYPE + " ASC";

		public static final String[] sProjection = { _ID, CN_CONTACT_ID,
				CN_TYPE, CN_NUMBER };

		public final static String SQL_CREATE_TABLE_STRUCTURE = "(" 
				+ _ID + " INTEGER" + " PRIMARY KEY" + ", " 
				+ CN_CONTACT_ID + " INTEGER" + ","
				+ CN_TYPE + " TEXT" + ","
				+ CN_NUMBER + " TEXT" 
				+ ")";

		public final static String SQL_CREATE_TABLE = "CREATE TABLE "
				+ TABLE_NAME + SQL_CREATE_TABLE_STRUCTURE;

		public final static String SQL_DROP_TABLE = "DROP TABLE IF EXISTS "
				+ TABLE_NAME;

		private BeaconContactDetails() {
		}
	}

	private Beacon() {
	}

}
