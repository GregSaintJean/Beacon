package com.therabbitmage.android.beacon.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;

public final class AndroidUtils {
	
	public final static boolean hasNetworkConnectivity(Context ctx){
		
		ConnectivityManager connMgr = (ConnectivityManager)ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		
		return networkInfo != null && networkInfo.isConnected();
	}
	
	//SDK 9 Version 2.3
	public final static boolean gingerbreadOfBetter(){
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD;
	}
	
	//SDK 10 Version 2.3.3
	public final static boolean gingerbreadMR1OfBetter(){
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1;
	}
	
	//SDK 11 Version 3.0
	public final static boolean honeycombOrBetter(){
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
	}
	
	//SDK 12 Version 3.1
	public final static boolean honeycombMR1OrBetter(){
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1;
	}
		
	//SDK 13 Version 3.2
	public final static boolean honeycombMR2OrBetter(){
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2;
	}
		
	//SDK 14 Version 4.0
	public final static boolean icsOrBetter(){
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH;
	}
	
	//SDK 15 Version 4.0.3
	public final static boolean icsMR1OrBetter(){
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1;
	}
		
	//SDK 16 Version 4.1
	public final static boolean jellybeanOfBetter(){
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
	}
	
	//SDK 17 Version 4.2
	public final static boolean jellybeanMR1OfBetter(){
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1;
	}
	
	//SDK 18 Version 4.2
	public final static boolean jellybeanMR2OfBetter(){
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2;
	}
		
	//SDK 19 Version 4.4
	public final static boolean kitkatOrBetter(){
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
	}
	
	//http://android-developers.blogspot.com/2011/09/androids-http-clients.html	
	@SuppressWarnings({ "unused", "deprecation" })
	private final static void disableConnectionReuseIfNecessary() {
		   // Work around pre-Froyo bugs in HTTP connection reuse.
		if (Integer.parseInt(Build.VERSION.SDK) < Build.VERSION_CODES.FROYO){
			System.setProperty("http.keepAlive", "false");
		}
	}
		
	private AndroidUtils(){}

}