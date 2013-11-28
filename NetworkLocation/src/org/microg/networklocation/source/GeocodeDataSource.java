package org.microg.networklocation.source;

import android.location.Address;

import java.util.List;
import java.util.Locale;

public interface GeocodeDataSource extends DataSource {
	void addAdressesToListForLocation(double lat, double lon, Locale locale, List<Address> addrs);
}
