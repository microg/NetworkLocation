package org.microg.networklocation.data;

import android.location.Location;

public class LocationSpec<T extends PropSpec> {
	private static final double EARTH_RADIUS = 6367;
	private T source;
	private boolean undefined = true;
	private boolean remote = true;
	private boolean submitted = false;
	private double latitude = 0;
	private double longitude = 0;
	private double altitude = 0;
	private boolean hasAltitude = false;
	private double accuracy = 0;

	LocationSpec(T source, Location location) {
		this.source = source;
		latitude = location.getLatitude();
		longitude = location.getLongitude();
		altitude = location.getAltitude();
		accuracy = location.getAccuracy();
	}

	public LocationSpec(T source, double latitude, double longitude, double accuracy) {
		this(latitude, longitude, accuracy);
		this.source = source;
	}

	public LocationSpec(T source, double latitude, double longitude, double accuracy, double altitude) {
		this(source, latitude, longitude, accuracy);
		this.altitude = altitude;
		if (altitude != 0) {
			hasAltitude = true;
		}
	}

	public LocationSpec(double latitude, double longitude, double accuracy) {
		this.latitude = latitude;
		this.longitude = longitude;
		this.accuracy = accuracy;
		if (((latitude != 0) || (longitude != 0)) && (accuracy != 0)) {
			undefined = false;
		}
	}

	public LocationSpec(double latitude, double longitude, double accuracy, double altitude, boolean remote,
	                    boolean submitted) {
		this(latitude, longitude, accuracy, altitude);
		this.remote = remote;
		this.submitted = submitted;
	}

	public LocationSpec(double latitude, double longitude, double accuracy, double altitude, int bools) {
		this(latitude, longitude, accuracy, altitude);
		setBools(bools);
	}

	LocationSpec(T source) {
		this.source = source;
		undefined = true;
	}

	public LocationSpec(double latitude, double longitude, double accuracy, double altitude) {
		this(latitude, longitude, accuracy);
		this.altitude = altitude;
	}

	private static int boolToInt(boolean b, int s) {
		return b ? (1 << s) : 0;
	}

	private static int boolToInt(boolean b) {
		return boolToInt(b, 0);
	}

	private static double degToRad(double deg) {
		return (deg * Math.PI) / 180;
	}

	private static boolean intToBool(int i, int s) {
		return ((i >> s) & 1) == 1;
	}

	public <S extends PropSpec> double distanceTo(LocationSpec<S> other) {
		return distanceTo(other.getLatitude(), other.getLongitude());
	}

	private double distanceTo(double latitude, double longitude) {
		double lat1 = degToRad(getLatitude());
		double lon1 = degToRad(getLongitude());
		double lat2 = degToRad(latitude);
		double lon2 = degToRad(longitude);
		double dLat = lat2 - lat1;
		double dLon = lon2 - lon1;
		double cordLength =
				Math.pow(Math.sin(dLat / 2), 2) + (Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin(dLon / 2), 2));
		double centralAngle = 2 * Math.atan2(Math.sqrt(cordLength), Math.sqrt(1 - cordLength));
		return EARTH_RADIUS * centralAngle;
	}

	public double distanceTo(Location location) {
		return distanceTo(location.getLatitude(), location.getLongitude());
	}

	public double getAccuracy() {
		return accuracy;
	}

	public double getAltitude() {
		return altitude;
	}

	public int getBools() {
		return boolToInt(hasAltitude, 3) + boolToInt(undefined, 2) + boolToInt(remote, 1) +
			   boolToInt(submitted);
	}

	private void setBools(int bools) {
		hasAltitude = intToBool(bools, 3);
		undefined = intToBool(bools, 2);
		remote = intToBool(bools, 1);
		submitted = intToBool(bools, 0);
	}

	public double getLatitude() {
		return latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public T getSource() {
		return source;
	}

	public void setSource(T source) {
		this.source = source;
	}

	public boolean isRemote() {
		return remote;
	}

	public boolean isSubmitted() {
		return submitted;
	}

	public boolean isUndefined() {
		return undefined;
	}

	@Override
	public String toString() {
		return "LocationSpec{" +
			   "source=" + source +
			   ", latitude=" + latitude +
			   ", longitude=" + longitude +
			   ", altitude=" + altitude +
			   ", accuracy=" + accuracy +
			   ", bools=" + getBools() +
			   '}';
	}
}
