package com.therabbitmage.android.beacon.entities.google.urlshortener;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.Gson;

public class Url implements Parcelable{
	private String kind;
	private String id;
	private String longUrl;
	private String status;
	private String created;
	
	public Url(String longUrl){
		this.longUrl = longUrl;
	}
	
	public Url(Parcel parcel){
		readFromParcel(parcel);
	}
	
	private void readFromParcel(Parcel parcel){
		kind = parcel.readString();
		id = parcel.readString();
		longUrl = parcel.readString();
		status = parcel.readString();
		created = parcel.readString();
	}
	
	public String getKind() {
		return kind;
	}

	public void setKind(String kind) {
		this.kind = kind;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getLongUrl(){
		return longUrl;
	}
	
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getCreated() {
		return created;
	}

	public void setCreated(String created) {
		this.created = created;
	}

	public String toJson(){
		return new Gson().toJson(this);
	}
	
	public static final Url fromJson(String s){
		return new Gson().fromJson(s, Url.class);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(kind);
		dest.writeString(id);
		dest.writeString(longUrl);
		dest.writeString(status);
		dest.writeString(created);
	}
	
	public static final Parcelable.Creator<Url> CREATOR =
			new Parcelable.Creator<Url>() {

				@Override
				public Url createFromParcel(Parcel source) {
					return new Url(source);
				}

				@Override
				public Url[] newArray(int size) {
					return new Url[size];
				}
			};

}

