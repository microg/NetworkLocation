package com.apple.iphone.services;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Collection;

import javax.net.ssl.HttpsURLConnection;

import com.apple.iphone.services.LocationsProtos.Request;
import com.apple.iphone.services.LocationsProtos.RequestWLAN;
import com.apple.iphone.services.LocationsProtos.Response;

public class LocationRetriever {
	private LocationRetriever() {

	}

	private static final String SERVICE_URL = "https://iphone-services.apple.com/clls/wloc";

	public static HttpsURLConnection createConnection() throws IOException {
		return createConnection(SERVICE_URL);
	}

	public static HttpsURLConnection createConnection(String url)
			throws MalformedURLException, IOException {
		return createConnection(new URL(url));
	}

	public static HttpsURLConnection createConnection(URL url)
			throws IOException {
		return (HttpsURLConnection) url.openConnection();
	}

	public static void prepareConnection(HttpsURLConnection connection,
			int length) throws ProtocolException {
		connection.setRequestMethod("POST");
		connection.setDoInput(true);
		connection.setDoOutput(true);
		connection.setUseCaches(false);
		connection.setRequestProperty("Content-Type",
				"application/x-www-form-urlencoded");
		connection.setRequestProperty("Content-Length", String.valueOf(length));
	}

	public static Request createRequest(String... macs) {
		Request.Builder builder = Request.newBuilder()
				.setSource("com.apple.maps").setUnknown3(0).setUnknown4(0);
		for (String mac : macs) {
			builder.addWlan(RequestWLAN.newBuilder().setMac(mac));
		}
		return builder.build();
	}

	public static byte[] combineBytes(byte[] first, byte[] second, byte divider) {
		byte[] bytes = new byte[first.length + second.length + 1];
		for (int i = 0; i < first.length; i++) {
			bytes[i] = first[i];
		}
		bytes[first.length] = (byte) second.length;
		for (int i = 0; i < second.length; i++) {
			bytes[i + first.length + 1] = second[i];
		}
		return bytes;
	}

	public static final byte[] firstBytes = new byte[] { 0, 1, 0, 5, 101, 110,
			95, 85, 83, 0, 0, 0, 11, 52, 46, 50, 46, 49, 46, 56, 67, 49, 52,
			56, 0, 0, 0, 1, 0, 0, 0 };

	public static Response retrieveLocations(Collection<String> macs)
			throws MalformedURLException, ProtocolException, IOException {
		return retrieveLocations(macs.toArray(new String[macs.size()]));
	}

	public static Response retrieveLocations(String... macs)
			throws IOException, ProtocolException {
		Request request = createRequest(macs);
		byte[] byteb = request.toByteArray();
		byte[] bytes = combineBytes(firstBytes, byteb, (byte) byteb.length);
		HttpsURLConnection connection = createConnection();
		prepareConnection(connection, bytes.length);
		OutputStream out = connection.getOutputStream();
		out.write(bytes);
		out.flush();
		out.close();
		InputStream in = connection.getInputStream();
		int i = 0;
		in.skip(10);
		Response response = Response.parseFrom(in);
		in.close();
		return response;
	}
}
