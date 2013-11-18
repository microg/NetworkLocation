package org.microg.networklocation.source;

import android.content.Context;
import android.location.Location;
import android.net.ConnectivityManager;
import android.util.Log;
import org.microg.networklocation.applewifi.LocationRetriever;
import org.microg.networklocation.applewifi.Response;
import org.microg.networklocation.data.WlanLocationData;
import org.microg.networklocation.database.WlanMap;

import java.util.Collection;
import java.util.Date;

public class AppleWlanLocationSource implements WlanLocationSource {

	private static final String TAG = "AppleWlanLocationSource";
	public static final float LATLON_WIRE = 1E8F;
	private final ConnectivityManager connectivityManager;

	public AppleWlanLocationSource(Context context) {
		this((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
	}

	public AppleWlanLocationSource(ConnectivityManager connectivityManager) {
		this.connectivityManager = connectivityManager;
	}

	@Override
	public boolean isSourceAvailable() {
		return connectivityManager.getActiveNetworkInfo() != null &&
			   connectivityManager.getActiveNetworkInfo().isAvailable() &&
			   connectivityManager.getActiveNetworkInfo().isConnected();
	}

	@Override
	public void requestMacLocations(Collection<String> macs, Collection<String> missingMacs, WlanMap wlanMap) {
		try {
			final Response response =
					LocationRetriever.retrieveLocations(macs);
			int newLocs = 0;
			int reqLocs = 0;
			for (Response.ResponseWLAN rw : response.wlan) {
				String mac = WlanLocationData.niceMac(rw.mac);
				Location loc = new Location(WlanLocationData.IDENTIFIER);
				loc.setLatitude(rw.location.latitude / LATLON_WIRE);
				loc.setLongitude(rw.location.longitude / LATLON_WIRE);
				loc.setAccuracy(rw.location.accuracy);
				if (rw.location.altitude != -500) {
					loc.setAltitude(rw.location.altitude);
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
			Log.d(TAG, "requestMacLocations: " + response.wlan.size() + " results, " + newLocs + " new, " + reqLocs +
					   " required");
		} catch (final Exception e) {
			Log.e(TAG, "requestMacLocations: " + macs, e);
		}
	}
}
