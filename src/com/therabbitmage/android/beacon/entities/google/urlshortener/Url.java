package com.therabbitmage.android.beacon.entities.google.urlshortener;

import com.google.gson.Gson;

public class Url {
	private String longUrl;
	
	public Url(String longUrl){
		this.longUrl = longUrl;
	}
	
	public String getLongUrl(){
		return longUrl;
	}
	
	public String toJson(){
		return new Gson().toJson(this);
	}

}
