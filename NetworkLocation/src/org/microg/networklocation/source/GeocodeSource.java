package org.microg.networklocation.source;

import android.location.Address;

import java.util.List;
import java.util.Locale;

public interface GeocodeSource extends DataSource {
	List<Address> getFromLocation(double latitude, double longitude, String sourcePackage, Locale locale);

	List<Address> getFromLocationName(String locationName, double lowerLeftLatitude, double lowerLeftLongitude,
							 double upperRightLatitude, double upperRightLongitude, String sourcePackage, Locale locale);
}
