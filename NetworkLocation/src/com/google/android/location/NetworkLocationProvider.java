package com.google.android.location;

import android.location.Criteria;
import android.location.Location;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.WorkSource;
import android.util.Log;

import com.android.location.provider.LocationProvider;

public class NetworkLocationProvider extends LocationProvider implements
		NetworkLocationProviderBase {

	private static final String IDENTIFIER = "network";
	private static final String TAG = "NetworkLocationProvider";

	private long autoTime;
	private boolean autoUpdate;
	private final NetworkLocationRetriever background;

	private final boolean internal;

	public NetworkLocationProvider() {
		this(false);
	}

	public NetworkLocationProvider(boolean internal) {
		Log.d(TAG, "new Provider-Object constructed");
		autoUpdate = false;
		autoTime = Long.MAX_VALUE;
		background = new NetworkLocationRetriever();
		background.start();
		this.internal = internal;
	}

	public NetworkLocationProvider(final LocationData data) {
		this();
		background.setData(data);
	}

	@Override
	public void onAddListener(final int uid, final WorkSource ws) {
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
	public void onEnableLocationTracking(final boolean enable) {
		autoUpdate = enable;
		background.setAuto(autoUpdate, autoTime);
	}

	@Override
	public int onGetAccuracy() {
		return Criteria.ACCURACY_COARSE;
	}

	@Override
	public String onGetInternalState() {
		Log.w(TAG,
				"Internal State not yet implemented. The application may not work.");
		return "[INTERNAL STATE NOT IMPLEMENTED]";
	}

	@Override
	public int onGetPowerRequirement() {
		return Criteria.POWER_LOW;
	}

	@Override
	public int onGetStatus(final Bundle extras) {
		return android.location.LocationProvider.AVAILABLE;
	}

	@Override
	public long onGetStatusUpdateTime() {
		return background.getLastTime();
	}

	@Override
	public boolean onHasMonetaryCost() {
		return false;
	}

	@Override
	public void onLocationChanged(Location location) {
		if (location != null) {
			background.setLastTime(SystemClock.elapsedRealtime());
			if (internal) {
				location = LocationDataProvider.Stub.renameSource(location,
						IDENTIFIER);
			}
			background.setLastLocation(location);
			reportLocation(location);
		}
	}

	@Override
	public boolean onMeetsCriteria(final Criteria criteria) {
		if (criteria.getAccuracy() == Criteria.ACCURACY_FINE) {
			return false;
		}
		if (criteria.isAltitudeRequired()) {
			return false;
		}
		if (criteria.isSpeedRequired()) {
			return false;
		}
		return true;
	}

	@Override
	public void onProviderDisabled(final String provider) {
	}

	@Override
	public void onProviderEnabled(final String provider) {
	}

	@Override
	public void onRemoveListener(final int uid, final WorkSource ws) {
	}

	@Override
	public boolean onRequiresCell() {
		return true;
	}

	@Override
	public boolean onRequiresNetwork() {
		return true;
	}

	@Override
	public boolean onRequiresSatellite() {
		return false;
	}

	@Override
	public boolean onSendExtraCommand(final String command, final Bundle extras) {
		return false;
	}

	@Override
	public void onSetMinTime(final long minTime, final WorkSource ws) {
		autoTime = minTime;
		background.setAuto(autoUpdate, autoTime);
	}

	@Override
	public void onStatusChanged(final String provider, final int status,
			final Bundle extras) {
	}

	@Override
	public boolean onSupportsAltitude() {
		return false;
	}

	@Override
	public boolean onSupportsBearing() {
		return true;
	}

	@Override
	public boolean onSupportsSpeed() {
		return false;
	}

	@Override
	public void onUpdateLocation(final Location location) {
		background.setLastLocation(location);
	}

	@Override
	public void onUpdateNetworkState(final int state, final NetworkInfo info) {
		Log.d(TAG, "onUpdateNetworkState: " + state + " (" + info + ")");
	}

	@Override
	public void setData(final LocationData data) {
		background.setData(data);
	}

}
