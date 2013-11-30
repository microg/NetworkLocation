package org.microg.networklocation.provider;

import android.location.LocationListener;
import android.os.IBinder;
import org.microg.networklocation.data.LocationCalculator;

public interface NetworkLocationProviderBase extends LocationListener {
	String NETWORK_LOCATION_TYPE = "networkLocationType";

	public void disable();

	public void enable();

	public IBinder getBinder();

	public boolean isActive();

	public void setCalculator(LocationCalculator locationCalculator);
}
