package com.therabbitmage.android.beacon.network.response;

import java.util.Map;

public class BaseResponse<T> {
	
	private Map<String, String> mHeaders;
	private String mContentType;
	private T mBodyResponse;
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
	
	public T getBodyResponse() {
		return mBodyResponse;
	}

	public void setBodyResponse(T bodyResponse) {
		this.mBodyResponse = bodyResponse;
	}
	
	public String getContentType() {
		return mContentType;
	}

	public void setContentType(String mContentType) {
		this.mContentType = mContentType;
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

