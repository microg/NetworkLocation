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

	public static void init(Context context) {
		if (context != null && instance == null
				&& Database.getInstance() != null) {
			instance = new LocationData(context, Database.getInstance());
		}
	}

	public static String niceMac(String mac) {
		mac = mac.toLowerCase();
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

	private final Database database;

	private LocationData(Context context, Database database) {
		this.context = context;
		this.database = database;
	}

	public android.location.Location calculateLocation(
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

	private Request createRequest(String... macs) {
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
		return calculateLocation(locs.values());
	}

	public Location getLocation(String mac) {
		return database.get(mac);
	}

	public Map<String, Location> getLocations(Collection<String> wlans) {
		final HashMap<String, Location> locs = new HashMap<String, Location>();
		for (final String wlan : wlans) {
			final Location loc = getLocation(wlan);
			if (loc != null) {
				locs.put(wlan, loc);
			}
		}
		return locs;
	}

	public Collection<String> getWLANs() {
		final ArrayList<String> wlans = new ArrayList<String>();
		final WifiManager wifiManager = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		final List<ScanResult> result = wifiManager.getScanResults();
		if (result != null) {
			for (final ScanResult scanResult : result) {
				final String mac = niceMac(scanResult.BSSID);
				wlans.add(mac);
			}
		}
		return wlans;
	}

	public Collection<String> missingInCache(Collection<String> wlans) {
		final ArrayList<String> macs = new ArrayList<String>();
		for (final String wlan : wlans) {
			if (!database.containsKey(wlan)) {
				macs.add(wlan);
			}
		}
		return macs;
	}

	private void requestLocations(String... macs) {
		if (macs == null || macs.length == 0) {
			return;
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
			while (i++ < 10 && in.read() != -1) {
				;
			}
			final Response response = Response.parseFrom(in);
			out.close();
			in.close();
			for (final ResponseWLAN rw : response.getWlanList()) {
				final String mac2 = niceMac(rw.getMac());
				final Location loc = new Location(
						NetworkLocationProvider.class.getName());
				loc.setLatitude(rw.getLocation().getLatitude() / 1E8F);
				loc.setLongitude(rw.getLocation().getLongitude() / 1E8F);
				loc.setAccuracy(rw.getLocation().getUnknown3());
				loc.setTime(new Date().getTime());
				database.put(mac2, loc);
			}

		} catch (final Exception e) {
			Log.e("LocationData", macs.toString(), e);
		}
	}

	public void requestMissing(Collection<String> wlans) {
		requestLocations(missingInCache(wlans).toArray(new String[0]));
	}

	public Map<String, Location> getNextFromCache(double latitude,
			double longitude, int num) {
		return database.getNext(latitude, longitude, num);
	}
}
