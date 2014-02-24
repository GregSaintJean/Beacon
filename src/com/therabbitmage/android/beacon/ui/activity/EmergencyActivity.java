package com.therabbitmage.android.beacon.ui.activity;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.therabbitmage.android.beacon.BeaconApp;
import com.therabbitmage.android.beacon.R;
import com.therabbitmage.android.beacon.utils.LocationUtils;

public class EmergencyActivity extends FragmentActivity {

	private static final String NO_PROVIDER = "no_provider";

	private LocationManager mLocationManager;
	private Location mBestLocation;
	private String mCurrentProvider;
	private GoogleMap mGoogleMap;
	private boolean runPeriodicUpdates;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.emergency_activity);
		runPeriodicUpdates = true;

		mLocationManager = ((BeaconApp) getApplicationContext())
				.getLocationManager();

		if (mGoogleMap == null) {
			mGoogleMap = ((SupportMapFragment) getSupportFragmentManager()
					.findFragmentById(R.id.map)).getMap();
			mGoogleMap.setMyLocationEnabled(true);

			startUpdates();
		}
	}

	public void startUpdates() {
		if (LocationUtils.hasGPSLocationProvider(this)) {
			mLocationManager.requestLocationUpdates(
					LocationManager.GPS_PROVIDER, LocationUtils.TWO_MINUTES,
					LocationUtils.FIVE_METERS, mLocationListener);
		} else if (LocationUtils.hasNetworkLocationProvider(this)) {
			mLocationManager.requestLocationUpdates(
					LocationManager.NETWORK_PROVIDER,
					LocationUtils.TWO_MINUTES, LocationUtils.FIVE_METERS,
					mLocationListener);
		}
	}

	public void stopUpdates() {
		mLocationManager.removeUpdates(mLocationListener);
	}

	private final LocationListener mLocationListener = new LocationListener() {

		@Override
		public void onLocationChanged(Location location) {
			if (LocationUtils.isBetterLocation(location, mBestLocation)) {
				mBestLocation = location;
				CameraUpdate update = CameraUpdateFactory.newLatLng(new LatLng(
						mBestLocation.getLatitude(), mBestLocation
								.getLongitude()));
				mGoogleMap.animateCamera(update);
			}
		}

		@Override
		public void onProviderDisabled(String provider) {

			if (runPeriodicUpdates) {
				if (provider.equals(LocationManager.GPS_PROVIDER)) {

					if (mLocationManager
							.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
						mLocationManager.removeUpdates(this);
						mLocationManager.requestLocationUpdates(
								LocationManager.NETWORK_PROVIDER,
								LocationUtils.TWO_MINUTES,
								LocationUtils.FIVE_METERS, this);
						mCurrentProvider = LocationManager.NETWORK_PROVIDER;
					} else {
						mLocationManager.removeUpdates(this);
						mCurrentProvider = NO_PROVIDER;
						// TODO Signal a alert dialog to get GPS.
					}

				} else if (provider.equals(LocationManager.NETWORK_PROVIDER)) {

					if (mLocationManager
							.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
						mLocationManager.removeUpdates(this);
						mLocationManager.requestLocationUpdates(
								LocationManager.GPS_PROVIDER,
								LocationUtils.TWO_MINUTES,
								LocationUtils.FIVE_METERS, this);
						mCurrentProvider = LocationManager.GPS_PROVIDER;
					} else {
						mLocationManager.removeUpdates(this);
						mCurrentProvider = NO_PROVIDER;
						// TODO Signal a alert dialog to get GPS.
					}
				}
			} else {
				mCurrentProvider = NO_PROVIDER;
			}

		}

		@Override
		public void onProviderEnabled(String provider) {

			if (runPeriodicUpdates) {
				if (provider.equals(LocationManager.GPS_PROVIDER)) {

					if (mCurrentProvider
							.equals(LocationManager.NETWORK_PROVIDER)
							|| mCurrentProvider.equals(NO_PROVIDER)) {
						if (!mCurrentProvider.equals(NO_PROVIDER)) {
							mLocationManager.removeUpdates(this);
						}
						mLocationManager.requestLocationUpdates(provider,
								LocationUtils.TWO_MINUTES,
								LocationUtils.FIVE_METERS, this);
						mCurrentProvider = provider;
					}

				} else if (provider.equals(LocationManager.NETWORK_PROVIDER)) {

					if (mCurrentProvider.equals(LocationManager.GPS_PROVIDER)) {

					} else if (mCurrentProvider.equals(NO_PROVIDER)) {
						mLocationManager.requestLocationUpdates(provider,
								LocationUtils.TWO_MINUTES,
								LocationUtils.FIVE_METERS, this);
						mCurrentProvider = provider;
					}

				}
			} else {
				mCurrentProvider = NO_PROVIDER;
			}

		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Might need to do this to keep things smooth
		}

	};

}
