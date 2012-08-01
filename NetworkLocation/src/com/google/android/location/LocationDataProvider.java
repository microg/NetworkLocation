package com.google.android.location;

import android.location.Location;

public interface LocationDataProvider {

	public class Stub implements LocationDataProvider {

		public static final String DEFAULT_IDENTIFIER = "unknown";

		@Override
		public Location getCurrentLocation() {
			return null;
		}

		@Override
		public String getIdentifier() {
			return DEFAULT_IDENTIFIER;
		}

		protected Location renameSource(Location location) {
			return renameSource(location, getIdentifier());
		}

		protected Location renameSource(Location location, String source) {
			if (location == null) return null;
			location = new Location(location);
			location.setProvider(source);
			return location;
		}

	}

	public android.location.Location getCurrentLocation();

	public String getIdentifier();
}
