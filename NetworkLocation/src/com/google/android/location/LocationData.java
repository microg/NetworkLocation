package com.google.android.location;

import java.util.HashMap;
import java.util.Map;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

public class LocationData extends LocationDataProvider.Stub implements
		LocationListener {

	public static final String IDENTIFIER = "network";
	private static final String IMPORTANT_PROVIDER = GsmLocationData.IDENTIFIER;

	private final Map<String, LocationDataProvider> providers;
	private final Map<String, Location> locations;
	private final LocationListener listener;

	private boolean inBlockOp = false;

	public LocationData(LocationListener listener) {
		providers = new HashMap<String, LocationDataProvider>();
		locations = new HashMap<String, Location>();
		this.listener = listener;
	}

	public void addProvider(LocationDataProvider provider) {
		providers.put(provider.getIdentifier(), provider);
	}

	private Location calculateLocation() {
		boolean preDidImportant = false;
		Location location = null;
		if (locations.containsKey(IMPORTANT_PROVIDER)) {
			location = locations.get(IMPORTANT_PROVIDER);
			preDidImportant = true;
		}
		for (final Location loc : locations.values()) {
			if (loc == null) {
				continue;
			}
			if (loc.getProvider().equalsIgnoreCase(getIdentifier())) {
				continue;
			}
			if (preDidImportant
					&& loc.getProvider().equalsIgnoreCase(IMPORTANT_PROVIDER)) {
				continue;
			}
			if (location == null) {
				location = renameSource(loc);
			} else if (locationDistance(location, loc) < location.getAccuracy()
					+ loc.getAccuracy()) {
				location = renameSource(loc);
			}
		}
		return location;
	}

	@Override
	public Location getCurrentLocation() {
		inBlockOp = true;
		for (final LocationDataProvider provider : providers.values()) {
			locations.put(provider.getIdentifier(),
					provider.getCurrentLocation());
		}
		inBlockOp = false;
		final Location loc = calculateLocation();
		onLocationChanged(loc);
		return loc;
	}

	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}

	private double locationDistance(Location loc1, Location loc2) {
		final double R = 6371; // km earthrad
		final double dLat = Math.toRadians(loc2.getLatitude()
				- loc1.getLatitude());
		final double dLon = Math.toRadians(loc2.getLongitude()
				- loc1.getLongitude());
		final double lat1 = Math.toRadians(loc1.getLatitude());
		final double lat2 = Math.toRadians(loc2.getLatitude());

		final double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
				+ Math.sin(dLon / 2) * Math.sin(dLon / 2) * Math.cos(lat1)
				* Math.cos(lat2);
		final double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		Log.d("LocationData", "distance between " + loc1 + " and " + loc2
				+ " = " + R * c * 1000);
		return R * c * 1000;
	}

	@Override
	public void onLocationChanged(Location location) {
		if (location == null) {
			return;
		}
		if (!location.getProvider().equalsIgnoreCase(getIdentifier())) {
			locations.put(location.getProvider(), location);
		}
		if (!inBlockOp) {
			listener.onLocationChanged(calculateLocation());
		}
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle bundle) {
		// TODO Auto-generated method stub

	}

}
