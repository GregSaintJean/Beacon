package com.therabbitmage.android.beacon.entities;

import android.os.Parcel;
import android.os.Parcelable;

public class Contact implements Parcelable{
	
	private String name;
	private String number;
	
	public Contact(){
		
	}
	
	public Contact(String name, String number){
		this.setName(name);
		this.setNumber(number);
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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
