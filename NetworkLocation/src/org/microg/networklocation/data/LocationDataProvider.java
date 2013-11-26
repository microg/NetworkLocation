package org.microg.networklocation.data;

import android.location.Location;

public interface LocationDataProvider {
	Location getCurrentLocation();

	String getIdentifier();
}
