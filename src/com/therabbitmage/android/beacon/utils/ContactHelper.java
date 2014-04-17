package com.therabbitmage.android.beacon.utils;

import java.util.ArrayList;

import android.database.Cursor;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Data;

import com.therabbitmage.android.beacon.entities.beacon.BeaconSMSContact;
import com.therabbitmage.android.beacon.entities.beacon.PhoneContact;
import com.therabbitmage.android.beacon.provider.Beacon;

public final class ContactHelper {
	
	private static final String DISPLAY_NAME =  AndroidUtils.honeycombOrBetter() ? Data.DISPLAY_NAME_PRIMARY : Data.DISPLAY_NAME;
	
	public static ArrayList<BeaconSMSContact> convertCursorToBeaconContacts(Cursor cursor){
		ArrayList<BeaconSMSContact> list = new ArrayList<BeaconSMSContact>();
		if(cursor == null || cursor.getCount() == 0){
			return list;
		}
		
		if(cursor.moveToFirst()){
			
			do
			{
				BeaconSMSContact c = new BeaconSMSContact();
				c.setContactId(cursor.getInt(cursor.getColumnIndex(Beacon.BeaconMobileContactDetails._ID)));
				c.setDisplayName(cursor.getString(cursor.getColumnIndex(Beacon.BeaconMobileContactDetails.CN_DISPLAY_NAME)));
				c.setNumber(cursor.getString(cursor.getColumnIndex(Beacon.BeaconMobileContactDetails.CN_NUMBER)));
				list.add(c);
			}
			while(cursor.moveToNext());
			
			cursor.close();
			
		}
		
		return list;
	}
	
	public static ArrayList<PhoneContact> convertCursortoPhoneContacts(Cursor cursor){
		ArrayList<PhoneContact> list = new ArrayList<PhoneContact>();
		if(cursor == null || cursor.getCount() == 0){
			return list;
		}
		
		if(cursor.moveToFirst()){
			do
			{
				PhoneContact c = new PhoneContact();
				c.setId(cursor.getInt(cursor.getColumnIndex(Data._ID)));
				c.setDisplayName(cursor.getString(cursor.getColumnIndex(DISPLAY_NAME)));
				c.setLabel(cursor.getString(cursor.getColumnIndex(Phone.LABEL)));
				c.setNumber(cursor.getString(cursor.getColumnIndex(Phone.NUMBER)));
				c.setType(cursor.getString(cursor.getColumnIndex(Phone.TYPE)));
				list.add(c);
			}
			while(cursor.moveToNext());
			
			cursor.close();
		}
		
		return list;
	}

}
