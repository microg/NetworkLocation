package org.microg.networklocation.provider;

import android.location.LocationListener;
import android.os.IBinder;
import org.microg.networklocation.data.LocationData;

public interface NetworkLocationProviderBase extends LocationListener {
	String NETWORK_LOCATION_TYPE = "networkLocationType";

	public IBinder getBinder();

	public boolean isActive();

	public void setData(LocationData data);

	public void disable();

	public void enable();
}
