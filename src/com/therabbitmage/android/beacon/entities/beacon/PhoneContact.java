package com.therabbitmage.android.beacon.entities.beacon;

public class PhoneContact {
	private int mId;
	private String mDisplay_name;
	private String mNumber;
	private String mType;
	private String mLabel;
	
	public PhoneContact(){
		
	}

	public int getId() {
		return mId;
	}

	public void setId(int mId) {
		this.mId = mId;
	}

	public String getDisplayName() {
		return mDisplay_name;
	}

	public void setDisplayName(String mDisplay_name) {
		this.mDisplay_name = mDisplay_name;
	}

	public String getNumber() {
		return mNumber;
	}

	public void setNumber(String mNumber) {
		this.mNumber = mNumber;
	}

	public String getType() {
		return mType;
	}

	public void setType(String mType) {
		this.mType = mType;
	}

	public String getLabel() {
		return mLabel;
	}

	public void setLabel(String mLabel) {
		this.mLabel = mLabel;
	}

}
