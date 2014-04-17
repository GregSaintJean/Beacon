package com.therabbitmage.android.beacon.network;

import java.io.IOException;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.net.http.AndroidHttpClient;
import android.util.Log;

public final class NetworkUtils {
	private static final String TAG = NetworkUtils.class.getSimpleName();
	
	public static final String TYPE_JSON = "application/json";
	public static final String TYPE_FORM_ENCODED = "application/x-www-form-urlencoded";
	
	private static HttpClient mHttpClient;
	
	public static HttpClient getInstance(){
		
		if(mHttpClient == null){
			mHttpClient = new DefaultHttpClient();
		}
		
		return mHttpClient;
		
	}
	
	public static HttpClient getAndroidInstance(String userAgent, boolean force){
		
		if(mHttpClient == null || force){
			mHttpClient = AndroidHttpClient.newInstance(userAgent);
		}
		
		return mHttpClient;
		
	}
	
	public static void release(){
		if(mHttpClient != null){
			mHttpClient.getConnectionManager().shutdown();
			mHttpClient = null;
		}
	}
	
	public static void setSocketTimeout(int timeout){
		getInstance();
		final HttpParams params = mHttpClient.getParams();
		HttpConnectionParams.setSoTimeout(params, timeout);
	}
	
	public static void setConnectionTimeout(int timeout){
		getInstance();
		final HttpParams params = mHttpClient.getParams();
		HttpConnectionParams.setConnectionTimeout(params, timeout);
	}
	
	public static HttpResponse get(String url, Map<String, String> headers) throws ClientProtocolException, IOException{
		//TODO create an object to handle ClientProtocolException and IOException
		final HttpGet httpGet = new HttpGet(url);
		
		if(headers != null && !headers.isEmpty()){
			
			for(String header : headers.keySet()){
				Log.v(TAG, header + ": " + headers.get(header));
				httpGet.setHeader(header, headers.get(header));
			}
			
		}
		
		return mHttpClient.execute(httpGet);
		
	}
	
	public static HttpResponse post(String url, Map<String, String> headers, String data) throws ClientProtocolException, IOException{
		//TODO create an object to handle ClientProtocolException and IOException
		final HttpPost httpPost = new HttpPost(url);
		
		if(headers != null && !headers.isEmpty()){
			
			for(String header : headers.keySet()){
				Log.v(TAG, header + ": " + headers.get(header));
				httpPost.setHeader(header, headers.get(header));
			}
			
		}
		
		StringEntity entity = new StringEntity(data);
		httpPost.setEntity(entity);
		
		return mHttpClient.execute(httpPost);
	}

}
