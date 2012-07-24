package com.google.android.location;

import java.util.Map;

import android.location.Location;

public class WlanMap {
	private final DatabaseHelper helper;

	public WlanMap(DatabaseHelper helper) {
		this.helper = helper;
		helper.startAutoCloseThread();
	}

	public boolean containsKey(String mac) {
		return (helper.getLocationWlan(mac) != null);
	}

	public Location get(String mac) {
		return helper.getLocationWlan(mac);
	}

	public Map<String, Location> getMap() {
		return helper.getWlanTable();
	}

	public Map<String, Location> getNext(double latitude, double longitude,
			int num) {
		return helper.getNextWlan(latitude, longitude, num);
	}

	public void put(String mac, Location location) {
		helper.insertWlanLocation(mac, location);
	}
}
