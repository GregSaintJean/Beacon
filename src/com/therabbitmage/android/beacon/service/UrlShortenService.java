package com.therabbitmage.android.beacon.service;

import java.io.IOException;
import java.net.URISyntaxException;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.therabbitmage.android.beacon.entities.google.urlshortener.Url;
import com.therabbitmage.android.beacon.network.NetworkResponse;
import com.therabbitmage.android.beacon.network.URLShortenerAPI;

public class UrlShortenService extends IntentService {
	
	private static final String TAG = UrlShortenService.class.getSimpleName();
	
	public static final String ACTION_SHORTEN = "action_shorten";
	public static final String BROADCAST_SUCCESS_URL =  "broadcast_success_url";
	public static final String BROADCAST_ERROR = "broadcast_error";
	public static final String EXTRA_ERROR = "extra_error";
	public static final String EXTRA_URL = "extra_url";
	public static final String EXTRA_RECEIVER = "extra_receiver";
	
	public static final int RESULT_CODE_SUCCESS = 0;
	public static final int RESULT_CODE_FAIL = 1;
	
	private LocalBroadcastManager mLBMgr;

	public UrlShortenService() {
		super(UrlShortenService.class.getSimpleName());
		mLBMgr = LocalBroadcastManager.getInstance(this);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		
		if(intent == null){
			return;
		}
		
		if(intent.getAction() == null){
			
			broadcastError(null);
			return;
		}
		
		if(intent.getExtras() == null){
			broadcastError(null);
			return ;
		}
		
		if(intent.getAction().equals(ACTION_SHORTEN)){
			
			if(intent.getExtras().containsKey(EXTRA_URL)){
				
				if(intent.getExtras().containsKey(EXTRA_RECEIVER)){
					grabShortenedUrl(intent.getExtras().getString(EXTRA_URL), (ResultReceiver)intent.getExtras().get(EXTRA_RECEIVER));
				} else {
					
				}
				
			} else {
				broadcastError((ResultReceiver)intent.getExtras().get(EXTRA_RECEIVER));
			}
			
		}

	}
	
	private void grabShortenedUrl(String url, ResultReceiver receiver){
		
		try{
			
			
			NetworkResponse response = URLShortenerAPI.urlShorten(url);
			if(response.getError() == 0){
				Url resultUrl = response.getUrl();
				Bundle resultData = new Bundle();
				resultData.putParcelable(EXTRA_URL, resultUrl);
				if(receiver != null){
					receiver.send(RESULT_CODE_SUCCESS, resultData);
				} else {
					Intent intent = new Intent(BROADCAST_SUCCESS_URL);
					intent.putExtra(EXTRA_URL, resultUrl);
					mLBMgr.sendBroadcast(intent);
				}
			} else {
				broadcastError(receiver);
			}
			
		} catch(IOException e){
			Log.e(TAG, e.toString());
			broadcastError(receiver);
		} catch (URISyntaxException e) {
			Log.e(TAG, e.toString());
			broadcastError(receiver);
		} catch(Exception e){
			Log.e(TAG, e.toString());
			broadcastError(receiver);
		}
		
	}
	
	private void broadcastError(ResultReceiver receiver){
		if(receiver == null){
			Intent intent = new Intent(BROADCAST_ERROR);
			mLBMgr.sendBroadcast(intent);
		} else {
			receiver.send(RESULT_CODE_FAIL, null);
		}
	}

}
