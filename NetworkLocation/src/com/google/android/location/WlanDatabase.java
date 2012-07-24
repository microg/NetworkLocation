package com.google.android.location;

import java.util.Map;

import android.content.Context;
import android.location.Location;

public class WlanDatabase {

	private static WlanDatabase instance;

	public static WlanDatabase getInstance() {
		return instance;
	}

	public static void init(Context context) {
		if (instance == null && context != null) {
			instance = new WlanDatabase(context);
		}
	}

	private final Thread autoClose;

	private boolean newRequest;

	private final WlanDatabaseHelper helper;

	private WlanDatabase(Context context) {
		helper = new WlanDatabaseHelper(context);
		autoClose = new Thread(new Runnable() {

			@Override
			public void run() {
				while (autoClose != null) {
					try {
						synchronized (autoClose) {
							autoClose.wait(120000); // wait 2min
						}
					} catch (final InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if (newRequest) {
						newRequest = false;
					} else if (helper.isOpen()) {
						helper.close();
					}
				}
			}
		});
		newRequest = false;
		autoClose.start();
	}

	public boolean containsKey(String mac) {
		newRequest = true;
		return (helper.getLocation(mac) != null);
	}

	public Location get(String mac) {
		newRequest = true;
		return helper.getLocation(mac);
	}

	public Map<String, Location> getMap() {
		newRequest = true;
		return helper.getDatabase();
	}

	public Map<String, Location> getNext(double latitude, double longitude,
			int num) {
		newRequest = true;
		return helper.getNext(latitude, longitude, num);
	}

	public void put(String mac, Location location) {
		newRequest = true;
		helper.insertLocation(mac, location);
	}
}
