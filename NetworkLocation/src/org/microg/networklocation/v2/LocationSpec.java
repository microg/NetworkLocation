package org.microg.networklocation.v2;

import android.location.Location;

public class LocationSpec {
	private PropSpec source;
	private boolean undefined = false;
	private double latitude;
	private double longitude;
	private double altitude;
	private double accuracy;

	LocationSpec(PropSpec source, Location location) {
		this.source = source;
		latitude = location.getLatitude();
		longitude = location.getLongitude();
		altitude = location.getAltitude();
		accuracy = location.getAccuracy();
	}

	LocationSpec(PropSpec source) {
		this.source = source;
		undefined = true;
	}

	public PropSpec getSource() {
		return source;
	}
}
