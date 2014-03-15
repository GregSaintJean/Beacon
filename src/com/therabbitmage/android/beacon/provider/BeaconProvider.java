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
	
	private static final int Beacon_Contact_Detail_List = 0;
	private static final int Beacon_Contact_Detail_ID = 1;
	
	public static HashMap<String, String> sBeaconContactDetailsProjectionMap;
	
	private SQLiteOpenHelper mOpenHelper;
	private static final UriMatcher sUriMatcher;

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		
		int count;
		String segment;
		
		switch(sUriMatcher.match(uri)){
			case Beacon_Contact_Detail_List:
				count = db.delete(Beacon.BeaconMobileContactDetails.TABLE_NAME, selection, selectionArgs);
				break;
			case Beacon_Contact_Detail_ID:
				segment = uri.getPathSegments().get(1);
				count = db.delete(Beacon.BeaconMobileContactDetails.TABLE_NAME, Beacon.BeaconMobileContactDetails._ID+"="+segment+
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
			case Beacon_Contact_Detail_List:
				return Beacon.BeaconMobileContactDetails.CONTENT_TYPE;
			case Beacon_Contact_Detail_ID:
				return Beacon.BeaconMobileContactDetails.CONTENT_ITEM_TYPE;
			default:
				throw new IllegalArgumentException("Unknown URI:" + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues initialValues) {
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		
		final long rowId;
		
		switch(sUriMatcher.match(uri)){
			case Beacon_Contact_Detail_List:
				rowId = db.insert(Beacon.BeaconMobileContactDetails.TABLE_NAME, null, initialValues);
				if(rowId >= 0){
					Uri insertUri = ContentUris.withAppendedId(Beacon.BeaconMobileContactDetails.CONTENT_URI, rowId);
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
        	case Beacon_Contact_Detail_List:
        		qb.setTables(Beacon.BeaconMobileContactDetails.TABLE_NAME);
        		qb.setProjectionMap(sBeaconContactDetailsProjectionMap);
        		orderBy = deriveSortOrder(sortOrder, Beacon.BeaconMobileContactDetails.DEFAULT_SORT_ORDER);
        		break;
        	case Beacon_Contact_Detail_ID:
        		qb.setTables(Beacon.BeaconMobileContactDetails.TABLE_NAME);
        		qb.setProjectionMap(sBeaconContactDetailsProjectionMap);
        		qb.appendWhere(Beacon.BeaconMobileContactDetails._ID+"="+uri.getPathSegments().get(1));
        		orderBy = deriveSortOrder(sortOrder, Beacon.BeaconMobileContactDetails.DEFAULT_SORT_ORDER);
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
        	case Beacon_Contact_Detail_List:
        		count = db.update(Beacon.BeaconMobileContactDetails.TABLE_NAME, initialValues, selection, selectionArgs);
        		break;
        	case Beacon_Contact_Detail_ID:
        		segment = uri.getPathSegments().get(1);
        		count = db.update(Beacon.BeaconMobileContactDetails.TABLE_NAME, initialValues, 
        				Beacon.BeaconMobileContactDetails._ID+"="+segment+(!TextUtils.isEmpty(selection) ? " AND ("+ selection + ")" : ""), 
        				selectionArgs);
        		break;
	        default:
	            throw new IllegalArgumentException("Unknown URI:" + uri);
        }
		
		getContext().getContentResolver().notifyChange(uri, null, false);
        return count;
	}
	
	public static class DatabaseHelper extends SQLiteOpenHelper{
		DatabaseHelper(Context ctx){
            super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
        }

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(Beacon.BeaconMobileContactDetails.SQL_CREATE_TABLE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
			db.execSQL(Beacon.BeaconMobileContactDetails.SQL_DROP_TABLE);
		}
	}
	
	static {
		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		sUriMatcher.addURI(Beacon.AUTHORITY, Beacon.BeaconMobileContactDetails.TABLE_NAME, Beacon_Contact_Detail_List);
		sUriMatcher.addURI(Beacon.AUTHORITY, Beacon.BeaconMobileContactDetails.TABLE_NAME + "/#", Beacon_Contact_Detail_ID);
		
		sBeaconContactDetailsProjectionMap = new HashMap<String, String>();
		sBeaconContactDetailsProjectionMap.put(Beacon.BeaconMobileContactDetails._ID, Beacon.BeaconMobileContactDetails._ID);
		sBeaconContactDetailsProjectionMap.put(Beacon.BeaconMobileContactDetails.CN_CONTACT_ID, Beacon.BeaconMobileContactDetails.CN_CONTACT_ID);
		sBeaconContactDetailsProjectionMap.put(Beacon.BeaconMobileContactDetails.CN_DISPLAY_NAME, Beacon.BeaconMobileContactDetails.CN_DISPLAY_NAME);
		sBeaconContactDetailsProjectionMap.put(Beacon.BeaconMobileContactDetails.CN_DELETE_FLAG, Beacon.BeaconMobileContactDetails.CN_DELETE_FLAG);
		sBeaconContactDetailsProjectionMap.put(Beacon.BeaconMobileContactDetails.CN_NUMBER, Beacon.BeaconMobileContactDetails.CN_NUMBER);
	}

}
