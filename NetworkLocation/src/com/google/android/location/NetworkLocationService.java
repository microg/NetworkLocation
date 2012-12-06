package com.google.android.location;

import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

public class NetworkLocationService extends Service {
	private static final String TAG = "NetworkLocationService";
	private LocationData data;
	private GeocodeProvider geoprovider;
	private CellMap gsmMap;
	private NetworkLocationProviderBase nlprovider;
	private WlanMap wlanMap;

	public NetworkLocationService() {
		Log.i(TAG, "new Service-Object constructed");
	}

	@Override
	public IBinder onBind(final Intent intent) {
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
		} else {
			Log.w(TAG, "Unknown Action onBind: " + action);
		}
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		wlanMap = new WlanMap(DatabaseHelper.getInstance(this));
		gsmMap = new CellMap(DatabaseHelper.getInstance(this));
		if (Build.VERSION.SDK_INT < 17) {
			nlprovider = new NetworkLocationProvider();
		} else {
			nlprovider = new NetworkLocationProviderV2();
		}
		data = new LocationData(nlprovider);
		data.addProvider(new CellLocationData(this, gsmMap, data));
		data.addProvider(new WlanLocationData(this, wlanMap, data));
		nlprovider.setData(data);
		geoprovider = new GeocodeProvider(this);
	}

	@Override
	public void onDestroy() {
		geoprovider = null;
		nlprovider = null;
	}

}
