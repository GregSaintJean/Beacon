package com.therabbitmage.android.beacon.provider;

import java.util.HashMap;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class BeaconProvider extends ContentProvider {
	private static final String TAG = BeaconProvider.class.getSimpleName();
	
	private static final String DATABASE_NAME = "beacon.db";
	private static final int DATABASE_VERSION = 1;
	
	private static final int Beacon_Contact_List = 0;
	private static final int Beacon_Contact_Detail_List = 1;
	
	private static final int Beacon_Contact_ID = 3;
	private static final int Beacon_Contact_Detail_ID = 4;
	
	private static HashMap<String, String> sBeaconContactProjectionMap;
	private static HashMap<String, String> sBeaconContactDetailsProjectionMap;
	
	private SQLiteOpenHelper mOpenHelper;
	private static final UriMatcher sUriMatcher;

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		
		int count;
		String segment;
		
		switch(sUriMatcher.match(uri)){
			case Beacon_Contact_List:
				count = db.delete(Beacon.BeaconContacts.TABLE_NAME, selection, selectionArgs);
				break;
			case Beacon_Contact_Detail_List:
				count = db.delete(Beacon.BeaconContactDetails.TABLE_NAME, selection, selectionArgs);
				break;
			case Beacon_Contact_ID:
				segment = uri.getPathSegments().get(1);
				count = db.delete(Beacon.BeaconContacts.TABLE_NAME, Beacon.BeaconContacts._ID+"="+segment+
						(!TextUtils.isEmpty(selection) ? " AND (" + selection + ")" : ""), 
						selectionArgs);
				break;
			case Beacon_Contact_Detail_ID:
				segment = uri.getPathSegments().get(1);
				count = db.delete(Beacon.BeaconContactDetails.TABLE_NAME, Beacon.BeaconContactDetails._ID+"="+segment+
						(!TextUtils.isEmpty(selection) ? " AND (" + selection + ")" : ""), 
						selectionArgs);
				break;
			default:
				throw new IllegalArgumentException("Unknown URI:" + uri);
		}
		
		getContext().getContentResolver().notifyChange(uri, null, false);
		
		return count;
	}

	@Override
	public String getType(Uri uri) {
		
		switch(sUriMatcher.match(uri)){
			case Beacon_Contact_List:
				return Beacon.BeaconContacts.CONTENT_TYPE;
			case Beacon_Contact_Detail_List:
				return Beacon.BeaconContactDetails.CONTENT_TYPE;
			case Beacon_Contact_ID:
				return Beacon.BeaconContacts.CONTENT_ITEM_TYPE;
			case Beacon_Contact_Detail_ID:
				return Beacon.BeaconContactDetails.CONTENT_ITEM_TYPE;
			default:
				throw new IllegalArgumentException("Unknown URI:" + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues initialValues) {
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		
		final long rowId;
		
		switch(sUriMatcher.match(uri)){
			case Beacon_Contact_List:
				rowId = db.insert(Beacon.BeaconContacts.TABLE_NAME, null, initialValues);
				if(rowId >= 0){
					Uri insertUri = ContentUris.withAppendedId(Beacon.BeaconContacts.CONTENT_URI, rowId);
                    getContext().getContentResolver().notifyChange(uri, null, false);
                    return insertUri;
				}
				break;
			case Beacon_Contact_Detail_List:
				rowId = db.insert(Beacon.BeaconContactDetails.TABLE_NAME, null, initialValues);
				if(rowId >= 0){
					Uri insertUri = ContentUris.withAppendedId(Beacon.BeaconContactDetails.CONTENT_URI, rowId);
                    getContext().getContentResolver().notifyChange(uri, null, false);
                    return insertUri;
				}
				break;
			default:
                throw new IllegalArgumentException("Unknown URI:" + uri);
		}
		
		throw new SQLException("Failed to insert row into " + uri);
	}

	@Override
	public boolean onCreate() {
		mOpenHelper = new DatabaseHelper(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        String orderBy = null;
        switch(sUriMatcher.match(uri)){
        	case Beacon_Contact_List:
        		qb.setTables(Beacon.BeaconContacts.TABLE_NAME);
        		qb.setProjectionMap(sBeaconContactProjectionMap);
        		orderBy = deriveSortOrder(sortOrder, Beacon.BeaconContacts.DEFAULT_SORT_ORDER);
        		break;
        	case Beacon_Contact_Detail_List:
        		qb.setTables(Beacon.BeaconContactDetails.TABLE_NAME);
        		qb.setProjectionMap(sBeaconContactDetailsProjectionMap);
        		orderBy = deriveSortOrder(sortOrder, Beacon.BeaconContactDetails.DEFAULT_SORT_ORDER);
        		break;
        	case Beacon_Contact_ID:
        		qb.setTables(Beacon.BeaconContacts.TABLE_NAME);
        		qb.setProjectionMap(sBeaconContactProjectionMap);
        		qb.appendWhere(Beacon.BeaconContacts._ID+"="+uri.getPathSegments().get(1));
        		orderBy = deriveSortOrder(sortOrder, Beacon.BeaconContacts.DEFAULT_SORT_ORDER);
        		break;
        	case Beacon_Contact_Detail_ID:
        		qb.setTables(Beacon.BeaconContactDetails.TABLE_NAME);
        		qb.setProjectionMap(sBeaconContactDetailsProjectionMap);
        		qb.appendWhere(Beacon.BeaconContactDetails._ID+"="+uri.getPathSegments().get(1));
        		orderBy = deriveSortOrder(sortOrder, Beacon.BeaconContactDetails.DEFAULT_SORT_ORDER);
        		break;
	        default:
	            throw new IllegalArgumentException("Unknown URI:" + uri);
        }
        
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, orderBy);
        c.setNotificationUri(getContext().getContentResolver(), uri);
		return c;
	}
	
	private String deriveSortOrder(String sortOrder, String defaultSortOrder){
    	
    	if(TextUtils.isEmpty(sortOrder))
    		return  defaultSortOrder;
    	else
    		return sortOrder;
    }

	@Override
	public int update(Uri uri, ContentValues initialValues, String selection,
			String[] selectionArgs) {
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		
		int count;
        String segment;
        
        switch(sUriMatcher.match(uri)){
        	case Beacon_Contact_List:
        		count = db.update(Beacon.BeaconContacts.TABLE_NAME, initialValues, selection, selectionArgs);
        	case Beacon_Contact_Detail_List:
        		count = db.update(Beacon.BeaconContactDetails.TABLE_NAME, initialValues, selection, selectionArgs);
        	case Beacon_Contact_ID:
        		segment = uri.getPathSegments().get(1);
        		count = db.update(Beacon.BeaconContacts.TABLE_NAME, initialValues, 
        				Beacon.BeaconContacts._ID+"="+segment+(!TextUtils.isEmpty(selection) ? " AND ("+ selection + ")" : ""),
        				selectionArgs);
        		break;
        	case Beacon_Contact_Detail_ID:
        		segment = uri.getPathSegments().get(1);
        		count = db.update(Beacon.BeaconContactDetails.TABLE_NAME, initialValues, 
        				Beacon.BeaconContactDetails._ID+"="+segment+(!TextUtils.isEmpty(selection) ? " AND ("+ selection + ")" : ""), 
        				selectionArgs);
        		break;
	        default:
	            throw new IllegalArgumentException("Unknown URI:" + uri);
        }
		
		getContext().getContentResolver().notifyChange(uri, null, false);
        return count;
	}
	
	private static class DatabaseHelper extends SQLiteOpenHelper{
		DatabaseHelper(Context ctx){
            super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
        }

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(Beacon.BeaconContacts.SQL_CREATE_TABLE);
			db.execSQL(Beacon.BeaconContactDetails.SQL_CREATE_TABLE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
			db.execSQL(Beacon.BeaconContacts.SQL_DROP_TABLE);
			db.execSQL(Beacon.BeaconContactDetails.SQL_DROP_TABLE);
		}
	}
	
	static {
		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		sUriMatcher.addURI(Beacon.AUTHORITY, Beacon.BeaconContacts.TABLE_NAME, Beacon_Contact_List);
		sUriMatcher.addURI(Beacon.AUTHORITY, Beacon.BeaconContactDetails.TABLE_NAME, Beacon_Contact_Detail_List);
		
		sUriMatcher.addURI(Beacon.AUTHORITY, Beacon.BeaconContacts.TABLE_NAME + "/#", Beacon_Contact_ID);
		sUriMatcher.addURI(Beacon.AUTHORITY, Beacon.BeaconContactDetails.TABLE_NAME + "/#", Beacon_Contact_Detail_ID);
		
		sBeaconContactProjectionMap = new HashMap<String, String>();
		sBeaconContactProjectionMap.put(Beacon.BeaconContacts._ID, Beacon.BeaconContacts._ID);
		sBeaconContactProjectionMap.put(Beacon.BeaconContacts.CN_FIRST_NAME, Beacon.BeaconContacts.CN_FIRST_NAME);
		sBeaconContactProjectionMap.put(Beacon.BeaconContacts.CN_LAST_NAME, Beacon.BeaconContacts.CN_LAST_NAME);
		
		sBeaconContactDetailsProjectionMap = new HashMap<String, String>();
		sBeaconContactDetailsProjectionMap.put(Beacon.BeaconContactDetails._ID, Beacon.BeaconContactDetails._ID);
		sBeaconContactDetailsProjectionMap.put(Beacon.BeaconContactDetails.CN_CONTACT_ID, Beacon.BeaconContactDetails.CN_CONTACT_ID);
		sBeaconContactDetailsProjectionMap.put(Beacon.BeaconContactDetails.CN_TYPE, Beacon.BeaconContactDetails.CN_TYPE);
		sBeaconContactDetailsProjectionMap.put(Beacon.BeaconContactDetails.CN_NUMBER, Beacon.BeaconContactDetails.CN_NUMBER);
	}

}
