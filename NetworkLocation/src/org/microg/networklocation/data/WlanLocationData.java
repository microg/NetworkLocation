package org.microg.networklocation.data;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.net.ConnectivityManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;
import org.microg.networklocation.database.WlanMap;
import org.microg.networklocation.source.WlanLocationSource;

import java.util.*;

public class WlanLocationData extends DefaultLocationDataProvider {

	public final static String IDENTIFIER = "wifi";
	private final static String TAG = "WlanLocationData";

	public static Collection<String> getWLANs(final Context context) {
		if (context == null) {
			return null;
		}
		final ArrayList<String> wlans = new ArrayList<String>();
		final WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		final List<ScanResult> result = wifiManager.getScanResults();
		if (result != null) {
			for (final ScanResult scanResult : result) {
				final String mac = scanResult.BSSID;
				wlans.add(niceMac(mac));
			}
		}
		return wlans;
	}

	public static String niceMac(String mac) {
		mac = mac.toLowerCase(Locale.getDefault());
		final StringBuilder builder = new StringBuilder();
		final String[] arr = mac.split(":");
		for (int i = 0; i < arr.length; i++) {
			if (arr[i].length() == 1) {
				builder.append("0");
			}
			builder.append(arr[i]);
			if (i < arr.length - 1) {
				builder.append(":");
			}
		}
		return builder.toString();
	}

	private final Context context;

	private final LocationListener listener;
	private final Stack<String> missingMacs;
	private Thread retriever;
	private final WlanLocationSource source;
	private final WlanMap wlanMap;

	public WlanLocationData(final Context context, final WlanMap wlanMap, final WlanLocationSource source,
							final LocationListener listener) {
		this.context = context;
		this.wlanMap = wlanMap;
		this.listener = listener;
		this.source = source;
		missingMacs = new Stack<String>();
	}

	private void addToMissing(final Collection<String> wlans) {
		for (final String wlan : wlans) {
			addToMissing(wlan);
		}
	}

	private void addToMissing(final String wlan) {
		synchronized (missingMacs) {
			if (!missingMacs.contains(wlan)) {
				missingMacs.add(wlan);
			}
		}
	}

	@Override
	public android.location.Location getCurrentLocation() {
		final Collection<String> wlans = getWLANs();
		requestMissing(wlans);
		final Map<String, Location> locs = getLocations(wlans);
		final Location location = calculateLocation(locs.values(), 1000);
		if (location == null || (location.getLatitude() == 0 && location.getLongitude() == 0)) {
			Log.d(TAG, "could not get location via wlan");
			return null;
		}
		listener.onLocationChanged(location);
		return location;
	}

	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}

	private Location getLocation(final String mac) {
		return renameSource(wlanMap.get(mac));
	}

	private Map<String, Location> getLocations(final Collection<String> wlans) {
		final HashMap<String, Location> locs = new HashMap<String, Location>();
		for (final String wlan : wlans) {
			final Location loc = getLocation(wlan);
			if (loc != null) {
				locs.put(wlan, loc);
			}
		}
		return locs;
	}

	private Collection<String> getWLANs() {
		return getWLANs(context);
	}

	private boolean isOnline() {
		final ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

		return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
	}

	private Collection<String> missingInCache(final Collection<String> wlans) {
		final ArrayList<String> macs = new ArrayList<String>();
		for (final String wlan : wlans) {
			if (!wlanMap.containsKey(wlan)) {
				macs.add(wlan);
			}
		}
		return macs;
	}

	private void requestLocations() {
		if (!source.isSourceAvailable())
			return;
		if (missingMacs == null) {
			return;
		}
		final Collection<String> macs = new ArrayList<String>();
		synchronized (missingMacs) {
			if (missingMacs.size() == 0) {
				return;
			}
			while (macs.size() < 10 && missingMacs.size() > 0) {
				final String mac = missingMacs.pop();
				if (!wlanMap.containsKey(mac)) {
					macs.add(mac);
				}
			}
			missingMacs.addAll(macs);
		}

		source.requestMacLocations(macs, missingMacs, wlanMap);
	}

	private void requestMissing(final Collection<String> wlans) {
		if (!source.isSourceAvailable())
			return;
		addToMissing(missingInCache(wlans));
		if ((retriever == null || !retriever.isAlive()) && missingMacs != null && missingMacs.size() > 0) {
			retriever = new Thread(new Runnable() {
				private boolean hasWork() {
					synchronized (missingMacs) {
						return missingMacs != null && missingMacs.size() > 0;
					}
				}

				@Override
				public void run() {
					while (hasWork() && isOnline()) {
						requestLocations();
						final Location loc = getCurrentLocation();
						listener.onLocationChanged(loc);
					}
				}
			});
			retriever.start();
		}

	}
}
