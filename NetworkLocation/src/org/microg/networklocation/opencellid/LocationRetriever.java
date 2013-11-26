package org.microg.networklocation.opencellid;

import org.microg.networklocation.source.OnlineCellLocationRetriever;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class LocationRetriever implements OnlineCellLocationRetriever {
	private static final String SERVICE_URL = "http://www.opencellid.org/cell/get?fmt=txt";

	LocationRetriever() {

	}

	private static byte[] readStreamToEnd(InputStream is) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		if (is != null) {
			byte[] buff = new byte[1024];
			while (true) {
				int nb = is.read(buff);
				if (nb < 0) {
					break;
				}
				bos.write(buff, 0, nb);
			}
			is.close();
		}
		return bos.toByteArray();
	}

	public Response retrieveCellLocation(int mcc, int mnc, int lac, int cellid) {
		try {
			URL url = new URL(SERVICE_URL + "&mcc=" + mcc + "&mnc=" + mnc + "&lac=" + lac + "&cellid=" + cellid);
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
}
