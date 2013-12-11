package org.microg.networklocation.provider;

import android.location.Location;
import android.location.LocationListener;
import android.os.IBinder;
import org.microg.networklocation.data.LocationCalculator;

public interface NetworkLocationProvider {
	String NETWORK_LOCATION_TYPE = "networkLocationType";

	void disable();

	void enable();

	IBinder getBinder();

	boolean isActive();

	void onLocationChanged(Location paramLocation);

	void setCalculator(LocationCalculator locationCalculator);
}
