package com.therabbitmage.android.beacon.network;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.therabbitmage.android.beacon.network.request.BaseRequest;
import com.therabbitmage.android.beacon.network.response.BaseResponse;

public class AndroidHttpClient extends BaseHttpClient {
	
	private static final String TAG = AndroidHttpClient.class.getSimpleName();
	private int mSocketTimeout = -1;
	private int mConnectionTimeout = -1;
	private HttpURLConnection mConn;
	
	public static BaseHttpClient getInstance(){
		return getInstance(false);
	}
	
	public static BaseHttpClient getInstance(boolean force){
		
		if(sClient == null || force){
			sClient = new AndroidHttpClient();
		}
		
		return sClient;
		
	}
	
	private AndroidHttpClient(){
		
	}
	
	@Override
	public void setSocketTimeout(int timeout) {
		
		if(timeout < 0){
			mSocketTimeout = 0;
			return;
		}
		
		mSocketTimeout = timeout;
	}

	@Override
	public void setConnectionTimeout(int timeout) {
		
		if(timeout < 0){
			mConnectionTimeout = 0;
			return;
		}
		
		mConnectionTimeout = timeout;
	}
	
	@Override
	public int getSocketTimeout() {
		return mSocketTimeout;
	}

	@Override
	public int getConnectionTimeout() {
		return mConnectionTimeout;
	}

	@Override
	public <T> BaseResponse<T> get(BaseRequest<T> baseRequest) {
		
		if(baseRequest == null){
			return handleNullMethodParameter(BaseRequest.class.getSimpleName(), new BaseResponse<T>());
		}
		
		try {
			URL theUrl = new URL(baseRequest.getUrl());
			mConn = (HttpURLConnection)theUrl.openConnection();
			if(mConnectionTimeout >= 0){
				mConn.setConnectTimeout(mConnectionTimeout);
			}
			
			if(mSocketTimeout >= 0){
				mConn.setReadTimeout(mSocketTimeout);
			}
			
			if(baseRequest.getHeaders() != null){
				
				Set<Entry<String, String>> entry = baseRequest.getHeaders().entrySet();
				
				for(Entry<String, String> e: entry){
					Log.i(TAG, "Request Header: " + e.getKey() + "=" + e.getValue());
					mConn.setRequestProperty(e.getKey(), e.getValue());
				}
			}
			
			Log.i(TAG, "Request Header: " + HEADER_USER_AGENT + "=" + sUserAgent);
			mConn.setRequestProperty(HEADER_USER_AGENT, sUserAgent);
			mConn.connect();
			//TODO Get Response code and response message
			
			BufferedReader in = new BufferedReader(new InputStreamReader(mConn.getInputStream()));
			StringBuilder serverBody = new StringBuilder();
			String line = new String();
			while((line = in.readLine()) != null){
				serverBody.append(line);
			}
			in.close();
			release();
			
		} catch(MalformedURLException e){
			Log.e(TAG, e.toString());
			handleException(e, baseRequest.getResponse());
		} catch (IOException e) {
			Log.e(TAG, e.toString());
			handleException(e, baseRequest.getResponse());
		}
		
		return null;
	}

	@Override
	public <T> BaseResponse<T> post(BaseRequest<T> baseRequest) {
		
		if(baseRequest == null){
			return handleNullMethodParameter(BaseRequest.class.getSimpleName(), new BaseResponse<T>());
		}
		
		try {
			URL theUrl = new URL(baseRequest.getUrl());
			mConn = (HttpURLConnection)theUrl.openConnection();
			if(mConnectionTimeout >= 0){
				mConn.setConnectTimeout(mConnectionTimeout);
			}
			
			if(mSocketTimeout >= 0){
				mConn.setReadTimeout(mSocketTimeout);
			}
			
			if(baseRequest.getHeaders() != null){
				
				Set<Entry<String, String>> entry = baseRequest.getHeaders().entrySet();
				
				for(Entry<String, String> e: entry){
					mConn.setRequestProperty(e.getKey(), e.getValue());
					Log.i(TAG, "Request Header: " + e.getKey() + "=" + e.getValue());
				}
				
			}
			
			if(baseRequest.getBodyType() != null){
				mConn.setRequestProperty(BaseHttpClient.HEADER_CONTENT_TYPE, baseRequest.getBodyType());
			}
			
			mConn.setRequestProperty(HEADER_USER_AGENT, sUserAgent);
			Log.i(TAG, "Request Header: " + HEADER_USER_AGENT + "=" + sUserAgent);
			
			mConn.setDoOutput(true);
			mConn.connect();
			int responseCode = mConn.getResponseCode();
			String responseMessage = mConn.getResponseMessage();
			
			Log.i(TAG, "HTTP Status Code = " + responseCode);
			Log.i(TAG, "Response Message = " + responseMessage);
			Map<String, List<String>> responseHeaders = mConn.getHeaderFields();
			int contentLength = 0;
			
			if(responseHeaders != null){
				
				Set<Entry<String,List<String>>> entry = responseHeaders.entrySet();
				
				for(Entry<String, List<String>> e : entry){
					StringBuilder builder = new StringBuilder();
					
					List<String> list = responseHeaders.get(e.getKey());
					builder.append("Response header: " + e.getKey() + " =");
					
					for(String s : list){
						builder.append(" " + e.getValue());
					}
					builder.append("\n");
					Log.i(TAG, builder.toString());
				}
				
			}
			
			DataOutputStream dos = (DataOutputStream) mConn.getOutputStream();
			dos.write(baseRequest.getBody());
			dos.flush();
			dos.close();
			BufferedReader in = new BufferedReader(new InputStreamReader(mConn.getInputStream()));
			StringBuilder serverBody = new StringBuilder();
			String line = new String();
			while((line = in.readLine()) != null){
				serverBody.append(line);
			}
			in.close();
			release();
			
		} catch(MalformedURLException e){
			Log.e(TAG, e.toString());
			handleException(e, baseRequest.getResponse());
		} catch (IOException e) {
			Log.e(TAG, e.toString());
			handleException(e, baseRequest.getResponse());
		}
		
		return null;
	}
	
	@Override
	public void release() {
		mConn.disconnect();
		mConn = null;
	}
	
	public void enableHttpResponseCache(Context ctx) {
		
		if(ctx == null){
			return;
		}
		
	    try {
	        long httpCacheSize = 10 * 1024 * 1024; // 10 MiB
	        File httpCacheDir = new File(ctx.getApplicationContext().getCacheDir(), "http");
	        Class.forName("android.net.http.HttpResponseCache")
	            .getMethod("install", File.class, long.class)
	            .invoke(null, httpCacheDir, httpCacheSize);
	    } catch (Exception httpResponseCacheNotAvailable) {
	    	Log.e(TAG, httpResponseCacheNotAvailable.toString());
	    }
	}
	
	//http://android-developers.blogspot.com/2011/09/androids-http-clients.html	
	@SuppressWarnings({ "unused", "deprecation" })
	private final static void disableConnectionReuseIfNecessary() {
		// Work around pre-Froyo bugs in HTTP connection reuse.
		if (Integer.parseInt(Build.VERSION.SDK) < Build.VERSION_CODES.FROYO){
			System.setProperty("http.keepAlive", "false");
		}
	}

}
