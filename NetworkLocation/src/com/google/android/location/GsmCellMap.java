package com.google.android.location;

import android.location.Location;

public class GsmCellMap {
	private final DatabaseHelper helper;

	public GsmCellMap(DatabaseHelper helper) {
		this.helper = helper;
		helper.startAutoCloseThread();
	}

	public boolean containsKey(int mcc, int mnc, int cid) {
		return (helper.getLocationGsmCell(mcc, mnc, cid) != null);
	}

	public Location get(int mcc, int mnc, int cid) {
		return helper.getLocationGsmCell(mcc, mnc, cid);
	}

	public void put(int mcc, int mnc, int cid, Location location) {
		helper.insertGsmCellLocation(mcc, mnc, cid, location);
	}
}
