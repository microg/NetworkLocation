package com.google.android.location;

import java.io.File;

import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import com.google.android.location.data.CellLocationData;
import com.google.android.location.data.LocationData;
import com.google.android.location.data.WlanLocationData;
import com.google.android.location.database.CellMap;
import com.google.android.location.database.DatabaseHelper;
import com.google.android.location.database.WlanMap;
import com.google.android.location.provider.GeocodeProvider;
import com.google.android.location.provider.NetworkLocationProvider;
import com.google.android.location.provider.NetworkLocationProviderBase;
import com.google.android.location.provider.NetworkLocationProviderV2;
import com.google.android.location.source.AppleWlanLocationSource;
import com.google.android.location.source.DBFileCellLocationSource;
import com.google.android.location.source.GoogleGeocodeDataSource;

public class NetworkLocationService extends Service {
	private static final String TAG = "NetworkLocationService";
	private LocationData data;
	private GeocodeProvider geoprovider;
	private CellMap gsmMap;
	private NetworkLocationProviderBase nlprovider;
	private WlanMap wlanMap;
	private OverlayLocationServer overlayServer;

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
		} else if (action
				.equalsIgnoreCase("com.google.android.location.internal.ANDROID_NLP")) {
			Log.w(TAG, "somebody wants internal stuff o.O");
			return nlprovider.getBinder();
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
		data.addProvider(new CellLocationData(this, gsmMap,
				new DBFileCellLocationSource(new File(Environment
						.getExternalStorageDirectory(), ".nogapps/cells.db")),
				data));
		data.addProvider(new WlanLocationData(this, wlanMap,
				new AppleWlanLocationSource(), data));
		nlprovider.setData(data);
		geoprovider = new GeocodeProvider(this, new GoogleGeocodeDataSource());
		reInitOverlayServer();
	}

	public void reInitOverlayServer() {
		overlayServer = new OverlayLocationServer(data, this);
		overlayServer.start();
	}

	@Override
	public void onDestroy() {
		geoprovider = null;
		nlprovider = null;
	}

}
