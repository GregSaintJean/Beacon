package com.therabbitmage.android.beacon.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;

public class LocationUtils {

	public static final float FIVE_METERS = 5;
	
	// Milliseconds per second
	private static final int MILLISECONDS_PER_SECOND = 1000;
	
	// Update frequency in seconds
	public static final int UPDATE_INTERVAL_IN_SECONDS = 20;
	
	// Update frequency in milliseconds
	public static final long UPDATE_INTERVAL = MILLISECONDS_PER_SECOND
			* UPDATE_INTERVAL_IN_SECONDS;
	
	// The fastest update frequency, in seconds
	private static final int FASTEST_INTERVAL_IN_SECONDS = 10;
	
	// A fast frequency ceiling in milliseconds
	public static final long FASTEST_INTERVAL = MILLISECONDS_PER_SECOND
			* FASTEST_INTERVAL_IN_SECONDS;
	
	private static final int ONE_MINUTE = 60;

	public static final long TWO_MINUTES = MILLISECONDS_PER_SECOND * ONE_MINUTE
			* 2;

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

		// Check whether the new location fix is newer or older
		long timeDelta = location.getTime() - currentBestLocation.getTime();
		boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
		boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
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
