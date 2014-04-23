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
import com.therabbitmage.android.beacon.network.URLShortenerAPI;

public class UrlShortenService extends IntentService {
	
	private static final String TAG = UrlShortenService.class.getSimpleName();
	
	public static final String ACTION_SHORTEN = "action_shorten";
	public static final String BROADCAST_SUCCESS_URL =  "broadcast_success_url";
	public static final String EXTRA_ERROR = "extra_error";
	public static final String EXTRA_URL = "extra_url";
	public static final String EXTRA_RECEIVER = "extra_receiver";
	
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
			
			//TODO Send an error
			return;
		}
		
		if(intent.getExtras() == null){
			//TODO Send an error
			return ;
		}
		
		if(intent.getAction().equals(ACTION_SHORTEN)){
			
			if(intent.getExtras().containsKey(EXTRA_URL)){
				
				if(intent.getExtras().containsKey(EXTRA_RECEIVER)){
					grabShortenedUrl(intent.getExtras().getString(EXTRA_URL), (ResultReceiver)intent.getExtras().get(EXTRA_RECEIVER));
				} else {
					
				}
				
			} else {
				//TODO Send an error
			}
			
		}

	}
	
	private void grabShortenedUrl(String url, ResultReceiver receiver){
		
		try{
			
			Url resultUrl = URLShortenerAPI.urlShorten(url);
			
			if(receiver != null){
				Bundle resultData = new Bundle();
				resultData.putParcelable(EXTRA_URL, resultUrl);
				receiver.send(0, resultData);
			} else {
				Intent intent = new Intent(BROADCAST_SUCCESS_URL);
				mLBMgr.sendBroadcast(intent);
			}
			
		} catch(IOException e){
			Log.e(TAG, e.toString());
		} catch (URISyntaxException e) {
			Log.e(TAG, e.toString());
		} catch(Exception e){
			Log.e(TAG, e.toString());
		}
		
	}

}
