package com.google.android.location;

import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.WorkSource;
import android.util.Log;

import com.android.location.provider.LocationProvider;

public class NetworkLocationProvider extends LocationProvider implements
		LocationListener, Runnable {

	private static final String TAG = NetworkLocationProvider.class.getName();

	private LocationData data;
	private Location lastLocation;
	private long lastTime;
	private boolean autoUpdate;

	private long autoTime;

	private final Thread background;

	private boolean active;

	public NetworkLocationProvider() {
		Log.d(TAG, "new Provider-Object constructed");
		autoTime = Long.MAX_VALUE;
		autoUpdate = false;
		background = new Thread(this);
		background.start();
		lastTime = 0;
		active = true;
	}

	public NetworkLocationProvider(LocationData data) {
		this();
		this.data = data;
	}

	@Override
	public void onAddListener(int uid, WorkSource ws) {
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
	public void onEnableLocationTracking(boolean enable) {
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
	public int onGetStatus(Bundle extras) {
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
	public void onLocationChanged(Location location) {
		Log.i(TAG, "onLocationChanged: " + location);
		if (location != null) {
			lastTime = SystemClock.elapsedRealtime();
			lastLocation = location;
			reportLocation(location);
		}
	}

	@Override
	public boolean onMeetsCriteria(Criteria criteria) {
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
	public void onProviderDisabled(String provider) {
		Log.d(TAG, "onProviderDisabled: " + provider);
	}

	@Override
	public void onProviderEnabled(String provider) {
		Log.d(TAG, "onProviderEnabled: " + provider);
	}

	@Override
	public void onRemoveListener(int uid, WorkSource ws) {
		Log.d(TAG, "onRemoveListener: " + uid);
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
	public boolean onSendExtraCommand(String command, Bundle extras) {
		Log.d(TAG, "onSendExtraCommand: " + command);
		return false;
	}

	@Override
	public void onSetMinTime(long minTime, WorkSource ws) {
		Log.d(TAG, "onSetMinTime: " + minTime);
		autoTime = minTime;
		synchronized (background) {
			background.notify();
		}
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		Log.d(TAG, "onStatusChanged: " + provider + " > " + status);
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
	public void onUpdateLocation(Location location) {
		lastLocation = location;
	}

	@Override
	public void onUpdateNetworkState(int state, NetworkInfo info) {
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

	public void setData(LocationData data) {
		this.data = data;
	}

}
