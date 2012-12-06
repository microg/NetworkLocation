package com.google.android.location;

import java.util.List;

import android.content.Context;
import android.location.Address;
import android.location.GeocoderParams;
import android.util.Log;

public class GeocodeProvider extends
		com.android.location.provider.GeocodeProvider {
	private static final String TAG = "LocationGeocodeProvider";

	public GeocodeProvider(final Context context) {
		Log.d(TAG, "new Provider-Object constructed");
	}

	@Override
	public String onGetFromLocation(final double latitude,
			final double longitude, final int maxResults,
			final GeocoderParams params, final List<Address> addrs) {
		Log.w(TAG,
				"GeocodeProvider not yet implemented. The application may not work.");
		return null;
	}

	@Override
	public String onGetFromLocationName(final String locationName,
			final double lowerLeftLatitude, final double lowerLeftLongitude,
			final double upperRightLatitude, final double upperRightLongitude,
			final int maxResults, final GeocoderParams params,
			final List<Address> addrs) {
		Log.w(TAG,
				"GeocodeProvider not yet implemented. The application may not work.");
		return null;
	}

}
