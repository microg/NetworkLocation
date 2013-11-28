package org.microg.networklocation.data;

import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import org.microg.networklocation.database.LocationDatabase;
import org.microg.networklocation.source.LocationSource;

import java.util.*;

public class LocationCalculator {
	public static final int MAX_WIFI_RADIUS = 500;
	private static final String TAG = "v2LocationCalculator";
	private final LocationDatabase locationDatabase;
	private final LocationRetriever locationRetriever;

	public LocationCalculator(LocationDatabase locationDatabase, LocationRetriever locationRetriever) {
		this.locationDatabase = locationDatabase;
		this.locationRetriever = locationRetriever;
	}

	private static <T extends PropSpec> Collection<Collection<LocationSpec<T>>> divideInClasses(
			Collection<LocationSpec<T>> locationSpecs, double accuracy) {
		Collection<Collection<LocationSpec<T>>> classes = new ArrayList<Collection<LocationSpec<T>>>();
		for (LocationSpec<T> locationSpec : locationSpecs) {
			boolean used = false;
			for (Collection<LocationSpec<T>> locClass : classes) {
				if (locationCompatibleWithClass(locationSpec, locClass, accuracy)) {
					locClass.add(locationSpec);
					used = true;
				}
			}
			if (!used) {
				Collection<LocationSpec<T>> locClass = new ArrayList<LocationSpec<T>>();
				locClass.add(locationSpec);
				classes.add(locClass);
			}
		}
		return classes;
	}

	private static <T extends PropSpec> boolean locationCompatibleWithClass(LocationSpec<T> locationSpec,
																			Collection<LocationSpec<T>> locClass,
																			double accuracy) {
		for (LocationSpec<T> spec : locClass) {
			if ((locationSpec.distanceBetween(spec) - locationSpec.getAccuracy() - spec.getAccuracy() -
				 accuracy) < 0) {
				return true;
			}
		}
		return false;
	}

	private static <T extends PropSpec> boolean locationCompatibleWithClass(Location location,
																			Collection<LocationSpec<T>> locClass) {
		for (LocationSpec<T> spec : locClass) {
			if ((spec.distanceBetween(location) - location.getAccuracy() - spec.getAccuracy()) < 0) {
				return true;
			}
		}
		return false;
	}

	private <T extends PropSpec> Location getAverageLocation(Collection<LocationSpec<T>> locationSpecs) {
		// TODO: This is a stupid way to do this, we could do better by using the signal strength and triangulation
		double latSum = 0, lonSum = 0, accSum = 0;
		for (LocationSpec<T> cellLocationSpec : locationSpecs) {
			latSum += cellLocationSpec.getLatitude();
			lonSum += cellLocationSpec.getLongitude();
			accSum += cellLocationSpec.getAccuracy();
		}

		Location location = new Location("network");
		location.setAccuracy((float) (accSum / locationSpecs.size()));
		location.setLatitude(latSum / locationSpecs.size());
		location.setLongitude(latSum / locationSpecs.size());
		return location;
	}

	public Location getCurrentCellLocation() {
		Collection<LocationSpec<CellSpec>> cellLocationSpecs = getLocation(getCurrentCells());

		if (cellLocationSpecs.isEmpty()) {
			return null;
		}
		Location location = getAverageLocation(cellLocationSpecs);


		Bundle b = new Bundle();
		b.putString("networkLocationType", "cell");
		location.setExtras(b);
		return location;
	}

	private Collection<CellSpec> getCurrentCells() {
		Log.d(TAG, "TODO: Implement: getCurrentCells()");
		return Collections.emptySet();
	}

	public Location getCurrentLocation() {
		Location cellLocation = getCurrentCellLocation();
		Location wlanLocation = getCurrentWlanLocation(cellLocation);
		if (wlanLocation != null) {
			return wlanLocation;
		}
		return cellLocation;
	}

	public Location getCurrentWlanLocation(Location cellLocation) {
		Collection<LocationSpec<WlanSpec>> wlanLocationSpecs = getLocation(getCurrentWlans());

		if ((wlanLocationSpecs.size() < 2) || ((cellLocation == null) && (wlanLocationSpecs.size() < 3))) {
			return null;
		}

		Location location = null;
		if (cellLocation == null) {
			List<Collection<LocationSpec<WlanSpec>>> classes = new ArrayList<Collection<LocationSpec<WlanSpec>>>(
					divideInClasses(wlanLocationSpecs, MAX_WIFI_RADIUS));
			Collections.sort(classes, CollectionSizeComparator.INSTANCE);
			location = getAverageLocation(classes.get(0));
		} else {
			List<Collection<LocationSpec<WlanSpec>>> classes = new ArrayList<Collection<LocationSpec<WlanSpec>>>(
					divideInClasses(wlanLocationSpecs, cellLocation.getAccuracy()));
			Collections.sort(classes, CollectionSizeComparator.INSTANCE);
			for (Collection<LocationSpec<WlanSpec>> locClass : classes) {
				if (locationCompatibleWithClass(cellLocation, locClass)) {
					location = getAverageLocation(locClass);
					break;
				}
			}
		}
		if (location != null) {
			Bundle b = new Bundle();
			b.putString("networkLocationType", "wifi");
			location.setExtras(b);
		}
		return location;
	}

	private Collection<WlanSpec> getCurrentWlans() {
		Log.d(TAG, "TODO: Implement: getCurrentWlans()");
		return Collections.emptySet();
	}

	private <T extends PropSpec> Collection<LocationSpec<T>> getLocation(Collection<T> specs) {
		Collection<LocationSpec<T>> locationSpecs = new HashSet<LocationSpec<T>>();
		for (T spec : specs) {
			LocationSpec<T> locationSpec = locationDatabase.get(spec);
			if (locationSpec == null) {
				locationRetriever.queueLocationRetrieval(spec);
			} else {
				locationSpecs.add(locationSpec);
			}
		}
		return locationSpecs;
	}

	public static class CollectionSizeComparator implements Comparator<Collection<LocationSpec<WlanSpec>>> {
		public static CollectionSizeComparator INSTANCE = new CollectionSizeComparator();

		@Override
		public int compare(Collection<LocationSpec<WlanSpec>> left, Collection<LocationSpec<WlanSpec>> right) {
			return (left.size() < right.size()) ? -1 : ((left.size() > right.size()) ? 1 : 0);
		}
	}
}

