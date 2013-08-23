package org.microg.netlocation.source;

import org.microg.netlocation.database.WlanMap;

import java.util.Collection;

public interface WlanLocationSource extends DataSource {
	public void requestMacLocations(final Collection<String> macs, final Collection<String> missingMacs,
									WlanMap wlanMap);
}
