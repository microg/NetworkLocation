package org.microg.networklocation;

import android.app.Service;
import android.content.*;
import android.location.Location;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import org.microg.networklocation.data.CellLocationData;
import org.microg.networklocation.data.LocationData;
import org.microg.networklocation.data.WlanLocationData;
import org.microg.networklocation.database.CellMap;
import org.microg.networklocation.database.DatabaseHelper;
import org.microg.networklocation.database.WlanMap;
import org.microg.networklocation.helper.Reflected;
import org.microg.networklocation.provider.GeocodeProvider;
import org.microg.networklocation.provider.NetworkLocationProvider;
import org.microg.networklocation.provider.NetworkLocationProviderBase;
import org.microg.networklocation.provider.NetworkLocationProviderV2;
import org.microg.networklocation.source.AppleWlanLocationSource;
import org.microg.networklocation.source.DBFileCellLocationSource;
import org.microg.networklocation.source.GoogleGeocodeDataSource;

import java.io.File;

public class MainService extends Service {
	public static final boolean DEBUG = true;
	private static final String TAG = "NetworkLocationService";
	private static Context context;
	private LocationData data;
	private GeocodeProvider geoprovider;
	private CellMap gsmMap;
	private NetworkLocationProviderBase nlprovider;
	private WlanMap wlanMap;
	private WifiManager wifiManager;

	public MainService() {
		if (DEBUG) {
			Log.d(TAG, "new Service-Object constructed");
		}
	}

	public static Context getContext() {
		return context;
	}

	public Location getCurrentLocation() {
		return data.getCurrentLocation();
	}

	public Location getRealLocation() {
		return data.getRealLocation();
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
		final String action = intent.getAction();
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
		wlanMap = new WlanMap(DatabaseHelper.getInstance(this));
		gsmMap = new CellMap(DatabaseHelper.getInstance(this));
		if (Build.VERSION.SDK_INT < 17) {
			nlprovider = new NetworkLocationProvider();
		} else {
			nlprovider = new NetworkLocationProviderV2();
		}
		data = new LocationData(nlprovider, this);
		File file = new File(Environment.getExternalStorageDirectory(), ".nogapps/cells.db");
		if (file.exists()) {
			data.addProvider(new CellLocationData(this, gsmMap, new DBFileCellLocationSource(file), data));
		} else if (DEBUG) {
			Log.d(TAG, "No cells.db found, ignoring CellLocation");
			Log.d(TAG, "If you want to add CellLocation support, download a cells.db to " + file.getAbsolutePath() +
					   " and reboot your phone");
		}
		data.addProvider(new WlanLocationData(this, wlanMap, new AppleWlanLocationSource(this), data));
		nlprovider.setData(data);
		registerReceiver(new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				updateProviderStateOnAirplaneMode();
			}
		}, new IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED));
		updateProviderStateOnAirplaneMode();
		geoprovider = new GeocodeProvider(this, new GoogleGeocodeDataSource());
	}

	@Override
	public void onDestroy() {
		geoprovider = null;
		nlprovider = null;
	}

	public void updateProviderStateOnAirplaneMode() {
		boolean airplane = isAirplaneModeOn();
		boolean wifi = wifiManager.isWifiEnabled();
		if (DEBUG)
			Log.d(TAG, "airplane:" + airplane + " | wifi:" + wifi);
		if (airplane && !wifi) {
			if (DEBUG)
				Log.d(TAG, "AirplaneMode is enabled and wifi is off, so no way to get location for us");
			nlprovider.disable();
		} else {
			if (DEBUG)
				Log.d(TAG, "AirplaneMode or wifi is enabled. make sure we're active!");
			nlprovider.enable();
		}
	}
}
