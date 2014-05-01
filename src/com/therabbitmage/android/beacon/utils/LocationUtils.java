package com.therabbitmage.android.beacon.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;

public class LocationUtils {
	
	public static final float FIVE_METERS = 5;

	/**
	 * Determines whether one Location reading is better than the current
	 * Location fix
	 * 
	 * @param location
	 *            The new Location that you want to evaluate
	 * @param currentBestLocation
	 *            The current Location fix, to which you want to compare the new
	 *            one
	 */
	public static boolean isBetterLocation(Location location,
			Location currentBestLocation) {
		if (currentBestLocation == null) {
			// A new location is always better than no location
			return true;
		}
		
		if(location == currentBestLocation){
			return false;
		}

		// Check whether the new location fix is newer or older
		long timeDelta = location.getTime() - currentBestLocation.getTime();
		boolean isSignificantlyNewer = timeDelta > (2 * ChronoUtils.ONE_MINUTE);
		boolean isSignificantlyOlder = timeDelta < -(2 * ChronoUtils.ONE_MINUTE);
		boolean isNewer = timeDelta > 0;

		// If it's been more than two minutes since the current location, use
		// the new location
		// because the user has likely moved
		if (isSignificantlyNewer) {
			return true;
			// If the new location is more than two minutes older, it must be
			// worse
		} else if (isSignificantlyOlder) {
			return false;
		}

		// Check whether the new location fix is more or less accurate
		int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation
				.getAccuracy());
		boolean isLessAccurate = accuracyDelta > 0;
		boolean isMoreAccurate = accuracyDelta < 0;
		boolean isSignificantlyLessAccurate = accuracyDelta > 200;

		// Check if the old and new location are from the same provider
		boolean isFromSameProvider = isSameProvider(location.getProvider(),
				currentBestLocation.getProvider());

		// Determine location quality using a combination of timeliness and
		// accuracy
		if (isMoreAccurate) {
			return true;
		} else if (isNewer && !isLessAccurate) {
			return true;
		} else if (isNewer && !isSignificantlyLessAccurate
				&& isFromSameProvider) {
			return true;
		}
		return false;
	}

	/** Checks whether two providers are the same */
	public static boolean isSameProvider(String provider1, String provider2) {
		if (provider1 == null) {
			return provider2 == null;
		}
		return provider1.equals(provider2);
	}

	public final static boolean hasGPSLocationProvider(Context ctx) {
		PackageManager manager = ctx.getPackageManager();

		if (manager.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS)) {
			return true;
		}

		return false;
	}

	public final static boolean hasNetworkLocationProvider(Context ctx) {
		PackageManager manager = ctx.getPackageManager();

		if (manager.hasSystemFeature(PackageManager.FEATURE_LOCATION_NETWORK)) {
			return true;
		}

		return false;
	}

	public final static boolean hasLocationProvider(Context ctx) {
		PackageManager manager = ctx.getPackageManager();
		if (manager.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS)
				|| manager
						.hasSystemFeature(PackageManager.FEATURE_LOCATION_NETWORK)) {
			return true;
		}

		return false;
	}

}
