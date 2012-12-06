package com.google.android.location;

import android.location.LocationListener;
import android.os.IBinder;

public interface NetworkLocationProviderBase extends LocationListener {
	public IBinder getBinder();

	public void setData(LocationData data);
}
