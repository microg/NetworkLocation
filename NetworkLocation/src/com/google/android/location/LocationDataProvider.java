package com.google.android.location;

import java.util.Collection;
import java.util.Date;

import android.location.Location;

public interface LocationDataProvider {

	public class Stub implements LocationDataProvider {

		public static final String DEFAULT_IDENTIFIER = "unknown";

		public android.location.Location calculateLocation(
				final Collection<Location> values) {
			if (values == null || values.size() == 0) {
				return null;
			}
			float lat = 0;
			float lon = 0;
			float acc = 0;
			float minacc = Float.MAX_VALUE;
			int n = 0;
			for (final Location location : values) {
				if (location.getAccuracy() != -1
						&& location.getAccuracy() < 1000) {
					lat += location.getLatitude();
					lon += location.getLongitude();
					acc += location.getAccuracy();
					minacc = Math.min(minacc, location.getAccuracy());
					n++;
				}
			}
			final Location loc = new android.location.Location(getIdentifier());
			loc.setAccuracy(Math.max(42,
					Math.min((float) Math.exp(1 - n) * acc / n, minacc)));
			loc.setLatitude(lat / n);
			loc.setLongitude(lon / n);
			loc.setTime(new Date().getTime());
			return loc;
		}

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
