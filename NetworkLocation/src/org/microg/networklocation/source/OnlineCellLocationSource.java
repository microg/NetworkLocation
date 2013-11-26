package org.microg.networklocation.source;

import android.content.Context;
import android.location.Location;
import android.net.ConnectivityManager;
import org.microg.networklocation.data.CellLocationData;
import org.microg.networklocation.database.CellMap;

public abstract class OnlineCellLocationSource implements CellLocationSource {
	private final ConnectivityManager connectivityManager;
	private final OnlineCellLocationRetriever locationRetriever;

	protected OnlineCellLocationSource(Context context, OnlineCellLocationRetriever locationRetriever) {
		this((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE), locationRetriever);
	}

	protected OnlineCellLocationSource(ConnectivityManager connectivityManager, OnlineCellLocationRetriever locationRetriever) {
		this.connectivityManager = connectivityManager;
		this.locationRetriever = locationRetriever;
	}

	@Override
	public boolean isSourceAvailable() {
		return (connectivityManager.getActiveNetworkInfo() != null) &&
			   connectivityManager.getActiveNetworkInfo().isAvailable() &&
			   connectivityManager.getActiveNetworkInfo().isConnected();
	}

	@Override
	public void requestCellLocation(int mcc, int mnc, int cid, int lac, CellMap cellMap) {
		OnlineCellLocationRetriever.Response response = locationRetriever.retrieveCellLocation(mcc, mnc, lac, cid);
		if (response != null) {
			Location location = new Location(CellLocationData.IDENTIFIER);
			location.setLatitude(response.getLatitude());
			location.setLongitude(response.getLongitude());
			location.setAccuracy(response.getAccuracy());
			cellMap.put(mcc, mnc, cid, location);
		}
	}
}
