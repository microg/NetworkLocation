package com.google.android.location.provider;

import java.util.List;

import android.content.Context;
import android.location.Address;
import android.location.GeocoderParams;
import android.util.Log;

import com.google.android.location.source.GeocodeDataSource;

public class GeocodeProvider extends
		com.android.location.provider.GeocodeProvider {
	private static final String TAG = "LocationGeocodeProvider";
	private final GeocodeDataSource source;

	public GeocodeProvider(final Context context, GeocodeDataSource source) {
		this.source = source;
	}

	@Override
	public String onGetFromLocation(final double latitude,
			final double longitude, final int maxResults,
			final GeocoderParams params, final List<Address> addrs) {
		source.addAdressesToListForLocation(latitude, longitude,
				params.getLocale(), addrs);
		if (addrs.size() > 0) {
			Log.d(TAG, addrs.get(0).getAddressLine(0));
			return addrs.get(0).getAddressLine(0);
		}
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
