package org.microg.netlocation.provider;

import android.location.LocationListener;
import android.os.IBinder;
import org.microg.netlocation.data.LocationData;

public interface NetworkLocationProviderBase extends LocationListener {
	public IBinder getBinder();

	public boolean isActive();

	public void setData(LocationData data);

	public void disable();

	public void enable();
}
