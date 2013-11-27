package org.microg.networklocation.v2;

import android.location.Location;
import android.util.Log;
import org.microg.networklocation.source.CellLocationSource;
import org.microg.networklocation.source.LocationSource;
import org.microg.networklocation.source.WlanLocationSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LocationCalculator {
	private static final String TAG = "v2LocationCalculator";
	private List<CellLocationSource> cellLocationSources;
	private List<WlanLocationSource> wlanLocationSources;
	private LocationDatabase locationDatabase;

	private CellSpec[] getCurrentCells() {
		Log.d(TAG, "TODO: Implement: getCurrentCells()");
		return new CellSpec[0];
	}

	public Location getCurrentLocation() {
		Log.d(TAG, "TODO: Implement: getCurrentLocation()");
		LocationSpec[] cellLocationSpecs = getLocation(getCurrentCells());
		return null;
	}

	private WlanSpec[] getCurrentWlans() {
		Log.d(TAG, "TODO: Implement: getCurrentWlans()");
		return new WlanSpec[0];
	}

	private LocationSpec[] getLocation(WlanSpec... wlanSpecs) {
		List<LocationSpec> locationSpecs = new ArrayList<LocationSpec>();
		for (WlanSpec wlanSpec : wlanSpecs) {
			LocationSpec locationSpec = locationDatabase.get(wlanSpec);
			if (locationSpec == null) {
				queueLocationRetrieval(wlanSpec);
			} else {
				locationSpecs.add(locationSpec);
			}
		}
		return locationSpecs.toArray(new LocationSpec[locationSpecs.size()]);
	}

	private LocationSpec[] getLocation(CellSpec... cellSpecs) {
		List<LocationSpec> locationSpecs = new ArrayList<LocationSpec>();
		for (CellSpec cellSpec : cellSpecs) {
			LocationSpec locationSpec = locationDatabase.get(cellSpec);
			if (locationSpec == null) {
				queueLocationRetrieval(cellSpec);
			} else {
				locationSpecs.add(locationSpec);
			}
		}
		return locationSpecs.toArray(new LocationSpec[locationSpecs.size()]);
	}

	private void queueLocationRetrieval(CellSpec cellSpec) {
		Log.d(TAG, "TODO: Implement: queueLocationRetrieval(CellSpec)");
	}

	private void queueLocationRetrieval(WlanSpec wlanSpec) {
		Log.d(TAG, "TODO: Implement: queueLocationRetrieval(WlanSpec)");
	}

	private <T extends PropSpec> void retrieveLocation(Iterable<? extends LocationSource<T>> locationSources, T... specs) {
		List<T> todo = new ArrayList<T>(Arrays.asList(specs));
		for (LocationSource<T> locationSource : locationSources) {
			for (LocationSpec locationSpec : locationSource.retrieveLocation(todo)) {
				locationDatabase.put(locationSpec);
				todo.remove(locationSpec.getSource());
			}
			if (todo.isEmpty()) {
				break;
			}
		}
		for (T spec : todo) {
			locationDatabase.put(new LocationSpec(spec));
		}
	}

	private void retrieveLocation(CellSpec... cellSpecs) {
		retrieveLocation(cellLocationSources, cellSpecs);
	}

	private void retrieveLocation(WlanSpec... wlanSpecs) {
		retrieveLocation(wlanLocationSources, wlanSpecs);
	}

}

