package com.therabbitmage.android.beacon.network.response;

import java.io.InputStream;
import java.util.Map;

public abstract class BaseResponse {
	
	private Map<String, String> mHeaders;
	private InputStream mBodyResponse;
	private Error mError;
	
	public BaseResponse(){
		setError(new Error());
	}

	public Map<String, String> getHeaders() {
		return mHeaders;
	}

	public void setHeaders(Map<String, String> headers) {
		this.mHeaders = headers;
	}

	public InputStream getBodyResponse() {
		return mBodyResponse;
	}

	public void setBodyResponse(InputStream bodyResponse) {
		this.mBodyResponse = bodyResponse;
	}
	
	public Error getError() {
		return mError;
	}

	public void setError(Error error) {
		this.mError = error;
	}

	public static class Error{
		
		public static final int NONE = 0;
		public static final int CLIENT_EXCEPTION = 1;
		public static final int SERVER_HTTP_STATUS = 2;
		public static final int SERVER_RESPONSE_ERROR = 3;
		
		private int mType;
		private Exception mException;
		
		public Error(){
			mType = NONE;
		}
		
		public Error(int type){
			switch(type){
			 case NONE:
			 case CLIENT_EXCEPTION:
			 case SERVER_HTTP_STATUS:
			 case SERVER_RESPONSE_ERROR:
				 mType = type;
				 return;
			 default:
				 mType = NONE;
				 return;
			}
		}

		public int getType() {
			return mType;
		}

		public void setType(int mType) {
			this.mType = mType;
		}

		public Exception getException() {
			return mException;
		}

		public void setException(Exception mException) {
			this.mException = mException;
		}
	}

}

