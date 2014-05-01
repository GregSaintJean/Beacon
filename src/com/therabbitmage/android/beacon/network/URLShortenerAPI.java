package com.therabbitmage.android.beacon.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.message.BasicHeader;

import android.net.Uri;
import android.util.Log;

import com.therabbitmage.android.beacon.entities.google.urlshortener.Url;

public final class URLShortenerAPI {
	
	private static final String TAG = URLShortenerAPI.class.getSimpleName();
	
	private static final String BASE_URL = "https://www.googleapis.com/urlshortener/v1/url";
	
	public static NetworkResponse urlShorten(String url) throws IOException, URISyntaxException{
	 	android.net.Uri.Builder uriBuilder = Uri.parse(BASE_URL).buildUpon();
	 	String uri = uriBuilder.build().toString();
	 	
	 	Header[] headers = new Header[1];
	 	headers[0] = new BasicHeader(ApacheNetworkUtils.HEADER_CONTENT_TYPE, ApacheNetworkUtils.TYPE_JSON);
	 	ApacheNetworkUtils.getAndroidInstance(ApacheNetworkUtils.sUserAgent, false);
	 	HttpResponse response = ApacheNetworkUtils.post(
	 			uri,
	 			ApacheNetworkUtils.getDefaultApacheHeaders(),
	 			new Url(url).toJson());
	 	ApacheNetworkUtils.toStringResponseHeaders(response.getAllHeaders());
	 	
	 	ApacheNetworkUtils.toStringStatusLine(response.getStatusLine());
	 	
	 	HttpEntity entity = response.getEntity();
	 	
	 	NetworkResponse networkResponse = new NetworkResponse();
	 	
	 	if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
	 		networkResponse.setError(0);
	 		BufferedReader br = new BufferedReader(new InputStreamReader(entity.getContent()));
		 	
		 	StringBuilder stringBuilder = new StringBuilder();
		 	
		 	String output = new String();
		 	
		 	while((output = br.readLine()) != null){
		 		stringBuilder.append(output);
		 	}
		 	
		 	br.close();
		 	Log.i(TAG, "Body: " + stringBuilder.toString());
		 	
		 	networkResponse.setUrl(Url.fromJson(stringBuilder.toString()));
	 		
	 	} else {
	 		networkResponse.setError(1);
	 	}
	 	
	 	return networkResponse;
	}

}

