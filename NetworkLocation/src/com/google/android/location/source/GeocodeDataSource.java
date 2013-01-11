package com.google.android.location.source;

import java.util.List;
import java.util.Locale;

import android.location.Address;

public interface GeocodeDataSource {
	public void addAdressesToListForLocation(double lat, double lon,
			Locale locale, List<Address> addrs);
}
