package com.google.android.location.source;

import java.util.Collection;

import com.google.android.location.database.WlanMap;

public interface WlanLocationSource {
	public void requestMacLocations(final Collection<String> macs,
			final Collection<String> missingMacs, WlanMap wlanMap);
}
