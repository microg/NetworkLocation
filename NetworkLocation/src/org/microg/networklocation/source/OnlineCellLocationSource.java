package org.microg.networklocation.source;

import android.content.Context;
import android.location.Location;
import android.net.ConnectivityManager;
import android.util.Log;
import org.microg.networklocation.data.CellLocationData;
import org.microg.networklocation.database.CellMap;
import org.microg.networklocation.v2.CellSpec;
import org.microg.networklocation.v2.LocationSpec;

import java.util.Arrays;
import java.util.List;

public abstract class OnlineCellLocationSource implements CellLocationSource {
	private static final String TAG = "OnlineCellLocationSource";

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

	@Override
	public LocationSpec[] retrieveLocation(CellSpec... specs) {
		Log.d(TAG, "TODO: Implement: retrieveLocation(CellSpec...)");
		return new LocationSpec[0]; //TODO: Implement
	}

	@Override
	public List<LocationSpec> retrieveLocation(List<CellSpec> specs) {
		return Arrays.asList(retrieveLocation(specs.toArray(new CellSpec[specs.size()])));
	}
}
