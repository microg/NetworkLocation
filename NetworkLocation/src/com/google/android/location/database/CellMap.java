package com.google.android.location.database;

import android.location.Location;

public class CellMap {
	private final DatabaseHelper helper;

	public CellMap(final DatabaseHelper helper) {
		this.helper = helper;
		helper.startAutoCloseThread();
	}

	public boolean containsKey(final int mcc, final int mnc, final int cid) {
		return (helper.getLocationGsmCell(mcc, mnc, cid) != null);
	}

	public Location get(final int mcc, final int mnc, final int cid) {
		return helper.getLocationGsmCell(mcc, mnc, cid);
	}

	public void put(final int mcc, final int mnc, final int cid,
			final Location location) {
		helper.insertGsmCellLocation(mcc, mnc, cid, location);
	}
}
