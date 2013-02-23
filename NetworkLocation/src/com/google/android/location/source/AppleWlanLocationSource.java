package com.google.android.location.source;

import java.util.Collection;
import java.util.Date;

import android.location.Location;
import android.util.Log;

import com.apple.iphone.services.Location.Response;
import com.apple.iphone.services.Location.ResponseWLAN;
import com.apple.iphone.services.LocationRetriever;
import com.google.android.location.data.WlanLocationData;
import com.google.android.location.database.WlanMap;

public class AppleWlanLocationSource implements WlanLocationSource {

	private static final String TAG = "AppleWlanLocationSource";

	@Override
	public void requestMacLocations(final Collection<String> macs,
			final Collection<String> missingMacs, final WlanMap wlanMap) {
		try {
			final Response response = LocationRetriever.retrieveLocations(macs);
			int newLocs = 0;
			int reqLocs = 0;
			for (final ResponseWLAN rw : response.getWlanList()) {
				final String mac = WlanLocationData.niceMac(rw.getMac());
				final Location loc = new Location(WlanLocationData.IDENTIFIER);
				loc.setLatitude(rw.getLocation().getLatitude() / 1E8F);
				loc.setLongitude(rw.getLocation().getLongitude() / 1E8F);
				loc.setAccuracy(rw.getLocation().getAccuracy());
				if (rw.getLocation().getAltitude() != -500) {
					loc.setAltitude(rw.getLocation().getAltitude());
				}
				loc.setTime(new Date().getTime());
				if (!wlanMap.containsKey(mac)) {
					newLocs++;
				}
				wlanMap.put(mac, loc);
				if (macs.contains(mac)) {
					macs.remove(mac);
				}
				synchronized (missingMacs) {
					if (missingMacs.contains(mac)) {
						reqLocs++;
						missingMacs.remove(mac);
					}
				}
			}
			Log.d(TAG, "requestMacLocations: " + response.getWlanCount()
					+ " results, " + newLocs + " new, " + reqLocs + " required");
		} catch (final Exception e) {
			Log.e(TAG, "requestMacLocations: " + macs, e);
		}
	}

}
