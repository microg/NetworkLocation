package org.microg.networklocation.provider;

import android.annotation.TargetApi;
import android.location.Criteria;
import android.location.Location;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.WorkSource;
import internal.com.android.location.provider.LocationProviderBase;
import internal.com.android.location.provider.LocationRequestUnbundled;
import internal.com.android.location.provider.ProviderPropertiesUnbundled;
import internal.com.android.location.provider.ProviderRequestUnbundled;
import org.microg.networklocation.NetworkLocationThread;
import org.microg.networklocation.data.DefaultLocationDataProvider;
import org.microg.networklocation.data.LocationCalculator;
import org.microg.networklocation.helper.Reflected;

@TargetApi(17)
public class NetworkLocationProviderV2 extends LocationProviderBase implements NetworkLocationProviderBase {

	private final static String IDENTIFIER = "network";
	private static final String TAG = "NetworkLocationProviderV2";
	private NetworkLocationThread background = new NetworkLocationThread();
	private boolean enabledByService = false;
	private boolean enabledBySetting = false;

	public NetworkLocationProviderV2() {
		super(TAG, ProviderPropertiesUnbundled
				.create(false, false, false, false, false, false, false, Criteria.POWER_LOW, Criteria.ACCURACY_COARSE));
	}

	@Deprecated
	public NetworkLocationProviderV2(final boolean internal) {
		this();
	}

	@Override
	public synchronized void disable() {
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
		background.start();
	}

	@Override
	public boolean isActive() {
		return background != null && background.isAlive() && background.isActive();
	}

	@Override
	public synchronized void onDisable() {
		enabledBySetting = false;
		background.disable();
	}

	@Override
	public synchronized void onEnable() {
		enabledBySetting = true;
		if (enabledByService)
			enableBackground();
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
	public void onLocationChanged(Location location) {
		if (location != null) {
			background.setLastTime(SystemClock.elapsedRealtime());
			background.setLastLocation(location);
			Bundle bundle = new Bundle();
			bundle.putString(NETWORK_LOCATION_TYPE, location.getProvider());
			location.setExtras(bundle);
			Reflected.androidLocationLocationMakeComplete(location);
			reportLocation(DefaultLocationDataProvider.renameSource(location, IDENTIFIER));
		}
	}

	@Override
	public void onProviderDisabled(final String provider) {
	}

	@Override
	public void onProviderEnabled(final String provider) {
	}

	@Override
	public void onSetRequest(final ProviderRequestUnbundled requests, final WorkSource ws) {
		long autoTime = Long.MAX_VALUE;
		boolean autoUpdate = false;
		for (final LocationRequestUnbundled request : requests.getLocationRequests()) {
			if (request.getInterval() < autoTime) {
				autoTime = request.getInterval();
			}
			autoUpdate = true;
		}
		if (autoTime < 5000) {
			autoTime = 5000;
		}
		background.setAuto(autoUpdate, autoTime);
	}

	@Override
	public void onStatusChanged(final String provider, final int status, final Bundle extras) {
	}

	@Override
	public void setCalculator(LocationCalculator locationCalculator) {
		background.setCalculator(locationCalculator);
	}

}
