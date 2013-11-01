package org.microg.networklocation.data;

import android.location.Location;

import java.util.Collection;
import java.util.Date;

public interface LocationDataProvider {

	public class Stub implements LocationDataProvider {

		public static final String DEFAULT_IDENTIFIER = "unknown";

		public static Location renameSource(final Location location, final String source) {
			return renameSource(location, source, new Date().getTime());
		}

		protected static Location renameSource(Location location, final String source, final long time) {
			if (location == null) {
				return null;
			}
			location = new Location(location);
			location.setProvider(source);
			location.setTime(time);
			return location;
		}

		public android.location.Location calculateLocation(final Collection<Location> values, final int minAccuracy) {
			if (values == null || values.size() == 0) {
				return null;
			}
			float lat = 0;
			float lon = 0;
			float acc = 0;
			float minacc = Float.MAX_VALUE;
			int n = 0;
			for (final Location location : values) {
				if (location != null && location.getAccuracy() != -1 && location.getAccuracy() < minAccuracy) {
					lat += location.getLatitude();
					lon += location.getLongitude();
					acc += location.getAccuracy();
					minacc = Math.min(minacc, location.getAccuracy());
					n++;
				}
			}
			if (n == 0) {
				return null;
			}
			final Location loc = new android.location.Location(getIdentifier());
			loc.setAccuracy(Math.max(42, Math.min((float) Math.exp(1 - n) * acc / n, minacc)));
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

	}

	public android.location.Location getCurrentLocation();

	public String getIdentifier();
}
