package com.google.android.location.data;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

import com.google.android.location.NetworkLocationService;

public class LocationData extends LocationDataProvider.Stub implements
		LocationListener {

	public static final String IDENTIFIER = "network";
	private static final String IMPORTANT_PROVIDER = CellLocationData.IDENTIFIER;
	private static final int NEW_TIME = 30000;
	private Boolean inBlockOp = false;
	private final LocationListener listener;
	private final Map<String, Location> locations;
	private Location overlayLocation = null;
	private final Map<String, LocationDataProvider> providers;
	private Location realLocation;

	private final NetworkLocationService service;

	public LocationData(final LocationListener listener,
			final NetworkLocationService service) {
		providers = new HashMap<String, LocationDataProvider>();
		locations = new HashMap<String, Location>();
		this.listener = listener;
		this.service = service;
	}

	public void addProvider(final LocationDataProvider provider) {
		providers.put(provider.getIdentifier(), provider);
	}

	private Location calculateLocation() {
		boolean preDidImportant = false;
		Location location = null;
		final long newt = new Date().getTime() - NEW_TIME;
		if (locations.containsKey(IMPORTANT_PROVIDER)
				&& locations.get(IMPORTANT_PROVIDER) != null) {
			final long oldt = locations.get(IMPORTANT_PROVIDER).getTime();
			location = new Location(locations.get(IMPORTANT_PROVIDER));
			if (oldt < newt) {
				location.setAccuracy(location.getAccuracy()
						+ ((newt - oldt) / 50));
			}
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
			final long oldt = loc.getTime();
			if (location == null) {
				location = new Location(loc);
			} else if (locationDistance(location, loc) < location.getAccuracy()
					+ loc.getAccuracy()
					+ ((oldt < newt) ? ((newt - oldt) / 50) : 0)) {
				location = new Location(loc);
			}
			if (oldt < newt) {
				location.setAccuracy(location.getAccuracy()
						+ ((newt - oldt) / 50));
			}
		}
		realLocation = location;
		if (overlayLocation != null
				&& overlayLocation.getTime() + 5 * 60 * 1000 > System
						.currentTimeMillis()) {
			Log.d("LocationData", "overlay:" + overlayLocation);
			location = new Location("wifi");
			location.setAccuracy(42);
			location.setAltitude(overlayLocation.getAltitude());
			location.setLatitude(overlayLocation.getLatitude());
			location.setLongitude(overlayLocation.getLongitude());
			location.setProvider("wifi");
			return location;
		} else if (overlayLocation != null) {
			overlayLocation = null;
			service.reInitOverlayNotification();
		}
		return location;
	}

	public void clearOverlayLocation() {
		overlayLocation = null;
		if (!inBlockOp) {
			listener.onLocationChanged(calculateLocation());
		}
	}

	@Override
	public Location getCurrentLocation() {
		synchronized (inBlockOp) {
			inBlockOp = true;
			for (final LocationDataProvider provider : providers.values()) {
				locations.put(provider.getIdentifier(),
						provider.getCurrentLocation());
			}
			inBlockOp = false;
		}
		final Location loc = calculateLocation();
		onLocationChanged(loc);
		return loc;
	}

	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}

	public Location getOverlayLocation() {
		return overlayLocation;
	}

	public Location getRealLocation() {
		return realLocation;
	}

	public NetworkLocationService getService() {
		return service;
	}

	private double locationDistance(final Location loc1, final Location loc2) {
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
		return R * c * 1000;
	}

	@Override
	public void onLocationChanged(final Location location) {
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
	public void onProviderDisabled(final String provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderEnabled(final String provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStatusChanged(final String provider, final int status,
			final Bundle bundle) {
		// TODO Auto-generated method stub

	}

	public void setOverlayLocation(final double lat, final double lon,
			final double alt) {
		final Location l = new Location("static");
		l.setAltitude(alt);
		l.setLatitude(lat);
		l.setLongitude(lon);
		l.setTime(System.currentTimeMillis());
		overlayLocation = l;
		if (!inBlockOp) {
			listener.onLocationChanged(calculateLocation());
		}
	}

}
