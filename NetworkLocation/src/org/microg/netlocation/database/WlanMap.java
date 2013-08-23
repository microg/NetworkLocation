package org.microg.netlocation.database;

import android.location.Location;

import java.util.Map;

public class WlanMap {
	private final DatabaseHelper helper;

	public WlanMap(final DatabaseHelper helper) {
		this.helper = helper;
	}

	public boolean containsKey(final String mac) {
		return (helper.getLocationWlan(mac) != null);
	}

	public Location get(final String mac) {
		return helper.getLocationWlan(mac);
	}

	public Map<String, Location> getNext(final double latitude, final double longitude, final int num) {
		return helper.getNextWlan(latitude, longitude, num);
	}

	public void put(final String mac, final Location location) {
		helper.insertWlanLocation(mac, location);
	}
}
