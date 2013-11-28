package org.microg.networklocation.data;

import android.location.Location;

public class LocationSpec<T extends PropSpec> {
	private static final double EARTH_RADIUS = 6367;
	private T source;
	private boolean undefined = false;
	private double latitude;
	private double longitude;
	private double altitude;
	private double accuracy;

	LocationSpec(T source, Location location) {
		this.source = source;
		latitude = location.getLatitude();
		longitude = location.getLongitude();
		altitude = location.getAltitude();
		accuracy = location.getAccuracy();
	}

	public LocationSpec(T source, double latitude, double longitude, double accuracy) {
		this.source = source;
		this.latitude = latitude;
		this.longitude = longitude;
		this.accuracy = accuracy;
	}

	public LocationSpec(double latitude, double longitude, double accuracy) {
		this.latitude = latitude;
		this.longitude = longitude;
		this.accuracy = accuracy;
	}

	LocationSpec(T source) {
		this.source = source;
		undefined = true;
	}

	private static double degToRad(double deg) {
		return (deg * Math.PI) / 180;
	}

	public <S extends PropSpec> double distanceBetween(LocationSpec<S> other) {
		return distanceBetween(other.getLatitude(), other.getLongitude());
	}

	private double distanceBetween(double latitude, double longitude) {
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

	public double distanceBetween(Location location) {
		return distanceBetween(location.getLatitude(), location.getLongitude());
	}

	public double getAccuracy() {
		return accuracy;
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
}
