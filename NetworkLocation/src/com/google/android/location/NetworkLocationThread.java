package com.google.android.location;

import android.location.Location;
import android.os.SystemClock;
import android.util.Log;

import com.google.android.location.data.LocationData;

public class NetworkLocationThread extends Thread {

	private final static String TAG = "NetworkLocationRetriever";
	private boolean active;

	private long autoTime;
	private boolean autoUpdate;
	private LocationData data;
	private boolean enabled;
	private Location lastLocation;
	private long lastTime;

	public NetworkLocationThread() {
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

	public boolean isActive() {
		return enabled && autoUpdate && autoTime < 60000;
	}

	@Override
	public void run() {
		while (enabled) {
			if (data != null && data.getService() != null) {
				data.getService().reInitOverlayNotification();
			}
			boolean waited = false;
			if (!active) {
				try {
					synchronized (this) {
						wait();
					}
					waited = true;
				} catch (final InterruptedException e) {
				}
			}
			if (!autoUpdate && active) {
				try {
					synchronized (this) {
						wait(60000);
					}
					waited = true;
				} catch (final InterruptedException e) {
				}
			}
			long wait;
			while ((wait = lastTime + autoTime - SystemClock.elapsedRealtime()) > 0
					&& autoUpdate && lastLocation != null) {
				final float w = wait / 1000F;
				Log.d(TAG, "waiting " + w + "s to update...");
				try {
					synchronized (this) {
						wait(wait);
					}
					waited = true;
				} catch (final InterruptedException e) {
					break;
				}
			}

			if (!waited) {
				Log.d(TAG, "waiting min 5s to prevent mass update...");
				try {
					synchronized (this) {
						wait(5000);
					}
					waited = true;
				} catch (final InterruptedException e) {
					break;
				}
			}
			if (active && data != null) {
				data.getCurrentLocation();
			} else {
				Log.d(TAG,
						"we're not active (or not initialized yet) = do not track!");
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
