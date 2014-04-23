package com.therabbitmage.android.beacon.network;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;

import android.content.Context;
import android.net.http.AndroidHttpClient;
import android.os.Build;
import android.util.Base64;
import android.util.Log;

public final class ApacheNetworkUtils {
	private static final String TAG = ApacheNetworkUtils.class.getSimpleName();
	
	public static final String PROTOCOL_HTTP = "http";
    public static final String PROTOCOL_HTTPS = "https";
    
    public static String sDEFAULT_USER_AGENT_NAME = "Beacon";
    public static String sDEFAULT_AUTHOR_NAME = "Gregory R Saint-Jean";
    
    public static String sCurrentUserAgentName = sDEFAULT_USER_AGENT_NAME;
    public static String sCurrentAuthorName = sDEFAULT_AUTHOR_NAME;
    
    public static final String sDEVICE_BUILD_INFO = "(Linux; U; Android "
            + Build.VERSION.RELEASE + ";" + Locale.getDefault().toString()
            + "; " + Build.DEVICE + "/" + android.os.Build.ID + ")";
    
    public static String sUserAgent = sCurrentUserAgentName + " by " + sCurrentAuthorName + ". "
            + sDEVICE_BUILD_INFO;
    
    public static final String HTTP_METHOD_GET = "GET";
    public static final String HTTP_METHOD_POST = "POST";
    public static final String HTTP_METHOD_DELETE = "DELETE";
    public static final String HTTP_METHOD_OPTIONS = "OPTIONS";
    public static final String HTTP_METHOD_HEAD = "HEAD";
    public static final String HTTP_METHOD_PUT = "PUT";
    public static final String HTTP_METHOD_CONNECT = "CONNECT";
    public static final String HTTP_METHOD_TRACE = "TRACE";
    
    //http://en.wikipedia.org/wiki/List_of_HTTP_header_fields
    /* The user agent string of the user agent http://en.wikipedia.org/wiki/User_agent_string#User_agent_identification */
    public static final String HEADER_USER_AGENT = HTTP.USER_AGENT;
    
    /* The length of the request body in octets (8-bit bytes) */
    public static final String HEADER_CONTENT_LENGTH = HTTP.CONTENT_LEN;
    
    /* The MIME type of the body of the request (used with POST and PUT requests) */
    public static final String HEADER_CONTENT_TYPE = HTTP.CONTENT_TYPE;
    
    /* The type of encoding used on the data. (Only used as a response header) http://en.wikipedia.org/wiki/HTTP_compression */
    public static final String HEADER_CONTENT_ENCODING = HTTP.CONTENT_ENCODING;
    
    /* Content-Types that are acceptable for the response */
    public static final String HEADER_ACCEPT = "Accept";
    
    /* Character sets that are acceptable */
    public static final String HEADER_ACCEPT_CHARSET = "Accept-Charset";
    
    /* A Base64-encoded binary MD5 sum of the content of the request body */
    public static final String HEADER_CONTENT_MD5 = "Content-MD5";
    
    /* Authentication credentials for HTTP authentication */
    public static final String HEADER_AUTHORIZATION = "Authorization";
    
    public static final String CHARSET_US_ASCII = HTTP.US_ASCII;
    public static final String CHARSET_UTF_8 = HTTP.UTF_8;
    public static final String CHARSET_UTF_16 = HTTP.UTF_16;
    
    public static final String HEADER = "HEADER";
    public static final String RESPONSE = "RESPONSE";
    public static final String REQUEST = "REQUEST";
	
    public static final String TYPE_TEXT = HTTP.PLAIN_TEXT_TYPE;
	public static final String TYPE_JSON = "application/json";
	public static final String TYPE_XML = "application/xml";
	public static final String TYPE_FORM_ENCODED = "application/x-www-form-urlencoded";
	public static final String TYPE_NO_MEDIA = "application/octet-stream"; //octets are 8 bit bytes
	public static final String TYPE_DEFAULT = HTTP.DEFAULT_CONTENT_TYPE; // Matches TYPE_NO_MEDIA
	
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
	
    public static void setUserAgent(Context ctx, String userAgent){
    	sCurrentUserAgentName = userAgent;
    	setAgent();
    }
    
    public static void setAuthor(Context ctx, String authorName){
    	sCurrentAuthorName = authorName;
    	setAgent();
    }
    
    public static void setAgent(){
    	sUserAgent = sCurrentUserAgentName + " by " + sCurrentAuthorName + " " + sDEVICE_BUILD_INFO;
    }
    
    public static String toStringRequestHeaders(Header[] headers){
    	
    	StringBuilder result = new StringBuilder();

        for(Header h : headers){
        	result.append(REQUEST + " " + HEADER + ": " + h.getName() + ":" + h.getValue() + "\n");
            Log.i(TAG, REQUEST + " " + HEADER + ": " + h.getName() + ":" + h.getValue());
        }
        
        return result.toString();
    }

    public static String toStringResponseHeaders(Header[] headers){
    	
    	StringBuilder result = new StringBuilder();

        for(Header h : headers){
        	result.append(RESPONSE + " " + HEADER + ": " + h.getName() + ":" + h.getValue() + "\n");
            Log.i(TAG, RESPONSE + " " + HEADER + ": " + h.getName() + ":" + h.getValue());
        }
        
        return result.toString();

    }
	
	public static HttpResponse get(String url, Header[] headers) throws ClientProtocolException, IOException {
		final HttpGet httpGet = new HttpGet(url);
		
		Log.i(TAG, "URI: " + httpGet.getURI());
		
		if(headers != null){
			toStringRequestHeaders(headers);
			httpGet.setHeaders(headers);
		} else {
			httpGet.setHeaders(getDefaultApacheHeaders());
		}
		
		return mHttpClient.execute(httpGet);
	}
	
	public static HttpResponse post(String url, Header[] headers, String data) throws ClientProtocolException, IOException {
		final HttpPost httpPost = new HttpPost(url);
		
		Log.i(TAG, "URI: " + httpPost.getURI());
		Log.i(TAG, "Request Body: " + data);
		
		httpPost.setEntity(new StringEntity(data));
		
		if(headers != null){
			toStringRequestHeaders(headers);
			httpPost.setHeaders(headers);
		} else {
			httpPost.setHeaders(getDefaultApacheHeaders());
		}
		
		return mHttpClient.execute(httpPost);
	}
	
	//TODO Doesn't work. Toss!
	public static final java.net.URI constructJavaUri(android.net.Uri uri) throws URISyntaxException{
		
		if(uri == null){
			return null;
		}
		
		return new java.net.URI(uri.getScheme(), uri.getAuthority(), uri.getPath(), uri.getQuery(), uri.getFragment());
		
	}
	
	public static final Header[] getDefaultApacheHeaders(){
		Header[] headers = new Header[3];
		headers[0] = new BasicHeader(HEADER_USER_AGENT, sUserAgent);
		headers[1] = new BasicHeader(HEADER_CONTENT_TYPE, TYPE_JSON);
		headers[2] = new BasicHeader(HEADER_ACCEPT, CHARSET_UTF_8);
		return headers;
	}
	
	public static final String toStringStatusLine(StatusLine line){
		StringBuilder builder = new StringBuilder();
		
		builder.append("Status Code: " + line.getStatusCode() + "\n");
		builder.append("Reason Phrase: " + line.getReasonPhrase() + "\n");
		builder.append("Protocol Version:\n" 
		+ "Protocol: " + line.getProtocolVersion().getProtocol() + "\n"
		+ "Major: " + line.getProtocolVersion().getMajor() + "\n"
		+ "Minor: " + line.getProtocolVersion().getMinor());
		Log.i(TAG, builder.toString());
		
		return builder.toString();
	}
	
	public static final String getBase64String(final String s){
		return getBase64String(s, Base64.DEFAULT);
	}
	
	public static final String getBase64String(final String s, final int flag){
		
		int f = 0;
		
		switch(flag){
			case Base64.CRLF:
			case Base64.DEFAULT:
			case Base64.NO_CLOSE:
			case Base64.NO_PADDING:
			case Base64.NO_WRAP:
			case Base64.URL_SAFE:
				f = flag;
				break;
			default:
				f = Base64.DEFAULT;
				break;
		}
		
		return Base64.encodeToString(s.getBytes(), f);		
	}
	
	public static final String decodeBase64(final String s){
		return decodeBase64(s, Base64.DEFAULT);
	}
	
	public static final String decodeBase64(final String s, final int flag){
		
		int f = 0;
		
		switch(flag){
			case Base64.CRLF:
			case Base64.DEFAULT:
			case Base64.NO_CLOSE:
			case Base64.NO_PADDING:
			case Base64.NO_WRAP:
			case Base64.URL_SAFE:
				f = flag;
				break;
			default:
				f = Base64.DEFAULT;
				break;
		}
		
		return new String(Base64.decode(s, f));
	}
	
	public static final String getMD5String(final String s){
		
		MessageDigest digester = null;
		
		try {
			digester = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			Log.e(TAG, "NoSuchAlgo Exception, this shouldn't happen.");
			Log.e(TAG, e.toString());
		}
		
		digester.update(s.getBytes());
		return new String(digester.digest());
	}
	
	//http://android-developers.blogspot.com/2011/09/androids-http-clients.html	
	@SuppressWarnings({ "unused", "deprecation" })
	private final static void disableConnectionReuseIfNecessary() {
		   // Work around pre-Froyo bugs in HTTP connection reuse.
		if (Integer.parseInt(Build.VERSION.SDK) < Build.VERSION_CODES.FROYO){
			System.setProperty("http.keepAlive", "false");
		}
	}
	
	private ApacheNetworkUtils(){}

}
