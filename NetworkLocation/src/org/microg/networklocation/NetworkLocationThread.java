package org.microg.networklocation;

import android.location.Location;
import android.os.SystemClock;
import android.util.Log;
import org.microg.networklocation.data.LocationData;

public class NetworkLocationThread extends Thread {

	private final static String TAG = "NetworkLocationThread";
	private long autoTime;
	private boolean autoUpdate;
	private boolean forceUpdate;
	private LocationData data;
	private boolean enabled;
	private Location lastLocation;
	private long lastTime;

	public NetworkLocationThread() {
		autoUpdate = false;
		autoTime = Long.MAX_VALUE;
		lastTime = 0;
		enabled = true;
		forceUpdate = true;
	}

	public NetworkLocationThread(NetworkLocationThread oldThread) {
		this();
		if (oldThread != null) {
			lastLocation = oldThread.lastLocation;
			lastTime = oldThread.lastTime;
			data = oldThread.data;
		}
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

	public void setLastTime(final long lastTime) {
		this.lastTime = lastTime;
	}

	public boolean isActive() {
		return enabled && autoUpdate && autoTime < 60000;
	}

	@Override
	public void run() {
		while (enabled) {
			boolean waited = false;
			try {
				synchronized (this) {
					if (!autoUpdate && !forceUpdate && enabled) {
						if (MainService.DEBUG)
							Log.d(TAG, "We're not active, wait until we are...");
						wait();
						waited = true;
					}
				}
			} catch (final InterruptedException e) {
			}
			long wait;
			while ((wait = lastTime + autoTime - SystemClock.elapsedRealtime()) > 0 && autoUpdate && !forceUpdate &&
				   enabled) {
				final float w = wait / 1000F;
				if (MainService.DEBUG) {
					Log.d(TAG, "lastTime: " + lastTime + " autoTime: " + autoTime + " currentTime: " +
							   SystemClock.elapsedRealtime());
					Log.d(TAG, "waiting " + w + "s to update...");
				}
				try {
					synchronized (this) {
						wait(wait);
					}
					waited = true;
				} catch (final InterruptedException e) {
					break;
				}
			}

			if (!waited && enabled) {
				if (MainService.DEBUG) {
					Log.d(TAG, "We did not wait, lastTime: " + lastTime + " autoTime: " + autoTime + " currentTime: " +
							   SystemClock.elapsedRealtime());
					Log.w(TAG, "waiting 5s to prevent mass update...");
				}
				try {
					synchronized (this) {
						wait(5000);
					}
				} catch (final InterruptedException e) {
					continue;
				}
			}
			if ((autoUpdate || forceUpdate) && data != null && enabled) {
				if (forceUpdate) {
					if (MainService.DEBUG)
						Log.d(TAG, "Update forced because of new incoming request");
					forceUpdate = false;
				}
				if (MainService.DEBUG)
					Log.d(TAG, "Now requesting \\o/");
				lastTime = SystemClock.elapsedRealtime();
				data.getCurrentLocation();
			} else {
				if (MainService.DEBUG)
					Log.d(TAG, "we're not active (or not initialized yet) = do not track!");
			}
		}
	}

	public void setAuto(final boolean autoUpdate, final long autoTime) {
		synchronized (this) {
			if (autoTime < this.autoTime) {
				forceUpdate = true;
			}
			this.autoUpdate = autoUpdate;
			this.autoTime = autoTime;
			notify();
		}
	}

	public void setData(final LocationData data) {
		this.data = data;
	}

	public void setLastLocation(final Location location) {
		lastLocation = location;
	}
}
