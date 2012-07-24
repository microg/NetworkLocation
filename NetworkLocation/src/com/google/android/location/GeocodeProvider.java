package com.google.android.location;

import java.util.List;

import android.content.Context;
import android.location.Address;
import android.location.GeocoderParams;
import android.util.Log;

public class GeocodeProvider extends
		com.android.location.provider.GeocodeProvider {
	private static final String TAG = GeocodeProvider.class.getName();

	private static GeocodeProvider instance;

	public static GeocodeProvider getInstance() {
		return instance;
	}

	public static void init(NetworkLocationService networkLocationService) {
		instance = new GeocodeProvider(networkLocationService);
	}

	public GeocodeProvider(Context context) {
		Log.i(TAG, "new Service-Object constructed");
	}

	@Override
	public String onGetFromLocation(double arg0, double arg1, int arg2,
			GeocoderParams arg3, List<Address> arg4) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String onGetFromLocationName(String arg0, double arg1, double arg2,
			double arg3, double arg4, int arg5, GeocoderParams arg6,
			List<Address> arg7) {
		// TODO Auto-generated method stub
		return null;
	}

}
