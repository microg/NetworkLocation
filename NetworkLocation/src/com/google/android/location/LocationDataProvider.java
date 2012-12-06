package com.google.android.location;

import java.util.Date;

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

		protected Location renameSource(final Location location) {
			return renameSource(location, getIdentifier());
		}

		protected Location renameSource(final Location location,
				final String source) {
			return renameSource(location, source, new Date().getTime());
		}

		protected Location renameSource(Location location, final String source,
				final long time) {
			if (location == null) {
				return null;
			}
			location = new Location(location);
			location.setProvider(source);
			location.setTime(time);
			return location;
		}

	}

	public android.location.Location getCurrentLocation();

	public String getIdentifier();
}
