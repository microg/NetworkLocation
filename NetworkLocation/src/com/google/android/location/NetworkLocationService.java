package com.google.android.location;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class NetworkLocationService extends Service {
	private static final String TAG = "my.NetworkLocationService";
	NetworkLocationProvider nlprovider;
	GeocodeProvider geoprovider;

	public NetworkLocationService() {
		Log.i(TAG, "ctr");
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
				.equalsIgnoreCase("com.google.android.location.NetworkLocationProvider")) {
			if (nlprovider == null) {
				nlprovider = NetworkLocationProvider.getInstance();
			}
			return nlprovider.getBinder();
		} else if (action
				.equalsIgnoreCase("com.google.android.location.GeocodeProvider")) {
			if (geoprovider == null) {
				geoprovider = GeocodeProvider.getInstance();
			}
			return geoprovider.getBinder();
		} else {
			Log.w(TAG, "Unknown Action onBind: " + action);
		}
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Database.init(this);
		NetworkLocationProvider.init();
		LocationData.init(this, NetworkLocationProvider.getInstance());
		NetworkLocationProvider.getInstance().setData(
				LocationData.getInstance());
		GeocodeProvider.init(this);
	}

	@Override
	public void onDestroy() {
		geoprovider = null;
		nlprovider = null;
	}

}
