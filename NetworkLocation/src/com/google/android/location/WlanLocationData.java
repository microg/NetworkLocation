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
import java.util.Stack;

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

public class WlanLocationData extends LocationDataProvider.Stub {

	public final static String IDENTIFIER = "wlan";

	private final static String TAG = "WlanLocationData";

	public static Collection<String> getWLANs(final Context context) {
		if (context == null) {
			return null;
		}
		final ArrayList<String> wlans = new ArrayList<String>();
		final WifiManager wifiManager = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
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

	private final LocationListener listener;
	private final Stack<String> missingMacs;
	private Thread retriever;
	private final WlanMap wlanMap;

	public WlanLocationData(final Context context, final WlanMap wlanMap,
			final LocationListener listener) {
		this.context = context;
		this.wlanMap = wlanMap;
		this.listener = listener;
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

	private android.location.Location calculateLocation(
			final Collection<Location> values) {
		if (values == null || values.size() == 0) {
			return null;
		}
		float lat = 0;
		float lon = 0;
		float acc = 0;
		float minacc = Float.MAX_VALUE;
		int n = 0;
		for (final Location location : values) {
			if (location.getAccuracy() != -1 && location.getAccuracy() < 1000) {
				lat += location.getLatitude();
				lon += location.getLongitude();
				acc += location.getAccuracy();
				minacc = Math.min(minacc, location.getAccuracy());
				n++;
			}
		}
		final Location loc = new android.location.Location(getIdentifier());
		loc.setAccuracy(Math.max(42,
				Math.min((float) Math.exp(1 - n) * acc / n, minacc)));
		loc.setLatitude(lat / n);
		loc.setLongitude(lon / n);
		loc.setTime(new Date().getTime());
		return loc;
	}

	private Request createRequest(final Collection<String> macs) {
		final Request.Builder request = Request.newBuilder()
				.setSource("com.apple.maps").setUnknown3(0).setUnknown4(0);
		for (final String mac : macs) {
			request.addWlan(RequestWLAN.newBuilder().setMac(mac));
		}

		return request.build();
	}

	@Override
	public android.location.Location getCurrentLocation() {
		final Collection<String> wlans = getWLANs();
		requestMissing(wlans);
		final Map<String, Location> locs = getLocations(wlans);
		final Location location = calculateLocation(locs.values());
		if (location == null
				|| (location.getLatitude() == 0 && location.getLongitude() == 0)) {
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
		if (missingMacs == null) {
			return;
		}
		Collection<String> macs;
		synchronized (missingMacs) {
			if (missingMacs.size() == 0) {
				return;
			}
			macs = new ArrayList<String>();
			while (macs.size() < 10 && missingMacs.size() > 0) {
				final String mac = missingMacs.pop();
				if (!wlanMap.containsKey(mac)) {
					macs.add(mac);
				}
			}
			missingMacs.addAll(macs);
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
				sb.append("0x").append(n).append(" ");
			}
			Log.d(TAG, "Response first bytes: " + sb.toString());
			final Response response = Response.parseFrom(in);
			out.close();
			in.close();
			for (final ResponseWLAN rw : response.getWlanList()) {
				final String mac2 = niceMac(rw.getMac());
				final Location loc = new Location(IDENTIFIER);
				loc.setProvider(getIdentifier());
				loc.setLatitude(rw.getLocation().getLatitude() / 1E8F);
				loc.setLongitude(rw.getLocation().getLongitude() / 1E8F);
				loc.setAccuracy(rw.getLocation().getAccuracy());
				if (rw.getLocation().getAltitude() != -500) {
					loc.setAltitude(rw.getLocation().getAltitude());
				}
				loc.setTime(new Date().getTime());
				wlanMap.put(mac2, loc);
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
			Log.e(TAG, "requestLocations: " + macs, e);
		}
	}

	private void requestMissing(final Collection<String> wlans) {
		addToMissing(missingInCache(wlans));
		Log.d(TAG, "missingInCache: " + missingMacs);
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
