package com.google.android.location;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class NetworkLocationService extends Service {
	private static final String TAG = NetworkLocationService.class.getName();
	private NetworkLocationProvider nlprovider;
	private GeocodeProvider geoprovider;
	private LocationData data;
	private WlanMap wlanMap;
	private GsmCellMap gsmMap;

	public NetworkLocationService() {
		Log.i(TAG, "new Service-Object constructed");
	}

	@Override
	public IBinder onBind(Intent intent) {
		if (intent == null) {
			return null;
		}
		final String action = intent.getAction();
		if (action == null) {
			return null;
		}
		if (action
				.equalsIgnoreCase("com.google.android.location.NetworkLocationProvider") || action
				.equalsIgnoreCase("com.android.location.service.NetworkLocationProvider")) {
			return nlprovider.getBinder();
		} else if (action
				.equalsIgnoreCase("com.google.android.location.GeocodeProvider") || action
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
		gsmMap = new GsmCellMap(DatabaseHelper.getInstance(this));
		nlprovider = new NetworkLocationProvider();
		data = new LocationData(this, gsmMap, wlanMap, nlprovider);
		nlprovider.setData(data);
		geoprovider = new GeocodeProvider(this);
	}

	@Override
	public void onDestroy() {
		geoprovider = null;
		nlprovider = null;
	}

}
