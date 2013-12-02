package org.microg.networklocation.provider;

import android.location.Address;
import android.location.GeocoderParams;
import android.util.Log;
import org.microg.networklocation.MainService;
import org.microg.networklocation.source.GeocodeSource;

import java.util.ArrayList;
import java.util.List;

public class GeocodeProvider extends internal.com.android.location.provider.GeocodeProvider {
	private static final String TAG = "LocationGeocodeProvider";
	private final List<GeocodeSource> sources;

	public GeocodeProvider(List<GeocodeSource> sources) {
		this.sources = new ArrayList<GeocodeSource>(sources);
	}

	@Override
	public String onGetFromLocation(double latitude, double longitude, int maxResults, GeocoderParams params,
									List<Address> addrs) {
		for (GeocodeSource source : sources) {
			if (source.isSourceAvailable()) {
				List<Address> addresses = null;
				try {
					addresses = source.getFromLocation(latitude, longitude, params.getLocale());
				} catch (Throwable t) {
					Log.w(TAG, source.getName() + " throws exception!", t);
				}
				if ((addresses != null) && !addresses.isEmpty()) {
					addrs.addAll(addresses);
					if (MainService.DEBUG) {
						Log.d(TAG, latitude + "/" + longitude + " reverse geolocated to:" + addrs.get(0));
					}
					return null; // null means everything is ok!
				}
			}
		}
		Log.d(TAG, "Could not reverse geolocate: " + latitude + "/" + longitude);
		return "unknown";
	}

	@Override
	public String onGetFromLocationName(String locationName, double lowerLeftLatitude, double lowerLeftLongitude,
										double upperRightLatitude, double upperRightLongitude, int maxResults,
										GeocoderParams params, List<Address> addrs) {
		if (MainService.DEBUG)
			Log.w(TAG, "GeocodeProvider not yet fully implemented. The application may not work.");
		return null;
	}

}
