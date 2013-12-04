package org.microg.networklocation;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import org.microg.networklocation.backends.apple.AppleWifiLocationSource;
import org.microg.networklocation.backends.file.NewFileCellLocationSource;
import org.microg.networklocation.backends.mozilla.IchnaeaCellLocationSource;
import org.microg.networklocation.backends.opencellid.OpenCellIdLocationSource;
import org.microg.networklocation.backends.mapquest.NominatimGeocodeSource;
import org.microg.networklocation.data.*;
import org.microg.networklocation.database.LocationDatabase;
import org.microg.networklocation.helper.Reflected;
import org.microg.networklocation.provider.GeocodeProvider;
import org.microg.networklocation.provider.NetworkLocationProvider;
import org.microg.networklocation.provider.NetworkLocationProviderBase;
import org.microg.networklocation.provider.NetworkLocationProviderV2;
import org.microg.networklocation.backends.file.OldFileCellLocationSource;
import org.microg.networklocation.source.GeocodeSource;
import org.microg.networklocation.source.LocationSource;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainService extends Service {
	public static final boolean DEBUG;
	static {
		DEBUG = Log.isLoggable("nlp", Log.DEBUG);
	}
	private static final String TAG = "NetworkLocationService";
	private static Context context;
	private LocationCalculator locationCalculator;
	private LocationRetriever locationRetriever;
	private GeocodeProvider geoprovider;
	private NetworkLocationProviderBase nlprovider;
	private WifiManager wifiManager;
	private BroadcastReceiver airplaneModeReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			updateProviderStateOnAirplaneMode();
		}
	};

	public MainService() {
		if (DEBUG) {
			Log.d(TAG, "new Service-Object constructed");
		}
	}

	public static Context getContext() {
		return context;
	}

	public Location getCurrentLocation() {
		return locationCalculator.getCurrentLocation();
	}

	public boolean isActive() {
		return nlprovider.isActive();
	}

	private boolean isAirplaneModeOn() {
		return ((Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN) ?
				(Settings.System.getInt(getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0)) :
				(Reflected.androidProviderSettingsGlobalGetInt(getContentResolver(), "airplane_mode_on", 0))) != 0;
	}

	@Override
	public IBinder onBind(final Intent intent) {
		if (DEBUG) {
			Log.d(TAG, "Incoming Bind Intent: " + intent);
		}
		if (intent == null) {
			return null;
		}
		String action = intent.getAction();
		if (action == null) {
			return null;
		}
		if (action.equalsIgnoreCase("com.google.android.location.NetworkLocationProvider") ||
			action.equalsIgnoreCase("com.android.location.service.NetworkLocationProvider") ||
			action.equalsIgnoreCase("com.android.location.service.v2.NetworkLocationProvider") ||
			action.equalsIgnoreCase("com.android.location.service.v3.NetworkLocationProvider")) {
			return nlprovider.getBinder();
		} else if (action.equalsIgnoreCase("com.google.android.location.GeocodeProvider") ||
				   action.equalsIgnoreCase("com.android.location.service.GeocodeProvider")) {
			return geoprovider.getBinder();
		} else if (action.equalsIgnoreCase("com.google.android.location.internal.ANDROID_NLP")) {
			Log.w(TAG, "somebody wants internal stuff o.O");
			return nlprovider.getBinder();
		}
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		context = this;
		if (DEBUG) {
			Log.d(TAG, "Creating Service");
		}
		wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
		if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN) {
			nlprovider = new NetworkLocationProvider();
		} else {
			nlprovider = new NetworkLocationProviderV2();
		}
		geoprovider = new GeocodeProvider();
		WifiSpecRetriever wifiSpecRetriever = new WifiSpecRetriever(context);
		CellSpecRetriever cellSpecRetriever = new CellSpecRetriever(context);
		LocationDatabase locationDatabase = new LocationDatabase(context);
		locationRetriever = new LocationRetriever(locationDatabase);
		locationCalculator = new LocationCalculator(locationDatabase, locationRetriever, cellSpecRetriever,
													wifiSpecRetriever);
		nlprovider.setCalculator(locationCalculator);

		List<LocationSource<WifiSpec>> wifiSources = new ArrayList<LocationSource<WifiSpec>>();
		wifiSources.add(new AppleWifiLocationSource(context));
		locationRetriever.setWifiLocationSources(wifiSources);

		List<LocationSource<CellSpec>> cellSources = new ArrayList<LocationSource<CellSpec>>();
		cellSources.add(new NewFileCellLocationSource(new File(Environment.getExternalStorageDirectory(), ".nogapps/lacells.db")));
		cellSources.add(new OldFileCellLocationSource(new File(Environment.getExternalStorageDirectory(), ".nogapps/cells.db")));
		cellSources.add(new OpenCellIdLocationSource(context));
		cellSources.add(new IchnaeaCellLocationSource(context));
		locationRetriever.setCellLocationSources(cellSources);

		locationRetriever.start();

		List<GeocodeSource> geocodeSources = new ArrayList<GeocodeSource>();
		geocodeSources.add(new NominatimGeocodeSource(context));
		geoprovider.setSources(geocodeSources);

		registerReceiver(airplaneModeReceiver, new IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED));
		updateProviderStateOnAirplaneMode();
	}

	@Override
	public void onDestroy() {
		if (DEBUG) {
			Log.d(TAG, "Destroying service");
		}
		unregisterReceiver(airplaneModeReceiver);
		geoprovider = null;
		nlprovider.disable();
		locationCalculator = null;
		locationRetriever.stop();
		locationRetriever = null;
		nlprovider = null;
		wifiManager = null;
	}

	public void updateProviderStateOnAirplaneMode() {
		boolean airplane = isAirplaneModeOn();
		boolean wifi = wifiManager.isWifiEnabled();
		if (DEBUG) {
			Log.d(TAG, "airplane:" + airplane + " | wifi:" + wifi);
		}
		if (airplane && !wifi) {
			if (DEBUG) {
				Log.d(TAG, "AirplaneMode is enabled and wifi is off, so no way to get location for us");
			}
			nlprovider.disable();
		} else {
			if (DEBUG) {
				Log.d(TAG, "AirplaneMode or wifi is enabled. make sure we're active!");
			}
			nlprovider.enable();
		}
	}
}
