package com.google.android.location;

import java.util.List;

import android.content.Context;
import android.location.Address;
import android.location.GeocoderParams;
import android.util.Log;

public class GeocodeProvider extends
		com.android.location.provider.GeocodeProvider {
	private static final String TAG = GeocodeProvider.class.getName();

	private static GeocodeProvider instance;

	public static GeocodeProvider getInstance() {
		return instance;
	}

	public static void init(NetworkLocationService networkLocationService) {
		instance = new GeocodeProvider(networkLocationService);
	}

	public GeocodeProvider(Context context) {
		// TODO Auto-generated constructor stub
	}

	@Override
	public String onGetFromLocation(double latitude, double longitude,
			int maxResults, GeocoderParams params, List<Address> addrs) {
		Log.i(TAG, "onGetFromLocation");
		Log.i(TAG, "> latitude:" + latitude);
		Log.i(TAG, "> longitude:" + longitude);
		Log.i(TAG, "> maxResults:" + maxResults);
		Log.i(TAG, "> params:" + params);
		Log.i(TAG, "> addrs:" + addrs);
		return null;
	}

	@Override
	public String onGetFromLocationName(String locationName,
			double lowerLeftLatitude, double lowerLeftLongitude,
			double upperRightLatitude, double upperRightLongitude,
			int maxResults, GeocoderParams params, List<Address> addrs) {
		Log.i(TAG, "onGetFromLocationName");
		Log.i(TAG, "> locationName:" + locationName);
		Log.i(TAG, "> maxResults:" + maxResults);
		Log.i(TAG, "> params:" + params);
		Log.i(TAG, "> addrs:" + addrs);
		return null;
	}

}
