package com.google.android.location;

import android.location.Location;
import android.os.SystemClock;
import android.util.Log;

public class NetworkLocationRetriever extends Thread {

	private final static String TAG = "NetworkLocationRetriever";
	private boolean active;

	private long autoTime;
	private boolean autoUpdate;
	private LocationData data;
	private boolean enabled;
	private Location lastLocation;
	private long lastTime;

	public NetworkLocationRetriever() {
		active = true;
		autoUpdate = false;
		autoTime = Long.MAX_VALUE;
		lastTime = 0;
		enabled = true;
	}

	public void disable() {
		enabled = false;
		synchronized (this) {
			notify();
		}
	}

	public long getLastTime() {
		return lastTime;
	}

	@Override
	public void run() {
		while (enabled) {
			boolean waited = false;
			if (!active) {
				Log.d(TAG, "waiting till notified...");
				try {
					synchronized (this) {
						wait();
					}
					waited = true;
				} catch (final InterruptedException e) {
					Log.w(TAG, "got interrupt!", e);
				}
			}
			if (!autoUpdate && active) {
				Log.d(TAG, "waiting max 60s to update...");
				try {
					synchronized (this) {
						wait(60000);
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
					synchronized (this) {
						wait(wait);
					}
					waited = true;
				} catch (final InterruptedException e) {
					Log.w(TAG, "got interrupt!", e);
				}
			}
			if (!waited) {
				Log.d(TAG, "waiting min 1s to prevent mass update...");
				try {
					synchronized (this) {
						wait(1000);
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

	public void setActive(final boolean bool) {
		active = true;
		synchronized (this) {
			notify();
		}
	}

	public void setAuto(final boolean autoUpdate, final long autoTime) {
		this.autoUpdate = autoUpdate;
		this.autoTime = autoTime;
		synchronized (this) {
			notify();
		}
	}

	public void setData(final LocationData data) {
		this.data = data;
	}

	public void setLastLocation(final Location location) {
		lastLocation = location;
	}

	public void setLastTime(final long lastTime) {
		this.lastTime = lastTime;
	}

}
