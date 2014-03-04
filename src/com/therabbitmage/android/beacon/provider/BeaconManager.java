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
	public int addPhoneContact(String name, int contactId, String number){
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
			values.put(Beacon.BeaconContacts.CN_DISPLAY_NAME, name);
			values.put(Beacon.BeaconContacts.CN_DELETE_FLAG, 0);
			values.put(Beacon.BeaconContacts.CN_RECORDS, 1);
			Uri addedUri = mResolver.insert(Beacon.BeaconContacts.CONTENT_URI, values);
			Log.d(TAG, "Row URI BeaconContacts= " + addedUri);
			int id = Integer.parseInt(addedUri.getPathSegments().get(addedUri.getPathSegments().size()-1));
			
			values = new ContentValues();
			values.put(Beacon.BeaconMobileContactDetails.CN_BEACON_ID, id);
			values.put(Beacon.BeaconMobileContactDetails.CN_CONTACT_ID, contactId);
			values.put(Beacon.BeaconMobileContactDetails.CN_DISPLAY_NAME, name);
			values.put(Beacon.BeaconMobileContactDetails.CN_DELETE_FLAG, 0);
			values.put(Beacon.BeaconMobileContactDetails.CN_NUMBER, number);
			addedUri = mResolver.insert(Beacon.BeaconMobileContactDetails.CONTENT_URI, values);
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
	
	public void removePhoneContact(int phoneContactId){
		
		String selection = Beacon.BeaconMobileContactDetails.CN_CONTACT_ID + "=?";
		String[] selectionArgs = new String[]{""+ phoneContactId};
		
		Cursor c = mResolver.query(
				Beacon.BeaconMobileContactDetails.CONTENT_URI,
				Beacon.BeaconMobileContactDetails.sProjection,
				selection, 
				selectionArgs, 
				Beacon.BeaconMobileContactDetails.DEFAULT_SORT_ORDER);
		
		if(c != null && c.getCount() > 0 && c.moveToFirst()){
			Log.d(TAG, "Cursor count = " + c.getCount());
			int beaconid = c.getInt(c.getColumnIndex(Beacon.BeaconMobileContactDetails.CN_BEACON_ID));
			String beaconSelection = Beacon.BeaconContacts._ID + "=?";
			String[] beaconSelectionArgs = new String[]{"" + beaconid};
			
			Cursor beaconCursor = mResolver.query(
					Beacon.BeaconContacts.CONTENT_URI,
					Beacon.BeaconContacts.sProjection,
					beaconSelection, 
					beaconSelectionArgs, 
					Beacon.BeaconContacts.DEFAULT_SORT_ORDER);
			
			if(beaconCursor != null && beaconCursor.getCount() > 0 && beaconCursor.moveToFirst()){
				int recordCount = beaconCursor.getInt(beaconCursor.getColumnIndex(Beacon.BeaconContacts.CN_RECORDS));
				String beaconChangeSelection = Beacon.BeaconContacts._ID + "=?";
				String[] beaconChangeSelectionArgs = new String[]{"" + beaconid};
				if(recordCount == 1){
					int rowsDeleted = mResolver.delete(Beacon.BeaconContacts.CONTENT_URI, beaconChangeSelection, beaconChangeSelectionArgs);
					Log.d(TAG, "Number of rows deleted = " + rowsDeleted);
				} else if(recordCount > 1){
					ContentValues values = new ContentValues();
					values.put(Beacon.BeaconContacts.CN_RECORDS, recordCount-1);
					int rowsUpdated = mResolver.update(
							Beacon.BeaconContacts.CONTENT_URI,
							values,
							beaconChangeSelection,
							beaconChangeSelectionArgs);
					Log.d(TAG, "Number of rows updated = " + rowsUpdated);
				}
				beaconCursor.close();
			}
			
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
}
