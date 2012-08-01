package com.google.android.location;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.telephony.CellLocation;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;

public class GsmLocationData extends LocationDataProvider.Stub {

	private static final String TAG = GsmLocationData.class.getName();

	public static final String IDENTIFIER = "gsm";
	private final GsmCellMap gsmMap;
	private final LocationListener listener;
	private final Context context;
	private TelephonyManager telephonyManager;

	public GsmLocationData(Context context, GsmCellMap gsmMap,
			LocationListener listener) {
		this.context = context;
		this.gsmMap = gsmMap;
		this.listener = listener;
	}

	@Override
	public Location getCurrentLocation() {
		if (telephonyManager == null) {
			telephonyManager = (TelephonyManager) context
					.getSystemService(Context.TELEPHONY_SERVICE);
		}
		final GsmCellLocation cell = getGsmCellLocation();
		final String operator = telephonyManager.getNetworkOperator();
		if (cell == null || operator == null) {
			Log.d(TAG, "could not get location via gsm");
			return null;
		}
		final Location location = getLocation(operator, cell);
		if (location == null) {
			Log.d(TAG, "could not get location via gsm");
			return null;
		}
		listener.onLocationChanged(location);
		return location;
	}

	private GsmCellLocation getGsmCellLocation() {
		final CellLocation cellLocation = telephonyManager.getCellLocation();
		if (cellLocation instanceof GsmCellLocation) {
			return (GsmCellLocation) cellLocation;
		}
		return null;
	}

	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}

	private Location getLocation(int mcc, int mnc, int cid) {
		Location result = renameSource(gsmMap.get(mcc, mnc, cid));
		if (result == null) {
			Log.w(TAG, "gsm cell is not in database: " + mcc + "/" + mnc + "/"
					+ cid);
		}
		return result;
	}

	private Location getLocation(String operator, GsmCellLocation cell) {
		if (operator == null || operator.length() < 3) {
			Log.w(TAG,
					"Not connected to any gsm cell - won't track location...");
			return null;
		}
		return getLocation(operator.substring(0, 3), operator.substring(3),
				cell.getCid());
	}

	private Location getLocation(String mcc, String mnc, int cid) {
		return getLocation(Integer.parseInt(mcc), Integer.parseInt(mnc), cid);
	}

}
