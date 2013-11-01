package org.microg.networklocation.source;

import android.content.Context;
import android.location.Location;
import android.net.ConnectivityManager;
import android.util.Log;
import com.apple.iphone.services.Location.Response;
import com.apple.iphone.services.Location.ResponseWLAN;
import org.microg.networklocation.applewifi.LocationRetriever;
import org.microg.networklocation.data.WlanLocationData;
import org.microg.networklocation.database.WlanMap;

import java.util.Collection;
import java.util.Date;

public class AppleWlanLocationSource implements WlanLocationSource {

	private static final String TAG = "AppleWlanLocationSource";
	private final ConnectivityManager connectivityManager;

	public AppleWlanLocationSource(Context context) {
		this((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
	}

	public AppleWlanLocationSource(ConnectivityManager connectivityManager) {
		this.connectivityManager = connectivityManager;
	}

	@Override
	public void requestMacLocations(final Collection<String> macs, final Collection<String> missingMacs,
									final WlanMap wlanMap) {
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
			Log.d(TAG, "requestMacLocations: " + response.getWlanCount() + " results, " + newLocs + " new, " + reqLocs +
					   " required");
		} catch (final Exception e) {
			Log.e(TAG, "requestMacLocations: " + macs, e);
		}
	}

	@Override
	public boolean isSourceAvailable() {
		return connectivityManager.getActiveNetworkInfo() != null &&
			   connectivityManager.getActiveNetworkInfo().isAvailable() &&
			   connectivityManager.getActiveNetworkInfo().isConnected();
	}
}
