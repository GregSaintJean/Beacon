package com.therabbitmage.android.beacon;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.google.analytics.tracking.android.GoogleAnalytics;
import com.therabbitmage.android.beacon.utils.AndroidUtils;
import com.therabbitmage.android.beacon.utils.ChronoUtils;

public class BeaconApp extends Application {
	
	private static final String TAG = BeaconApp.class.getSimpleName();
	private static final boolean IS_DEBUG = BuildConfig.DEBUG;
	
	private static final int NO_ID = -1;
	
	private static BeaconApp sBeacon;
	private static UUID sUUID;
	private GoogleAnalytics mGoogleAnalytics;
	
	//Should Beacon be online?
	private static boolean sIsBeaconOnline = false;
	
	//Is Beacon currently transmitting?
	private static boolean sIsBeaconActive = false;
	
	private static boolean sHasNetworkConnectivity = false;
	private static boolean sIsGpsOnline = false;
	
	private static boolean sIsTablet = false;
	private static boolean sHasSmsCapability = false;
	private static boolean sHasLocationCapability = false;
	private static boolean sHasNetworkLocationCapability = false;
	private static boolean sHasGpsCapability = false;

	@Override
	public void onCreate() {
		super.onCreate();
		
		sBeacon = this;
		
		mGoogleAnalytics = GoogleAnalytics.getInstance(this);
		mGoogleAnalytics.setAppOptOut(getAnalyticsOptOut());
		setAnalyticsOptOut(mGoogleAnalytics.getAppOptOut());
		
		if(IS_DEBUG){
			Log.d(TAG, "This is a debug build.");
			printDebugKeyHash();
		}
		
		sIsTablet = checkIsTablet();
		sHasSmsCapability = AndroidUtils.checkPhoneAndSmsCapability(this);
		sHasGpsCapability = AndroidUtils.checkGpsCapability(this);
		sHasLocationCapability = AndroidUtils.checkLocationCapability(this);
		sHasNetworkLocationCapability = AndroidUtils.checkNetworkLocationCapability(this);		
		
		String uuid = pullUUID();
		
		if(uuid != null){
			sUUID = UUID.fromString(uuid);
		} else {
			sUUID = UUID.randomUUID();
			putUUID(sUUID.toString());
		}
		
		if(IS_DEBUG){
			Log.d(TAG, "UUID = " + sUUID.toString());
		}

	}
	
	public static final boolean isDebug(){
		return IS_DEBUG;
	}
	
	public static final void setisBeaconOnline(boolean isBeaconOnline){
		sIsBeaconOnline = isBeaconOnline;
	}
	
	public static final boolean isBeaconOnline(){
		return sIsBeaconOnline;
	}
	
	public static final boolean isActive() {
		return sIsBeaconActive;
	}

	public static final void setActive(boolean isActive) {
		BeaconApp.sIsBeaconActive = isActive;
	}

	public static final BeaconApp getInstance(){
		return sBeacon;
	}
	
	public GoogleAnalytics getGoogleAnalytics(){
		return mGoogleAnalytics;
	}

	public final void printDebugKeyHash(){
		try {
			PackageInfo info = getPackageManager().getPackageInfo(
					BeaconApp.class.getPackage().getName(),
					PackageManager.GET_SIGNATURES);
			for (Signature signature : info.signatures) {
				MessageDigest md = MessageDigest.getInstance("SHA");
				md.update(signature.toByteArray());
				if(IS_DEBUG){
					Log.d(TAG, String.format(getString(R.string.logcat_keyhash), (Base64.encodeToString(md.digest(), Base64.DEFAULT))));
				}
			}
		} catch (NameNotFoundException e) {
			if(IS_DEBUG){
				Log.d(TAG + e.getStackTrace()[0].getLineNumber(), e.toString());
			}
			

		} catch (NoSuchAlgorithmException e) {
			if(IS_DEBUG){
				Log.d(TAG + e.getStackTrace()[0].getLineNumber(), e.toString());
			}
		}
	}

	public static final boolean hasTwitterRequestToken() {
		return !TextUtils.isEmpty(getTwitterRequestToken());
	}

	public static final boolean hasTwitterRequestSecretToken() {
		return !TextUtils.isEmpty(getTwitterRequestSecretToken());
	}
	
	private static final void putUUID(String uuid){
		BeaconApp app = BeaconApp.getInstance();
		
		SharedPreferences sharedPref= app.getSharedPreferences(app.getString(R.string.beacon_pref), Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putString(app.getString(R.string.uuid), uuid);
		editor.commit();
	}
	
	private static final String pullUUID(){
		BeaconApp app = BeaconApp.getInstance();
		SharedPreferences sharedPref= app.getSharedPreferences(app.getString(R.string.beacon_pref), Context.MODE_PRIVATE);
		return sharedPref.getString(app.getString(R.string.uuid), null);
	}
	
	public static final UUID getUUID(){
		return sUUID;
	}
	
	public static final void setAnalyticsOptOut(boolean optOut){
		BeaconApp app = getInstance();
		SharedPreferences sharedPref = app.getSharedPreferences(
				app.getString(R.string.beacon_pref), Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putBoolean(app.getString(R.string.analytics_opt_out), optOut);
		editor.commit();
	}
	
	public static final boolean getAnalyticsOptOut(){
		BeaconApp app = getInstance();
		SharedPreferences sharedPref = app.getSharedPreferences(
				app.getString(R.string.beacon_pref), Context.MODE_PRIVATE);
		return sharedPref.getBoolean(app.getString(R.string.analytics_opt_out), false);
	}

	public static final String getTwitterRequestToken() {
		SharedPreferences sharedPref = BeaconApp.getInstance().getSharedPreferences(
				BeaconApp.getInstance().getString(R.string.twitter_pref), Context.MODE_PRIVATE);
		return sharedPref.getString(BeaconApp.getInstance().getString(R.string.twitter_request_token),
				"");
	}

	public static final String getTwitterRequestSecretToken() {
		SharedPreferences sharedPref = BeaconApp.getInstance().getSharedPreferences(
				BeaconApp.getInstance().getString(R.string.twitter_pref), Context.MODE_PRIVATE);
		return sharedPref.getString(BeaconApp.getInstance().getString(R.string.twitter_request_secret_token), "");
	}

	public static final void setTwitterRequestToken(String token) {
		SharedPreferences sharedPref = BeaconApp.getInstance().getSharedPreferences(
				BeaconApp.getInstance().getString(R.string.twitter_pref), Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putString(BeaconApp.getInstance().getString(R.string.twitter_request_token), token);
		editor.commit();
	}

	public static final void setTwitterRequestSecretToken(String token) {
		SharedPreferences sharedPref = BeaconApp.getInstance().getSharedPreferences(
				BeaconApp.getInstance().getString(R.string.twitter_pref), Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putString(BeaconApp.getInstance().getString(R.string.twitter_request_secret_token),
				token);
		editor.commit();
	}

	public static final void clearTwitterRequestTokenAndSecret() {
		SharedPreferences sharedPref = BeaconApp.getInstance().getSharedPreferences(
				BeaconApp.getInstance().getString(R.string.twitter_pref), Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.remove(BeaconApp.getInstance().getString(R.string.twitter_request_token));
		editor.commit();
		editor.remove(BeaconApp.getInstance().getString(R.string.twitter_request_secret_token));
		editor.commit();
	}
	
	public static final boolean hasTwitterLogin(){
		return hasTwitterAccessToken() && hasTwitterAccessTokenSecret();
	}

	public static final boolean hasTwitterAccessToken() {
		return !TextUtils.isEmpty(getTwitterAccessToken());
	}

	public static final boolean hasTwitterAccessTokenSecret() {
		return !TextUtils.isEmpty(getTwitterAccessTokenSecret());
	}

	public static final String getTwitterAccessToken() {
		SharedPreferences sharedPref = BeaconApp.getInstance().getSharedPreferences(
				BeaconApp.getInstance().getString(R.string.twitter_pref), Context.MODE_PRIVATE);
		return sharedPref.getString(BeaconApp.getInstance().getString(R.string.twitter_access_token),
				"");
	}
	
	public static final String getTwitterAccessTokenSecret() {
		SharedPreferences sharedPref = BeaconApp.getInstance().getSharedPreferences(
				BeaconApp.getInstance().getString(R.string.twitter_pref), Context.MODE_PRIVATE);
		return sharedPref.getString(
				BeaconApp.getInstance().getString(R.string.twitter_access_token_secret), "");
	}
	
	public static final void setTwitterAccessToken(String token) {
		SharedPreferences sharedPref = BeaconApp.getInstance().getSharedPreferences(
				BeaconApp.getInstance().getString(R.string.twitter_pref), Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putString(BeaconApp.getInstance().getString(R.string.twitter_access_token), token);
		editor.commit();
	}

	public static final void setTwitterAccessTokenSecret(String token) {
		SharedPreferences sharedPref = BeaconApp.getInstance().getSharedPreferences(
				BeaconApp.getInstance().getString(R.string.twitter_pref), Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putString(BeaconApp.getInstance().getString(R.string.twitter_access_token_secret), token);
		editor.commit();
	}

	public static final void clearTwitterAccessTokenAndSecret() {
		SharedPreferences sharedPref = BeaconApp.getInstance().getSharedPreferences(
				BeaconApp.getInstance().getString(R.string.twitter_pref), Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.remove(BeaconApp.getInstance().getString(R.string.twitter_access_token));
		editor.commit();
		editor.remove(BeaconApp.getInstance().getString(R.string.twitter_access_token_secret));
		editor.commit();
		editor.remove(BeaconApp.getInstance().getString(R.string.twitter_user_id));
		editor.commit();
	}

	public static final boolean hasTwitterUserId() {
		return getTwitterUserId() >= 0;
	}
	
	public static final int getTwitterUserId() {
		SharedPreferences sharedPref = BeaconApp.getInstance().getSharedPreferences(
				BeaconApp.getInstance().getString(R.string.twitter_pref), Context.MODE_PRIVATE);
		return sharedPref.getInt(BeaconApp.getInstance().getString(R.string.twitter_user_id), NO_ID);
	}

	public static final void setTwitterUserId(int userId) {
		SharedPreferences sharedPref = BeaconApp.getInstance().getSharedPreferences(
				BeaconApp.getInstance().getString(R.string.twitter_pref), Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putInt(BeaconApp.getInstance().getString(R.string.twitter_user_id), userId);
		editor.commit();
	}
	
	public static final String getTwitterScreenName() {
		SharedPreferences sharedPref = BeaconApp.getInstance().getSharedPreferences(
				BeaconApp.getInstance().getString(R.string.twitter_pref), Context.MODE_PRIVATE);
		return sharedPref.getString(BeaconApp.getInstance().getString(R.string.twitter_username), "");
	}

	public static final void setTwitterScreenName(String userName) {
		SharedPreferences sharedPref = BeaconApp.getInstance().getSharedPreferences(
				BeaconApp.getInstance().getString(R.string.twitter_pref), Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putString(BeaconApp.getInstance().getString(R.string.twitter_username), userName);
		editor.commit();
	}

	public static final boolean isFirstRun() {
		SharedPreferences sharedPref = BeaconApp.getInstance().getSharedPreferences(
				BeaconApp.getInstance().getString(R.string.beacon_pref), Context.MODE_PRIVATE);
		return sharedPref.getBoolean(BeaconApp.getInstance().getString(R.string.first_time_run), true);
	}

	public static final void firstRunExecuted() {
		SharedPreferences sharedPref = BeaconApp.getInstance().getSharedPreferences(
				BeaconApp.getInstance().getString(R.string.beacon_pref), Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putBoolean(BeaconApp.getInstance().getString(R.string.first_time_run), false);
		editor.commit();
	}

	public static final boolean isSetupDone() {
		SharedPreferences sharedPref = BeaconApp.getInstance().getSharedPreferences(BeaconApp.getInstance().getString(R.string.beacon_pref), 
				Context.MODE_PRIVATE);
		return sharedPref.getBoolean(BeaconApp.getInstance().getString(R.string.is_setup_done), false);
	}

	public static final void setIsSetupDone(boolean isSetupDone) {
		SharedPreferences sharedPref = BeaconApp.getInstance().getSharedPreferences(
				BeaconApp.getInstance().getString(R.string.beacon_pref), Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putBoolean(BeaconApp.getInstance().getString(R.string.is_setup_done), isSetupDone);
		editor.commit();
	}
	
	private static final boolean checkIsTablet(){
		return BeaconApp.getInstance().getResources().getBoolean(R.bool.isTablet);
	}
	
	public static final boolean isTablet(){
		return sIsTablet;
	}
	
	public static final void setSmsInactiveTransmissionInterval(long interval){
		SharedPreferences sharedPref = BeaconApp.getInstance().getSharedPreferences(
				BeaconApp.getInstance().getString(R.string.beacon_pref), Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putLong(BeaconApp.getInstance().getString(R.string.beacon_sms_inactive_transmission_interval), interval);
		editor.commit();
	}
	
	public static final void setSmsActiveTransmissionInterval(long interval){
		SharedPreferences sharedPref = BeaconApp.getInstance().getSharedPreferences(
				BeaconApp.getInstance().getString(R.string.beacon_pref), Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putLong(BeaconApp.getInstance().getString(R.string.beacon_sms_active_transmission_interval), interval);
		editor.commit();
	}
	
	public static final long getSmsInactiveTransmissionInterval(){
		SharedPreferences sharedPref = BeaconApp.getInstance().getSharedPreferences(BeaconApp.getInstance().getString(R.string.beacon_pref), 
				Context.MODE_PRIVATE);
		return sharedPref.getLong(BeaconApp.getInstance().getString(R.string.beacon_sms_inactive_transmission_interval), 
				BeaconApp.getInstance().getResources().getInteger(R.integer.default_sms_inactive_transmission_interval));
	}
	
	public static final long getSmsActiveTransmissionInterval(){
		SharedPreferences sharedPref = BeaconApp.getInstance().getSharedPreferences(BeaconApp.getInstance().getString(R.string.beacon_pref), 
				Context.MODE_PRIVATE);
		return sharedPref.getLong(BeaconApp.getInstance().getString(R.string.beacon_sms_active_transmission_interval), 
				BeaconApp.getInstance().getResources().getInteger(R.integer.default_sms_active_transmission_interval));
	}
	
	public static final void setTwitterInactiveTransmissionInterval(long interval){
		SharedPreferences sharedPref = BeaconApp.getInstance().getSharedPreferences(
				BeaconApp.getInstance().getString(R.string.beacon_pref), Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putLong(BeaconApp.getInstance().getString(R.string.beacon_twitter_inactive_transmission_interval), interval);
		editor.commit();
	}
	
	public static final void setTwitterActiveTransmissionInterval(long interval){
		SharedPreferences sharedPref = BeaconApp.getInstance().getSharedPreferences(
				BeaconApp.getInstance().getString(R.string.beacon_pref), Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putLong(BeaconApp.getInstance().getString(R.string.beacon_twitter_active_transmission_interval), interval);
		editor.commit();
	}
	
	public static final long getTwitterInactiveTransmissionInterval(){
		SharedPreferences sharedPref = BeaconApp.getInstance().getSharedPreferences(BeaconApp.getInstance().getString(R.string.beacon_pref), 
				Context.MODE_PRIVATE);
		return sharedPref.getLong(BeaconApp.getInstance().getString(R.string.beacon_twitter_inactive_transmission_interval), 
				BeaconApp.getInstance().getResources().getInteger(R.integer.default_twitter_inactive_transmission_interval));
	}
	
	public static final long getTwitterActiveTransmissionInterval(){
		SharedPreferences sharedPref = BeaconApp.getInstance().getSharedPreferences(BeaconApp.getInstance().getString(R.string.beacon_pref), 
				Context.MODE_PRIVATE);
		return sharedPref.getLong(BeaconApp.getInstance().getString(R.string.beacon_twitter_active_transmission_interval), 
				BeaconApp.getInstance().getResources().getInteger(R.integer.default_twitter_active_transmission_interval));
	}
	
	public static final long getActiveTransmissionInterval(){
		if(IS_DEBUG){
			return (15 * ChronoUtils.ONE_SECOND); //15 seconds
		}
		
		return 15 * ChronoUtils.ONE_MINUTE; //15 minutes
	}
	
	public static final long getInactiveTransmissionInterval(){
		
		if(IS_DEBUG){
			return (30 * ChronoUtils.ONE_SECOND); //30 seconds
		}
		
		return 1 * ChronoUtils.ONE_HOUR; //One Hour
	}

	public static boolean hasNetworkConnectivity() {
		return sHasNetworkConnectivity;
	}

	public static void setHasNetworkConnectivity(boolean hasNetworkConnectivity) {
		sHasNetworkConnectivity = hasNetworkConnectivity;
	}
	
	public static boolean isGpsOnline() {
		return sIsGpsOnline;
	}

	public static void setGpsOnline(boolean isGpsOnline) {
		sIsGpsOnline = isGpsOnline;
	}

	public static final boolean hasSmsCapability(){
		return sHasSmsCapability;
	}
	
	public static final boolean hasGpsCapability(){
		return sHasGpsCapability;
	}
	
	public static final boolean hasLocationCapability(){
		return sHasLocationCapability;
	}
	
	public static final boolean hasNetworkLocationCapability(){
		return sHasNetworkLocationCapability;
	}
	
}
