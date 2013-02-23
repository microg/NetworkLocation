package com.google.android.location.database;

import java.util.Map;

import android.location.Location;

public class WlanMap {
	private final DatabaseHelper helper;

	public WlanMap(final DatabaseHelper helper) {
		this.helper = helper;
		helper.startAutoCloseThread();
	}

	public boolean containsKey(final String mac) {
		return (helper.getLocationWlan(mac) != null);
	}

	public Location get(final String mac) {
		return helper.getLocationWlan(mac);
	}

	public Map<String, Location> getNext(final double latitude,
			final double longitude, final int num) {
		return helper.getNextWlan(latitude, longitude, num);
	}

	public void put(final String mac, final Location location) {
		helper.insertWlanLocation(mac, location);
	}
}
