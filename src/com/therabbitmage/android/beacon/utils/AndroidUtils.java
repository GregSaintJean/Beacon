package com.therabbitmage.android.beacon.utils;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import org.apache.http.protocol.HTTP;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.StrictMode;
import android.telephony.SmsManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.therabbitmage.android.beacon.R;

public final class AndroidUtils {
	
	private static final String TAG = AndroidUtils.class.getSimpleName();
	
	public final static void logBundleContents(Context ctx, Bundle bundle, String logTag){
		
		if(ctx == null || bundle == null || logTag == null){
			return;
		}
		
		Set<String> keys = bundle.keySet();
		
		if(keys != null){
			for(String key : keys){
				
				Object obj = bundle.get(key);
				
				if(obj instanceof Integer){
					Log.i(logTag, key + ": " + ((Integer)obj));
					return;
				}
				
				if(obj instanceof Double){
					Log.i(logTag, key + ": " + ((Double)obj));
					return;
				}
				
				if(obj instanceof Float){
					Log.i(logTag, key + ": " + ((Float)obj));
					return;
				}
				
				if(obj instanceof String){
					Log.i(logTag, key + ": " + ((String)obj));
					return;
				}
				
				if(obj instanceof Parcelable){
					Log.i(logTag, key + " (Parcelable): " + obj.toString());
				}
				
				if(obj instanceof Serializable){
					Log.i(logTag, key + " (Serializable): " + obj.toString());
				}
				
			}
		}
	}
	
	public static final boolean hasNetworkConnectivity(Context ctx){
		
		if(ctx == null){
			return false;
		}
		
		ConnectivityManager connMgr = (ConnectivityManager)ctx
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		
		return networkInfo != null && networkInfo.isConnected();
	}
	
	public static final boolean isGpsOnline(Context ctx){
		LocationManager lm = (LocationManager)ctx.getSystemService(Context.LOCATION_SERVICE);
		return lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
	}
	
	public final static void sendSms(Context ctx, String number, String message,
			PendingIntent sentIntent, PendingIntent deliveryIntent){
		SmsManager smsManager = SmsManager.getDefault();
		List<String> messages = smsManager.divideMessage(message);
		for(int i = 0; i < messages.size(); i++){
			smsManager.sendTextMessage(number, null, messages.get(i), sentIntent, deliveryIntent);
		}
	}
	
	
	public final static void requestSendSMS(Context ctx, String number, String message){
		if(number == null){
			throw new NullPointerException(ctx.getString(R.string.error_phone_number_required));
		}
		
		assert(message != null);
		
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setData(Uri.parse("smsto:" + number));
		intent.setType(HTTP.PLAIN_TEXT_TYPE);
		intent.putExtra("sms_body", message);
		if(intent.resolveActivity(ctx.getPackageManager()) != null){
			ctx.startActivity(intent);
		}
	}
	
	public static final void callNumber(Context ctx, String number){
		Intent intent = new Intent(Intent.ACTION_CALL);
	    intent.setData(Uri.parse("tel:" + number));
	    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	    if (intent.resolveActivity(ctx.getPackageManager()) != null) {
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
	
	public static final boolean checkPhoneAndSmsCapability(Context ctx){
		return ctx.getPackageManager().hasSystemFeature(PackageManager.FEATURE_TELEPHONY);
	}
	
	public static final boolean checkGpsCapability(Context ctx){
		return ctx.getPackageManager().hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS);
	}
	
	public static final boolean checkLocationCapability(Context ctx){
		return ctx.getPackageManager().hasSystemFeature(PackageManager.FEATURE_LOCATION);
	}
	
	public static final boolean checkNetworkLocationCapability(Context ctx) {
		return ctx.getPackageManager().hasSystemFeature(PackageManager.FEATURE_LOCATION_NETWORK);
	}
	
	public static final boolean isNetworkLocationOnline(Context ctx){
		LocationManager lm = (LocationManager)ctx.getSystemService(LocationManager.GPS_PROVIDER);
		return lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
	}
	
	public static final LocationManager getLocationManager(Context ctx) {
		return (LocationManager) ctx
				.getSystemService(Context.LOCATION_SERVICE);
	}
	
	public static final boolean isGoogleServicesAvailable(Context ctx){
		// Check that Google Play services is available
        int resultCode =
                GooglePlayServicesUtil.isGooglePlayServicesAvailable(ctx);

        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            // In debug mode, log the status
            Log.d(TAG, ctx.getString(R.string.play_services_available));

            // Continue
            return true;
        // Google Play services was not available for some reason
        } else {
        	
        	Log.e(TAG, ctx.getString(R.string.error_general));
        	Log.e(TAG, ctx.getString(R.string.result_code) + resultCode);
        	
        	switch(resultCode){
        		case ConnectionResult.SERVICE_MISSING:
        			Log.e(TAG, ctx.getString(R.string.error_service_missing));
        			break;
        		case ConnectionResult.SERVICE_INVALID:
        			Log.e(TAG, ctx.getString(R.string.error_service_invalid));
        			break;
        		case ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED:
        			Log.e(TAG, ctx.getString(R.string.error_requires_updating));
        			break;
        		case ConnectionResult.SERVICE_DISABLED:
        			Log.e(TAG, ctx.getString(R.string.error_service_disabled));
        			break;
        		case ConnectionResult.DEVELOPER_ERROR:
        			Log.e(TAG, ctx.getString(R.string.error_developer_error));
        		case ConnectionResult.NETWORK_ERROR:
        			Log.e(TAG, ctx.getString(R.string.error_network_connectivity));
        	}
        	
           Log.d(TAG, ctx.getString(R.string.play_services_unavailable));
            return false;
        }
	}
		
	private AndroidUtils(){}

}