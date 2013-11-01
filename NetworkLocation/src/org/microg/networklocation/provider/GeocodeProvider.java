package org.microg.networklocation.provider;

import android.content.Context;
import android.location.Address;
import android.location.GeocoderParams;
import android.util.Log;
import org.microg.networklocation.MainService;
import org.microg.networklocation.source.GeocodeDataSource;

import java.util.List;

public class GeocodeProvider extends com.android.location.provider.GeocodeProvider {
	private static final String TAG = "LocationGeocodeProvider";
	private final GeocodeDataSource source;

	public GeocodeProvider(final Context context, final GeocodeDataSource source) {
		this.source = source;
	}

	@Override
	public String onGetFromLocation(final double latitude, final double longitude, final int maxResults,
									final GeocoderParams params, final List<Address> addrs) {
		if (source.isSourceAvailable()) {
			source.addAdressesToListForLocation(latitude, longitude, params.getLocale(), addrs);
			if (addrs.size() > 0) {
				if (MainService.DEBUG)
					Log.d(TAG, addrs.get(0).getAddressLine(0));
				return addrs.get(0).getAddressLine(0);
			}
		}
		return null;
	}

	@Override
	public String onGetFromLocationName(final String locationName, final double lowerLeftLatitude,
										final double lowerLeftLongitude, final double upperRightLatitude,
										final double upperRightLongitude, final int maxResults,
										final GeocoderParams params, final List<Address> addrs) {
		if (MainService.DEBUG)
			Log.w(TAG, "GeocodeProvider not yet fully implemented. The application may not work.");
		return null;
	}

}
