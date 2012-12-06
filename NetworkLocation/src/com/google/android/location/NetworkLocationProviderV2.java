package com.google.android.location;

import android.location.Criteria;
import android.location.Location;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.WorkSource;
import android.util.Log;

import com.android.location.provider.LocationProviderBase;
import com.android.location.provider.LocationRequestUnbundled;
import com.android.location.provider.ProviderPropertiesUnbundled;
import com.android.location.provider.ProviderRequestUnbundled;

public class NetworkLocationProviderV2 extends LocationProviderBase implements
		NetworkLocationProviderBase {

	private static final String TAG = "NetworkLocationProviderV2";

	private final NetworkLocationRetriever background;

	public NetworkLocationProviderV2() {
		super(TAG, ProviderPropertiesUnbundled.create(true, false, true, false,
				false, false, false, Criteria.POWER_LOW,
				Criteria.ACCURACY_COARSE));
		background = new NetworkLocationRetriever();
		background.start();
	}

	@Override
	public void onDisable() {
		background.setActive(false);
	}

	@Override
	public void onEnable() {
		background.setActive(true);
	}

	@Override
	public int onGetStatus(final Bundle arg0) {
		return android.location.LocationProvider.AVAILABLE;
	}

	@Override
	public long onGetStatusUpdateTime() {
		return background.getLastTime();
	}

	@Override
	public void onLocationChanged(final Location location) {
		Log.i(TAG, "onLocationChanged: " + location);
		if (location != null) {
			background.setLastTime(SystemClock.elapsedRealtime());
			background.setLastLocation(location);
			location.makeComplete();
			reportLocation(location);
		}
	}

	@Override
	public void onProviderDisabled(final String provider) {
	}

	@Override
	public void onProviderEnabled(final String provider) {
	}

	@Override
	public void onSetRequest(final ProviderRequestUnbundled requests,
			final WorkSource ws) {
		long autoTime = Long.MAX_VALUE;
		boolean autoUpdate = false;
		for (final LocationRequestUnbundled request : requests
				.getLocationRequests()) {
			if (request.getInterval() < autoTime) {
				autoTime = request.getInterval();
			}
			autoUpdate = true;
		}
		background.setAuto(autoUpdate, autoTime);
	}

	@Override
	public void onStatusChanged(final String provider, final int status,
			final Bundle extras) {
	}

	@Override
	public void setData(final LocationData data) {
		background.setData(data);
	}

}
