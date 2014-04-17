package com.therabbitmage.android.beacon.provider;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class BeaconManager {
	private static final String TAG = BeaconManager.class.getSimpleName();
	
	private ContentResolver mResolver;
	
	public BeaconManager(Context ctx){
		mResolver =  ctx.getContentResolver();
	}
	
	/**
	 * Adds a phone record from the android contacts database to the Beacon phone database
	 * @param name The display name of the contact
	 * @param contactid the id of the contact in the android database
	 * @param number the phone number of the contact
	 * @return the id of the row added in the Beacon phone database. -1 if nothing was added (because it already exists)
	 */
	public int addPhoneContact(int contactId, String name, String number){
		String selection = Beacon.BeaconMobileContactDetails.CN_CONTACT_ID + "=?";
		String[] selectionArgs = new String[]{""+contactId};
		
		Cursor c = mResolver.query(
				Beacon.BeaconMobileContactDetails.CONTENT_URI,
				Beacon.BeaconMobileContactDetails.sProjection,
				selection, 
				selectionArgs, 
				Beacon.BeaconMobileContactDetails.DEFAULT_SORT_ORDER);
		
		if(c == null || c.getCount() <= 0){
			
			ContentValues values = new ContentValues();
			values.put(Beacon.BeaconMobileContactDetails.CN_CONTACT_ID, contactId);
			values.put(Beacon.BeaconMobileContactDetails.CN_DISPLAY_NAME, name);
			values.put(Beacon.BeaconMobileContactDetails.CN_DELETE_FLAG, 0);
			values.put(Beacon.BeaconMobileContactDetails.CN_NUMBER, number);
			Uri addedUri = mResolver.insert(Beacon.BeaconMobileContactDetails.CONTENT_URI, values);
			Log.d(TAG, "Row URI BeaconMobileContactDetails= " + addedUri);
			return Integer.parseInt(addedUri.getPathSegments().get(addedUri.getPathSegments().size()-1));
		} else {
			if(c != null){
				c.close();
			}
			Log.d(TAG, "Contact already retained");
			return -1;
		}
		
		
	}
	
	/**
	 * Removes phone contact data from Beacon phone database.
	 * @param beaconDetailid the id that corresponds to the phone contact record
	 */
	
	public void removePhoneContactByContactId(int phoneContactId){
		
		String selection = Beacon.BeaconMobileContactDetails.CN_CONTACT_ID + "=?";
		String[] selectionArgs = new String[]{""+ phoneContactId};
		
		Cursor c = mResolver.query(
				Beacon.BeaconMobileContactDetails.CONTENT_URI,
				Beacon.BeaconMobileContactDetails.sProjection,
				selection, 
				selectionArgs, 
				Beacon.BeaconMobileContactDetails.DEFAULT_SORT_ORDER);
		
		if(c != null && c.getCount() > 0 && c.moveToFirst()){
			int rows = mResolver.delete(
					Beacon.BeaconMobileContactDetails.CONTENT_URI, 
					selection, 
					selectionArgs);
			c.close();
			Log.d(TAG, "Number of rows deleted =" + rows);
		} else {
			Log.d(TAG, "Beacon contact detail doesn't exist");
		}
	}
	
	public void removePhoneContactByBeaconId(int beaconId){
		String selection = Beacon.BeaconMobileContactDetails._ID + "=?";
		String[] selectionArgs = new String[]{"" + beaconId};
		
		Cursor c = mResolver.query(
				Beacon.BeaconMobileContactDetails.CONTENT_URI,
				Beacon.BeaconMobileContactDetails.sProjection,
				selection, 
				selectionArgs, 
				Beacon.BeaconMobileContactDetails.DEFAULT_SORT_ORDER);
		
		if(c != null && c.getCount() > 0 && c.moveToFirst()){
			int rows = mResolver.delete(Beacon.BeaconMobileContactDetails.CONTENT_URI,
					selection, selectionArgs);
			c.close();
			Log.d(TAG, "Number of rows deleted =" + rows);
		} else {
			Log.d(TAG, "Beacon contact detail doesn't exist");
		}
	}
	
}
