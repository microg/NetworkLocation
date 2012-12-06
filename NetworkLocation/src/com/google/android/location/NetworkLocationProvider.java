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
		LocationBinder, Runnable {

	private static final String TAG = "NetworkLocationProvider";

	private boolean active;
	private long autoTime;
	private boolean autoUpdate;
	private final Thread background;

	private LocationData data;

	private Location lastLocation;

	private long lastTime;

	public NetworkLocationProvider() {
		Log.d(TAG, "new Provider-Object constructed");
		autoTime = Long.MAX_VALUE;
		autoUpdate = false;
		lastTime = 0;
		active = true;
		background = new Thread(this);
		background.start();
	}

	public NetworkLocationProvider(final LocationData data) {
		this();
		this.data = data;
	}

	@Override
	public void onAddListener(final int uid, final WorkSource ws) {
	}

	@Override
	public void onDisable() {
		active = false;
		synchronized (background) {
			background.notify();
		}
	}

	@Override
	public void onEnable() {
		active = true;
		synchronized (background) {
			background.notify();
		}
	}

	@Override
	public void onEnableLocationTracking(final boolean enable) {
		autoUpdate = enable;
		if (autoUpdate) {
			synchronized (background) {
				background.notify();
			}
		}
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
		return lastTime;
	}

	@Override
	public boolean onHasMonetaryCost() {
		return false;
	}

	@Override
	public void onLocationChanged(final Location location) {
		Log.i(TAG, "onLocationChanged: " + location);
		if (location != null) {
			lastTime = SystemClock.elapsedRealtime();
			lastLocation = location;
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
		Log.d(TAG, "onSetMinTime: " + minTime);
		autoTime = minTime;
		synchronized (background) {
			background.notify();
		}
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
		lastLocation = location;
	}

	@Override
	public void onUpdateNetworkState(final int state, final NetworkInfo info) {
		Log.d(TAG, "onUpdateNetworkState: " + state + " (" + info + ")");
	}

	@Override
	public void run() {
		while (background != null) {
			boolean waited = false;
			if (!active) {
				Log.d(TAG, "waiting till notified...");
				try {
					synchronized (background) {
						background.wait();
					}
					waited = true;
				} catch (final InterruptedException e) {
					Log.w(TAG, "got interrupt!", e);
				}
			}
			if (!autoUpdate && active) {
				Log.d(TAG, "waiting max 60s to update...");
				try {
					synchronized (background) {
						background.wait(60000);
					}
					waited = true;
				} catch (final InterruptedException e) {
					Log.w(TAG, "got interrupt!", e);
				}
			}
			long wait;
			while ((wait = lastTime + autoTime - SystemClock.elapsedRealtime()) > 0
					&& autoUpdate && lastLocation != null) {
				final float w = wait / 1000F;
				Log.d(TAG, "waiting max " + w + "s to update...");
				try {
					synchronized (background) {
						background.wait(wait);
					}
					waited = true;
				} catch (final InterruptedException e) {
					Log.w(TAG, "got interrupt!", e);
				}
			}
			if (!waited) {
				Log.d(TAG, "waiting max 1s to prevent mass update...");
				try {
					synchronized (background) {
						background.wait(1000);
					}
					waited = true;
				} catch (final InterruptedException e) {
					Log.w(TAG, "got interrupt!", e);
				}
			}
			if (active) {
				Log.d(TAG, "recieving new location...");
				data.getCurrentLocation();
			} else {
				Log.d(TAG, "we're not active = do not track!");
			}
		}
	}

	@Override
	public void setData(final LocationData data) {
		this.data = data;
	}
}
