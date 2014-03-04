package com.therabbitmage.android.beacon.utils;

import org.apache.http.protocol.HTTP;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.StrictMode;

import com.therabbitmage.android.beacon.R;

public final class AndroidUtils {
	
	public final static boolean hasNetworkConnectivity(Context ctx){
		
		ConnectivityManager connMgr = (ConnectivityManager)ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		
		return networkInfo != null && networkInfo.isConnected();
	}
	
	public final static void sendSMS(Context ctx, String number, String message){
		if(number == null){
			throw new NullPointerException(ctx.getString(R.string.error_phone_number_required));
		}
		
		assert(message != null);
		
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setData(Uri.parse("sms:" + number));
		intent.setType(HTTP.PLAIN_TEXT_TYPE);
		intent.putExtra("sms_body", message);
		if(intent.resolveActivity(ctx.getPackageManager()) != null){
			ctx.startActivity(intent);
		}
	}
	
	public static final void dialNumber(Context ctx, String number){
		Intent intent = new Intent(Intent.ACTION_DIAL);
	    intent.setData(Uri.parse("tel:" + number));
	    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	    if (intent.resolveActivity(ctx.getPackageManager()) != null) {
	        ctx.startActivity(intent);
	    }
	}
	
	/**
     * Enables strict mode. This should only be called when debugging the application and is useful
     * for finding some potential bugs or best practice violations.
     */
    @TargetApi(11)
    public static void enableStrictMode() {
        // Strict mode is only available on gingerbread or later
        if (gingerbreadOrBetter()) {

            // Enable all thread strict mode policies
            StrictMode.ThreadPolicy.Builder threadPolicyBuilder =
                    new StrictMode.ThreadPolicy.Builder()
                            .detectAll()
                            .penaltyLog();

            // Enable all VM strict mode policies
            StrictMode.VmPolicy.Builder vmPolicyBuilder =
                    new StrictMode.VmPolicy.Builder()
                            .detectAll()
                            .penaltyLog();

            // Use builders to enable strict mode policies
            StrictMode.setThreadPolicy(threadPolicyBuilder.build());
            StrictMode.setVmPolicy(vmPolicyBuilder.build());
        }
    }

	
	//SDK 9 Version 2.3
	public final static boolean gingerbreadOrBetter(){
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