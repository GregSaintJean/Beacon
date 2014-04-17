package com.therabbitmage.android.beacon.entities.beacon;

import android.os.Parcel;
import android.os.Parcelable;

public class BeaconSMSContact implements Parcelable{
	
	private int mContactId;
	private String mDisplayName;
	private String mNumber;
	
	public BeaconSMSContact(){
		
	}
	
	public BeaconSMSContact(int contactId, String name, String number){
		this.setContactId(contactId);
		this.mDisplayName = name;
		this.mNumber = number;
	}

	public int getContactId() {
		return mContactId;
	}

	public void setContactId(int contactId) {
		this.mContactId = contactId;
	}

	public String getNumber() {
		return mNumber;
	}

	public void setNumber(String number) {
		this.mNumber = number;
	}

	public String getDisplayName() {
		return mDisplayName;
	}

	public void setDisplayName(String displayName) {
		this.mDisplayName = displayName;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}
}
