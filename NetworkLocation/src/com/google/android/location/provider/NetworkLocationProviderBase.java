package com.google.android.location.provider;

import android.location.LocationListener;
import android.os.IBinder;

import com.google.android.location.data.LocationData;

public interface NetworkLocationProviderBase extends LocationListener {
	public IBinder getBinder();

	public boolean isActive();

	public void setData(LocationData data);
}
