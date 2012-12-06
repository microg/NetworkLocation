package com.google.android.location;

import android.location.Criteria;
import android.location.Location;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.WorkSource;
import android.util.Log;

import com.android.location.provider.LocationProviderBase;
import com.android.location.provider.ProviderPropertiesUnbundled;
import com.android.location.provider.ProviderRequestUnbundled;

public class NetworkLocationProviderV2 extends LocationProviderBase implements
		LocationBinder, Runnable {

	private static final String TAG = "NetworkLocationProviderV2";

	private boolean active;
	private final long autoTime;
	private final boolean autoUpdate;
	private final Thread background;
	private LocationData data;
	private Location lastLocation;
	private long lastTime;

	public NetworkLocationProviderV2() {
		super(TAG, ProviderPropertiesUnbundled.create(true, false, true, false,
				false, false, false, Criteria.POWER_LOW,
				Criteria.ACCURACY_COARSE));
		active = true;
		autoUpdate = false;
		autoTime = Long.MAX_VALUE;
		lastTime = 0;
		background = new Thread(this);
		background.start();
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
	public int onGetStatus(final Bundle arg0) {
		return android.location.LocationProvider.AVAILABLE;
	}

	@Override
	public long onGetStatusUpdateTime() {
		return lastTime;
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
	public void onProviderDisabled(final String provider) {
	}

	@Override
	public void onProviderEnabled(final String provider) {
	}

	@Override
	public void onSetRequest(final ProviderRequestUnbundled arg0,
			final WorkSource arg1) {
		// TODO Auto-generated method stub
		Log.w(TAG,
				"Not yet implemented: NetworkLocationProviderV2.onSetRequest");
	}

	@Override
	public void onStatusChanged(final String provider, final int status,
			final Bundle extras) {
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
