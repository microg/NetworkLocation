package com.google.android.location;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.apple.iphone.services.LocationsProtos.Request;
import com.apple.iphone.services.LocationsProtos.RequestWLAN;
import com.apple.iphone.services.LocationsProtos.Response;
import com.apple.iphone.services.LocationsProtos.ResponseWLAN;

public class LocationData {
	private static LocationData instance;

	public static LocationData getInstance() {
		return instance;
	}

	public static void init(Context context, LocationListener listener) {
		if (context != null && instance == null
				&& Database.getInstance() != null) {
			instance = new LocationData(context, Database.getInstance(),
					listener);
		}
	}

	private Collection<String> missingMacs;

	private final Context context;
	private final Database database;
	private final LocationListener listener;

	private Thread retriever;

	private LocationData(Context context, Database database,
			LocationListener listener) {
		this.context = context;
		this.database = database;
		this.listener = listener;
		missingMacs = new ArrayList<String>();
	}

	private android.location.Location calculateLocation(
			Collection<Location> values) {
		if (values == null || values.size() == 0) {
			return null;
		}
		float lat = 0;
		float lon = 0;
		float acc = 0;
		int n = 0;
		for (final Location location : values) {
			if (location.getAccuracy() != -1 && location.getAccuracy() < 1000) {
				lat += location.getLatitude();
				lon += location.getLongitude();
				acc += location.getAccuracy();
				n++;
			}
		}
		final Location loc = new android.location.Location("network");
		loc.setAccuracy(acc / n);
		loc.setLatitude(lat / n);
		loc.setLongitude(lon / n);
		loc.setTime(new Date().getTime());
		return loc;
	}

	private Request createRequest(Collection<String> macs) {
		final Request.Builder request = Request.newBuilder()
				.setSource("com.apple.maps").setUnknown3(0).setUnknown4(0);
		for (final String mac : macs) {
			request.addWlan(RequestWLAN.newBuilder().setMac(mac));
		}

		return request.build();
	}

	public Map<String, Location> getCache() {
		return database.getMap();
	}

	public android.location.Location getCurrentLocation() {
		final Collection<String> wlans = getWLANs();
		requestMissing(wlans);
		final Map<String, Location> locs = getLocations(wlans);
		final Location loc = calculateLocation(locs.values());
		listener.onLocationChanged(loc);
		return loc;
	}

	private Location getLocation(String mac) {
		return database.get(mac);
	}

	private Map<String, Location> getLocations(Collection<String> wlans) {
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
		final ArrayList<String> wlans = new ArrayList<String>();
		final WifiManager wifiManager = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		final List<ScanResult> result = wifiManager.getScanResults();
		if (result != null) {
			for (final ScanResult scanResult : result) {
				final String mac = scanResult.BSSID;
				wlans.add(mac);
			}
		}
		return wlans;
	}

	private Collection<String> missingInCache(Collection<String> wlans) {
		final ArrayList<String> macs = new ArrayList<String>();
		for (final String wlan : wlans) {
			if (!database.containsKey(wlan)) {
				macs.add(wlan);
			}
		}
		return macs;
	}

	private void requestLocations() {
		if (missingMacs == null) {
			return;
		}
		Collection<String> macs;
		synchronized (missingMacs) {
			if (missingMacs.size() == 0) {
				return;
			}
			macs = new ArrayList<String>();
			for (String mac : missingMacs) {
				if (!database.containsKey(mac))
					macs.add(mac);
				if (macs.size() > 10)
					break;
			}
		}

		try {
			final URL url = new URL(
					"https://iphone-services.apple.com/clls/wloc");
			final Request request = createRequest(macs);
			final byte[] bytea = new byte[] { 0, 1, 0, 5, 101, 110, 95, 85, 83,
					0, 0, 0, 11, 52, 46, 50, 46, 49, 46, 56, 67, 49, 52, 56, 0,
					0, 0, 1, 0, 0, 0 }; // 120
			final byte[] byteb = request.toByteArray();
			final byte[] bytes = new byte[bytea.length + byteb.length + 1];
			for (int i = 0; i < bytea.length; i++) {
				bytes[i] = bytea[i];
			}
			bytes[bytea.length] = (byte) byteb.length;
			for (int i = 0; i < byteb.length; i++) {
				bytes[i + bytea.length + 1] = byteb[i];
			}
			final HttpsURLConnection connection = (HttpsURLConnection) url
					.openConnection();
			connection.setRequestMethod("POST");
			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setUseCaches(false);
			connection.setRequestProperty("Content-Type",
					"application/x-www-form-urlencoded");
			connection.setRequestProperty("Content-Length",
					String.valueOf(bytes.length));
			final OutputStream out = connection.getOutputStream();
			out.write(bytes);
			out.flush();
			final InputStream in = connection.getInputStream();
			int i = 0;
			int n = -1;
			final StringBuilder sb = new StringBuilder();
			while (i++ < 10 && (n = in.read()) != -1) {
				sb.append((char) n);
			}
			Log.d(LocationData.class.getName(),
					"Response first bytes: " + sb.toString());
			final Response response = Response.parseFrom(in);
			out.close();
			in.close();
			for (final ResponseWLAN rw : response.getWlanList()) {
				final String mac2 = rw.getMac();
				final Location loc = new Location(
						NetworkLocationProvider.class.getName());
				loc.setProvider("network");
				loc.setLatitude(rw.getLocation().getLatitude() / 1E8F);
				loc.setLongitude(rw.getLocation().getLongitude() / 1E8F);
				loc.setAccuracy(rw.getLocation().getUnknown3());
				loc.setTime(new Date().getTime());
				database.put(mac2, loc);
				if (macs.contains(mac2)) {
					macs.remove(mac2);
				}
				synchronized (missingMacs) {
					if (missingMacs.contains(mac2)) {
						missingMacs.remove(mac2);
					}
				}
			}

		} catch (final Exception e) {
			Log.e("LocationData", "requestLocations: " + macs, e);
		}
	}

	private void addToMissing(String wlan) {
		synchronized (missingMacs) {
			if (!missingMacs.contains(wlan)) {
				missingMacs.add(wlan);
			}
		}
	}

	private void addToMissing(Collection<String> wlans) {
		for (String wlan : wlans) {
			addToMissing(wlan);
		}
	}

	private void requestMissing(Collection<String> wlans) {
		addToMissing(missingInCache(wlans));
		Log.d(LocationData.class.getName(), "missingInCache: " + missingMacs);
		if ((retriever == null || !retriever.isAlive()) && missingMacs != null
				&& missingMacs.size() > 0) {
			retriever = new Thread(new Runnable() {
				private boolean hasWork() {
					synchronized (missingMacs) {
						return missingMacs != null && missingMacs.size() > 0;
					}
				}

				@Override
				public void run() {
					while (hasWork()) {
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
