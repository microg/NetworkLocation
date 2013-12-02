package org.microg.networklocation.source;

import android.location.Address;

import java.util.List;
import java.util.Locale;

public interface GeocodeSource extends DataSource {
	List<Address> getFromLocation(double lat, double lon, Locale locale);
}
