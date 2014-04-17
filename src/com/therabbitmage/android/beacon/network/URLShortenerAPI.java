package com.therabbitmage.android.beacon.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.protocol.HTTP;

import android.net.Uri;

import com.therabbitmage.android.beacon.entities.google.urlshortener.Url;

public final class URLShortenerAPI {
	
	private static final String BASE_URL = "https://www.googleapis.com/urlshortener/v1/url";
	private static final String KEY = "key";
	
	public static String urlShorten(String url, String api_key) throws IOException{
	 	Uri.Builder uriBuilder = new Uri.Builder();
	 	uriBuilder.authority(BASE_URL);
	 	uriBuilder.appendQueryParameter(KEY, api_key);
	 	
	 	Map<String, String> requestHeaders = new HashMap<String, String>();
	 	requestHeaders.put(HTTP.CONTENT_TYPE, NetworkUtils.TYPE_JSON);
	 	
	 	HttpResponse response = NetworkUtils.post(url, requestHeaders, new Url(url).toJson());
	 	HttpEntity entity = response.getEntity();
	 	
	 	BufferedReader br = new BufferedReader(new InputStreamReader(entity.getContent()));
	 	
	 	StringBuilder stringBuilder = new StringBuilder();
	 	
	 	String output = new String();
	 	
	 	while((output = br.readLine()) != null){
	 		stringBuilder.append(output);
	 	}
	 	
	 	br.close();
	 	NetworkUtils.release();
	 	
	 	return br.toString();
	 	
	}

}
