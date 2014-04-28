package com.therabbitmage.android.beacon.network;

import java.io.IOException;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.HttpConnectionParams;

import android.util.Log;

import com.therabbitmage.android.beacon.network.request.BaseRequest;
import com.therabbitmage.android.beacon.network.response.BaseResponse;
import com.therabbitmage.android.beacon.network.response.BaseResponse.Error;

public class ApacheHttpClient extends BaseHttpClient{
	
	private static final String TAG = ApacheHttpClient.class.getSimpleName();
	
	private HttpClient mHttpClient;
	
	public static BaseHttpClient getInstance(){
		return getInstance(false);
	}
	
	public static BaseHttpClient getInstance(boolean force){
		
		if(sClient == null || force){
			sClient = new ApacheHttpClient();
		}
		
		return sClient;
	}
	
	private ApacheHttpClient(){
		mHttpClient = new DefaultHttpClient();
	}

	@Override
	public void setSocketTimeout(int timeout) {
		if(timeout < 0){
			HttpConnectionParams.setSoTimeout(mHttpClient.getParams(), 0);
			return;
		}
		HttpConnectionParams.setSoTimeout(mHttpClient.getParams(), timeout);
	}

	@Override
	public void setConnectionTimeout(int timeout) {
		if(timeout < 0){
			HttpConnectionParams.setConnectionTimeout(mHttpClient.getParams(), 0);
			return;
		}
		HttpConnectionParams.setConnectionTimeout(mHttpClient.getParams(), timeout);
	}
	
	@Override
	public int getSocketTimeout() {
		return HttpConnectionParams.getSoTimeout(mHttpClient.getParams());
	}

	@Override
	public int getConnectionTimeout() {
		return HttpConnectionParams.getConnectionTimeout(mHttpClient.getParams());
	}

	@Override
	public <T> BaseResponse<T> get(BaseRequest<T> baseRequest) {
		
		if(baseRequest == null){
			return handleNullMethodParameter(BaseRequest.class.getSimpleName(), new BaseResponse<T>());
		}
		
		HttpGet httpGet = new HttpGet(baseRequest.getUrl());
		
		if(baseRequest.getHeaders() != null){
			
			Set<Entry<String, String>> entries = baseRequest.getHeaders().entrySet();
			
			for(Entry<String, String> e: entries){
				httpGet.addHeader(new BasicHeader(e.getKey(), e.getValue()));
			}
			
		}
		
		httpGet.addHeader(HEADER_USER_AGENT, sUserAgent);
		
		
		//TODO Get Response code and response message
		//TODO Connect
		
		try {
			mHttpClient.execute(httpGet);
		} catch (ClientProtocolException e) {
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
			return handleNullMethodParameter(BaseRequest.class.getSimpleName(), new BaseResponse());
		}
		
		HttpPost httpPost = new HttpPost(baseRequest.getUrl());
		
		if(baseRequest.getHeaders() != null){
			
			Set<Entry<String, String>> entries = baseRequest.getHeaders().entrySet();
			
			for(Entry<String, String> e: entries){
				httpPost.addHeader(new BasicHeader(e.getKey(), e.getValue()));
			}
			
		}
		
		httpPost.addHeader(HEADER_USER_AGENT, sUserAgent);
		ByteArrayEntity entity = new ByteArrayEntity(baseRequest.getBody());
		httpPost.setEntity(entity);
		//TODO Get Response code and response message
		
		try {
			HttpResponse httpResponse = mHttpClient.execute(httpPost);
			
			BaseResponse<T> response = baseRequest.getResponse();
			
			if(httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
				//TODO Handle more success statuses
				response.setError(new Error(Error.NONE));
			} else {
				
			}
			
			
		} catch (ClientProtocolException e) {
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
		mHttpClient.getConnectionManager().shutdown();
	}

}
