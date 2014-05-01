package com.therabbitmage.android.beacon;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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

import com.therabbitmage.android.beacon.utils.AndroidUtils;
import com.therabbitmage.android.beacon.utils.ChronoUtils;

public class BeaconApp extends Application {
	private static final String TAG = BeaconApp.class.getSimpleName();
	
	private static BeaconApp sBeacon;

	private static final int NO_ID = -1;
	
	//Should Beacon be online?
	private static boolean sIsBeaconOnline;
	
	//Is Beacon currently transmitting?
	private static boolean sIsBeaconActive;
	private static boolean sIsBeaconMoving;
	
	private static boolean sHasNetworkConnectivity;
	private static boolean sIsGpsOnline;
	
	private static boolean mIsTablet;
	private static boolean mHasSmsCapability;
	private static boolean mHasLocationCapability;
	private static boolean mHasNetworkLocationCapability;
	private static boolean mHasGpsCapability;

	@Override
	public void onCreate() {
		super.onCreate();
		
		sBeacon = this;
		
		if(BuildConfig.DEBUG){
			printDebugKeyHash();
		}
		
		mIsTablet = checkIsTablet();
		mHasSmsCapability = AndroidUtils.checkPhoneAndSmsCapability(this);
		mHasGpsCapability = AndroidUtils.checkGpsCapability(this);
		mHasLocationCapability = AndroidUtils.checkLocationCapability(this);
		mHasNetworkLocationCapability = AndroidUtils.checkNetworkLocationCapability(this);

	}
	
	public static void setBeaconStatus(boolean isBeaconOnline){
		sIsBeaconOnline = isBeaconOnline;
	}
	
	public static boolean isBeaconOnline(){
		return sIsBeaconOnline;
	}
	
	public static boolean isActive() {
		return sIsBeaconActive;
	}

	public static void setActive(boolean isActive) {
		BeaconApp.sIsBeaconActive = isActive;
	}

	public static boolean isBeaconMoving() {
		return sIsBeaconMoving;
	}

	public static void setBeaconMoving(boolean isBeaconMoving) {
		BeaconApp.sIsBeaconMoving = isBeaconMoving;
	}

	public static final BeaconApp getInstance(){
		return sBeacon;
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
	
	private final boolean checkIsTablet(){
		return getResources().getBoolean(R.bool.isTablet);
	}
	
	public final boolean isTablet(){
		return mIsTablet;
	}
	
	public final void setSmsInactiveTransmissionInterval(long interval){
		SharedPreferences sharedPref = getSharedPreferences(
				getString(R.string.beacon_pref), Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putLong(getString(R.string.beacon_sms_inactive_transmission_interval), interval);
		editor.commit();
	}
	
	public final void setSmsActiveTransmissionInterval(long interval){
		SharedPreferences sharedPref = getSharedPreferences(
				getString(R.string.beacon_pref), Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putLong(getString(R.string.beacon_sms_active_transmission_interval), interval);
		editor.commit();
	}
	
	public final long getSmsInactiveTransmissionInterval(){
		SharedPreferences sharedPref = getSharedPreferences(getString(R.string.beacon_pref), 
				Context.MODE_PRIVATE);
		return sharedPref.getLong(getString(R.string.beacon_sms_inactive_transmission_interval), 
				getResources().getInteger(R.integer.default_sms_inactive_transmission_interval));
	}
	
	public final long getSmsActiveTransmissionInterval(){
		SharedPreferences sharedPref = getSharedPreferences(getString(R.string.beacon_pref), 
				Context.MODE_PRIVATE);
		return sharedPref.getLong(getString(R.string.beacon_sms_active_transmission_interval), 
				getResources().getInteger(R.integer.default_sms_active_transmission_interval));
	}
	
	public final void setTwitterInactiveTransmissionInterval(long interval){
		SharedPreferences sharedPref = getSharedPreferences(
				getString(R.string.beacon_pref), Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putLong(getString(R.string.beacon_twitter_inactive_transmission_interval), interval);
		editor.commit();
	}
	
	public final void setTwitterActiveTransmissionInterval(long interval){
		SharedPreferences sharedPref = getSharedPreferences(
				getString(R.string.beacon_pref), Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putLong(getString(R.string.beacon_twitter_active_transmission_interval), interval);
		editor.commit();
	}
	
	public final long getTwitterInactiveTransmissionInterval(){
		SharedPreferences sharedPref = getSharedPreferences(getString(R.string.beacon_pref), 
				Context.MODE_PRIVATE);
		return sharedPref.getLong(getString(R.string.beacon_twitter_inactive_transmission_interval), 
				getResources().getInteger(R.integer.default_twitter_inactive_transmission_interval));
	}
	
	public final long getTwitterActiveTransmissionInterval(){
		SharedPreferences sharedPref = getSharedPreferences(getString(R.string.beacon_pref), 
				Context.MODE_PRIVATE);
		return sharedPref.getLong(getString(R.string.beacon_twitter_active_transmission_interval), 
				getResources().getInteger(R.integer.default_twitter_active_transmission_interval));
	}
	
	public final void setNextActiveTransmissionInterval(long interval){
		//TODO Implement
	}
	
	public final void setNextInactiveTransmissionInterval(long interval){
		//TODO Implement
	}
	
	public final long getActiveTransmissionInterval(){
		return 12 * ChronoUtils.ONE_MINUTE;
	}
	
	public final long getInactiveTransmissionInterval(){
		return 20 * ChronoUtils.ONE_MINUTE;
	}

	public static boolean hasNetworkConnectivity() {
		return sHasNetworkConnectivity;
	}

	public void setHasNetworkConnectivity(boolean hasNetworkConnectivity) {
		sHasNetworkConnectivity = hasNetworkConnectivity;
	}
	
	public static boolean isGpsOnline() {
		return sIsGpsOnline;
	}

	public void setGpsOnline(boolean isGpsOnline) {
		sIsGpsOnline = isGpsOnline;
	}

	public final boolean hasSmsCapability(){
		return mHasSmsCapability;
	}
	
	public final boolean hasGpsCapability(){
		return mHasGpsCapability;
	}
	
	public final boolean hasLocationCapability(){
		return mHasLocationCapability;
	}
	
	public final boolean hasNetworkLocationCapability(){
		return mHasNetworkLocationCapability;
	}
	
}
