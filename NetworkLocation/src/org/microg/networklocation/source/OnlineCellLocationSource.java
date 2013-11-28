package org.microg.networklocation.source;

import android.content.Context;
import android.location.Location;
import android.net.ConnectivityManager;
import org.microg.networklocation.data.CellLocationData;
import org.microg.networklocation.database.CellMap;
import org.microg.networklocation.data.CellSpec;
import org.microg.networklocation.data.LocationSpec;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class OnlineCellLocationSource extends CellLocationSource {
	private static final String TAG = "OnlineCellLocationSource";
	private final ConnectivityManager connectivityManager;
	private final OnlineCellLocationRetriever locationRetriever;

	protected OnlineCellLocationSource(Context context, OnlineCellLocationRetriever locationRetriever) {
		this((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE), locationRetriever);
	}

	protected OnlineCellLocationSource(ConnectivityManager connectivityManager,
									   OnlineCellLocationRetriever locationRetriever) {
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
	public Collection<LocationSpec<CellSpec>> retrieveLocation(Collection<CellSpec> cellSpecs) {
		List<LocationSpec<CellSpec>> locationSpecs = new ArrayList<LocationSpec<CellSpec>>();
		for (CellSpec cellSpec : cellSpecs) {
			OnlineCellLocationRetriever.Response response = locationRetriever
					.retrieveCellLocation(cellSpec.getMcc(), cellSpec.getMnc(), cellSpec.getLac(), cellSpec.getCid());
			if (response != null) {
				locationSpecs.add(new LocationSpec<CellSpec>(cellSpec, response.getLatitude(), response.getLongitude(),
															 response.getAccuracy()));
			}
		}
		return locationSpecs;
	}
}
