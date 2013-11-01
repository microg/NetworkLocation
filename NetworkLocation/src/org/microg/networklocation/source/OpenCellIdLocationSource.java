package org.microg.networklocation.source;

import android.content.Context;
import android.location.Location;
import android.net.ConnectivityManager;
import org.microg.networklocation.data.CellLocationData;
import org.microg.networklocation.database.CellMap;
import org.microg.networklocation.opencellid.SingleLocationRetriever;

public class OpenCellIdLocationSource implements CellLocationSource {

	private static final String TAG = "OpenCellIdLocationSource";
	private final ConnectivityManager connectivityManager;

	public OpenCellIdLocationSource(Context context) {
		this((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
	}

	public OpenCellIdLocationSource(ConnectivityManager connectivityManager) {
		this.connectivityManager = connectivityManager;
	}

	@Override
	public boolean isSourceAvailable() {
		return (connectivityManager.getActiveNetworkInfo() != null) &&
			   connectivityManager.getActiveNetworkInfo().isAvailable() &&
			   connectivityManager.getActiveNetworkInfo().isConnected();
	}

	@Override
	public void requestCellLocation(int mcc, int mnc, int cid, CellMap cellMap) {
		SingleLocationRetriever.Response response = SingleLocationRetriever.retrieveLocation(mcc, mnc, cid);
		if (response != null) {
			Location location = new Location(CellLocationData.IDENTIFIER);
			location.setLatitude(response.getLatitude());
			location.setLongitude(response.getLongitude());
			location.setAccuracy(response.getAccuracy());
			cellMap.put(mcc, mnc, cid, location);
		}
	}
}
