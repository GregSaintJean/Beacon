package com.therabbitmage.android.beacon.network;

import com.therabbitmage.android.beacon.network.request.BaseRequest;
import com.therabbitmage.android.beacon.network.response.BaseResponse;
import com.therabbitmage.android.beacon.utils.AndroidUtils;

public class SimpleHttpClient extends BaseHttpClient{
	
	public static final int GOOGLE = 0;
	public static final int APACHE = 1;
	
	public static BaseHttpClient getInstance(){
		return getInstance(false);
	}
	
	public static BaseHttpClient getInstance(int clientType){
		
		switch(clientType){
			case GOOGLE:
				sClient =  AndroidHttpClient.getInstance();
			case APACHE:
				sClient = ApacheHttpClient.getInstance();
			default:
				sClient = getInstance(false);
		}
		
		return sClient;
		
	}
	
	public static BaseHttpClient getInstance(boolean force){
		
		if(sClient == null || force){
			if(AndroidUtils.gingerbreadOrBetter()){
				sClient =  AndroidHttpClient.getInstance();
			} else {
				sClient = ApacheHttpClient.getInstance();
			}
		}
		return sClient;
	}

	@Override
	public void setSocketTimeout(int timeout) {
		sClient.setSocketTimeout(timeout);
	}

	@Override
	public void setConnectionTimeout(int timeout) {
		sClient.setConnectionTimeout(timeout);
	}
	
	@Override
	public int getSocketTimeout() {
		return sClient.getSocketTimeout();
	}

	@Override
	public int getConnectionTimeout() {
		return sClient.getConnectionTimeout();
	}

	@Override
	public <T> BaseResponse get(BaseRequest<T> baseRequest) {
		return sClient.get(baseRequest);
	}

	@Override
	public <T> BaseResponse post(BaseRequest<T> baseRequest) {
		return sClient.post(baseRequest);
	}

	@Override
	public void release() {
		sClient.release();
	}
}
