package com.therabbitmage.android.beacon.network.request;

import java.util.Map;

import android.support.v4.util.ArrayMap;

public abstract class BaseRequest {
	
	public static final int METHOD_GET = 656;
	public static final int METHOD_POST = 782;
	
	private String mUrl;
	private int mMethod;
	private Map<String, String> mParameters;
	private Map<String, String> mHeaders;
	
	public BaseRequest(String url){
		this(url, METHOD_GET);
	}
	
	public BaseRequest(String url, int method){
		this(url, METHOD_GET, new ArrayMap<String, String>());
	}
	
	public BaseRequest(String url, int method, Map<String, String> headers){
		setUrl(url);
		setMethod(method);
		setParameters(new ArrayMap<String, String>());
		setHeaders(headers);
	}

	public String getUrl() {
		return mUrl;
	}

	public void setUrl(String mUrl) {
		this.mUrl = mUrl;
	}

	public int getMethod() {
		return mMethod;
	}

	public void setMethod(int method) {
		
		switch(method){
			case METHOD_POST:
				mMethod = METHOD_POST;
				return;
			case METHOD_GET:
			default:
				mMethod = METHOD_GET;
				return;
		}
	}

	public Map<String, String> getParameters() {
		return mParameters;
	}

	public void setParameters(Map<String, String> parameters) {
		if(parameters == null){
			this.mParameters = new ArrayMap<String, String>();
		} else {
			this.mParameters = parameters;
		}
		
	}
	
	public void addParameter(String key, String value){
		if(key == null
				|| key.length() == 0){
			return;
		}
		
		if(value == null
				|| value.length() == 0){
			return;
		}
		mParameters.put(key, value);
	}
	
	public String getParameter(String key){
		return mParameters.get(key);
	}
	
	public boolean hasParameter(String key){
		return mParameters.containsKey(key);
	}

	public Map<String, String> getHeaders() {
		return mHeaders;
	}
	
	public String getHeader(String key){
		return mHeaders.get(key);
	}
	
	public boolean hasHeader(String key){
		return mHeaders.containsKey(key);
	}

	public void setHeaders(Map<String, String> headers) {
		
		if(headers == null){
			this.mHeaders = new ArrayMap<String, String>();
		} else {
			this.mHeaders = headers;
		}
		
		
	}
	
	public void addHeader(String key, String value){
		if(key == null
				|| key.length() == 0){
			return;
		}
		
		if(value == null
				|| value.length() == 0){
			return;
		}
		mHeaders.put(key, value);
	}

}
