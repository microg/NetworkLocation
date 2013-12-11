package org.microg.networklocation.platform;

import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.WorkSource;
import android.util.Log;
import internal.com.android.location.provider.LocationProvider;
import org.microg.networklocation.MainService;
import org.microg.networklocation.NetworkLocationThread;
import org.microg.networklocation.data.LocationCalculator;

class NetworkLocationProvider extends LocationProvider
		implements org.microg.networklocation.provider.NetworkLocationProvider {

	private static final String IDENTIFIER = "network";
	private static final String TAG = "NetworkLocationProvider";
	private NetworkLocationThread background;
	private long autoTime;
	private boolean autoUpdate;
	private boolean enabledByService = false;
	private boolean enabledBySetting = false;

	public NetworkLocationProvider() {
		autoUpdate = false;
		autoTime = Long.MAX_VALUE;
		background = new NetworkLocationThread();
	}

	@Deprecated
	public NetworkLocationProvider(final boolean internal) {
		this();
	}

	public NetworkLocationProvider(LocationCalculator data) {
		this();
		background.setCalculator(data);
	}

	@Override
	public synchronized void disable() {
		background.setLocationProvider(null);
		background.disable();
		enabledByService = false;
	}

	@Override
	public synchronized void enable() {
		enabledByService = true;
		if (enabledBySetting)
			enableBackground();
	}

	private void enableBackground() {
		background.disable();
		background = new NetworkLocationThread(background);
		background.setLocationProvider(this);
		background.start();
	}

	@Override
	public boolean isActive() {
		return background != null && background.isAlive() && background.isActive();
	}

	@Override
	public void onAddListener(final int uid, final WorkSource ws) {
		if (MainService.DEBUG) {
			Log.d(TAG, uid + " is listening as " + ws != null ? (ws + " (contents:" + ws.describeContents() + ")") :
					   "[unknown WorkSource]");
		}
	}

	@Override
	public void onDisable() {
		enabledBySetting = false;
		background.disable();
	}

	@Override
	public void onEnable() {
		enabledBySetting = true;
		if (enabledByService)
			enableBackground();
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
		if (MainService.DEBUG)
			Log.w(TAG, "Internal State not yet implemented. The application may not work.");
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
			background.setLastLocation(location);
			if (MainService.DEBUG) {
				Log.d(TAG, "Reporting: " + location);
			}
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
		if (MainService.DEBUG)
			Log.d(TAG, "onUpdateNetworkState: " + state + " (" + info + ")");
	}

	@Override
	public void setCalculator(LocationCalculator locationCalculator) {
		background.setCalculator(locationCalculator);
	}

}
