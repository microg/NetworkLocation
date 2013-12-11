package org.microg.networklocation.database;

import android.location.Address;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This provides basic caching capabilities until the real database is ready.
 */
public class GeocodeDatabase {
	private Map<Long, List<Address>> intDb = new HashMap<Long, List<Address>>();
	private Map<String, List<Address>> stringDb = new HashMap<String, List<Address>>();

	private static long dbIdent(double latitude, double longitude) {
		int l1 = (int) (latitude * 1000);
		int l2 = (int) (longitude * 1000);
		long ll1 = l1 < 0 ? 1 << 32 + (-l1) : l1;
		long ll2 = (l2 < 0 ? 1 << 32 + (-l2) : l2) << 32;
		return ll1 & ll2;
	}

	public List<Address> get(String locationName) {
		return stringDb.get(locationName);
	}

	public List<Address> get(double latitude, double longitude) {
		return intDb.get(dbIdent(latitude, longitude));
	}

	public void put(double latitude, double longitude, List<Address> addresses) {
		intDb.put(dbIdent(latitude, longitude), addresses);
	}

	public void put(String locationName, List<Address> addresses) {
		stringDb.put(locationName, addresses);
	}
}
