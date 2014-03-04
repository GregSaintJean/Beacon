package com.therabbitmage.android.beacon;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.therabbitmage.android.beacon.R;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.location.LocationManager;
import android.net.Uri;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.therabbitmage.android.beacon.utils.AndroidUtils;

public class BeaconApp extends Application {
	private static final String TAG = BeaconApp.class.getSimpleName();

	private static final String US_EMERGENCY_NUMBER = "911";
	private static final int NO_ID = -1;

	@Override
	public void onCreate() {
		super.onCreate();
		
		if(BuildConfig.DEBUG){
			printDebugKeyHash();
		}

	}
	
	public final void printDebugKeyHash(){
		try {
			PackageInfo info = getPackageManager().getPackageInfo(
					BeaconApp.class.getPackage().getName(),
					PackageManager.GET_SIGNATURES);
			for (Signature signature : info.signatures) {
				MessageDigest md = MessageDigest.getInstance("SHA");
				md.update(signature.toByteArray());
				Log.d("KeyHash:",
						Base64.encodeToString(md.digest(), Base64.DEFAULT));
			}
		} catch (NameNotFoundException e) {
			Log.d(TAG + e.getStackTrace()[0].getLineNumber(), e.toString());

		} catch (NoSuchAlgorithmException e) {
			Log.d(TAG + e.getStackTrace()[0].getLineNumber(), e.toString());
		}
	}

	public LocationManager getLocationManager() {
		return (LocationManager) this
				.getSystemService(Context.LOCATION_SERVICE);
	}

	public final void sms911(String number, String message) {
		AndroidUtils.sendSMS(this, US_EMERGENCY_NUMBER, message);
	}

	public final void dial911() {
		AndroidUtils.dialNumber(this, US_EMERGENCY_NUMBER);
	}

	public final boolean hasFacebookLogin() {
		return !TextUtils.isEmpty(getFacebookAccessToken());
	}
	
	public final String getFacebookAccessToken() {
		SharedPreferences sharedPref = getSharedPreferences(
				getString(R.string.facebook_pref), Context.MODE_PRIVATE);
		return sharedPref.getString(
				getString(R.string.facebook_access_token_key), "");
	}

	public final void setFacebookAccessToken(String token) {
		SharedPreferences sharedPref = getSharedPreferences(
				getString(R.string.facebook_pref), Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putString(getString(R.string.facebook_access_token_key), token);
		editor.commit();
	}

	public final boolean hasTwitterRequestToken() {
		return !TextUtils.isEmpty(getTwitterRequestToken());
	}

	public final boolean hasTwitterRequestSecretToken() {
		return !TextUtils.isEmpty(getTwitterRequestSecretToken());
	}

	public final String getTwitterRequestToken() {
		SharedPreferences sharedPref = getSharedPreferences(
				getString(R.string.twitter_pref), Context.MODE_PRIVATE);
		return sharedPref.getString(getString(R.string.twitter_request_token),
				"");
	}

	public final String getTwitterRequestSecretToken() {
		SharedPreferences sharedPref = getSharedPreferences(
				getString(R.string.twitter_pref), Context.MODE_PRIVATE);
		return sharedPref.getString(
				getString(R.string.twitter_request_secret_token), "");
	}

	public final void setTwitterRequestToken(String token) {
		SharedPreferences sharedPref = getSharedPreferences(
				getString(R.string.twitter_pref), Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putString(getString(R.string.twitter_request_token), token);
		editor.commit();
	}

	public final void setTwitterRequestSecretToken(String token) {
		SharedPreferences sharedPref = getSharedPreferences(
				getString(R.string.twitter_pref), Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putString(getString(R.string.twitter_request_secret_token),
				token);
		editor.commit();
	}

	public final void clearTwitterRequestTokenAndSecret() {
		SharedPreferences sharedPref = getSharedPreferences(
				getString(R.string.twitter_pref), Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.remove(getString(R.string.twitter_request_token));
		editor.commit();
		editor.remove(getString(R.string.twitter_request_secret_token));
		editor.commit();
	}
	
	public final boolean hasTwitterLogin(){
		return hasTwitterAccessToken() && hasTwitterAccessTokenSecret();
	}

	public final boolean hasTwitterAccessToken() {
		return !TextUtils.isEmpty(getTwitterAccessToken());
	}

	public final boolean hasTwitterAccessTokenSecret() {
		return !TextUtils.isEmpty(getTwitterAccessTokenSecret());
	}

	public final String getTwitterAccessToken() {
		SharedPreferences sharedPref = getSharedPreferences(
				getString(R.string.twitter_pref), Context.MODE_PRIVATE);
		return sharedPref.getString(getString(R.string.twitter_access_token),
				"");
	}
	
	public final String getTwitterAccessTokenSecret() {
		SharedPreferences sharedPref = getSharedPreferences(
				getString(R.string.twitter_pref), Context.MODE_PRIVATE);
		return sharedPref.getString(
				getString(R.string.twitter_access_token_secret), "");
	}
	
	public final void setTwitterAccessToken(String token) {
		SharedPreferences sharedPref = getSharedPreferences(
				getString(R.string.twitter_pref), Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putString(getString(R.string.twitter_access_token), token);
		editor.commit();
	}

	public final void setTwitterAccessTokenSecret(String token) {
		SharedPreferences sharedPref = getSharedPreferences(
				getString(R.string.twitter_pref), Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putString(getString(R.string.twitter_access_token_secret), token);
		editor.commit();
	}

	public final void clearTwitterAccessTokenAndSecret() {
		SharedPreferences sharedPref = getSharedPreferences(
				getString(R.string.twitter_pref), Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.remove(getString(R.string.twitter_access_token));
		editor.commit();
		editor.remove(getString(R.string.twitter_access_token_secret));
		editor.commit();
		editor.remove(getString(R.string.twitter_user_id));
		editor.commit();
	}

	public final boolean hasTwitterUserId() {
		return getTwitterUserId() >= 0;
	}
	
	public final int getTwitterUserId() {
		SharedPreferences sharedPref = getSharedPreferences(
				getString(R.string.twitter_pref), Context.MODE_PRIVATE);
		return sharedPref.getInt(getString(R.string.twitter_user_id), NO_ID);
	}

	public final void setTwitterUserId(int userId) {
		SharedPreferences sharedPref = getSharedPreferences(
				getString(R.string.twitter_pref), Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putInt(getString(R.string.twitter_user_id), userId);
		editor.commit();
	}
	
	public final String getTwitterScreenName() {
		SharedPreferences sharedPref = getSharedPreferences(
				getString(R.string.twitter_pref), Context.MODE_PRIVATE);
		return sharedPref.getString(getString(R.string.twitter_username), "");
	}

	public final void setTwitterScreenName(String userName) {
		SharedPreferences sharedPref = getSharedPreferences(
				getString(R.string.twitter_pref), Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putString(getString(R.string.twitter_username), userName);
		editor.commit();
	}

	public final boolean isFirstRun() {
		SharedPreferences sharedPref = getSharedPreferences(
				getString(R.string.beacon_pref), Context.MODE_PRIVATE);
		return sharedPref.getBoolean(getString(R.string.first_time_run), true);
	}

	public final void firstRunExecuted() {
		SharedPreferences sharedPref = getSharedPreferences(
				getString(R.string.beacon_pref), Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putBoolean(getString(R.string.first_time_run), false);
		editor.commit();
	}

	public final boolean isSetupDone() {
		SharedPreferences sharedPref = getSharedPreferences(getString(R.string.beacon_pref), 
				Context.MODE_PRIVATE);
		return sharedPref.getBoolean(getString(R.string.is_setup_done), false);
	}

	public final void setIsSetupDone(boolean isSetupDone) {
		SharedPreferences sharedPref = getSharedPreferences(
				getString(R.string.beacon_pref), Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putBoolean(getString(R.string.is_setup_done), isSetupDone);
		editor.commit();
	}
	
	public final boolean isTablet(){
		return getResources().getBoolean(R.bool.isTablet);
	}
	
	public final boolean isGoogleServicesAvailable(){
		// Check that Google Play services is available
        int resultCode =
                GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            // In debug mode, log the status
            Log.d(TAG, getString(R.string.play_services_available));

            // Continue
            return true;
        // Google Play services was not available for some reason
        } else {
           Log.d(TAG, getString(R.string.play_services_unavailable));
            return false;
        }
	}

	// TODO I may not need the stuff below

	// http://stackoverflow.com/questions/15426144/turning-on-and-off-gps-programmatically-in-android-4-0-and-above
	public void turnGPSOn() {
		Intent intent = new Intent("android.location.GPS_ENABLED_CHANGE");
		intent.putExtra("enabled", true);
		this.sendBroadcast(intent);

		String provider = Settings.Secure.getString(this.getContentResolver(),
				Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
		if (!provider.contains("gps")) { // if gps is disabled
			final Intent poke = new Intent();
			poke.setClassName("com.android.settings",
					"com.android.settings.widget.SettingsAppWidgetProvider");
			poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
			poke.setData(Uri.parse("3"));
			this.sendBroadcast(poke);

		}
	}

	// automatic turn off the gps
	public void turnGPSOff() {
		String provider = Settings.Secure.getString(this.getContentResolver(),
				Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
		if (provider.contains("gps")) { // if gps is enabled
			final Intent poke = new Intent();
			poke.setClassName("com.android.settings",
					"com.android.settings.widget.SettingsAppWidgetProvider");
			poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
			poke.setData(Uri.parse("3"));
			this.sendBroadcast(poke);
		}
	}
}
