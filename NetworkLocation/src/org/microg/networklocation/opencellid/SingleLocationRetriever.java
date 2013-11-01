package org.microg.networklocation.opencellid;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class SingleLocationRetriever {
	private static final String SERVICE_URL = "http://www.opencellid.org/cell/get?fmt=txt";

	private SingleLocationRetriever() {

	}

	private static byte[] readStreamToEnd(final InputStream is) throws IOException {
		final ByteArrayOutputStream bos = new ByteArrayOutputStream();
		if (is != null) {
			final byte[] buff = new byte[1024];
			while (true) {
				final int nb = is.read(buff);
				if (nb < 0) {
					break;
				}
				bos.write(buff, 0, nb);
			}
			is.close();
		}
		return bos.toByteArray();
	}

	public static Response retrieveLocation(int mcc, int mnc, int cellid) {
		try {
			URL url = new URL(SERVICE_URL + "&mcc=" + mcc + "&mnc=" + mnc + "&cellid=" + cellid);
			URLConnection urlConnection = url.openConnection();
			urlConnection.setDoInput(true);
			InputStream inputStream = urlConnection.getInputStream();
			String result = new String(readStreamToEnd(inputStream));
			String[] split = result.split(",");
			return new Response(Double.parseDouble(split[0]), Double.parseDouble(split[1]), Float.parseFloat(split[2]));
		} catch (IOException e) {
			return null;
		}
	}

	public static class Response {
		private double latitude;
		private double longitude;
		private float accuracy;

		public Response(double latitude, double longitude, float accuracy) {
			this.latitude = latitude;
			this.longitude = longitude;
			this.accuracy = accuracy;
		}

		public float getAccuracy() {
			return accuracy;
		}

		public double getLatitude() {
			return latitude;
		}

		public double getLongitude() {
			return longitude;
		}
	}
}
