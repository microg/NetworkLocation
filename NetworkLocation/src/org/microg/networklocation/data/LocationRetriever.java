package org.microg.networklocation.data;

import android.util.Log;
import org.microg.networklocation.database.LocationDatabase;
import org.microg.networklocation.source.LocationSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class LocationRetriever {
	private LocationDatabase locationDatabase;
	private List<LocationSource<CellSpec>> cellLocationSources = new ArrayList<LocationSource<CellSpec>>();
	private List<LocationSource<WlanSpec>> wlanLocationSources = new ArrayList<LocationSource<WlanSpec>>();

	public List<LocationSource<CellSpec>> getCellLocationSources() {
		return cellLocationSources;
	}

	public void setCellLocationSources(List<LocationSource<CellSpec>> cellLocationSources) {
		this.cellLocationSources = new ArrayList<LocationSource<CellSpec>>(cellLocationSources);
	}

	public List<LocationSource<WlanSpec>> getWlanLocationSources() {
		return wlanLocationSources;
	}

	public void setWlanLocationSources(List<LocationSource<WlanSpec>> wlanLocationSources) {
		this.wlanLocationSources = new ArrayList<LocationSource<WlanSpec>>(wlanLocationSources);
	}

	private static final String TAG = "v2LocationRetriever";

	public LocationRetriever(LocationDatabase locationDatabase) {
		this.locationDatabase = locationDatabase;
	}

	public void queueLocationRetrieval(CellSpec cellSpec) {
		Log.d(TAG, "TODO: Implement: queueLocationRetrieval(CellSpec)");
	}

	public void queueLocationRetrieval(WlanSpec wlanSpec) {
		Log.d(TAG, "TODO: Implement: queueLocationRetrieval(WlanSpec)");
	}

	public <T extends PropSpec> void queueLocationRetrieval(T spec) {
		if (spec instanceof CellSpec) {
			CellSpec cellSpec = (CellSpec) spec;
			queueLocationRetrieval(cellSpec);
		} else if (spec instanceof WlanSpec) {
			WlanSpec wlanSpec = (WlanSpec) spec;
			queueLocationRetrieval(wlanSpec);
		} else {
			throw new IllegalArgumentException("spec must be Cell or Wifi spec");
		}
	}

	private <T extends PropSpec> void retrieveLocation(Iterable<? extends LocationSource<T>> locationSources,
													   T... specs) {
		Collection<T> todo = new ArrayList<T>(Arrays.asList(specs));
		for (LocationSource<T> locationSource : locationSources) {
			for (LocationSpec<T> locationSpec : locationSource.retrieveLocation(todo)) {
				locationDatabase.put(locationSpec);
				todo.remove(locationSpec.getSource());
			}
			if (todo.isEmpty()) {
				break;
			}
		}
		for (T spec : todo) {
			locationDatabase.put(new LocationSpec<T>(spec));
		}
	}

	private void retrieveLocation(CellSpec... cellSpecs) {
		retrieveLocation(cellLocationSources, cellSpecs);
	}

	private void retrieveLocation(WlanSpec... wlanSpecs) {
		retrieveLocation(wlanLocationSources, wlanSpecs);
	}
}
