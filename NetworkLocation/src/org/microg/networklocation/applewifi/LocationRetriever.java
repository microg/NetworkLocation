package org.microg.networklocation.applewifi;

import javax.net.ssl.HttpsURLConnection;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Collection;

public class LocationRetriever {
	public static final byte[] firstBytes =
			new byte[]{0, 1, 0, 5, 101, 110, 95, 85, 83, 0, 0, 0, 11, 52, 46, 50, 46, 49, 46, 56, 67, 49, 52, 56, 0, 0,
					   0, 1, 0, 0, 0};
	private static final String SERVICE_URL = "https://iphone-services.apple.com/clls/wloc";

	private LocationRetriever() {

	}

	private static byte[] combineBytes(final byte[] first, final byte[] second, final byte divider) {
		final byte[] bytes = new byte[first.length + second.length + 1];
		for (int i = 0; i < first.length; i++) {
			bytes[i] = first[i];
		}
		bytes[first.length] = divider;
		for (int i = 0; i < second.length; i++) {
			bytes[i + first.length + 1] = second[i];
		}
		return bytes;
	}

	private static HttpsURLConnection createConnection() throws IOException {
		return createConnection(SERVICE_URL);
	}

	private static HttpsURLConnection createConnection(final String url) throws MalformedURLException, IOException {
		return createConnection(new URL(url));
	}

	private static HttpsURLConnection createConnection(final URL url) throws IOException {
		return (HttpsURLConnection) url.openConnection();
	}

	private static Location.Request createRequest(final String... macs) {
		final Location.Request builder = new Location.Request().setSource("com.apple.maps").setUnknown3(0).setUnknown4(0);
		for (final String mac : macs) {
			builder.addWlan(new Location.RequestWLAN().setMac(mac));
		}
		return builder;
	}

	private static void prepareConnection(final HttpsURLConnection connection, final int length)
			throws ProtocolException {
		connection.setRequestMethod("POST");
		connection.setDoInput(true);
		connection.setDoOutput(true);
		connection.setUseCaches(false);
		connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		connection.setRequestProperty("Content-Length", String.valueOf(length));
	}

	public static Location.Response retrieveLocations(final Collection<String> macs)
			throws MalformedURLException, ProtocolException, IOException {
		return retrieveLocations(macs.toArray(new String[macs.size()]));
	}

	public static Location.Response retrieveLocations(final String... macs) throws IOException {
		Location.Request request = createRequest(macs);
		byte[] byteb = request.toByteArray();
		byte[] bytes = combineBytes(firstBytes, byteb, (byte) byteb.length);
		HttpsURLConnection connection = createConnection();
		prepareConnection(connection, bytes.length);
		OutputStream out = connection.getOutputStream();
		out.write(bytes);
		out.flush();
		out.close();
		InputStream in = connection.getInputStream();
		in.skip(10);
		Location.Response response = Location.Response.parseFrom(readStreamToEnd(in));
		in.close();
		return response;
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
}
