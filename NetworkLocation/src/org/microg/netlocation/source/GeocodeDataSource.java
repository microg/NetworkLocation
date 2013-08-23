package org.microg.netlocation.source;

import android.location.Address;

import java.util.List;
import java.util.Locale;

public interface GeocodeDataSource extends DataSource {
	public void addAdressesToListForLocation(double lat, double lon, Locale locale, List<Address> addrs);
}
