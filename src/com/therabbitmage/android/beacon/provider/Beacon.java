package com.therabbitmage.android.beacon.provider;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.ContactsContract.Contacts;

import com.therabbitmage.android.beacon.utils.AndroidUtils;

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
	
	public static class PhoneContacts{
		
		//Taken from ContactsList Sample Google Project
		
		 // The selection clause for the CursorLoader query. The search criteria defined here
        // restrict results to contacts that have a display name and are linked to visible groups.
        // Notice that the search on the string provided by the user is implemented by appending
        // the search string to CONTENT_FILTER_URI.
        @SuppressLint("InlinedApi")
        public final static String SELECTION =
                (AndroidUtils.honeycombOrBetter() ? Contacts.DISPLAY_NAME_PRIMARY : Contacts.DISPLAY_NAME) +
                "<>''" + " AND " + Contacts.IN_VISIBLE_GROUP + "=1";

        // The desired sort order for the returned Cursor. In Android 3.0 and later, the primary
        // sort key allows for localization. In earlier versions. use the display name as the sort
        // key.
        @SuppressLint("InlinedApi")
        public final static String SORT_ORDER =
        		AndroidUtils.honeycombOrBetter() ? Contacts.SORT_KEY_PRIMARY : Contacts.DISPLAY_NAME;

        // The projection for the CursorLoader query. This is a list of columns that the Contacts
        // Provider should return in the Cursor.
        @SuppressLint("InlinedApi")
        public final static String[] PROJECTION = {

                // The contact's row id
                Contacts._ID,

                // A pointer to the contact that is guaranteed to be more permanent than _ID. Given
                // a contact's current _ID value and LOOKUP_KEY, the Contacts Provider can generate
                // a "permanent" contact URI.
                Contacts.LOOKUP_KEY,

                // In platform version 3.0 and later, the Contacts table contains
                // DISPLAY_NAME_PRIMARY, which either contains the contact's displayable name or
                // some other useful identifier such as an email address. This column isn't
                // available in earlier versions of Android, so you must use Contacts.DISPLAY_NAME
                // instead.
                AndroidUtils.honeycombOrBetter() ? Contacts.DISPLAY_NAME_PRIMARY : Contacts.DISPLAY_NAME,

                // In Android 3.0 and later, the thumbnail image is pointed to by
                // PHOTO_THUMBNAIL_URI. In earlier versions, there is no direct pointer; instead,
                // you generate the pointer from the contact's ID value and constants defined in
                // android.provider.ContactsContract.Contacts.
                AndroidUtils.honeycombOrBetter() ? Contacts.PHOTO_THUMBNAIL_URI : Contacts._ID,

                // The sort order column for the returned Cursor, used by the AlphabetIndexer
                SORT_ORDER,
        };
		
		private PhoneContacts(){}
	}

	private Beacon() {
	}

}
