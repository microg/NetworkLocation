package com.google.android.location;

import android.location.LocationListener;
import android.os.IBinder;

public interface LocationBinder extends LocationListener {
	public IBinder getBinder();

	public void setData(LocationData data);
}
