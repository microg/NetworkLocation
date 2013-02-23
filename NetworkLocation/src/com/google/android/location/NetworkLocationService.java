package com.google.android.location;

import java.io.File;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

import com.google.android.location.data.CellLocationData;
import com.google.android.location.data.LocationData;
import com.google.android.location.data.WlanLocationData;
import com.google.android.location.database.CellMap;
import com.google.android.location.database.DatabaseHelper;
import com.google.android.location.database.WlanMap;
import com.google.android.location.debug.OverlayLocationNotifier;
import com.google.android.location.debug.OverlayLocationServer;
import com.google.android.location.provider.GeocodeProvider;
import com.google.android.location.provider.NetworkLocationProvider;
import com.google.android.location.provider.NetworkLocationProviderBase;
import com.google.android.location.provider.NetworkLocationProviderV2;
import com.google.android.location.source.AppleWlanLocationSource;
import com.google.android.location.source.DBFileCellLocationSource;
import com.google.android.location.source.GoogleGeocodeDataSource;

public class NetworkLocationService extends Service {
	public final static String ACTION_DEBUG_MOVE_OVERLAY_DOWN = NetworkLocationService.class
			.getName() + ".DEBUG_MOVE_OVERLAY_DOWN";
	public final static String ACTION_DEBUG_MOVE_OVERLAY_LEFT = NetworkLocationService.class
			.getName() + ".DEBUG_MOVE_OVERLAY_LEFT";
	public final static String ACTION_DEBUG_MOVE_OVERLAY_RIGHT = NetworkLocationService.class
			.getName() + ".DEBUG_MOVE_OVERLAY_RIGHT";
	public final static String ACTION_DEBUG_MOVE_OVERLAY_UP = NetworkLocationService.class
			.getName() + ".DEBUG_MOVE_OVERLAY_UP";
	public final static String ACTION_DEBUG_TOGGLE_OVERLAY = NetworkLocationService.class
			.getName() + ".DEBUG_TOGGLE_OVERLAY";

	private static final String TAG = "NetworkLocationService";
	private LocationData data;
	private GeocodeProvider geoprovider;
	private CellMap gsmMap;
	private NetworkLocationProviderBase nlprovider;
	private final BroadcastReceiver overlayMoveReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(final Context context, final Intent intent) {
			Location old = getOverlayLocation();
			if (old == null) {
				old = getRealLocation();
			}
			double lat = old.getLatitude();
			double lon = old.getLongitude();
			final double alt = old.getAltitude();
			if (intent.getAction().equalsIgnoreCase(
					ACTION_DEBUG_MOVE_OVERLAY_LEFT)) {
				lon -= 0.000200;
			} else if (intent.getAction().equalsIgnoreCase(
					ACTION_DEBUG_MOVE_OVERLAY_RIGHT)) {
				lon += 0.000200;
			} else if (intent.getAction().equalsIgnoreCase(
					ACTION_DEBUG_MOVE_OVERLAY_UP)) {
				lat += 0.000100;
			} else if (intent.getAction().equalsIgnoreCase(
					ACTION_DEBUG_MOVE_OVERLAY_DOWN)) {
				lat -= 0.000100;
			}
			setOverlayLocation(lat, lon, alt);
		}
	};
	private OverlayLocationNotifier overlayNotifier;
	private OverlayLocationServer overlayServer;
	private final BroadcastReceiver overlayToggleReciever = new BroadcastReceiver() {

		@Override
		public void onReceive(final Context context, final Intent intent) {
			if (getOverlayLocation() != null) {
				clearOverlayLocation();
				if (debugEnabled()) {
					Log.d(TAG, "Cleared OverlayLocation");
				}
				reInitOverlayNotification();
			} else {
				final Location l = getRealLocation();
				setOverlayLocation(l.getLatitude(), l.getLongitude(),
						l.getAltitude());
			}
		}
	};
	private WlanMap wlanMap;

	public NetworkLocationService() {
		Log.i(TAG, "new Service-Object constructed");
	}

	public void clearOverlayLocation() {
		data.clearOverlayLocation();
		reInitOverlayNotification();
	}

	private boolean debugEnabled() {
		return (Settings.System.getInt(getContentResolver(),
				"com.google.android.location.DEBUG", 0) == 1);
	}

	public Location getCurrentLocation() {
		return data.getCurrentLocation();
	}

	public Location getOverlayLocation() {
		return data.getOverlayLocation();
	}

	public Location getRealLocation() {
		return data.getRealLocation();
	}

	public boolean isActive() {
		return nlprovider.isActive();
	}

	@Override
	public IBinder onBind(final Intent intent) {
		if (debugEnabled()) {
			Log.d(TAG, "Incoming Bind Intent: " + intent);
		}
		if (intent == null) {
			return null;
		}
		final String action = intent.getAction();
		if (action == null) {
			return null;
		}
		if (action
				.equalsIgnoreCase("com.google.android.location.NetworkLocationProvider")
				|| action
						.equalsIgnoreCase("com.android.location.service.NetworkLocationProvider")
				|| action
						.equalsIgnoreCase("com.android.location.service.v2.NetworkLocationProvider")) {
			return nlprovider.getBinder();
		} else if (action
				.equalsIgnoreCase("com.google.android.location.GeocodeProvider")
				|| action
						.equalsIgnoreCase("com.android.location.service.GeocodeProvider")) {
			return geoprovider.getBinder();
		} else if (action
				.equalsIgnoreCase("com.google.android.location.internal.ANDROID_NLP")) {
			Log.w(TAG, "somebody wants internal stuff o.O");
			return nlprovider.getBinder();
		} else {
			if (debugEnabled()) {
				Log.w(TAG, "Unknown Action onBind: " + action);
			}
		}
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		if (debugEnabled()) {
			Log.d(TAG, "Creating Service");
		}
		wlanMap = new WlanMap(DatabaseHelper.getInstance(this));
		gsmMap = new CellMap(DatabaseHelper.getInstance(this));
		if (Build.VERSION.SDK_INT < 17) {
			nlprovider = new NetworkLocationProvider();
		} else {
			nlprovider = new NetworkLocationProviderV2();
		}
		data = new LocationData(nlprovider, this);
		data.addProvider(new CellLocationData(this, gsmMap,
				new DBFileCellLocationSource(new File(Environment
						.getExternalStorageDirectory(), ".nogapps/cells.db")),
				data));
		data.addProvider(new WlanLocationData(this, wlanMap,
				new AppleWlanLocationSource(), data));
		nlprovider.setData(data);
		geoprovider = new GeocodeProvider(this, new GoogleGeocodeDataSource());
		reInitDebug();
	}

	@Override
	public void onDestroy() {
		geoprovider = null;
		nlprovider = null;
	}

	private void reInitDebug() {
		if (debugEnabled()) {
			registerReceiver(overlayToggleReciever, new IntentFilter(
					ACTION_DEBUG_TOGGLE_OVERLAY));
			registerReceiver(overlayMoveReceiver, new IntentFilter(
					ACTION_DEBUG_MOVE_OVERLAY_DOWN));
			registerReceiver(overlayMoveReceiver, new IntentFilter(
					ACTION_DEBUG_MOVE_OVERLAY_UP));
			registerReceiver(overlayMoveReceiver, new IntentFilter(
					ACTION_DEBUG_MOVE_OVERLAY_LEFT));
			registerReceiver(overlayMoveReceiver, new IntentFilter(
					ACTION_DEBUG_MOVE_OVERLAY_RIGHT));
		} else {
			unregisterReceiver(overlayMoveReceiver);
			unregisterReceiver(overlayToggleReciever);
		}
		reInitOverlayServer();
		reInitOverlayNotification();
	}

	public void reInitOverlayNotification() {
		if (debugEnabled() && nlprovider.isActive()) {
			if (overlayNotifier == null) {
				overlayNotifier = new OverlayLocationNotifier(this);
			}
			overlayNotifier.start();
		} else if (overlayNotifier != null) {
			overlayNotifier.stop();
			overlayNotifier = null;
		}
	}

	@SuppressWarnings("deprecation")
	public void reInitOverlayServer() {
		if (overlayServer != null && overlayServer.isAlive()) {
			overlayServer.stop();
		}
		if (debugEnabled()) {
			overlayServer = new OverlayLocationServer(this);
			overlayServer.start();
		}
	}

	public void setOverlayLocation(final double lat, final double lon,
			final double alt) {
		data.setOverlayLocation(lat, lon, alt);
		reInitOverlayNotification();
	}

	public void updateOverlayNotification() {
		reInitOverlayNotification();
	}

}
