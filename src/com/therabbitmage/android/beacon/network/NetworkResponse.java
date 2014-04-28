package com.therabbitmage.android.beacon.network;

import com.therabbitmage.android.beacon.entities.google.urlshortener.Url;

public class NetworkResponse {
	
	private Url mUrl;
	private int mError;
	
	public Url getUrl() {
		return mUrl;
	}
	public void setUrl(Url mUrl) {
		this.mUrl = mUrl;
	}
	public int getError() {
		return mError;
	}
	public void setError(int mError) {
		this.mError = mError;
	}

}
