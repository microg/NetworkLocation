package org.microg.networklocation.database;

import android.util.Log;
import org.microg.networklocation.data.LocationSpec;
import org.microg.networklocation.data.PropSpec;

public class LocationDatabase {
	private static final String TAG = "v2LocationDatabase";

	public <T extends PropSpec> LocationSpec<T> get(T propSpec) {
		LocationSpec<T> locationSpec = get(propSpec.getIdentBlob());
		locationSpec.setSource(propSpec);
		return locationSpec;
	}

	private <T extends PropSpec> LocationSpec<T> get(byte[] identBlob) {
		Log.d(TAG, "TODO: Implement: get(byte[])");

		return null;
	}

	public <T extends PropSpec> void put(LocationSpec<T> locationSpec) {
		put(locationSpec.getSource().getIdentBlob(), locationSpec);
	}

	private <T extends PropSpec> void put(byte[] identBlob, LocationSpec<T> locationSpec) {
		Log.d(TAG, "TODO: Implement: put(byte[], LocationSpec)");
	}
}
