package org.microg.networklocation.source;

import org.microg.networklocation.database.WlanMap;

import java.util.Collection;

public interface WlanLocationSource extends DataSource {
	public void requestMacLocations(final Collection<String> macs, final Collection<String> missingMacs,
									WlanMap wlanMap);
}
